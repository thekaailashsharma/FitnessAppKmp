package org.awi.fitness.repository

import org.awi.fitness.utils.currentTimeMillis
import org.awi.fitness.utils.toFirestoreDocId
import io.ktor.http.isSuccess
import kotlinx.datetime.Clock
import org.awi.fitness.model.*
import org.awi.fitness.network.ApiService

class ChallengesRepository : ApiService() {

    companion object {
        private const val PROJECT_ID = "fitness-admin-73a72"
        private const val BASE_URL =
            "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents"
        private const val CHALLENGES_COLLECTION = "fitness_testing_challenges"
        private const val TEMPLATES_COLLECTION = "fitness_testing_challenge_templates"
    }

    // ---------------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------------

    /** Challenges the current user has already joined. */
    suspend fun getActiveChallenges(): Result<List<Challenge>> = runCatching {
        val token = userSettings.authToken ?: error("Not authenticated")
        val email = userSettings.userEmail ?: error("No user email")

        val (response, status) = get<FirestoreListResponse<ChallengeFields>>(
            "$BASE_URL/$CHALLENGES_COLLECTION?pageSize=50", token
        )
        if (!status.isSuccess()) error("Failed to fetch challenges: $status")

        val docs = response.documents ?: return@runCatching emptyList()

        docs
            .filter { doc ->
                val ids = doc.fields?.participantIds?.arrayValue?.values?.map { it.value } ?: emptyList()
                email in ids
            }
            .map { doc ->
                val progress = fetchUserProgress(doc.name?.split("/")?.last() ?: "", email, token)
                doc.toChallenge(email, progress)
            }
    }

    /** Available challenge templates + public challenges the user has NOT joined yet. */
    suspend fun getAvailableChallenges(): Result<List<Challenge>> = runCatching {
        val token = userSettings.authToken ?: error("Not authenticated")
        val email = userSettings.userEmail ?: error("No user email")

        // Fetch templates
        val (templatesResp, templatesStatus) = get<FirestoreListResponse<ChallengeFields>>(
            "$BASE_URL/$TEMPLATES_COLLECTION?pageSize=50", token
        )
        val templateChallenges = if (templatesStatus.isSuccess()) {
            templatesResp.documents
                ?.filter { it.fields?.isActive?.value != false }
                ?.map { it.toChallenge(email, 0) }
                ?: emptyList()
        } else emptyList()

        // Fetch public challenges the user hasn't joined
        val (challengesResp, challengesStatus) = get<FirestoreListResponse<ChallengeFields>>(
            "$BASE_URL/$CHALLENGES_COLLECTION?pageSize=50", token
        )
        val publicUnjoined = if (challengesStatus.isSuccess()) {
            challengesResp.documents
                ?.filter { doc ->
                    val ids = doc.fields?.participantIds?.arrayValue?.values?.map { it.value } ?: emptyList()
                    (doc.fields?.isPublic?.value ?: true) && email !in ids
                }
                ?.map { it.toChallenge(email, 0) }
                ?: emptyList()
        } else emptyList()

        templateChallenges + publicUnjoined
    }

    /** Add the current user to a challenge's participantIds and create their progress doc.
     *  If the challengeId belongs to a template (not in challenges collection), we first
     *  copy the template into the challenges collection, then join.
     */
    suspend fun joinChallenge(challengeId: String): Result<Unit> = runCatching {
        val token = userSettings.authToken ?: error("Not authenticated")
        val email = userSettings.userEmail ?: error("No user email")

        // 1. Try to read from challenges collection first
        val challengeDocUrl = "$BASE_URL/$CHALLENGES_COLLECTION/$challengeId"
        val (challengeResp, challengeStatus) = get<FirestoreResponse<ChallengeFields>>(challengeDocUrl, token)

        val (realChallengeId, fields) = if (challengeStatus.isSuccess() && challengeResp.fields != null) {
            // Already exists in challenges collection
            challengeId to challengeResp.fields!!
        } else {
            // Not in challenges — try template collection
            val templateUrl = "$BASE_URL/$TEMPLATES_COLLECTION/$challengeId"
            val (templateResp, templateStatus) = get<FirestoreResponse<ChallengeFields>>(templateUrl, token)
            if (!templateStatus.isSuccess() || templateResp.fields == null) {
                error("Challenge not found in either collection")
            }
            // Copy template to challenges collection
            val copyBody = ChallengeFirestoreRequest(
                fields = templateResp.fields!!.copy(
                    participantIds = ArrayValueWrapper(arrayValue = ArrayValue(emptyList()))
                )
            )
            val (newDoc, newStatus) = post<FirestoreResponse<ChallengeFields>>(
                "$BASE_URL/$CHALLENGES_COLLECTION", copyBody, token
            )
            if (!newStatus.isSuccess()) error("Failed to create challenge from template")
            val newId = newDoc.name?.split("/")?.last() ?: error("No doc ID returned")
            newId to templateResp.fields!!
        }

        // 2. Read current participantIds on the real challenge doc
        val realDocUrl = "$BASE_URL/$CHALLENGES_COLLECTION/$realChallengeId"
        val (currentDocResp, _) = get<FirestoreResponse<ChallengeFields>>(realDocUrl, token)
        val currentIds = currentDocResp.fields?.participantIds?.arrayValue?.values
            ?.map { it.value } ?: emptyList()

        if (email in currentIds) return@runCatching Unit // already joined

        val newIds = currentIds + email
        val targetValue = fields.targetValue?.value?.toIntOrNull() ?: 1

        // 3. PATCH participantIds
        val patchUrl = "$realDocUrl?updateMask.fieldPaths=participantIds"
        val patchBody = ChallengeParticipantsUpdateRequest(
            fields = ChallengeParticipantsFields(
                participantIds = ArrayValueWrapper(
                    arrayValue = ArrayValue(values = newIds.map { StringValue(it) })
                )
            )
        )
        val (_, patchStatus) = patch<FirestoreResponse<ChallengeFields>>(patchUrl, patchBody, token)
        if (!patchStatus.isSuccess()) error("Failed to update participants")

        // 4. Create progress doc
        val now = currentTimeMillis()
        val safeEmail = email.toFirestoreDocId()
        val progressUrl = "$BASE_URL/$CHALLENGES_COLLECTION/$realChallengeId/progress/$safeEmail"
        val progressBody = ChallengeProgressFirestoreRequest(
            fields = ChallengeProgressFields(
                challengeId = StringValue(realChallengeId),
                userId = StringValue(email),
                currentValue = IntegerValue("0"),
                targetValue = IntegerValue(targetValue.toString()),
                joinedAt = IntegerValue(now.toString()),
                displayName = StringValue(email.substringBefore("@"))
            )
        )
        patch<FirestoreResponse<ChallengeProgressFields>>(progressUrl, progressBody, token)
    }

    /** Update the current user's progress for a challenge. Awards XP when target is hit. */
    suspend fun updateProgress(challengeId: String, newValue: Int): Result<Unit> = runCatching {
        val token = userSettings.authToken ?: error("Not authenticated")
        val email = userSettings.userEmail ?: error("No user email")
        val safeEmail = email.toFirestoreDocId()

        val progressUrl = "$BASE_URL/$CHALLENGES_COLLECTION/$challengeId/progress/$safeEmail"

        // Read current progress to know targetValue
        val (existingDoc, _) = get<FirestoreResponse<ChallengeProgressFields>>(progressUrl, token)
        val targetValue = existingDoc.fields?.targetValue?.value?.toIntOrNull() ?: newValue

        val now = currentTimeMillis()
        val isCompleted = newValue >= targetValue

        val completedAtStr = if (isCompleted) now.toString() else null

        val patchUrl = buildString {
            append(progressUrl)
            append("?updateMask.fieldPaths=currentValue")
            if (isCompleted) append("&updateMask.fieldPaths=completedAt")
        }
        val progressBody = ChallengeProgressFirestoreRequest(
            fields = ChallengeProgressFields(
                currentValue = IntegerValue(newValue.toString()),
                completedAt = completedAtStr?.let { IntegerValue(it) }
            )
        )
        val (_, status) = patch<FirestoreResponse<ChallengeProgressFields>>(patchUrl, progressBody, token)
        if (!status.isSuccess()) error("Failed to update progress")

        if (isCompleted) {
            awardXp(challengeId, token)
        }
    }

    /** Leaderboard for a specific challenge (all participants' progress, sorted by value). */
    suspend fun getLeaderboard(challengeId: String): Result<List<LeaderboardEntry>> = runCatching {
        val token = userSettings.authToken ?: error("Not authenticated")

        val url = "$BASE_URL/$CHALLENGES_COLLECTION/$challengeId/progress?pageSize=100"
        val (response, status) = get<FirestoreListResponse<ChallengeProgressFields>>(url, token)
        if (!status.isSuccess()) return@runCatching emptyList()

        (response.documents ?: emptyList())
            .sortedByDescending { it.fields?.currentValue?.value?.toIntOrNull() ?: 0 }
            .mapIndexed { index, doc -> doc.toLeaderboardEntry(index + 1) }
    }

    /** Create a new challenge (public). */
    suspend fun createChallenge(challenge: Challenge): Result<Challenge> = runCatching {
        val token = userSettings.authToken ?: error("Not authenticated")
        val email = userSettings.userEmail ?: error("No user email")
        val now = currentTimeMillis()

        val body = ChallengeFirestoreRequest(
            fields = ChallengeFields(
                title = StringValue(challenge.title),
                description = StringValue(challenge.description),
                type = StringValue(challenge.type.name),
                targetType = StringValue(challenge.targetType.name),
                targetValue = IntegerValue(challenge.targetValue.toString()),
                xpReward = IntegerValue(challenge.xpReward.toString()),
                badgeIcon = StringValue(challenge.badgeIcon.ifBlank { "🏆" }),
                startDate = IntegerValue(challenge.startDateLong.takeIf { it > 0 }?.toString() ?: now.toString()),
                endDate = IntegerValue(challenge.endDateLong.toString()),
                createdBy = StringValue(email),
                isPublic = BooleanValue(challenge.isPublic),
                isActive = BooleanValue(true),
                participantIds = ArrayValueWrapper(arrayValue = ArrayValue(emptyList()))
            )
        )
        val (response, status) = post<FirestoreResponse<ChallengeFields>>(
            "$BASE_URL/$CHALLENGES_COLLECTION", body, token
        )
        if (!status.isSuccess()) error("Failed to create challenge")

        val docId = response.name?.split("/")?.last() ?: ""
        challenge.copy(id = docId)
    }

    /**
     * Seed 5 system challenge templates into Firestore if the collection is empty.
     * Safe to call every time the challenges screen loads.
     */
    suspend fun seedSystemTemplatesIfNeeded(): Result<Unit> = runCatching {
        val token = userSettings.authToken ?: error("Not authenticated")

        val (response, status) = get<FirestoreListResponse<ChallengeFields>>(
            "$BASE_URL/$TEMPLATES_COLLECTION?pageSize=1", token
        )
        if (status.isSuccess() && !response.documents.isNullOrEmpty()) return@runCatching Unit

        val now = currentTimeMillis()
        val week = 7L * 24 * 60 * 60 * 1000
        val month = 30L * 24 * 60 * 60 * 1000

        val templates = listOf(
            ChallengeFirestoreRequest(
                fields = ChallengeFields(
                    title = StringValue("7-Day Workout Streak"),
                    description = StringValue("Complete 7 workouts in 7 days and build an unstoppable habit."),
                    type = StringValue("WEEKLY"),
                    targetType = StringValue("WORKOUTS"),
                    targetValue = IntegerValue("7"),
                    xpReward = IntegerValue("500"),
                    badgeIcon = StringValue("🔥"),
                    startDate = IntegerValue(now.toString()),
                    endDate = IntegerValue((now + week).toString()),
                    createdBy = StringValue("system"),
                    isPublic = BooleanValue(true),
                    isActive = BooleanValue(true),
                    participantIds = ArrayValueWrapper(arrayValue = ArrayValue(emptyList()))
                )
            ),
            ChallengeFirestoreRequest(
                fields = ChallengeFields(
                    title = StringValue("Calorie Goal Week"),
                    description = StringValue("Stay within your calorie goal for 5 out of 7 days this week."),
                    type = StringValue("WEEKLY"),
                    targetType = StringValue("CALORIES"),
                    targetValue = IntegerValue("5"),
                    xpReward = IntegerValue("300"),
                    badgeIcon = StringValue("🥗"),
                    startDate = IntegerValue(now.toString()),
                    endDate = IntegerValue((now + week).toString()),
                    createdBy = StringValue("system"),
                    isPublic = BooleanValue(true),
                    isActive = BooleanValue(true),
                    participantIds = ArrayValueWrapper(arrayValue = ArrayValue(emptyList()))
                )
            ),
            ChallengeFirestoreRequest(
                fields = ChallengeFields(
                    title = StringValue("Daily Active Day"),
                    description = StringValue("Complete at least 1 workout today and start your streak."),
                    type = StringValue("DAILY"),
                    targetType = StringValue("WORKOUTS"),
                    targetValue = IntegerValue("1"),
                    xpReward = IntegerValue("100"),
                    badgeIcon = StringValue("⚡"),
                    startDate = IntegerValue(now.toString()),
                    endDate = IntegerValue((now + week).toString()),
                    createdBy = StringValue("system"),
                    isPublic = BooleanValue(true),
                    isActive = BooleanValue(true),
                    participantIds = ArrayValueWrapper(arrayValue = ArrayValue(emptyList()))
                )
            ),
            ChallengeFirestoreRequest(
                fields = ChallengeFields(
                    title = StringValue("Protein Champion"),
                    description = StringValue("Log your meals every day for 7 consecutive days."),
                    type = StringValue("WEEKLY"),
                    targetType = StringValue("STREAK"),
                    targetValue = IntegerValue("7"),
                    xpReward = IntegerValue("400"),
                    badgeIcon = StringValue("💪"),
                    startDate = IntegerValue(now.toString()),
                    endDate = IntegerValue((now + week).toString()),
                    createdBy = StringValue("system"),
                    isPublic = BooleanValue(true),
                    isActive = BooleanValue(true),
                    participantIds = ArrayValueWrapper(arrayValue = ArrayValue(emptyList()))
                )
            ),
            ChallengeFirestoreRequest(
                fields = ChallengeFields(
                    title = StringValue("Ultimate Kickoff"),
                    description = StringValue("Complete 10 workouts in 30 days to prove you've got what it takes."),
                    type = StringValue("CUSTOM"),
                    targetType = StringValue("WORKOUTS"),
                    targetValue = IntegerValue("10"),
                    xpReward = IntegerValue("1000"),
                    badgeIcon = StringValue("🏆"),
                    startDate = IntegerValue(now.toString()),
                    endDate = IntegerValue((now + month).toString()),
                    createdBy = StringValue("system"),
                    isPublic = BooleanValue(true),
                    isActive = BooleanValue(true),
                    participantIds = ArrayValueWrapper(arrayValue = ArrayValue(emptyList()))
                )
            ),
            ChallengeFirestoreRequest(
                fields = ChallengeFields(
                    title = StringValue("Community Sharer"),
                    description = StringValue("Share 5 posts with the community this week. Inspire others!"),
                    type = StringValue("WEEKLY"),
                    targetType = StringValue("POSTS"),
                    targetValue = IntegerValue("5"),
                    xpReward = IntegerValue("250"),
                    badgeIcon = StringValue("✍️"),
                    startDate = IntegerValue(now.toString()),
                    endDate = IntegerValue((now + week).toString()),
                    createdBy = StringValue("system"),
                    isPublic = BooleanValue(true),
                    isActive = BooleanValue(true),
                    participantIds = ArrayValueWrapper(arrayValue = ArrayValue(emptyList()))
                )
            ),
            ChallengeFirestoreRequest(
                fields = ChallengeFields(
                    title = StringValue("Meal Master"),
                    description = StringValue("Log all your meals for 5 consecutive days."),
                    type = StringValue("WEEKLY"),
                    targetType = StringValue("MEALS"),
                    targetValue = IntegerValue("15"),
                    xpReward = IntegerValue("350"),
                    badgeIcon = StringValue("🥗"),
                    startDate = IntegerValue(now.toString()),
                    endDate = IntegerValue((now + week).toString()),
                    createdBy = StringValue("system"),
                    isPublic = BooleanValue(true),
                    isActive = BooleanValue(true),
                    participantIds = ArrayValueWrapper(arrayValue = ArrayValue(emptyList()))
                )
            )
        )

        for (template in templates) {
            post<FirestoreResponse<ChallengeFields>>(
                "$BASE_URL/$TEMPLATES_COLLECTION", template, token
            )
        }
    }

    // ---------------------------------------------------------------------------
    // Helpers accessible to ViewModel
    // ---------------------------------------------------------------------------

    fun currentUserEmail(): String = userSettings.userEmail ?: ""

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    private suspend fun fetchUserProgress(
        challengeId: String,
        email: String,
        token: String
    ): Int = try {
        val safeEmail = email.toFirestoreDocId()
        val url = "$BASE_URL/$CHALLENGES_COLLECTION/$challengeId/progress/$safeEmail"
        val (response, status) = get<FirestoreResponse<ChallengeProgressFields>>(url, token)
        if (status.isSuccess()) {
            response.fields?.currentValue?.value?.toIntOrNull() ?: 0
        } else 0
    } catch (e: Exception) {
        0
    }

    private suspend fun awardXp(challengeId: String, token: String) {
        try {
            val now = currentTimeMillis()
            val xpEntry = ChallengeFirestoreRequest(
                fields = ChallengeFields(
                    createdBy = StringValue(challengeId),
                    startDate = IntegerValue(now.toString())
                )
            )
            post<FirestoreResponse<ChallengeFields>>(
                "$BASE_URL/fitness_testing_xp_log", xpEntry, token
            )
        } catch (e: Exception) {
            // XP log is best-effort; don't fail the update
        }
    }
}

private fun String.toFirestoreDocId(): String = toFirestoreDocId(this)

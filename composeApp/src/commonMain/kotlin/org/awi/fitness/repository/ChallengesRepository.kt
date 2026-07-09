package org.awi.fitness.repository

import org.awi.fitness.utils.currentTimeMillis
import org.awi.fitness.utils.toFirestoreDocId
import io.ktor.http.isSuccess
import kotlinx.datetime.Clock
import org.awi.fitness.model.*
import org.awi.fitness.network.ApiService

class ChallengesRepository : ApiService() {

    companion object {
        private const val PROJECT_ID = "awi-fitness-app"
        private const val BASE_URL =
            "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents"
        private const val CHALLENGES_COLLECTION = "fitness_testing_challenges"
        private const val TEMPLATES_COLLECTION = "fitness_testing_challenge_templates"
        private const val USERS_COLLECTION = "fitness_testing_users"
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

        // Read current progress to know targetValue + whether it was already completed.
        val (existingDoc, _) = get<FirestoreResponse<ChallengeProgressFields>>(progressUrl, token)
        val targetValue = existingDoc.fields?.targetValue?.value?.toIntOrNull() ?: newValue
        val wasCompleted = existingDoc.fields?.completedAt?.value?.toLongOrNull() != null

        val now = currentTimeMillis()
        val isCompleted = newValue >= targetValue
        val justCompleted = isCompleted && !wasCompleted

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

        // Award the challenge's real XP exactly once, on the completion transition.
        if (justCompleted) {
            val xpReward = fetchChallengeXpReward(challengeId, token)
            userSettings.recordChallengeCompleted(xpReward)
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
            .map { entry ->
                // Enrich with the participant's REAL level + streak from their user doc.
                val (level, streak) = fetchUserLevelStreak(entry.userId, token)
                entry.copy(level = level, streak = streak)
            }
    }

    /**
     * Seed system challenge templates into Firestore if the collection is empty.
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

        // Only auto-progressing types: WORKOUTS (workout completion), MEALS (meal completion),
        // POSTS (community posts), STREAK (daily check-in / workout). No CALORIE or STEP types —
        // the app has no tracking source for them, so every challenge here actually moves.
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
                    difficulty = StringValue("INTERMEDIATE"),
                    durationDays = IntegerValue("7"),
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
                    difficulty = StringValue("BEGINNER"),
                    durationDays = IntegerValue("1"),
                    participantIds = ArrayValueWrapper(arrayValue = ArrayValue(emptyList()))
                )
            ),
            ChallengeFirestoreRequest(
                fields = ChallengeFields(
                    title = StringValue("7-Day Consistency"),
                    description = StringValue("Check in or complete a workout every day for 7 days in a row."),
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
                    difficulty = StringValue("INTERMEDIATE"),
                    durationDays = IntegerValue("7"),
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
                    difficulty = StringValue("ADVANCED"),
                    durationDays = IntegerValue("30"),
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
                    difficulty = StringValue("BEGINNER"),
                    durationDays = IntegerValue("7"),
                    participantIds = ArrayValueWrapper(arrayValue = ArrayValue(emptyList()))
                )
            ),
            ChallengeFirestoreRequest(
                fields = ChallengeFields(
                    title = StringValue("Meal Master"),
                    description = StringValue("Log 15 meals this week to fuel your progress."),
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
                    difficulty = StringValue("INTERMEDIATE"),
                    durationDays = IntegerValue("7"),
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

    /** Read a challenge's real xpReward from either the challenges or templates collection. */
    private suspend fun fetchChallengeXpReward(challengeId: String, token: String): Int = try {
        val (resp, status) = get<FirestoreResponse<ChallengeFields>>(
            "$BASE_URL/$CHALLENGES_COLLECTION/$challengeId", token
        )
        resp.fields?.xpReward?.value?.toIntOrNull() ?: 0
    } catch (e: Exception) {
        0
    }

    /**
     * Best-effort sync of the local gamification stats (streak / level / totalXp) onto the
     * current user's fitness_testing_users doc so Profile + Community show REAL values.
     * Uses updateMask so only these fields are touched — other profile fields are preserved.
     * Never throws.
     */
    suspend fun syncStatsToFirestore() {
        try {
            val token = userSettings.authToken ?: return
            val email = userSettings.userEmail ?: return
            val docId = email.toFirestoreDocId()
            // Also sync last-activity millis so the retention push functions know who
            // has (not) worked out today. lastWorkoutDate is "yyyy-MM-dd"; when it is
            // today, stamp "now" so the function's 24h window treats the user as active.
            val lastWorkoutMillisField =
                if (userSettings.lastWorkoutDate.isNotBlank()) {
                    IntegerValue(currentTimeMillis().toString())
                } else null

            // Deep-personalization signals for touching notifications: best-ever streak,
            // lifetime workouts, goal, name, and weight progress (start vs latest weigh-in).
            val weighIns = userSettings.weighIns.value
            val startWeight = weighIns.firstOrNull()?.weight
                ?: userSettings.profileWeightKg.takeIf { it > 0f }
            val currentWeight = weighIns.lastOrNull()?.weight
                ?: userSettings.profileWeightKg.takeIf { it > 0f }
            val goalName = userSettings.fitnessGoal.takeIf { it.isNotBlank() }
            val name = userSettings.userName?.takeIf { it.isNotBlank() }

            val url = "$BASE_URL/$USERS_COLLECTION/$docId" +
                "?updateMask.fieldPaths=streakDays" +
                "&updateMask.fieldPaths=level" +
                "&updateMask.fieldPaths=totalXp" +
                "&updateMask.fieldPaths=longestStreak" +
                "&updateMask.fieldPaths=totalWorkouts" +
                (if (lastWorkoutMillisField != null) "&updateMask.fieldPaths=lastWorkoutMillis" else "") +
                (if (goalName != null) "&updateMask.fieldPaths=goal" else "") +
                (if (name != null) "&updateMask.fieldPaths=displayName" else "") +
                (if (startWeight != null) "&updateMask.fieldPaths=weightStartKg" else "") +
                (if (currentWeight != null) "&updateMask.fieldPaths=weightCurrentKg" else "")
            val body = UserStatsUpdateRequest(
                fields = UserStatsFields(
                    streakDays = IntegerValue(userSettings.currentStreak.toString()),
                    level = IntegerValue(userSettings.userLevel.toString()),
                    totalXp = IntegerValue(userSettings.totalXp.toString()),
                    lastWorkoutMillis = lastWorkoutMillisField,
                    longestStreak = IntegerValue(userSettings.longestStreak.toString()),
                    totalWorkouts = IntegerValue(userSettings.workoutsCompleted.toString()),
                    goal = goalName?.let { StringValue(it) },
                    displayName = name?.let { StringValue(it) },
                    weightStartKg = startWeight?.let { IntegerValue(it.toInt().toString()) },
                    weightCurrentKg = currentWeight?.let { IntegerValue(it.toInt().toString()) }
                )
            )
            patch<FirestoreResponse<UserStatsFields>>(url, body, token)
        } catch (e: Exception) {
            // best-effort; never throws
        }
    }

    /**
     * Pull the user's real stats (streak / level / XP / workouts) FROM Firestore into local
     * settings. Called on login so a returning/re-signed-up user sees their real numbers
     * instead of a reset-to-zero local state. Best-effort; never throws.
     */
    suspend fun syncStatsFromFirestore() {
        try {
            val token = userSettings.authToken ?: return
            val email = userSettings.userEmail ?: return
            val docId = email.toFirestoreDocId()
            val (resp, status) = get<FirestoreResponse<UserStatsFields>>("$BASE_URL/$USERS_COLLECTION/$docId", token)
            if (!status.isSuccess()) return
            val f = resp.fields ?: return
            f.streakDays?.value?.toIntOrNull()?.let { userSettings.currentStreak = it }
            f.longestStreak?.value?.toIntOrNull()?.let { if (it > userSettings.longestStreak) userSettings.longestStreak = it }
            f.level?.value?.toIntOrNull()?.let { userSettings.userLevel = it }
            f.totalXp?.value?.toIntOrNull()?.let { userSettings.totalXp = it }
            f.totalWorkouts?.value?.toIntOrNull()?.let { userSettings.workoutsCompleted = it }
        } catch (e: Exception) {
            // best-effort; never throws
        }
    }

    /**
     * Register the device's push (FCM) token on the current user's fitness_testing_users
     * doc so the tajlyRetentionPush Cloud Function can reach this device. Merge-safe
     * (updateMask), best-effort, never throws. No-op if no token is available (e.g. iOS
     * before Firebase Messaging is integrated, or FCM not configured).
     */
    suspend fun registerPushToken(fcmToken: String) {
        try {
            println("[PUSH] registerPushToken() called, token len=${fcmToken.length}")
            if (fcmToken.isBlank()) { println("[PUSH] registerPushToken: BLANK token, abort"); return }
            val token = userSettings.authToken ?: run {
                println("[PUSH] registerPushToken: NO authToken (not logged in) — will NOT write"); return
            }
            val email = userSettings.userEmail ?: run {
                println("[PUSH] registerPushToken: NO userEmail — will NOT write"); return
            }
            val docId = email.toFirestoreDocId()
            val url = "$BASE_URL/$USERS_COLLECTION/$docId?updateMask.fieldPaths=fcmToken"
            val body = UserStatsUpdateRequest(
                fields = UserStatsFields(fcmToken = StringValue(fcmToken))
            )
            val (_, status) = patch<FirestoreResponse<UserStatsFields>>(url, body, token)
            println("[PUSH] registerPushToken: WROTE fcmToken for docId=$docId -> HTTP $status")
        } catch (e: Exception) {
            println("[PUSH] registerPushToken: EXCEPTION ${e::class.simpleName}: ${e.message}")
        }
    }

    /** Fetch a participant's real level + streak from their user doc (for the leaderboard). */
    private suspend fun fetchUserLevelStreak(email: String, token: String): Pair<Int, Int> = try {
        val docId = email.toFirestoreDocId()
        val (resp, status) = get<FirestoreResponse<org.awi.fitness.model.CommunityUserFields>>(
            "$BASE_URL/$USERS_COLLECTION/$docId", token
        )
        if (status.isSuccess()) {
            val level = resp.fields?.level?.value?.toIntOrNull() ?: 1
            val streak = resp.fields?.streakDays?.value?.toIntOrNull() ?: 0
            level to streak
        } else 1 to 0
    } catch (e: Exception) {
        1 to 0
    }
}

private fun String.toFirestoreDocId(): String = toFirestoreDocId(this)

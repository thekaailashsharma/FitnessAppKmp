package org.awi.fitness.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// Firestore request / response wrappers
// ---------------------------------------------------------------------------

@Serializable
data class ChallengeFirestoreRequest(
    val fields: ChallengeFields
)

@Serializable
data class ChallengeProgressFirestoreRequest(
    val fields: ChallengeProgressFields
)

@Serializable
data class ChallengeParticipantsUpdateRequest(
    val fields: ChallengeParticipantsFields
)

/** Merge-safe stat sync onto the user's fitness_testing_users doc (used with updateMask). */
@Serializable
data class UserStatsUpdateRequest(
    val fields: UserStatsFields
)

@Serializable
data class UserStatsFields(
    @SerialName("streakDays") val streakDays: IntegerValue? = null,
    @SerialName("level") val level: IntegerValue? = null,
    @SerialName("totalXp") val totalXp: IntegerValue? = null,
    // Retention push: device token + last-activity timestamp read by the
    // retention push Cloud Functions. Written via updateMask so unrelated
    // profile fields are never clobbered.
    @SerialName("fcmToken") val fcmToken: StringValue? = null,
    @SerialName("lastWorkoutMillis") val lastWorkoutMillis: IntegerValue? = null,
    // Deep-personalization signals for touching, milestone-aware notifications.
    @SerialName("longestStreak") val longestStreak: IntegerValue? = null,
    @SerialName("totalWorkouts") val totalWorkouts: IntegerValue? = null,
    @SerialName("goal") val goal: StringValue? = null,
    @SerialName("displayName") val displayName: StringValue? = null,
    @SerialName("weightStartKg") val weightStartKg: IntegerValue? = null,
    @SerialName("weightCurrentKg") val weightCurrentKg: IntegerValue? = null
)

// ---------------------------------------------------------------------------
// Firestore field shapes
// ---------------------------------------------------------------------------

@Serializable
data class ChallengeFields(
    @SerialName("title") val title: StringValue? = null,
    @SerialName("description") val description: StringValue? = null,
    @SerialName("type") val type: StringValue? = null,
    @SerialName("targetType") val targetType: StringValue? = null,
    @SerialName("targetValue") val targetValue: IntegerValue? = null,
    @SerialName("xpReward") val xpReward: IntegerValue? = null,
    @SerialName("badgeIcon") val badgeIcon: StringValue? = null,
    @SerialName("startDate") val startDate: IntegerValue? = null,
    @SerialName("endDate") val endDate: IntegerValue? = null,
    @SerialName("createdBy") val createdBy: StringValue? = null,
    @SerialName("isPublic") val isPublic: BooleanValue? = null,
    @SerialName("isActive") val isActive: BooleanValue? = null,
    @SerialName("difficulty") val difficulty: StringValue? = null,
    @SerialName("durationDays") val durationDays: IntegerValue? = null,
    @SerialName("participantIds") val participantIds: ArrayValueWrapper? = null
)

@Serializable
data class ChallengeProgressFields(
    @SerialName("challengeId") val challengeId: StringValue? = null,
    @SerialName("userId") val userId: StringValue? = null,
    @SerialName("currentValue") val currentValue: IntegerValue? = null,
    @SerialName("targetValue") val targetValue: IntegerValue? = null,
    @SerialName("joinedAt") val joinedAt: IntegerValue? = null,
    @SerialName("completedAt") val completedAt: IntegerValue? = null,
    @SerialName("displayName") val displayName: StringValue? = null
)

@Serializable
data class ChallengeParticipantsFields(
    @SerialName("participantIds") val participantIds: ArrayValueWrapper
)

// ---------------------------------------------------------------------------
// Mapping helpers: Firestore → domain
// ---------------------------------------------------------------------------

fun FirestoreDocument<ChallengeFields>.toChallenge(
    userEmail: String,
    progressValue: Int = 0
): Challenge {
    val docId = name?.split("/")?.last() ?: ""
    val participantList = fields?.participantIds?.arrayValue?.values?.map { it.value } ?: emptyList()
    val isJoined = userEmail in participantList

    val targetTypeStr = fields?.targetType?.value ?: "WORKOUTS"
    val targetType = runCatching { ChallengeTargetType.valueOf(targetTypeStr) }
        .getOrDefault(ChallengeTargetType.WORKOUTS)

    val typeStr = fields?.type?.value ?: "DAILY"
    val type = runCatching { ChallengeType.valueOf(typeStr) }.getOrDefault(ChallengeType.DAILY)

    val xpReward = fields?.xpReward?.value?.toIntOrNull() ?: 0
    val targetValue = fields?.targetValue?.value?.toIntOrNull() ?: 1
    val badgeIcon = fields?.badgeIcon?.value?.takeIf { it.isNotBlank() } ?: "🏆"

    // Real difficulty / duration when present; 0 duration and BEGINNER-default flag "unknown".
    val difficultyStr = fields?.difficulty?.value?.takeIf { it.isNotBlank() }
    val difficulty = difficultyStr?.let {
        runCatching { ChallengeDifficulty.valueOf(it.uppercase()) }.getOrNull()
    } ?: ChallengeDifficulty.BEGINNER
    val hasDifficulty = difficultyStr != null
    val durationDays = fields?.durationDays?.value?.toIntOrNull() ?: 0

    return Challenge(
        id = docId,
        title = fields?.title?.value ?: "",
        description = fields?.description?.value ?: "",
        type = type,
        category = targetType.toCategory(),
        difficulty = difficulty,
        hasDifficulty = hasDifficulty,
        duration = durationDays,
        target = targetValue,
        unit = targetType.toUnit(),
        reward = Reward(xp = xpReward),
        isActive = isJoined,
        isCompleted = false,
        progress = progressValue,
        iconName = badgeIcon,
        color = targetType.toColor(),
        targetType = targetType,
        xpReward = xpReward,
        badgeIcon = badgeIcon,
        startDateLong = fields?.startDate?.value?.toLongOrNull() ?: 0L,
        endDateLong = fields?.endDate?.value?.toLongOrNull() ?: 0L,
        createdBy = fields?.createdBy?.value ?: "system",
        isPublic = fields?.isPublic?.value ?: true,
        participantIds = participantList,
        isJoined = isJoined,
        targetValue = targetValue
    )
}

fun FirestoreDocument<ChallengeProgressFields>.toChallengeProgress(): ChallengeProgress {
    return ChallengeProgress(
        challengeId = fields?.challengeId?.value ?: "",
        userId = fields?.userId?.value ?: "",
        currentValue = fields?.currentValue?.value?.toIntOrNull() ?: 0,
        targetValue = fields?.targetValue?.value?.toIntOrNull() ?: 0,
        joinedAt = fields?.joinedAt?.value?.toLongOrNull() ?: 0L,
        completedAt = fields?.completedAt?.value?.toLongOrNull(),
        displayName = fields?.displayName?.value ?: ""
    )
}

fun FirestoreDocument<ChallengeProgressFields>.toLeaderboardEntry(rank: Int): LeaderboardEntry {
    val docId = name?.split("/")?.last() ?: ""
    val displayName = fields?.displayName?.value?.takeIf { it.isNotBlank() } ?: docId
    return LeaderboardEntry(
        userId = fields?.userId?.value ?: docId,
        username = displayName,
        avatar = null,
        xp = fields?.currentValue?.value?.toIntOrNull() ?: 0,
        level = 1,
        rank = rank,
        streak = 0
    )
}

// ---------------------------------------------------------------------------
// Extension helpers for ChallengeTargetType
// ---------------------------------------------------------------------------

fun ChallengeTargetType.toColor(): Long = when (this) {
    ChallengeTargetType.WORKOUTS -> 0xFFC9A84C   // Gold
    ChallengeTargetType.CALORIES -> 0xFFFF5722
    ChallengeTargetType.STEPS -> 0xFF2196F3
    ChallengeTargetType.STREAK -> 0xFFFF9800
    ChallengeTargetType.POSTS -> 0xFF9C27B0
    ChallengeTargetType.MEALS -> 0xFF4CAF50
}

fun ChallengeTargetType.toUnit(): String = when (this) {
    ChallengeTargetType.WORKOUTS -> "workouts"
    ChallengeTargetType.CALORIES -> "kcal"
    ChallengeTargetType.STEPS -> "steps"
    ChallengeTargetType.STREAK -> "days"
    ChallengeTargetType.POSTS -> "posts"
    ChallengeTargetType.MEALS -> "meals"
}

fun ChallengeTargetType.toCategory(): ChallengeCategory = when (this) {
    ChallengeTargetType.WORKOUTS -> ChallengeCategory.EXERCISE
    ChallengeTargetType.CALORIES -> ChallengeCategory.CALORIES
    ChallengeTargetType.STEPS -> ChallengeCategory.STEPS
    ChallengeTargetType.STREAK -> ChallengeCategory.CONSISTENCY
    ChallengeTargetType.POSTS -> ChallengeCategory.CONSISTENCY
    ChallengeTargetType.MEALS -> ChallengeCategory.CALORIES
}

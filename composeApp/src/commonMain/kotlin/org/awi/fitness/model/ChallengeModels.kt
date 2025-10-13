package org.awi.fitness.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Challenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: ChallengeType = ChallengeType.DAILY,
    val category: ChallengeCategory = ChallengeCategory.EXERCISE,
    val difficulty: ChallengeDifficulty = ChallengeDifficulty.BEGINNER,
    val duration: Int = 1, // in days
    val target: Int = 0, // target value (reps, steps, etc.)
    val unit: String = "", // unit of measurement
    val reward: Reward = Reward(),
    val isActive: Boolean = false,
    val isCompleted: Boolean = false,
    val progress: Int = 0, // current progress
    val startDate: String = "",
    val endDate: String = "",
    val imageUrl: String? = null,
    val iconName: String = "",
    val color: Long = 0xFF123456
)

@Serializable
enum class ChallengeType {
    @SerialName("DAILY")
    DAILY,
    @SerialName("WEEKLY")
    WEEKLY,
    @SerialName("MONTHLY")
    MONTHLY,
    @SerialName("CUSTOM")
    CUSTOM
}

@Serializable
enum class ChallengeCategory {
    @SerialName("EXERCISE")
    EXERCISE,
    @SerialName("STEPS")
    STEPS,
    @SerialName("CALORIES")
    CALORIES,
    @SerialName("CONSISTENCY")
    CONSISTENCY,
    @SerialName("STRENGTH")
    STRENGTH,
    @SerialName("CARDIO")
    CARDIO,
    @SerialName("FLEXIBILITY")
    FLEXIBILITY
}

@Serializable
enum class ChallengeDifficulty {
    @SerialName("BEGINNER")
    BEGINNER,
    @SerialName("INTERMEDIATE")
    INTERMEDIATE,
    @SerialName("ADVANCED")
    ADVANCED
}

@Serializable
data class Reward(
    val xp: Int = 0,
    val badges: List<Badge> = emptyList(),
    val streakBonus: Int = 0,
    val title: String = ""
)

@Serializable
data class Badge(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconName: String = "",
    val rarity: BadgeRarity = BadgeRarity.COMMON,
    val unlockedAt: String = ""
)

@Serializable
enum class BadgeRarity {
    @SerialName("COMMON")
    COMMON,
    @SerialName("RARE")
    RARE,
    @SerialName("EPIC")
    EPIC,
    @SerialName("LEGENDARY")
    LEGENDARY
}

@Serializable
data class UserProgress(
    val userId: String = "",
    val totalXp: Int = 0,
    val currentLevel: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val badgesEarned: List<Badge> = emptyList(),
    val challengesCompleted: Int = 0,
    val rank: Int = 0
)

@Serializable
data class LeaderboardEntry(
    val userId: String = "",
    val username: String = "",
    val avatar: String? = null,
    val xp: Int = 0,
    val level: Int = 1,
    val rank: Int = 0,
    val streak: Int = 0
)

@Serializable
data class ChallengeProgress(
    val challengeId: String = "",
    val userId: String = "",
    val currentProgress: Int = 0,
    val targetProgress: Int = 0,
    val isCompleted: Boolean = false,
    val completedAt: String? = null,
    val lastUpdated: String = ""
)

package org.awi.fitness.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkoutPlan(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val difficulty: WorkoutDifficulty = WorkoutDifficulty.BEGINNER,
    val duration: Int = 12, // in weeks
    val category: WorkoutCategory = WorkoutCategory.STRENGTH,
    val imageUrl: String? = null
)

@Serializable
data class Exercise(
    val id: String = "",
    val planId: String = "",
    val name: String = "",
    val description: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val restTime: Int = 0, // in seconds
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null,
    val isCompleted: Boolean = false,
    val dayOfWeek: Int = 1, // 1-7
    val orderInDay: Int = 0,
    val completedTimestamp: Long = 0L
)

@Serializable
enum class WorkoutDifficulty {
    @SerialName("BEGINNER")
    BEGINNER,
    @SerialName("INTERMEDIATE")
    INTERMEDIATE,
    @SerialName("ADVANCED")
    ADVANCED
}

@Serializable
enum class WorkoutCategory {
    @SerialName("STRENGTH")
    STRENGTH,
    @SerialName("CARDIO")
    CARDIO,
    @SerialName("FLEXIBILITY")
    FLEXIBILITY,
    @SerialName("HIIT")
    HIIT,
    @SerialName("YOGA")
    YOGA
}

@Serializable
data class WorkoutPlanWithExercises(
    val plan: WorkoutPlan,
    val exercises: List<Exercise> = emptyList()
)

@Serializable
enum class FitnessGoal {
    @SerialName("WEIGHT_LOSS")
    WEIGHT_LOSS,
    @SerialName("MUSCLE_GAIN")
    MUSCLE_GAIN,
    @SerialName("ENDURANCE")
    ENDURANCE,
    @SerialName("FLEXIBILITY")
    FLEXIBILITY
}

@Serializable
enum class FitnessLevel {
    @SerialName("BEGINNER")
    BEGINNER,
    @SerialName("INTERMEDIATE")
    INTERMEDIATE,
    @SerialName("ADVANCED")
    ADVANCED
}

@Serializable
data class WorkoutActivity(
    val id: String = "",
    val type: ActivityType = ActivityType.EXERCISE_COMPLETED,
    val description: String = "",
    val timestamp: Long = 0,
    val exerciseId: String = "",
    val planId: String = ""
)

@Serializable
enum class ActivityType {
    @SerialName("EXERCISE_COMPLETED")
    EXERCISE_COMPLETED,
    @SerialName("WORKOUT_MISSED")
    WORKOUT_MISSED
}

@Serializable
data class WorkoutProgress(
    val id: String = "",
    val exerciseId: String = "",
    val date: Long = 0,
    val completedSets: Int = 0,
    val completedReps: Int = 0
)

@Serializable
data class MissedWorkout(
    val id: String = "",
    val planId: String = "",
    val date: Long = 0,
    val reason: String? = null
) 
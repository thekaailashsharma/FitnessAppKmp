package org.awi.fitness.data

enum class Gender {
    MALE, FEMALE, OTHER
}

enum class ActivityLevel(val factor: Double) {
    SEDENTARY(1.2),
    LIGHTLY_ACTIVE(1.375),
    MODERATELY_ACTIVE(1.55),
    VERY_ACTIVE(1.725),
    SUPER_ACTIVE(1.9)
}

enum class Goal(val calorieAdjustment: Int) {
    LOSE_WEIGHT(-500),
    MAINTAIN(0),
    GAIN_MUSCLE(500)
}

data class CalorieData(
    val weight: Float,
    val height: Float,
    val age: Int,
    val gender: Gender,
    val activityLevel: ActivityLevel,
    val goal: Goal,
    val calculatedCalories: Int,
    val bmr: Float,
    val tdee: Float,
    val lastUpdated: Long = 0
)

data class CalorieUiState(
    val weight: String = "",
    val height: String = "",
    val age: String = "",
    val gender: Gender = Gender.MALE,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATELY_ACTIVE,
    val goal: Goal = Goal.MAINTAIN,
    val calculatedCalories: Int = 0,
    val bmr: Float = 0f,
    val tdee: Float = 0f,
    val isCalculated: Boolean = false,
    val showRecalculate: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) 
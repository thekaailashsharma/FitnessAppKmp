package org.awi.fitness.utils

import org.awi.fitness.model.WorkoutCategory
import org.awi.fitness.model.WorkoutDifficulty

val fitnessTips = listOf(
    "Stay hydrated throughout the day.",
    "Get at least 7-8 hours of sleep every night.",
    "Warm up before and cool down after workouts.",
    "Incorporate both cardio and strength training.",
    "Maintain a balanced and nutritious diet.",
    "Set realistic and achievable fitness goals.",
    "Track your progress regularly.",
    "Consistency is more important than intensity.",
    "Don’t skip rest days – your muscles need recovery.",
    "Use proper form to avoid injury.",
    "Listen to your body and adjust when needed.",
    "Stretch regularly to improve flexibility.",
    "Add variety to your workouts to prevent plateaus.",
    "Avoid processed foods and sugary drinks.",
    "Stay motivated by working out with a friend.",
    "Focus on quality over quantity in your workouts.",
    "Avoid late-night heavy meals.",
    "Celebrate small wins to stay encouraged.",
    "Start slow if you're a beginner and build up.",
    "Stay positive and enjoy the journey."
)

fun List<String>.topFiveTips(): List<String> {
    return this.take(5)
}

fun normalizeWorkoutDifficulty(difficulty: String): WorkoutDifficulty {
    return when (difficulty.trim().uppercase()) {
        "BEGINNER", "EASY", "BASIC", "NOVICE" -> WorkoutDifficulty.BEGINNER
        "INTERMEDIATE", "MEDIUM", "MODERATE" -> WorkoutDifficulty.INTERMEDIATE
        "ADVANCED", "HARD", "EXPERT", "DIFFICULT" -> WorkoutDifficulty.ADVANCED
        else -> WorkoutDifficulty.BEGINNER // Default to beginner if unknown
    }
}

fun normalizeWorkoutCategory(category: String): WorkoutCategory {
    return when (category.trim().uppercase()) {
        "STRENGTH", "WEIGHT", "RESISTANCE", "WEIGHTS", "MUSCLE" -> WorkoutCategory.STRENGTH
        "CARDIO", "AEROBIC", "ENDURANCE", "RUNNING" -> WorkoutCategory.CARDIO
        "HIIT", "HIGH INTENSITY", "INTERVAL", "INTENSE" -> WorkoutCategory.HIIT
        "FLEXIBILITY", "STRETCHING", "MOBILITY" -> WorkoutCategory.FLEXIBILITY
        "YOGA", "MIND-BODY", "BALANCE" -> WorkoutCategory.YOGA
        else -> when {
            category.contains("STRENGTH") || category.contains("WEIGHT") -> WorkoutCategory.STRENGTH
            category.contains("CARDIO") || category.contains("ENDURANCE") -> WorkoutCategory.CARDIO
            category.contains("HIIT") || category.contains("INTENSE") -> WorkoutCategory.HIIT
            category.contains("FLEX") || category.contains("STRETCH") -> WorkoutCategory.FLEXIBILITY
            category.contains("YOGA") || category.contains("BALANCE") -> WorkoutCategory.YOGA
            else -> WorkoutCategory.STRENGTH // Default to strength if unknown
        }
    }
}
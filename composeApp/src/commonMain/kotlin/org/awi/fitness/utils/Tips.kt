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

val homeFitnessTips = listOf(
    "Drink at least 8 glasses of water daily",
    "Eat a balanced diet with fruits and vegetables",
    "Get 7–9 hours of sleep every night",
    "Exercise at least 30 minutes daily",
    "Take the stairs instead of the elevator",
    "Stretch before and after workouts",
    "Limit processed and junk foods",
    "Include strength training in your routine",
    "Take regular walk breaks if sitting long",
    "Avoid sugary drinks",
    "Practice portion control",
    "Cook at home more often",
    "Incorporate healthy fats like avocado",
    "Limit alcohol consumption",
    "Track your fitness progress",
    "Try new workouts to stay motivated",
    "Focus on consistency over intensity",
    "Listen to your body and rest when needed",
    "Avoid late-night eating",
    "Don’t skip breakfast",
    "Use a standing desk when possible",
    "Keep healthy snacks on hand",
    "Do breathing exercises for stress",
    "Cut down on refined carbs",
    "Stay active during weekends",
    "Keep a food journal",
    "Do yoga or meditation regularly",
    "Get regular medical checkups",
    "Avoid smoking and secondhand smoke",
    "Practice mindful eating",
    "Read food labels carefully",
    "Set realistic fitness goals",
    "Warm up properly before workouts",
    "Don’t compare yourself to others",
    "Stay positive about your journey",
    "Eat slowly and chew properly",
    "Avoid multitasking while eating",
    "Get sunlight exposure for Vitamin D",
    "Keep your body hydrated during workouts",
    "Use fitness apps to track progress",
    "Wear comfortable workout clothes",
    "Add more fiber to your diet",
    "Avoid crash diets",
    "Take a probiotic daily",
    "Limit screen time before bed",
    "Make time for hobbies",
    "Sleep and wake at regular times",
    "Snack on nuts and seeds",
    "Practice gratitude and mindfulness",
    "Lift weights safely and with good form",
    "Join a fitness community or group",
    "Replace sugary desserts with fruits",
    "Brush and floss your teeth daily",
    "Eat whole grains instead of white bread",
    "Try intermittent fasting if suitable",
    "Drink green tea regularly",
    "Add cinnamon to meals for blood sugar control",
    "Keep healthy food visible and accessible",
    "Incorporate fun physical activities",
    "Limit caffeine in the evening",
    "Keep your workout gear ready",
    "Meal prep to avoid unhealthy choices",
    "Dance for fitness and fun",
    "Celebrate small wins",
    "Don’t overtrain your muscles",
    "Balance cardio and weight training",
    "Avoid emotional eating",
    "Stay consistent with your routine",
    "Try meatless meals once a week",
    "Replace soda with sparkling water",
    "Be patient with weight loss",
    "Don’t weigh yourself daily",
    "Use a foam roller after workouts",
    "Try a new sport or activity",
    "Add herbs and spices for health benefits",
    "Use smaller plates for better portions",
    "Limit salt intake",
    "Avoid eating out too frequently",
    "Pack healthy snacks for travel",
    "Choose water over energy drinks",
    "Keep track of your steps daily",
    "Get regular eye checkups",
    "Don’t ignore mental health",
    "Do bodyweight exercises at home",
    "Join group fitness classes",
    "Build a morning routine",
    "Add lemon to your water",
    "Avoid fad diets",
    "Swap fried food with baked alternatives",
    "Set non-weight goals (e.g., run a 5K)",
    "Practice deep breathing before bed",
    "Keep your posture correct",
    "Limit red meat consumption",
    "Go for nature walks",
    "Start slow if new to exercise",
    "Find a workout buddy",
    "Avoid late-night screens",
    "Try high-intensity interval training",
    "Read fitness books for motivation",
    "Laugh more often",
    "Create a relaxing sleep environment",
    "Don’t skip rest days",
    "Reward yourself healthily",
    "Make health a long-term priority"
)

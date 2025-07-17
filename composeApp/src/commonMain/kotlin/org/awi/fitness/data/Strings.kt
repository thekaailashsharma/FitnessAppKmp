package org.awi.fitness.data

object Strings {
    private val strings = mapOf(
        Language.ENGLISH to mapOf(
            // Auth
            StringKey.APP_NAME to "Fitness App",
            StringKey.SIGN_IN to "Sign In",
            StringKey.SIGN_UP to "Sign Up",
            StringKey.EMAIL to "Email",
            StringKey.PASSWORD to "Password",
            StringKey.CREATE_ACCOUNT to "Create Account",
            StringKey.SIGN_IN_CONTINUE to "Sign in to continue",
            StringKey.ALREADY_HAVE_ACCOUNT to "Already have an account?",
            StringKey.DONT_HAVE_ACCOUNT to "Don't have an account?",
            
            // Navigation
            StringKey.HOME to "Home",
            StringKey.WORKOUTS to "Workouts",
            StringKey.CALORIES to "Calories",
            StringKey.PROFILE to "Profile",
            
            // Profile
            StringKey.BACK to "Back",
            StringKey.SETTINGS to "Settings",
            StringKey.DARK_THEME to "Dark Theme",
            StringKey.LANGUAGE to "Language",
            StringKey.LOGOUT to "Logout",
            StringKey.FITNESS_ENTHUSIAST to "Fitness Enthusiast",
            StringKey.FITNESS_STATS to "Fitness Stats",
            StringKey.BMR to "BMR",
            StringKey.TDEE to "TDEE",
            StringKey.GOAL to "Goal",
            
            // Home Screen
            StringKey.WELCOME_BACK to "Welcome back",
            StringKey.WORKOUTS_COMPLETED to "Workouts",
            StringKey.TODAY to "Today",
            StringKey.CALORIE_GOAL to "Cal Goal",
            StringKey.CALORIE_CALCULATOR to "Calorie Calculator",
            StringKey.CALCULATE_DAILY_CALORIES to "Calculate your daily calorie needs",
            StringKey.WORKOUT_SCHEDULE to "Workout Schedule",
            StringKey.PLAN_MANAGE_WORKOUT to "Plan and manage your workout routine",
            StringKey.RECENT_WORKOUTS to "Recent Workouts",
            StringKey.EXERCISES_COMPLETED to "exercises completed",
            StringKey.DAILY_WELLNESS_TIPS to "Daily Wellness Tips",
            
            // Workout
            StringKey.WORKOUT_TYPE to "Workout Type",
            StringKey.RECURRING to "Recurring",
            StringKey.TITLE to "Title",
            StringKey.DESCRIPTION to "Description",
            StringKey.ADD to "Add",
            StringKey.SAVE to "Save",
            StringKey.CANCEL to "Cancel",
            StringKey.RETRY to "Retry",
            StringKey.AN_ERROR_OCCURRED to "An error occurred",
            
            // Days
            StringKey.MONDAY to "Mon",
            StringKey.TUESDAY to "Tue",
            StringKey.WEDNESDAY to "Wed",
            StringKey.THURSDAY to "Thu",
            StringKey.FRIDAY to "Fri",
            StringKey.SATURDAY to "Sat",
            StringKey.SUNDAY to "Sun",
            
            // Fitness Goals
            StringKey.WORKOUT_DAYS_QUESTION to "How many days per week can you workout?",
            StringKey.SPECIFIC_REQUIREMENTS_QUESTION to "Any specific requirements for your workout?",
            StringKey.SPECIFIC_REQUIREMENTS_HINT to "E.g., focus areas, time constraints, equipment availability...",
            
            // Workout Types
            StringKey.CARDIO to "Cardio",
            StringKey.STRENGTH to "Strength",
            StringKey.FLEXIBILITY to "Flexibility",
            StringKey.HIIT to "HIIT",
            StringKey.YOGA to "Yoga",
            StringKey.OTHER to "Other",
            
            // Recurring Types
            StringKey.NONE to "None",
            StringKey.DAILY to "Daily",
            StringKey.WEEKLY to "Weekly",
            StringKey.MONTHLY to "Monthly",
            
            // Meal Screen
            StringKey.MEAL_PLANS to "Meal Plans",
            StringKey.PERSONALIZED_MEALS to "Personalized meals assigned by your trainer",
            StringKey.NO_MEAL_PLANS to "No Meal Plans Yet",
            StringKey.NO_MEAL_PLANS_DESC to "Your trainer hasn't assigned any meal plans yet",
            StringKey.SHOW_LESS to "Show less",
            StringKey.SHOW_MORE to "Show more",
            StringKey.INGREDIENTS to "Ingredients",
            StringKey.INSTRUCTIONS to "Instructions",
            
            // Calorie Calculator Screen
            StringKey.CALORIE_CALCULATOR_TITLE to "Calorie Calculator",
            StringKey.WEIGHT_KG to "Weight (kg)",
            StringKey.HEIGHT_CM to "Height (cm)",
            StringKey.AGE to "Age",
            StringKey.GENDER to "Gender",
            StringKey.ACTIVITY_LEVEL to "Activity Level",
            StringKey.CALCULATE to "Calculate",
            StringKey.RECALCULATE to "Recalculate",
            StringKey.UNKNOWN_ERROR to "Unknown error occurred",
            
            // Weight Tracking
            StringKey.WEIGHT_TRACKING to "Weight Tracking",
            StringKey.ADD_WEIGHT to "Add Weight",
            StringKey.PROGRESS_GRAPH to "Progress Graph",
            StringKey.WEIGHT_NOTE to "Note (optional)",
            StringKey.ADD_WEIGHT_ENTRY to "Add Weight Entry",
            
            // Measurement Tracking
            StringKey.MEASUREMENTS to "Measurements",
            StringKey.ADD_MEASUREMENTS to "Add Measurements",
            StringKey.ANALYZING_MEASUREMENTS to "Analyzing your measurements...",
            StringKey.ANALYSIS to "Analysis",
            StringKey.RECOMMENDATIONS to "Recommendations",
            StringKey.WAIST_CM to "Waist (cm)",
            StringKey.HIPS_CM to "Hips (cm)",
            StringKey.ARMS_CM to "Arms (cm)",
            StringKey.WAIST to "Waist",
            StringKey.HIPS to "Hips",
            StringKey.ARMS to "Arms",
            
            // Calorie Results
            StringKey.BASAL_METABOLIC_RATE to "Basal Metabolic Rate (BMR)",
            StringKey.BMR_DESC to "The calories your body burns at complete rest",
            StringKey.TOTAL_DAILY_ENERGY to "Total Daily Energy Expenditure (TDEE)",
            StringKey.TDEE_DESC to "Your BMR adjusted for activity level",
            StringKey.GOAL_ADJUSTMENT to "Goal Adjustment",
            StringKey.GOAL_ADJUSTMENT_DESC to "Calorie adjustment based on your chosen goal",
            StringKey.CALORIES_PER_DAY to "kcal/day",
            
            // Citations
            StringKey.MEDICAL_CITATIONS to "Medical Citations",
            StringKey.BMR_CITATION to "BMR calculation uses the Mifflin-St Jeor Equation (1990), validated by the Academy of Nutrition and Dietetics as the most accurate for healthy adults.",
            StringKey.ACTIVITY_LEVEL_CITATION to "Activity level multipliers based on WHO/FAO/UNU Expert Consultation (2004) guidelines for human energy requirements.",
            StringKey.CALORIE_ADJUSTMENT_CITATION to "Calorie adjustments of ±500 kcal/day based on evidence from the National Institutes of Health for safe and sustainable weight management.",
            StringKey.VIEW_CITATIONS to "View Medical Citations",
            StringKey.HIDE_CITATIONS to "Hide Citations",
            
            // Account Management
            StringKey.DELETE_ACCOUNT to "Delete Account",
            StringKey.DELETE_ACCOUNT_CONFIRMATION to "Delete Account?",
            StringKey.DELETE_ACCOUNT_DESCRIPTION to "This action cannot be undone. All your data will be permanently deleted.",
            StringKey.CONFIRM to "Confirm",
            StringKey.CANCEL_DELETE to "Cancel"
        ),
        Language.DUTCH to mapOf(
            // Auth
            StringKey.APP_NAME to "Fitness App",
            StringKey.SIGN_IN to "Inloggen",
            StringKey.SIGN_UP to "Registreren",
            StringKey.EMAIL to "E-mail",
            StringKey.PASSWORD to "Wachtwoord",
            StringKey.CREATE_ACCOUNT to "Account Aanmaken",
            StringKey.SIGN_IN_CONTINUE to "Log in om door te gaan",
            StringKey.ALREADY_HAVE_ACCOUNT to "Heb je al een account?",
            StringKey.DONT_HAVE_ACCOUNT to "Heb je nog geen account?",
            
            // Navigation
            StringKey.HOME to "Home",
            StringKey.WORKOUTS to "Workouts",
            StringKey.CALORIES to "Calorieën",
            StringKey.PROFILE to "Profiel",
            
            // Profile
            StringKey.BACK to "Terug",
            StringKey.SETTINGS to "Instellingen",
            StringKey.DARK_THEME to "Donker Thema",
            StringKey.LANGUAGE to "Taal",
            StringKey.LOGOUT to "Uitloggen",
            StringKey.FITNESS_ENTHUSIAST to "Fitness Liefhebber",
            StringKey.FITNESS_STATS to "Fitness Statistieken",
            StringKey.BMR to "BMR",
            StringKey.TDEE to "TDEE",
            StringKey.GOAL to "Doel",
            
            // Home Screen
            StringKey.WELCOME_BACK to "Welkom terug",
            StringKey.WORKOUTS_COMPLETED to "Workouts",
            StringKey.TODAY to "Vandaag",
            StringKey.CALORIE_GOAL to "Cal Doel",
            StringKey.CALORIE_CALCULATOR to "Calorie Calculator",
            StringKey.CALCULATE_DAILY_CALORIES to "Bereken je dagelijkse caloriebehoefte",
            StringKey.WORKOUT_SCHEDULE to "Workout Schema",
            StringKey.PLAN_MANAGE_WORKOUT to "Plan en beheer je workoutroutine",
            StringKey.RECENT_WORKOUTS to "Recente Workouts",
            StringKey.EXERCISES_COMPLETED to "oefeningen voltooid",
            StringKey.DAILY_WELLNESS_TIPS to "Dagelijkse Welzijnstips",
            
            // Workout
            StringKey.WORKOUT_TYPE to "Workout Type",
            StringKey.RECURRING to "Herhalend",
            StringKey.TITLE to "Titel",
            StringKey.DESCRIPTION to "Beschrijving",
            StringKey.ADD to "Toevoegen",
            StringKey.SAVE to "Opslaan",
            StringKey.CANCEL to "Annuleren",
            StringKey.RETRY to "Opnieuw",
            StringKey.AN_ERROR_OCCURRED to "Er is een fout opgetreden",
            
            // Days
            StringKey.MONDAY to "Ma",
            StringKey.TUESDAY to "Di",
            StringKey.WEDNESDAY to "Wo",
            StringKey.THURSDAY to "Do",
            StringKey.FRIDAY to "Vr",
            StringKey.SATURDAY to "Za",
            StringKey.SUNDAY to "Zo",
            
            // Fitness Goals
            StringKey.WORKOUT_DAYS_QUESTION to "Hoeveel dagen per week kun je trainen?",
            StringKey.SPECIFIC_REQUIREMENTS_QUESTION to "Heb je specifieke vereisten voor je workout?",
            StringKey.SPECIFIC_REQUIREMENTS_HINT to "Bijv. focusgebieden, tijdsbeperkingen, beschikbare apparatuur...",
            
            // Workout Types
            StringKey.CARDIO to "Cardio",
            StringKey.STRENGTH to "Kracht",
            StringKey.FLEXIBILITY to "Flexibiliteit",
            StringKey.HIIT to "HIIT",
            StringKey.YOGA to "Yoga",
            StringKey.OTHER to "Overig",
            
            // Recurring Types
            StringKey.NONE to "Geen",
            StringKey.DAILY to "Dagelijks",
            StringKey.WEEKLY to "Wekelijks",
            StringKey.MONTHLY to "Maandelijks",
            
            // Meal Screen
            StringKey.MEAL_PLANS to "Maaltijdplannen",
            StringKey.PERSONALIZED_MEALS to "Gepersonaliseerde maaltijden toegewezen door je trainer",
            StringKey.NO_MEAL_PLANS to "Nog Geen Maaltijdplannen",
            StringKey.NO_MEAL_PLANS_DESC to "Je trainer heeft nog geen maaltijdplannen toegewezen",
            StringKey.SHOW_LESS to "Toon minder",
            StringKey.SHOW_MORE to "Toon meer",
            StringKey.INGREDIENTS to "Ingrediënten",
            StringKey.INSTRUCTIONS to "Instructies",
            
            // Calorie Calculator Screen
            StringKey.CALORIE_CALCULATOR_TITLE to "Calorie Calculator",
            StringKey.WEIGHT_KG to "Gewicht (kg)",
            StringKey.HEIGHT_CM to "Lengte (cm)",
            StringKey.AGE to "Leeftijd",
            StringKey.GENDER to "Geslacht",
            StringKey.ACTIVITY_LEVEL to "Activiteitsniveau",
            StringKey.CALCULATE to "Bereken",
            StringKey.RECALCULATE to "Herbereken",
            StringKey.UNKNOWN_ERROR to "Onbekende fout opgetreden",
            
            // Weight Tracking
            StringKey.WEIGHT_TRACKING to "Gewicht Bijhouden",
            StringKey.ADD_WEIGHT to "Gewicht Toevoegen",
            StringKey.PROGRESS_GRAPH to "Voortgangsgrafiek",
            StringKey.WEIGHT_NOTE to "Notitie (optioneel)",
            StringKey.ADD_WEIGHT_ENTRY to "Gewicht Toevoegen",
            
            // Measurement Tracking
            StringKey.MEASUREMENTS to "Metingen",
            StringKey.ADD_MEASUREMENTS to "Metingen Toevoegen",
            StringKey.ANALYZING_MEASUREMENTS to "Je metingen worden geanalyseerd...",
            StringKey.ANALYSIS to "Analyse",
            StringKey.RECOMMENDATIONS to "Aanbevelingen",
            StringKey.WAIST_CM to "Taille (cm)",
            StringKey.HIPS_CM to "Heupen (cm)",
            StringKey.ARMS_CM to "Armen (cm)",
            StringKey.WAIST to "Taille",
            StringKey.HIPS to "Heupen",
            StringKey.ARMS to "Armen",
            
            // Calorie Results
            StringKey.BASAL_METABOLIC_RATE to "Basaal Metabolisme (BMR)",
            StringKey.BMR_DESC to "De calorieën die je lichaam in rust verbrandt",
            StringKey.TOTAL_DAILY_ENERGY to "Totaal Dagelijks Energieverbruik",
            StringKey.TDEE_DESC to "Je BMR aangepast aan je activiteitsniveau",
            StringKey.GOAL_ADJUSTMENT to "Doel Aanpassing",
            StringKey.GOAL_ADJUSTMENT_DESC to "Calorie-aanpassing gebaseerd op je gekozen doel",
            StringKey.CALORIES_PER_DAY to "kcal/dag",
            
            // Citations
            StringKey.MEDICAL_CITATIONS to "Medische Citaties",
            StringKey.BMR_CITATION to "BMR berekening gebruikt de Mifflin-St Jeor Vergelijking (1990), gevalideerd door de Academie van Voeding en Voeding (2004) als de meest nauwkeurige voor gezonde volwassenen.",
            StringKey.ACTIVITY_LEVEL_CITATION to "Activiteitsniveau vermenigvuldigers gebaseerd op richtlijnen van de Wereldgezondheidsorganisatie (WHO), Voedsel- en Landbouworganisatie (FAO) en Verenigde Naties (UNU) voor menselijke energiebehoeften.",
            StringKey.CALORIE_ADJUSTMENT_CITATION to "Calorie-aanpassingen van ±500 kcal/dag gebaseerd op bewijs uit de National Institutes of Health voor veilige en duurzame gewichtsbeheersing.",
            StringKey.VIEW_CITATIONS to "Bekijk Medische Citaties",
            StringKey.HIDE_CITATIONS to "Verberg Citaties",
            
            // Account Management
            StringKey.DELETE_ACCOUNT to "Account Verwijderen",
            StringKey.DELETE_ACCOUNT_CONFIRMATION to "Account Verwijderen?",
            StringKey.DELETE_ACCOUNT_DESCRIPTION to "Deze actie kan niet ongedaan worden gemaakt. Al uw gegevens worden permanent verwijderd.",
            StringKey.CONFIRM to "Bevestigen",
            StringKey.CANCEL_DELETE to "Annuleren"
        )
    )

    fun get(key: StringKey, language: Language): String {
        return strings[language]?.get(key) ?: strings[Language.ENGLISH]?.get(key) ?: key.name
    }
}
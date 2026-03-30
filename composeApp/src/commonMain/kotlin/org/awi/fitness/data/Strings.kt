package org.awi.fitness.data

object Strings {
    private val strings = mapOf(
        Language.ENGLISH to mapOf(
            // Auth
            StringKey.APP_NAME to "Fitness & Health by Sivv",
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

            // Meal Plan Feature
            StringKey.MEAL_PLAN to "Meal Plan",
            StringKey.CREATE_MY_PLAN to "Create My Plan",
            StringKey.PLAN_MEALS_WITH_AI to "Plan Your Meals with AI",
            StringKey.PLAN_MEALS_WITH_AI_DESC to "Generate a personalized weekly meal plan tailored to your fitness goals",
            StringKey.CREATE_YOUR_MEAL_PLAN to "Create Your Meal Plan",
            StringKey.CREATE_PLAN_SUBTITLE to "AI will design a 7-day plan based on your preferences",
            StringKey.WHATS_YOUR_GOAL to "What's your goal?",
            StringKey.DIETARY_PREFERENCE to "Dietary preference",
            StringKey.ANY_ALLERGIES to "Any allergies or restrictions?",
            StringKey.ALLERGIES_HINT to "e.g., nuts, dairy, gluten...",
            StringKey.MEALS_PER_DAY to "Meals per day",
            StringKey.DAILY_CALORIES to "Daily calories",
            StringKey.USE_CALCULATED_CALORIES to "Use my calculated target",
            StringKey.SET_CUSTOM_TARGET to "Set custom target",
            StringKey.GENERATE_MY_PLAN to "Generate My Plan",
            StringKey.CREATING_YOUR_PLAN to "Creating Your Plan...",
            StringKey.DESIGNING_BREAKFASTS to "Designing your breakfasts",
            StringKey.PLANNING_LUNCHES to "Planning your lunches",
            StringKey.CRAFTING_DINNERS to "Crafting your dinners",
            StringKey.BUILDING_SHOPPING_LIST to "Building shopping list",
            StringKey.STEP_ANALYZING_PREFERENCES to "Analyzing your preferences",
            StringKey.STEP_GENERATING_MEALS to "Generating meals with AI",
            StringKey.STEP_BALANCING_NUTRITION to "Perfecting nutrition balance",
            StringKey.STEP_SAVING_PLAN to "Saving your plan",
            StringKey.BREAKFAST to "Breakfast",
            StringKey.LUNCH to "Lunch",
            StringKey.DINNER to "Dinner",
            StringKey.SNACK to "Snack",
            StringKey.KCAL to "kcal",
            StringKey.PROTEIN_SHORT to "P",
            StringKey.CARBS_SHORT to "C",
            StringKey.FAT_SHORT to "F",
            StringKey.PREP_TIME to "prep",
            StringKey.MIN_SHORT to "min",
            StringKey.MARK_AS_EATEN to "Mark as Eaten",
            StringKey.SWAP_THIS_MEAL to "Swap This Meal",
            StringKey.SWAPPING_MEAL to "Finding a new meal...",
            StringKey.SHOPPING_LIST to "Shopping List",
            StringKey.MY_PLANS to "My Plans",
            StringKey.ACTIVE to "Active",
            StringKey.INACTIVE to "Inactive",
            StringKey.SET_ACTIVE to "Set Active",
            StringKey.DELETE_PLAN to "Delete Plan",
            StringKey.DELETE_PLAN_CONFIRM to "Are you sure you want to delete this plan?",
            StringKey.CREATE_NEW_PLAN to "Create New Plan",
            StringKey.NOT_TODAY_TITLE to "Not Today's Meal",
            StringKey.NOT_TODAY_DESC to "You're marking a meal for a different day. Continue?",
            StringKey.MARK_ANYWAY to "Mark Anyway",
            StringKey.MACRO_PROGRESS to "Today's Nutrition",
            StringKey.NO_ACTIVE_PLAN to "No active plan",
            StringKey.WEIGHT_LOSS to "Weight Loss",
            StringKey.MUSCLE_GAIN to "Muscle Gain",
            StringKey.MAINTENANCE to "Maintenance",
            StringKey.GENERAL_HEALTH to "General Health",
            StringKey.BALANCED to "Balanced",
            StringKey.KETO to "Keto",
            StringKey.VEGETARIAN to "Vegetarian",
            StringKey.VEGAN to "Vegan",
            StringKey.MEDITERRANEAN to "Mediterranean",
            StringKey.HIGH_PROTEIN to "High Protein",
            
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
            StringKey.CANCEL_DELETE to "Cancel",
            
            // New translations
            StringKey.VIEW_ALL to "View All",
            StringKey.EXERCISES to "Exercises",
            StringKey.COMPLETED to "Completed",

            // Manual Meal
            StringKey.ADD_MEAL to "Add Meal",
            StringKey.EDIT_MEAL to "Edit Meal",
            StringKey.DELETE_MEAL to "Delete Meal",
            StringKey.DELETE_MEAL_CONFIRM to "Are you sure you want to remove this meal from your plan?",
            StringKey.MEAL_NAME to "Meal Name",
            StringKey.MEAL_NAME_HINT to "e.g. Grilled Chicken Salad",
            StringKey.MEAL_SLOT to "Meal Slot",
            StringKey.CALORIES_LABEL to "Calories",
            StringKey.PROTEIN_LABEL to "Protein (g)",
            StringKey.CARBS_LABEL to "Carbs (g)",
            StringKey.FAT_LABEL to "Fat (g)",
            StringKey.SAVE_MEAL to "Save Meal",
            StringKey.EDIT to "Edit",
            StringKey.DELETE to "Delete",

            // Workout UI
            StringKey.WORKOUT_PLANS to "Workout Plans",
            StringKey.DELETE_WORKOUT_PLAN to "Delete Workout Plan",
            StringKey.DELETE_WORKOUT_CONFIRM to "Are you sure you want to delete this workout plan? This action cannot be undone.",
            StringKey.SHOW_GRID to "Show Grid",
            StringKey.SHOW_LIST to "Show List",
            StringKey.ADD_NEW_PLAN to "Add New Plan",
            StringKey.NO_WORKOUT_PLAN to "No Workout Plan Yet",
            StringKey.NO_WORKOUT_PLAN_DESC to "Set up your fitness goals to get a personalized plan",
            StringKey.SET_UP_MY_GOALS to "Set Up My Goals",
            StringKey.EXERCISE_DETAILS to "Exercise Details",
            StringKey.SETS to "Sets",
            StringKey.REPS to "Reps",
            StringKey.REST to "Rest",
            StringKey.AI_POWERED_TIPS to "AI-Powered Tips",
            StringKey.DURATION to "Duration",
            StringKey.CATEGORY to "Category",
            StringKey.TODAYS_EXERCISES to "Today's Exercises",
            StringKey.START_WORKOUT to "Start Workout",
            StringKey.WEEKS to "weeks",

            // Discover
            StringKey.DISCOVER to "Discover",
            StringKey.NO_ARTICLES_FOUND to "No articles found",

            // Home dashboard
            StringKey.TODAYS_MEALS to "Today's Meals",
            StringKey.MEALS_EATEN to "Eaten",
            StringKey.CALCULATE_YOUR_NEEDS to "Calculate Your Daily Needs",
            StringKey.CALCULATE_YOUR_NEEDS_DESC to "Get your personalized BMR, TDEE, and calorie goal",
            StringKey.NOT_SET to "—",
            StringKey.MEALS_PROGRESS to "Meals",

            // Meal add options
            StringKey.GENERATE_WITH_AI to "Generate with AI",
            StringKey.ADD_MANUALLY to "Add Manually",

            // Meal editor
            StringKey.ADD_INGREDIENT to "Add Ingredient",
            StringKey.ADD_INSTRUCTION to "Add Step",
            StringKey.INGREDIENT_HINT to "e.g. 200g chicken breast",
            StringKey.INSTRUCTION_HINT to "e.g. Preheat oven to 180°C",
            StringKey.PREP_TIME_LABEL to "Prep Time (min)",
            StringKey.DIETARY_TAGS to "Dietary Tags",
            StringKey.REMOVE to "Remove",

            // Swap confirmation
            StringKey.SWAP_CONFIRM_TITLE to "Swap This Meal?",
            StringKey.SWAP_CONFIRM_DESC to "AI will generate a new meal for this slot. This replaces the current meal.",
            StringKey.MEAL_SWAPPED to "Meal swapped successfully",

            // Misc
            StringKey.UPCOMING_WORKOUT to "Upcoming Workout",
            StringKey.REFRESH_TIPS to "Refresh Tips"
        ),
        Language.DUTCH to mapOf(
            // Auth
            StringKey.APP_NAME to "Fitness & Health by Sivv",
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

            // Meal Plan Feature
            StringKey.MEAL_PLAN to "Maaltijdplan",
            StringKey.CREATE_MY_PLAN to "Maak Mijn Plan",
            StringKey.PLAN_MEALS_WITH_AI to "Plan Je Maaltijden met AI",
            StringKey.PLAN_MEALS_WITH_AI_DESC to "Genereer een gepersonaliseerd weekmenu afgestemd op je fitnessdoelen",
            StringKey.CREATE_YOUR_MEAL_PLAN to "Maak Je Maaltijdplan",
            StringKey.CREATE_PLAN_SUBTITLE to "AI ontwerpt een 7-daags plan op basis van je voorkeuren",
            StringKey.WHATS_YOUR_GOAL to "Wat is je doel?",
            StringKey.DIETARY_PREFERENCE to "Dieetvoorkeur",
            StringKey.ANY_ALLERGIES to "Allergieën of beperkingen?",
            StringKey.ALLERGIES_HINT to "bijv. noten, zuivel, gluten...",
            StringKey.MEALS_PER_DAY to "Maaltijden per dag",
            StringKey.DAILY_CALORIES to "Dagelijkse calorieën",
            StringKey.USE_CALCULATED_CALORIES to "Gebruik mijn berekend doel",
            StringKey.SET_CUSTOM_TARGET to "Stel aangepast doel in",
            StringKey.GENERATE_MY_PLAN to "Genereer Mijn Plan",
            StringKey.CREATING_YOUR_PLAN to "Je Plan Wordt Gemaakt...",
            StringKey.DESIGNING_BREAKFASTS to "Ontbijt wordt ontworpen",
            StringKey.PLANNING_LUNCHES to "Lunch wordt gepland",
            StringKey.CRAFTING_DINNERS to "Diner wordt samengesteld",
            StringKey.BUILDING_SHOPPING_LIST to "Boodschappenlijst wordt gemaakt",
            StringKey.STEP_ANALYZING_PREFERENCES to "Voorkeuren worden geanalyseerd",
            StringKey.STEP_GENERATING_MEALS to "Maaltijden genereren met AI",
            StringKey.STEP_BALANCING_NUTRITION to "Voedingswaarde perfectioneren",
            StringKey.STEP_SAVING_PLAN to "Je plan wordt opgeslagen",
            StringKey.BREAKFAST to "Ontbijt",
            StringKey.LUNCH to "Lunch",
            StringKey.DINNER to "Diner",
            StringKey.SNACK to "Snack",
            StringKey.KCAL to "kcal",
            StringKey.PROTEIN_SHORT to "E",
            StringKey.CARBS_SHORT to "K",
            StringKey.FAT_SHORT to "V",
            StringKey.PREP_TIME to "bereid.",
            StringKey.MIN_SHORT to "min",
            StringKey.MARK_AS_EATEN to "Markeer als Gegeten",
            StringKey.SWAP_THIS_MEAL to "Wissel Deze Maaltijd",
            StringKey.SWAPPING_MEAL to "Nieuwe maaltijd zoeken...",
            StringKey.SHOPPING_LIST to "Boodschappenlijst",
            StringKey.MY_PLANS to "Mijn Plannen",
            StringKey.ACTIVE to "Actief",
            StringKey.INACTIVE to "Inactief",
            StringKey.SET_ACTIVE to "Activeren",
            StringKey.DELETE_PLAN to "Plan Verwijderen",
            StringKey.DELETE_PLAN_CONFIRM to "Weet je zeker dat je dit plan wilt verwijderen?",
            StringKey.CREATE_NEW_PLAN to "Nieuw Plan Maken",
            StringKey.NOT_TODAY_TITLE to "Niet Vandaag's Maaltijd",
            StringKey.NOT_TODAY_DESC to "Je markeert een maaltijd voor een andere dag. Doorgaan?",
            StringKey.MARK_ANYWAY to "Toch Markeren",
            StringKey.MACRO_PROGRESS to "Voeding Vandaag",
            StringKey.NO_ACTIVE_PLAN to "Geen actief plan",
            StringKey.WEIGHT_LOSS to "Afvallen",
            StringKey.MUSCLE_GAIN to "Spieropbouw",
            StringKey.MAINTENANCE to "Onderhoud",
            StringKey.GENERAL_HEALTH to "Algemene Gezondheid",
            StringKey.BALANCED to "Gebalanceerd",
            StringKey.KETO to "Keto",
            StringKey.VEGETARIAN to "Vegetarisch",
            StringKey.VEGAN to "Veganistisch",
            StringKey.MEDITERRANEAN to "Mediterraan",
            StringKey.HIGH_PROTEIN to "Hoog Eiwit",
            
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
            StringKey.CANCEL_DELETE to "Annuleren",
            
            // New translations
            StringKey.VIEW_ALL to "Bekijk Alles",
            StringKey.EXERCISES to "Oefeningen",
            StringKey.COMPLETED to "Voltooid",

            // Manual Meal
            StringKey.ADD_MEAL to "Maaltijd Toevoegen",
            StringKey.EDIT_MEAL to "Maaltijd Bewerken",
            StringKey.DELETE_MEAL to "Maaltijd Verwijderen",
            StringKey.DELETE_MEAL_CONFIRM to "Weet je zeker dat je deze maaltijd uit je plan wilt verwijderen?",
            StringKey.MEAL_NAME to "Maaltijdnaam",
            StringKey.MEAL_NAME_HINT to "bijv. Gegrilde Kip Salade",
            StringKey.MEAL_SLOT to "Maaltijdmoment",
            StringKey.CALORIES_LABEL to "Calorieën",
            StringKey.PROTEIN_LABEL to "Eiwit (g)",
            StringKey.CARBS_LABEL to "Koolhydraten (g)",
            StringKey.FAT_LABEL to "Vet (g)",
            StringKey.SAVE_MEAL to "Maaltijd Opslaan",
            StringKey.EDIT to "Bewerken",
            StringKey.DELETE to "Verwijderen",

            // Workout UI
            StringKey.WORKOUT_PLANS to "Trainingsplannen",
            StringKey.DELETE_WORKOUT_PLAN to "Trainingsplan Verwijderen",
            StringKey.DELETE_WORKOUT_CONFIRM to "Weet je zeker dat je dit trainingsplan wilt verwijderen? Deze actie kan niet ongedaan worden gemaakt.",
            StringKey.SHOW_GRID to "Raster Weergave",
            StringKey.SHOW_LIST to "Lijst Weergave",
            StringKey.ADD_NEW_PLAN to "Nieuw Plan Toevoegen",
            StringKey.NO_WORKOUT_PLAN to "Nog Geen Trainingsplan",
            StringKey.NO_WORKOUT_PLAN_DESC to "Stel je fitnessdoelen in om een persoonlijk plan te krijgen",
            StringKey.SET_UP_MY_GOALS to "Mijn Doelen Instellen",
            StringKey.EXERCISE_DETAILS to "Oefening Details",
            StringKey.SETS to "Sets",
            StringKey.REPS to "Herhalingen",
            StringKey.REST to "Rust",
            StringKey.AI_POWERED_TIPS to "AI-Aangedreven Tips",
            StringKey.DURATION to "Duur",
            StringKey.CATEGORY to "Categorie",
            StringKey.TODAYS_EXERCISES to "Oefeningen van Vandaag",
            StringKey.START_WORKOUT to "Training Starten",
            StringKey.WEEKS to "weken",

            // Discover
            StringKey.DISCOVER to "Ontdekken",
            StringKey.NO_ARTICLES_FOUND to "Geen artikelen gevonden",

            // Home dashboard
            StringKey.TODAYS_MEALS to "Maaltijden Vandaag",
            StringKey.MEALS_EATEN to "Gegeten",
            StringKey.CALCULATE_YOUR_NEEDS to "Bereken Je Dagelijkse Behoefte",
            StringKey.CALCULATE_YOUR_NEEDS_DESC to "Ontvang je persoonlijke BMR, TDEE en calorie doel",
            StringKey.NOT_SET to "—",
            StringKey.MEALS_PROGRESS to "Maaltijden",

            // Meal add options
            StringKey.GENERATE_WITH_AI to "Genereer met AI",
            StringKey.ADD_MANUALLY to "Handmatig Toevoegen",

            // Meal editor
            StringKey.ADD_INGREDIENT to "Ingrediënt Toevoegen",
            StringKey.ADD_INSTRUCTION to "Stap Toevoegen",
            StringKey.INGREDIENT_HINT to "bijv. 200g kipfilet",
            StringKey.INSTRUCTION_HINT to "bijv. Verwarm de oven voor op 180°C",
            StringKey.PREP_TIME_LABEL to "Bereidingstijd (min)",
            StringKey.DIETARY_TAGS to "Dieet Tags",
            StringKey.REMOVE to "Verwijderen",

            // Swap confirmation
            StringKey.SWAP_CONFIRM_TITLE to "Deze Maaltijd Wisselen?",
            StringKey.SWAP_CONFIRM_DESC to "AI genereert een nieuwe maaltijd voor dit moment. Dit vervangt de huidige maaltijd.",
            StringKey.MEAL_SWAPPED to "Maaltijd succesvol gewisseld",

            // Misc
            StringKey.UPCOMING_WORKOUT to "Komende Training",
            StringKey.REFRESH_TIPS to "Tips Vernieuwen"
        )
    )

    fun get(key: StringKey, language: Language): String {
        return strings[language]?.get(key) ?: strings[Language.ENGLISH]?.get(key) ?: key.name
    }
}
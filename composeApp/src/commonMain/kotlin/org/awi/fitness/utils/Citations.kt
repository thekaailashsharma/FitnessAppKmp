package org.awi.fitness.utils

data class Citation(
    val text: String,
    val sourceUrl: String
)

object Citations {
    // BMI and Body Composition Citations
    val BMI_CALCULATION = Citation(
        text = "BMI ranges and classifications based on World Health Organization (WHO) guidelines.",
        sourceUrl = "https://www.who.int/news-room/fact-sheets/detail/obesity-and-overweight"
    ) // WHO defines BMI thresholds for overweight and obesity :contentReference[oaicite:1]{index=1}

    val BODY_FAT_RANGES = Citation(
        text = "Body fat percentage ranges based on American Council on Exercise (ACE) guidelines.",
        sourceUrl = "https://www.healthline.com/health/exercise-fitness/ideal-body-fat-percentage"
    ) // ACE essential/athletes/fitness/average/obese ranges :contentReference[oaicite:2]{index=2}

    // Calorie and Metabolism Citations
    val BMR_CALCULATION = Citation(
        text = "BMR calculation uses the Mifflin–St Jeor Equation, validated by the Academy of Nutrition and Dietetics.",
        sourceUrl = "https://www.eatrightpro.org/news-center/practice-trends/adjusted-or-ideal-body-weight-for-nutrition-assessment"
    )

    val ACTIVITY_LEVEL_MULTIPLIERS = Citation(
        text = "Activity level multipliers based on WHO/FAO/UNU (2004) human energy requirements.",
        sourceUrl = "https://www.who.int/publications/i/item/9241546743"
    )

    val CALORIE_ADJUSTMENT = Citation(
        text = "Calorie adjustment of ±500 kcal/day for safe weight management (NIH guidelines).",
        sourceUrl = "https://www.strengthlog.com/nutrition-for-strength-training"
    )

    // Workout and Exercise Citations
    val WORKOUT_FREQUENCY = Citation(
        text = "Exercise frequency recommendations based on American College of Sports Medicine (ACSM) guidelines.",
        sourceUrl = "https://pubmed.ncbi.nlm.nih.gov/20048509/"
    )

    val REST_PERIODS = Citation(
        text = "Rest period recommendations based on National Strength and Conditioning Association (NSCA) guidelines.",
        sourceUrl = "https://www.nsca.com/education/articles/kinetic-select/rest-period-recommendations/"
    )

    val EXERCISE_INTENSITY = Citation(
        text = "Exercise intensity zones based on American Heart Association recommendations.",
        sourceUrl = "https://www.heart.org/en/healthy-living/fitness/fitness-basics/aha-recs-for-physical-activity-in-adults"
    )

    // Weight Management Citations
    val WEIGHT_LOSS_RATE = Citation(
        text = "Safe weight loss of 0.5–1 kg/week based on CDC guidelines.",
        sourceUrl = "https://pmc.ncbi.nlm.nih.gov/articles/PMC7052702"
    )

    val MEASUREMENT_FREQUENCY = Citation(
        text = "Body composition tracking guidelines based on ACE standards.",
        sourceUrl = "https://download.tomtom.com/open/manuals/band/html/en-us/ACEBodyCompositionPercentageChart-Ibiza.htm"
    )

    // Nutrition Citations
    val MACRO_DISTRIBUTION = Citation(
        text = "Macronutrient distribution ranges from Dietary Guidelines for Americans 2020‑2025.",
        sourceUrl = "https://www.healthline.com/nutrition/best-macronutrient-ratio"
    )

    val PROTEIN_REQUIREMENTS = Citation(
        text = "Protein requirements based on the International Society of Sports Nutrition position stand (2017).",
        sourceUrl = "https://jissn.biomedcentral.com/articles/10.1186/s12970-017-0177-8"
    )

    val MEAL_TIMING = Citation(
        text = "Meal timing recommendations from ISSN nutrient timing position stand (2020).",
        sourceUrl = "https://jissn.biomedcentral.com/articles/10.1186/s12970-020-00383-4"
    )

    // Disclaimer
    val MEDICAL_DISCLAIMER = Citation(
        text = "This app provides general fitness information and is not a substitute for professional medical advice.",
        sourceUrl = "https://www.mayoclinic.org/healthy-lifestyle/fitness/expert-answers/exercise/faq-20057916"
    )
}

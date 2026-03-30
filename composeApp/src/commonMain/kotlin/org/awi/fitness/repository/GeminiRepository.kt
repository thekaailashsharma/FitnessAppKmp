package org.awi.fitness.repository

import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.awi.fitness.data.ActivityLevel
import org.awi.fitness.data.Gender
import org.awi.fitness.data.Goal
import org.awi.fitness.data.MeasurementEntry
import org.awi.fitness.data.WeighInEntry
import org.awi.fitness.model.DietaryType
import org.awi.fitness.model.GeneratedMealSwapResponse
import org.awi.fitness.model.MealGoal
import org.awi.fitness.model.MealPlanGenerationResponse
import org.awi.fitness.model.MealSlot
import org.awi.fitness.network.ApiService

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent
)

@Serializable
data class GeminiError(
    val message: String
)

@Serializable
data class WorkoutGenerationResponse(
    val workoutName: String,
    val description: String,
    val exercises: List<GeneratedExercise>,
    val tips: List<String>,
    val estimatedDuration: Int,
    val difficulty: String,
    val category: String
)

@Serializable
data class GeneratedExercise(
    val name: String,
    val description: String,
    val sets: Int,
    val reps: Int,
    val restTime: Int,
    val tips: String
)

@Serializable
data class CalorieCalculationResponse(
    val bmr: Float,
    val tdee: Float,
    val targetCalories: Float
)

@Serializable
data class MeasurementAnalysis(
    val insights: List<String>,
    val trends: Map<String, String>,
    val recommendations: List<String>
)

class GeminiRepository : ApiService() {
    companion object {
        private const val GEMINI_API_KEY = "AIzaSyDAAoM5EaGDHMXSqkwgALTJ0hbcnIYbuGc"
        private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
    }

    private val geminiJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private fun extractJsonFromResponse(raw: String): String {
        var cleaned = raw
            .replace("```json", "")
            .replace("```", "")
            .trim()
        val firstBrace = cleaned.indexOf('{')
        val lastBrace = cleaned.lastIndexOf('}')
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1)
        }
        return cleaned
    }

    private fun buildGeminiRequest(prompt: String) = GeminiRequest(
        contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
    )

    private suspend fun callGemini(prompt: String): Result<String> {
        return try {
            val url = "$GEMINI_BASE_URL?key=$GEMINI_API_KEY"
            val (response, status) = post<GeminiResponse>(url = url, body = buildGeminiRequest(prompt))
            if (status.isSuccess()) {
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: return Result.failure(Exception("No response generated"))
                Result.success(text)
            } else {
                Result.failure(Exception(response.error?.message ?: "Gemini API error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateMealPlan(
        goal: MealGoal,
        dietaryType: DietaryType,
        restrictions: String,
        mealsPerDay: Int,
        targetCalories: Int,
        targetProtein: Int,
        targetCarbs: Int,
        targetFat: Int,
        language: String = "en"
    ): Result<MealPlanGenerationResponse> {
        val slots = if (mealsPerDay == 4) "BREAKFAST, LUNCH, DINNER, SNACK" else "BREAKFAST, LUNCH, DINNER"
        val totalMeals = mealsPerDay * 7
        val langInstruction = if (language == "nl") "\nIMPORTANT: All text content (planName, description, meal names, ingredients, instructions, dietaryTags, shopping list category names and items) MUST be written entirely in Dutch (Nederlands). JSON keys must remain in English." else ""

        val prompt = """
You are a professional nutritionist AI. Generate a complete 7-day meal plan as valid JSON.$langInstruction

User Profile:
- Goal: ${goal.displayName}
- Dietary Preference: ${dietaryType.displayName}
- Restrictions/Allergies: ${restrictions.ifBlank { "None" }}
- Meals per day: $mealsPerDay ($slots)
- Daily calorie target: $targetCalories kcal
- Daily macros: ${targetProtein}g protein, ${targetCarbs}g carbs, ${targetFat}g fat

Rules:
- Generate exactly $totalMeals meals total (7 days × $mealsPerDay meals)
- Each day (1=Monday to 7=Sunday) must have exactly $mealsPerDay meals
- Use ID format "d{day}_{slot}" (e.g. "d1_breakfast", "d3_lunch")
- Daily macros should approximately sum to the calorie target
- Max 8 ingredients per meal with quantities
- Max 5 instruction steps per meal
- Meal names under 40 characters
- Vary meals across all 7 days — no repeated meals
- prepTimeMinutes should be realistic (5-45 minutes)

Respond with ONLY valid JSON, no markdown fences, no explanation text:
{"planName":"string","description":"2 sentences max","meals":[{"id":"d1_breakfast","name":"Meal Name","mealSlot":"BREAKFAST","dayOfWeek":1,"calories":450,"protein":35,"carbs":40,"fat":15,"ingredients":["200g item 1","1 item 2"],"instructions":["Step 1","Step 2"],"prepTimeMinutes":10,"dietaryTags":["Tag1"]}],"shoppingList":{"Proteins":["500g chicken breast"],"Dairy":["500g Greek yogurt"],"Produce":["3 bananas"],"Grains":["500g brown rice"],"Pantry":["olive oil"]}}
        """.trimIndent()

        return try {
            val rawText = callGemini(prompt).getOrThrow()
            val cleanJson = extractJsonFromResponse(rawText)
            val parsed = geminiJson.decodeFromString<MealPlanGenerationResponse>(cleanJson)

            if (parsed.meals.isEmpty()) {
                return Result.failure(Exception("Generated plan has no meals"))
            }
            Result.success(parsed)
        } catch (firstError: Exception) {
            try {
                val retryPrompt = "$prompt\n\nCRITICAL: Output MUST be valid JSON only. No text before or after the JSON object."
                val rawText = callGemini(retryPrompt).getOrThrow()
                val cleanJson = extractJsonFromResponse(rawText)
                val parsed = geminiJson.decodeFromString<MealPlanGenerationResponse>(cleanJson)
                if (parsed.meals.isEmpty()) Result.failure(Exception("Generated plan has no meals"))
                else Result.success(parsed)
            } catch (retryError: Exception) {
                Result.failure(Exception("Failed to generate meal plan. Please try again."))
            }
        }
    }

    suspend fun generateMealSwap(
        mealSlot: MealSlot,
        dayOfWeek: Int,
        dietaryType: DietaryType,
        targetCalories: Int,
        targetProtein: Int,
        targetCarbs: Int,
        targetFat: Int,
        existingMealNames: List<String>,
        restrictions: String
    ): Result<GeneratedMealSwapResponse> {
        val prompt = """
You are a professional nutritionist AI. Generate a single replacement ${mealSlot.displayName} meal.

Requirements:
- Dietary preference: ${dietaryType.displayName}
- Restrictions: ${restrictions.ifBlank { "None" }}
- Target macros for this meal: approximately ${targetCalories / 3} kcal, ${targetProtein / 3}g protein, ${targetCarbs / 3}g carbs, ${targetFat / 3}g fat
- Must be DIFFERENT from these meals: ${existingMealNames.joinToString(", ")}
- Max 8 ingredients with quantities
- Max 5 instruction steps

Respond with ONLY valid JSON, no markdown, no explanation:
{"id":"swap_d${dayOfWeek}_${mealSlot.name.lowercase()}","name":"Meal Name","mealSlot":"${mealSlot.name}","dayOfWeek":$dayOfWeek,"calories":450,"protein":35,"carbs":40,"fat":15,"ingredients":["200g item"],"instructions":["Step 1"],"prepTimeMinutes":10,"dietaryTags":["Tag"]}
        """.trimIndent()

        return try {
            val rawText = callGemini(prompt).getOrThrow()
            val cleanJson = extractJsonFromResponse(rawText)
            Result.success(geminiJson.decodeFromString<GeneratedMealSwapResponse>(cleanJson))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to generate meal swap. Please try again."))
        }
    }

    suspend fun generateWorkoutPlan(
        goal: String,
        fitnessLevel: String,
        workoutDays: Int,
        specificRequirements: String,
        language: String = "en"
    ): Result<WorkoutGenerationResponse> {
        val splitAdvice = when {
            workoutDays <= 2 -> "Full Body each day"
            workoutDays == 3 -> "Push / Pull / Legs split"
            workoutDays == 4 -> "Upper / Lower alternating split"
            workoutDays >= 5 -> "Push / Pull / Legs / Upper / Lower split"
            else -> "Full Body"
        }
        val exercisesPerDay = when (fitnessLevel.uppercase()) {
            "BEGINNER" -> 4
            "INTERMEDIATE" -> 5
            else -> 6
        }
        val totalExercises = exercisesPerDay * workoutDays

        val langInstruction = if (language == "nl") "\nIMPORTANT: All text content (workoutName, description, exercise names, exercise descriptions, tips) MUST be written entirely in Dutch (Nederlands). JSON keys must remain in English." else ""

        val prompt = """
You are a professional gym trainer AI. Generate a structured gym workout plan as valid JSON.$langInstruction

User Profile:
- Goal: $goal
- Fitness Level: $fitnessLevel
- Workout Days: $workoutDays days per week
- Split Recommendation: $splitAdvice
- Specific Requirements: ${specificRequirements.ifBlank { "None" }}

Rules:
- Generate exactly $totalExercises exercises total ($exercisesPerDay exercises × $workoutDays days)
- Focus on GYM exercises: barbell, dumbbell, cable machine, Smith machine, bodyweight compound lifts
- Always include compound lifts: bench press, squat, deadlift, overhead press, barbell row, pull-ups
- Add isolation work: bicep curls, tricep extensions, lateral raises, leg curls, calf raises
- Each exercise must have sets (3-5), reps (6-15), and rest time in seconds (60-180)
- Include a brief description of proper form for each exercise
- Include practical tips for each exercise
- The plan name should reflect the split type (e.g. "Push/Pull/Legs Program")
- estimatedDuration is total weekly workout time in minutes
- difficulty must be exactly: BEGINNER, INTERMEDIATE, or ADVANCED
- category must be exactly: STRENGTH, CARDIO, HIIT, FLEXIBILITY, or YOGA

Respond with ONLY valid JSON, no markdown fences, no explanation text:
{"workoutName":"Plan Name","description":"2 sentences max","exercises":[{"name":"Barbell Bench Press","description":"Lie on bench, grip bar shoulder-width, lower to chest, press up","sets":4,"reps":8,"restTime":120,"tips":"Keep feet flat, arch back slightly"}],"tips":["tip1","tip2","tip3"],"estimatedDuration":60,"difficulty":"INTERMEDIATE","category":"STRENGTH"}
        """.trimIndent()

        return try {
            val rawText = callGemini(prompt).getOrThrow()
            val cleanJson = extractJsonFromResponse(rawText)
            val workoutResponse = geminiJson.decodeFromString<WorkoutGenerationResponse>(cleanJson)
            if (workoutResponse.exercises.isEmpty()) {
                return Result.failure(Exception("Generated plan has no exercises"))
            }
            Result.success(workoutResponse)
        } catch (firstError: Exception) {
            try {
                val retryPrompt = "$prompt\n\nCRITICAL: Output MUST be valid JSON only. No text before or after the JSON object."
                val rawText = callGemini(retryPrompt).getOrThrow()
                val cleanJson = extractJsonFromResponse(rawText)
                val workoutResponse = geminiJson.decodeFromString<WorkoutGenerationResponse>(cleanJson)
                if (workoutResponse.exercises.isEmpty()) Result.failure(Exception("Generated plan has no exercises"))
                else Result.success(workoutResponse)
            } catch (retryError: Exception) {
                Result.failure(Exception("Failed to generate workout plan. Please try again."))
            }
        }
    }

    suspend fun calculateCalories(
        weight: Float,
        height: Float,
        age: Int,
        gender: Gender,
        activityLevel: ActivityLevel,
        goal: Goal
    ): Result<CalorieCalculationResponse> {
        return try {
            val prompt = """
                Calculate calories for a person with the following details:
                Weight: $weight kg
                Height: $height cm
                Age: $age years
                Gender: $gender
                Activity Level: $activityLevel
                Goal: $goal
                
                Please provide the response in this exact JSON format:
                {
                    "bmr": float,
                    "tdee": float,
                    "targetCalories": float
                }
                
                BMR should be calculated using the Mifflin-St Jeor Equation.
                TDEE should be BMR multiplied by activity level factor.
                Target calories should be adjusted based on the goal.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = prompt)
                        )
                    )
                )
            )

            val url = "$GEMINI_BASE_URL?key=$GEMINI_API_KEY"
            val (response, status) = post<GeminiResponse>(
                url = url,
                body = request
            )

            if (status.isSuccess()) {
                val generatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: return Result.failure(Exception("No response generated"))

                val cleanJson = generatedText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                val calorieResponse = kotlinx.serialization.json.Json.decodeFromString<CalorieCalculationResponse>(cleanJson)
                Result.success(calorieResponse)
            } else {
                Result.failure(Exception(response.error?.message ?: "Failed to calculate calories"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeMeasurements(
        measurements: List<MeasurementEntry>,
        weighIns: List<WeighInEntry>
    ): Result<MeasurementAnalysis> {
        return try {
            val prompt = """
                Analyze the following body measurements and weight data:
                
                Measurements (waist, hips, arms in cm):
                ${measurements.joinToString("\n") { 
                    "${it.date}: Waist=${it.waist}, Hips=${it.hips}, Arms=${it.arms}" 
                }}
                
                Weight entries (in kg):
                ${weighIns.joinToString("\n") { 
                    "${it.date}: ${it.weight}kg ${if (it.note.isNotEmpty()) "- ${it.note}" else ""}" 
                }}
                
                Please provide the response in this exact JSON format:
                {
                    "insights": [
                        "Key observation 1",
                        "Key observation 2"
                    ],
                    "trends": {
                        "waist": "trend description",
                        "hips": "trend description",
                        "arms": "trend description",
                        "weight": "trend description"
                    },
                    "recommendations": [
                        "recommendation 1",
                        "recommendation 2"
                    ]
                }
                
                Focus on meaningful changes and patterns. If there's insufficient data, mention that in the insights.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = prompt)
                        )
                    )
                )
            )

            val url = "$GEMINI_BASE_URL?key=$GEMINI_API_KEY"
            val (response, status) = post<GeminiResponse>(
                url = url,
                body = request
            )

            if (status.isSuccess()) {
                val generatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: return Result.failure(Exception("No response generated"))

                val cleanJson = generatedText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                val analysis = kotlinx.serialization.json.Json.decodeFromString<MeasurementAnalysis>(cleanJson)
                Result.success(analysis)
            } else {
                Result.failure(Exception(response.error?.message ?: "Failed to analyze measurements"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 
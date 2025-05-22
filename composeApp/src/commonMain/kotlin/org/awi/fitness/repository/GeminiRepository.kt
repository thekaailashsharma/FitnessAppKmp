package org.awi.fitness.repository

import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import org.awi.fitness.data.ActivityLevel
import org.awi.fitness.data.Gender
import org.awi.fitness.data.Goal
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

class GeminiRepository : ApiService() {
    companion object {
        private const val GEMINI_API_KEY = "AIzaSyBYjPI1XnhwFFFZznzw3XLpr1RKcl0tc7U" // Replace with actual key
        private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
    }

    suspend fun generateWorkoutPlan(
        goal: String,
        fitnessLevel: String,
        workoutDays: Int,
        specificRequirements: String
    ): Result<WorkoutGenerationResponse> {
        return try {
            val prompt = """
                Generate a personalized workout plan with the following requirements:
                - Fitness Goal: $goal
                - Fitness Level: $fitnessLevel
                - Workout Days: $workoutDays days per week
                - Specific Requirements: $specificRequirements

                Please provide the response in this exact JSON format:
                {
                    "workoutName": "Name of the workout plan",
                    "description": "Brief description",
                    "exercises": [
                        {
                            "name": "Exercise name",
                            "description": "How to perform",
                            "sets": number,
                            "reps": number,
                            "restTime": seconds,
                            "tips": "Important tips"
                        }
                    ],
                    "tips": ["tip1", "tip2", "tip3"],
                    "estimatedDuration": minutes,
                    "difficulty": "BEGINNER/INTERMEDIATE/ADVANCED",
                    "category": "STRENGTH/CARDIO/HIIT/FLEXIBILITY/YOGA"
                }
                
                Keep the response concise and focused on essential information.
                Ensure all exercises are appropriate for the specified fitness level.
                Include 4-6 exercises maximum.
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
                body = request,
            )

            if (status.isSuccess()) {
                val generatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: return Result.failure(Exception("No response generated"))

                // Clean up the JSON response by removing markdown code block markers
                val cleanJson = generatedText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                // Parse the JSON response into WorkoutGenerationResponse
                val workoutResponse = kotlinx.serialization.json.Json.decodeFromString<WorkoutGenerationResponse>(cleanJson)
                Result.success(workoutResponse)
            } else {
                Result.failure(Exception(response.error?.message ?: "Failed to generate workout plan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
} 
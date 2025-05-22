package org.awi.fitness.repository

import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.awi.fitness.model.*
import org.awi.fitness.network.ApiService

class WorkoutRepository : ApiService() {
    companion object {
        private const val PROJECT_ID = "fitness-admin-73a72"
    }

    fun getAllWorkoutPlans(): Flow<List<WorkoutPlanWithExercises>> = flow {
        try {
            println("Getting all workout plans...")
            val token = userSettings.authToken ?: throw Exception("Not authenticated")
            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/workout_plans"
            
            val (response, status) = get<FirestoreListResponse<WorkoutPlanFields>>(url, token)
            println("Workout plans response status: $status")

            if (status.isSuccess()) {
                val plans = response.documents?.map { it.toWorkoutPlan() } ?: emptyList()
                println("Retrieved ${plans.size} workout plans")
                val plansWithExercises = plans.map { plan ->
                    println("Getting exercises for plan: ${plan.id}")
                    val exercises = getExercisesForPlan(plan.id).getOrDefault(emptyList())
                    println("Retrieved ${exercises.size} exercises for plan: ${plan.id}")
                    WorkoutPlanWithExercises(plan, exercises)
                }
                emit(plansWithExercises)
            } else {
                println("Failed to get workout plans. Status: $status")
                emit(emptyList())
            }
        } catch (e: Exception) {
            println("Error getting workout plans: ${e.message}")
            e.printStackTrace()
            emit(emptyList())
        }
    }

    private suspend fun getExercisesForPlan(planId: String): Result<List<Exercise>> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/exercises"
            
            val (response, status) = get<FirestoreListResponse<ExerciseFields>>(url, token)

            if (status.isSuccess()) {
                val exercises = response.documents
                    ?.map { it.toExercise() }
                    ?.filter { it.planId == planId }
                    ?: emptyList()
                Result.success(exercises)
            } else {
                Result.failure(Exception("Failed to fetch exercises"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setExerciseCompleted(exerciseId: String, completed: Boolean): Result<Unit> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/exercises/$exerciseId"
            
            val exercise = Exercise(id = exerciseId, isCompleted = completed)
            val request = exercise.toFirestoreRequest()
            val (_, status) = patch<Unit>(url, request, token)

            if (status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update exercise"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertWorkoutPlan(plan: WorkoutPlan): Result<String> {
        return try {
            println("Inserting workout plan: ${plan.name}")
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/workout_plans"
            
            val request = plan.toFirestoreRequest()
            println("Request body: ${kotlinx.serialization.json.Json.encodeToString(WorkoutPlanFirestoreRequest.serializer(), request)}")
            
            val (response, status) = post<FirestoreResponse<WorkoutPlanFields>>(url, request, token)
            println("Insert workout plan response status: $status")
            println("Full response: $response")

            if (status.isSuccess()) {
                val planId = response.name?.split("/")?.last() ?: ""
                println("Successfully created workout plan with ID: $planId")
                Result.success(planId)
            } else {
                println("Failed to create workout plan. Status: $status")
                println("Response: $response")
                Result.failure(Exception("Failed to create workout plan. Status: $status"))
            }
        } catch (e: Exception) {
            println("Error creating workout plan: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun insertExercise(exercise: Exercise): Result<String> {
        return try {
            println("Inserting exercise: ${exercise.name} for plan: ${exercise.planId}")
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/exercises"
            
            val request = exercise.toFirestoreRequest()
            println("Request body: $request")
            
            val (response, status) = post<FirestoreResponse<ExerciseFields>>(url, request, token)
            println("Insert exercise response status: $status")

            if (status.isSuccess()) {
                val exerciseId = response.name?.split("/")?.last() ?: ""
                println("Successfully created exercise with ID: $exerciseId")
                Result.success(exerciseId)
            } else {
                println("Failed to create exercise. Status: $status")
                println("Response error: ${response.error?.error}")
                Result.failure(Exception("Failed to create exercise: ${response.error?.error}"))
            }
        } catch (e: Exception) {
            println("Error creating exercise: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun getCompletionCountForDay(planId: String, date: Long): Flow<Int> = flow {
        try {
            val token = userSettings.authToken ?: throw Exception("Not authenticated")
            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/exercises"
            
            val (response, status) = get<FirestoreListResponse<ExerciseFields>>(url, token)

            if (status.isSuccess()) {
                val completedCount = response.documents
                    ?.map { it.toExercise() }
                    ?.count { it.planId == planId && it.isCompleted }
                    ?: 0
                emit(completedCount)
            } else {
                emit(0)
            }
        } catch (e: Exception) {
            emit(0)
        }
    }

    fun getRecentActivities(planId: String): Flow<List<WorkoutActivity>> = flow {
        try {
            val token = userSettings.authToken ?: throw Exception("Not authenticated")
            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/activities"
            
            val (response, status) = get<FirestoreListResponse<WorkoutActivityFields>>(url, token)

            if (status.isSuccess()) {
                val activities = response.documents
                    ?.map { it.toWorkoutActivity() }
                    ?.filter { it.planId == planId }
                    ?.sortedByDescending { it.timestamp }
                    ?: emptyList()
                emit(activities)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Helper functions to convert Firestore documents to models
    private fun FirestoreDocument<*>.toWorkoutPlan(): WorkoutPlan {
        val fields = this.fields as? Map<*, *>
        return WorkoutPlan(
            id = this.name?.split("/")?.last() ?: "",
            name = (fields?.get("name") as? Map<*, *>)?.get("stringValue") as? String ?: "",
            description = (fields?.get("description") as? Map<*, *>)?.get("stringValue") as? String ?: "",
            difficulty = WorkoutDifficulty.valueOf((fields?.get("difficulty") as? Map<*, *>)?.get("stringValue") as? String ?: "BEGINNER"),
            duration = ((fields?.get("duration") as? Map<*, *>)?.get("integerValue") as? String)?.toIntOrNull() ?: 12,
            category = WorkoutCategory.valueOf((fields?.get("category") as? Map<*, *>)?.get("stringValue") as? String ?: "STRENGTH"),
            imageUrl = (fields?.get("imageUrl") as? Map<*, *>)?.get("stringValue") as? String
        )
    }

    private fun FirestoreDocument<*>.toExercise(): Exercise {
        val fields = this.fields as? Map<*, *>
        return Exercise(
            id = this.name?.split("/")?.last() ?: "",
            planId = (fields?.get("planId") as? Map<*, *>)?.get("stringValue") as? String ?: "",
            name = (fields?.get("name") as? Map<*, *>)?.get("stringValue") as? String ?: "",
            description = (fields?.get("description") as? Map<*, *>)?.get("stringValue") as? String ?: "",
            sets = ((fields?.get("sets") as? Map<*, *>)?.get("integerValue") as? String)?.toIntOrNull() ?: 0,
            reps = ((fields?.get("reps") as? Map<*, *>)?.get("integerValue") as? String)?.toIntOrNull() ?: 0,
            restTime = ((fields?.get("restTime") as? Map<*, *>)?.get("integerValue") as? String)?.toIntOrNull() ?: 0,
            videoUrl = (fields?.get("videoUrl") as? Map<*, *>)?.get("stringValue") as? String,
            thumbnailUrl = (fields?.get("thumbnailUrl") as? Map<*, *>)?.get("stringValue") as? String,
            isCompleted = (fields?.get("isCompleted") as? Map<*, *>)?.get("booleanValue") as? Boolean ?: false,
            dayOfWeek = ((fields?.get("dayOfWeek") as? Map<*, *>)?.get("integerValue") as? String)?.toIntOrNull() ?: 1,
            orderInDay = ((fields?.get("orderInDay") as? Map<*, *>)?.get("integerValue") as? String)?.toIntOrNull() ?: 0
        )
    }

    private fun FirestoreDocument<*>.toWorkoutActivity(): WorkoutActivity {
        val fields = this.fields as? Map<*, *>
        return WorkoutActivity(
            id = this.name?.split("/")?.last() ?: "",
            type = ActivityType.valueOf((fields?.get("type") as? Map<*, *>)?.get("stringValue") as? String ?: "EXERCISE_COMPLETED"),
            description = (fields?.get("description") as? Map<*, *>)?.get("stringValue") as? String ?: "",
            timestamp = ((fields?.get("timestamp") as? Map<*, *>)?.get("integerValue") as? String)?.toLongOrNull() ?: 0,
            exerciseId = (fields?.get("exerciseId") as? Map<*, *>)?.get("stringValue") as? String ?: "",
            planId = (fields?.get("planId") as? Map<*, *>)?.get("stringValue") as? String ?: ""
        )
    }
} 
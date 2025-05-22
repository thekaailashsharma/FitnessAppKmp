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

    suspend fun getAllWorkoutPlans(): Result<List<WorkoutPlanWithExercises>> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/workout_plans"
            
            val (plansResponse, plansStatus) = get<FirestoreListResponse<WorkoutPlanFields>>(url, token)
            println("Plans response: $plansResponse")

            if (!plansStatus.isSuccess()) {
                return Result.failure(Exception("Failed to fetch workout plans"))
            }

            val plans = plansResponse.documents?.map { it.toWorkoutPlan() } ?: emptyList()
            val plansWithExercises = mutableListOf<WorkoutPlanWithExercises>()

            // For each plan, fetch its exercises
            for (plan in plans) {
                val exercisesUrl = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/exercises"
                val (exercisesResponse, exercisesStatus) = get<FirestoreListResponse<ExerciseFields>>(exercisesUrl, token)

                if (!exercisesStatus.isSuccess()) {
                    return Result.failure(Exception("Failed to fetch exercises for plan: ${plan.id}"))
                }

                val exercises = exercisesResponse.documents
                    ?.map { it.toExercise() }
                    ?.filter { it.planId == plan.id }
                    ?.sortedBy { it.orderInDay }
                    ?: emptyList()

                plansWithExercises.add(WorkoutPlanWithExercises(plan, exercises))
            }

            Result.success(plansWithExercises)
        } catch (e: Exception) {
            println("Error fetching workout plans: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
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

    suspend fun setExerciseCompleted(exercise: Exercise, completed: Boolean): Result<Unit> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val exerciseUrl = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/exercises/${exercise.id}"
            
            // Update completion status using the provided exercise data
            val updatedExercise = exercise.copy(isCompleted = completed)
            
            // Create request with all fields
            val request = ExerciseFirestoreRequest(
                fields = ExerciseFields(
                    planId = StringValue(updatedExercise.planId),
                    name = StringValue(updatedExercise.name),
                    description = StringValue(updatedExercise.description),
                    sets = IntegerValue(updatedExercise.sets.toString()),
                    reps = IntegerValue(updatedExercise.reps.toString()),
                    restTime = IntegerValue(updatedExercise.restTime.toString()),
                    videoUrl = updatedExercise.videoUrl?.let { StringValue(it) },
                    thumbnailUrl = updatedExercise.thumbnailUrl?.let { StringValue(it) },
                    isCompleted = BooleanValue(updatedExercise.isCompleted),
                    dayOfWeek = IntegerValue(updatedExercise.dayOfWeek.toString()),
                    orderInDay = IntegerValue(updatedExercise.orderInDay.toString())
                )
            )
            
            val (_, status) = patch<Unit>(exerciseUrl, request, token)

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
                val planId = response.fields?.id?.value ?: ""
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
                println("Response error: ${response.error?.message}")
                Result.failure(Exception("Failed to create exercise: ${response.error?.message}"))
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
    private fun FirestoreDocument<WorkoutPlanFields>.toWorkoutPlan(): WorkoutPlan {
        return fields?.toDomainModel() ?: WorkoutPlan()
    }

    private fun FirestoreDocument<ExerciseFields>.toExercise(): Exercise {
        val documentId = name?.split("/")?.last() ?: ""
        return fields?.toDomainModel(documentId) ?: Exercise()
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
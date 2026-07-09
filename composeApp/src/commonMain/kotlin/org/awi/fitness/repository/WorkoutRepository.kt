package org.awi.fitness.repository

import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.awi.fitness.model.*
import org.awi.fitness.network.ApiService

class WorkoutRepository : ApiService() {
    companion object {
        private const val PROJECT_ID = "awi-fitness-app"
    }

    suspend fun getAllWorkoutPlans(): Result<List<WorkoutPlanWithExercises>> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))

            val plansUrl = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/workout_plans"
            val (plansResponse, plansStatus) = get<FirestoreListResponse<WorkoutPlanFields>>(plansUrl, token)
            println("Plans response: $plansResponse")

            if (!plansStatus.isSuccess()) {
                return Result.failure(Exception("Failed to fetch workout plans"))
            }

            // Scope plans to the signed-in user. Legacy shared plans carry a blank
            // ownerId and are intentionally hidden (per product decision). If we can't
            // resolve an owner id we show none of the Firestore plans (the code-defined
            // pre-made plan is added by the UI and is unaffected).
            val owner = currentOwnerId()
            val allPlans = plansResponse.documents?.map { it.toWorkoutPlan() } ?: emptyList()
            val plans = if (owner.isBlank()) emptyList()
                else allPlans.filter { it.ownerId == owner }

            // Fetch all exercises once, then group by planId
            val exercisesUrl = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/exercises"
            val (exercisesResponse, exercisesStatus) = get<FirestoreListResponse<ExerciseFields>>(exercisesUrl, token)

            if (!exercisesStatus.isSuccess()) {
                return Result.failure(Exception("Failed to fetch exercises"))
            }

            val allExercises = exercisesResponse.documents
                ?.map { it.toExercise() }
                ?: emptyList()

            val exercisesByPlanId = allExercises.groupBy { it.planId }

            val plansWithExercises = plans.map { plan ->
                val exercises = (exercisesByPlanId[plan.id] ?: emptyList())
                    .sortedWith(compareBy({ it.dayOfWeek }, { it.orderInDay }))
                WorkoutPlanWithExercises(plan, exercises)
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
                // Use the Firestore DOCUMENT id (matches plan.id at load, which powers the
                //    exercises join). Previously returned the random `fields.id` UUID, so
                //    exercises were keyed by an id that never matched → empty plan detail.
                val planId = response.name?.split("/")?.lastOrNull()
                    ?: response.fields?.id?.value ?: ""
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

    suspend fun deleteWorkoutPlan(planId: String): Result<Unit> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            
            // Delete the workout plan
            val planUrl = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/workout_plans/$planId"
            val (_, planStatus) = delete<Unit>(planUrl, token)
            
            if (!planStatus.isSuccess()) {
                return Result.failure(Exception("Failed to delete workout plan"))
            }

            // Delete associated exercises
            val exercisesUrl = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/exercises"
            val (exercisesResponse, exercisesStatus) = get<FirestoreListResponse<ExerciseFields>>(exercisesUrl, token)
            
            if (exercisesStatus.isSuccess()) {
                exercisesResponse.documents
                    ?.map { it.toExercise() }
                    ?.filter { it.planId == planId }
                    ?.forEach { exercise ->
                        val exerciseUrl = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/exercises/${exercise.id}"
                        delete<Unit>(exerciseUrl, token)
                    }
            }

            Result.success(Unit)
        } catch (e: Exception) {
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
                    ?.mapNotNull { it.toWorkoutActivity() }
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

    /** Resolves the current user's owner id (userId preferred, email as fallback). */
    private fun currentOwnerId(): String =
        userSettings.userId?.takeIf { it.isNotBlank() }
            ?: userSettings.userEmail?.takeIf { it.isNotBlank() }
            ?: ""

    // Helper functions to convert Firestore documents to models
    private fun FirestoreDocument<WorkoutPlanFields>.toWorkoutPlan(): WorkoutPlan {
        val documentId = name?.split("/")?.last() ?: ""
        // toDomainModel now maps the proper `ownerId` field.
        return fields?.toDomainModel(documentId) ?: WorkoutPlan(id = documentId)
    }

    private fun FirestoreDocument<ExerciseFields>.toExercise(): Exercise {
        val documentId = name?.split("/")?.last() ?: ""
        return fields?.toDomainModel(documentId) ?: Exercise()
    }

    // Null-safe: rows with an unknown/missing `type` are skipped (return null) instead of
    // throwing, so one bad document no longer collapses the whole activity list to empty.
    private fun FirestoreDocument<*>.toWorkoutActivity(): WorkoutActivity? {
        val fields = this.fields as? Map<*, *> ?: return null
        val typeStr = (fields["type"] as? Map<*, *>)?.get("stringValue") as? String
        val type = runCatching { ActivityType.valueOf(typeStr ?: "") }.getOrNull() ?: return null
        return WorkoutActivity(
            id = this.name?.split("/")?.last() ?: "",
            type = type,
            description = (fields["description"] as? Map<*, *>)?.get("stringValue") as? String ?: "",
            timestamp = ((fields["timestamp"] as? Map<*, *>)?.get("integerValue") as? String)?.toLongOrNull() ?: 0,
            exerciseId = (fields["exerciseId"] as? Map<*, *>)?.get("stringValue") as? String ?: "",
            planId = (fields["planId"] as? Map<*, *>)?.get("stringValue") as? String ?: ""
        )
    }
} 
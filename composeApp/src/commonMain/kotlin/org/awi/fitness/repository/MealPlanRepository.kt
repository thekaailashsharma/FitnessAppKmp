package org.awi.fitness.repository

import io.ktor.client.request.delete
import io.ktor.client.request.headers
import io.ktor.http.isSuccess
import org.awi.fitness.model.FirestoreListResponse
import org.awi.fitness.model.FirestoreResponse
import org.awi.fitness.model.MealPlan
import org.awi.fitness.model.MealPlanFields
import org.awi.fitness.model.toFirestoreRequest
import org.awi.fitness.model.toMealPlan
import org.awi.fitness.network.ApiService
import org.awi.fitness.network.KtorClient

class MealPlanRepository : ApiService() {
    companion object {
        private const val PROJECT_ID = "awi-fitness-app"
        private const val COLLECTION = "user_meal_plans"
        private const val BASE_URL = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/$COLLECTION"
    }

    suspend fun createPlan(plan: MealPlan): Result<MealPlan> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val request = plan.toFirestoreRequest()
            val (response, status) = post<FirestoreResponse<MealPlanFields>>(BASE_URL, request, token)

            if (status.isSuccess()) {
                Result.success(response.toMealPlan())
            } else {
                Result.failure(Exception(response.error?.message ?: "Failed to create meal plan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPlansForUser(email: String): Result<List<MealPlan>> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val (response, status) = get<FirestoreListResponse<MealPlanFields>>(BASE_URL, token)

            if (status.isSuccess()) {
                val plans = response.documents
                    ?.map { it.toMealPlan() }
                    ?.filter { it.clientEmail == email }
                    ?: emptyList()
                Result.success(plans)
            } else {
                Result.failure(Exception("Failed to fetch meal plans"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePlan(plan: MealPlan): Result<MealPlan> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            if (plan.id.isBlank()) return Result.failure(Exception("Invalid plan ID"))

            val url = "$BASE_URL/${plan.id}"
            val request = plan.toFirestoreRequest()
            val (response, status) = patch<FirestoreResponse<MealPlanFields>>(url, request, token)

            if (status.isSuccess()) {
                Result.success(response.toMealPlan())
            } else {
                Result.failure(Exception("Failed to update meal plan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePlan(planId: String): Result<Unit> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val url = "$BASE_URL/$planId"
            val response = KtorClient.httpClient.delete(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete meal plan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package org.awi.fitness.repository

import io.ktor.http.isSuccess
import org.awi.fitness.model.FirestoreListResponse
import org.awi.fitness.model.Program
import org.awi.fitness.model.ProgramFields
import org.awi.fitness.model.ProgramType
import org.awi.fitness.model.toProgram
import org.awi.fitness.network.ApiService

class MealRepository : ApiService() {
    companion object {
        private const val PROJECT_ID = "fitness-admin-73a72"
    }

    suspend fun getMealsByEmail(email: String): Result<List<Program>> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))

            val url = "https://firestore.googleapis.com/v1/projects/${PROJECT_ID}/databases/(default)/documents/meals"

            val (response, status) = get<FirestoreListResponse<ProgramFields>>(url, token)

            if (status.isSuccess()) {
                Result.success(response.documents?.mapNotNull { doc ->
                    // Extract document ID from the full path
                    val docId = doc.name?.split("/")?.lastOrNull() ?: return@mapNotNull null
                    doc.toProgram().copy(id = docId)
                }?.filter { it.type == ProgramType.MEAL && email in it.assignedClients } ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch meals"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
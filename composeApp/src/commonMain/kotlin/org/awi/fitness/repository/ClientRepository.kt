package org.awi.fitness.repository

import io.ktor.http.isSuccess
import org.awi.fitness.model.Client
import org.awi.fitness.model.ClientFields
import org.awi.fitness.model.FirestoreListResponse
import org.awi.fitness.model.FirestoreResponse
import org.awi.fitness.model.NotificationFields
import org.awi.fitness.model.NotificationFieldsRequest
import org.awi.fitness.model.StringValue
import org.awi.fitness.model.toClient
import org.awi.fitness.model.toFirestoreRequest
import org.awi.fitness.network.ApiService
import org.awi.fitness.utils.DateUtils


class ClientRepository : ApiService() {
    companion object {
        internal const val PROJECT_ID = "fitness-admin-73a72" // Replace with your Firebase project ID
    }

    suspend fun createClient(client: Client): Result<Client> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            
            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/clients"
            
            val request = client.toFirestoreRequest()
            val (response, status) = post<FirestoreResponse<ClientFields>>(url, request, token)

            if (status.isSuccess()) {
                Result.success(response.toClient())
            } else {
                Result.failure(Exception(response.error?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClients(): Result<List<Client>> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            
            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/clients"
            
            val (response, status) = get<FirestoreListResponse<ClientFields>>(url, token)

            if (status.isSuccess()) {
                Result.success(response.documents?.map { it.toClient() } ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch clients"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateClient(client: Client): Result<Client> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            
            if (client.id.isBlank()) {
                return Result.failure(Exception("Invalid document ID"))
            }
            
            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/clients/${client.id}"
            
            val request = client.toFirestoreRequest()
            val (response, status) = patch<FirestoreResponse<ClientFields>>(url, request, token)

            if (status.isSuccess()) {
                Result.success(response.toClient())
            } else {
                Result.failure(Exception("Failed to update client"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteClient(clientId: String): Result<Unit> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            
            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/clients/$clientId"
            
            val (_, status) = delete<Unit>(url, token)

            if (status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete client"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClientByEmail(email: String): Result<Client?> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))

            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/clients"
            
            val (response, status) = get<FirestoreListResponse<ClientFields>>(url, token)

            if (status.isSuccess()) {
                val clients = response.documents?.map { it.toClient() }?.first { it.email == email }
                Result.success(clients)
            } else {
                Result.failure(Exception("Failed to fetch client"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNotificationFields(
        clientId: String,
        fcmToken: String?,
        timezone: String,
        language: String
    ): Result<Unit> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            if (clientId.isBlank()) return Result.failure(Exception("No client ID"))

            val now = kotlinx.datetime.Clock.System.now().toString()
            val request = NotificationFieldsRequest(
                fields = NotificationFields(
                    fcmToken = fcmToken?.let { StringValue(it) },
                    timezone = StringValue(timezone),
                    lastActiveAt = StringValue(now),
                    language = StringValue(language)
                )
            )

            val mask = listOfNotNull(
                "fcmToken",
                "timezone",
                "lastActiveAt",
                "language"
            ).joinToString("&updateMask.fieldPaths=")

            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/clients/$clientId?updateMask.fieldPaths=$mask"

            val (_, status) = patch<FirestoreResponse<ClientFields>>(url, request, token)
            if (status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to update notification fields"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearFcmToken(clientId: String): Result<Unit> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            if (clientId.isBlank()) return Result.failure(Exception("No client ID"))

            val request = NotificationFieldsRequest(
                fields = NotificationFields(
                    fcmToken = StringValue("")
                )
            )
            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/clients/$clientId?updateMask.fieldPaths=fcmToken"
            val (_, status) = patch<FirestoreResponse<ClientFields>>(url, request, token)
            if (status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to clear FCM token"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 
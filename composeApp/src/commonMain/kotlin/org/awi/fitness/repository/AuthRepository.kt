package org.awi.fitness.repository

import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.datetime.Clock
import org.awi.fitness.model.Client
import org.awi.fitness.model.ClientFields
import org.awi.fitness.model.FirebaseErrorType
import org.awi.fitness.model.FirestoreListResponse
import org.awi.fitness.model.SignUpFirebaseAuth
import org.awi.fitness.model.SignUpFirebaseResponse
import org.awi.fitness.model.toClient
import org.awi.fitness.network.ApiService
import org.awi.fitness.repository.ClientRepository.Companion.PROJECT_ID
import org.awi.fitness.utils.DateUtils

class AuthRepository : ApiService() {
    companion object {
        private const val API_KEY = "AIzaSyAQClgruJ0Q0l_B4v3J8Sv8gury_8NGK8g"
    }

    suspend fun signUp(email: String, password: String): Result<SignUpFirebaseResponse> {
        return try {
            val request = SignUpFirebaseAuth(email, password)
            val (response, status) = signUpFirebase(request)
            
            if (status.isSuccess()) {
                saveUserSession(response)
                Result.success(response)
            } else {
                val errorMessage = response.error?.error?.message ?: "Unknown error"
                Result.failure(FirebaseException(FirebaseErrorType.fromErrorMessage(errorMessage)))
            }
        } catch (e: Exception) {
            Result.failure(FirebaseException(FirebaseErrorType.NetworkError))
        }
    }

    suspend fun signIn(email: String, password: String): Result<SignUpFirebaseResponse> {
        return try {
            val request = SignUpFirebaseAuth(email, password)
            val (response, status) = signInFirebase(request)
            
            if (status.isSuccess()) {
                saveUserSession(response)
                Result.success(response)
            } else {
                val errorMessage = response.error?.error?.message ?: "Unknown error"
                Result.failure(FirebaseException(FirebaseErrorType.fromErrorMessage(errorMessage)))
            }
        } catch (e: Exception) {
            Result.failure(FirebaseException(FirebaseErrorType.NetworkError))
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val userId = userSettings.userId ?: return Result.failure(Exception("User ID not found"))
            val email = userSettings.userEmail ?: return Result.failure(Exception("Email not found"))

            // First delete the user's data from Firestore
            val clientRepository = ClientRepository()
            val clientResult = clientRepository.getClientByEmail(email)
            
            clientResult.fold(
                onSuccess = { client ->
                    if (client != null) {
                        clientRepository.deleteClient(client.id)
                    }
                },
                onFailure = { /* Ignore failure to delete client data */ }
            )

            // Then delete the Firebase Auth account
            val deleteUrl = "https://identitytoolkit.googleapis.com/v1/accounts:delete?key=$API_KEY"
            val request = mapOf("idToken" to token)
            val (_, status) = unAuthenticatedPost<Unit>(deleteUrl, request)

            if (status.isSuccess()) {
                // Clear all local data
                userSettings.clearUserData()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete account"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveUserSession(response: SignUpFirebaseResponse) {
        userSettings.apply {
            authToken = response.idToken
            refreshToken = response.refreshToken
            userEmail = response.email
            userId = response.localId
            tokenExpiryTime = (Clock.System.now().toEpochMilliseconds() + 
                (response.expiresIn?.toLong() ?: 0) * 1000).toString()
            isLoggedIn = true
        }
    }

    fun logout() {
        userSettings.clearUserData()
    }

    private suspend fun signUpFirebase(request: SignUpFirebaseAuth): Pair<SignUpFirebaseResponse, HttpStatusCode> {
        val signUpUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$API_KEY"
        return unAuthenticatedPost(signUpUrl, request)
    }

    private suspend fun signInFirebase(request: SignUpFirebaseAuth): Pair<SignUpFirebaseResponse, HttpStatusCode> {
        val signInUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$API_KEY"
        return unAuthenticatedPost(signInUrl, request)
    }

    suspend fun getClientByEmail(email: String): Result<Client?> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))

            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/clients"

            val (response, status) = unAuthenticatedGet<FirestoreListResponse<ClientFields>>(url, token)

            if (status.isSuccess()) {
                val clients = response.documents?.map { it.toClient() }?.firstOrNull { it.email == email && DateUtils.isDateValid(it.endDate) }
                Result.success(clients)
            } else {
                Result.failure(Exception("Failed to fetch client"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean {
        return userSettings.isLoggedIn
    }
}

class FirebaseException(val errorType: FirebaseErrorType) : Exception(errorType.message) 
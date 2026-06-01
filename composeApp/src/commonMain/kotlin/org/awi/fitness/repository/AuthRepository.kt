package org.awi.fitness.repository

import org.awi.fitness.utils.currentTimeMillis
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import org.awi.fitness.model.Client
import org.awi.fitness.model.ClientFields
import org.awi.fitness.model.FirebaseErrorType
import org.awi.fitness.model.FirestoreListResponse
import org.awi.fitness.model.SignUpFirebaseAuth
import org.awi.fitness.model.SignUpFirebaseResponse
import org.awi.fitness.model.toClient
import org.awi.fitness.network.ApiService
import org.awi.fitness.repository.ClientRepository.Companion.PROJECT_ID

class AuthRepository : ApiService() {
    companion object {
        private const val API_KEY = "AIzaSyAQClgruJ0Q0l_B4v3J8Sv8gury_8NGK8g"
    }

    suspend fun signUp(email: String, password: String): Result<SignUpFirebaseResponse> {
        return try {
            val (response, status) = signUpFirebase(SignUpFirebaseAuth(email, password))
            if (status.isSuccess()) {
                saveUserSession(response)
                Result.success(response)
            } else {
                val msg = response.error?.message ?: "Unknown error"
                Result.failure(FirebaseException(FirebaseErrorType.fromErrorMessage(msg)))
            }
        } catch (e: Exception) {
            Result.failure(FirebaseException(FirebaseErrorType.NetworkError))
        }
    }

    suspend fun signIn(email: String, password: String): Result<SignUpFirebaseResponse> {
        return try {
            val (response, status) = signInFirebase(SignUpFirebaseAuth(email, password))
            if (status.isSuccess()) {
                saveUserSession(response)
                Result.success(response)
            } else {
                val msg = response.error?.message ?: "Unknown error"
                Result.failure(FirebaseException(FirebaseErrorType.fromErrorMessage(msg)))
            }
        } catch (e: Exception) {
            Result.failure(FirebaseException(FirebaseErrorType.NetworkError))
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            val request = mapOf("requestType" to "PASSWORD_RESET", "email" to email)
            val url = "https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=$API_KEY"
            val (_, status) = unAuthenticatedPost<Map<String, String>>(url, request)
            if (status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to send reset email. Check the address and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val email = userSettings.userEmail ?: return Result.failure(Exception("Email not found"))

            // Best-effort delete client data
            runCatching {
                val clientResult = ClientRepository().getClientByEmail(email)
                clientResult.getOrNull()?.let { ClientRepository().deleteClient(it.id) }
            }

            val url = "https://identitytoolkit.googleapis.com/v1/accounts:delete?key=$API_KEY"
            val (_, status) = unAuthenticatedPost<Unit>(url, mapOf("idToken" to token))

            if (status.isSuccess()) {
                userSettings.clearUserData()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete account"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Used only by SplashScreen legacy client check — kept for backward compat but no longer blocks auth
    suspend fun getClientByEmail(email: String): Result<Client?> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/clients"
            val (response, status) = unAuthenticatedGet<FirestoreListResponse<ClientFields>>(url, token)
            if (status.isSuccess()) {
                val client = response.documents?.map { it.toClient() }
                    ?.firstOrNull { it.email == email }
                Result.success(client)
            } else {
                Result.failure(Exception("Failed to fetch client"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() { userSettings.clearUserData() }
    fun isLoggedIn(): Boolean = userSettings.isLoggedIn

    private fun saveUserSession(response: SignUpFirebaseResponse) {
        userSettings.apply {
            authToken = response.idToken
            refreshToken = response.refreshToken
            userEmail = response.email
            userId = response.localId
            tokenExpiryTime = (currentTimeMillis() +
                (response.expiresIn?.toLong() ?: 0) * 1000).toString()
            isLoggedIn = true
        }
    }

    private suspend fun signUpFirebase(req: SignUpFirebaseAuth): Pair<SignUpFirebaseResponse, HttpStatusCode> =
        unAuthenticatedPost("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$API_KEY", req)

    private suspend fun signInFirebase(req: SignUpFirebaseAuth): Pair<SignUpFirebaseResponse, HttpStatusCode> =
        unAuthenticatedPost("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$API_KEY", req)
}

class FirebaseException(val errorType: FirebaseErrorType) : Exception(errorType.message)

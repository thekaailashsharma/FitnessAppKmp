package org.awi.fitness.repository

import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.datetime.Clock
import org.awi.fitness.model.SignUpFirebaseAuth
import org.awi.fitness.model.SignUpFirebaseResponse
import org.awi.fitness.network.ApiService

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
                Result.failure(Exception(response.error?.error?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
                Result.failure(Exception(response.error?.error?.message ?: "Unknown error"))
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
        return post(signUpUrl, request)
    }

    private suspend fun signInFirebase(request: SignUpFirebaseAuth): Pair<SignUpFirebaseResponse, HttpStatusCode> {
        val signInUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$API_KEY"
        return post(signInUrl, request)
    }

    fun isLoggedIn(): Boolean {
        return userSettings.isLoggedIn
    }

    suspend fun checkAuthState(): Boolean {
        val token = userSettings.authToken
        val expiryTimeStr = userSettings.tokenExpiryTime
        
        if (token == null || expiryTimeStr == null) return false
        
        val expiryTime = expiryTimeStr.toLong()
        val currentTime = Clock.System.now().toEpochMilliseconds()
        
        if (currentTime >= expiryTime - 5 * 60 * 1000) {
            return refreshToken().isSuccess
        }
        
        return true
    }
} 
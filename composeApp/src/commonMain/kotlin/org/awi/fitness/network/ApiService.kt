package org.awi.fitness.network

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.Clock
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.RefreshTokenRequest
import org.awi.fitness.model.RefreshTokenResponse

abstract class ApiService {
    protected open val userSettings: UserSettings = UserSettings.getInstance()

    companion object {
        private const val API_KEY = "AIzaSyAQClgruJ0Q0l_B4v3J8Sv8gury_8NGK8g"
    }

    protected suspend fun refreshToken(): Result<RefreshTokenResponse> {
        return try {
            val refreshToken = userSettings.refreshToken ?: return Result.failure(Exception("No refresh token found"))
            
            val request = RefreshTokenRequest(
                grantType = "refresh_token",
                refreshToken = refreshToken
            )
            
            val (response, status) = post<RefreshTokenResponse>(
                "https://securetoken.googleapis.com/v1/token?key=$API_KEY",
                request
            )

            if (status.isSuccess()) {
                saveUserSession(response)
                Result.success(response)
            } else {
                Result.failure(Exception(response.error?.error?.message ?: "Failed to refresh token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveUserSession(response: RefreshTokenResponse) {
        userSettings.apply {
            authToken = response.idToken
            refreshToken = response.refreshToken
            userId = response.userId
            tokenExpiryTime = (Clock.System.now().toEpochMilliseconds() + 
                (response.expiresIn?.toLong() ?: 0) * 1000).toString()
            isLoggedIn = true
        }
    }

    protected suspend fun <T> handleRequest(
        requestCall: suspend (String?) -> Pair<T, HttpStatusCode>,
        token: String? = null
    ): Pair<T, HttpStatusCode> {
        val initialResponse = requestCall(token)
        
        if (initialResponse.second == HttpStatusCode.Unauthorized && token != null) {
            // Try to refresh the token
            val refreshResult = refreshToken()
            
            if (refreshResult.isSuccess) {
                // Retry the original request with the new token
                return requestCall(userSettings.authToken)
            }
        }
        
        return initialResponse
    }

    protected suspend inline fun <reified T> post(
        url: String,
        body: Any,
        token: String? = null,
        crossinline headers: HeadersBuilder.() -> Unit = {}
    ): Pair<T, HttpStatusCode> {
        return handleRequest({ currentToken ->
            KtorClient.httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
                headers {
                    headers()
                    currentToken?.let {
                        append(HttpHeaders.Authorization, "Bearer $currentToken")
                    }
                }
            }.let { response ->
                response.body<T>() to response.status
            }
        }, token)
    }

    protected suspend inline fun <reified T> get(endpoint: String, token: String? = null): Pair<T, HttpStatusCode> {
        return handleRequest({ currentToken ->
            KtorClient.httpClient.get(endpoint) {
                headers {
                    append("Accept", "application/json")
                    append("Accept", "*/*")
                    append("Content-Type", "application/json")
                    currentToken?.let {
                        append("Authorization", "Bearer $currentToken")
                    }
                }
            }.let { response ->
                response.body<T>() to response.status
            }
        }, token)
    }

    protected suspend inline fun <reified T> put(endpoint: String, body: Any, token: String? = null): Pair<T, HttpStatusCode> {
        return handleRequest({ currentToken ->
            KtorClient.httpClient.put(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(body)
                headers {
                    append("Accept", "application/json")
                    append("Accept", "*/*")
                    append("Content-Type", "application/json")
                    currentToken?.let {
                        append("Authorization", "Bearer $currentToken")
                    }
                }
            }.let { response ->
                response.body<T>() to response.status
            }
        }, token)
    }

    protected suspend inline fun <reified T> delete(endpoint: String, token: String? = null): Pair<T, HttpStatusCode> {
        return handleRequest({ currentToken ->
            KtorClient.httpClient.delete(endpoint) {
                headers {
                    append("Accept", "application/json")
                    append("Accept", "*/*")
                    append("Content-Type", "application/json")
                    currentToken?.let {
                        append("Authorization", "Bearer $currentToken")
                    }
                }
            }.let { response ->
                response.body<T>() to response.status
            }
        }, token)
    }

    protected suspend inline fun <reified T> patch(endpoint: String, body: Any, token: String? = null): Pair<T, HttpStatusCode> {
        return handleRequest({ currentToken ->
            KtorClient.httpClient.patch(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(body)
                headers {
                    append("Accept", "application/json")
                    append("Accept", "*/*")
                    append("Content-Type", "application/json")
                    currentToken?.let {
                        append("Authorization", "Bearer $currentToken")
                    }
                }
            }.let { response ->
                response.body<T>() to response.status
            }
        }, token)
    }
} 
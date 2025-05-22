package org.awi.fitness.network

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.datetime.Clock
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.ClientFields
import org.awi.fitness.model.FirestoreListResponse
import org.awi.fitness.model.RefreshTokenRequest
import org.awi.fitness.model.RefreshTokenResponse
import org.awi.fitness.model.toClient
import org.awi.fitness.repository.ClientRepository.Companion.PROJECT_ID
import org.awi.fitness.utils.DateUtils

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}

abstract class ApiService {
    protected open val userSettings: UserSettings = UserSettings.getInstance()

    companion object {
        private const val API_KEY = "AIzaSyAQClgruJ0Q0l_B4v3J8Sv8gury_8NGK8g"
    }

    private suspend fun validateClientStatus(): ValidationResult {
        val email = userSettings.userEmail ?: return ValidationResult.Invalid("Not authenticated")
        val token = userSettings.authToken ?: return ValidationResult.Invalid("Not authenticated")

        return try {
            val url =
                "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/clients"

            // Direct API call without using handleRequest to avoid infinite loop
            val response = KtorClient.httpClient.get(url) {
                headers {
                    append("Accept", "application/json")
                    append("Accept", "*/*")
                    append("Content-Type", "application/json")
                    append("Authorization", "Bearer $token")
                }
            }

            if (response.status.isSuccess()) {
                val clientsResponse = response.body<FirestoreListResponse<ClientFields>>()
                val client = clientsResponse.documents
                    ?.map { it.toClient() }
                    ?.firstOrNull { it.email == email }

                when {
                    client == null -> ValidationResult.Invalid("Account not found")
                    !DateUtils.isDateValid(client.endDate) -> ValidationResult.Invalid("Plan expired")
                    else -> ValidationResult.Valid
                }
            } else {
                ValidationResult.Invalid("Failed to verify account status")
            }
        } catch (e: Exception) {
            ValidationResult.Invalid("Failed to verify account status")
        }
    }

    protected suspend fun refreshToken(): Result<RefreshTokenResponse> {
        return try {
            val refreshToken = userSettings.refreshToken
                ?: return Result.failure(Exception("No refresh token found"))

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
                Result.failure(
                    Exception(
                        response.error?.error?.message ?: "Failed to refresh token"
                    )
                )
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
        // First validate client status
        when (val validationResult = validateClientStatus()) {
            is ValidationResult.Invalid -> {
                userSettings.clearUserData()
                throw ClientValidationException(validationResult.reason)
            }

            ValidationResult.Valid -> {
                // Proceed with the request
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
        }
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
                println("Original Response is ${response.bodyAsText()}")
                response.body<T>() to response.status
            }
        }, token)
    }

    protected suspend inline fun <reified T> unAuthenticatedPost(
        url: String,
        body: Any,
        crossinline headers: HeadersBuilder.() -> Unit = {}
    ): Pair<T, HttpStatusCode> {
        return KtorClient.httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
            headers {
                headers()
            }
        }.let { response ->
            response.body<T>() to response.status
        }
    }

    protected suspend inline fun <reified T> unAuthenticatedGet(
        url: String,
        token: String? = null,
        crossinline headers: HeadersBuilder.() -> Unit = {}
    ): Pair<T, HttpStatusCode> {
        return KtorClient.httpClient.get(url) {
            headers {
                append("Accept", "application/json")
                append("Accept", "*/*")
                append("Content-Type", "application/json")
                append("Authorization", "Bearer $token")
                headers()
            }
        }.let { response ->
            response.body<T>() to response.status
        }
    }

    protected suspend inline fun <reified T> get(
        url: String,
        token: String? = null,
        crossinline headers: HeadersBuilder.() -> Unit = {}
    ): Pair<T, HttpStatusCode> {
        return handleRequest({ currentToken ->
            KtorClient.httpClient.get(url) {
                headers {
                    append("Accept", "application/json")
                    append("Accept", "*/*")
                    append("Content-Type", "application/json")
                    headers()
                    currentToken?.let {
                        append("Authorization", "Bearer $currentToken")
                    }
                }
            }.let { response ->
                response.body<T>() to response.status
            }
        }, token)
    }


    protected suspend inline fun <reified T> put(
        url: String,
        body: Any,
        token: String? = null,
        crossinline headers: HeadersBuilder.() -> Unit = {}
    ): Pair<T, HttpStatusCode> {
        return handleRequest({ currentToken ->
            KtorClient.httpClient.put(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
                headers {
                    append("Accept", "application/json")
                    append("Accept", "*/*")
                    append("Content-Type", "application/json")
                    headers()
                    currentToken?.let {
                        append("Authorization", "Bearer $currentToken")
                    }
                }
            }.let { response ->
                response.body<T>() to response.status
            }
        }, token)
    }

    protected suspend inline fun <reified T> delete(
        url: String,
        token: String? = null,
        crossinline headers: HeadersBuilder.() -> Unit = {}
    ): Pair<T, HttpStatusCode> {
        return handleRequest({ currentToken ->
            KtorClient.httpClient.delete(url) {
                headers {
                    append("Accept", "application/json")
                    append("Accept", "*/*")
                    append("Content-Type", "application/json")
                    headers()
                    currentToken?.let {
                        append("Authorization", "Bearer $currentToken")
                    }
                }
            }.let { response ->
                response.body<T>() to response.status
            }
        }, token)
    }

    protected suspend inline fun <reified T> patch(
        url: String,
        body: Any,
        token: String? = null,
        crossinline headers: HeadersBuilder.() -> Unit = {}
    ): Pair<T, HttpStatusCode> {
        return handleRequest({ currentToken ->
            KtorClient.httpClient.patch(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
                headers {
                    append("Accept", "application/json")
                    append("Accept", "*/*")
                    append("Content-Type", "application/json")
                    headers()
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

class ClientValidationException(message: String) : Exception(message) 
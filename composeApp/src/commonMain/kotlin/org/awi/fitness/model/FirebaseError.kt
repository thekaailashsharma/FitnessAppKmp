package org.awi.fitness.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FirebaseErrorResponse(
    val error: FirebaseError
)

@Serializable
data class FirebaseError(
    val code: Int,
    val message: String,
    val errors: List<FirebaseErrorDetail>? = null
)

@Serializable
data class FirebaseErrorDetail(
    val message: String,
    val domain: String,
    val reason: String
)

sealed class FirebaseErrorType(val message: String) {
    data object InvalidCredentials : FirebaseErrorType("Invalid email or password")
    data object EmailExists : FirebaseErrorType("Email already exists")
    data object WeakPassword : FirebaseErrorType("Password should be at least 6 characters")
    data object InvalidEmail : FirebaseErrorType("Invalid email format")
    data object EmailNotFound : FirebaseErrorType("Email not found")
    data object TooManyAttempts : FirebaseErrorType("Too many attempts. Please try again later")
    data object NetworkError : FirebaseErrorType("Network error. Please check your connection")
    data class Unknown(val errorMessage: String) : FirebaseErrorType(errorMessage)

    companion object {
        fun fromErrorMessage(message: String): FirebaseErrorType {
            return when {
                message.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) -> InvalidCredentials
                message.contains("EMAIL_EXISTS", ignoreCase = true) -> EmailExists
                message.contains("WEAK_PASSWORD", ignoreCase = true) -> WeakPassword
                message.contains("INVALID_EMAIL", ignoreCase = true) -> InvalidEmail
                message.contains("EMAIL_NOT_FOUND", ignoreCase = true) -> EmailNotFound
                message.contains("TOO_MANY_ATTEMPTS_TRY_LATER", ignoreCase = true) -> TooManyAttempts
                message.contains("NETWORK_ERROR", ignoreCase = true) -> NetworkError
                else -> Unknown(message)
            }
        }
    }
} 
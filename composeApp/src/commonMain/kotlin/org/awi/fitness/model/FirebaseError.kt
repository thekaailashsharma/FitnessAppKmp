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

sealed class FirebaseErrorType(val message: String) {
    data object InvalidCredentials : FirebaseErrorType("The email or password you entered is incorrect. Please try again.")
    data object EmailExists : FirebaseErrorType("An account with this email already exists. Please sign in or use a different email.")
    data object WeakPassword : FirebaseErrorType("Password must be at least 6 characters long and include a mix of letters and numbers.")
    data object InvalidEmail : FirebaseErrorType("Please enter a valid email address (e.g., example@domain.com).")
    data object EmailNotFound : FirebaseErrorType("No account found with this email. Please check the email or create a new account.")
    data object TooManyAttempts : FirebaseErrorType("Too many login attempts. For security, please try again after 30 minutes.")
    data object NetworkError : FirebaseErrorType("Unable to connect to the server. Please check your internet connection and try again.")
    data object UserDisabled : FirebaseErrorType("This account has been disabled. Please contact support for assistance.")
    data object OperationNotAllowed : FirebaseErrorType("This operation is not allowed. Please contact support for assistance.")
    data object ExpiredActionCode : FirebaseErrorType("This action code has expired. Please request a new one.")
    data object InvalidActionCode : FirebaseErrorType("Invalid action code. Please request a new one.")
    data object UserNotFound : FirebaseErrorType("User account not found. Please check your credentials or create a new account.")
    data class Unknown(val errorMessage: String) : FirebaseErrorType("$errorMessage. Please try again or contact support.")

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
                message.contains("USER_DISABLED", ignoreCase = true) -> UserDisabled
                message.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) -> OperationNotAllowed
                message.contains("EXPIRED_ACTION_CODE", ignoreCase = true) -> ExpiredActionCode
                message.contains("INVALID_ACTION_CODE", ignoreCase = true) -> InvalidActionCode
                message.contains("USER_NOT_FOUND", ignoreCase = true) -> UserNotFound
                else -> Unknown(message)
            }
        }
    }
} 
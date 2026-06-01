package org.awi.fitness.viewmodel

import cafe.adriel.voyager.core.model.StateScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.FirebaseErrorType
import org.awi.fitness.repository.AuthRepository
import org.awi.fitness.repository.ClientRepository
import org.awi.fitness.repository.FirebaseException

sealed class AuthState {
    data object Initial : AuthState()
    data object Loading : AuthState()
    data class Error(val message: String, val code: String = "") : AuthState()
    data class Success(val message: String) : AuthState()
    data object PasswordResetSent : AuthState()
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val clientRepository: ClientRepository = ClientRepository()
) : StateScreenModel<AuthState>(AuthState.Initial) {

    private val userSettings = UserSettings.getInstance()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    fun updateEmail(value: String) {
        _email.value = value
        if (mutableState.value is AuthState.Error) mutableState.value = AuthState.Initial
    }

    fun updatePassword(value: String) {
        _password.value = value
        if (mutableState.value is AuthState.Error) mutableState.value = AuthState.Initial
    }

    // Single entry point — tries sign-in first, auto-creates account if email not found
    suspend fun signIn() {
        if (!validateInput()) return
        mutableState.value = AuthState.Loading

        val result = authRepository.signIn(email.value, password.value)
        result.fold(
            onSuccess = { mutableState.value = AuthState.Success("Authenticated") },
            onFailure = { error ->
                val errorType = (error as? FirebaseException)?.errorType
                // Wrong password — show error directly, don't try to sign up
                if (errorType is FirebaseErrorType.InvalidCredentials) {
                    mutableState.value = AuthState.Error(
                        "Incorrect password. Tap 'Forgot password?' to reset it.",
                        "INCORRECT_PASSWORD"
                    )
                } else {
                    handleAuthError(error)
                }
            }
        )
    }

    // Explicit sign-up path
    suspend fun signUp() {
        if (!validateInput()) return
        mutableState.value = AuthState.Loading

        val result = authRepository.signUp(email.value, password.value)
        result.fold(
            onSuccess = { mutableState.value = AuthState.Success("Account created") },
            onFailure = { handleAuthError(it) }
        )
    }

    suspend fun sendPasswordReset(resetEmail: String): Boolean {
        if (resetEmail.isBlank() || !resetEmail.contains("@")) return false
        val result = authRepository.sendPasswordReset(resetEmail)
        if (result.isSuccess) mutableState.value = AuthState.PasswordResetSent
        return result.isSuccess
    }

    fun logout() {
        authRepository.logout()
        mutableState.value = AuthState.Initial
        _email.value = ""
        _password.value = ""
    }

    fun checkAuthState(): Boolean = authRepository.isLoggedIn()

    fun resetToInitial() {
        mutableState.value = AuthState.Initial
    }

    suspend fun deleteAccount(): Result<Unit> {
        mutableState.value = AuthState.Loading
        return try {
            val result = authRepository.deleteAccount()
            result.fold(
                onSuccess = {
                    mutableState.value = AuthState.Initial
                    Result.success(Unit)
                },
                onFailure = { error ->
                    mutableState.value = AuthState.Error(error.message ?: "Failed to delete account")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            mutableState.value = AuthState.Error(e.message ?: "Failed to delete account")
            Result.failure(e)
        }
    }

    private fun handleAuthError(error: Throwable) {
        val message = when (error) {
            is FirebaseException -> error.errorType.message
            else -> error.message ?: "Something went wrong. Please try again."
        }
        mutableState.value = AuthState.Error(message)
    }

    private fun validateInput(): Boolean {
        if (email.value.isBlank() || password.value.isBlank()) {
            mutableState.value = AuthState.Error("Please enter your email and password.")
            return false
        }
        if (!email.value.contains("@")) {
            mutableState.value = AuthState.Error("Please enter a valid email address.")
            return false
        }
        if (password.value.length < 6) {
            mutableState.value = AuthState.Error("Password must be at least 6 characters.")
            return false
        }
        return true
    }
}

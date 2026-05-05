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
    data object ClientNotFound : AuthState()
    data object AccessRequestSent : AuthState()
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
        if (mutableState.value is AuthState.Error) {
            mutableState.value = AuthState.Initial
        }
    }

    fun updatePassword(value: String) {
        _password.value = value
        if (mutableState.value is AuthState.Error) {
            mutableState.value = AuthState.Initial
        }
    }

    suspend fun authenticate() {
        if (!validateInput()) return

        mutableState.value = AuthState.Loading

        val signInResult = authRepository.signIn(email.value, password.value)

        signInResult.fold(
            onSuccess = {
                verifyClientExists()
            },
            onFailure = { signInError ->
                val errorType = (signInError as? FirebaseException)?.errorType

                if (errorType is FirebaseErrorType.EmailNotFound ||
                    errorType is FirebaseErrorType.UserNotFound) {
                    val signUpResult = authRepository.signUp(email.value, password.value)
                    signUpResult.fold(
                        onSuccess = { verifyClientExists() },
                        onFailure = { handleAuthError(it) }
                    )
                } else if (errorType is FirebaseErrorType.InvalidCredentials) {
                    val signUpResult = authRepository.signUp(email.value, password.value)
                    signUpResult.fold(
                        onSuccess = { verifyClientExists() },
                        onFailure = { signUpError ->
                            val signUpErrorType = (signUpError as? FirebaseException)?.errorType
                            if (signUpErrorType is FirebaseErrorType.EmailExists) {
                                mutableState.value = AuthState.Error("INCORRECT_PASSWORD", "INCORRECT_PASSWORD")
                            } else {
                                handleAuthError(signUpError)
                            }
                        }
                    )
                } else {
                    handleAuthError(signInError)
                }
            }
        )
    }

    suspend fun sendPasswordReset(resetEmail: String): Boolean {
        if (resetEmail.isBlank() || !resetEmail.contains("@")) return false
        return authRepository.sendPasswordReset(resetEmail).isSuccess
    }

    suspend fun requestAccess() {
        mutableState.value = AuthState.Loading
        val result = authRepository.submitAccessRequest(email.value)
        result.fold(
            onSuccess = {
                authRepository.logout()
                mutableState.value = AuthState.AccessRequestSent
            },
            onFailure = {
                mutableState.value = AuthState.Error(
                    it.message ?: "Failed to send request"
                )
            }
        )
    }

    fun resetToInitial() {
        mutableState.value = AuthState.Initial
    }

    @Deprecated("Use authenticate() instead", ReplaceWith("authenticate()"))
    suspend fun signUp() {
        authenticate()
    }

    @Deprecated("Use authenticate() instead", ReplaceWith("authenticate()"))
    suspend fun signIn() {
        authenticate()
    }

    private fun handleAuthError(error: Throwable) {
        val message = when (error) {
            is FirebaseException -> error.errorType.message
            else -> error.message ?: "Unknown error occurred"
        }
        mutableState.value = AuthState.Error(message)
    }

    private suspend fun verifyClientExists() {
        val result = authRepository.getClientByEmail(email.value)

        result.fold(
            onSuccess = { client ->
                if (client != null) {
                    mutableState.value = AuthState.Success("Successfully authenticated!")
                } else {
                    mutableState.value = AuthState.ClientNotFound
                }
            },
            onFailure = {
                authRepository.logout()
                mutableState.value = AuthState.Error(
                    it.message ?: "Failed to verify account",
                    "VERIFICATION_FAILED"
                )
            }
        )
    }

    private fun validateInput(): Boolean {
        if (email.value.isBlank() || password.value.isBlank()) {
            mutableState.value = AuthState.Error("Email and password cannot be empty")
            return false
        }
        if (!email.value.contains("@")) {
            mutableState.value = AuthState.Error("Please enter a valid email")
            return false
        }
        if (password.value.length < 6) {
            mutableState.value = AuthState.Error("Password must be at least 6 characters")
            return false
        }
        return true
    }

    fun logout() {
        authRepository.logout()
        mutableState.value = AuthState.Initial
        _email.value = ""
        _password.value = ""
    }

    fun checkAuthState(): Boolean {
        return authRepository.isLoggedIn()
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
                    mutableState.value = AuthState.Error(
                        error.message ?: "Failed to delete account"
                    )
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            mutableState.value = AuthState.Error(
                e.message ?: "Failed to delete account"
            )
            Result.failure(e)
        }
    }
}

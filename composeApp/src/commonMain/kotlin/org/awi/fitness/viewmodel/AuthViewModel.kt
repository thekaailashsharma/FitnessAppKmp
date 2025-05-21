package org.awi.fitness.viewmodel

import cafe.adriel.voyager.core.model.StateScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.awi.fitness.data.UserSettings
import org.awi.fitness.repository.AuthRepository

sealed class AuthState {
    data object Initial : AuthState()
    data object Loading : AuthState()
    data class Error(val message: String) : AuthState()
    data class Success(val message: String) : AuthState()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : StateScreenModel<AuthState>(AuthState.Initial) {

    private val userSettings = UserSettings.getInstance()
    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    fun updateEmail(value: String) {
        _email.value = value
        // Reset error state when user starts typing
        if (mutableState.value is AuthState.Error) {
            mutableState.value = AuthState.Initial
        }
    }

    fun updatePassword(value: String) {
        _password.value = value
        // Reset error state when user starts typing
        if (mutableState.value is AuthState.Error) {
            mutableState.value = AuthState.Initial
        }
    }

    suspend fun signUp() {
        if (!validateInput()) return
        
        mutableState.value = AuthState.Loading
        
        val result = authRepository.signUp(email.value, password.value)
        
        mutableState.value = result.fold(
            onSuccess = { AuthState.Success("Successfully signed up!") },
            onFailure = { AuthState.Error(it.message ?: "Unknown error occurred") }
        )
    }

    suspend fun signIn() {
        if (!validateInput()) return
        
        mutableState.value = AuthState.Loading
        
        val result = authRepository.signIn(email.value, password.value)
        
        mutableState.value = result.fold(
            onSuccess = { AuthState.Success("Successfully signed in!") },
            onFailure = { AuthState.Error(it.message ?: "Unknown error occurred") }
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
} 
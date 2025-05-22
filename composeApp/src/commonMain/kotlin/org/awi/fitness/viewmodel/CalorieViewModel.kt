package org.awi.fitness.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.awi.fitness.data.*
import org.awi.fitness.repository.GeminiRepository
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

class CalorieViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalorieUiState())
    val uiState: StateFlow<CalorieUiState> = _uiState.asStateFlow()
    
    private val userSettings = UserSettings.getInstance()
    private val geminiRepository = GeminiRepository()

    init {
        // Load saved values from UserSettings if they exist
        val savedCalories = userSettings.calculatedCalories
        val savedBmr = userSettings.bmr
        val savedTdee = userSettings.tdee
        
        if (savedCalories > 0) {
            _uiState.value = _uiState.value.copy(
                calculatedCalories = savedCalories,
                bmr = savedBmr,
                tdee = savedTdee,
                isCalculated = true,
                showRecalculate = true
            )
        }
    }

    fun updateWeight(weight: String) {
        _uiState.value = _uiState.value.copy(weight = weight)
    }

    fun updateHeight(height: String) {
        _uiState.value = _uiState.value.copy(height = height)
    }

    fun updateAge(age: String) {
        _uiState.value = _uiState.value.copy(age = age)
    }

    fun updateGender(gender: Gender) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun updateActivityLevel(level: ActivityLevel) {
        _uiState.value = _uiState.value.copy(activityLevel = level)
    }

    fun updateGoal(goal: Goal) {
        _uiState.value = _uiState.value.copy(goal = goal)
    }

    fun calculateCalories() {
        val state = _uiState.value
        
        viewModelScope.launch {
            try {
                val weight = state.weight.toFloatOrNull() ?: return@launch
                val height = state.height.toFloatOrNull() ?: return@launch
                val age = state.age.toIntOrNull() ?: return@launch

                _uiState.value = state.copy(isLoading = true, error = null)

                val result = geminiRepository.calculateCalories(
                    weight = weight,
                    height = height,
                    age = age,
                    gender = state.gender,
                    activityLevel = state.activityLevel,
                    goal = state.goal
                )

                result.onSuccess { response ->
                    // Store in UserSettings
                    userSettings.bmr = response.bmr
                    userSettings.tdee = response.tdee
                    userSettings.calculatedCalories = response.targetCalories.roundToInt()

                    _uiState.value = state.copy(
                        calculatedCalories = response.targetCalories.roundToInt(),
                        bmr = response.bmr,
                        tdee = response.tdee,
                        isCalculated = true,
                        showRecalculate = true,
                        isLoading = false,
                        error = null
                    )
                }.onFailure { error ->
                    _uiState.value = state.copy(
                        error = "Error calculating calories: ${error.message}",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    error = "Error calculating calories: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun resetCalculation() {
        _uiState.value = _uiState.value.copy(
            isCalculated = false,
            showRecalculate = false,
            isLoading = false,
            error = null
        )
    }
} 
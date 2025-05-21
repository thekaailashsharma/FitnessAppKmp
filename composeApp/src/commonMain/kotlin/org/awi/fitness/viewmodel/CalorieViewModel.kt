package org.awi.fitness.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.awi.fitness.data.*
import kotlin.math.roundToInt

class CalorieViewModel {
    private val _uiState = MutableStateFlow(CalorieUiState())
    val uiState: StateFlow<CalorieUiState> = _uiState.asStateFlow()

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
        
        try {
            val weight = state.weight.toFloatOrNull() ?: return
            val height = state.height.toFloatOrNull() ?: return
            val age = state.age.toIntOrNull() ?: return

            // Calculate BMR using Mifflin-St Jeor Equation
            val bmr = when (state.gender) {
                Gender.MALE -> (10 * weight) + (6.25 * height) - (5 * age) + 5
                Gender.FEMALE -> (10 * weight) + (6.25 * height) - (5 * age) - 161
                Gender.OTHER -> ((10 * weight) + (6.25 * height) - (5 * age) - 78) // Average
            }

            // Calculate TDEE
            val tdee = bmr * state.activityLevel.factor

            // Adjust calories based on goal
            val finalCalories = (tdee + state.goal.calorieAdjustment).roundToInt()

            _uiState.value = state.copy(
                calculatedCalories = finalCalories,
                bmr = bmr.toFloat(),
                tdee = tdee.toFloat(),
                isCalculated = true,
                showRecalculate = true,
                error = null
            )
        } catch (e: Exception) {
            _uiState.value = state.copy(error = "Invalid input values")
        }
    }

    fun resetCalculation() {
        _uiState.value = _uiState.value.copy(
            isCalculated = false,
            showRecalculate = false
        )
    }
} 
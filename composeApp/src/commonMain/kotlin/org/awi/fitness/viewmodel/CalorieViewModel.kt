package org.awi.fitness.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.awi.fitness.data.*
import kotlin.math.roundToInt

class CalorieViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalorieUiState())
    val uiState: StateFlow<CalorieUiState> = _uiState.asStateFlow()

    private val userSettings = UserSettings.getInstance()

    init {
        // Prefill inputs from the stats captured during onboarding (or the latest
        //    weigh-in) so the calculator opens ready-to-go instead of blank.
        val latestWeight = userSettings.weighIns.value.maxByOrNull { it.date }?.weight
            ?: userSettings.profileWeightKg.takeIf { it > 0f }
        val prefHeight = userSettings.profileHeightCm.takeIf { it > 0 }
        val prefAge = userSettings.profileAge.takeIf { it > 0 }
        val prefGender = when (userSettings.profileGender.uppercase()) {
            "FEMALE" -> Gender.FEMALE
            "MALE" -> Gender.MALE
            else -> _uiState.value.gender
        }
        _uiState.value = _uiState.value.copy(
            weight = latestWeight?.let { if (it % 1f == 0f) it.toInt().toString() else it.toString() }
                ?: _uiState.value.weight,
            height = prefHeight?.toString() ?: _uiState.value.height,
            age = prefAge?.toString() ?: _uiState.value.age,
            gender = prefGender
        )

        // Load saved calculation results from UserSettings if they exist
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

        val weight = state.weight.toFloatOrNull() ?: return
        val height = state.height.toFloatOrNull() ?: return
        val age = state.age.toIntOrNull() ?: return

        // Local Mifflin-St Jeor computation — pure arithmetic, offline-safe, instant.
        // BMR = 10*kg + 6.25*cm - 5*age + (male ? +5 : -161)
        val genderConstant = if (state.gender == Gender.MALE) 5.0 else -161.0
        val bmr = (10.0 * weight + 6.25 * height - 5.0 * age + genderConstant).toFloat()
        // TDEE = BMR * activity factor
        val tdee = (bmr * state.activityLevel.factor).toFloat()
        // Target = TDEE + goal delta (-500 cut / 0 maintain / +500 gain)
        val targetCalories = (tdee + state.goal.calorieAdjustment).roundToInt()

        // Persist the SAME number that is displayed (single source of truth).
        userSettings.bmr = bmr
        userSettings.tdee = tdee
        userSettings.calculatedCalories = targetCalories

        _uiState.value = state.copy(
            calculatedCalories = targetCalories,
            bmr = bmr,
            tdee = tdee,
            isCalculated = true,
            showRecalculate = true,
            isLoading = false,
            error = null
        )
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
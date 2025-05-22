package org.awi.fitness.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.awi.fitness.model.Program
import org.awi.fitness.repository.MealRepository

data class MealUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val meals: List<Program> = emptyList()
)

class MealViewModel {
    private val mealRepository = MealRepository()
    
    private val _state = MutableStateFlow(MealUiState())
    val state: StateFlow<MealUiState> = _state.asStateFlow()

    suspend fun loadMealsByEmail(email: String) {
        try {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val mealsResult = mealRepository.getMealsByEmail(email)
            mealsResult.fold(
                onSuccess = { meals ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            meals = meals
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to load meals: ${error.message}"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            _state.update { 
                it.copy(
                    isLoading = false,
                    error = "Failed to load meals: ${e.message}"
                )
            }
        }
    }
} 
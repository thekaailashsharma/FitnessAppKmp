package org.awi.fitness.viewmodel
import org.awi.fitness.data.tr
import org.awi.fitness.data.StringKey

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.*
import org.awi.fitness.repository.GeminiRepository
import org.awi.fitness.repository.WorkoutRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class WorkoutUIState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val workoutPlans: List<WorkoutPlanWithExercises> = emptyList(),
    val selectedPlanId: String? = null
)

class WorkoutViewModel {
    private val workoutRepository = WorkoutRepository()
    private val geminiRepository = GeminiRepository()
    private val userSettings = UserSettings.getInstance()

    private val _state = MutableStateFlow(WorkoutUIState())
    val state: StateFlow<WorkoutUIState> = _state.asStateFlow()

    suspend fun saveUserGoals(
        goal: FitnessGoal,
        level: FitnessLevel,
        workoutDays: Int,
        specificRequirements: String
    ) {
        try {
            _state.update { it.copy(isLoading = true, error = null) }

            // Generate workout plan using Gemini
            val generatedPlan = geminiRepository.generateWorkoutPlan(
                goal = goal.name,
                fitnessLevel = level.name,
                workoutDays = workoutDays,
                specificRequirements = specificRequirements,
                language = userSettings.language.value
            ).getOrThrow()

            // Tag the plan with the current user so it is scoped to them (and so it
            // survives the owner filter in getAllWorkoutPlans).
            val ownerId = userSettings.userId?.takeIf { it.isNotBlank() }
                ?: userSettings.userEmail?.takeIf { it.isNotBlank() }
                ?: ""

            val workoutPlan = WorkoutPlan(
                name = generatedPlan.workoutName,
                description = generatedPlan.description,
                difficulty = normalizeWorkoutDifficulty(generatedPlan.difficulty),
                duration = generatedPlan.estimatedDuration,
                category = normalizeWorkoutCategory(generatedPlan.category),
                ownerId = ownerId
            )

            val planId = workoutRepository.insertWorkoutPlan(workoutPlan).getOrThrow()

            val exercisesPerDay = if (workoutDays > 0) generatedPlan.exercises.size / workoutDays else generatedPlan.exercises.size
            generatedPlan.exercises.forEachIndexed { index, exercise ->
                val day = if (exercisesPerDay > 0) (index / exercisesPerDay) + 1 else 1
                val orderInDay = if (exercisesPerDay > 0) (index % exercisesPerDay) + 1 else index + 1
                val newExercise = Exercise(
                    planId = planId,
                    name = exercise.name,
                    description = exercise.description,
                    sets = exercise.sets,
                    reps = exercise.reps,
                    restTime = exercise.restTime,
                    isCompleted = false,
                    dayOfWeek = day.coerceIn(1, workoutDays),
                    orderInDay = orderInDay
                )
                workoutRepository.insertExercise(newExercise).getOrThrow()
            }

            _state.update { it.copy(isLoading = false, selectedPlanId = planId) }
        } catch (e: Exception) {
            _state.update { 
                it.copy(
                    isLoading = false,
                    error = "${tr(StringKey.VME_CREATE_WORKOUT_PLAN_FAILED)}: ${e.message}"
                )
            }
        }
    }

    private fun normalizeWorkoutDifficulty(difficulty: String): WorkoutDifficulty {
        return when (difficulty.trim().uppercase()) {
            "BEGINNER", "EASY", "BASIC", "NOVICE" -> WorkoutDifficulty.BEGINNER
            "INTERMEDIATE", "MEDIUM", "MODERATE" -> WorkoutDifficulty.INTERMEDIATE
            "ADVANCED", "HARD", "EXPERT", "DIFFICULT" -> WorkoutDifficulty.ADVANCED
            else -> WorkoutDifficulty.BEGINNER // Default to beginner if unknown
        }
    }

    private fun normalizeWorkoutCategory(category: String): WorkoutCategory {
        return when (category.trim().uppercase()) {
            "STRENGTH", "WEIGHT", "RESISTANCE", "WEIGHTS", "MUSCLE" -> WorkoutCategory.STRENGTH
            "CARDIO", "AEROBIC", "ENDURANCE", "RUNNING" -> WorkoutCategory.CARDIO
            "HIIT", "HIGH INTENSITY", "INTERVAL", "INTENSE" -> WorkoutCategory.HIIT
            "FLEXIBILITY", "STRETCHING", "MOBILITY" -> WorkoutCategory.FLEXIBILITY
            "YOGA", "MIND-BODY", "BALANCE" -> WorkoutCategory.YOGA
            else -> when {
                category.contains("STRENGTH") || category.contains("WEIGHT") -> WorkoutCategory.STRENGTH
                category.contains("CARDIO") || category.contains("ENDURANCE") -> WorkoutCategory.CARDIO
                category.contains("HIIT") || category.contains("INTENSE") -> WorkoutCategory.HIIT
                category.contains("FLEX") || category.contains("STRETCH") -> WorkoutCategory.FLEXIBILITY
                category.contains("YOGA") || category.contains("BALANCE") -> WorkoutCategory.YOGA
                else -> WorkoutCategory.STRENGTH // Default to strength if unknown
            }
        }
    }

    fun resetPlanSelection() {
        _state.update { it.copy(selectedPlanId = null, error = null) }
    }

    suspend fun loadIfNeeded() {
        if (_state.value.workoutPlans.isNotEmpty() || _state.value.isLoading) return
        loadWorkoutPlans()
    }

    suspend fun loadWorkoutPlans() {
        try {
            _state.update { it.copy(isLoading = true, error = null) }

            val plansResult = workoutRepository.getAllWorkoutPlans()
            plansResult.fold(
                onSuccess = { plans ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            workoutPlans = plans
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "${tr(StringKey.VME_LOAD_WORKOUT_PLANS_FAILED)}: ${error.message}"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = "${tr(StringKey.VME_LOAD_WORKOUT_PLANS_FAILED)}: ${e.message}"
                )
            }
        }
    }

    suspend fun setExerciseCompleted(exercise: Exercise, completed: Boolean) {
        try {
            val updatedExercise = exercise.copy(
                isCompleted = completed,
            )
            workoutRepository.setExerciseCompleted(updatedExercise, completed).getOrThrow()
            loadWorkoutPlans() // Reload to update UI
        } catch (e: Exception) {
            _state.update { 
                it.copy(error = "${tr(StringKey.VME_UPDATE_EXERCISE_FAILED)}: ${e.message}")
            }
        }
    }

    suspend fun deleteWorkoutPlan(planId: String): Result<Unit> {
        return try {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = workoutRepository.deleteWorkoutPlan(planId)
            result.fold(
                onSuccess = {
                    loadWorkoutPlans() // Reload to update UI
                    Result.success(Unit)
                },
                onFailure = { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "${tr(StringKey.VME_DELETE_WORKOUT_PLAN_FAILED)}: ${error.message}"
                        )
                    }
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            _state.update { 
                it.copy(
                    isLoading = false,
                    error = "${tr(StringKey.VME_DELETE_WORKOUT_PLAN_FAILED)}: ${e.message}"
                )
            }
            Result.failure(e)
        }
    }

    /**
     * Completes several exercises robustly: the network writes are fired in parallel (no
     * serial blocking loop / artificial delays) and the plans are reloaded once at the end.
     * Any failure is surfaced via the error state and returned to the caller.
     */
    suspend fun completeExercises(exercises: List<Exercise>): Result<Unit> {
        if (exercises.isEmpty()) return Result.success(Unit)
        return try {
            kotlinx.coroutines.coroutineScope {
                exercises.map { ex ->
                    async { workoutRepository.setExerciseCompleted(ex, true).getOrThrow() }
                }.awaitAll()
            }
            loadWorkoutPlans()
            Result.success(Unit)
        } catch (e: Exception) {
            _state.update { it.copy(error = "${tr(StringKey.VME_FINISH_WORKOUT_FAILED)}: ${e.message}") }
            Result.failure(e)
        }
    }
}
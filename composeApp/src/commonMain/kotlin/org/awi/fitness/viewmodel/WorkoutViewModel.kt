package org.awi.fitness.viewmodel

import cafe.adriel.voyager.core.model.StateScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
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

data class ProgressUiState(
    val weeklyProgress: List<Pair<Long, Float>> = emptyList(),
    val completionRate: Int = 0,
    val strengthGain: Int = 0,
    val totalWorkoutHours: Int = 0,
    val warnings: List<String> = emptyList(),
    val recentActivities: List<WorkoutActivity> = emptyList(),
    val exercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = true
)

class WorkoutViewModel {
    private val workoutRepository = WorkoutRepository()
    private val geminiRepository = GeminiRepository()
    
    private val _state = MutableStateFlow(WorkoutUIState())
    val state: StateFlow<WorkoutUIState> = _state.asStateFlow()

    private val _progressUiState = MutableStateFlow(ProgressUiState())
    val progressUiState: StateFlow<ProgressUiState> = _progressUiState.asStateFlow()

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
                specificRequirements = specificRequirements
            ).getOrThrow()

            // Create workout plan
            val workoutPlan = WorkoutPlan(
                name = generatedPlan.workoutName,
                description = generatedPlan.description,
                difficulty = normalizeWorkoutDifficulty(generatedPlan.difficulty),
                duration = generatedPlan.estimatedDuration,
                category = normalizeWorkoutCategory(generatedPlan.category)
            )

            val planId = workoutRepository.insertWorkoutPlan(workoutPlan).getOrThrow()

            // Create exercises
            generatedPlan.exercises.forEachIndexed { index, exercise ->
                val newExercise = Exercise(
                    planId = planId,
                    name = exercise.name,
                    description = exercise.description,
                    sets = exercise.sets,
                    reps = exercise.reps,
                    restTime = exercise.restTime,
                    isCompleted = false,
                    dayOfWeek = (index % workoutDays) + 1,
                    orderInDay = (index / workoutDays) + 1
                )
                workoutRepository.insertExercise(newExercise).getOrThrow()
            }

            _state.update { it.copy(isLoading = false, selectedPlanId = planId) }
        } catch (e: Exception) {
            _state.update { 
                it.copy(
                    isLoading = false,
                    error = "Failed to create workout plan: ${e.message}"
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

    suspend fun loadWorkoutPlans() {
        try {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val plansResult = workoutRepository.getAllWorkoutPlans()
            plansResult.fold(
                onSuccess = { plans ->
                    println("x ${plans.size} workout plans")
                    plans.forEach { plan ->
                        println("Plan: ${plan.plan.name} with ${plan.exercises.size} exercises")
                    }
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            workoutPlans = plans.filter { plan -> plan.exercises.isNotEmpty() }
                        )
                    }
                },
                onFailure = { error ->
                    println("Failed to load workout plans: ${error.message}")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to load workout plans: ${error.message}"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            println("Error loading workout plans: ${e.message}")
            e.printStackTrace()
            _state.update { 
                it.copy(
                    isLoading = false,
                    error = "Failed to load workout plans: ${e.message}"
                )
            }
        }
    }

    suspend fun setExerciseCompleted(exercise: Exercise, completed: Boolean) {
        try {
            workoutRepository.setExerciseCompleted(exercise, completed).getOrThrow()
            loadWorkoutPlans() // Reload to update UI
        } catch (e: Exception) {
            _state.update { 
                it.copy(error = "Failed to update exercise: ${e.message}")
            }
        }
    }

    suspend fun loadProgress(planId: String) {
        // Load recent activities
        workoutRepository.getRecentActivities(planId).collect { activities ->
            _progressUiState.value = _progressUiState.value.copy(
                recentActivities = activities
            )
        }

        // Load completion rate
        val currentTime = Clock.System.now().toEpochMilliseconds()
        workoutRepository.getCompletionCountForDay(planId, currentTime).collect { completedToday ->
            val warnings = mutableListOf<String>()
            if (completedToday == 0) {
                warnings.add("You haven't completed any exercises today")
            }
            
            _progressUiState.value = _progressUiState.value.copy(
                warnings = warnings,
                completionRate = calculateCompletionRate(completedToday)
            )
        }

        // Generate sample progress data for the graph
        val weeklyProgress = (0..6).map { daysAgo ->
            val timestamp = Clock.System.now().toEpochMilliseconds() - (daysAgo * 24 * 60 * 60 * 1000L)
            timestamp to (0.3f + kotlin.random.Random.nextFloat() * 0.5f)
        }.reversed()

        _progressUiState.value = _progressUiState.value.copy(
            weeklyProgress = weeklyProgress,
            strengthGain = 15,
            totalWorkoutHours = 24
        )
    }

    private fun calculateCompletionRate(completedToday: Int): Int {
        return (completedToday * 100) / 5 // Assuming 5 exercises per day
    }
} 
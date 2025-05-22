package org.awi.fitness.viewmodel

import cafe.adriel.voyager.core.model.StateScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import org.awi.fitness.model.*
import org.awi.fitness.repository.WorkoutRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class WorkoutUiState(
    val workoutPlans: List<WorkoutPlanWithExercises> = emptyList(),
    val selectedPlanId: String? = null,
    val selectedDayOfWeek: Int = 1,
    val isLoading: Boolean = true,
    val error: String? = null
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

class WorkoutViewModel(
    private val repository: WorkoutRepository = WorkoutRepository()
) : StateScreenModel<WorkoutUiState>(WorkoutUiState()) {

    private val _progressUiState = MutableStateFlow(ProgressUiState())
    val progressUiState: StateFlow<ProgressUiState> = _progressUiState.asStateFlow()


    suspend fun loadWorkoutPlans() {
        try {
            println("Loading workout plans in ViewModel...")
            mutableState.value = mutableState.value.copy(isLoading = true, error = null)
            repository.getAllWorkoutPlans()
                .collect { plans ->
                    println("Received ${plans.size} workout plans")
                    mutableState.value = mutableState.value.copy(
                        workoutPlans = plans,
                        isLoading = false,
                        error = null
                    )
                }
        } catch (e: Exception) {
            println("Error loading workout plans: ${e.message}")
            mutableState.value = mutableState.value.copy(
                isLoading = false,
                error = "Failed to load workout plans: ${e.message}"
            )
        }
    }

    suspend fun setExerciseCompleted(exerciseId: String, completed: Boolean) {
        repository.setExerciseCompleted(exerciseId, completed)
    }

    fun updateSelectedDay(dayOfWeek: Int) {
        mutableState.value = mutableState.value.copy(selectedDayOfWeek = dayOfWeek)
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun saveUserGoals(
        goal: FitnessGoal,
        level: FitnessLevel,
        workoutDays: Int
    ) {
        mutableState.value = mutableState.value.copy(isLoading = true)
        
        try {
            // Create workout plan based on goals
            val workoutPlan = WorkoutPlan(
                id = Uuid.random().toString(),
                name = "${goal.name.lowercase().replaceFirstChar { it.uppercase() }} Program",
                description = "Personalized ${level.name.lowercase()} level program for ${goal.name.lowercase()}",
                difficulty = when(level) {
                    FitnessLevel.BEGINNER -> WorkoutDifficulty.BEGINNER
                    FitnessLevel.INTERMEDIATE -> WorkoutDifficulty.INTERMEDIATE
                    FitnessLevel.ADVANCED -> WorkoutDifficulty.ADVANCED
                },
                duration = 12, // 12-week program
                category = when(goal) {
                    FitnessGoal.WEIGHT_LOSS -> WorkoutCategory.HIIT
                    FitnessGoal.MUSCLE_GAIN -> WorkoutCategory.STRENGTH
                    FitnessGoal.ENDURANCE -> WorkoutCategory.CARDIO
                    FitnessGoal.FLEXIBILITY -> WorkoutCategory.FLEXIBILITY
                }
            )

            val planIdResult = repository.insertWorkoutPlan(workoutPlan)
            
            planIdResult.fold(
                onSuccess = { planId ->
                    // Generate exercises for each day
                    generateExercisesForPlan(planId, goal, level, workoutDays)
                    
                    mutableState.value = mutableState.value.copy(
                        selectedPlanId = planId,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    mutableState.value = mutableState.value.copy(
                        error = "Failed to create workout plan: ${error.message}",
                        isLoading = false
                    )
                }
            )
        } catch (e: Exception) {
            mutableState.value = mutableState.value.copy(
                error = "Failed to create workout plan: ${e.message}",
                isLoading = false
            )
        }
    }

    private suspend fun generateExercisesForPlan(
        planId: String,
        goal: FitnessGoal,
        level: FitnessLevel,
        workoutDays: Int
    ) {
        val exercises = getExercisesForGoal(goal, level)
        val daysPerWeek = workoutDays
        
        // Distribute exercises across workout days
        exercises.chunked(exercises.size / daysPerWeek).forEachIndexed { dayIndex, dayExercises ->
            dayExercises.forEachIndexed { orderIndex, exercise ->
                repository.insertExercise(
                    Exercise(
                        planId = planId,
                        name = exercise.first,
                        description = exercise.second,
                        sets = when(level) {
                            FitnessLevel.BEGINNER -> 3
                            FitnessLevel.INTERMEDIATE -> 4
                            FitnessLevel.ADVANCED -> 5
                        },
                        reps = 12,
                        restTime = when(level) {
                            FitnessLevel.BEGINNER -> 90
                            FitnessLevel.INTERMEDIATE -> 60
                            FitnessLevel.ADVANCED -> 45
                        },
                        dayOfWeek = dayIndex + 1,
                        orderInDay = orderIndex
                    )
                )
            }
        }
    }

    private fun getExercisesForGoal(goal: FitnessGoal, level: FitnessLevel): List<Pair<String, String>> {
        return when (goal) {
            FitnessGoal.WEIGHT_LOSS -> listOf(
                "Burpees" to "Full body exercise that combines a squat, push-up, and jump",
                "Mountain Climbers" to "Dynamic plank exercise that targets core and cardio",
                "Jump Rope" to "High-intensity cardio exercise for fat burning",
                "Squat Jumps" to "Explosive lower body exercise that elevates heart rate"
            )
            FitnessGoal.MUSCLE_GAIN -> listOf(
                "Bench Press" to "Compound exercise for chest, shoulders, and triceps",
                "Deadlift" to "Full body compound exercise focusing on posterior chain",
                "Squats" to "Lower body compound exercise for strength and muscle",
                "Pull-ups" to "Upper body compound exercise for back and biceps"
            )
            FitnessGoal.ENDURANCE -> listOf(
                "Running" to "Outdoor or treadmill running for cardiovascular endurance",
                "Cycling" to "Stationary or outdoor cycling for leg strength and endurance",
                "Swimming" to "Full body exercise that improves cardiovascular fitness",
                "Rowing" to "Indoor rowing machine for upper and lower body endurance"
            )
            FitnessGoal.FLEXIBILITY -> listOf(
                "Yoga" to "Mind-body practice that improves flexibility and relaxation",
                "Pilates" to "Core-focused exercise for strength, flexibility, and posture",
                "Stretching" to "Static and dynamic stretches for muscle flexibility",
                "Foam Rolling" to "Self-myofascial release technique for muscle recovery"
            )
        }
    }

    suspend fun loadProgress(planId: String) {
        // Load recent activities
        repository.getRecentActivities(planId).collect { activities ->
            _progressUiState.value = _progressUiState.value.copy(
                recentActivities = activities
            )
        }

        // Load completion rate
        val currentTime = Clock.System.now().toEpochMilliseconds()
        repository.getCompletionCountForDay(planId, currentTime).collect { completedToday ->
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
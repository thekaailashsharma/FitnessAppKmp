package org.awi.fitness.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.Client
import org.awi.fitness.model.WorkoutPlanWithExercises
import org.awi.fitness.repository.ClientRepository
import org.awi.fitness.repository.WorkoutRepository
import org.awi.fitness.utils.homeFitnessTips
import kotlinx.datetime.*
import kotlin.random.Random

data class HomeScreenState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val userProfile: Client? = null,
    val workoutPlans: List<WorkoutPlanWithExercises> = emptyList(),
    val totalWorkouts: Int = 0,
    val completedWorkouts: Int = 0,
    val caloriesGoal: Int = 0,
    val bmr: Float = 0f,
    val tdee: Float = 0f,
    val scheduledWorkoutsToday: Int = 0,
    val totalScheduledWorkouts: Int = 0,
    val upcomingWorkout: WorkoutSchedulePreview? = null,
    val dailyTips: List<DailyTip> = emptyList()
)

data class WorkoutSchedulePreview(
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val color: Long
)

data class DailyTip(
    val tip: String,
    val icon: TipIcon,
    val color: Long
)

enum class TipIcon {
    WATER, FOOD, SLEEP, EXERCISE, MINDFULNESS, HEALTH;
    
    companion object {
        fun random(): TipIcon = values()[Random.nextInt(values().size)]
    }
}

class HomeViewModel {
    private val userSettings = UserSettings.getInstance()
    private val clientRepository = ClientRepository()
    private val workoutRepository = WorkoutRepository()

    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        _state.value = _state.value.copy(isLoading = true)

        kotlinx.coroutines.MainScope().launch {
            try {
                // Load user profile
                val userEmail = userSettings.userEmail
                if (userEmail != null) {
                    val profileResult = clientRepository.getClientByEmail(userEmail)
                    profileResult.onSuccess { client ->
                        _state.value = _state.value.copy(userProfile = client)
                    }
                }

                // Load workout plans
                val workoutPlansResult = workoutRepository.getAllWorkoutPlans()
                workoutPlansResult.onSuccess { plans ->
                    val completedWorkouts = plans.sumOf { plan ->
                        plan.exercises.count { it.isCompleted }
                    }
                    val totalWorkouts = plans.sumOf { it.exercises.size }

                    _state.value = _state.value.copy(
                        workoutPlans = plans,
                        completedWorkouts = completedWorkouts,
                        totalWorkouts = totalWorkouts
                    )
                }

                // Load workout schedules
                val schedules = userSettings.workoutSchedules.value
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val scheduledToday = schedules.count { schedule ->
                    val scheduleDate = Instant.fromEpochMilliseconds(schedule.startTime)
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date
                    scheduleDate == today
                }

                // Find next upcoming workout
                val currentTime = Clock.System.now().toEpochMilliseconds()
                val upcomingWorkout = schedules
                    .filter { it.startTime > currentTime }
                    .minByOrNull { it.startTime }
                    ?.let { WorkoutSchedulePreview(it.title, it.startTime, it.endTime, it.color) }

                // Generate random tips
                val tipColors = listOf(
                    0xFF1976D2, // Blue
                    0xFF388E3C, // Green
                    0xFFF57C00, // Orange
                    0xFF7B1FA2, // Purple
                    0xFFC2185B, // Pink
                    0xFF00796B  // Teal
                )

                val randomTips = homeFitnessTips.shuffled().take(6).map { tip ->
                    DailyTip(
                        tip = tip,
                        icon = TipIcon.random(),
                        color = tipColors[Random.nextInt(tipColors.size)]
                    )
                }

                _state.value = _state.value.copy(
                    scheduledWorkoutsToday = scheduledToday,
                    totalScheduledWorkouts = schedules.size,
                    upcomingWorkout = upcomingWorkout,
                    dailyTips = randomTips
                )

                // Load calorie and fitness stats
                _state.value = _state.value.copy(
                    caloriesGoal = userSettings.calculatedCalories,
                    bmr = userSettings.bmr,
                    tdee = userSettings.tdee,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun refresh() {
        loadDashboardData()
    }
} 
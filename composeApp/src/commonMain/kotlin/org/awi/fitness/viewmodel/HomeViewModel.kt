package org.awi.fitness.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.Client
import org.awi.fitness.repository.ClientRepository
import org.awi.fitness.utils.getHomeFitnessTips
import kotlinx.datetime.*
import kotlin.random.Random

data class HomeScreenState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val userProfile: Client? = null,
    val caloriesGoal: Int = 0,
    val bmr: Float = 0f,
    val tdee: Float = 0f,
    val scheduledWorkoutsToday: Int = 0,
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
    WATER, FOOD, SLEEP, EXERCISE, MINDFULNESS, HEALTH
}

/** Maps a wellness tip to a matching icon by its content (deterministic, not random). */
private fun tipIconFor(tip: String): TipIcon {
    val t = tip.lowercase()
    return when {
        t.contains("water") || t.contains("hydrat") || t.contains("drink") -> TipIcon.WATER
        t.contains("sleep") || t.contains("rest") || t.contains("recover") -> TipIcon.SLEEP
        t.contains("eat") || t.contains("food") || t.contains("meal") || t.contains("protein") || t.contains("nutrition") || t.contains("diet") || t.contains("veg") || t.contains("fruit") -> TipIcon.FOOD
        t.contains("workout") || t.contains("exercise") || t.contains("train") || t.contains("run") || t.contains("walk") || t.contains("strength") || t.contains("move") -> TipIcon.EXERCISE
        t.contains("breath") || t.contains("stress") || t.contains("relax") || t.contains("mind") || t.contains("medit") || t.contains("calm") -> TipIcon.MINDFULNESS
        else -> TipIcon.HEALTH
    }
}

class HomeViewModel {
    private val userSettings = UserSettings.getInstance()
    private val clientRepository = ClientRepository()

    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    fun loadIfNeeded() {
        if (!_state.value.isLoading && _state.value.userProfile != null) return
        if (_state.value.isLoading && _state.value.userProfile != null) return
        loadDashboardData()
    }

    private fun loadDashboardData() {
        _state.value = _state.value.copy(isLoading = true)

        kotlinx.coroutines.MainScope().launch {
            try {
                val userEmail = userSettings.userEmail
                if (userEmail != null) {
                    val profileResult = clientRepository.getClientByEmail(userEmail)
                    profileResult.onSuccess { client ->
                        _state.value = _state.value.copy(userProfile = client)
                    }
                }

                val schedules = userSettings.workoutSchedules.value
                val today = org.awi.fitness.utils.todayLocalDate()
                val scheduledToday = schedules.count { schedule ->
                    val scheduleDate = Instant.fromEpochMilliseconds(schedule.startTime)
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date
                    scheduleDate == today
                }

                val currentTime = org.awi.fitness.utils.currentTimeMillis()
                val upcomingWorkout = schedules
                    .filter { it.startTime > currentTime }
                    .minByOrNull { it.startTime }
                    ?.let { WorkoutSchedulePreview(it.title, it.startTime, it.endTime, it.color) }

                val tipColors = listOf(
                    0xFF1976D2, 0xFF388E3C, 0xFFF57C00,
                    0xFF7B1FA2, 0xFFC2185B, 0xFF00796B
                )
                val currentLang = userSettings.language.value ?: "en"
                // Stable per day: seed the shuffle with the date so the tips (and their order)
                // don't re-randomize on every load. The icon is derived from the tip content,
                // and the color is assigned by position — both deterministic.
                val daySeed = org.awi.fitness.utils.todayLocalDate().toEpochDays().toLong()
                val rng = Random(daySeed)
                val randomTips = getHomeFitnessTips(currentLang).shuffled(rng).take(6).mapIndexed { index, tip ->
                    DailyTip(
                        tip = tip,
                        icon = tipIconFor(tip),
                        color = tipColors[index % tipColors.size]
                    )
                }

                _state.value = _state.value.copy(
                    scheduledWorkoutsToday = scheduledToday,
                    upcomingWorkout = upcomingWorkout,
                    dailyTips = randomTips,
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
        _state.value = _state.value.copy(isLoading = false)
        loadDashboardData()
    }
}

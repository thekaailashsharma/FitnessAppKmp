package org.awi.fitness.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.datetime.toLocalDateTime

@Serializable
data class WeighInEntry(
    val weight: Float,
    val date: Long,
    val note: String = ""
)

@Serializable
data class MeasurementEntry(
    val waist: Float,
    val hips: Float,
    val arms: Float,
    val date: Long,
    val note: String = ""
)

@Serializable
data class WorkoutSchedule(
    val id: String,
    val title: String,
    val description: String = "",
    val startTime: Long,
    val endTime: Long,
    val workoutType: WorkoutType,
    val recurringType: RecurringType = RecurringType.NONE,
    val color: Long,
    val isCompleted: Boolean = false
)

@Serializable
enum class WorkoutType {
    CARDIO, STRENGTH, FLEXIBILITY, HIIT, YOGA, OTHER
}

@Serializable
enum class RecurringType {
    NONE, DAILY, WEEKLY, MONTHLY
}

class UserSettings internal constructor(internal val settings: Settings) {
    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val KEY_APPLE_USER_ID = "apple_user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_APPLE_IDENTITY_TOKEN = "apple_identity_token"
        private const val KEY_APPLE_AUTH_CODE = "apple_auth_code"
        private const val KEY_AUTH_PROVIDER = "auth_provider"
        private const val IS_DARK_THEME = "is_dark_theme"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_CALCULATED_CALORIES = "calculated_calories"
        private const val KEY_BMR = "bmr"
        private const val KEY_TDEE = "tdee"
        private const val KEY_WEIGH_INS = "weigh_ins"
        private const val KEY_MEASUREMENTS = "measurements"
        private const val KEY_LAST_REMINDER_CHECK = "last_reminder_check"
        private const val KEY_WEIGH_IN_REMINDER_ENABLED = "weigh_in_reminder_enabled"
        private const val KEY_MEASUREMENT_REMINDER_ENABLED = "measurement_reminder_enabled"
        private const val KEY_WORKOUT_SCHEDULES = "workout_schedules"
        private const val KEY_MEAL_COMPLETIONS = "meal_completions"
        private const val KEY_SHOPPING_CHECKS = "shopping_checks"
        private const val KEY_LAST_ARTICLE_REFRESH = "last_article_refresh"
        private const val KEY_HAS_COMPLETED_ONBOARDING = "has_completed_onboarding"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_SELECTED_AVATAR_ID = "selected_avatar_id"
        private const val KEY_LAST_CHECK_IN_DATE = "last_check_in_date"
        private const val KEY_DAILY_CHECK_INS = "daily_check_ins"
        private const val KEY_USER_BIO = "user_bio"
        private const val KEY_PROFILE_PHOTO_URL = "profile_photo_url"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_LAST_WORKOUT_DATE = "last_workout_date"
        private const val KEY_TOTAL_XP = "total_xp"
        private const val KEY_USER_LEVEL = "user_level"
        private const val KEY_FITNESS_GOAL = "fitness_goal"
        private const val KEY_FITNESS_LEVEL = "fitness_level"
        private const val KEY_WORKOUT_DAYS = "workout_days_per_week"

        private var instance: UserSettings? = null

        @OptIn(InternalCoroutinesApi::class)
        fun getInstance(): UserSettings {
            return instance ?: synchronized(lock = SynchronizedObject()) {
                instance ?: UserSettings(createSettings()).also { instance = it }
            }
        }
    }

    private val _language = MutableStateFlow(settings[KEY_LANGUAGE, Language.ENGLISH.code])
    val language = _language.asStateFlow()

    private val _isLoggedInFlow = MutableStateFlow(settings[KEY_IS_LOGGED_IN, false])
    val isLoggedInFlow = _isLoggedInFlow.asStateFlow()

    private val _isDarkThemeFlow = MutableStateFlow(
        if (settings.hasKey(IS_DARK_THEME)) settings[IS_DARK_THEME, false] else null
    )
    val isDarkThemeFlow: StateFlow<Boolean?> = _isDarkThemeFlow.asStateFlow()

    fun setLanguage(value: String) {
        settings[KEY_LANGUAGE] = value
        _language.value = value
    }

    var authToken: String?
        get() = settings[KEY_AUTH_TOKEN]
        set(value) {
            settings[KEY_AUTH_TOKEN] = value
        }

    var userEmail: String?
        get() = settings[KEY_USER_EMAIL]
        set(value) {
            settings[KEY_USER_EMAIL] = value
        }

    var userId: String?
        get() = settings[KEY_USER_ID]
        set(value) {
            settings[KEY_USER_ID] = value
        }

    var isLoggedIn: Boolean
        get() = settings[KEY_IS_LOGGED_IN, false]
        set(value) {
            settings[KEY_IS_LOGGED_IN] = value
            _isLoggedInFlow.value = value
        }

    var refreshToken: String?
        get() = settings[KEY_REFRESH_TOKEN]
        set(value) {
            settings[KEY_REFRESH_TOKEN] = value
        }

    var tokenExpiryTime: String?
        get() = settings[KEY_TOKEN_EXPIRY]
        set(value) {
            settings[KEY_TOKEN_EXPIRY] = value
        }

    var appleUserId: String?
        get() = settings[KEY_APPLE_USER_ID]
        set(value) {
            settings[KEY_APPLE_USER_ID] = value
        }

    var userName: String?
        get() = settings[KEY_USER_NAME]
        set(value) {
            settings[KEY_USER_NAME] = value
        }

    var userBio: String?
        get() = settings[KEY_USER_BIO]
        set(value) {
            settings[KEY_USER_BIO] = value
        }

    var profilePhotoUrl: String?
        get() = settings[KEY_PROFILE_PHOTO_URL]
        set(value) {
            settings[KEY_PROFILE_PHOTO_URL] = value
        }

    var currentStreak: Int
        get() = settings[KEY_CURRENT_STREAK, 0]
        set(value) { settings[KEY_CURRENT_STREAK] = value }

    var lastWorkoutDate: String
        get() = settings[KEY_LAST_WORKOUT_DATE, ""]
        set(value) { settings[KEY_LAST_WORKOUT_DATE] = value }

    var totalXp: Int
        get() = settings[KEY_TOTAL_XP, 0]
        set(value) { settings[KEY_TOTAL_XP] = value }

    var userLevel: Int
        get() = settings[KEY_USER_LEVEL, 1]
        set(value) { settings[KEY_USER_LEVEL] = value }

    fun recordWorkoutCompleted() {
        val today = org.awi.fitness.utils.todayLocalDate().toString()
        val last = lastWorkoutDate
        val newStreak = when {
            last.isBlank() -> 1
            last == today -> currentStreak // already recorded today
            else -> {
                val todayDate = org.awi.fitness.utils.todayLocalDate()
                val lastDate = try {
                    kotlinx.datetime.LocalDate.parse(last)
                } catch (_: Exception) { null }
                if (lastDate != null && todayDate.toEpochDays().toLong() - lastDate.toEpochDays().toLong() == 1L) {
                    currentStreak + 1
                } else {
                    1 // streak broken
                }
            }
        }
        currentStreak = newStreak
        lastWorkoutDate = today
        // XP for workout: 50 XP base
        totalXp += 50
        // Level up every 500 XP
        userLevel = (totalXp / 500) + 1
    }

    fun recordXpEarned(xp: Int) {
        totalXp += xp
        userLevel = (totalXp / 500) + 1
    }

    // Fitness preferences set during onboarding — used to generate plan in background
    var fitnessGoal: String
        get() = settings[KEY_FITNESS_GOAL, ""]
        set(value) { settings[KEY_FITNESS_GOAL] = value }

    var fitnessLevel: String
        get() = settings[KEY_FITNESS_LEVEL, ""]
        set(value) { settings[KEY_FITNESS_LEVEL] = value }

    var workoutDaysPerWeek: Int
        get() = settings[KEY_WORKOUT_DAYS, 3]
        set(value) { settings[KEY_WORKOUT_DAYS] = value }

    var appleIdentityToken: String?
        get() = settings[KEY_APPLE_IDENTITY_TOKEN]
        set(value) {
            settings[KEY_APPLE_IDENTITY_TOKEN] = value
        }

    var appleAuthorizationCode: String?
        get() = settings[KEY_APPLE_AUTH_CODE]
        set(value) {
            settings[KEY_APPLE_AUTH_CODE] = value
        }

    var isDarkTheme: Boolean?
        get() = if (settings.hasKey(IS_DARK_THEME)) settings[IS_DARK_THEME, false] else null
        set(value) {
            if (value == null) {
                settings.remove(IS_DARK_THEME)
            } else {
                settings[IS_DARK_THEME] = value
            }
            _isDarkThemeFlow.value = value
        }

    var authProvider: String?
        get() = settings[KEY_AUTH_PROVIDER]
        set(value) {
            settings[KEY_AUTH_PROVIDER] = value
        }

    var calculatedCalories: Int
        get() = settings[KEY_CALCULATED_CALORIES, 0]
        set(value) {
            settings[KEY_CALCULATED_CALORIES] = value
        }

    var bmr: Float
        get() = settings[KEY_BMR, 0f]
        set(value) {
            settings[KEY_BMR] = value
        }

    var tdee: Float
        get() = settings[KEY_TDEE, 0f]
        set(value) {
            settings[KEY_TDEE] = value
        }

    private val _weighIns = MutableStateFlow<List<WeighInEntry>>(emptyList())
    val weighIns = _weighIns.asStateFlow()

    private val _measurements = MutableStateFlow<List<MeasurementEntry>>(emptyList())
    val measurements = _measurements.asStateFlow()

    private val _workoutSchedules = MutableStateFlow<List<WorkoutSchedule>>(emptyList())
    val workoutSchedules: StateFlow<List<WorkoutSchedule>> = _workoutSchedules.asStateFlow()

    private val _mealCompletions = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val mealCompletions: StateFlow<Map<String, Set<String>>> = _mealCompletions.asStateFlow()

    private val _shoppingChecks = MutableStateFlow<Set<String>>(emptySet())
    val shoppingChecks: StateFlow<Set<String>> = _shoppingChecks.asStateFlow()

    private val _dailyCheckIns = MutableStateFlow<List<org.awi.fitness.model.DailyCheckIn>>(emptyList())
    val dailyCheckIns: StateFlow<List<org.awi.fitness.model.DailyCheckIn>> = _dailyCheckIns.asStateFlow()

    init {
        loadWeighIns()
        loadMeasurements()
        loadWorkoutSchedules()
        loadMealCompletions()
        loadShoppingChecks()
        loadDailyCheckIns()
    }

    private fun loadWeighIns() {
        val weighInsJson = settings[KEY_WEIGH_INS, "[]"]
        _weighIns.value = Json.decodeFromString(weighInsJson)
    }

    private fun loadMeasurements() {
        val measurementsJson = settings[KEY_MEASUREMENTS, "[]"]
        _measurements.value = Json.decodeFromString(measurementsJson)
    }

    private fun loadWorkoutSchedules() {
        val schedulesJson = settings[KEY_WORKOUT_SCHEDULES, "[]"]
        _workoutSchedules.value = Json.decodeFromString(schedulesJson)
    }

    fun addWeighIn(entry: WeighInEntry) {
        val currentList = _weighIns.value.toMutableList()
        currentList.add(entry)
        _weighIns.value = currentList
        settings[KEY_WEIGH_INS] = Json.encodeToString(currentList)
    }

    fun addMeasurement(entry: MeasurementEntry) {
        val currentList = _measurements.value.toMutableList()
        currentList.add(entry)
        _measurements.value = currentList
        settings[KEY_MEASUREMENTS] = Json.encodeToString(currentList)
    }

    var weighInReminderEnabled: Boolean
        get() = settings[KEY_WEIGH_IN_REMINDER_ENABLED, false]
        set(value) {
            settings[KEY_WEIGH_IN_REMINDER_ENABLED] = value
        }

    var measurementReminderEnabled: Boolean
        get() = settings[KEY_MEASUREMENT_REMINDER_ENABLED, false]
        set(value) {
            settings[KEY_MEASUREMENT_REMINDER_ENABLED] = value
        }

    var lastReminderCheck: Long
        get() = settings[KEY_LAST_REMINDER_CHECK, 0L]
        set(value) {
            settings[KEY_LAST_REMINDER_CHECK] = value
        }

    fun addWorkoutSchedule(schedule: WorkoutSchedule) {
        val currentList = _workoutSchedules.value.toMutableList()
        currentList.add(schedule)
        _workoutSchedules.value = currentList
        settings[KEY_WORKOUT_SCHEDULES] = Json.encodeToString(currentList)
    }

    fun updateWorkoutSchedule(schedule: WorkoutSchedule) {
        val currentList = _workoutSchedules.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == schedule.id }
        if (index != -1) {
            currentList[index] = schedule
            _workoutSchedules.value = currentList
            settings[KEY_WORKOUT_SCHEDULES] = Json.encodeToString(currentList)
        }
    }

    fun deleteWorkoutSchedule(scheduleId: String) {
        val currentList = _workoutSchedules.value.toMutableList()
        currentList.removeAll { it.id == scheduleId }
        _workoutSchedules.value = currentList
        settings[KEY_WORKOUT_SCHEDULES] = Json.encodeToString(currentList)
    }

    // ── Meal Completions ──

    private fun loadMealCompletions() {
        val json = settings[KEY_MEAL_COMPLETIONS, "{}"]
        _mealCompletions.value = try {
            Json.decodeFromString<Map<String, Set<String>>>(json)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun isMealCompleted(date: String, mealId: String): Boolean {
        return _mealCompletions.value[date]?.contains(mealId) == true
    }

    fun toggleMealCompletion(date: String, mealId: String) {
        val current = _mealCompletions.value.toMutableMap()
        val daySet = current[date]?.toMutableSet() ?: mutableSetOf()
        if (daySet.contains(mealId)) daySet.remove(mealId) else daySet.add(mealId)
        if (daySet.isEmpty()) current.remove(date) else current[date] = daySet
        _mealCompletions.value = current
        settings[KEY_MEAL_COMPLETIONS] = Json.encodeToString(current)
    }

    fun getCompletionsForDate(date: String): Set<String> {
        return _mealCompletions.value[date] ?: emptySet()
    }

    // ── Shopping List Checks ──

    private fun loadShoppingChecks() {
        val json = settings[KEY_SHOPPING_CHECKS, "[]"]
        _shoppingChecks.value = try {
            Json.decodeFromString<Set<String>>(json)
        } catch (_: Exception) {
            emptySet()
        }
    }

    fun toggleShoppingCheck(item: String) {
        val current = _shoppingChecks.value.toMutableSet()
        if (current.contains(item)) current.remove(item) else current.add(item)
        _shoppingChecks.value = current
        settings[KEY_SHOPPING_CHECKS] = Json.encodeToString(current)
    }

    fun isShoppingItemChecked(item: String): Boolean {
        return _shoppingChecks.value.contains(item)
    }

    fun clearShoppingChecks() {
        _shoppingChecks.value = emptySet()
        settings[KEY_SHOPPING_CHECKS] = "[]"
    }

    var lastArticleRefresh: Long
        get() = settings[KEY_LAST_ARTICLE_REFRESH, 0L]
        set(value) {
            settings[KEY_LAST_ARTICLE_REFRESH] = value
        }

    var hasCompletedOnboarding: Boolean
        get() = settings[KEY_HAS_COMPLETED_ONBOARDING, false]
        set(value) {
            settings[KEY_HAS_COMPLETED_ONBOARDING] = value
        }

    var geminiApiKey: String?
        get() = settings[KEY_GEMINI_API_KEY]
        set(value) {
            settings[KEY_GEMINI_API_KEY] = value
        }

    var selectedAvatarId: String?
        get() = settings[KEY_SELECTED_AVATAR_ID]
        set(value) {
            if (value.isNullOrBlank()) {
                settings.remove(KEY_SELECTED_AVATAR_ID)
            } else {
                settings[KEY_SELECTED_AVATAR_ID] = value
            }
        }

    var lastCheckInDate: Long
        get() = settings[KEY_LAST_CHECK_IN_DATE, 0L]
        set(value) {
            settings[KEY_LAST_CHECK_IN_DATE] = value
        }

    private fun loadDailyCheckIns() {
        val checkInsJson = settings[KEY_DAILY_CHECK_INS, "[]"]
        _dailyCheckIns.value = Json.decodeFromString(checkInsJson)
    }

    fun addDailyCheckIn(checkIn: org.awi.fitness.model.DailyCheckIn) {
        val currentList = _dailyCheckIns.value.toMutableList()
        currentList.removeAll { it.date == checkIn.date }
        currentList.add(checkIn)
        _dailyCheckIns.value = currentList
        settings[KEY_DAILY_CHECK_INS] = Json.encodeToString(currentList)
        lastCheckInDate = checkIn.date
    }

    fun getTodayCheckIn(): org.awi.fitness.model.DailyCheckIn? {
        val today = org.awi.fitness.utils.currentInstant()
            .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
            .date
        val todayStart = today.toEpochDays() * 86400000L
        return _dailyCheckIns.value.firstOrNull { it.date == todayStart }
    }

    fun isCheckInCompletedToday(): Boolean {
        return getTodayCheckIn()?.completed == true
    }

    fun clearUserData() {
        // Preserve device-level preferences that survive logout
        val preservedOnboarding = hasCompletedOnboarding
        val preservedDark = isDarkTheme
        val preservedLang = language.value

        settings.clear()

        // Restore device-level prefs immediately after clear
        hasCompletedOnboarding = preservedOnboarding
        if (preservedDark != null) isDarkTheme = preservedDark
        setLanguage(preservedLang)

        // Clear auth state
        authToken = null
        refreshToken = null
        userEmail = null
        userId = null
        tokenExpiryTime = null
        isLoggedIn = false
        appleUserId = null
        userName = null
        appleIdentityToken = null
        appleAuthorizationCode = null
        authProvider = null
        _weighIns.value = emptyList()
        _measurements.value = emptyList()
        _workoutSchedules.value = emptyList()
        _mealCompletions.value = emptyMap()
        _shoppingChecks.value = emptySet()
        selectedAvatarId = null
        _dailyCheckIns.value = emptyList()
        lastCheckInDate = 0L
    }
}
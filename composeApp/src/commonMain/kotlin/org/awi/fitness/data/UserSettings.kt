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

    init {
        loadWeighIns()
        loadMeasurements()
        loadWorkoutSchedules()
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

    fun clearUserData() {
        settings.clear()
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
    }
} 
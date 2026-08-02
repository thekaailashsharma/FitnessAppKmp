package org.awi.fitness.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.datetime.toLocalDateTime
import org.awi.fitness.utils.currentTimeMillis

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
        private const val KEY_LONGEST_STREAK = "longest_streak"
        private const val KEY_CACHED_FCM_TOKEN = "cached_fcm_token"
        private const val KEY_BLOCKED_USERS = "blocked_user_ids"
        private const val KEY_LAST_WORKOUT_DATE = "last_workout_date"
        private const val KEY_TOTAL_XP = "total_xp"
        private const val KEY_USER_LEVEL = "user_level"
        private const val KEY_WORKOUTS_COMPLETED = "workouts_completed"
        private const val KEY_CHALLENGES_COMPLETED = "challenges_completed"
        private const val KEY_EARNED_BADGES = "earned_badges"
        private const val KEY_CHALLENGE_DAILY_ADVANCES = "challenge_daily_advances"

        /** Single source of truth for XP→level; level = totalXp / XP_PER_LEVEL + 1. */
        const val XP_PER_LEVEL = 500
        /** XP granted for completing a daily check-in. */
        const val CHECK_IN_XP = 20
        private const val KEY_FITNESS_GOAL = "fitness_goal"
        private const val KEY_FITNESS_LEVEL = "fitness_level"
        private const val KEY_WORKOUT_DAYS = "workout_days_per_week"
        private const val KEY_SOCIAL_LINKS = "social_links"
        private const val KEY_COMMUNITY_BANNER = "community_banner"
        private const val KEY_COMMUNITY_INTRO_SEEN = "community_intro_seen"
        private const val KEY_SCAN_LOG = "scan_log"
        private const val KEY_BACKDROPS_CONFIG = "backdrops_config"
        private const val KEY_BACKDROPS_CACHE = "backdrops_cache"
        private const val KEY_BACKDROPS_LAST_FETCH = "backdrops_last_fetch"
        private const val KEY_BACKDROPS_SELECTION = "backdrops_selection"
        private const val KEY_SELECTED_HOME_BACKDROP = "selected_home_backdrop"
        private const val KEY_PROFILE_HEIGHT_CM = "profile_height_cm"
        private const val KEY_PROFILE_WEIGHT_KG = "profile_weight_kg"
        private const val KEY_PROFILE_AGE = "profile_age"
        private const val KEY_PROFILE_GENDER = "profile_gender"
        private const val KEY_ONBOARDING_STEP = "onboarding_step"
        private const val KEY_PREMIUM_LAST_VERIFIED = "premium_last_verified_at"
        private const val KEY_PREMIUM_GRACE_HOURS = "premium_grace_hours"
        private const val KEY_SUBCFG_SHOW_REFUND = "subcfg_show_refund"
        private const val KEY_SUBCFG_SHOW_PLAN_SWITCH = "subcfg_show_plan_switch"
        private const val KEY_SUBCFG_SHOW_PURCHASE_HISTORY = "subcfg_show_purchase_history"
        private const val KEY_SUBCFG_SHOW_INVOICES = "subcfg_show_invoices"
        private const val KEY_SUBCFG_SUPPORT_URL = "subcfg_support_url"
        private const val KEY_FEATURE_CONFIG_JSON = "feature_config_json"
        private const val KEY_FEATURE_USAGE_DAY = "feature_usage_day"
        private const val KEY_FEATURE_USAGE_COUNTS = "feature_usage_counts"

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

    /** Community social links (newline-separated: website / handle / other), persisted locally. */
    var socialLinks: String?
        get() = settings[KEY_SOCIAL_LINKS]
        set(value) {
            settings[KEY_SOCIAL_LINKS] = value
        }

    /** Chosen community cover banner (drawable name), persisted locally. */
    var communityBanner: String?
        get() = settings[KEY_COMMUNITY_BANNER]
        set(value) {
            settings[KEY_COMMUNITY_BANNER] = value
        }

    /** Whether the community "constellation" intro has been shown once. */
    var communityIntroSeen: Boolean
        get() = settings[KEY_COMMUNITY_INTRO_SEEN, false]
        set(value) {
            settings[KEY_COMMUNITY_INTRO_SEEN] = value
        }

    /** Locally-journaled AI meal-scan results (JSON array). */
    var scanLog: String
        get() = settings[KEY_SCAN_LOG, "[]"]
        set(value) {
            settings[KEY_SCAN_LOG] = value
        }

    // ── Remote-controllable backdrops (additive; degrade to bundled drawables) ──

    /** Cached remote app configuration for backdrops, as JSON (null until first fetch). */
    var backdropsConfig: String?
        get() = settings[KEY_BACKDROPS_CONFIG]
        set(value) {
            settings[KEY_BACKDROPS_CONFIG] = value
        }

    /** Cached list of remote backdrops, as a JSON array (defaults to empty). */
    var backdropsCache: String
        get() = settings[KEY_BACKDROPS_CACHE, "[]"]
        set(value) {
            settings[KEY_BACKDROPS_CACHE] = value
        }

    /** Epoch millis of the last successful backdrop fetch (0 = never). */
    var backdropsLastFetch: Long
        get() = settings[KEY_BACKDROPS_LAST_FETCH, 0L]
        set(value) {
            settings[KEY_BACKDROPS_LAST_FETCH] = value
        }

    /** Memoized per-surface backdrop selection, as a JSON object (defaults to empty). */
    var backdropsSelection: String
        get() = settings[KEY_BACKDROPS_SELECTION, "{}"]
        set(value) {
            settings[KEY_BACKDROPS_SELECTION] = value
        }

    private val _selectedHomeBackdropFlow =
        MutableStateFlow(settings.getStringOrNull(KEY_SELECTED_HOME_BACKDROP))

    /** The user's chosen Home backdrop id, or null when following the theme-aware default. */
    val selectedHomeBackdropFlow: StateFlow<String?> = _selectedHomeBackdropFlow.asStateFlow()

    /** The bundled drawable id the user picked as their Home backdrop (null = automatic default). */
    var selectedHomeBackdrop: String?
        get() = settings.getStringOrNull(KEY_SELECTED_HOME_BACKDROP)
        set(value) {
            if (value.isNullOrBlank()) {
                settings.remove(KEY_SELECTED_HOME_BACKDROP)
            } else {
                settings[KEY_SELECTED_HOME_BACKDROP] = value
            }
            _selectedHomeBackdropFlow.value = value
        }

    var currentStreak: Int
        get() = settings[KEY_CURRENT_STREAK, 0]
        set(value) { settings[KEY_CURRENT_STREAK] = value }

    /** Best streak ever reached — powers "you're 1 day from your record" push moments. */
    var longestStreak: Int
        get() = settings[KEY_LONGEST_STREAK, 0]
        set(value) { settings[KEY_LONGEST_STREAK] = value }

    /** FCM token handed over from the iOS Firebase SDK (Swift) — read on login to register. */
    var cachedFcmToken: String?
        get() = settings[KEY_CACHED_FCM_TOKEN, ""].ifBlank { null }
        set(value) { settings[KEY_CACHED_FCM_TOKEN] = value ?: "" }

    /** User IDs (emails) the user has blocked — their posts are filtered out of the feed.
     *  Required for App Store UGC compliance (Guideline 1.2). */
    var blockedUserIds: Set<String>
        get() = try { Json.decodeFromString(settings[KEY_BLOCKED_USERS, "[]"]) } catch (_: Exception) { emptySet() }
        set(value) { settings[KEY_BLOCKED_USERS] = Json.encodeToString(value) }

    fun blockUser(userId: String) {
        if (userId.isNotBlank()) blockedUserIds = blockedUserIds + userId
    }

    var lastWorkoutDate: String
        get() = settings[KEY_LAST_WORKOUT_DATE, ""]
        set(value) { settings[KEY_LAST_WORKOUT_DATE] = value }

    var totalXp: Int
        get() = settings[KEY_TOTAL_XP, 0]
        set(value) { settings[KEY_TOTAL_XP] = value }

    var userLevel: Int
        get() = settings[KEY_USER_LEVEL, 1]
        set(value) { settings[KEY_USER_LEVEL] = value }

    /** Total workouts completed (lifetime) — drives the "First Steps" achievement. */
    var workoutsCompleted: Int
        get() = settings[KEY_WORKOUTS_COMPLETED, 0]
        set(value) { settings[KEY_WORKOUTS_COMPLETED] = value }

    /** Total challenges completed (lifetime) — drives challenge achievements. */
    var challengesCompleted: Int
        get() = settings[KEY_CHALLENGES_COMPLETED, 0]
        set(value) { settings[KEY_CHALLENGES_COMPLETED] = value }

    private val _earnedBadgeIds = MutableStateFlow(loadEarnedBadgeIds())
    /** Earned achievement/badge ids, persisted locally. */
    val earnedBadgeIdsFlow: StateFlow<Set<String>> = _earnedBadgeIds.asStateFlow()

    val earnedBadgeIds: Set<String>
        get() = _earnedBadgeIds.value

    private fun loadEarnedBadgeIds(): Set<String> =
        try { Json.decodeFromString<Set<String>>(settings[KEY_EARNED_BADGES, "[]"]) }
        catch (_: Exception) { emptySet() }

    /** Recompute level from a single xp-per-level constant. */
    private fun recomputeLevel() {
        userLevel = (totalXp / XP_PER_LEVEL) + 1
    }

    /** Advance the daily activity streak at most once per calendar day. */
    private fun advanceStreakForToday() {
        val today = org.awi.fitness.utils.todayLocalDate().toString()
        val last = lastWorkoutDate
        if (last == today) return // already counted today
        val newStreak = when {
            last.isBlank() -> 1
            else -> {
                val todayDate = org.awi.fitness.utils.todayLocalDate()
                val lastDate = try {
                    kotlinx.datetime.LocalDate.parse(last)
                } catch (_: Exception) { null }
                if (lastDate != null && todayDate.toEpochDays().toLong() - lastDate.toEpochDays().toLong() == 1L) {
                    currentStreak + 1
                } else {
                    1 // streak broken / first day
                }
            }
        }
        currentStreak = newStreak
        if (newStreak > longestStreak) longestStreak = newStreak
        lastWorkoutDate = today
    }

    fun recordWorkoutCompleted() {
        advanceStreakForToday()
        workoutsCompleted += 1
        // XP for workout: 50 XP base
        totalXp += 50
        recomputeLevel()
        onStatsChanged()
    }

    fun recordXpEarned(xp: Int) {
        totalXp += xp
        recomputeLevel()
        onStatsChanged()
    }

    /** Called when a challenge is completed — counts it and awards its XP via [recordXpEarned]. */
    fun recordChallengeCompleted(xpReward: Int) {
        challengesCompleted += 1
        recordXpEarned(xpReward) // also recomputes level, evaluates badges, syncs
    }

    /** Daily check-in reward: counts today toward the streak and grants XP. */
    fun recordCheckInReward() {
        advanceStreakForToday()
        recordXpEarned(CHECK_IN_XP) // grants XP, recomputes level, evaluates badges, syncs
    }

    /**
     * Award any newly-earned achievement badges from current real stats.
     * Returns the ids awarded this call. Idempotent — already-earned ids are skipped.
     */
    fun evaluateBadges(): List<String> {
        val stats = org.awi.fitness.model.AchievementStats(
            totalXp = totalXp,
            level = userLevel,
            currentStreak = currentStreak,
            workoutsCompleted = workoutsCompleted,
            challengesCompleted = challengesCompleted
        )
        val current = _earnedBadgeIds.value
        val newly = org.awi.fitness.model.AchievementCatalog.all
            .filter { it.badge.id !in current && it.isEarned(stats) }
            .map { it.badge.id }
        if (newly.isNotEmpty()) {
            val updated = current + newly
            _earnedBadgeIds.value = updated
            settings[KEY_EARNED_BADGES] = Json.encodeToString(updated)
        }
        return newly
    }

    // Per-challenge, per-day advance guard for day-based (streak) challenges.
    private fun loadChallengeDailyAdvances(): MutableMap<String, String> =
        try { Json.decodeFromString<Map<String, String>>(settings[KEY_CHALLENGE_DAILY_ADVANCES, "{}"]).toMutableMap() }
        catch (_: Exception) { mutableMapOf() }

    /** True if this challenge has NOT yet been advanced today (day-based challenges only). */
    fun canAdvanceChallengeToday(challengeId: String): Boolean {
        val today = org.awi.fitness.utils.todayLocalDate().toString()
        return loadChallengeDailyAdvances()[challengeId] != today
    }

    /** Mark a day-based challenge as advanced for today. */
    fun markChallengeAdvancedToday(challengeId: String) {
        val today = org.awi.fitness.utils.todayLocalDate().toString()
        val map = loadChallengeDailyAdvances()
        map[challengeId] = today
        settings[KEY_CHALLENGE_DAILY_ADVANCES] = Json.encodeToString(map)
    }

    // Fire whenever XP / level / streak change: award badges + best-effort sync to Firestore.
    private val statsScope = kotlinx.coroutines.MainScope()
    private fun onStatsChanged() {
        evaluateBadges()
        statsScope.launch {
            runCatching { org.awi.fitness.repository.ChallengesRepository().syncStatsToFirestore() }
        }
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

    // Physical stats captured in onboarding — used to prefill the calorie calculator and profile.
    //    0 / "" means "not set yet".
    var profileHeightCm: Int
        get() = settings[KEY_PROFILE_HEIGHT_CM, 0]
        set(value) { settings[KEY_PROFILE_HEIGHT_CM] = value }

    var profileWeightKg: Float
        get() = settings[KEY_PROFILE_WEIGHT_KG, 0f]
        set(value) { settings[KEY_PROFILE_WEIGHT_KG] = value }

    var profileAge: Int
        get() = settings[KEY_PROFILE_AGE, 0]
        set(value) { settings[KEY_PROFILE_AGE] = value }

    /** "MALE" / "FEMALE" — empty when not set. */
    var profileGender: String
        get() = settings[KEY_PROFILE_GENDER, ""]
        set(value) { settings[KEY_PROFILE_GENDER] = value }

    /** Resume point for an unfinished onboarding (0 when not started / already done). */
    var onboardingStep: Int
        get() = settings[KEY_ONBOARDING_STEP, 0]
        set(value) { settings[KEY_ONBOARDING_STEP] = value }

    /** Epoch millis of the last time premium was confirmed active online — drives the offline grace window. */
    var premiumLastVerifiedAt: Long
        get() = settings[KEY_PREMIUM_LAST_VERIFIED, 0L]
        set(value) { settings[KEY_PREMIUM_LAST_VERIFIED] = value }

    /** How long (hours) to trust the last online verification when offline before locking. Remote-configurable. */
    var premiumGraceHours: Int
        get() = settings[KEY_PREMIUM_GRACE_HOURS, 48]
        set(value) { settings[KEY_PREMIUM_GRACE_HOURS] = value }

    // Subscription-management UI flags — cached from Firestore config/subscription (server-driven).
    //    Default to visible so the screen is fully functional before the first config fetch.
    var subCfgShowRefund: Boolean
        get() = settings[KEY_SUBCFG_SHOW_REFUND, true]
        set(value) { settings[KEY_SUBCFG_SHOW_REFUND] = value }

    var subCfgShowPlanSwitch: Boolean
        get() = settings[KEY_SUBCFG_SHOW_PLAN_SWITCH, true]
        set(value) { settings[KEY_SUBCFG_SHOW_PLAN_SWITCH] = value }

    var subCfgShowPurchaseHistory: Boolean
        get() = settings[KEY_SUBCFG_SHOW_PURCHASE_HISTORY, true]
        set(value) { settings[KEY_SUBCFG_SHOW_PURCHASE_HISTORY] = value }

    var subCfgShowInvoices: Boolean
        get() = settings[KEY_SUBCFG_SHOW_INVOICES, true]
        set(value) { settings[KEY_SUBCFG_SHOW_INVOICES] = value }

    var subCfgSupportUrl: String
        get() = settings[KEY_SUBCFG_SUPPORT_URL, ""]
        set(value) { settings[KEY_SUBCFG_SUPPORT_URL] = value }

    // ── Feature gating (freemium control plane) ──────────────────────────────
    // Raw JSON of the remote feature config (config/features). Empty → use bundled default.
    var featureConfigJson: String
        get() = settings[KEY_FEATURE_CONFIG_JSON, ""]
        set(value) { settings[KEY_FEATURE_CONFIG_JSON] = value }

    // Per-day free-usage counters (for freeDailyLimit gates). Auto-reset each calendar day
    //    (day bucket = epoch-ms / 86_400_000). Stored as a small JSON map feature→count.
    private fun currentDayBucket(): Long = currentTimeMillis() / 86_400_000L

    private fun featureCountsToday(): MutableMap<String, Int> {
        if (settings[KEY_FEATURE_USAGE_DAY, 0L] != currentDayBucket()) return mutableMapOf()
        val raw = settings[KEY_FEATURE_USAGE_COUNTS, ""]
        return if (raw.isBlank()) mutableMapOf()
        else try { Json.decodeFromString<Map<String, Int>>(raw).toMutableMap() } catch (_: Exception) { mutableMapOf() }
    }

    /** How many times the free user has used [featureKey] today (resets daily). */
    fun featureUsageToday(featureKey: String): Int = featureCountsToday()[featureKey] ?: 0

    /** Record one use of [featureKey] today (call after a successful free use). */
    fun incrementFeatureUsage(featureKey: String) {
        val counts = featureCountsToday()
        counts[featureKey] = (counts[featureKey] ?: 0) + 1
        settings[KEY_FEATURE_USAGE_DAY] = currentDayBucket()
        settings[KEY_FEATURE_USAGE_COUNTS] = Json.encodeToString<Map<String, Int>>(counts)
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
        // Backdrops are device-level and survive logout
        val preservedBackdropsConfig = backdropsConfig
        val preservedBackdropsCache = backdropsCache
        val preservedBackdropsLastFetch = backdropsLastFetch
        val preservedBackdropsSelection = backdropsSelection
        val preservedSelectedHomeBackdrop = selectedHomeBackdrop

        settings.clear()

        // Restore device-level prefs immediately after clear
        hasCompletedOnboarding = preservedOnboarding
        if (preservedDark != null) isDarkTheme = preservedDark
        setLanguage(preservedLang)
        backdropsConfig = preservedBackdropsConfig
        backdropsCache = preservedBackdropsCache
        backdropsLastFetch = preservedBackdropsLastFetch
        backdropsSelection = preservedBackdropsSelection
        selectedHomeBackdrop = preservedSelectedHomeBackdrop

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
        _earnedBadgeIds.value = emptySet()
    }
}
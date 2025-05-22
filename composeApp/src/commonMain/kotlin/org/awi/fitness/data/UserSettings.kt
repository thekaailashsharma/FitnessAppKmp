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

class UserSettings internal constructor(private val settings: Settings) {
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
    }
} 
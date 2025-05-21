package org.awi.fitness.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.awi.fitness.FitnessApp

actual fun createSettings(): Settings {
    return SharedPreferencesSettings(
        FitnessApp.context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
    )
} 
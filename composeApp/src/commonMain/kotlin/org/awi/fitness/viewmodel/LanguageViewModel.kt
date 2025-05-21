package org.awi.fitness.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.StateFlow
import org.awi.fitness.data.Language
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.Strings
import org.awi.fitness.data.UserSettings

class LanguageViewModel(settings: Settings) : ScreenModel {
    private val userSettings = UserSettings.getInstance()
    val currentLanguage: StateFlow<String> = userSettings.language

    fun setLanguage(language: Language) {
        userSettings.setLanguage(language.code)
    }

    fun getString(key: StringKey): String {
        return Strings.get(key, Language.entries.find { it.code == currentLanguage.value } ?: Language.ENGLISH)
    }
} 
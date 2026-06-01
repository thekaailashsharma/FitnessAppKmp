package org.awi.fitness.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.awi.fitness.data.Language
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.Strings
import org.awi.fitness.viewmodel.LanguageViewModel

/** Compose-aware localized string — always recomposes when language changes. */
@Composable
fun LanguageViewModel.localizedString(key: StringKey): String {
    val code by currentLanguage.collectAsState()
    val language = Language.entries.find { it.code == code } ?: Language.ENGLISH
    return Strings.get(key, language)
}

fun localizedString(key: StringKey, language: Language): String = Strings.get(key, language)

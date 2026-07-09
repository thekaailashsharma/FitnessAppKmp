package org.awi.fitness.data

/**
 * Resolves a [StringKey] to the current app language outside of composition
 * (e.g. from ViewModels). Composables should use LanguageViewModel.getString /
 * localizedString instead.
 */
fun tr(key: StringKey): String {
    val lang = Language.entries.find { it.code == UserSettings.getInstance().language.value }
        ?: Language.ENGLISH
    return Strings.get(key, lang)
}

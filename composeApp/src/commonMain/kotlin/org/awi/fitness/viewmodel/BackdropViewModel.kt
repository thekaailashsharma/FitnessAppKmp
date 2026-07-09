package org.awi.fitness.viewmodel

import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.bg_athlete_female
import fitnessappkmp.composeapp.generated.resources.bg_athlete_male
import fitnessappkmp.composeapp.generated.resources.bg_gym_dark
import fitnessappkmp.composeapp.generated.resources.bg_paywall
import fitnessappkmp.composeapp.generated.resources.bg_run_dawn
import fitnessappkmp.composeapp.generated.resources.bg_success
import fitnessappkmp.composeapp.generated.resources.bg_weights
import fitnessappkmp.composeapp.generated.resources.bg_yoga_calm
import fitnessappkmp.composeapp.generated.resources.card_coach
import fitnessappkmp.composeapp.generated.resources.card_community
import fitnessappkmp.composeapp.generated.resources.card_compete
import fitnessappkmp.composeapp.generated.resources.card_fuel
import fitnessappkmp.composeapp.generated.resources.card_hydrate
import fitnessappkmp.composeapp.generated.resources.card_move
import fitnessappkmp.composeapp.generated.resources.card_read
import fitnessappkmp.composeapp.generated.resources.card_sleep
import fitnessappkmp.composeapp.generated.resources.card_streak
import fitnessappkmp.composeapp.generated.resources.card_yoga
import fitnessappkmp.composeapp.generated.resources.hero_dark_1
import fitnessappkmp.composeapp.generated.resources.hero_dark_2
import fitnessappkmp.composeapp.generated.resources.hero_light_1
import fitnessappkmp.composeapp.generated.resources.hero_light_2
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.AppConfig
import org.awi.fitness.model.Backdrop
import org.awi.fitness.repository.BackdropRepository
import org.awi.fitness.utils.currentInstant
import org.awi.fitness.utils.currentTimeMillis
import org.awi.fitness.utils.deviceLocale
import org.awi.fitness.utils.todayDateString
import org.jetbrains.compose.resources.DrawableResource
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * The resolved backdrop for a given surface. Callers render [Remote.url] and fall
 * back to [Remote.fallback] on load failure, or render [Bundled.res] directly.
 */
sealed class BackdropChoice {
    data class Remote(val url: String, val fallback: DrawableResource) : BackdropChoice()
    data class Bundled(val res: DrawableResource) : BackdropChoice()
}

/** A user-pickable backdrop, referenced by a stable [id] (persisted in UserSettings). */
data class BackdropOption(val id: String, val label: String, val res: DrawableResource)

/**
 * The set of bundled images a user can choose as their Home backdrop. Each [BackdropOption.id]
 * is a stable string safe to persist; [resFor] resolves it back to a drawable. Only full-bleed
 * photographic assets are listed here (figure/logo PNGs are intentionally excluded).
 */
object HomeBackdropCatalog {
    val options: List<BackdropOption> = listOf(
        BackdropOption("hero_dark_1", "Midnight Gold", Res.drawable.hero_dark_1),
        BackdropOption("hero_dark_2", "Nightfall", Res.drawable.hero_dark_2),
        BackdropOption("hero_light_1", "First Light", Res.drawable.hero_light_1),
        BackdropOption("hero_light_2", "Daybreak", Res.drawable.hero_light_2),
        BackdropOption("bg_run_dawn", "Dawn Run", Res.drawable.bg_run_dawn),
        BackdropOption("bg_gym_dark", "The Iron Room", Res.drawable.bg_gym_dark),
        BackdropOption("bg_weights", "Heavy Metal", Res.drawable.bg_weights),
        BackdropOption("bg_athlete_female", "In Motion", Res.drawable.bg_athlete_female),
        BackdropOption("bg_athlete_male", "Full Send", Res.drawable.bg_athlete_male),
        BackdropOption("bg_success", "Summit", Res.drawable.bg_success),
        BackdropOption("bg_yoga_calm", "Stillness", Res.drawable.bg_yoga_calm),
        BackdropOption("card_move", "Move", Res.drawable.card_move),
        BackdropOption("card_coach", "Coach", Res.drawable.card_coach),
        BackdropOption("card_fuel", "Fuel", Res.drawable.card_fuel),
        BackdropOption("card_compete", "Compete", Res.drawable.card_compete),
        BackdropOption("card_streak", "Streak", Res.drawable.card_streak),
        BackdropOption("card_yoga", "Flow", Res.drawable.card_yoga),
        BackdropOption("card_hydrate", "Hydrate", Res.drawable.card_hydrate),
        BackdropOption("card_sleep", "Rest", Res.drawable.card_sleep),
        BackdropOption("card_read", "Read", Res.drawable.card_read),
        BackdropOption("card_community", "Together", Res.drawable.card_community)
    )

    fun resFor(id: String?): DrawableResource? =
        if (id == null) null else options.firstOrNull { it.id == id }?.res
}

class BackdropViewModel {
    private val repository = BackdropRepository()
    private val userSettings = UserSettings.getInstance()
    private val json = Json { ignoreUnknownKeys = true }

    private val _selection = MutableStateFlow<Map<String, BackdropChoice>>(emptyMap())
    val selection: StateFlow<Map<String, BackdropChoice>> = _selection.asStateFlow()

    /**
     * The user's explicitly chosen Home backdrop id (null = follow the theme-aware default).
     * Mirrors [UserSettings.selectedHomeBackdropFlow] so the UI recomposes on change.
     */
    val homeBackdropSelection: StateFlow<String?> = userSettings.selectedHomeBackdropFlow

    /** Persists (or clears, when [id] is null) the user's Home backdrop choice. */
    fun selectHomeBackdrop(id: String?) {
        userSettings.selectedHomeBackdrop = id
    }

    /** The id shown as selected when the user hasn't picked one — matches the live Home default. */
    fun defaultHomeBackdropId(): String =
        if (userSettings.isDarkTheme == true) "hero_dark_1" else "hero_light_1"

    companion object {
        // Static surface -> bundled fallback drawable.
        private val FALLBACKS: Map<String, DrawableResource> = mapOf(
            "home" to Res.drawable.hero_dark_1,
            "train" to Res.drawable.bg_weights,
            "community" to Res.drawable.card_community,
            "profile" to Res.drawable.bg_yoga_calm,
            "paywall" to Res.drawable.bg_paywall
        )
    }

    init {
        // Recompute from whatever is cached so the UI has a value immediately.
        recompute(loadCachedBackdrops(), loadCachedConfig())
    }

    /** Fetches remote data if the cache is stale, otherwise recomputes from cache. */
    fun refreshIfStale() {
        val config = loadCachedConfig()
        val ttlMillis = config.cacheTtlHours.toLong() * 60L * 60L * 1000L
        val stale = currentTimeMillis() - userSettings.backdropsLastFetch > ttlMillis
        if (!stale) {
            recompute(loadCachedBackdrops(), config)
            return
        }
        MainScope().launch {
            val fetchedConfig = repository.fetchConfig().getOrDefault(AppConfig())
            val fetchedBackdrops = repository.fetchBackdrops().getOrDefault(emptyList())

            // Persist to local cache.
            userSettings.backdropsConfig = json.encodeToString(fetchedConfig)
            userSettings.backdropsCache = json.encodeToString(fetchedBackdrops)
            userSettings.backdropsLastFetch = currentTimeMillis()

            recompute(fetchedBackdrops, fetchedConfig)
        }
    }

    /** Current choice for [surface], falling back to the theme-aware bundled drawable. */
    fun choiceFor(surface: String): BackdropChoice {
        _selection.value[surface]?.let { return it }
        return BackdropChoice.Bundled(fallbackFor(surface))
    }

    private fun currentTheme(): String = if (userSettings.isDarkTheme == true) "dark" else "light"

    /** Theme-aware bundled fallback — a dark photo under a light theme reads as mud. */
    private fun fallbackFor(surface: String): DrawableResource = when (surface) {
        "home" -> if (currentTheme() == "light") Res.drawable.hero_light_1 else Res.drawable.hero_dark_1
        else -> FALLBACKS[surface] ?: Res.drawable.hero_dark_1
    }

    // ──────────────────────────────────────────────────────────────────
    // Selection algorithm
    // ──────────────────────────────────────────────────────────────────

    private fun recompute(backdrops: List<Backdrop>, config: AppConfig) {
        if (!config.backdropsEnabled) {
            _selection.value = bundledOnly()
            userSettings.backdropsSelection = "{}"
            return
        }

        val theme = if (userSettings.isDarkTheme == true) "dark" else "light"
        val bucket = currentBucket()
        val locale = deviceLocale()
        val seed = if (config.rotationPolicy == "daily") todayDateString() else config.rotationPolicy

        val result = mutableMapOf<String, BackdropChoice>()
        val urlMemo = mutableMapOf<String, String>()

        for (surface in FALLBACKS.keys) {
            val fallback = fallbackFor(surface)
            val candidates = backdrops.filter { b ->
                b.active &&
                    surface in b.surfaces &&
                    (b.theme == "both" || b.theme == theme) &&
                    (b.timeOfDay.isEmpty() || bucket in b.timeOfDay)
            }

            val scored = candidates
                .map { it to localeScore(it, locale) }
                .filter { it.second >= 0 }

            if (scored.isEmpty()) {
                result[surface] = BackdropChoice.Bundled(fallback)
                continue
            }

            val maxScore = scored.maxOf { it.second }
            val top = scored
                .filter { it.second == maxScore }
                .map { it.first }
                .sortedBy { it.id }

            val picked = weightedPick(top, stableHash("$surface|$seed"))
            result[surface] = BackdropChoice.Remote(picked.imageUrl, fallback)
            urlMemo[surface] = picked.imageUrl
        }

        _selection.value = result
        userSettings.backdropsSelection = json.encodeToString(urlMemo)
    }

    private fun bundledOnly(): Map<String, BackdropChoice> =
        FALLBACKS.keys.associateWith { BackdropChoice.Bundled(fallbackFor(it)) }

    private fun localeScore(backdrop: Backdrop, locale: org.awi.fitness.utils.DeviceLocale): Int {
        var score = 0
        score += when {
            locale.region in backdrop.regions -> 2
            backdrop.regions.isEmpty() -> 0
            else -> -1
        }
        score += when {
            locale.language in backdrop.langs -> 1
            backdrop.langs.isEmpty() -> 0
            else -> -1
        }
        return score
    }

    private fun weightedPick(candidates: List<Backdrop>, hash: Int): Backdrop {
        val total = candidates.sumOf { maxOf(1, it.weight) }
        if (total <= 0) return candidates.first()
        val pick = ((hash % total) + total) % total
        var acc = 0
        for (b in candidates) {
            acc += maxOf(1, b.weight)
            if (pick < acc) return b
        }
        return candidates.last()
    }

    /** Deterministic, platform-independent string hash. */
    private fun stableHash(input: String): Int {
        var h = 0
        for (c in input) {
            h = h * 31 + c.code
        }
        return h
    }

    private fun currentBucket(): String {
        val hour = currentInstant()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .hour
        return when (hour) {
            in 5..10 -> "morning"
            in 11..16 -> "day"
            in 17..20 -> "evening"
            else -> "night"
        }
    }

    private fun loadCachedBackdrops(): List<Backdrop> = try {
        json.decodeFromString(userSettings.backdropsCache)
    } catch (_: Exception) {
        emptyList()
    }

    private fun loadCachedConfig(): AppConfig = try {
        userSettings.backdropsConfig?.let { json.decodeFromString<AppConfig>(it) } ?: AppConfig()
    } catch (_: Exception) {
        AppConfig()
    }
}

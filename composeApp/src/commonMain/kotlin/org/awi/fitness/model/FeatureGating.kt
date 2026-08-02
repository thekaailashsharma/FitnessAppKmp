package org.awi.fitness.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Server-driven feature gating (freemium control plane).
 *
 * The entire config is stored as ONE JSON string in Firestore at `config/features` (field `json`),
 * so you can add features, flip free/paid, change daily free limits, or toggle the paywall — all
 * from Firestore with zero app release. Parsed here into [FeatureConfig].
 *
 * Payment-safety principles baked in:
 *  - Premium users (RevenueCat / premiumOverride) ALWAYS pass, independent of this config.
 *  - If the config can't load, we fall back to [DEFAULT_FEATURE_CONFIG] — the app stays usable
 *    (free features work) and paid features stay locked (no accidental revenue give-away), and the
 *    paywall stays DISMISSIBLE (so the app is never an accidental hard-gate → App Store safe).
 *  - Unknown feature keys default to FREE (never block a feature we forgot to configure).
 */

@Serializable
data class FeatureRule(
    val free: Boolean = true,        // is this feature available to free users at all?
    val freeDailyLimit: Int? = null, // if set, free users get this many uses/day, then paywall
)

@Serializable
data class PaywallCfg(
    val dismissible: Boolean = true, // can the user close the paywall and use the app free?
    val showOnLaunch: Boolean = true, // show the paywall once after login (as an upsell)
)

@Serializable
data class FeatureConfig(
    val paywall: PaywallCfg = PaywallCfg(),
    val features: Map<String, FeatureRule> = emptyMap(),
)

/** Result of a gate check. */
sealed class GateResult {
    data object Allowed : GateResult()
    /** Blocked → caller should present the paywall. [reasonKey] is for optional analytics/UI. */
    data class Blocked(val reasonKey: String) : GateResult()
}

/**
 * Canonical feature keys. Keep these in sync with the Firestore `config/features` JSON.
 */
object Feature {
    const val AI_COACH = "ai_coach"                 // AI chat coach
    const val AI_MEAL_SCAN = "ai_meal_scan"         // snap-a-plate AI
    const val AI_WORKOUT_GEN = "ai_workout_gen"     // AI-generated workout plans
    const val AI_MEAL_PLAN_GEN = "ai_meal_plan_gen" // AI-generated meal plans
    const val PREMADE_WORKOUTS = "premade_workouts"
    const val MEAL_PLAN_VIEW = "meal_plan_view"
    const val CHALLENGES_BASIC = "challenges_basic"
    const val CHALLENGES_CUSTOM = "challenges_custom"
    const val COMMUNITY = "community"
    const val BACKDROPS = "backdrops"
    const val FOCUS_MODE = "focus_mode"
}

/**
 * The bundled default config — mirrors what we seed into Firestore. This is the FAIL-SAFE used
 * when the remote config hasn't loaded. Free-feature generous, paid-feature locked, paywall
 * dismissible.
 */
const val DEFAULT_FEATURE_CONFIG_JSON: String = """
{
  "paywall": { "dismissible": true, "showOnLaunch": true },
  "features": {
    "ai_coach":         { "free": true,  "freeDailyLimit": 3 },
    "ai_meal_scan":     { "free": true,  "freeDailyLimit": 1 },
    "ai_workout_gen":   { "free": false },
    "ai_meal_plan_gen": { "free": false },
    "premade_workouts": { "free": true },
    "meal_plan_view":   { "free": true },
    "challenges_basic": { "free": true },
    "challenges_custom":{ "free": false },
    "community":        { "free": true },
    "backdrops":        { "free": false },
    "focus_mode":       { "free": false }
  }
}
"""

private val json = Json { ignoreUnknownKeys = true; isLenient = true }

val DEFAULT_FEATURE_CONFIG: FeatureConfig by lazy { parseFeatureConfig(DEFAULT_FEATURE_CONFIG_JSON) }

/** Parses the config JSON; on any error returns the compiled-in default (never throws). */
fun parseFeatureConfig(raw: String?): FeatureConfig {
    if (raw.isNullOrBlank()) return json.decodeFromString(FeatureConfig.serializer(), DEFAULT_FEATURE_CONFIG_JSON)
    return try {
        json.decodeFromString(FeatureConfig.serializer(), raw)
    } catch (e: Exception) {
        json.decodeFromString(FeatureConfig.serializer(), DEFAULT_FEATURE_CONFIG_JSON)
    }
}

/**
 * The single decision point for "can this user use this feature right now?".
 *
 * @param isPremium  from SubscriptionRepository.checkPremiumAccess() (RevenueCat OR override)
 * @param usedToday  how many times the free user has used this feature today (for daily limits)
 */
object FeatureGate {

    fun evaluate(
        config: FeatureConfig,
        featureKey: String,
        isPremium: Boolean,
        usedToday: Int = 0,
    ): GateResult {
        // Premium always passes — independent of the config (payment safety).
        if (isPremium) return GateResult.Allowed
        // Unknown feature → treat as free (never block something we forgot to configure).
        val rule = config.features[featureKey] ?: return GateResult.Allowed
        if (!rule.free) return GateResult.Blocked("premium_only")
        val limit = rule.freeDailyLimit
        if (limit != null && usedToday >= limit) return GateResult.Blocked("daily_limit")
        return GateResult.Allowed
    }
}

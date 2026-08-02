package org.awi.fitness.repository

import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.DEFAULT_FEATURE_CONFIG
import org.awi.fitness.model.FeatureConfig
import org.awi.fitness.model.FeatureRule
import org.awi.fitness.model.PaywallCfg
import org.awi.fitness.model.parseFeatureConfig
import org.awi.fitness.network.ApiService

// ── Native Firestore value wrappers (so the config is editable field-by-field in the console) ──
@Serializable private data class FgBool(val booleanValue: Boolean? = null)
@Serializable private data class FgInt(val integerValue: String? = null)

@Serializable private data class FgPaywallFields(
    val dismissible: FgBool? = null,
    val showOnLaunch: FgBool? = null,
)
@Serializable private data class FgPaywallMap(val fields: FgPaywallFields? = null)
@Serializable private data class FgPaywall(val mapValue: FgPaywallMap? = null)

@Serializable private data class FgRuleFields(
    val free: FgBool? = null,
    val freeDailyLimit: FgInt? = null,
)
@Serializable private data class FgRuleMap(val fields: FgRuleFields? = null)
@Serializable private data class FgRule(val mapValue: FgRuleMap? = null)

@Serializable private data class FgFeaturesMap(val fields: Map<String, FgRule>? = null)
@Serializable private data class FgFeatures(val mapValue: FgFeaturesMap? = null)

@Serializable private data class FeatureDocFields(
    val paywall: FgPaywall? = null,
    val features: FgFeatures? = null,
)
@Serializable private data class FeatureDocument(val fields: FeatureDocFields? = null)

/**
 * Server-driven freemium control plane. The config lives as NATIVE Firestore fields at
 * `config/features` (nested maps: paywall {dismissible, showOnLaunch} and features {<key>{free,
 * freeDailyLimit}}) — so you can edit any single value directly in the Firestore console.
 *
 * Fetched at login, converted to [FeatureConfig], and cached locally (as our own compact JSON) so
 * [currentConfig] can read it synchronously anywhere. Best-effort: on failure the last cached
 * config (or the bundled default) stays in effect — gating never breaks, premium never leaks.
 */
class FeatureGatingRepository : ApiService() {
    companion object {
        private const val FIRESTORE_PROJECT = "awi-fitness-app"
        private const val CONFIG_DOC_URL =
            "https://firestore.googleapis.com/v1/projects/$FIRESTORE_PROJECT/databases/(default)/documents/config/features"

        private val cacheJson = Json { ignoreUnknownKeys = true }

        /** Synchronous read of the active config from cache (falls back to bundled default). */
        fun currentConfig(): FeatureConfig {
            val raw = UserSettings.getInstance().featureConfigJson
            return if (raw.isBlank()) DEFAULT_FEATURE_CONFIG else parseFeatureConfig(raw)
        }
    }

    /** Fetches the native Firestore config, converts it, and caches it. Best-effort; never throws. */
    suspend fun fetchAndStore() {
        try {
            val settings = UserSettings.getInstance()
            val (doc, status) = get<FeatureDocument>(url = CONFIG_DOC_URL, token = settings.authToken)
            if (!status.isSuccess()) return
            val fields = doc.fields ?: return
            val config = fields.toFeatureConfig()
            // Cache as our own compact JSON so currentConfig() can read it synchronously.
            settings.featureConfigJson = cacheJson.encodeToString(FeatureConfig.serializer(), config)
        } catch (e: Exception) {
            // Non-fatal — cached / default config remains in effect.
        }
    }

    private fun FeatureDocFields.toFeatureConfig(): FeatureConfig {
        val pw = paywall?.mapValue?.fields
        val paywallCfg = PaywallCfg(
            dismissible = pw?.dismissible?.booleanValue ?: true,
            showOnLaunch = pw?.showOnLaunch?.booleanValue ?: true,
        )
        val featureRules = features?.mapValue?.fields?.mapValues { (_, rule) ->
            val rf = rule.mapValue?.fields
            FeatureRule(
                free = rf?.free?.booleanValue ?: true,
                freeDailyLimit = rf?.freeDailyLimit?.integerValue?.toIntOrNull(),
            )
        } ?: emptyMap()
        return FeatureConfig(paywall = paywallCfg, features = featureRules)
    }
}

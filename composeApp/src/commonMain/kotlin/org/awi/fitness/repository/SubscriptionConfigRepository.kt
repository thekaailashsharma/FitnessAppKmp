package org.awi.fitness.repository

import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import org.awi.fitness.data.UserSettings
import org.awi.fitness.network.ApiService

// Firestore read wrappers (values arrive typed: stringValue / booleanValue / integerValue-as-string).
@Serializable
data class SubCfgBoolValue(val booleanValue: Boolean? = null)

@Serializable
data class SubCfgStringValue(val stringValue: String? = null)

@Serializable
data class SubCfgIntValue(val integerValue: String? = null)

@Serializable
data class SubscriptionConfigFields(
    val showRefund: SubCfgBoolValue? = null,
    val showPlanSwitch: SubCfgBoolValue? = null,
    val showPurchaseHistory: SubCfgBoolValue? = null,
    val showInvoices: SubCfgBoolValue? = null,
    val graceHours: SubCfgIntValue? = null,
    val supportUrl: SubCfgStringValue? = null,
)

@Serializable
data class SubscriptionConfigDocument(
    val fields: SubscriptionConfigFields? = null,
)

/**
 * Server-driven subscription-management config, stored at Firestore `config/subscription`.
 * Lets ops toggle button visibility, the offline grace window, and the support URL remotely
 * with zero app release. All fields are optional — anything absent keeps the local default.
 */
class SubscriptionConfigRepository : ApiService() {
    companion object {
        private const val FIRESTORE_PROJECT = "awi-fitness-app"
        private const val CONFIG_DOC_URL =
            "https://firestore.googleapis.com/v1/projects/$FIRESTORE_PROJECT/databases/(default)/documents/config/subscription"
    }

    /** Fetches the remote config and caches it into UserSettings. Best-effort; never throws. */
    suspend fun fetchAndStore() {
        try {
            val settings = UserSettings.getInstance()
            val (doc, status) = get<SubscriptionConfigDocument>(url = CONFIG_DOC_URL, token = settings.authToken)
            if (!status.isSuccess()) return
            val f = doc.fields ?: return
            f.showRefund?.booleanValue?.let { settings.subCfgShowRefund = it }
            f.showPlanSwitch?.booleanValue?.let { settings.subCfgShowPlanSwitch = it }
            f.showPurchaseHistory?.booleanValue?.let { settings.subCfgShowPurchaseHistory = it }
            f.showInvoices?.booleanValue?.let { settings.subCfgShowInvoices = it }
            f.supportUrl?.stringValue?.takeIf { it.isNotBlank() }?.let { settings.subCfgSupportUrl = it }
            f.graceHours?.integerValue?.toIntOrNull()?.takeIf { it in 1..720 }?.let { settings.premiumGraceHours = it }
        } catch (e: Exception) {
            // Non-fatal — cached / default flags remain in effect.
        }
    }
}

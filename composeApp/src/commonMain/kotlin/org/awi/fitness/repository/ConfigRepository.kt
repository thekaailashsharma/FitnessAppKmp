package org.awi.fitness.repository

import kotlinx.serialization.Serializable
import org.awi.fitness.AppConfig
import org.awi.fitness.network.ApiService

@Serializable
data class FirestoreConfigDoc(
    val fields: FirestoreConfigFields? = null
)

@Serializable
data class FirestoreConfigFields(
    val geminiApiKey: FirestoreStringValue? = null
)

@Serializable
data class FirestoreStringValue(
    val stringValue: String? = null
)

class ConfigRepository : ApiService() {

    suspend fun fetchAndApplyConfig() {
        try {
            val token = userSettings.authToken ?: return
            val url = "https://firestore.googleapis.com/v1/projects/${ClientRepository.PROJECT_ID}" +
                    "/databases/(default)/documents/config/app"
            val (response, status) = get<FirestoreConfigDoc>(url, token)
            if (!status.value.toString().startsWith("2")) return

            val geminiKey = response.fields?.geminiApiKey?.stringValue ?: return
            AppConfig.setGeminiApiKey(geminiKey)
        } catch (_: Exception) { }
    }
}

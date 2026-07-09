package org.awi.fitness.repository

import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import org.awi.fitness.data.UserSettings
import org.awi.fitness.network.ApiService

@Serializable
data class FirestoreStringValue(
    val stringValue: String? = null
)

@Serializable
data class ConfigAppFields(
    val geminiApiKey: FirestoreStringValue? = null
)

@Serializable
data class ConfigAppDocument(
    val fields: ConfigAppFields? = null
)

class ConfigRepository : ApiService() {
    companion object {
        private const val FIRESTORE_PROJECT = "awi-fitness-app"
        private const val CONFIG_DOC_URL =
            "https://firestore.googleapis.com/v1/projects/$FIRESTORE_PROJECT/databases/(default)/documents/config/app"
    }

    suspend fun fetchGeminiApiKey(authToken: String?): String? {
        return try {
            val (response, status) = get<ConfigAppDocument>(
                url = CONFIG_DOC_URL,
                token = authToken
            )
            if (status.isSuccess()) {
                response.fields?.geminiApiKey?.stringValue
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchAndStoreGeminiKey() {
        val userSettings = UserSettings.getInstance()
        val key = fetchGeminiApiKey(userSettings.authToken)
        if (!key.isNullOrBlank()) {
            userSettings.geminiApiKey = key
        }
    }
}

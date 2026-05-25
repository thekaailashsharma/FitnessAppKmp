package org.awi.fitness.repository

import org.awi.fitness.AppConfig
import org.awi.fitness.network.ApiService

class ConfigRepository : ApiService() {

    suspend fun fetchAndApplyConfig() {
        try {
            val token = userSettings.authToken ?: return
            val url = "https://firestore.googleapis.com/v1/projects/${ClientRepository.PROJECT_ID}" +
                    "/databases/(default)/documents/config/app"
            val (response, status) = get<Map<String, Any?>>(url, token)
            if (!status.value.toString().startsWith("2")) return

            @Suppress("UNCHECKED_CAST")
            val fields = (response["fields"] as? Map<String, Any?>) ?: return
            @Suppress("UNCHECKED_CAST")
            val geminiKey = ((fields["geminiApiKey"] as? Map<String, Any?>)
                ?.get("stringValue") as? String)
                ?: return

            AppConfig.setGeminiApiKey(geminiKey)
        } catch (_: Exception) { }
    }
}

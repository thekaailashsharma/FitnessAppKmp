package org.awi.fitness.repository

import org.awi.fitness.AppConfig
import org.awi.fitness.network.ApiService

class ConfigRepository : ApiService() {

    suspend fun fetchAndApplyConfig() {
        println("[AppConfig] fetchAndApplyConfig: starting fetch")
        try {
            val token = userSettings.authToken
            if (token == null) {
                println("[AppConfig] fetchAndApplyConfig: no auth token, skipping")
                return
            }
            val url = "https://firestore.googleapis.com/v1/projects/${ClientRepository.PROJECT_ID}" +
                    "/databases/(default)/documents/config/app"
            println("[AppConfig] fetchAndApplyConfig: fetching $url")
            val (response, status) = get<Map<String, Any?>>(url, token)
            println("[AppConfig] fetchAndApplyConfig: status=$status")
            if (!status.value.toString().startsWith("2")) {
                println("[AppConfig] fetchAndApplyConfig: non-2xx response, aborting")
                return
            }

            @Suppress("UNCHECKED_CAST")
            val fields = (response["fields"] as? Map<String, Any?>)
            if (fields == null) {
                println("[AppConfig] fetchAndApplyConfig: no 'fields' in response")
                return
            }
            @Suppress("UNCHECKED_CAST")
            val geminiKey = ((fields["geminiApiKey"] as? Map<String, Any?>)
                ?.get("stringValue") as? String)
            if (geminiKey == null) {
                println("[AppConfig] fetchAndApplyConfig: geminiApiKey field not found")
                return
            }

            println("[AppConfig] fetchAndApplyConfig: got key from Firestore: ${geminiKey.take(10)}...")
            AppConfig.setGeminiApiKey(geminiKey)
            println("[AppConfig] fetchAndApplyConfig: AppConfig.geminiApiKey now = ${AppConfig.geminiApiKey.take(10)}...")
        } catch (e: Exception) {
            println("[AppConfig] fetchAndApplyConfig: exception: ${e.message}")
        }
    }
}

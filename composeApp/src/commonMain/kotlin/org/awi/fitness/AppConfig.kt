package org.awi.fitness

object AppConfig {
    private const val FALLBACK_GEMINI_KEY = "AIzaSyAIrxceccZ5tX880-v9q5rZ5_bMpMsOiYc"

    var geminiApiKey: String = FALLBACK_GEMINI_KEY
        private set

    fun setGeminiApiKey(key: String) {
        if (key.isNotBlank()) {
            println("[AppConfig] setGeminiApiKey: updating key to ${key.take(10)}...")
            geminiApiKey = key
        }
    }
}

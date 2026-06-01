package org.awi.fitness

object AppConfig {
    private const val FALLBACK_GEMINI_KEY = "AIzaSyAk3g6-GfLL8xagbTUVrR2rtchWOoqzSUM"

    var geminiApiKey: String = FALLBACK_GEMINI_KEY
        private set

    fun setGeminiApiKey(key: String) {
        if (key.isNotBlank()) {
            println("[AppConfig] setGeminiApiKey: updating key to ${key.take(10)}...")
            geminiApiKey = key
        }
    }
}

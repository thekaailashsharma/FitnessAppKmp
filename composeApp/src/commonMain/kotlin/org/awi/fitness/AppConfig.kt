package org.awi.fitness

object AppConfig {
    private const val FALLBACK_GEMINI_KEY = "AIzaSyDAAoM5EaGDHMXSqkwgALTJ0hbcnIYbuGc"

    var geminiApiKey: String = FALLBACK_GEMINI_KEY
        private set

    fun setGeminiApiKey(key: String) {
        if (key.isNotBlank()) geminiApiKey = key
    }
}

package org.awi.fitness.voice

/** Text-to-speech: the coach reads a message aloud. */
expect object CoachTts {
    fun isAvailable(): Boolean
    fun speak(text: String)
    fun stop()
}

/** Speech-to-text: user talks, we get text back. Single-shot recognition. */
expect class VoiceInput() {
    fun isAvailable(): Boolean
    /** Begins listening; calls onPartial as text streams (optional) and onFinal once, or onError. */
    fun start(onPartial: (String) -> Unit, onFinal: (String) -> Unit, onError: (String) -> Unit)
    fun stop()
}

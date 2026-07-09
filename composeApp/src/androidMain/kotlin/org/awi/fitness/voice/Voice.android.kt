package org.awi.fitness.voice

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import org.awi.fitness.FitnessApp
import java.util.Locale

/** Text-to-speech backed by [android.speech.tts.TextToSpeech]. */
actual object CoachTts {

    @Volatile
    private var initialized = false
    private var tts: TextToSpeech? = null

    private fun ensureInit() {
        if (tts != null) return
        tts = TextToSpeech(FitnessApp.context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                initialized = true
            } else {
                initialized = false
            }
        }
    }

    actual fun isAvailable(): Boolean {
        ensureInit()
        return initialized
    }

    actual fun speak(text: String) {
        ensureInit()
        if (text.isBlank()) return
        // If not yet initialized, defer once via a short retry — otherwise speak now.
        val engine = tts ?: return
        if (initialized) {
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tajly_coach")
        } else {
            // Best-effort: post a short delayed attempt after init completes.
            Handler(Looper.getMainLooper()).postDelayed({
                if (initialized) engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tajly_coach")
            }, 300)
        }
    }

    actual fun stop() {
        tts?.stop()
    }
}

/** Speech-to-text backed by [android.speech.SpeechRecognizer]. Single-shot. */
actual class VoiceInput actual constructor() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null

    actual fun isAvailable(): Boolean =
        SpeechRecognizer.isRecognitionAvailable(FitnessApp.context)

    actual fun start(
        onPartial: (String) -> Unit,
        onFinal: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        mainHandler.post {
            try {
                if (!SpeechRecognizer.isRecognitionAvailable(FitnessApp.context)) {
                    onError("Speech recognition not available")
                    return@post
                }
                // Release any previous instance before starting a new session.
                recognizer?.destroy()
                val sr = SpeechRecognizer.createSpeechRecognizer(FitnessApp.context)
                recognizer = sr

                sr.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}

                    override fun onError(error: Int) {
                        onError(errorMessage(error))
                    }

                    override fun onResults(results: Bundle?) {
                        val text = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            .orEmpty()
                        onFinal(text)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val text = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                        if (!text.isNullOrEmpty()) onPartial(text)
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }
                sr.startListening(intent)
            } catch (t: Throwable) {
                onError(t.message ?: "Failed to start recognition")
            }
        }
    }

    actual fun stop() {
        mainHandler.post {
            try {
                recognizer?.stopListening()
                recognizer?.destroy()
            } catch (_: Throwable) {
            } finally {
                recognizer = null
            }
        }
    }

    private fun errorMessage(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
        SpeechRecognizer.ERROR_NETWORK -> "Network error"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
        SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
        SpeechRecognizer.ERROR_SERVER -> "Server error"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
        else -> "Recognition error ($error)"
    }
}

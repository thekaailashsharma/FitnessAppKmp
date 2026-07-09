package org.awi.fitness.voice

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.setActive
import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechUtterance
import platform.Foundation.NSError
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognitionResult
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus

// A single retained synthesizer instance so utterances are not deallocated mid-speech.
private val sharedSynthesizer = AVSpeechSynthesizer()

/** Text-to-speech backed by AVFoundation's AVSpeechSynthesizer. */
@OptIn(ExperimentalForeignApi::class)
actual object CoachTts {

    actual fun isAvailable(): Boolean = true

    actual fun speak(text: String) {
        if (text.isBlank()) return
        try {
            val utterance = AVSpeechUtterance(string = text)
            val languageCode = AVSpeechSynthesisVoice.currentLanguageCode()
            val voice = AVSpeechSynthesisVoice.voiceWithLanguage(languageCode)
            if (voice != null) {
                utterance.voice = voice
            }
            sharedSynthesizer.speakUtterance(utterance)
        } catch (_: Throwable) {
            // Speaking should never crash the app.
        }
    }

    actual fun stop() {
        try {
            sharedSynthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        } catch (_: Throwable) {
        }
    }
}

/**
 * Speech-to-text backed by the Speech framework (SFSpeechRecognizer) + AVAudioEngine.
 * Single-shot recognition. Defensive throughout: any failure degrades to onError rather
 * than crashing.
 */
@OptIn(ExperimentalForeignApi::class)
actual class VoiceInput actual constructor() {

    private val recognizer: SFSpeechRecognizer? = try {
        SFSpeechRecognizer()
    } catch (_: Throwable) {
        null
    }

    private val audioEngine = AVAudioEngine()
    private var request: SFSpeechAudioBufferRecognitionRequest? = null
    private var task: SFSpeechRecognitionTask? = null

    actual fun isAvailable(): Boolean {
        val r = recognizer ?: return false
        val authorized = SFSpeechRecognizer.authorizationStatus() ==
            SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized
        return r.available && authorized
    }

    actual fun start(
        onPartial: (String) -> Unit,
        onFinal: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val r = recognizer
        if (r == null) {
            onError("Speech recognizer unavailable")
            return
        }

        // Request authorization, then begin once we know the result.
        SFSpeechRecognizer.requestAuthorization { status ->
            if (status != SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized) {
                onError("Speech recognition not authorized")
                return@requestAuthorization
            }
            beginRecognition(r, onPartial, onFinal, onError)
        }
    }

    private fun beginRecognition(
        r: SFSpeechRecognizer,
        onPartial: (String) -> Unit,
        onFinal: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Tear down any prior session.
            cleanup()

            // Configure the audio session for recording.
            val session = AVAudioSession.sharedInstance()
            memScoped {
                val err = alloc<kotlinx.cinterop.ObjCObjectVar<NSError?>>()
                session.setCategory(
                    AVAudioSessionCategoryRecord,
                    mode = AVAudioSessionModeMeasurement,
                    options = 0u,
                    error = err.ptr
                )
                session.setActive(true, error = err.ptr)
            }

            val recognitionRequest = SFSpeechAudioBufferRecognitionRequest()
            recognitionRequest.shouldReportPartialResults = true
            request = recognitionRequest

            val inputNode = audioEngine.inputNode
            val recordingFormat = inputNode.outputFormatForBus(0u)

            inputNode.installTapOnBus(
                bus = 0u,
                bufferSize = 1024u,
                format = recordingFormat
            ) { buffer, _ ->
                if (buffer != null) {
                    recognitionRequest.appendAudioPCMBuffer(buffer)
                }
            }

            audioEngine.prepare()
            memScoped {
                val err = alloc<kotlinx.cinterop.ObjCObjectVar<NSError?>>()
                val started = audioEngine.startAndReturnError(err.ptr)
                if (!started) {
                    onError(err.value?.localizedDescription ?: "Audio engine failed to start")
                    cleanup()
                    return
                }
            }

            task = r.recognitionTaskWithRequest(recognitionRequest) { result, error ->
                var finished = false
                if (result != null) {
                    val transcript = (result as SFSpeechRecognitionResult)
                        .bestTranscription.formattedString
                    if (result.isFinal()) {
                        finished = true
                        onFinal(transcript)
                    } else {
                        onPartial(transcript)
                    }
                }
                if (error != null) {
                    // If we already delivered a final result, treat the terminal error as
                    // normal completion rather than a failure.
                    if (!finished) {
                        onError((error as NSError).localizedDescription)
                    }
                    finished = true
                }
                if (finished) {
                    cleanup()
                }
            }
        } catch (t: Throwable) {
            onError(t.message ?: "Failed to start recognition")
            cleanup()
        }
    }

    actual fun stop() {
        try {
            request?.endAudio()
            task?.finish()
        } catch (_: Throwable) {
        } finally {
            cleanup()
        }
    }

    private fun cleanup() {
        try {
            if (audioEngine.running) {
                audioEngine.stop()
            }
            audioEngine.inputNode.removeTapOnBus(0u)
        } catch (_: Throwable) {
        }
        try {
            task?.cancel()
        } catch (_: Throwable) {
        }
        task = null
        request = null
    }
}

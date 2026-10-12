// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.app.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dev.openautoai.voice.*
import dev.openautoai.core.i18n.AppLanguage
import java.util.Locale

/** Android speech services may process audio online. No raw audio is recorded or archived by this app. */
internal class AndroidVoiceAudio(
    private val context: Context,
    private val main: Handler
) : VoiceAudio {
    private var tts: TextToSpeech? = null
    private var recognizer: SpeechRecognizer? = null
    private var operation = 0L
    private var ready = false
    private var speechCallback: ((VoiceProblem?) -> Unit)? = null
    private var chunks = emptyList<String>()
    private var chunkIndex = 0
    private var utterance: String? = null

    override fun prepare(complete: (VoiceProblem?) -> Unit): Cancel {
        close()
        val token = ++operation
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            complete(VoiceProblem.PERMISSION)
            return Cancel {}
        }
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            complete(VoiceProblem.RECOGNIZER_UNAVAILABLE)
            return Cancel {}
        }
        tts = TextToSpeech(context) { status -> main.post {
            if (token != operation) return@post
            val engine = tts
            try {
                val problem = if (status != TextToSpeech.SUCCESS || engine == null) VoiceProblem.TTS_UNAVAILABLE
                    else {
                        val store = SpeechSettingsStore(context)
                        store.migrateLegacy(engine.voices, engine.defaultEngine)
                        AndroidSpeechSettings.apply(engine, store.load())
                    }
                if (problem != null || engine == null) {
                    complete(problem ?: VoiceProblem.TTS_UNAVAILABLE)
                } else {
                    engine.setAudioAttributes(AndroidPlaybackFocus.attributes)
                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) = Unit
                        override fun onDone(utteranceId: String?) { main.post {
                            if (utteranceId == null || utteranceId != utterance) return@post
                            utterance = null
                            chunkIndex++
                            if (chunkIndex < chunks.size) speakChunk() else finishSpeech(null)
                        } }
                        @Deprecated("Required by Android's base listener")
                        override fun onError(utteranceId: String?) { speechError(utteranceId) }
                        override fun onError(utteranceId: String?, errorCode: Int) { speechError(utteranceId) }
                        override fun onStop(utteranceId: String?, interrupted: Boolean) { speechError(utteranceId) }
                    })
                    ready = true
                    complete(null)
                }
            } catch (_: Exception) { complete(VoiceProblem.TTS_UNAVAILABLE) }
        } }
        return Cancel { if (operation == token) close() }
    }

    override fun listen(complete: (Recognition) -> Unit): Cancel {
        val token = ++operation
        if (!ready) {
            complete(Recognition.Failed(VoiceProblem.TTS_UNAVAILABLE))
            return Cancel {}
        }
        destroyRecognizer()
        fun finish(result: Recognition) {
            if (token != operation) return
            operation++ // Duplicate or late recognizer callbacks must not start another request.
            destroyRecognizer()
            complete(result)
        }
        try {
            if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                finish(Recognition.Failed(VoiceProblem.PERMISSION))
            } else {
                val speech = SpeechRecognizer.createSpeechRecognizer(context)
                recognizer = speech
                run {
                    speech.setRecognitionListener(object : RecognitionListener {
                        override fun onResults(results: Bundle?) {
                            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                            finish(Recognition.Text(text))
                        }
                        override fun onError(error: Int) { finish(Recognition.Failed(recognitionProblem(error))) }
                        override fun onReadyForSpeech(params: Bundle?) = Unit
                        override fun onBeginningOfSpeech() = Unit
                        override fun onRmsChanged(rmsdB: Float) = Unit
                        override fun onBufferReceived(buffer: ByteArray?) = Unit
                        override fun onEndOfSpeech() = Unit
                        override fun onPartialResults(partialResults: Bundle?) = Unit
                        override fun onEvent(eventType: Int, params: Bundle?) = Unit
                    })
                    speech.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, AppLanguage.locale().toLanguageTag())
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    })
                }
            }
        } catch (_: SecurityException) { finish(Recognition.Failed(VoiceProblem.PERMISSION)) }
        catch (_: Exception) { finish(Recognition.Failed(VoiceProblem.RECOGNITION_FAILED)) }
        return Cancel { if (operation == token) { operation++; destroyRecognizer() } }
    }

    override fun speak(text: String, complete: (VoiceProblem?) -> Unit): Cancel {
        val token = ++operation
        destroyRecognizer()
        speechCallback = complete
        chunks = SpeechChunks.split(text, TextToSpeech.getMaxSpeechInputLength() - 1)
        chunkIndex = 0
        if (!ready || chunks.isEmpty()) finishSpeech(VoiceProblem.TTS_FAILED) else speakChunk()
        return Cancel { if (operation == token) {
            operation++
            utterance = null
            speechCallback = null
            runCatching { tts?.stop() }
        } }
    }

    private fun speakChunk() {
        val id = "openauto-$operation-$chunkIndex"
        utterance = id
        val result = runCatching { tts?.speak(chunks[chunkIndex], TextToSpeech.QUEUE_FLUSH, Bundle(), id) }.getOrNull()
        if (result != TextToSpeech.SUCCESS) finishSpeech(VoiceProblem.TTS_FAILED)
    }

    private fun speechError(id: String?) { main.post {
        if (id != null && id == utterance) finishSpeech(VoiceProblem.TTS_FAILED)
    } }

    private fun finishSpeech(problem: VoiceProblem?) {
        utterance = null
        val callback = speechCallback
        speechCallback = null
        chunks = emptyList()
        callback?.invoke(problem)
    }

    private fun destroyRecognizer() {
        val old = recognizer
        recognizer = null
        runCatching { old?.cancel() }
        runCatching { old?.destroy() }
    }

    override fun close() {
        operation++
        ready = false
        utterance = null
        speechCallback = null
        chunks = emptyList()
        destroyRecognizer()
        val oldTts = tts
        tts = null
        runCatching { oldTts?.stop() }
        runCatching { oldTts?.shutdown() }

    }

    companion object {
        internal fun recognitionProblem(code: Int): VoiceProblem = when (code) {
            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceProblem.NO_INPUT
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> VoiceProblem.PERMISSION
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> VoiceProblem.RECOGNIZER_BUSY
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
            SpeechRecognizer.ERROR_SERVER, SpeechRecognizer.ERROR_SERVER_DISCONNECTED -> VoiceProblem.RECOGNITION_NETWORK
            SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED, SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> VoiceProblem.RECOGNIZER_UNAVAILABLE
            else -> VoiceProblem.RECOGNITION_FAILED
        }
    }
}

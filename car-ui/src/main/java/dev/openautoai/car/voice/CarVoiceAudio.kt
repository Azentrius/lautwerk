// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.car.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.speech.*
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.car.app.CarContext
import dev.openautoai.core.i18n.AppLanguage
import dev.openautoai.voice.*
import java.util.concurrent.ExecutorService

/** The car path only uses the on-device recognizer with external PCM; the phone path uses its mic. */
internal class CarVoiceAudio(
    private val context: Context,
    private val car: CarContext?,
    private val main: Handler,
    private val worker: ExecutorService,
    private val microphoneControl: ExecutorService
) : VoiceAudio {
    private var generation = 0L
    private var tts: TextToSpeech? = null
    private var speech: SpeechRecognizer? = null
    private var capture: Cancel? = null
    private var pipe: Array<ParcelFileDescriptor>? = null
    private var utterance: String? = null
    private var spoken: ((VoiceProblem?) -> Unit)? = null
    private var chunks = emptyList<String>()
    private var chunkIndex = 0

    override fun prepare(complete: (VoiceProblem?) -> Unit): Cancel {
        close()
        val token = ++generation
        // Playback-only diagnostics deliberately require neither a microphone nor a recognizer.
        tts = TextToSpeech(context) { status -> main.post {
            if (token != generation) return@post
            val engine = tts
            try {
                if (status != TextToSpeech.SUCCESS || engine == null || engine.setLanguage(AppLanguage.locale()) < 0) {
                    complete(VoiceProblem.TTS_UNAVAILABLE); return@post
                }
                val local = engine.voices.orEmpty().filter {
                    !it.isNetworkConnectionRequired && it.locale.language == AppLanguage.locale().language &&
                        TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty()
                }.sortedWith(compareByDescending<android.speech.tts.Voice> { it.quality }.thenBy { it.name })
                val selected = engine.voice?.takeIf { it in local } ?: local.firstOrNull()
                if (selected == null || engine.setVoice(selected) != TextToSpeech.SUCCESS) {
                    complete(VoiceProblem.TTS_VOICE_UNAVAILABLE); return@post
                }
                engine.setAudioAttributes(VoiceFocus.attributes)
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) { Log.i(VoiceFocus.TAG, "voice.tts.start") }
                    override fun onDone(id: String?) { main.post {
                        if (id == null || id != utterance) return@post
                        utterance = null
                        chunkIndex++
                        if (chunkIndex < chunks.size) speakChunk() else finishSpeech(null)
                    } }
                    @Deprecated("Required by Android")
                    override fun onError(id: String?) = failed(id)
                    override fun onError(id: String?, code: Int) = failed(id)
                    override fun onStop(id: String?, interrupted: Boolean) = failed(id)
                })
                Log.i(VoiceFocus.TAG, "voice.tts.ready local=true")
                complete(null)
            } catch (_: Exception) { complete(VoiceProblem.TTS_UNAVAILABLE) }
        } }
        return Cancel { if (generation == token) close() }
    }

    override fun listen(complete: (Recognition) -> Unit): Cancel {
        val token = ++generation
        stopRecognition()
        fun finish(result: Recognition) {
            if (token != generation) return
            generation++
            stopRecognition()
            Log.i(VoiceFocus.TAG, "voice.recognition.result success=${result is Recognition.Text}")
            complete(result)
        }
        try {
            if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                throw AudioFailure(VoiceProblem.PERMISSION)
            if (car != null && (Build.VERSION.SDK_INT < 33 || car.carAppApiLevel < 5 ||
                    !SpeechRecognizer.isOnDeviceRecognitionAvailable(context)))
                throw AudioFailure(VoiceProblem.RECOGNIZER_UNAVAILABLE)
            val local = Build.VERSION.SDK_INT >= 31 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
            val recognizer = if (local) SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                else SpeechRecognizer.createSpeechRecognizer(context)
            speech = recognizer
            recognizer.setRecognitionListener(object : FinalSpeechListener(
                recognized = { text ->
                    finish(if (text.isBlank()) Recognition.Failed(VoiceProblem.NO_INPUT) else Recognition.Text(text))
                }, failed = { error ->
                    Log.i(VoiceFocus.TAG, "voice.recognition.error code=$error car=${car != null}")
                    finish(Recognition.Failed(problem(error)))
                }, endOfSpeech = {
                    // This marks a speech boundary, not the end of an external audio session.
                    Log.i(VoiceFocus.TAG, "voice.recognition.endOfSpeech car=${car != null}")
                }, segmented = car != null) {
                override fun onReadyForSpeech(params: Bundle?) { Log.i(VoiceFocus.TAG, "voice.recognition.ready car=${car != null} local=$local") }
                override fun onBeginningOfSpeech() { Log.i(VoiceFocus.TAG, "voice.recognition.beginOfSpeech") }
                override fun onResults(results: Bundle?) {
                    Log.i(VoiceFocus.TAG, "voice.recognition.final kind=legacy")
                    super.onResults(results)
                }
                override fun onSegmentResults(segmentResults: Bundle) {
                    Log.i(VoiceFocus.TAG, "voice.recognition.final kind=segment")
                    super.onSegmentResults(segmentResults)
                }
                override fun onEndOfSegmentedSession() {
                    Log.i(VoiceFocus.TAG, "voice.recognition.segmentedEnd")
                    super.onEndOfSegmentedSession()
                }
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, AppLanguage.locale().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            if (car != null && Build.VERSION.SDK_INT >= 33) {
                val descriptors = ParcelFileDescriptor.createPipe()
                pipe = descriptors
                ExternalSpeechInput.attach(intent, descriptors[0])
                capture = CarMicrophone(car, main, worker, microphoneControl).stream(descriptors[1]) {
                    finish(Recognition.Failed(it))
                }
            }
            recognizer.startListening(intent)
        } catch (error: AudioFailure) { finish(Recognition.Failed(error.problem)) }
        catch (_: SecurityException) { finish(Recognition.Failed(VoiceProblem.PERMISSION)) }
        catch (_: Exception) { finish(Recognition.Failed(VoiceProblem.RECOGNITION_FAILED)) }
        return Cancel { if (token == generation) { generation++; stopRecognition() } }
    }

    override fun speak(text: String, complete: (VoiceProblem?) -> Unit): Cancel {
        val token = ++generation
        stopRecognition()
        spoken = complete
        chunks = SpeechChunks.split(text, TextToSpeech.getMaxSpeechInputLength() - 1)
        chunkIndex = 0
        if (tts == null || chunks.isEmpty()) finishSpeech(VoiceProblem.TTS_FAILED) else speakChunk()
        return Cancel { if (token == generation) {
            generation++; utterance = null; spoken = null; chunks = emptyList()
            runCatching { tts?.stop() }
        } }
    }

    private fun speakChunk() {
        val id = "car-voice-$generation-$chunkIndex"
        utterance = id
        if (runCatching { tts?.speak(chunks[chunkIndex], TextToSpeech.QUEUE_FLUSH, Bundle(), id) }.getOrNull() != TextToSpeech.SUCCESS)
            finishSpeech(VoiceProblem.TTS_FAILED)
    }
    private fun failed(id: String?) { main.post { if (id != null && id == utterance) finishSpeech(VoiceProblem.TTS_FAILED) } }
    private fun finishSpeech(problem: VoiceProblem?) {
        utterance = null
        val callback = spoken; spoken = null
        chunks = emptyList()
        Log.i(VoiceFocus.TAG, "voice.tts.end success=${problem == null}")
        callback?.invoke(problem)
    }
    private fun stopRecognition() {
        capture?.cancel(); capture = null
        val previous = speech; speech = null
        runCatching { previous?.cancel() }; runCatching { previous?.destroy() }
        pipe?.forEach { runCatching { it.close() } }; pipe = null
    }
    override fun close() {
        generation++
        utterance = null; spoken = null; chunks = emptyList()
        stopRecognition()
        val old = tts; tts = null
        runCatching { old?.stop() }; runCatching { old?.shutdown() }
    }
    companion object {
        fun problem(code: Int): VoiceProblem = when (code) {
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

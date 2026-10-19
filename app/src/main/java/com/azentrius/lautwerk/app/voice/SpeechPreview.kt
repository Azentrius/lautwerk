// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.app.voice

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.azentrius.lautwerk.core.i18n.AppLanguage
import com.azentrius.lautwerk.core.i18n.Messages
import com.azentrius.lautwerk.voice.Cancel
import com.azentrius.lautwerk.voice.VoiceProblem

/** Fixed-text preview through the phone's configured Android speech service. */
internal class SpeechPreview(private val context: Context, private val store: SpeechSettingsStore) {
    var voices by mutableStateOf<List<Voice>>(emptyList()); private set
    var settings by mutableStateOf(store.load()); private set
    var ready by mutableStateOf(false); private set
    var playing by mutableStateOf(false); private set
    var notice by mutableStateOf<String?>(null); private set
    private val main = Handler(Looper.getMainLooper())
    private val focus = AndroidPlaybackFocus(context, main)
    private var engine: TextToSpeech? = null
    private var lease: Cancel? = null
    private var generation = 0L
    private var sequence = 0L
    private var utterance: String? = null
    private var timeout: Runnable? = null
    private var initTimeout: Runnable? = null

    fun open() {
        close()
        settings = store.load()
        notice = null
        val token = generation
        initTimeout = Runnable {
            if (token == generation) failedConnection(Messages.PREVIEW_TIMEOUT_INIT.text())
        }.also { main.postDelayed(it, 12_000) }
        try {
            engine = TextToSpeech(context, { status -> main.post {
                if (token != generation || initTimeout == null) return@post
                initTimeout?.let(main::removeCallbacks); initTimeout = null
                val current = engine
                try {
                    if (status != TextToSpeech.SUCCESS || current == null ||
                        current.setLanguage(AppLanguage.locale()) < TextToSpeech.LANG_AVAILABLE) {
                        failedConnection(VoiceProblem.TTS_UNAVAILABLE.message)
                    } else {
                        current.setAudioAttributes(AndroidPlaybackFocus.attributes)
                        store.migrateLegacy(current.voices, current.defaultEngine)
                        settings = store.load()
                        voices = AndroidSpeechSettings.available(current.voices)
                        current.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(id: String?) = Unit
                            override fun onDone(id: String?) = finished(token, id, null)
                            @Deprecated("Required by Android's base listener")
                            override fun onError(id: String?) = finished(token, id, Messages.PREVIEW_FAILED.text())
                            override fun onError(id: String?, code: Int) = finished(token, id, Messages.PREVIEW_FAILED.text())
                            override fun onStop(id: String?, interrupted: Boolean) = finished(token, id, null)
                        })
                        ready = true
                    }
                } catch (_: Exception) { failedConnection(VoiceProblem.TTS_UNAVAILABLE.message) }
            } })
        } catch (_: Exception) { failedConnection(VoiceProblem.TTS_UNAVAILABLE.message) }
    }

    private fun failedConnection(message: String) {
        initTimeout?.let(main::removeCallbacks); initTimeout = null
        ready = false; notice = message
        val old = engine; engine = null; runCatching { old?.shutdown() }
    }

    fun choose(voice: Voice?) {
        stop()
        if (!ready || (voice != null && voice !in voices)) return
        store.choose(if (voice == null) null else engine?.defaultEngine, voice?.name)
        settings = store.load(); notice = Messages.VOICE_SAVED.text()
    }

    fun rate(value: Float) {
        stop(); store.rate(value); settings = store.load(); notice = Messages.RATE_SAVED.text()
    }

    fun play() {
        stop()
        notice = null
        val current = engine
        if (current == null || !ready) {
            notice = VoiceProblem.TTS_UNAVAILABLE.message
            return
        }
        try {
            val problem = AndroidSpeechSettings.apply(current, settings)
            if (problem != null) { notice = problem.message; return }
            lease = focus.request { stop(); notice = VoiceProblem.AUDIO_FOCUS.message }
            if (lease == null) { notice = VoiceProblem.AUDIO_FOCUS.message; return }
            val id = "preview-${generation}-${++sequence}"
            utterance = id; playing = true
            timeout = Runnable { if (utterance == id) { stop(); notice = Messages.PREVIEW_TIMEOUT.text() } }
                .also { main.postDelayed(it, 20_000) }
            if (current.speak(Messages.PREVIEW_SENTENCE.text(), TextToSpeech.QUEUE_FLUSH, Bundle(), id) != TextToSpeech.SUCCESS) {
                stop(); notice = Messages.PREVIEW_START_FAILED.text()
            }
        } catch (_: Exception) { stop(); notice = Messages.PREVIEW_START_FAILED.text() }
    }

    private fun finished(token: Long, id: String?, message: String?) { main.post {
        if (generation == token && id != null && id == utterance) { stop(); notice = message }
    } }

    fun stop() {
        utterance = null; playing = false
        timeout?.let(main::removeCallbacks); timeout = null
        runCatching { engine?.stop() }
        val old = lease; lease = null; old?.cancel()
    }

    fun close() {
        generation++
        stop()
        initTimeout?.let(main::removeCallbacks); initTimeout = null
        ready = false; voices = emptyList()
        val old = engine; engine = null; runCatching { old?.shutdown() }
    }
}

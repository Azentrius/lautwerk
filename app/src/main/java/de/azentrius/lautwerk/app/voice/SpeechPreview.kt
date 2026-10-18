// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.app.voice

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TtsSpan
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.azentrius.lautwerk.core.i18n.AppLanguage
import de.azentrius.lautwerk.core.i18n.Messages
import de.azentrius.lautwerk.speech.PocketAlba
import de.azentrius.lautwerk.voice.Cancel
import de.azentrius.lautwerk.voice.VoiceProblem

/** Fixed-text preview only. Each explicitly selected engine owns its own connection. */
internal class SpeechPreview(private val context: Context, private val store: SpeechSettingsStore) {
    var voices by mutableStateOf<List<Voice>>(emptyList()); private set
    var settings by mutableStateOf(store.load()); private set
    var androidReady by mutableStateOf(false); private set
    var pocketReady by mutableStateOf(false); private set
    val ready get() = androidReady || pocketReady
    var pocketNotice by mutableStateOf<String?>(null); private set
    var playing by mutableStateOf(false); private set
    var notice by mutableStateOf<String?>(null); private set
    private val main = Handler(Looper.getMainLooper())
    private val focus = AndroidPlaybackFocus(context, main)
    private var engine: TextToSpeech? = null
    private var pocket: TextToSpeech? = null
    private var lease: Cancel? = null
    private var generation = 0L
    private var sequence = 0L
    private var utterance: String? = null
    private var timeout: Runnable? = null
    private val initTimeouts = mutableMapOf<Boolean, Runnable>()

    fun open() {
        close()
        settings = store.load()
        notice = null
        pocketNotice = PocketAlba.installationProblem(context)?.message
        connect(false)
        if (pocketNotice == null) connect(true)
    }

    private fun connect(isPocket: Boolean) {
        val token = generation
        initTimeouts[isPocket] = Runnable {
            if (token == generation) failedConnection(isPocket, Messages.PREVIEW_TIMEOUT_INIT.text())
        }.also { main.postDelayed(it, 12_000) }
        try {
            val connection = TextToSpeech(context, { status -> main.post {
                if (token != generation || isPocket !in initTimeouts) return@post
                initTimeouts.remove(isPocket)?.let(main::removeCallbacks)
                val current = if (isPocket) pocket else engine
                try {
                    val problem = when {
                        status != TextToSpeech.SUCCESS || current == null -> VoiceProblem.TTS_UNAVAILABLE
                        isPocket -> PocketAlba.apply(current, PocketAlba.settings())
                        current.setLanguage(AppLanguage.locale()) < TextToSpeech.LANG_AVAILABLE -> VoiceProblem.TTS_UNAVAILABLE
                        else -> null
                    }
                    if (problem != null || current == null) {
                        failedConnection(isPocket, (problem ?: VoiceProblem.TTS_UNAVAILABLE).message)
                    } else {
                        current.setAudioAttributes(AndroidPlaybackFocus.attributes)
                        if (!isPocket) {
                            store.migrateLegacy(current.voices, current.defaultEngine)
                            settings = store.load()
                            voices = AndroidSpeechSettings.available(current.voices)
                        }
                        current.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(id: String?) = Unit
                            override fun onDone(id: String?) = finished(token, id, null)
                            @Deprecated("Required by Android's base listener")
                            override fun onError(id: String?) = finished(token, id, Messages.PREVIEW_FAILED.text())
                            override fun onError(id: String?, code: Int) = finished(token, id, Messages.PREVIEW_FAILED.text())
                            override fun onStop(id: String?, interrupted: Boolean) = finished(token, id, null)
                        })
                        if (isPocket) pocketReady = true else androidReady = true
                    }
                } catch (_: Exception) { failedConnection(isPocket, VoiceProblem.TTS_UNAVAILABLE.message) }
            } }, if (isPocket) PocketAlba.ENGINE else null)
            if (isPocket) pocket = connection else engine = connection
        } catch (_: Exception) { failedConnection(isPocket, VoiceProblem.TTS_UNAVAILABLE.message) }
    }

    private fun failedConnection(isPocket: Boolean, message: String) {
        initTimeouts.remove(isPocket)?.let(main::removeCallbacks)
        if (isPocket) {
            pocketReady = false; pocketNotice = message
            val old = pocket; pocket = null; runCatching { old?.shutdown() }
        } else {
            androidReady = false; notice = message
            val old = engine; engine = null; runCatching { old?.shutdown() }
        }
    }

    fun choose(voice: Voice?) {
        stop()
        if (!androidReady || (voice != null && voice !in voices)) return
        store.choose(if (voice == null) null else engine?.defaultEngine, voice?.name)
        settings = store.load(); notice = Messages.VOICE_SAVED.text()
    }

    fun choosePocket() {
        stop()
        if (!pocketReady) return
        val problem = PocketAlba.installationProblem(context)
        if (problem != null) { pocketNotice = problem.message; return }
        store.choose(PocketAlba.ENGINE, PocketAlba.VOICE)
        settings = store.load(); notice = Messages.VOICE_SAVED.text()
    }

    fun rate(value: Float) {
        if (PocketAlba.selected(settings)) return
        stop(); store.rate(value); settings = store.load(); notice = Messages.RATE_SAVED.text()
    }

    fun play() {
        stop()
        val isPocket = PocketAlba.selected(settings)
        notice = null
        if (isPocket) PocketAlba.installationProblem(context)?.let { notice = it.message; return }
        val current = if (isPocket) pocket else engine
        if (current == null || !(if (isPocket) pocketReady else androidReady)) {
            notice = if (isPocket) pocketNotice ?: VoiceProblem.POCKET_VOICE_UNAVAILABLE.message else VoiceProblem.TTS_UNAVAILABLE.message
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
            // Pocket converts CharSequence to String and cannot honor Android TtsSpan hints.
            val text = if (isPocket) pocketPreviewText(Messages.PREVIEW_SENTENCE.text())
                else previewSpeechText(Messages.PREVIEW_SENTENCE.text())
            if (current.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), id) != TextToSpeech.SUCCESS) {
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
        runCatching { engine?.stop() }; runCatching { pocket?.stop() }
        val old = lease; lease = null; old?.cancel()
    }

    fun close() {
        generation++
        stop()
        initTimeouts.values.forEach(main::removeCallbacks); initTimeouts.clear()
        androidReady = false; pocketReady = false; voices = emptyList()
        val old = engine; engine = null; runCatching { old?.shutdown() }
        val oldPocket = pocket; pocket = null; runCatching { oldPocket?.shutdown() }
    }
}

/** Only the fixed preview; never rewrites a provider response or archived message. */
internal fun pocketPreviewText(sentence: String): String = sentence.replace("OpenAuto AI", "OpenAuto A I")

/** Spell the brand suffix in the fixed sample without rewriting the localized sentence. */
internal fun previewSpeechText(sentence: String): CharSequence {
    val name = "OpenAuto AI"
    val nameStart = sentence.indexOf(name)
    if (nameStart < 0) return sentence
    val suffixStart = nameStart + name.length - 2
    return SpannableString(sentence).apply {
        setSpan(TtsSpan.VerbatimBuilder("AI").build(), suffixStart, suffixStart + 2,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

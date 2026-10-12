package dev.openautoai.app.voice

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
import dev.openautoai.voice.Cancel
import dev.openautoai.voice.VoiceProblem
import java.util.Locale

/** Fixed-text preview only: no microphone, provider, credentials or chat access. Main thread only. */
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

    fun open() {
        close()
        settings = store.load()
        notice = null
        val token = generation
        timeout = Runnable { if (token == generation && !ready) { close(); notice = "Sprachausgabe antwortet nicht. Bitte erneut öffnen." } }
            .also { main.postDelayed(it, 12_000) }
        try {
            engine = TextToSpeech(context) { status -> main.post {
                if (token != generation) return@post
                timeout?.let(main::removeCallbacks); timeout = null
                val current = engine
                try {
                    if (status != TextToSpeech.SUCCESS || current == null || current.setLanguage(Locale.getDefault()) < TextToSpeech.LANG_AVAILABLE) {
                        close(); notice = VoiceProblem.TTS_UNAVAILABLE.message
                    } else {
                        current.setAudioAttributes(AndroidPlaybackFocus.attributes)
                        voices = AndroidSpeechSettings.available(current.voices)
                        current.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) = Unit
                            override fun onDone(utteranceId: String?) = finished(token, utteranceId, null)
                            @Deprecated("Required by Android's base listener")
                            override fun onError(utteranceId: String?) = finished(token, utteranceId, "Hörprobe fehlgeschlagen. Bitte eine andere Stimme versuchen.")
                            override fun onError(utteranceId: String?, errorCode: Int) = finished(token, utteranceId, "Hörprobe fehlgeschlagen. Bitte eine andere Stimme versuchen.")
                            override fun onStop(utteranceId: String?, interrupted: Boolean) = finished(token, utteranceId, null)
                        })
                        ready = true
                    }
                } catch (_: Exception) { close(); notice = VoiceProblem.TTS_UNAVAILABLE.message }
            } }
        } catch (_: Exception) { close(); notice = VoiceProblem.TTS_UNAVAILABLE.message }
    }

    fun choose(voice: Voice?) {
        stop()
        if (!ready || (voice != null && voice !in voices)) return
        store.choose(if (voice == null) null else engine?.defaultEngine, voice?.name)
        settings = store.load()
        notice = "Stimme gespeichert – gilt für das nächste Gespräch mit OpenAI und Claude."
    }

    fun rate(value: Float) { stop(); store.rate(value); settings = store.load(); notice = "Sprechtempo gespeichert." }

    fun play() {
        stop()
        val current = engine ?: return
        if (!ready) return
        notice = null
        try {
            val problem = AndroidSpeechSettings.apply(current, settings)
            if (problem != null) { notice = problem.message; return }
            lease = focus.request { stop(); notice = VoiceProblem.AUDIO_FOCUS.message }
            if (lease == null) { notice = VoiceProblem.AUDIO_FOCUS.message; return }
            val id = "preview-${generation}-${++sequence}"
            utterance = id
            playing = true
            timeout = Runnable { if (utterance == id) { stop(); notice = "Die Hörprobe hat zu lange gedauert." } }
                .also { main.postDelayed(it, 20_000) }
            if (current.speak("Hallo, ich bin OpenAuto AI. Mit dieser Stimme begleite ich dein nächstes Gespräch. Wie klingt das für dich?",
                    TextToSpeech.QUEUE_FLUSH, Bundle(), id) != TextToSpeech.SUCCESS) {
                stop(); notice = "Hörprobe konnte nicht starten."
            }
        } catch (_: Exception) { stop(); notice = "Hörprobe konnte nicht starten." }
    }

    private fun finished(token: Long, id: String?, message: String?) { main.post {
        if (generation == token && id != null && id == utterance) { stop(); notice = message }
    } }

    fun stop() {
        utterance = null
        playing = false
        timeout?.let(main::removeCallbacks); timeout = null
        runCatching { engine?.stop() }
        val old = lease; lease = null; old?.cancel()
    }

    fun close() {
        generation++
        stop()
        ready = false
        voices = emptyList()
        val old = engine; engine = null
        runCatching { old?.shutdown() }
    }
}

package dev.openautoai.app.voice

import android.content.Context
import android.content.SharedPreferences
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import dev.openautoai.voice.VoiceProblem
import java.util.Locale

internal data class SpeechSettings(val engine: String? = null, val voice: String? = null, val rate: Float = 1f)

/** Non-secret preferences, separate from credentials, chat data and system-wide TTS settings. */
internal class SpeechSettingsStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(context.getSharedPreferences("speech_output", Context.MODE_PRIVATE))
    fun load(): SpeechSettings = SpeechSettings(
        runCatching { prefs.getString("engine", null) }.getOrNull(),
        runCatching { prefs.getString("voice", null) }.getOrNull(),
        safeRate(runCatching { prefs.getFloat("rate", 1f) }.getOrDefault(1f)))
    fun choose(engine: String?, voice: String?) {
        prefs.edit().putString("engine", engine).putString("voice", voice).apply()
    }
    fun rate(value: Float) { prefs.edit().putFloat("rate", safeRate(value)).apply() }
    companion object {
        private fun safeRate(value: Float) = if (value.isFinite()) value.coerceIn(0.8f, 1.2f) else 1f
    }
}

internal object AndroidSpeechSettings {
    fun available(voices: Set<Voice>?, locale: Locale = Locale.getDefault()): List<Voice> =
        voices.orEmpty().filter {
            it.locale.language == locale.language && TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty()
        }.sortedWith(compareBy<Voice> { it.isNetworkConnectionRequired }
            .thenBy { it.locale.country != locale.country }.thenBy { it.name })

    fun apply(engine: TextToSpeech, settings: SpeechSettings, locale: Locale = Locale.getDefault()): VoiceProblem? {
        if (engine.setLanguage(locale) < TextToSpeech.LANG_AVAILABLE) return VoiceProblem.TTS_UNAVAILABLE
        if (settings.voice != null) {
            // Engine IDs are part of the preference; do not silently substitute after an engine change.
            if (settings.engine != engine.defaultEngine) return VoiceProblem.TTS_VOICE_UNAVAILABLE
            val voice = available(engine.voices, locale).find { it.name == settings.voice }
                ?: return VoiceProblem.TTS_VOICE_UNAVAILABLE
            if (engine.setVoice(voice) != TextToSpeech.SUCCESS) return VoiceProblem.TTS_VOICE_UNAVAILABLE
        }
        return if (engine.setSpeechRate(settings.rate) == TextToSpeech.SUCCESS) null else VoiceProblem.TTS_FAILED
    }
}

// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.speech

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import dev.openautoai.core.i18n.AppLanguage
import dev.openautoai.voice.VoiceProblem
import java.util.Locale

object AndroidSpeechSettings {
    fun available(voices: Set<Voice>?, locale: Locale = AppLanguage.locale()): List<Voice> =
        voices.orEmpty().filter {
            it.locale.language == locale.language && TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty()
        }.sortedWith(compareBy<Voice> { it.isNetworkConnectionRequired }
            .thenBy { it.locale.country != locale.country }.thenBy { it.name })

    fun apply(engine: TextToSpeech, settings: SpeechSettings, locale: Locale = AppLanguage.locale()): VoiceProblem? {
        if (PocketAlba.selected(settings)) return PocketAlba.apply(engine, settings, locale)
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

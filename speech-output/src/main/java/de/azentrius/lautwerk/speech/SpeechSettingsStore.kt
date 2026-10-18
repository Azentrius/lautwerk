// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.speech

import android.content.Context
import android.content.SharedPreferences
import android.speech.tts.Voice
import de.azentrius.lautwerk.core.i18n.AppLanguage
import java.util.Locale

data class SpeechSettings(val engine: String? = null, val voice: String? = null, val rate: Float = 1f)

/** Non-secret preferences, separate from credentials, chat data and system-wide TTS settings. */
class SpeechSettingsStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(context.getSharedPreferences("speech_output", Context.MODE_PRIVATE))
    fun load(locale: Locale = AppLanguage.locale()): SpeechSettings {
        val suffix = if (prefs.getBoolean("voices_migrated", false)) "_${locale.language}" else ""
        return SpeechSettings(
            runCatching { prefs.getString("engine$suffix", null) }.getOrNull(),
            runCatching { prefs.getString("voice$suffix", null) }.getOrNull(),
            safeRate(runCatching { prefs.getFloat("rate", 1f) }.getOrDefault(1f)))
    }
    fun migrateLegacy(voices: Set<Voice>?, engine: String) {
        if (prefs.getBoolean("voices_migrated", false)) return
        val old = load()
        if (old.voice == null) { prefs.edit().putBoolean("voices_migrated", true).apply(); return }
        if (old.engine != engine) return // Preserve the existing explicit missing-engine error.
        val voice = voices.orEmpty().find { it.name == old.voice } ?: return
        prefs.edit().putString("engine_${voice.locale.language}", old.engine)
            .putString("voice_${voice.locale.language}", old.voice).putBoolean("voices_migrated", true).apply()
    }
    fun choose(engine: String?, voice: String?, locale: Locale = AppLanguage.locale()) {
        prefs.edit().putString("engine_${locale.language}", engine).putString("voice_${locale.language}", voice)
            .putBoolean("voices_migrated", true).apply()
    }
    fun rate(value: Float) { prefs.edit().putFloat("rate", safeRate(value)).apply() }
    companion object {
        private fun safeRate(value: Float) = if (value.isFinite()) value.coerceIn(0.8f, 1.2f) else 1f
    }
}


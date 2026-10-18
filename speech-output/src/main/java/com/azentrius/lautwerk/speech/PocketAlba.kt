// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.speech

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.azentrius.lautwerk.core.i18n.AppLanguage
import com.azentrius.lautwerk.voice.VoiceProblem
import java.util.Locale

/** Only the separately installed cancellation-fixed engine; never the broken upstream 0.5.3. */
object PocketAlba {
    const val ENGINE = "org.pockettts.android.engine.openautotest"
    const val VOICE = "german-fp32::alba-mackenna-casual"
    const val VERSION = "0.5.3-openauto-cancel.1"
    fun selected(settings: SpeechSettings) = settings.engine == ENGINE
    fun settings(rate: Float = 1f) = SpeechSettings(ENGINE, VOICE, rate)

    fun installationProblem(context: Context, locale: Locale = AppLanguage.locale()): VoiceProblem? {
        if (locale.language != Locale.GERMAN.language) return VoiceProblem.POCKET_LANGUAGE
        return try {
            @Suppress("DEPRECATION")
            val info = context.packageManager.getPackageInfo(ENGINE, PackageManager.GET_PERMISSIONS)
            // This integration is tied to the evaluated companion build. Future versions need review.
            if (info.versionName != VERSION || info.longVersionCode != 24L ||
                info.applicationInfo?.enabled != true ||
                Manifest.permission.INTERNET in info.requestedPermissions.orEmpty()) VoiceProblem.POCKET_UNAVAILABLE
            else null
        } catch (_: PackageManager.NameNotFoundException) { VoiceProblem.POCKET_UNAVAILABLE }
          catch (_: RuntimeException) { VoiceProblem.POCKET_UNAVAILABLE }
    }

    fun voice(voices: Set<Voice>?): Voice? = voices.orEmpty().firstOrNull {
        it.name == VOICE && it.locale.language == "de" && !it.isNetworkConnectionRequired &&
            TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty()
    }

    fun apply(engine: TextToSpeech, settings: SpeechSettings, locale: Locale = AppLanguage.locale()): VoiceProblem? {
        if (locale.language != "de") return VoiceProblem.POCKET_LANGUAGE
        if (settings.voice != VOICE) return VoiceProblem.POCKET_VOICE_UNAVAILABLE
        // TextToSpeech may silently fall back during binding. The stable, engine-specific offline
        // voice ID must be present BEFORE any text is sent; the system default is not its identity.
        val alba = voice(engine.voices) ?: return VoiceProblem.POCKET_VOICE_UNAVAILABLE
        if (engine.setVoice(alba) != TextToSpeech.SUCCESS || engine.voice?.name != VOICE)
            return VoiceProblem.POCKET_VOICE_UNAVAILABLE
        // Pocket 0.5.3 ignores SynthesisRequest.speechRate. Preserve the user's Android voice rate.
        return if (engine.setSpeechRate(1f) == TextToSpeech.SUCCESS) null else VoiceProblem.TTS_FAILED
    }
}

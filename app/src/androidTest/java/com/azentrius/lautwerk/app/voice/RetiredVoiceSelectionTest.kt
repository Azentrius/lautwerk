// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.app.voice

import android.content.Context
import android.content.SharedPreferences
import android.speech.tts.Voice
import androidx.test.platform.app.InstrumentationRegistry
import java.util.Locale
import java.util.UUID
import org.junit.Assert.*
import org.junit.Test

/** Migration fixtures only. No speech, microphone, credentials or personal preferences. */
class RetiredVoiceSelectionTest {
    @Test fun retiredLocalizedChoicesReturnToDefaultWithoutChangingPhoneVoiceOrRate() = fixture { prefs ->
        prefs.edit().putBoolean("voices_migrated", true)
            .putString("engine_de", RETIRED).putString("voice_de", "retired-voice")
            .putString("engine_fr", RETIRED).putString("voice_fr", "retired-voice")
            .putString("engine_en", "phone.engine").putString("voice_en", "phone-en")
            .putFloat("rate", 1.1f).putString("unrelated", "preserve").commit()
        val store = SpeechSettingsStore(prefs)
        assertEquals(SpeechSettings(rate = 1.1f), store.load(Locale.GERMAN))
        assertEquals(SpeechSettings(rate = 1.1f), store.load(Locale.FRENCH))
        assertEquals(SpeechSettings("phone.engine", "phone-en", 1.1f), store.load(Locale.ENGLISH))
        assertFalse(prefs.contains("engine_de")); assertFalse(prefs.contains("voice_de"))
        assertEquals("preserve", prefs.getString("unrelated", null))
        val after = prefs.all.toMap()
        SpeechSettingsStore(prefs)
        assertEquals(after, prefs.all)
    }

    @Test fun legacyCompanionChoiceDoesNotBlockLanguageMigrationOrNewPhoneSelection() = fixture { prefs ->
        prefs.edit().putString("engine", RETIRED).putString("voice", "retired-voice")
            .putFloat("rate", .9f).commit()
        val store = SpeechSettingsStore(prefs)
        assertEquals(SpeechSettings(rate = .9f), store.load(Locale.GERMAN))
        assertFalse(prefs.contains("engine")); assertFalse(prefs.contains("voice"))
        store.migrateLegacy(emptySet(), "phone.engine")
        assertTrue(prefs.getBoolean("voices_migrated", false))
        store.choose("phone.engine", "phone-de", Locale.GERMAN)
        assertEquals(SpeechSettings("phone.engine", "phone-de", .9f), SpeechSettingsStore(prefs).load(Locale.GERMAN))
    }

    @Test fun legacyPhoneVoiceSurvivesWhenAnotherLanguageUsedTheCompanion() = fixture { prefs ->
        prefs.edit().putString("engine", "phone.engine").putString("voice", "phone-en")
            .putString("engine_de", RETIRED).putString("voice_de", "retired-voice")
            .putFloat("rate", 1.1f).commit()
        val store = SpeechSettingsStore(prefs)
        val english = Voice("phone-en", Locale.ENGLISH, Voice.QUALITY_NORMAL, Voice.LATENCY_NORMAL, false, emptySet())
        store.migrateLegacy(setOf(english), "phone.engine")
        assertEquals(SpeechSettings("phone.engine", "phone-en", 1.1f), store.load(Locale.ENGLISH))
        assertEquals(SpeechSettings(rate = 1.1f), store.load(Locale.GERMAN))
    }

    @Test fun otherAndroidEnginesAndVoiceNamesAreNotReset() = fixture { prefs ->
        prefs.edit().putBoolean("voices_migrated", true).putString("engine_de", "another.engine")
            .putString("voice_de", "local.voice").putFloat("rate", .9f).commit()
        val before = prefs.all.toMap()
        assertEquals(SpeechSettings("another.engine", "local.voice", .9f), SpeechSettingsStore(prefs).load(Locale.GERMAN))
        assertEquals(before, prefs.all)
    }

    private fun fixture(test: (SharedPreferences) -> Unit) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "retired-voice-${UUID.randomUUID()}"
        try { test(context.getSharedPreferences(name, Context.MODE_PRIVATE)) }
        finally { context.deleteSharedPreferences(name) }
    }

    companion object {
        private const val RETIRED = "org.pockettts.android.engine.openautotest"
    }
}

// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.app.voice

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.test.platform.app.InstrumentationRegistry
import de.azentrius.lautwerk.speech.PocketAlba
import de.azentrius.lautwerk.voice.VoiceProblem
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** No sound or provider calls; only synthetic catalogs and isolated preferences. */
class PocketSelectionTest {
    @Test fun pocketChoiceKeepsAndroidRateAndOtherLanguageChoice() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "pocket-selection-${UUID.randomUUID()}"
        try {
            val store = SpeechSettingsStore(context.getSharedPreferences(name, Context.MODE_PRIVATE))
            store.choose("android.engine", "english.voice", Locale.ENGLISH)
            store.rate(1.1f)
            store.choose(PocketAlba.ENGINE, PocketAlba.VOICE, Locale.GERMAN)
            assertEquals(PocketAlba.settings(1.1f), store.load(Locale.GERMAN))
            assertEquals("english.voice", store.load(Locale.ENGLISH).voice)
            store.choose(null, null, Locale.GERMAN)
            assertEquals(SpeechSettings(rate=1.1f), store.load(Locale.GERMAN))
            assertEquals("english.voice", store.load(Locale.ENGLISH).voice)
        } finally { context.deleteSharedPreferences(name) }
    }

    @Test fun acceptsOnlyTheExactInstalledOfflineGermanAlbaVoice() {
        fun voice(name: String = PocketAlba.VOICE, locale: Locale = Locale.GERMAN,
                  online: Boolean = false, missing: Boolean = false) =
            Voice(name, locale, Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, online,
                if (missing) setOf(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) else emptySet())
        val valid = voice()
        assertEquals(valid, PocketAlba.voice(setOf(valid)))
        for (invalid in listOf(voice("other"), voice(locale=Locale.ENGLISH), voice(online=true), voice(missing=true)))
            assertNull(PocketAlba.voice(setOf(invalid)))
        assertNull(PocketAlba.voice(null))
    }

    @Test fun aFallbackAndroidEngineCannotAcceptThePocketChoice() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        var tts: TextToSpeech? = null
        val ready = CountDownLatch(1)
        var status = TextToSpeech.ERROR
        try {
            instrumentation.runOnMainSync {
                tts = TextToSpeech(instrumentation.targetContext) { result -> Handler(Looper.getMainLooper()).post {
                    status=result; ready.countDown()
                } }
            }
            assertTrue(ready.await(15, TimeUnit.SECONDS)); assertEquals(TextToSpeech.SUCCESS,status)
            instrumentation.runOnMainSync {
                val engine=requireNotNull(tts)
                // If the device default already is Pocket, only the language/identity checks apply.
                if (PocketAlba.voice(engine.voices)==null)
                    assertEquals(VoiceProblem.POCKET_VOICE_UNAVAILABLE, PocketAlba.apply(engine,PocketAlba.settings(),Locale.GERMAN))
                assertEquals(VoiceProblem.POCKET_LANGUAGE, PocketAlba.apply(engine,PocketAlba.settings(),Locale.ENGLISH))
                assertEquals(VoiceProblem.POCKET_VOICE_UNAVAILABLE,
                    PocketAlba.apply(engine,PocketAlba.settings().copy(voice="wrong-voice"),Locale.GERMAN))
                assertFalse(engine.isSpeaking)
            }
        } finally { instrumentation.runOnMainSync { tts?.shutdown() } }
    }

    @Test fun pocketSampleSpellsTheBrandWithoutChangingOtherWords() {
        assertEquals("Hallo, OpenAuto A I. Ein Test.",pocketPreviewText("Hallo, OpenAuto AI. Ein Test."))
        assertEquals("A fixed test sentence.",pocketPreviewText("A fixed test sentence."))
    }
}

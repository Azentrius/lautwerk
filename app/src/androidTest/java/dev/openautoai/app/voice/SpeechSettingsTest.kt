package dev.openautoai.app.voice

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.openautoai.voice.VoiceProblem
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** No microphone, audible preview, personal preferences or provider/API access. */
@RunWith(AndroidJUnit4::class)
class SpeechSettingsTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test fun voiceAndRatePersistIndependentlyAndInvalidRatesAreSafe() {
        val name = "speech-test-${UUID.randomUUID()}"
        val prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        try {
            val store = SpeechSettingsStore(prefs)
            assertEquals(SpeechSettings(), store.load())
            store.choose("test.engine", "test.voice")
            store.rate(0.9f)
            assertEquals(SpeechSettings("test.engine", "test.voice", 0.9f), SpeechSettingsStore(prefs).load())
            store.choose(null, null)
            assertEquals(SpeechSettings(rate = 0.9f), store.load())
            prefs.edit().putFloat("rate", Float.NaN).commit()
            assertEquals(1f, store.load().rate)
            store.rate(500f)
            assertEquals(1.2f, store.load().rate)
        } finally { context.deleteSharedPreferences(name) }
    }

    @Test fun catalogExcludesMissingDataAndOtherLanguagesWithStableLocalFirstOrder() {
        fun voice(name: String, locale: Locale = Locale.GERMANY, online: Boolean = false, missing: Boolean = false) =
            Voice(name, locale, Voice.QUALITY_NORMAL, Voice.LATENCY_NORMAL, online,
                if (missing) setOf(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) else emptySet())
        val all = setOf(voice("network", online = true), voice("z-local"), voice("a-local"),
            voice("missing", missing = true), voice("english", Locale.US))
        assertEquals(listOf("a-local", "z-local", "network"),
            AndroidSpeechSettings.available(all, Locale.GERMANY).map { it.name })
    }

    @Test fun realAndroidEngineAcceptsAvailableLocalChoiceAndRejectsMissingOrChangedEngine() {
        var engine: TextToSpeech? = null
        val initialized = CountDownLatch(1)
        var status = TextToSpeech.ERROR
        try {
            instrumentation.runOnMainSync {
                engine = TextToSpeech(context) { result -> Handler(Looper.getMainLooper()).post {
                    status = result; initialized.countDown()
                } }
            }
            assertTrue("TTS initialization timeout", initialized.await(15, TimeUnit.SECONDS))
            assertEquals(TextToSpeech.SUCCESS, status)
            instrumentation.runOnMainSync {
                val tts = requireNotNull(engine)
                val voices = AndroidSpeechSettings.available(tts.voices)
                val local = requireNotNull(voices.firstOrNull { !it.isNetworkConnectionRequired }) { "No local voice for device language" }
                assertNull(AndroidSpeechSettings.apply(tts, SpeechSettings(tts.defaultEngine, local.name, 0.9f)))
                assertEquals(local.name, tts.voice.name)
                assertFalse(tts.isSpeaking)
                assertEquals(VoiceProblem.TTS_VOICE_UNAVAILABLE,
                    AndroidSpeechSettings.apply(tts, SpeechSettings(tts.defaultEngine, "missing-test-voice")))
                assertEquals(VoiceProblem.TTS_VOICE_UNAVAILABLE,
                    AndroidSpeechSettings.apply(tts, SpeechSettings("different.engine", local.name)))
                assertNull(AndroidSpeechSettings.apply(tts, SpeechSettings()))
                instrumentation.sendStatus(0, Bundle().apply {
                    putString("stream", "\nVoice inventory: ${voices.count { !it.isNetworkConnectionRequired }} local, ${voices.count { it.isNetworkConnectionRequired }} online; locale=${Locale.getDefault().toLanguageTag()}; engine=${tts.defaultEngine}\n" + voices.joinToString("\n") { "${it.name}: network=${it.isNetworkConnectionRequired}, quality=${it.quality}, locale=${it.locale.toLanguageTag()}" } + "\n")
                })
            }
        } finally { instrumentation.runOnMainSync { engine?.shutdown() } }
    }
}

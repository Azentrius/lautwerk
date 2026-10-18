// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.app.voice

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.azentrius.lautwerk.app.SpeechSettingsActivity
import com.azentrius.lautwerk.core.i18n.AppLanguage
import com.azentrius.lautwerk.speech.PocketAlba
import com.azentrius.lautwerk.voice.Cancel
import com.azentrius.lautwerk.voice.PlaybackFocusAudio
import com.azentrius.lautwerk.voice.VoiceProblem
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.util.Locale
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/** Opt-in fixed German audio. Never recognizes speech, contacts providers or edits user preferences. */
class PocketPlaybackTest {
    private val instrumentation=InstrumentationRegistry.getInstrumentation()
    private val target=instrumentation.targetContext
    private fun main(action: ()->Unit) = instrumentation.runOnMainSync(action)

    @Test fun chosenPreviewStopsRestartsAndKeepsTheStoredSelection() = fixture { context, store ->
        val preview=SpeechPreview(context,store)
        try {
            main { preview.open() }
            await { preview.pocketReady && preview.androidReady }
            main { preview.choosePocket(); preview.play() }
            SystemClock.sleep(1500)
            main { preview.stop(); assertFalse(preview.playing); preview.play(); assertTrue(preview.playing) }
            await(15_000) { !preview.playing }
            main { assertNull(preview.notice); assertEquals(PocketAlba.ENGINE,store.load().engine) }
            main { preview.play(); preview.close(); assertFalse(preview.playing) }
            SystemClock.sleep(1500)
            main { assertFalse(preview.playing); assertEquals(PocketAlba.VOICE,store.load().voice) }
        } finally { main { preview.close() } }
    }

    @Test fun conversationOutputCancelsBeforeAudioAndDuringPlaybackThenRestarts() = fixture { context, _ ->
        val handler=Handler(Looper.getMainLooper())
        val audio=PlaybackFocusAudio(AndroidVoiceAudio(context,handler),AndroidPlaybackFocus(context,handler))
        val oldCallbacks=AtomicInteger()
        try {
            val prepared=CountDownLatch(1)
            var problem: VoiceProblem?=VoiceProblem.TTS_UNAVAILABLE
            main { audio.prepare { problem=it; prepared.countDown() } }
            assertTrue(prepared.await(15,TimeUnit.SECONDS)); assertNull(problem)
            for (delay in listOf(50L,1500L,1500L)) {
                var stop: Cancel?=null
                main { stop=audio.speak(LONG) { oldCallbacks.incrementAndGet() } }
                SystemClock.sleep(delay)
                val finished=CountDownLatch(1)
                problem=VoiceProblem.TTS_FAILED
                main { stop!!.cancel(); audio.speak(SHORT) { problem=it; finished.countDown() } }
                assertTrue("New output blocked by cancelled synthesis",finished.await(12,TimeUnit.SECONDS))
                assertNull(problem); assertEquals(0,oldCallbacks.get())
            }
        } finally { main { audio.close() } }
    }

    private fun fixture(test:(Context,SpeechSettingsStore)->Unit) {
        assumeTrue(InstrumentationRegistry.getArguments().getString("pocketAudio")=="true")
        val oldLanguage=AppLanguage.preferences
        val name="pocket-playback-${UUID.randomUUID()}"
        val prefs=target.getSharedPreferences(name,Context.MODE_PRIVATE)
        val context=object:ContextWrapper(target) {
            override fun getSharedPreferences(request:String,mode:Int):SharedPreferences =
                if(request=="speech_output")prefs else super.getSharedPreferences(request,mode)
        }
        val store=SpeechSettingsStore(prefs)
        try {
            AppLanguage.preferences={ listOf(Locale.GERMANY) }
            assertNull("Install the tested Pocket/Alba companion first",PocketAlba.installationProblem(target))
            store.choose(PocketAlba.ENGINE,PocketAlba.VOICE)
            ActivityScenario.launch(SpeechSettingsActivity::class.java).use { test(context,store) }
        } finally { AppLanguage.preferences=oldLanguage; target.deleteSharedPreferences(name) }
    }
    private fun await(timeout:Long=15_000,condition:()->Boolean) {
        val end=SystemClock.elapsedRealtime()+timeout
        while(SystemClock.elapsedRealtime()<end) {
            var satisfied=false
            main { satisfied=condition() }
            if(satisfied)return
            SystemClock.sleep(50)
        }
        fail("Preview did not reach the expected state")
    }
    companion object {
        private const val SHORT="Hallo, hier ist Alba. Der Neustart funktioniert."
        private val LONG="Dies ist eine feste deutsche Testausgabe. Sie wird gleich unterbrochen. " .repeat(10)
    }
}

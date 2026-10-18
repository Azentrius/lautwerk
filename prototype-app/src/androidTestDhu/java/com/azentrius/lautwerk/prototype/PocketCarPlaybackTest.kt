// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.prototype

import android.content.*
import android.os.IBinder
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.azentrius.lautwerk.car.voice.CarVoiceService
import com.azentrius.lautwerk.car.voice.VoiceMode
import com.azentrius.lautwerk.car.voice.VoiceSetupActivity
import com.azentrius.lautwerk.core.i18n.AppLanguage
import com.azentrius.lautwerk.speech.PocketAlba
import com.azentrius.lautwerk.speech.SpeechSettingsStore
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.Closeable
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Opt-in fixed output through the real car service on the phone; no host/mic/provider request. */
class PocketCarPlaybackTest {
    @Test fun pocketOutputStopsDuringPreparationAndPlaybackAndRestarts() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("pocketAudio")=="true")
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val context=instrumentation.targetContext
        val speech=context.getSharedPreferences("speech_output",Context.MODE_PRIVATE)
        val outcomes=context.getSharedPreferences("car_voice_v1",Context.MODE_PRIVATE)
        val savedSpeech=speech.all.toMap()
        val savedOutcomes=outcomes.all.toMap()
        val oldLanguage=AppLanguage.preferences
        val connected=CountDownLatch(1)
        val caller=Any()
        var service:CarVoiceService?=null
        val connection=object:ServiceConnection {
            override fun onServiceDisconnected(name:ComponentName?)=Unit
            override fun onServiceConnected(name:ComponentName?,binder:IBinder?) {
                service=(binder as CarVoiceService.LocalBinder).service
                connected.countDown()
            }
        }
        var bound=false
        try {
            AppLanguage.preferences={listOf(Locale.GERMANY)}
            assertNull(PocketAlba.installationProblem(context))
            SpeechSettingsStore(context).choose(PocketAlba.ENGINE,PocketAlba.VOICE)
            ActivityScenario.launch(VoiceSetupActivity::class.java).use {
                try {
                    bound=context.bindService(Intent(context,CarVoiceService::class.java),connection,Context.BIND_AUTO_CREATE)
                    assertTrue(bound);assertTrue(connected.await(10,TimeUnit.SECONDS))
                    for(delay in listOf(50L,1500L)) {
                        instrumentation.runOnMainSync { service!!.start(caller,null,VoiceMode.PLAYBACK) }
                        SystemClock.sleep(delay)
                        val done=CountDownLatch(1)
                        var observer:Closeable?=null
                        var sawActive=false
                        var successful=false
                        instrumentation.runOnMainSync {
                            service!!.stop();assertFalse(service!!.state.active)
                            observer=service!!.observe { state ->
                                if(state.active)sawActive=true
                                if(sawActive&&!state.active) {
                                    successful=state.status==context.getString(com.azentrius.lautwerk.car.R.string.voice_check_done)
                                    done.countDown()
                                }
                            }
                            service!!.start(caller,null,VoiceMode.PLAYBACK)
                        }
                        try {
                            assertTrue("Car output restart timed out",done.await(20,TimeUnit.SECONDS))
                            assertTrue("Car output did not complete successfully",successful)
                        } finally { instrumentation.runOnMainSync { observer?.close() } }
                    }
                } finally {
                    if(bound)instrumentation.runOnMainSync { service?.release(caller);context.unbindService(connection) }
                }
            }
        } finally {
            restore(speech,savedSpeech);restore(outcomes,savedOutcomes)
            AppLanguage.preferences=oldLanguage
        }
    }
    private fun restore(prefs:SharedPreferences,values:Map<String,*>) {
        val editor=prefs.edit().clear()
        for((key,value) in values)when(value) {
            is String->editor.putString(key,value)
            is Boolean->editor.putBoolean(key,value)
            is Float->editor.putFloat(key,value)
            is Int->editor.putInt(key,value)
            is Long->editor.putLong(key,value)
        }
        check(editor.commit())
    }
}

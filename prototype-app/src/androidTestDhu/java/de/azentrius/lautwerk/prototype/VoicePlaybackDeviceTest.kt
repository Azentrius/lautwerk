// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.prototype

import android.content.*
import android.os.IBinder
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import de.azentrius.lautwerk.car.voice.CarVoiceService
import de.azentrius.lautwerk.car.voice.VoiceMode
import de.azentrius.lautwerk.car.voice.VoiceSetupActivity
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.Closeable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Opt-in: this test plays the fixed output sample aloud. Human audibility is recorded separately. */
class VoicePlaybackDeviceTest {
    @Test fun foregroundServicePlaysTheFixedSampleWithoutMicrophoneOrProvider() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("audioPlayback") == "true")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val completed = CountDownLatch(1)
        var service: CarVoiceService? = null
        var observer: Closeable? = null
        var successful = false
        var sawActive = false
        val caller = Any()
        val connection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) = Unit
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                service = (binder as CarVoiceService.LocalBinder).service
                observer = service!!.observe { state ->
                    if (state.active) sawActive = true
                    if (sawActive && !state.active) {
                        successful = state.status == context.getString(de.azentrius.lautwerk.car.R.string.voice_check_done)
                        completed.countDown()
                    }
                }
                service!!.start(caller, null, VoiceMode.PLAYBACK)
                service!!.start(caller, null, VoiceMode.PLAYBACK)
                assertTrue("Repeated Start must not toggle the active session off", service!!.state.active)
            }
        }
        ActivityScenario.launch(VoiceSetupActivity::class.java).use {
            try {
                assertTrue(context.bindService(Intent(context, CarVoiceService::class.java), connection, Context.BIND_AUTO_CREATE))
                assertTrue("Audio service did not complete", completed.await(30, TimeUnit.SECONDS))
                assertTrue("Audio service reported a fixed error; inspect voice event logs", successful)
            } finally {
                instrumentation.runOnMainSync { service?.release(caller); observer?.close(); context.unbindService(connection) }
            }
        }
    }
}

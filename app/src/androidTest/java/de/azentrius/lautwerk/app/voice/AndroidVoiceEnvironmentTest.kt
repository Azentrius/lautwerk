// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.app.voice

import android.os.Handler
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/** Reads system state without opening mic/TTS, changing routes, locking the phone or placing calls. */
@RunWith(AndroidJUnit4::class)
class AndroidVoiceEnvironmentTest {
    @Test fun realObserverDeliversOnMainAndUnregistersWithoutLateCallbacks() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val observed = CountDownLatch(1)
        val calls = AtomicInteger()
        var cleanup: de.azentrius.lautwerk.voice.Cancel? = null
        instrumentation.runOnMainSync {
            val environment = AndroidVoiceEnvironment(instrumentation.targetContext, Handler(Looper.getMainLooper()))
            environment.current()
            cleanup = environment.observe {
                assertEquals(Looper.getMainLooper(), Looper.myLooper())
                calls.incrementAndGet(); observed.countDown()
            }
        }
        try { assertTrue(observed.await(3, TimeUnit.SECONDS)) }
        finally { instrumentation.runOnMainSync { cleanup!!.cancel(); cleanup!!.cancel() } }
        val stoppedAt = calls.get()
        val waited = CountDownLatch(1)
        Handler(Looper.getMainLooper()).postDelayed({ waited.countDown() }, 1100)
        assertTrue(waited.await(3, TimeUnit.SECONDS))
        assertEquals(stoppedAt, calls.get())
    }
}

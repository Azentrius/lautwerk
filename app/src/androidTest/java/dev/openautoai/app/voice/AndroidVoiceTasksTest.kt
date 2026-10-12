package dev.openautoai.app.voice

import android.os.Handler
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.openautoai.voice.Cancel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/** No microphone, TTS, stored credentials or live API calls. Tests the real Android thread boundary. */
@RunWith(AndroidJUnit4::class)
class AndroidVoiceTasksTest {
    @Test fun blockingWorkIsOffMainAndCompletionReturnsToMain() {
        val worker = Executors.newSingleThreadExecutor()
        val done = CountDownLatch(1)
        val workedOffMain = AtomicBoolean(false)
        val completedOnMain = AtomicBoolean(false)
        try {
            val tasks = AndroidVoiceTasks(worker, Handler(Looper.getMainLooper()))
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                tasks.run({ workedOffMain.set(Looper.myLooper() != Looper.getMainLooper()); "ok" }) {
                    completedOnMain.set(Looper.myLooper() == Looper.getMainLooper() && it.getOrNull() == "ok")
                    done.countDown()
                }
            }
            assertTrue(done.await(5, TimeUnit.SECONDS))
            assertTrue(workedOffMain.get())
            assertTrue(completedOnMain.get())
        } finally { worker.shutdownNow() }
    }

    @Test fun cancelSuppressesCompletionEvenWhenBlockingWorkReturnsLate() {
        val worker = Executors.newSingleThreadExecutor()
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val returned = CountDownLatch(1)
        val called = AtomicBoolean(false)
        var cancellation: Cancel? = null
        try {
            val tasks = AndroidVoiceTasks(worker, Handler(Looper.getMainLooper()))
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                cancellation = tasks.run({
                    entered.countDown()
                    while (release.count > 0) { try { release.await() } catch (_: InterruptedException) { /* Simulate slow network cancellation. */ } }
                    returned.countDown()
                    "late"
                }) { called.set(true) }
            }
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            InstrumentationRegistry.getInstrumentation().runOnMainSync { cancellation!!.cancel() }
            release.countDown()
            assertTrue(returned.await(5, TimeUnit.SECONDS))
            worker.submit {}.get(5, TimeUnit.SECONDS) // Wait until the completion post itself has happened.
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            assertFalse(called.get())
        } finally { release.countDown(); worker.shutdownNow() }
    }

    @Test fun cancellationReleasesBlockedWorkAndAllowsImmediateFreshTask() {
        val worker = Executors.newSingleThreadExecutor()
        val registered = CountDownLatch(1)
        val released = CountDownLatch(1)
        val freshDone = CountDownLatch(1)
        val oldCallback = AtomicBoolean(false)
        var cancellation: Cancel? = null
        try {
            val tasks = AndroidVoiceTasks(worker, Handler(Looper.getMainLooper()))
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                cancellation = tasks.run({ token ->
                    val registration = token.onCancel { released.countDown() }
                    try {
                        registered.countDown()
                        while (released.count > 0) { try { released.await() } catch (_: InterruptedException) {} }
                        token.check()
                    } finally { registration.close() }
                }) { oldCallback.set(true) }
            }
            assertTrue(registered.await(5, TimeUnit.SECONDS))
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                cancellation!!.cancel()
                tasks.run({ it.check(); "fresh" }) {
                    if (it.getOrNull() == "fresh") freshDone.countDown()
                }
                cancellation!!.cancel() // Stale/double Stop must not cancel the new task.
            }
            assertTrue(freshDone.await(5, TimeUnit.SECONDS))
            assertFalse(oldCallback.get())
        } finally { released.countDown(); worker.shutdownNow() }
    }

}

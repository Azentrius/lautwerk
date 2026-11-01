// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.voice

import java.util.concurrent.Executor
import org.junit.Assert.*
import org.junit.Test

class AudioReleaseFenceTest {
    private class Queue : Executor {
        val tasks = ArrayDeque<Runnable>()
        override fun execute(command: Runnable) { tasks.add(command) }
        fun next() { tasks.removeFirst().run() }
    }

    @Test fun acknowledgementFollowsActualHostStopAndCallerThreadDelivery() {
        val control = Queue(); val main = Queue(); val events = mutableListOf<String>()
        val fence = AudioReleaseFence(control, main)
        val capture = SerialAudioCapture(control, Queue()).start({ object : SerialAudioCapture.Device {
            override fun start() { events.add("start") }
            override fun read(cancelled: () -> Boolean) = Unit
            override fun stop() { fence.cleanup { events.add("stop") } }
        } }, { throw AssertionError(it) })
        control.next()
        capture.cancel()
        fence.await { ready -> assertTrue(ready); events.add("released") }
        assertEquals(listOf("start"), events)
        control.next()
        assertEquals(listOf("start", "stop"), events)
        control.next()
        assertEquals(listOf("start", "stop"), events)
        main.next()
        assertEquals(listOf("start", "stop", "released"), events)
    }

    @Test fun failureInAnyEarlierCleanupRemainsUnsafeAcrossAttempts() {
        val control = Queue(); val main = Queue()
        val fence = AudioReleaseFence(control, main)
        control.execute { fence.cleanup { throw IllegalStateException() } }
        fence.await { assertFalse(it) }
        control.next(); control.next(); main.next()
        fence.cleanup {}
        fence.await { assertFalse(it) }
        control.next(); main.next()
    }

    @Test fun cancellationAlsoSuppressesAnAlreadyQueuedCallback() {
        val control = Queue(); val main = Queue()
        val fence = AudioReleaseFence(control, main)
        val waiting = fence.await { fail("Cancelled callback") }
        control.next(); waiting.cancel(); main.next()
    }

    @Test fun rejectedControlTaskFailsClosed() {
        val fence = AudioReleaseFence({ throw java.util.concurrent.RejectedExecutionException() }, { it.run() })
        var result: Boolean? = null
        fence.await { result = it }
        assertEquals(false, result)
    }
}

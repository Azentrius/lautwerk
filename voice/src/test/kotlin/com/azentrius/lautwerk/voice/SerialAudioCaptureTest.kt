// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.voice

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Test

class SerialAudioCaptureTest {
    private class Queue : Executor {
        val tasks = ArrayDeque<Runnable>()
        override fun execute(task: Runnable) { tasks.add(task) }
        fun drain() { while (tasks.isNotEmpty()) tasks.removeFirst().run() }
    }
    private open class Device(private val events: MutableList<String>, private val name: String) : SerialAudioCapture.Device {
        override fun start() { events.add("$name.start") }
        override fun read(cancelled: () -> Boolean) { events.add("$name.read") }
        override fun stop() { events.add("$name.stop") }
    }
    @Test fun cancelReturnsWhileHostStartIsBlockedAndNeverStartsReading() {
        val control = Executors.newSingleThreadExecutor()
        val entered = CountDownLatch(1)
        val unblock = CountDownLatch(1)
        val stopped = CountDownLatch(1)
        val reads = Queue()
        try {
            val session = SerialAudioCapture(control, reads).start({ object : SerialAudioCapture.Device {
                override fun start() { entered.countDown(); check(unblock.await(5, TimeUnit.SECONDS)) }
                override fun read(cancelled: () -> Boolean) { fail("Cancelled start must not capture") }
                override fun stop() { stopped.countDown() }
            } }, { throw AssertionError(it) })
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            // If cancellation waits for host start, this future cannot complete until unblock opens.
            val caller = Executors.newSingleThreadExecutor()
            try { caller.submit { session.cancel() }.get(1, TimeUnit.SECONDS) }
            finally { caller.shutdownNow() }
            assertEquals(1, unblock.count)
            unblock.countDown()
            assertTrue(stopped.await(5, TimeUnit.SECONDS))
            control.submit {}.get(5, TimeUnit.SECONDS)
            assertTrue(reads.tasks.isEmpty())
        } finally { unblock.countDown(); control.shutdownNow() }
    }
    @Test fun oldStopCompletesBeforeNextHostStartAndCancellationIsIdempotent() {
        val control = Queue(); val reader = Queue(); val events = mutableListOf<String>()
        val capture = SerialAudioCapture(control, reader)
        val first = capture.start({ Device(events, "first") }, { throw AssertionError(it) })
        control.drain()
        first.cancel(); first.cancel()
        val second = capture.start({ Device(events, "second") }, { throw AssertionError(it) })
        assertEquals(listOf("first.start"), events) // Cancel never invokes host stop inline.
        control.drain()
        assertEquals(listOf("first.start", "first.stop", "second.start"), events)
        second.cancel()
        reader.drain(); control.drain()
        assertEquals(listOf("first.start", "first.stop", "second.start", "second.stop"), events)
    }
    @Test fun startFailureReleasesPartiallyOpenedDeviceExactlyOnce() {
        val control = Queue(); val events = mutableListOf<String>(); val errors = mutableListOf<Exception>()
        val session = SerialAudioCapture(control, Queue()).start({ object : Device(events, "failed") {
            override fun start() { super.start(); throw IllegalStateException() }
        } }, errors::add)
        control.drain(); session.cancel(); control.drain()
        assertEquals(1, errors.size)
        assertEquals(listOf("failed.start", "failed.stop"), events)
    }
    @Test fun cancelledBeforeControlRunsDoesNotOpenTheHost() {
        val control = Queue()
        val session = SerialAudioCapture(control, Queue()).start({ error("Must not open") }, { throw AssertionError(it) })
        session.cancel(); control.drain()
    }
    @Test fun readFailureReportsOnceAndClosesTheHost() {
        val control = Queue(); val reader = Queue(); val events = mutableListOf<String>(); val errors = mutableListOf<Exception>()
        val session = SerialAudioCapture(control, reader).start({ object : Device(events, "failed") {
            override fun read(cancelled: () -> Boolean) { throw IllegalStateException() }
        } }, errors::add)
        control.drain(); reader.drain(); control.drain(); session.cancel(); control.drain()
        assertEquals(1, errors.size)
        assertEquals(listOf("failed.start", "failed.stop"), events)
    }
}

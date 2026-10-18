// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.voice

import org.junit.Assert.*
import org.junit.Test

class VoiceInterruptionsTest {
    private class Environment : VoiceEnvironment {
        var value = AudioEnvironment(true, false, setOf(10))
        val callbacks = mutableListOf<(AudioEnvironment) -> Unit>()
        var closed = 0
        var whileSubscribing: AudioEnvironment? = null
        override fun current() = value
        override fun observe(changed: (AudioEnvironment) -> Unit): Cancel {
            callbacks += changed
            whileSubscribing?.let { value = it; changed(it) }
            return Cancel { closed++ }
        }
        fun send(next: AudioEnvironment) { value = next; callbacks.last()(next) }
    }

    @Test fun initialDeviceSnapshotAndNormalPollingDoNotInterrupt() {
        val environment = Environment()
        val guard = VoiceInterruptions(environment) { fail("initial snapshot is not a route change") }
        assertNull(guard.start())
        repeat(10) { environment.send(environment.value) }
        guard.close()
        assertEquals(1, environment.closed)
    }

    @Test fun lockedScreenOrOngoingCallBlocksStartBeforeObservation() {
        for (locked in listOf(true, false)) {
            val environment = Environment().apply { value = value.copy(screenAvailable = !locked, communicationBusy = !locked) }
            val guard = VoiceInterruptions(environment) { fail("must return start rejection") }
            assertEquals(if (locked) VoiceInterruption.SCREEN_OFF else VoiceInterruption.COMMUNICATION, guard.start())
            assertTrue(environment.callbacks.isEmpty())
        }
    }

    @Test fun screenCallAndDeviceChangesStopOnceAndNeverAutoResume() {
        val baseline = AudioEnvironment(true, false, setOf(10))
        for ((value, expected) in listOf(
            baseline.copy(screenAvailable = false) to VoiceInterruption.SCREEN_OFF,
            baseline.copy(communicationBusy = true) to VoiceInterruption.COMMUNICATION,
            baseline.copy(externalOutputs = emptySet()) to VoiceInterruption.AUDIO_DEVICE,
            baseline.copy(externalOutputs = setOf(10, 20)) to VoiceInterruption.AUDIO_DEVICE)) {
            val environment = Environment()
            val events = mutableListOf<VoiceInterruption>()
            val guard = VoiceInterruptions(environment) { events += it }
            guard.start(); environment.send(value); environment.send(value); environment.send(baseline)
            assertEquals(listOf(expected), events)
            assertEquals(1, environment.closed)
        }
    }

    @Test fun oldQueuedCallbackCannotInterruptRestartedSession() {
        val environment = Environment()
        val events = mutableListOf<VoiceInterruption>()
        val guard = VoiceInterruptions(environment) { events += it }
        guard.start(); val old = environment.callbacks.last(); guard.close(); guard.start()
        old(environment.value.copy(screenAvailable = false))
        assertTrue(events.isEmpty())
        environment.send(environment.value.copy(communicationBusy = true))
        assertEquals(listOf(VoiceInterruption.COMMUNICATION), events)
    }

    @Test fun interruptionDuringRegistrationRejectsStartAndClosesReturnedHandle() {
        val environment = Environment().apply { whileSubscribing = value.copy(communicationBusy = true) }
        val events = mutableListOf<VoiceInterruption>()
        val guard = VoiceInterruptions(environment) { events += it }
        assertEquals(VoiceInterruption.COMMUNICATION, guard.start())
        assertEquals(listOf(VoiceInterruption.COMMUNICATION), events)
        assertEquals(1, environment.closed)
    }
}

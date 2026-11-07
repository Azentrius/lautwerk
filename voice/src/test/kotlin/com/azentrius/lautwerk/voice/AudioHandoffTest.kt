// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.voice

import org.junit.Assert.*
import org.junit.Test

class AudioHandoffTest {
    private class Fixture {
        var released: ((Boolean) -> Unit)? = null
        var timeout: (() -> Unit)? = null
        var cancellations = 0
        var launches = 0
        val phases = mutableListOf<HandoffPhase>()
        val flow = AudioHandoff({ done -> released = done; Cancel { cancellations++ } },
            VoiceScheduler { _, action -> timeout = action; Cancel {} }, phases::add)
        fun open() = flow.request { launches++; HandoffPhase.REQUESTED }
    }

    @Test fun waitsForAcknowledgementAndNeverReportsExternalSessionSuccess() {
        val f = Fixture()
        f.open(); f.open()
        assertEquals(0, f.launches)
        assertEquals(listOf(HandoffPhase.RELEASING), f.phases)
        f.released!!(true)
        f.released!!(true)
        f.open()
        assertEquals(1, f.launches)
        assertEquals(HandoffPhase.REQUESTED, f.flow.phase)
    }

    @Test fun cleanupFailurePreventsLaunch() {
        val f = Fixture(); f.open(); f.released!!(false)
        assertEquals(0, f.launches)
        assertEquals(HandoffPhase.RELEASE_FAILED, f.flow.phase)
    }

    @Test fun timeoutRejectsLateCompletion() {
        val f = Fixture(); f.open(); f.timeout!!(); f.released!!(true)
        assertEquals(0, f.launches)
        assertEquals(1, f.cancellations)
        assertEquals(HandoffPhase.RELEASE_FAILED, f.flow.phase)
    }

    @Test fun leavingScreenCancelsAndDoesNotAffectANewAttempt() {
        val f = Fixture(); f.open()
        val old = f.released!!
        f.flow.cancel(); f.open(); old(true)
        assertEquals(0, f.launches)
        f.released!!(true)
        assertEquals(1, f.launches)
    }

    @Test fun exceptionsAndKnownDispatchFailuresRemainFailures() {
        for (result in listOf(HandoffPhase.BLOCKED, HandoffPhase.UNAVAILABLE, HandoffPhase.LOCKED)) {
            val flow = AudioHandoff({ done -> done(true); Cancel {} }, VoiceScheduler { _, _ -> Cancel {} }, {})
            flow.request { result }
            assertEquals(result, flow.phase)
            flow.request { throw SecurityException("Test fixture") }
            assertEquals(HandoffPhase.BLOCKED, flow.phase)
        }
    }

    @Test fun synchronousReleaseErrorNeverDispatches() {
        var launched = false
        val flow = AudioHandoff({ throw IllegalStateException() }, VoiceScheduler { _, _ -> Cancel {} }, {})
        flow.request { launched = true; HandoffPhase.REQUESTED }
        assertFalse(launched)
        assertEquals(HandoffPhase.RELEASE_FAILED, flow.phase)
    }
}

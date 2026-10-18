// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.voice

import org.junit.Assert.*
import org.junit.Test

class AudioCheckTest {
    private class Audio : VoiceAudio {
        val preparations = mutableListOf<(VoiceProblem?) -> Unit>()
        val recognitions = mutableListOf<(Recognition) -> Unit>()
        val outputs = mutableListOf<String>()
        val completions = mutableListOf<(VoiceProblem?) -> Unit>()
        var closed = 0
        override fun prepare(complete: (VoiceProblem?) -> Unit): Cancel { preparations += complete; return Cancel {} }
        override fun listen(complete: (Recognition) -> Unit): Cancel { recognitions += complete; return Cancel {} }
        override fun speak(text: String, complete: (VoiceProblem?) -> Unit): Cancel {
            outputs += text; completions += complete; return Cancel {}
        }
        override fun close() { closed++ }
    }
    private class Fixture {
        val audio = Audio()
        val deadlines = mutableListOf<() -> Unit>()
        val states = mutableListOf<Pair<VoicePhase, VoiceProblem?>>()
        val check = AudioCheck(audio, VoiceScheduler { _, task -> deadlines += task; Cancel {} }) { phase, error -> states += phase to error }
    }
    @Test fun outputTestNeverListensAndFinishesAfterPlayback() {
        val f = Fixture(); f.check.start(false, "Synthetic output check")
        f.audio.preparations.single()(null)
        assertEquals(listOf("Synthetic output check"), f.audio.outputs)
        assertTrue(f.audio.recognitions.isEmpty())
        f.audio.completions.single()(null)
        assertEquals(VoicePhase.STOPPED to null, f.states.last())
    }
    @Test fun microphoneTestReadsOneSentenceWithoutAConversationBackend() {
        val f = Fixture(); f.check.start(true, "unused")
        f.audio.preparations.single()(null)
        f.audio.recognitions.single()(Recognition.Text("Synthetic microphone fixture"))
        assertEquals(listOf("Synthetic microphone fixture"), f.audio.outputs)
        f.audio.completions.single()(null)
        assertEquals(1, f.audio.recognitions.size)
        assertEquals(VoicePhase.STOPPED to null, f.states.last())
    }
    @Test fun permissionErrorDoesNotReadOrRestart() {
        val f = Fixture(); f.check.start(true, "unused"); f.audio.preparations.single()(null)
        f.audio.recognitions.single()(Recognition.Failed(VoiceProblem.PERMISSION))
        assertTrue(f.audio.outputs.isEmpty())
        assertEquals(VoicePhase.STOPPED to VoiceProblem.PERMISSION, f.states.last())
    }
    @Test fun timeoutStillAppliesAfterMovingFromPreparationToListening() {
        val f = Fixture(); f.check.start(true, "unused"); f.audio.preparations.single()(null)
        f.deadlines.single()()
        f.audio.recognitions.single()(Recognition.Text("Late fixture"))
        assertTrue(f.audio.outputs.isEmpty())
        assertEquals(VoicePhase.STOPPED to VoiceProblem.TIMEOUT, f.states.last())
    }
    @Test fun restartIgnoresAllOldCallbacksAndDeadlines() {
        val f = Fixture(); f.check.start(true, "unused"); f.audio.preparations[0](null)
        f.check.cancel(); f.check.start(false, "new fixture")
        val before = f.states.toList()
        f.audio.recognitions[0](Recognition.Text("stale fixture")); f.deadlines[0](); f.audio.preparations[0](null)
        assertEquals(before, f.states)
        f.audio.preparations[1](null)
        assertEquals(listOf("new fixture"), f.audio.outputs)
    }
    @Test fun duplicateResultsCannotProduceMoreThanOneOutput() {
        val f = Fixture(); f.check.start(true, "unused"); f.audio.preparations.single()(null)
        f.audio.recognitions.single()(Recognition.Text("first fixture"))
        f.audio.recognitions.single()(Recognition.Text("duplicate fixture"))
        assertEquals(listOf("first fixture"), f.audio.outputs)
    }
}

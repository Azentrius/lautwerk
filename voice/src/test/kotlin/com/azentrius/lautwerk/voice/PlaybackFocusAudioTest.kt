// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.voice

import org.junit.Assert.*
import org.junit.Test

class PlaybackFocusAudioTest {
    private class Focus(private val events: MutableList<String>) : PlaybackFocus {
        var granted = true
        var loseDuringRequest = false
        val losses = mutableListOf<() -> Unit>()
        override fun request(onLost: () -> Unit): Cancel? {
            events += "request"
            losses += onLost
            if (loseDuringRequest) onLost()
            return if (granted) Cancel { events += "release" } else null
        }
    }
    private class Audio(private val events: MutableList<String>) : VoiceAudio {
        val completions = mutableListOf<(VoiceProblem?) -> Unit>()
        var immediate = false
        var throws = false
        override fun prepare(complete: (VoiceProblem?) -> Unit): Cancel {
            events += "prepare"; complete(null); return Cancel {}
        }
        override fun listen(complete: (Recognition) -> Unit): Cancel {
            events += "listen"; return Cancel {}
        }
        override fun speak(text: String, complete: (VoiceProblem?) -> Unit): Cancel {
            events += "speak"
            if (throws) throw IllegalStateException("synthetic")
            completions += complete
            if (immediate) complete(null)
            return Cancel { events += "stop" }
        }
        override fun close() { events += "close" }
    }
    private class Fixture {
        val events = mutableListOf<String>()
        val focus = Focus(events)
        val audio = Audio(events)
        val subject = PlaybackFocusAudio(audio, focus)
        val results = mutableListOf<VoiceProblem?>()
        fun speak() = subject.speak("Testantwort") { results += it }
    }

    @Test fun initializationAndRecognitionDoNotCompeteWithRecognizerFocus() {
        val f = Fixture()
        f.subject.prepare { assertNull(it) }
        f.subject.listen {}
        f.subject.listen {}
        assertEquals(listOf("prepare", "listen", "listen"), f.events)
        assertTrue(f.focus.losses.isEmpty())
    }

    @Test fun focusOnlyCoversPlaybackAndIsReleasedBeforeNextRecognition() {
        val f = Fixture()
        f.subject.speak("Antwort") { assertNull(it); f.subject.listen {} }
        assertEquals(listOf("request", "speak"), f.events)
        f.audio.completions.single()(null)
        assertEquals(listOf("request", "speak", "stop", "release", "listen"), f.events)
        f.focus.losses.single()() // Late loss after release must not stop the next turn.
        assertEquals("listen", f.events.last())
    }

    @Test fun deniedFocusDoesNotStartPlayback() {
        val f = Fixture(); f.focus.granted = false
        f.speak()
        assertEquals(listOf("request"), f.events)
        assertEquals(listOf(VoiceProblem.AUDIO_FOCUS), f.results)
    }

    @Test fun focusLossStopsOutputOnceAndIgnoresLateCompletion() {
        val f = Fixture(); f.speak()
        f.focus.losses.single()()
        f.audio.completions.single()(null)
        f.focus.losses.single()()
        assertEquals(listOf("request", "speak", "stop", "release"), f.events)
        assertEquals(listOf(VoiceProblem.AUDIO_FOCUS), f.results)
    }

    @Test fun cancelReleasesFocusWithoutDeliveringAnErrorOrRestarting() {
        val f = Fixture(); val pending = f.speak()
        pending.cancel(); pending.cancel()
        f.focus.losses.single()(); f.audio.completions.single()(null)
        assertEquals(listOf("request", "speak", "stop", "release"), f.events)
        assertTrue(f.results.isEmpty())
    }

    @Test fun closingStopsOutputAndReleasesFocusBeforeEngineShutdown() {
        val f = Fixture(); f.speak(); f.subject.close()
        assertEquals(listOf("request", "speak", "stop", "release", "close"), f.events)
        f.focus.losses.single()(); assertTrue(f.results.isEmpty())
    }

    @Test fun oldCallbacksAndCancelHandleCannotInterruptNewPlayback() {
        val f = Fixture(); val old = f.speak(); f.speak()
        val before = f.events.toList()
        old.cancel(); f.focus.losses.first()(); f.audio.completions.first()(null)
        assertEquals(before, f.events); assertTrue(f.results.isEmpty())
        f.audio.completions.last()(null)
        assertEquals(listOf<VoiceProblem?>(null), f.results)
        assertEquals(2, f.events.count { it == "release" })
    }

    @Test fun immediateCompletionDoesNotLeaveFocusOrSpeechActive() {
        val f = Fixture(); f.audio.immediate = true
        val pending = f.speak(); pending.cancel()
        assertEquals(listOf("request", "speak", "release", "stop"), f.events)
        assertEquals(listOf<VoiceProblem?>(null), f.results)
    }

    @Test fun focusLostDuringAcquisitionReleasesReturnedHandleWithoutSpeaking() {
        val f = Fixture(); f.focus.loseDuringRequest = true
        f.speak()
        assertEquals(listOf("request", "release"), f.events)
        assertEquals(listOf(VoiceProblem.AUDIO_FOCUS), f.results)
    }

    @Test fun ttsFailureReleasesFocusBeforeReportingFailure() {
        val f = Fixture(); f.speak()
        f.audio.completions.single()(VoiceProblem.TTS_FAILED)
        assertEquals("release", f.events.last())
        assertEquals(listOf(VoiceProblem.TTS_FAILED), f.results)
    }

    @Test fun ttsStartExceptionReleasesFocus() {
        val f = Fixture(); f.audio.throws = true; f.speak()
        assertEquals(listOf("request", "speak", "release"), f.events)
        assertEquals(listOf(VoiceProblem.TTS_FAILED), f.results)
    }
}

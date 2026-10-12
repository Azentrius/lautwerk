// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.voice

import dev.openautoai.core.ai.*
import dev.openautoai.core.storage.Conversation
import org.junit.Assert.*
import org.junit.Test

class VoiceControllerTest {
    private class Clock : VoiceScheduler {
        data class Job(val at: Long, val action: () -> Unit, var cancelled: Boolean = false)
        var now = 0L
        val jobs = mutableListOf<Job>()
        override fun after(delayMillis: Long, action: () -> Unit): Cancel {
            val job = Job(now + delayMillis, action)
            jobs += job
            return Cancel { job.cancelled = true }
        }
        fun advance(millis: Long) {
            val end = now + millis
            while (true) {
                val job = jobs.filter { !it.cancelled && it.at <= end }.minByOrNull { it.at } ?: break
                now = job.at; job.cancelled = true; job.action()
            }
            now = end
        }
    }
    private class Audio : VoiceAudio {
        var prepares = 0
        var listens = 0
        var closes = 0
        val spoken = mutableListOf<String>()
        lateinit var prepared: (VoiceProblem?) -> Unit
        lateinit var heard: (Recognition) -> Unit
        lateinit var finished: (VoiceProblem?) -> Unit
        override fun prepare(complete: (VoiceProblem?) -> Unit): Cancel { prepares++; prepared = complete; return Cancel {} }
        override fun listen(complete: (Recognition) -> Unit): Cancel { listens++; heard = complete; return Cancel {} }
        override fun speak(text: String, complete: (VoiceProblem?) -> Unit): Cancel { spoken += text; finished = complete; return Cancel {} }
        override fun close() { closes++ }
    }
    private class Backend : VoiceBackend {
        lateinit var opened: (Result<Conversation>) -> Unit
        lateinit var replied: (Result<ChatReply>) -> Unit
        lateinit var switched: (Result<Conversation>) -> Unit
        val phrases = mutableListOf<String>()
        val switches = mutableListOf<Provider>()
        var cancellations = 0
        override fun open(id: String, complete: (Result<Conversation>) -> Unit): Cancel { opened = complete; return Cancel { cancellations++ } }
        override fun reply(conversation: Conversation, phrase: String, complete: (Result<ChatReply>) -> Unit): Cancel {
            phrases += phrase; replied = complete; return Cancel { cancellations++ }
        }
        override fun switchProvider(provider: Provider, complete: (Result<Conversation>) -> Unit): Cancel {
            switches += provider; switched = complete; return Cancel { cancellations++ }
        }
    }
    private class Fixture {
        val audio = Audio()
        val backend = Backend()
        val clock = Clock()
        val states = mutableListOf<VoiceState>()
        var focusHeld = false
        var focusGranted = true
        var lostFocus: (() -> Unit)? = null
        val focusedAudio = PlaybackFocusAudio(audio, PlaybackFocus { lost ->
            lostFocus = lost
            if (focusGranted) {
                focusHeld = true
                Cancel { focusHeld = false }
            } else null
        })
        val controller = VoiceController(focusedAudio, backend, clock) { states += it }
        fun conversation(provider: Provider = Provider.OPENAI) = Conversation(provider.name, "Test", provider, ModelCatalog.default(provider), 0, emptyList())
        fun ready(provider: Provider = Provider.OPENAI) {
            controller.start(provider.name)
            backend.opened(Result.success(conversation(provider)))
            audio.prepared(null)
            clock.advance(250)
            assertEquals(VoicePhase.LISTENING, controller.state.phase)
        }
        fun phrase(text: String) = audio.heard(Recognition.Text(text))
    }

    @Test fun englishCommandsSwitchAndStopWithoutBecomingProviderRequests() {
        val old=dev.openautoai.core.i18n.AppLanguage.preferences
        try {
            dev.openautoai.core.i18n.AppLanguage.preferences={listOf(java.util.Locale.ENGLISH)}
            val f=Fixture(); f.ready()
            assertTrue(f.controller.state.status.startsWith("Listening"))
            f.phrase("Switch to Claude.")
            assertEquals(listOf(Provider.CLAUDE),f.backend.switches)
            f.backend.switched(Result.success(f.conversation(Provider.CLAUDE)))
            assertEquals("We are now talking to Claude / Anthropic. This is a new conversation.",f.audio.spoken.last())
            f.audio.finished(null);f.clock.advance(350)
            f.phrase("End conversation!")
            assertFalse(f.controller.state.active);assertEquals("Conversation ended",f.controller.state.status)
            assertTrue(f.backend.phrases.isEmpty())
        } finally { dev.openautoai.core.i18n.AppLanguage.preferences=old }
    }

    @Test fun bothProvidersFollowListenThinkSpeakAndListenWithoutOverlap() {
        Provider.entries.forEach { provider ->
            val f = Fixture(); f.ready(provider)
            assertNull(f.lostFocus) // No playback focus request during recognition.
            f.phrase("Guten Morgen")
            assertEquals(VoicePhase.THINKING, f.controller.state.phase)
            assertFalse(f.focusHeld)
            assertEquals(1, f.audio.listens)
            f.backend.replied(Result.success(ChatReply("Guten Morgen zurück")))
            assertEquals(VoicePhase.SPEAKING, f.controller.state.phase)
            assertTrue(f.focusHeld)
            f.clock.advance(1000)
            assertEquals(1, f.audio.listens)
            f.audio.finished(null)
            f.clock.advance(350)
            assertEquals(2, f.audio.listens)
            assertFalse(f.focusHeld)
            assertEquals(provider, f.controller.state.conversation!!.provider)
            assertEquals(listOf("Guten Morgen zurück"), f.audio.spoken)
            assertTrue(f.states.none { !it.active }) // Start does not publish a spurious Stop to the Service.
        }
    }

    @Test fun missingCredentialsStopBeforeAnyMicrophoneOrTtsInitialization() {
        val f = Fixture(); f.controller.start("missing")
        f.backend.opened(Result.failure(ChatFailure(ChatError.MISSING_KEY)))
        assertFalse(f.controller.state.active)
        assertEquals(ChatError.MISSING_KEY.userMessage, f.controller.state.notice)
        assertEquals(0, f.audio.prepares)
        assertEquals(0, f.audio.listens)
    }

    @Test fun stopDuringPreparationIgnoresLateOpenAndReadyCallbacks() {
        val f = Fixture(); f.controller.start("old")
        val oldOpen = f.backend.opened
        f.controller.stop(); oldOpen(Result.success(f.conversation()))
        assertEquals(0, f.audio.prepares)
        f.controller.start("new"); f.backend.opened(Result.success(f.conversation()))
        val oldReady = f.audio.prepared
        f.controller.stop(); oldReady(null); f.clock.advance(1000)
        assertFalse(f.controller.state.active)
        assertEquals(0, f.audio.listens)
    }

    @Test fun duplicateRecognitionAndLateResponseCannotStartExtraRequestsOrSpeech() {
        val f = Fixture(); f.ready()
        val result = f.audio.heard
        result(Recognition.Text("Hallo")); result(Recognition.Text("Doppelt"))
        assertEquals(listOf("Hallo"), f.backend.phrases)
        val lateReply = f.backend.replied
        f.controller.stop()
        lateReply(Result.success(ChatReply("Zu spät")))
        assertTrue(f.audio.spoken.isEmpty())
        assertEquals(1, f.backend.cancellations)
        assertFalse(f.controller.state.active)
    }

    @Test fun stopDuringListeningAndSpeechRejectsLateCallbacksAfterRestart() {
        val f = Fixture(); f.ready()
        val oldHeard = f.audio.heard
        f.controller.stop(); f.ready(Provider.CLAUDE)
        oldHeard(Recognition.Text("Veraltete Aufnahme"))
        assertTrue(f.backend.phrases.isEmpty())
        f.phrase("Hallo"); f.backend.replied(Result.success(ChatReply("Antwort")))
        val oldDone = f.audio.finished
        f.controller.stop(); f.ready()
        val count = f.audio.listens
        oldDone(null); f.clock.advance(1000)
        assertEquals(count, f.audio.listens)
        assertEquals(VoicePhase.LISTENING, f.controller.state.phase)
    }

    @Test fun ttsInitializationAndPlaybackFailuresStopInsteadOfRecordingSilently() {
        val f = Fixture(); f.controller.start("test"); f.backend.opened(Result.success(f.conversation()))
        f.audio.prepared(VoiceProblem.TTS_UNAVAILABLE)
        assertEquals(0, f.audio.listens)
        assertFalse(f.controller.state.active)
        f.ready(); f.phrase("Hallo"); f.backend.replied(Result.success(ChatReply("Gespeicherte Antwort")))
        f.audio.finished(VoiceProblem.TTS_FAILED); f.clock.advance(1000)
        assertEquals(VoiceProblem.TTS_FAILED.message, f.controller.state.notice)
        assertFalse(f.controller.state.active)
        assertEquals(1, f.audio.listens)
    }

    @Test fun twoSilentAttemptsEndSessionAndNoInfiniteRetryOccurs() {
        val f = Fixture(); f.ready()
        f.audio.heard(Recognition.Failed(VoiceProblem.NO_INPUT)); f.clock.advance(700)
        assertEquals(2, f.audio.listens)
        f.phrase(" "); f.clock.advance(60_000)
        assertFalse(f.controller.state.active)
        assertEquals(2, f.audio.listens)
        assertTrue(f.backend.phrases.isEmpty())
    }

    @Test fun permissionBusyNetworkAndUnknownRecognitionErrorsDoNotRetry() {
        listOf(VoiceProblem.PERMISSION, VoiceProblem.RECOGNIZER_BUSY, VoiceProblem.RECOGNITION_NETWORK, VoiceProblem.RECOGNITION_FAILED).forEach { problem ->
            val f = Fixture(); f.ready(); f.audio.heard(Recognition.Failed(problem)); f.clock.advance(5000)
            assertFalse(f.controller.state.active)
            assertEquals(problem.message, f.controller.state.notice)
            assertEquals(1, f.audio.listens)
        }
    }

    @Test fun exactCommandsAreLocalButLongerQuestionsRemainQuestions() {
        val f = Fixture(); f.ready(); f.phrase("  Wechsle   zu Claude!  ")
        assertEquals(listOf(Provider.CLAUDE), f.backend.switches)
        assertTrue(f.backend.phrases.isEmpty())
        f.backend.switched(Result.success(f.conversation(Provider.CLAUDE)))
        assertEquals(Provider.CLAUDE, f.controller.state.conversation!!.provider)
        f.audio.finished(null); f.clock.advance(350)
        f.phrase("Wechsle zu Claude und erkläre mir den Unterschied")
        assertEquals(1, f.backend.phrases.size)
    }

    @Test fun stopCommandIsNeverSentToProviderAndSameProviderDoesNotCreateChat() {
        val f = Fixture(); f.ready(); f.phrase("Wechsel zu OpenAI")
        assertTrue(f.backend.switches.isEmpty())
        f.audio.finished(null); f.clock.advance(350); f.phrase("Stopp.")
        assertFalse(f.controller.state.active)
        assertTrue(f.backend.phrases.isEmpty())
    }

    @Test fun providerFailureIsSafeAndHasNoAutomaticRetryOrFallback() {
        val f = Fixture(); f.ready(); f.phrase("Hallo")
        f.backend.replied(Result.failure(ChatFailure(ChatError.QUOTA)))
        f.clock.advance(5000)
        assertEquals(ChatError.QUOTA.userMessage, f.controller.state.notice)
        assertEquals(1, f.backend.phrases.size)
        assertTrue(f.backend.switches.isEmpty())
        assertTrue(f.audio.spoken.isEmpty())
    }

    @Test fun unknownFailuresDoNotRevealUnderlyingMessages() {
        val f = Fixture(); f.controller.start("test")
        f.backend.opened(Result.failure(IllegalStateException("private transcript and secret")))
        assertFalse(f.controller.state.notice!!.contains("private"))
        assertFalse(f.controller.state.active)
    }

    @Test fun truncatedAndRefusedResponsesHaveLocalNoticesBeforeSpeech() {
        listOf(Completion.TRUNCATED, Completion.REFUSED).forEach { completion ->
            val f = Fixture(); f.ready(); f.phrase("Hallo")
            f.backend.replied(Result.success(ChatReply("Antwort", completion)))
            assertNotNull(f.controller.state.notice)
            assertTrue(f.audio.spoken.single().endsWith("Antwort"))
            assertTrue(f.audio.spoken.single().startsWith(f.controller.state.notice!!))
        }
    }

    @Test fun preparationAndRequestTimeoutsStopAndCancelPendingWork() {
        val f = Fixture(); f.controller.start("test")
        f.clock.advance(15_000)
        assertFalse(f.controller.state.active)
        assertEquals(1, f.backend.cancellations)
        f.ready(); f.phrase("Hallo"); f.clock.advance(70_000)
        assertFalse(f.controller.state.active)
        assertEquals(VoiceProblem.TIMEOUT.message, f.controller.state.notice)
        assertEquals(2, f.backend.cancellations)
    }

    @Test fun lateSessionDeadlineCannotStopANewerSession() {
        val f = Fixture(); f.ready()
        val oldDeadline = f.clock.jobs.single { it.at == 600_000L }.action
        f.controller.stop(); f.ready()
        oldDeadline()
        assertTrue(f.controller.state.active)
        val currentDeadline = f.clock.jobs.last { !it.cancelled && it.at - f.clock.now > 500_000 }.action
        currentDeadline()
        assertFalse(f.controller.state.active)
        assertTrue(f.controller.state.notice!!.contains("zehn Minuten"))
    }

    @Test fun playbackFocusLossEndsEitherProviderWithoutResumingOrRetrying() {
        Provider.entries.forEach { provider ->
            val f = Fixture(); f.ready(provider); f.phrase("Hallo")
            f.backend.replied(Result.success(ChatReply("Antwort")))
            f.lostFocus!!()
            f.audio.finished(null); f.clock.advance(1000)
            assertFalse(f.focusHeld)
            assertFalse(f.controller.state.active)
            assertEquals(VoiceProblem.AUDIO_FOCUS.message, f.controller.state.notice)
            assertEquals(1, f.audio.listens)
            assertEquals(1, f.backend.phrases.size)
        }
    }

    @Test fun playbackFocusDenialStopsBothProvidersBeforeTts() {
        Provider.entries.forEach { provider ->
            val f = Fixture(); f.focusGranted = false; f.ready(provider); f.phrase("Hallo")
            f.backend.replied(Result.success(ChatReply("Antwort")))
            assertFalse(f.controller.state.active)
            assertEquals(VoiceProblem.AUDIO_FOCUS.message, f.controller.state.notice)
            assertTrue(f.audio.spoken.isEmpty())
        }
    }

    @Test fun systemInterruptionsStopBothProvidersInEveryPhaseAndIgnoreOldCallbacksAfterRestart() {
        for (provider in Provider.entries) for (phase in VoicePhase.entries.filter { it != VoicePhase.STOPPED }) {
            val f = Fixture()
            if (phase == VoicePhase.PREPARING) f.controller.start(provider.name) else f.ready(provider)
            if (phase == VoicePhase.THINKING || phase == VoicePhase.SPEAKING) f.phrase("Hallo")
            if (phase == VoicePhase.SPEAKING) f.backend.replied(Result.success(ChatReply("Antwort")))
            val late = when (phase) {
                VoicePhase.PREPARING -> { val callback = f.backend.opened; { callback(Result.success(f.conversation(provider))) } }
                VoicePhase.LISTENING -> { val callback = f.audio.heard; { callback(Recognition.Text("zu spät")) } }
                VoicePhase.THINKING -> { val callback = f.backend.replied; { callback(Result.success(ChatReply("zu spät"))) } }
                VoicePhase.SPEAKING -> { val callback = f.audio.finished; { callback(null) } }
                else -> error("unexpected phase")
            }
            var environment = AudioEnvironment(true, false, emptySet())
            lateinit var signal: (AudioEnvironment) -> Unit
            val guard = VoiceInterruptions(object : VoiceEnvironment {
                override fun current() = environment
                override fun observe(changed: (AudioEnvironment) -> Unit): Cancel { signal = changed; return Cancel {} }
            }) { f.controller.stop(it.message) }
            guard.start()
            environment = environment.copy(communicationBusy = true)
            signal(environment)
            assertFalse(f.controller.state.active)
            assertFalse(f.focusHeld)
            assertEquals(VoiceInterruption.COMMUNICATION.message, f.controller.state.notice)
            val requests = f.backend.phrases.size
            val spoken = f.audio.spoken.size
            late(); f.clock.advance(1000)
            assertFalse(f.controller.state.active)
            f.ready(provider)
            late(); f.clock.advance(1000)
            assertEquals(VoicePhase.LISTENING, f.controller.state.phase)
            assertEquals(requests, f.backend.phrases.size)
            assertEquals(spoken, f.audio.spoken.size)
        }
    }

}

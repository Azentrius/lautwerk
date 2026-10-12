package dev.openautoai.voice

import dev.openautoai.core.ai.*
import dev.openautoai.core.storage.StorageFailure
import java.util.Locale

/** Single-threaded conversation state machine. Every operation has its own stale-callback guard. */
class VoiceController(
    private val audio: VoiceAudio,
    private val backend: VoiceBackend,
    private val scheduler: VoiceScheduler,
    private val changed: (VoiceState) -> Unit
) {
    var state = VoiceState()
        private set
    private var step = 0L
    private var sessionGeneration = 0L
    private var pending: Cancel? = null
    private var deadline: Cancel? = null
    private var sessionDeadline: Cancel? = null
    private var misses = 0

    fun start(id: String) {
        reset()
        misses = 0
        state = VoiceState(phase = VoicePhase.PREPARING, status = "Gespräch wird vorbereitet")
        val token = begin(VoicePhase.PREPARING, state.status, 15_000)
        // This is a short, explicitly started session, not an always-on recognition service.
        val sessionToken = sessionGeneration
        sessionDeadline = scheduler.after(10 * 60_000L) {
            if (state.active && sessionToken == sessionGeneration)
                stop("Die Sprachsitzung ist nach zehn Minuten beendet. Du kannst sie erneut starten.")
        }
        launch(token) { backend.open(id) { result ->
            if (accept(token)) result.fold({ conversation ->
                state = state.copy(conversation = conversation)
                prepare()
            }, ::fail)
        } }
    }

    private fun prepare() {
        val token = begin(VoicePhase.PREPARING, "Sprachdienste werden vorbereitet", 12_000)
        launch(token) { audio.prepare { problem ->
            if (accept(token)) {
                if (problem != null) stop(problem.message) else listenAfter(250)
            }
        } }
    }

    private fun listenAfter(delay: Long) {
        val token = begin(VoicePhase.PREPARING, "Mikrofon wird vorbereitet", 5_000)
        launch(token) { scheduler.after(delay) { if (accept(token)) listen() } }
    }

    private fun listen() {
        val conversation = state.conversation ?: return stop("Gespräch nicht gefunden")
        val token = begin(VoicePhase.LISTENING, "Ich höre zu – ${conversation.provider.displayName}", 45_000)
        launch(token) { audio.listen { result ->
            if (accept(token)) when (result) {
                is Recognition.Text -> recognized(result.text)
                is Recognition.Failed -> recognitionFailed(result.problem)
            }
        } }
    }

    private fun recognitionFailed(problem: VoiceProblem) {
        if (problem == VoiceProblem.NO_INPUT && ++misses < 2) listenAfter(700)
        else stop(problem.message)
    }

    private fun recognized(raw: String) {
        val phrase = raw.trim()
        if (phrase.isBlank()) return recognitionFailed(VoiceProblem.NO_INPUT)
        misses = 0
        val command = phrase.lowercase(Locale.ROOT).trimEnd('.', '!', '?').replace(Regex("\\s+"), " ")
        when (command) {
            "stop", "stopp", "beenden", "gespräch beenden" -> stop()
            "wechsle zu claude", "wechsel zu claude" -> switchProvider(Provider.CLAUDE)
            "wechsle zu chatgpt", "wechsel zu chatgpt", "wechsle zu openai", "wechsel zu openai" -> switchProvider(Provider.OPENAI)
            else -> reply(phrase)
        }
    }

    private fun switchProvider(provider: Provider) {
        if (state.conversation?.provider == provider) return speak("Wir sprechen bereits mit ${provider.displayName}.")
        val token = begin(VoicePhase.PREPARING, "Anbieterwechsel wird vorbereitet", 15_000)
        launch(token) { backend.switchProvider(provider) { result ->
            if (accept(token)) result.fold({ conversation ->
                state = state.copy(conversation = conversation, notice = null)
                speak("Wir sprechen jetzt mit ${provider.displayName}. Das ist ein neues Gespräch.")
            }, ::fail)
        } }
    }

    private fun reply(phrase: String) {
        val conversation = state.conversation ?: return stop("Gespräch nicht gefunden")
        val token = begin(VoicePhase.THINKING, "${conversation.provider.displayName} antwortet", 70_000)
        launch(token) { backend.reply(conversation, phrase) { result ->
            if (accept(token)) result.fold({ response ->
                val notice = when (response.completion) {
                    Completion.TRUNCATED -> "Die Antwort wurde am Limit gekürzt."
                    Completion.REFUSED -> "Der Anbieter hat diese Anfrage abgelehnt."
                    else -> null
                }
                state = state.copy(notice = notice)
                speak(listOfNotNull(notice, response.text).joinToString(" "))
            }, ::fail)
        } }
    }

    private fun speak(text: String) {
        val token = begin(VoicePhase.SPEAKING, "Antwort wird vorgelesen", 120_000)
        launch(token) { audio.speak(text) { problem ->
            if (accept(token)) {
                if (problem != null) stop(problem.message) else listenAfter(350)
            }
        } }
    }

    private fun fail(error: Throwable) = stop(when (error) {
        is ChatFailure -> error.message
        is StorageFailure -> error.message
        else -> "Das Sprachgespräch ist fehlgeschlagen. Bitte erneut starten."
    })

    private fun begin(phase: VoicePhase, status: String, timeout: Long): Long {
        step++
        pending?.cancel(); pending = null
        deadline?.cancel()
        val token = step
        state = state.copy(phase = phase, status = status)
        changed(state)
        deadline = scheduler.after(timeout) { if (state.active && step == token) stop(VoiceProblem.TIMEOUT.message) }
        return token
    }

    private fun launch(token: Long, operation: () -> Cancel) {
        try {
            val handle = operation()
            if (state.active && step == token) pending = handle else handle.cancel()
        } catch (error: Exception) { if (state.active && step == token) fail(error) }
    }

    private fun accept(token: Long): Boolean {
        if (!state.active || token != step) return false
        deadline?.cancel(); deadline = null
        pending = null
        return true
    }

    /** Stop is local and immediate. A provider may already be processing an HTTP request. */
    private fun reset() {
        step++
        sessionGeneration++
        pending?.cancel(); pending = null
        deadline?.cancel(); deadline = null
        sessionDeadline?.cancel(); sessionDeadline = null
        runCatching { audio.close() }
    }

    fun stop(notice: String? = null) {
        reset()
        state = state.copy(phase = VoicePhase.STOPPED, status = "Gespräch beendet", notice = notice)
        changed(state)
    }
}

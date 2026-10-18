// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.voice

/** A bounded, cancellable audio check. It has no backend, credentials or persistence dependency. */
class AudioCheck(
    private val audio: VoiceAudio,
    private val scheduler: VoiceScheduler,
    private val changed: (VoicePhase, VoiceProblem?) -> Unit
) {
    private var generation = 0L
    private var sessionGeneration = 0L
    private var pending: Cancel? = null
    private var timeout: Cancel? = null
    private var active = false

    fun start(microphone: Boolean, sample: String) {
        cancel()
        active = true
        val session = sessionGeneration
        timeout = scheduler.after(60_000) { if (active && session == sessionGeneration) finish(VoiceProblem.TIMEOUT) }
        step(VoicePhase.PREPARING) { done -> audio.prepare { problem ->
            done {
                if (problem != null) finish(problem)
                else if (microphone) listen() else speak(sample)
            }
        } }
    }

    private fun listen() = step(VoicePhase.LISTENING) { done -> audio.listen { result -> done {
        when (result) {
            is Recognition.Failed -> finish(result.problem)
            is Recognition.Text -> if (result.text.isBlank()) finish(VoiceProblem.NO_INPUT) else speak(result.text)
        }
    } } }

    private fun speak(text: String) = step(VoicePhase.SPEAKING) { done ->
        audio.speak(text) { problem -> done { finish(problem) } }
    }

    private fun step(phase: VoicePhase, operation: (((() -> Unit) -> Unit)) -> Cancel) {
        val token = ++generation
        pending?.cancel(); pending = null
        changed(phase, null)
        if (!active || token != generation) return
        try {
            val handle = operation { action ->
                if (active && token == generation) {
                    generation++
                    pending = null
                    action()
                }
            }
            if (active && token == generation) pending = handle else handle.cancel()
        } catch (_: Exception) { if (active && token == generation) finish(VoiceProblem.RECOGNITION_FAILED) }
    }

    private fun finish(problem: VoiceProblem?) {
        cancel()
        changed(VoicePhase.STOPPED, problem)
    }

    fun cancel() {
        active = false
        sessionGeneration++
        generation++
        pending?.cancel(); pending = null
        timeout?.cancel(); timeout = null
        audio.close()
    }
}

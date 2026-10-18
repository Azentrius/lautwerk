// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.voice

import com.azentrius.lautwerk.core.i18n.Messages

data class AudioEnvironment(val screenAvailable: Boolean, val communicationBusy: Boolean,
                            val externalOutputs: Set<Int>)

interface VoiceEnvironment {
    fun current(): AudioEnvironment
    /** Calls back on the controller thread. */
    fun observe(changed: (AudioEnvironment) -> Unit): Cancel
}

enum class VoiceInterruption(private val text: Messages) {
    SCREEN_OFF(Messages.INTERRUPTION_SCREEN_OFF),
    COMMUNICATION(Messages.INTERRUPTION_COMMUNICATION),
    AUDIO_DEVICE(Messages.INTERRUPTION_AUDIO_DEVICE);
    val message: String get() = text.text()
}

/** Observe only one explicit session; never resume automatically after an interruption. */
class VoiceInterruptions(private val environment: VoiceEnvironment, private val interrupted: (VoiceInterruption) -> Unit) {
    private var generation = 0L
    private var watching = false
    private var subscription: Cancel? = null

    fun start(): VoiceInterruption? {
        close()
        val baseline = environment.current()
        fun reason(value: AudioEnvironment) = when {
            !value.screenAvailable -> VoiceInterruption.SCREEN_OFF
            value.communicationBusy -> VoiceInterruption.COMMUNICATION
            value.externalOutputs != baseline.externalOutputs -> VoiceInterruption.AUDIO_DEVICE
            else -> null
        }
        reason(baseline)?.let { return it }
        val token = generation
        watching = true
        var duringStart: VoiceInterruption? = null
        fun inspect(value: AudioEnvironment) {
            if (!watching || token != generation) return
            reason(value)?.let {
                duringStart = it
                close() // Invalidate even callbacks already queued by Android.
                interrupted(it)
            }
        }
        try {
            val handle = environment.observe(::inspect)
            if (watching && token == generation) subscription = handle else handle.cancel()
            inspect(environment.current()) // Close the initial snapshot/registration race.
            return duringStart
        } catch (error: Exception) { close(); throw error }
    }

    fun close() {
        generation++
        watching = false
        val old = subscription
        subscription = null
        old?.cancel()
    }
}

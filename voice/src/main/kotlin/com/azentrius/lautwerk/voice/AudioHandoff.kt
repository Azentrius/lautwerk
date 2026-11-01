// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.voice

enum class HandoffPhase { READY, RELEASING, REQUESTED, UNAVAILABLE, BLOCKED, LOCKED, RELEASE_FAILED }

/** Caller-thread coordinator. REQUESTED deliberately says nothing about the external session. */
class AudioHandoff(
    private val release: ((Boolean) -> Unit) -> Cancel,
    private val scheduler: VoiceScheduler,
    private val changed: (HandoffPhase) -> Unit
) {
    var phase = HandoffPhase.READY
        private set
    private var generation = 0L
    private var pending: Cancel? = null
    private var deadline: Cancel? = null

    fun request(dispatch: () -> HandoffPhase) {
        if (phase == HandoffPhase.RELEASING || phase == HandoffPhase.REQUESTED) return
        val token = ++generation
        update(HandoffPhase.RELEASING)
        deadline = scheduler.after(15_000) {
            if (token == generation && phase == HandoffPhase.RELEASING) {
                generation++
                pending?.cancel(); pending = null
                deadline = null
                update(HandoffPhase.RELEASE_FAILED)
            }
        }
        try {
            val handle = release { ready ->
                if (token == generation && phase == HandoffPhase.RELEASING) {
                    deadline?.cancel(); deadline = null
                    pending = null
                    update(if (!ready) HandoffPhase.RELEASE_FAILED else try {
                        dispatch()
                    } catch (_: Exception) { HandoffPhase.BLOCKED })
                }
            }
            if (token == generation && phase == HandoffPhase.RELEASING) pending = handle else handle.cancel()
        } catch (_: Exception) {
            if (token == generation && phase == HandoffPhase.RELEASING) {
                deadline?.cancel(); deadline = null
                update(HandoffPhase.RELEASE_FAILED)
            }
        }
    }

    fun cancel() {
        generation++
        pending?.cancel(); pending = null
        deadline?.cancel(); deadline = null
        phase = HandoffPhase.READY
    }

    private fun update(next: HandoffPhase) { phase = next; changed(next) }
}

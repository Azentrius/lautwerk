// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.voice

import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

/** A fence on the same serial executor as host capture stop. A cleanup failure stays unsafe. */
class AudioReleaseFence(private val control: Executor, private val callbacks: Executor) {
    private val failed = AtomicBoolean(false)
    fun markUnsafe() { failed.set(true) }

    fun cleanup(action: () -> Unit) {
        try { action() } catch (_: Exception) { failed.set(true) }
    }

    /** Call only after cancelling all owned audio, with new starts excluded until completion. */
    fun await(complete: (Boolean) -> Unit): Cancel {
        val cancelled = AtomicBoolean(false)
        try {
            control.execute {
                callbacks.execute { if (!cancelled.get()) complete(!failed.get()) }
            }
        } catch (_: Exception) {
            failed.set(true)
            if (!cancelled.get()) complete(false)
        }
        return Cancel { cancelled.set(true) }
    }
}

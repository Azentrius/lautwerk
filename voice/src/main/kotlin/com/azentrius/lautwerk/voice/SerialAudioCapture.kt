// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.voice

import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

/** Host start/stop may block. A shared serial control executor keeps them off the UI thread and
 * orders an old stop before a new start. The read executor may block until stop closes its input.
 * Keep both executors alive until all returned handles have been cancelled. */
class SerialAudioCapture(private val control: Executor, private val reader: Executor) {
    interface Device {
        fun start()
        fun read(cancelled: () -> Boolean)
        fun stop()
    }

    fun start(create: () -> Device, failed: (Exception) -> Unit): Cancel {
        val cancelled = AtomicBoolean(false)
        // Accessed only on the serial control executor, never locked by the calling UI thread.
        var device: Device? = null
        fun release() {
            val previous = device
            device = null
            runCatching { previous?.stop() }
        }
        val handle = Cancel {
            if (cancelled.compareAndSet(false, true)) control.execute { release() }
        }
        control.execute {
            if (cancelled.get()) return@execute
            try {
                val current = create()
                device = current
                current.start()
                if (cancelled.get()) release()
                else reader.execute {
                    try { if (!cancelled.get()) current.read(cancelled::get) }
                    catch (error: Exception) { if (!cancelled.get()) failed(error) }
                    finally { handle.cancel() }
                }
            } catch (error: Exception) {
                if (!cancelled.get()) failed(error)
                release()
            }
        }
        return handle
    }
}

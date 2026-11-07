// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.car.controls

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.azentrius.lautwerk.car.LautwerkCarService
import com.azentrius.lautwerk.car.handoff.OfficialAssistant
import com.azentrius.lautwerk.voice.ControlLeaseStore
import java.io.Closeable

enum class NativeResult {
    READY, NEEDS_ROLE, NEEDS_LISTENER, NEEDS_SHORTCUT, LOCKED, BUSY,
    START_REQUESTED, END_REQUESTED, UNAVAILABLE, BLOCKED, RELEASE_FAILED
}

interface NativeControlPort {
    fun readiness(target: OfficialAssistant): NativeResult
    fun start(target: OfficialAssistant): NativeResult
    fun ticket(target: OfficialAssistant): ControlLeaseStore.Ticket?
    fun end(ticket: ControlLeaseStore.Ticket): NativeResult
    fun observe(changed: () -> Unit): Closeable
}

/** In-process state only. Holds no notification body, transcript, private intent or session token. */
object NativeControls {
    private val leases = ControlLeaseStore<PendingIntent>()
    private val listeners = mutableSetOf<() -> Unit>()
    private var service: NativeControlListener? = null
    private var startPending = false
    private val main = Handler(Looper.getMainLooper())
    private val expire = Runnable { startPending = false; notifyChanged() }

    fun connect(source: NativeControlListener) { service = source; refresh(); notifyChanged() }
    fun disconnect(source: NativeControlListener) {
        if (service !== source) return
        service = null; leases.clear(); startPending = false
        main.removeCallbacks(expire); notifyChanged()
    }
    fun changed(source: NativeControlListener) { if (service === source) { refresh(); notifyChanged() } }
    private fun notifyChanged() { listeners.toList().forEach { it() } }

    private fun refresh(): Boolean {
        val source = service ?: return false
        return try {
            if (!source.getSystemService(NotificationManager::class.java).isNotificationListenerAccessGranted(
                    ComponentName(source, NativeControlListener::class.java))) {
                disconnect(source); false
            } else {
                val records = source.readControls()
                leases.retain(records.map { it.key }.toSet())
                records.forEach { leases.update(it.key, it.target.ordinal, it.postedAt, it.action) }
                if (leases.hasRecords) { startPending = false; main.removeCallbacks(expire) }
                true
            }
        } catch (_: Exception) { disconnect(source); false }
    }

    fun port(context: Context): NativeControlPort = object : NativeControlPort {
        private val shortcuts = NativeShortcuts(context.applicationContext)
        override fun readiness(target: OfficialAssistant): NativeResult = when {
            shortcuts.locked() -> NativeResult.LOCKED
            !shortcuts.hasAccess() -> NativeResult.NEEDS_ROLE
            !refresh() -> NativeResult.NEEDS_LISTENER
            !shortcuts.configured(target) -> NativeResult.NEEDS_SHORTCUT
            startPending || leases.hasRecords -> NativeResult.BUSY
            else -> NativeResult.READY
        }
        override fun start(target: OfficialAssistant): NativeResult {
            val ready = readiness(target)
            if (ready != NativeResult.READY) return ready
            startPending = true
            val result = shortcuts.start(target)
            if (result == NativeResult.START_REQUESTED) main.postDelayed(expire, 15_000) else startPending = false
            Log.i(LautwerkCarService.TAG, "control.start target=${target.name} result=$result")
            notifyChanged()
            return result
        }
        override fun ticket(target: OfficialAssistant): ControlLeaseStore.Ticket? =
            if (refresh()) leases.ticket(target.ordinal) else null

        override fun end(ticket: ControlLeaseStore.Ticket): NativeResult {
            // Re-query current controls and revalidate the rendered lease immediately before send.
            if (!refresh()) return NativeResult.NEEDS_LISTENER
            val action = leases.consume(ticket) ?: return NativeResult.UNAVAILABLE
            val result = try { action.send(); NativeResult.END_REQUESTED }
                catch (_: PendingIntent.CanceledException) { NativeResult.UNAVAILABLE }
                catch (_: Exception) { NativeResult.BLOCKED }
            Log.i(LautwerkCarService.TAG, "control.end target=${ticket.target} result=$result")
            notifyChanged()
            return result
        }
        override fun observe(changed: () -> Unit): Closeable {
            listeners.add(changed)
            return Closeable { listeners.remove(changed) }
        }
    }
}

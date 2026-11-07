// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.car.controls

import android.app.Notification
import android.app.PendingIntent
import android.os.Build
import android.os.Process
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.azentrius.lautwerk.car.handoff.ExperimentScreenFactory
import com.azentrius.lautwerk.car.handoff.OfficialAssistant

data class NativeControlRecord(val key: String, val target: OfficialAssistant, val postedAt: Long, val action: PendingIntent)

/** Compatibility matcher for the two observed native Voice action pairs. No body or extras access. */
object NativeActionFilter {
    data class Description(val label: String, val creator: String?, val sameUser: Boolean,
        val reply: Boolean, val authenticationRequired: Boolean)

    fun selectIndex(targetPackage: String, foregroundService: Boolean, actions: List<Description>): Int? {
        if (!foregroundService || OfficialAssistant.entries.none { it.packageName == targetPackage } || actions.size != 2) return null
        val matches = actions.indices.filter { actions[it].label in setOf("Auflegen", "Hang up") }
        val index = matches.singleOrNull() ?: return null
        if (actions.none { it.label in setOf("Stummschalten", "Mute", "Stummschaltung aufheben", "Unmute") }) return null
        return index.takeIf { actions[it].let { action ->
            action.creator == targetPackage && action.sameUser && !action.reply && !action.authenticationRequired
        } }
    }

    fun endAction(targetPackage: String, foregroundService: Boolean, actions: Array<Notification.Action>?): PendingIntent? {
        if (!foregroundService || OfficialAssistant.entries.none { it.packageName == targetPackage }) return null
        if (actions == null || actions.size != 2) return null
        val index = selectIndex(targetPackage, foregroundService, actions.map {
            Description(it.title?.toString().orEmpty(), it.actionIntent?.creatorPackage,
                it.actionIntent?.creatorUserHandle == Process.myUserHandle(),
                !it.remoteInputs.isNullOrEmpty() || !it.dataOnlyRemoteInputs.isNullOrEmpty(),
                Build.VERSION.SDK_INT >= 31 && it.isAuthenticationRequired)
        }) ?: return null
        return actions[index].actionIntent
    }
}

/** System authorization is broad. This implementation discards other packages before field access. */
class NativeControlListener : NotificationListenerService() {
    override fun onListenerConnected() {
        if (!ExperimentScreenFactory.nativeControlsEnabled(this)) { requestUnbind(); return }
        NativeControls.connect(this)
    }
    override fun onListenerDisconnected() { NativeControls.disconnect(this) }
    override fun onDestroy() { NativeControls.disconnect(this); super.onDestroy() }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (target(sbn) != null) NativeControls.changed(this)
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (target(sbn) != null) NativeControls.changed(this)
    }

    private fun target(sbn: StatusBarNotification): OfficialAssistant? =
        OfficialAssistant.entries.firstOrNull { it.packageName == sbn.packageName }
            ?.takeIf { sbn.user == Process.myUserHandle() }

    fun readControls(): List<NativeControlRecord> {
        // Called only after onListenerConnected and while the explicit debug opt-in is enabled.
        if (!ExperimentScreenFactory.nativeControlsEnabled(this)) return emptyList()
        return activeNotifications.orEmpty().mapNotNull { sbn ->
            val assistant = target(sbn) ?: return@mapNotNull null
            val notification = sbn.notification
            val action = NativeActionFilter.endAction(assistant.packageName,
                notification.flags and Notification.FLAG_FOREGROUND_SERVICE != 0, notification.actions)
                ?: return@mapNotNull null
            NativeControlRecord(sbn.key, assistant, sbn.postTime, action)
        }
    }
}

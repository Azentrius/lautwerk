// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.app

import androidx.lifecycle.ViewModel

/** Retained only across configuration changes, never saved to disk or a Bundle.
 * A cancelled permission callback cannot authorize another start. */
internal class ConversationStartGate : ViewModel() {
    enum class Permission { MICROPHONE, NOTIFICATIONS }
    var sessionId: String? = null; private set
    var waiting: Permission? = null; private set
    var microphoneGranted = false; private set
    var notificationAnswered = false; private set

    fun begin(id: String): Boolean {
        if (sessionId != null || waiting != null) return false
        sessionId = id
        microphoneGranted = false
        notificationAnswered = false
        return true
    }
    fun launched(permission: Permission) { check(sessionId != null && waiting == null); waiting = permission }
    fun microphoneResult(granted: Boolean): Boolean {
        if (waiting != Permission.MICROPHONE) return false
        waiting = null
        if (sessionId == null) return false
        microphoneGranted = granted
        return true
    }
    fun notificationResult() {
        if (waiting != Permission.NOTIFICATIONS) return
        waiting = null
        if (sessionId != null) notificationAnswered = true
    }
    fun take(resumed: Boolean, hasMicrophone: Boolean, needsNotification: Boolean): String? {
        if (!resumed || waiting != null || !hasMicrophone || (needsNotification && !notificationAnswered)) return null
        return sessionId.also { sessionId = null }
    }
    /** Keep the in-flight dialog marker until its callback is drained. */
    fun launchFailed() { waiting = null; cancel() }
    fun cancel() { sessionId = null; microphoneGranted = false; notificationAnswered = false }
}

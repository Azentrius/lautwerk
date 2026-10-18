// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.app

import org.junit.Assert.*
import org.junit.Test

class ConversationStartGateTest {
    @Test fun cancelledPermissionResultCannotStartOldOrNewConversation() {
        val gate=ConversationStartGate()
        assertTrue(gate.begin("old"));gate.launched(ConversationStartGate.Permission.MICROPHONE);gate.cancel()
        assertFalse(gate.begin("new"));assertFalse(gate.microphoneResult(true))
        assertNull(gate.take(true,true,false));assertTrue(gate.begin("new"))
        assertEquals("new",gate.take(true,true,false));assertNull(gate.take(true,true,false))
    }
    @Test fun backgroundAndMissingPermissionsPreventHandoffAndOptionalDenialAllowsIt() {
        val gate=ConversationStartGate();gate.begin("a")
        assertNull(gate.take(false,true,false));assertNull(gate.take(true,false,false))
        gate.launched(ConversationStartGate.Permission.NOTIFICATIONS)
        assertNull(gate.take(true,true,true));gate.notificationResult()
        assertEquals("a",gate.take(true,true,true));assertNull(gate.take(true,true,true))
    }
    @Test fun newProcessHasNoRestoredStartAndIgnoresOldCallbacks() {
        val gate=ConversationStartGate()
        assertFalse(gate.microphoneResult(true));gate.notificationResult()
        assertNull(gate.take(true,true,false));assertTrue(gate.begin("explicit-new"))
    }
}

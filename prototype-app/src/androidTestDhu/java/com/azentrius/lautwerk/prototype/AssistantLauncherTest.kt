// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.prototype

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.azentrius.lautwerk.car.handoff.AndroidAssistantLauncher
import com.azentrius.lautwerk.car.handoff.OfficialAssistant
import com.azentrius.lautwerk.voice.HandoffPhase
import org.junit.Assert.*
import org.junit.Test

/** Does not launch or inspect an installed assistant. Android services are replaced with fixtures. */
class AssistantLauncherTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun dispatchUsesPhoneApplicationContextInsteadOfDisplayContext() {
        var sent: Intent? = null
        val phone = object : ContextWrapper(context) {
            override fun startActivity(intent: Intent) { sent = intent }
        }
        val display = object : ContextWrapper(context) {
            override fun getApplicationContext(): Context = phone
            override fun startActivity(intent: Intent) { throw AssertionError("Must not use display context") }
        }
        val launcher = AndroidAssistantLauncher(display, { false }, { Intent(Intent.ACTION_MAIN) })
        assertEquals(HandoffPhase.REQUESTED, launcher.open(OfficialAssistant.CHATGPT))
        assertNotNull(sent)
    }

    @Test fun usesResolvedFrontDoorAndNeverReportsVoiceSuccess() {
        for (assistant in OfficialAssistant.entries) {
            val resolved = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                .setPackage(assistant.packageName)
            var sent: Intent? = null
            val launcher = AndroidAssistantLauncher(context, { false }, {
                assertEquals(assistant.packageName, it); resolved
            }, { sent = it })
            assertEquals(HandoffPhase.REQUESTED, launcher.open(assistant))
            assertSame(resolved, sent)
            val intent = requireNotNull(sent)
            assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
            assertNull(intent.extras)
            assertNull(intent.data)
        }
    }

    @Test fun lockedAndMissingAppDoNotDispatch() {
        val locked = AndroidAssistantLauncher(context, { true }, { throw AssertionError("Must not query") },
            { throw AssertionError("Must not launch") })
        assertEquals(HandoffPhase.LOCKED, locked.open(OfficialAssistant.CHATGPT))
        val missing = AndroidAssistantLauncher(context, { false }, { null }, { throw AssertionError("Must not launch") })
        assertEquals(HandoffPhase.UNAVAILABLE, missing.open(OfficialAssistant.CLAUDE))
    }

    @Test fun blockedAndDisappearingAppsProduceFixedFailureStates() {
        val blocked = AndroidAssistantLauncher(context, { false }, { Intent(Intent.ACTION_MAIN) },
            { throw SecurityException("Private platform detail") })
        assertEquals(HandoffPhase.BLOCKED, blocked.open(OfficialAssistant.CHATGPT))
        val removed = AndroidAssistantLauncher(context, { false }, { Intent(Intent.ACTION_MAIN) },
            { throw ActivityNotFoundException("Private platform detail") })
        assertEquals(HandoffPhase.UNAVAILABLE, removed.open(OfficialAssistant.CLAUDE))
    }
}

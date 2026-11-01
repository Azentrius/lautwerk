// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.prototype

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.azentrius.lautwerk.car.handoff.AndroidAssistantLauncher
import com.azentrius.lautwerk.car.handoff.AssistantEntry
import com.azentrius.lautwerk.car.handoff.AssistantIntentResolver
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
        val launcher = AndroidAssistantLauncher(display, AssistantEntry.LAUNCHER, { false }, { Intent(Intent.ACTION_MAIN) })
        assertEquals(HandoffPhase.REQUESTED, launcher.open(OfficialAssistant.CHATGPT))
        assertNotNull(sent)
    }

    @Test fun usesResolvedFrontDoorAndNeverReportsVoiceSuccess() {
        for (assistant in OfficialAssistant.entries) {
            val resolved = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                .setPackage(assistant.packageName)
            var sent: Intent? = null
            val launcher = AndroidAssistantLauncher(context, AssistantEntry.LAUNCHER, { false }, {
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

    @Test fun assistIsPackageScopedWithoutPrivateComponentsExtrasOrLauncherFallback() {
        for (assistant in OfficialAssistant.entries) for (available in listOf(false, true)) {
            var query: Intent? = null
            val resolver = AssistantIntentResolver(context, AssistantEntry.ASSIST,
                frontDoor = { throw AssertionError("No launcher fallback") },
                canAssist = { query = it; available })
            val resolved = resolver.resolve(assistant.packageName)
            val intent = requireNotNull(query)
            assertEquals(Intent.ACTION_ASSIST, intent.action)
            assertEquals(assistant.packageName, intent.`package`)
            assertNull(intent.component); assertNull(intent.extras); assertNull(intent.data)
            assertNull(intent.categories); assertEquals(0, intent.flags)
            if (available) assertSame(intent, resolved) else assertNull(resolved)
        }
    }

    @Test fun assistLeavesLockDecisionToAndroidWithoutDismissalFlags() {
        var sent: Intent? = null
        val launcher = AndroidAssistantLauncher(context, AssistantEntry.ASSIST,
            locked = { throw AssertionError("Public assist delegates lock policy to Android") },
            resolve = { Intent(Intent.ACTION_ASSIST).setPackage(it) }, dispatch = { sent = it })
        assertEquals(HandoffPhase.REQUESTED, launcher.open(OfficialAssistant.CHATGPT))
        val intent = requireNotNull(sent)
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, intent.flags)
        assertNull(intent.extras); assertNull(intent.component)
    }

    @Test fun unsupportedAssistAndAndroidDenialNeverReportSuccessOrRetry() {
        var calls = 0
        val unavailable = AndroidAssistantLauncher(context, AssistantEntry.ASSIST,
            resolve = { null }, dispatch = { throw AssertionError("Must not dispatch") })
        assertEquals(HandoffPhase.UNAVAILABLE, unavailable.open(OfficialAssistant.CLAUDE))
        val denied = AndroidAssistantLauncher(context, AssistantEntry.ASSIST,
            resolve = { Intent(Intent.ACTION_ASSIST).setPackage(it) },
            dispatch = { calls++; throw SecurityException("Private detail") })
        assertEquals(HandoffPhase.BLOCKED, denied.open(OfficialAssistant.CLAUDE))
        assertEquals(1, calls)
    }

    @Test fun lockedAndMissingAppDoNotDispatch() {
        val locked = AndroidAssistantLauncher(context, AssistantEntry.LAUNCHER, { true }, { throw AssertionError("Must not query") },
            { throw AssertionError("Must not launch") })
        assertEquals(HandoffPhase.LOCKED, locked.open(OfficialAssistant.CHATGPT))
        val missing = AndroidAssistantLauncher(context, AssistantEntry.LAUNCHER, { false }, { null }, { throw AssertionError("Must not launch") })
        assertEquals(HandoffPhase.UNAVAILABLE, missing.open(OfficialAssistant.CLAUDE))
    }

    @Test fun blockedAndDisappearingAppsProduceFixedFailureStates() {
        val blocked = AndroidAssistantLauncher(context, AssistantEntry.LAUNCHER, { false }, { Intent(Intent.ACTION_MAIN) },
            { throw SecurityException("Private platform detail") })
        assertEquals(HandoffPhase.BLOCKED, blocked.open(OfficialAssistant.CHATGPT))
        val removed = AndroidAssistantLauncher(context, AssistantEntry.LAUNCHER, { false }, { Intent(Intent.ACTION_MAIN) },
            { throw ActivityNotFoundException("Private platform detail") })
        assertEquals(HandoffPhase.UNAVAILABLE, removed.open(OfficialAssistant.CLAUDE))
    }
}

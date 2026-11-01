// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.prototype

import android.content.res.Configuration
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.car.app.OnDoneCallback
import androidx.car.app.model.Action
import androidx.car.app.model.MessageTemplate
import androidx.car.app.serialization.Bundleable
import androidx.car.app.testing.ScreenController
import androidx.car.app.testing.TestCarContext
import androidx.lifecycle.Lifecycle
import androidx.test.platform.app.InstrumentationRegistry
import com.azentrius.lautwerk.car.controls.*
import com.azentrius.lautwerk.car.handoff.OfficialAssistant
import com.azentrius.lautwerk.car.handoff.ExperimentScreenFactory
import com.azentrius.lautwerk.car.voice.CarVoicePort
import com.azentrius.lautwerk.car.voice.CarVoiceState
import com.azentrius.lautwerk.voice.Cancel
import com.azentrius.lautwerk.voice.ControlLeaseStore
import java.io.Closeable
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

/** Synthetic controls only. Never requests roles/access or reads another app's notifications. */
class NativeControlTest {
    @Test fun onlyObservedVoiceShortcutLabelsAreOffered() {
        assertTrue(NativeShortcuts.allowedLabel(OfficialAssistant.CHATGPT, "Audio"))
        assertTrue(NativeShortcuts.allowedLabel(OfficialAssistant.CLAUDE, "Sprachmodus starten"))
        for (target in OfficialAssistant.entries) for (label in listOf("Fixture conversation", "Approve", "New session", "")) {
            assertFalse(NativeShortcuts.allowedLabel(target, label))
        }
    }
    @Suppress("DEPRECATION")
    @Test fun permissionBearingComponentsMatchExplicitBuildOptIn() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val enabled = ExperimentScreenFactory.nativeControlsEnabled(context)
        InstrumentationRegistry.getArguments().getString("nativeControlsExpected")?.let { assertEquals(it.toBooleanStrict(), enabled) }
        for (type in listOf(ControlVoiceInteractionService::class.java, ControlVoiceSessionService::class.java,
                ControlRecognitionService::class.java, NativeControlListener::class.java)) {
            val info = context.packageManager.getServiceInfo(ComponentName(context, type), PackageManager.MATCH_DISABLED_COMPONENTS)
            assertEquals(enabled, info.enabled)
            assertTrue(info.permission.startsWith("android.permission.BIND_"))
        }
    }
    private class Audio : CarVoicePort {
        override val state = CarVoiceState()
        var completion: ((Boolean) -> Unit)? = null
        override fun connect(changed: () -> Unit) = Unit
        override fun start() = Unit
        override fun stop() = Unit
        override fun close() = Unit
        override fun releaseForHandoff(complete: (Boolean) -> Unit): Cancel { completion = complete; return Cancel {} }
    }
    private class Controls : NativeControlPort {
        var ready = NativeResult.READY
        var launches = 0
        var sends = 0
        var lease: ControlLeaseStore.Ticket? = null
        override fun readiness(target: OfficialAssistant) = ready
        override fun start(target: OfficialAssistant): NativeResult {
            launches++; ready = NativeResult.BUSY; return NativeResult.START_REQUESTED
        }
        override fun ticket(target: OfficialAssistant) = lease
        override fun end(ticket: ControlLeaseStore.Ticket): NativeResult {
            if (ticket != lease) return NativeResult.UNAVAILABLE
            sends++; lease = null; ready = NativeResult.READY; return NativeResult.END_REQUESTED
        }
        override fun observe(changed: () -> Unit) = Closeable {}
    }
    private fun click(action: Action) {
        action.onClickDelegate!!.sendClick(object : OnDoneCallback {
            override fun onSuccess(response: Bundleable?) = Unit
            override fun onFailure(response: Bundleable) { throw AssertionError("Fixture action failed") }
        })
    }
    private fun context(language: String): TestCarContext {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        return TestCarContext.createCarContext(target.createConfigurationContext(Configuration(target.resources.configuration)
            .apply { setLocale(Locale.forLanguageTag(language)) }))
    }

    @Test fun releaseBeforeStartEndOnceAndRestartInBothLanguages() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            for (language in listOf("en", "de")) for (target in OfficialAssistant.entries) {
                val audio = Audio(); val controls = Controls()
                val screen = NativeControlScreen(context(language), target, audio, controls)
                val lifecycle = ScreenController(screen).moveToState(Lifecycle.State.RESUMED)
                try {
                    var template = screen.onGetTemplate() as MessageTemplate
                    assertEquals(2, template.actions.size)
                    assertEquals(if (language == "de") "Auflegen" else "Hang up", template.actions[1].title.toString())
                    template.actions.forEach { assertTrue(it.onClickDelegate!!.isParkedOnly) }
                    assertFalse(template.actions[1].isEnabled)
                    val start = template.actions[0]
                    click(start); click(start)
                    assertEquals(0, controls.launches)
                    audio.completion!!(true)
                    click(start)
                    assertEquals(1, controls.launches)
                    template = screen.onGetTemplate() as MessageTemplate
                    assertFalse(template.actions[0].isEnabled)
                    assertTrue(template.message.toString().contains(if (language == "de") "nicht bestätigt" else "not confirmed"))
                    controls.lease = ControlLeaseStore.Ticket("fixture", target.ordinal, 1)
                    val end = (screen.onGetTemplate() as MessageTemplate).actions[1]
                    assertTrue(end.isEnabled)
                    click(end); click(end)
                    assertEquals(1, controls.sends)
                    template = screen.onGetTemplate() as MessageTemplate
                    assertTrue(template.message.toString().contains(if (language == "de") "nicht bestätigt" else "not confirmed"))
                    click(template.actions[0]); audio.completion!!(true)
                    assertEquals(2, controls.launches)
                } finally { lifecycle.moveToState(Lifecycle.State.DESTROYED) }
            }
        }
    }

    @Test fun deniedAccessHiddenScreenAndFailedAudioNeverStart() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            for (mode in 0..3) {
                val audio = Audio(); val controls = Controls()
                if (mode == 0) controls.ready = NativeResult.NEEDS_ROLE
                if (mode == 1) controls.ready = NativeResult.NEEDS_LISTENER
                val screen = NativeControlScreen(context("en"), OfficialAssistant.CHATGPT, audio, controls)
                val lifecycle = ScreenController(screen).moveToState(Lifecycle.State.RESUMED)
                try {
                    val start = (screen.onGetTemplate() as MessageTemplate).actions.first()
                    click(start)
                    if (mode == 2) { lifecycle.moveToState(Lifecycle.State.CREATED); audio.completion!!(true) }
                    if (mode == 3) audio.completion!!(false)
                    assertEquals(0, controls.launches)
                } finally { lifecycle.moveToState(Lifecycle.State.DESTROYED) }
            }
        }
    }

    @Test fun notificationCompatibilityRejectsUnrelatedRepliesAmbiguityAndAuthentication() {
        for (target in OfficialAssistant.entries) for (language in listOf("en", "de")) {
            val end = NativeActionFilter.Description(if (language == "de") "Auflegen" else "Hang up", target.packageName, true, false, false)
            val mute = end.copy(label = if (language == "de") "Stummschalten" else "Mute")
            assertEquals(0, NativeActionFilter.selectIndex(target.packageName, true, listOf(end, mute)))
            assertNull(NativeActionFilter.selectIndex("example.other", true, listOf(end, mute)))
            assertNull(NativeActionFilter.selectIndex(target.packageName, false, listOf(end, mute)))
            assertNull(NativeActionFilter.selectIndex(target.packageName, true, listOf(end, end)))
            for (invalid in listOf(end.copy(label = "Approve"), end.copy(creator = "example.other"),
                    end.copy(creator = null), end.copy(sameUser = false), end.copy(reply = true), end.copy(authenticationRequired = true))) {
                assertNull(NativeActionFilter.selectIndex(target.packageName, true, listOf(invalid, mute)))
            }
        }
    }
}

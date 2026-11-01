// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.prototype

import android.content.res.Configuration
import androidx.car.app.OnDoneCallback
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.MessageTemplate
import androidx.car.app.serialization.Bundleable
import androidx.car.app.testing.ScreenController
import androidx.car.app.testing.TestCarContext
import androidx.lifecycle.Lifecycle
import androidx.test.platform.app.InstrumentationRegistry
import com.azentrius.lautwerk.car.LautwerkScreen
import com.azentrius.lautwerk.car.handoff.AssistantLauncher
import com.azentrius.lautwerk.car.handoff.HandoffScreen
import com.azentrius.lautwerk.car.handoff.OfficialAssistant
import com.azentrius.lautwerk.car.voice.CarVoicePort
import com.azentrius.lautwerk.car.voice.CarVoiceState
import com.azentrius.lautwerk.voice.Cancel
import com.azentrius.lautwerk.voice.HandoffPhase
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

/** Fake audio and launcher only. A pass proves neither Android launch permission nor DHU rendering. */
class HandoffScreenTest {
    private class Port : CarVoicePort {
        override val state = CarVoiceState()
        var acknowledgement: ((Boolean) -> Unit)? = null
        override fun connect(changed: () -> Unit) = Unit
        override fun start() = Unit
        override fun stop() = Unit
        override fun close() = Unit
        override fun releaseForHandoff(complete: (Boolean) -> Unit): Cancel {
            acknowledgement = complete
            return Cancel {}
        }
    }

    private fun click(action: Action) {
        action.onClickDelegate!!.sendClick(object : OnDoneCallback {
            override fun onSuccess(response: Bundleable?) = Unit
            override fun onFailure(response: Bundleable) { throw AssertionError("Fixture action failed") }
        })
    }

    @Test fun localizedActionsWaitForReleaseAndOnlyReportRequested() {
        val inst = InstrumentationRegistry.getInstrumentation()
        inst.runOnMainSync {
            for (language in listOf("de", "en")) for (assistant in OfficialAssistant.entries) {
                val config = Configuration(inst.targetContext.resources.configuration).apply { setLocale(Locale.forLanguageTag(language)) }
                val context = TestCarContext.createCarContext(inst.targetContext.createConfigurationContext(config))
                val audio = Port(); val launched = mutableListOf<OfficialAssistant>()
                val screen = HandoffScreen(context, audio, AssistantLauncher { launched.add(it); HandoffPhase.REQUESTED })
                val controller = ScreenController(screen).moveToState(Lifecycle.State.RESUMED)
                try {
                    val template = screen.onGetTemplate() as MessageTemplate
                    assertEquals(if (language == "de") listOf("ChatGPT öffnen", "Claude öffnen") else
                        listOf("Open ChatGPT", "Open Claude"), template.actions.map { it.title.toString() })
                    assertEquals(2, template.actions.size)
                    template.actions.forEach { assertEquals(CarColor.PRIMARY, it.backgroundColor); assertNull(it.icon); assertEquals(0, it.flags) }
                    @Suppress("DEPRECATION")
                    assertEquals(Action.BACK, template.headerAction)
                    val action = template.actions[assistant.ordinal]
                    assertTrue(action.onClickDelegate!!.isParkedOnly)
                    click(action); click(action)
                    assertTrue(launched.isEmpty())
                    assertTrue((screen.onGetTemplate() as MessageTemplate).actions.none { it.isEnabled })
                    audio.acknowledgement!!(true)
                    click(action)
                    assertEquals(listOf(assistant), launched)
                    val message = (screen.onGetTemplate() as MessageTemplate).message.toString()
                    assertTrue(message.contains(if (language == "de") "nicht bestätigt" else "not confirmed"))
                } finally { controller.moveToState(Lifecycle.State.DESTROYED) }
            }
        }
    }

    @Test fun hiddenScreenAndReleaseFailureNeverLaunch() {
        val inst = InstrumentationRegistry.getInstrumentation()
        inst.runOnMainSync {
            for (leave in listOf(false, true)) {
                val context = TestCarContext.createCarContext(inst.targetContext)
                val audio = Port()
                val screen = HandoffScreen(context, audio, AssistantLauncher { throw AssertionError("Must not launch") })
                val controller = ScreenController(screen).moveToState(Lifecycle.State.RESUMED)
                click((screen.onGetTemplate() as MessageTemplate).actions.first())
                if (leave) controller.moveToState(Lifecycle.State.CREATED)
                audio.acknowledgement!!(leave)
                controller.moveToState(Lifecycle.State.DESTROYED)
            }
        }
    }

    @Test fun baselineKeepsOneActionAndExperimentAddsOnlyOneEntry() {
        val inst = InstrumentationRegistry.getInstrumentation()
        inst.runOnMainSync {
            val context = TestCarContext.createCarContext(inst.targetContext)
            val normal = LautwerkScreen(context, voice = Port()).onGetTemplate() as MessageTemplate
            val experiment = LautwerkScreen(context, voice = Port(), handoffExperiment = true).onGetTemplate() as MessageTemplate
            assertEquals(1, normal.actions.size)
            assertEquals(2, experiment.actions.size)
            assertEquals(normal.message, experiment.message)
            assertEquals(normal.actions.first().title, experiment.actions.first().title)
            assertEquals(normal.actions.first().backgroundColor, experiment.actions.first().backgroundColor)
        }
    }
}

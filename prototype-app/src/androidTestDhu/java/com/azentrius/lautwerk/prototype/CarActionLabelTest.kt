// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.prototype

import android.content.Context
import android.content.res.Configuration
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.MessageTemplate
import androidx.car.app.testing.ScreenController
import androidx.car.app.testing.TestCarContext
import androidx.lifecycle.Lifecycle
import androidx.test.platform.app.InstrumentationRegistry
import com.azentrius.lautwerk.car.LautwerkScreen
import com.azentrius.lautwerk.car.voice.CarVoicePort
import com.azentrius.lautwerk.car.voice.CarVoiceState
import com.azentrius.lautwerk.car.voice.VoiceMode
import com.azentrius.lautwerk.car.voice.VoicePreferences
import com.azentrius.lautwerk.settings.ModelSelectionStore
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

/** Fixed states only. Never binds a speech service, opens a microphone or calls a provider. */
class CarActionLabelTest {
    private class Port : CarVoicePort {
        override var state = CarVoiceState()
        private var changed: () -> Unit = {}
        override fun connect(changed: () -> Unit) { this.changed = changed }
        override fun start() { state = CarVoiceState(active = true); changed() }
        override fun stop() { state = CarVoiceState(); changed() }
        override fun close() { changed = {} }
    }

    @Test fun localizedActionsDescribeEveryModeAndKeepEndDuringEveryActivePhase() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            for (language in listOf("de", "en", "fr")) {
                val config = Configuration(instrumentation.targetContext.resources.configuration).apply {
                    setLocale(Locale.forLanguageTag(language))
                }
                val context = TestCarContext.createCarContext(instrumentation.targetContext.createConfigurationContext(config))
                val prefs = context.getSharedPreferences("car_label_$language", Context.MODE_PRIVATE)
                prefs.edit().clear().commit()
                try {
                    val modes = VoicePreferences(prefs)
                    val audio = Port()
                    val screen = LautwerkScreen(context, ModelSelectionStore(prefs), audio, modes)
                    for (mode in VoiceMode.entries) {
                        modes.mode = mode
                        val template = screen.onGetTemplate() as MessageTemplate
                        val expected = if (mode == VoiceMode.CONVERSATION) {
                            if (language == "de") "Gespräch starten" else "Start conversation"
                        } else if (language == "de") "Audiotest starten" else "Start audio check"
                        assertEquals(expected, template.actions.single().title.toString())
                        for (summary in listOf("Preparing audio", "Listening", "Preparing the reply", "Speaking")) {
                            audio.state = CarVoiceState(active = true, summary = summary)
                            val active = screen.onGetTemplate() as MessageTemplate
                            assertEquals(if (language == "de") "Beenden" else "End", active.actions.single().title.toString())
                            assertEquals(CarColor.PRIMARY, active.actions.single().backgroundColor)
                            assertEquals(0, active.actions.single().flags)
                            assertNull(active.actions.single().icon)
                            @Suppress("DEPRECATION")
                            assertEquals(Action.APP_ICON, active.headerAction)
                            assertNull(active.icon)
                            assertFalse(active.isLoading)
                        }
                        audio.state = CarVoiceState()
                        assertEquals(expected, (screen.onGetTemplate() as MessageTemplate).actions.single().title.toString())
                    }
                } finally { prefs.edit().clear().commit() }
            }
        }
    }

    @Test fun phoneModeChangesRefreshOnlyAnIdleVisibleScreen() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val config = Configuration(instrumentation.targetContext.resources.configuration).apply { setLocale(Locale.ENGLISH) }
            val context = TestCarContext.createCarContext(instrumentation.targetContext.createConfigurationContext(config))
            val prefs = context.getSharedPreferences("car_label_lifecycle", Context.MODE_PRIVATE)
            prefs.edit().clear().commit()
            val modes = VoicePreferences(prefs)
            val audio = Port()
            val screen = LautwerkScreen(context, ModelSelectionStore(prefs), audio, modes)
            val controller = ScreenController(screen).moveToState(Lifecycle.State.RESUMED)
            try {
                modes.mode = VoiceMode.CONVERSATION
                assertEquals("Start conversation", (controller.templatesReturned.last() as MessageTemplate).actions.single().title.toString())
                modes.mode = VoiceMode.MICROPHONE
                assertEquals("Start audio check", (controller.templatesReturned.last() as MessageTemplate).actions.single().title.toString())
                val beforeOutcome = controller.templatesReturned.size
                modes.rememberOutcome("Fixed test outcome", "Fixed test outcome")
                assertEquals(beforeOutcome, controller.templatesReturned.size)
                audio.start()
                val beforeActiveChange = controller.templatesReturned.size
                modes.mode = VoiceMode.CONVERSATION
                assertEquals(beforeActiveChange, controller.templatesReturned.size)
                assertEquals("End", (screen.onGetTemplate() as MessageTemplate).actions.single().title.toString())
                audio.stop()
                assertEquals("Start conversation", (controller.templatesReturned.last() as MessageTemplate).actions.single().title.toString())
                controller.moveToState(Lifecycle.State.CREATED)
                val beforeHiddenChange = controller.templatesReturned.size
                modes.mode = VoiceMode.PLAYBACK
                assertEquals(beforeHiddenChange, controller.templatesReturned.size)
                controller.moveToState(Lifecycle.State.RESUMED)
                assertEquals("Start audio check", (screen.onGetTemplate() as MessageTemplate).actions.single().title.toString())
            } finally {
                controller.moveToState(Lifecycle.State.DESTROYED)
                prefs.edit().clear().commit()
            }
        }
    }
}

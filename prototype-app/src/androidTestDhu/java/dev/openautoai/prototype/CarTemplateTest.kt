// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.prototype

import android.content.Context
import androidx.car.app.testing.ScreenController
import androidx.lifecycle.Lifecycle
import dev.openautoai.core.ai.ModelSelection
import dev.openautoai.core.ai.Provider
import dev.openautoai.settings.ModelSelectionStore
import androidx.car.app.OnDoneCallback
import androidx.car.app.serialization.Bundleable
import androidx.car.app.model.MessageTemplate
import androidx.car.app.testing.TestCarContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.openautoai.car.OpenAutoScreen
import dev.openautoai.car.voice.CarVoicePort
import dev.openautoai.car.voice.CarVoiceState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the real template action delegate; does not prove DHU discovery. */
@RunWith(AndroidJUnit4::class)
class CarTemplateTest {
    private class AudioPort(private val context: Context) : CarVoicePort {
        override var state = CarVoiceState()
        private var changed: () -> Unit = {}
        override fun connect(changed: () -> Unit) { this.changed = changed }
        override fun start() {
            state = CarVoiceState(true, context.getString(dev.openautoai.car.R.string.voice_speaking))
            changed()
        }
        override fun stop() {
            state = CarVoiceState(false, context.getString(dev.openautoai.car.R.string.voice_stopped))
            changed()
        }
        override fun close() { state = CarVoiceState(); changed = {} }
    }
    @Test fun staleActionsKeepTheirOriginalIntent() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val context = TestCarContext.createCarContext(instrumentation.targetContext)
            val audio = AudioPort(context)
            val screen = OpenAutoScreen(context, voice = audio)
            val start = (screen.onGetTemplate() as MessageTemplate).actions.single().onClickDelegate!!
            val done = object : OnDoneCallback {
                override fun onSuccess(response: Bundleable?) = Unit
                override fun onFailure(response: Bundleable) { throw AssertionError("Action failed") }
            }
            start.sendClick(done)
            start.sendClick(done)
            org.junit.Assert.assertTrue("An old Start must never stop an active session", audio.state.active)
            val end = (screen.onGetTemplate() as MessageTemplate).actions.single().onClickDelegate!!
            // A session may end on its own before the host delivers the cached End action.
            audio.stop()
            end.sendClick(done)
            end.sendClick(done)
            org.junit.Assert.assertFalse("An old End must never start a new session", audio.state.active)
        }
    }
    @Test fun englishTemplateKeepsTitleAndWorkingStartAction() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val config=android.content.res.Configuration(instrumentation.targetContext.resources.configuration).apply { setLocale(java.util.Locale.ENGLISH) }
            val localized=instrumentation.targetContext.createConfigurationContext(config)
            val context=TestCarContext.createCarContext(localized)
            val prefs=localized.getSharedPreferences("car_english_test",Context.MODE_PRIVATE)
            prefs.edit().clear().commit()
            try {
                val screen=OpenAutoScreen(context,ModelSelectionStore(prefs),AudioPort(context))
                val template=screen.onGetTemplate() as MessageTemplate
                @Suppress("DEPRECATION")
                assertEquals("OpenAuto AI",template.title.toString())
                assertEquals("Start",template.actions.single().title.toString())
                assertEquals("Ready.\nChatGPT / OpenAI · GPT-5.4 mini",template.message.toString())
                template.actions.single().onClickDelegate!!.sendClick(object:OnDoneCallback {
                    override fun onSuccess(response:Bundleable?)=Unit
                    override fun onFailure(response:Bundleable) { throw AssertionError("Start failed") }
                })
                assertEquals("Speaking\nChatGPT / OpenAI · GPT-5.4 mini",(screen.onGetTemplate() as MessageTemplate).message.toString())
                val active = screen.onGetTemplate() as MessageTemplate
                assertEquals("End", active.actions.single().title.toString())
                active.actions.single().onClickDelegate!!.sendClick(object:OnDoneCallback {
                    override fun onSuccess(response:Bundleable?)=Unit
                    override fun onFailure(response:Bundleable) { throw AssertionError("End failed") }
                })
                assertEquals("Start", (screen.onGetTemplate() as MessageTemplate).actions.single().title.toString())
                assertEquals("Ended\nChatGPT / OpenAI · GPT-5.4 mini", (screen.onGetTemplate() as MessageTemplate).message.toString())
            } finally { prefs.edit().clear().commit() }
        }
    }

    @Test fun observesPhoneSelectionWhileStartedAndRefreshesOnReturn() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val context = TestCarContext.createCarContext(instrumentation.targetContext)
            val prefs = instrumentation.targetContext.getSharedPreferences("car_lifecycle_test", Context.MODE_PRIVATE)
            prefs.edit().clear().commit()
            val screen = OpenAutoScreen(context, ModelSelectionStore(prefs), AudioPort(context))
            val controller = ScreenController(screen).moveToState(Lifecycle.State.RESUMED)
            try {
                val phoneStore = ModelSelectionStore(prefs)
                phoneStore.select(ModelSelection(Provider.CLAUDE, "claude-haiku-4-5"))
                assertEquals("Bereit.\nClaude / Anthropic · Claude Haiku 4.5",
                    (controller.templatesReturned.last() as MessageTemplate).message.toString())
                controller.moveToState(Lifecycle.State.CREATED)
                val before = controller.templatesReturned.size
                phoneStore.select(ModelSelection(Provider.OPENAI, "gpt-5.4"))
                assertEquals(before, controller.templatesReturned.size)
                controller.moveToState(Lifecycle.State.RESUMED)
                assertEquals("Bereit.\nChatGPT / OpenAI · GPT-5.4",
                    (controller.templatesReturned.last() as MessageTemplate).message.toString())
            } finally {
                controller.moveToState(Lifecycle.State.DESTROYED)
                prefs.edit().clear().commit()
            }
        }
    }

    @Test fun templateHasTitleAndWorkingStartAction() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val context = TestCarContext.createCarContext(instrumentation.targetContext)
            val prefs = instrumentation.targetContext.getSharedPreferences("car_selection_test", Context.MODE_PRIVATE)
            prefs.edit().clear().commit()
            val store = ModelSelectionStore(prefs)
            val screen = OpenAutoScreen(context, store, AudioPort(context))
            val template = screen.onGetTemplate() as MessageTemplate
            @Suppress("DEPRECATION")
            assertEquals("OpenAuto AI", template.title.toString())
            assertEquals("Bereit.\nChatGPT / OpenAI · GPT-5.4 mini", template.message.toString())
            assertEquals(1, template.actions.size)
            assertEquals("Starten", template.actions.single().title.toString())
            var error: Any? = null
            template.actions.single().onClickDelegate!!.sendClick(object : OnDoneCallback {
                override fun onSuccess(response: Bundleable?) = Unit
                override fun onFailure(response: Bundleable) { error = response }
            })
            assertNull(error)
            assertEquals("Spricht\nChatGPT / OpenAI · GPT-5.4 mini",
                (screen.onGetTemplate() as MessageTemplate).message.toString())
            store.select(ModelSelection(Provider.CLAUDE, "claude-opus-4-6"))
            assertEquals("Spricht\nClaude / Anthropic · Claude Opus 4.6",
                (screen.onGetTemplate() as MessageTemplate).message.toString())
            prefs.edit().clear().commit()
        }
    }
}

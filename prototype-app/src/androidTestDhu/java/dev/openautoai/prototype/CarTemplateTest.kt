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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the real template action delegate; does not prove DHU discovery. */
@RunWith(AndroidJUnit4::class)
class CarTemplateTest {
    @Test fun englishTemplateKeepsTitleAndWorkingStartAction() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val config=android.content.res.Configuration(instrumentation.targetContext.resources.configuration).apply { setLocale(java.util.Locale.ENGLISH) }
            val localized=instrumentation.targetContext.createConfigurationContext(config)
            val context=TestCarContext.createCarContext(localized)
            val prefs=localized.getSharedPreferences("car_english_test",Context.MODE_PRIVATE)
            prefs.edit().clear().commit()
            try {
                val screen=OpenAutoScreen(context,ModelSelectionStore(prefs))
                val template=screen.onGetTemplate() as MessageTemplate
                @Suppress("DEPRECATION")
                assertEquals("OpenAuto AI",template.title.toString())
                assertEquals("Start",template.actions.single().title.toString())
                assertEquals("Ready for the UI test.\nChatGPT / OpenAI · GPT-5.4 mini",template.message.toString())
                template.actions.single().onClickDelegate!!.sendClick(object:OnDoneCallback {
                    override fun onSuccess(response:Bundleable?)=Unit
                    override fun onFailure(response:Bundleable) { throw AssertionError("Start failed") }
                })
                assertEquals("Started · Test 1\nChatGPT / OpenAI · GPT-5.4 mini",(screen.onGetTemplate() as MessageTemplate).message.toString())
            } finally { prefs.edit().clear().commit() }
        }
    }

    @Test fun observesPhoneSelectionWhileStartedAndRefreshesOnReturn() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val context = TestCarContext.createCarContext(instrumentation.targetContext)
            val prefs = instrumentation.targetContext.getSharedPreferences("car_lifecycle_test", Context.MODE_PRIVATE)
            prefs.edit().clear().commit()
            val screen = OpenAutoScreen(context, ModelSelectionStore(prefs))
            val controller = ScreenController(screen).moveToState(Lifecycle.State.RESUMED)
            try {
                val phoneStore = ModelSelectionStore(prefs)
                phoneStore.select(ModelSelection(Provider.CLAUDE, "claude-haiku-4-5"))
                assertEquals("Bereit für den UI-Test.\nClaude / Anthropic · Claude Haiku 4.5",
                    (controller.templatesReturned.last() as MessageTemplate).message.toString())
                controller.moveToState(Lifecycle.State.CREATED)
                val before = controller.templatesReturned.size
                phoneStore.select(ModelSelection(Provider.OPENAI, "gpt-5.4"))
                assertEquals(before, controller.templatesReturned.size)
                controller.moveToState(Lifecycle.State.RESUMED)
                assertEquals("Bereit für den UI-Test.\nChatGPT / OpenAI · GPT-5.4",
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
            val screen = OpenAutoScreen(context, store)
            val template = screen.onGetTemplate() as MessageTemplate
            @Suppress("DEPRECATION")
            assertEquals("OpenAuto AI", template.title.toString())
            assertEquals("Bereit für den UI-Test.\nChatGPT / OpenAI · GPT-5.4 mini", template.message.toString())
            assertEquals(1, template.actions.size)
            assertEquals("Starten", template.actions.single().title.toString())
            var error: Any? = null
            template.actions.single().onClickDelegate!!.sendClick(object : OnDoneCallback {
                override fun onSuccess(response: Bundleable?) = Unit
                override fun onFailure(response: Bundleable) { error = response }
            })
            assertNull(error)
            assertEquals("Gestartet · Test 1\nChatGPT / OpenAI · GPT-5.4 mini",
                (screen.onGetTemplate() as MessageTemplate).message.toString())
            store.select(ModelSelection(Provider.CLAUDE, "claude-opus-4-6"))
            assertEquals("Gestartet · Test 1\nClaude / Anthropic · Claude Opus 4.6",
                (screen.onGetTemplate() as MessageTemplate).message.toString())
            prefs.edit().clear().commit()
        }
    }
}

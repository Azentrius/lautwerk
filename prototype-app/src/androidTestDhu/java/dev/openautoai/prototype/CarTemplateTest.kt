package dev.openautoai.prototype

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
    @Test fun templateHasTitleAndWorkingStartAction() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val context = TestCarContext.createCarContext(instrumentation.targetContext)
            val screen = OpenAutoScreen(context)
            val template = screen.onGetTemplate() as MessageTemplate
            @Suppress("DEPRECATION")
            assertEquals("OpenAuto AI", template.title.toString())
            assertEquals("Bereit für den UI-Test.", template.message.toString())
            assertEquals(1, template.actions.size)
            assertEquals("Starten", template.actions.single().title.toString())
            var error: Any? = null
            template.actions.single().onClickDelegate!!.sendClick(object : OnDoneCallback {
                override fun onSuccess(response: Bundleable?) = Unit
                override fun onFailure(response: Bundleable) { error = response }
            })
            assertNull(error)
            assertEquals("Gestartet · Test 1", (screen.onGetTemplate() as MessageTemplate).message.toString())
        }
    }
}

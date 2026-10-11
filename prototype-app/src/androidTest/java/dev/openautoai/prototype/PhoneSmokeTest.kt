package dev.openautoai.prototype

import android.content.Context
import android.widget.Spinner
import androidx.test.platform.app.InstrumentationRegistry
import dev.openautoai.core.ai.ModelSelection
import dev.openautoai.core.ai.Provider
import dev.openautoai.settings.ModelSelectionStore
import org.junit.Before
import org.junit.After
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhoneSmokeTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val prefs = instrumentation.targetContext.getSharedPreferences("model_selection_v1", Context.MODE_PRIVATE)
    private var saved: Map<String, *> = emptyMap<String, Any>()

    @Before fun preserveSelection() {
        saved = prefs.all
        prefs.edit().clear().commit()
    }

    @After fun restoreSelection() {
        val editor = prefs.edit().clear()
        saved.forEach { (key, value) -> editor.putString(key, value as String) }
        editor.commit()
    }

    @Test fun choosesModelsRemembersEachProviderAndRestoresAfterRelaunch() {
        val store = ModelSelectionStore(instrumentation.targetContext)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.findViewById<Spinner>(R.id.model).setSelection(1) }
            instrumentation.waitForIdleSync()
            assertEquals("gpt-5.4", store.load().modelId)
            scenario.onActivity { it.findViewById<Spinner>(R.id.provider).setSelection(Provider.CLAUDE.ordinal) }
            instrumentation.waitForIdleSync()
            scenario.onActivity {
                assertEquals(3, it.findViewById<Spinner>(R.id.model).count)
                it.findViewById<Spinner>(R.id.model).setSelection(2)
            }
            instrumentation.waitForIdleSync()
            assertEquals(ModelSelection(Provider.CLAUDE, "claude-haiku-4-5"), store.load())
            scenario.onActivity { it.findViewById<Spinner>(R.id.provider).setSelection(Provider.OPENAI.ordinal) }
            instrumentation.waitForIdleSync()
            assertEquals("gpt-5.4", store.load().modelId)
            scenario.onActivity { it.findViewById<Spinner>(R.id.provider).setSelection(Provider.CLAUDE.ordinal) }
            instrumentation.waitForIdleSync()
            scenario.recreate()
            scenario.onActivity {
                assertEquals("Claude Haiku 4.5", it.findViewById<Spinner>(R.id.model).selectedItem)
                assertEquals(Provider.CLAUDE.ordinal, it.findViewById<Spinner>(R.id.provider).selectedItemPosition)
            }
        }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity {
                assertEquals("Claude Haiku 4.5", it.findViewById<Spinner>(R.id.model).selectedItem)
            }
        }
    }

    @Test fun startButtonAndActivityRecreation() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertEquals("Bereit für den UI-Test.", activity.findViewById<TextView>(R.id.status).text.toString())
                activity.findViewById<Button>(R.id.start).performClick()
                assertEquals("Gestartet · Test 1", activity.findViewById<TextView>(R.id.status).text.toString())
            }
            scenario.recreate()
            scenario.onActivity { activity ->
                assertEquals("Gestartet · Test 1", activity.findViewById<TextView>(R.id.status).text.toString())
            }
        }
    }
}

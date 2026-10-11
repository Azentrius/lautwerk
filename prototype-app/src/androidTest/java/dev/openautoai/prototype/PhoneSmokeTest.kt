package dev.openautoai.prototype

import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhoneSmokeTest {
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

package dev.openautoai.app.voice

import android.graphics.Bitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.openautoai.app.SpeechSettingsActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Opens the settings screen only; never changes saved choices or plays/records audio. */
@RunWith(AndroidJUnit4::class)
class SpeechSettingsScreenTest {
    @get:Rule val compose = createAndroidComposeRule<SpeechSettingsActivity>()

    @Test fun catalogLoadsAndPickerOpensWithoutStartingAPreview() {
        compose.waitUntil(15_000) {
            compose.onAllNodesWithTag("speech-voice-picker").fetchSemanticsNodes()
                .any { !it.config.contains(SemanticsProperties.Disabled) }
        }
        compose.onNodeWithText("Stimme und Tempo").assertIsDisplayed()
        compose.onNodeWithText("Hörprobe stoppen").assertIsNotEnabled()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
            File(instrumentation.targetContext.cacheDir, "speech-settings-test.png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            bitmap.recycle()
        }
        compose.onNodeWithTag("speech-voice-picker").performClick()
        assertTrue(compose.onAllNodesWithText("Auf dem Gerät", substring = true).fetchSemanticsNodes().isNotEmpty())
        compose.onNodeWithText("Hörprobe stoppen").assertIsNotEnabled()
    }
}

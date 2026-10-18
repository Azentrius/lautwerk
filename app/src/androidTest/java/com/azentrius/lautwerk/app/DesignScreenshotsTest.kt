// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.app

import android.graphics.Bitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.TextLayoutResult
import com.azentrius.lautwerk.app.design.AzDark
import com.azentrius.lautwerk.app.design.AzLight
import org.junit.Assert.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.azentrius.lautwerk.core.ai.*
import com.azentrius.lautwerk.core.i18n.AppLanguage
import org.junit.*
import java.io.File
import java.util.Locale

/** Captures only invented fixture content. Never opens the user's archive or starts audio. */
class DesignScreenshotsTest {
    @get:Rule val compose=createEmptyComposeRule()
    private val f=HomeTestFixture()
    private val old=AppLanguage.preferences
    private var scenario:ActivityScenario<LifecycleProbeActivity>?=null
    @After fun close() {
        scenario?.close();LifecycleProbeActivity.factory=null;LifecycleProbeActivity.dark=false;LifecycleProbeActivity.fontScale=1f
        LifecycleProbeActivity.locale=Locale.GERMAN;AppLanguage.preferences=old;f.close()
    }
    private fun launch(dark:Boolean,locale:Locale,fontScale:Float) {
        LifecycleProbeActivity.dark=dark;LifecycleProbeActivity.locale=locale;LifecycleProbeActivity.fontScale=fontScale
        AppLanguage.preferences={listOf(locale)}
        f.archive.items=listOf(Conversation("visual","Erfundener Design-Test",Provider.CLAUDE,ModelCatalog.default(Provider.CLAUDE),0,
            listOf(Turn("user","Was testen wir heute?"),Turn("assistant","Wir prüfen die Gestaltung mit erfundenen Nachrichten."))))
        LifecycleProbeActivity.factory={ HomeViewModel(f.selections,f.keys,f.archive,f.exports,f.findHistory).also { f.home=it } }
        scenario=ActivityScenario.launch(LifecycleProbeActivity::class.java)
        compose.waitUntil(5000) { !f.home.state.loading&&!f.home.state.historySearching }
    }
    private fun capture(name:String) {
        compose.waitForIdle()
        android.os.SystemClock.sleep(500) // Wait for the platform window transition before capturing pixels.
        val i=InstrumentationRegistry.getInstrumentation()
        i.uiAutomation.takeScreenshot()?.let { b->File(i.targetContext.cacheDir,name).outputStream().use { b.compress(Bitmap.CompressFormat.PNG,100,it) };b.recycle() }
    }
    private fun titleEvidence(dark: Boolean, locale: Locale, scale: Float, name: String) {
        launch(dark, locale, scale)
        val node = compose.onNodeWithTag("app-title").assertIsDisplayed()
        compose.onAllNodesWithText("Lautwerk.").assertCountEquals(1)
        val config = node.fetchSemanticsNode().config
        val text = config[SemanticsProperties.Text].single()
        assertEquals("Lautwerk.", text.text)
        assertEquals(listOf("Lautwerk"), config[SemanticsProperties.ContentDescription])
        val dot = text.spanStyles.single()
        assertEquals(".", text.text.substring(dot.start, dot.end))
        assertNull("The dot keeps the title font", dot.item.fontFamily)
        assertNull(dot.item.fontStyle)
        assertNull(dot.item.fontWeight)
        assertEquals((if (dark) AzDark else AzLight)["signature"], dot.item.color)
        val results = mutableListOf<TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        val layout = results.single()
        assertFalse("The complete app title must fit its width", layout.didOverflowWidth)
        assertFalse("The title must grow with the system font", layout.didOverflowHeight)
        assertEquals(text.length, layout.getLineEnd(layout.lineCount - 1, visibleEnd = true))
        capture(name)
    }
    @Test fun titleLightGerman() = titleEvidence(false, Locale.GERMANY, 1f, "title-light-100.png")
    @Test fun titleDarkEnglish() = titleEvidence(true, Locale.ENGLISH, 1f, "title-dark-100.png")
    @Test fun titleLightGermanAtTwoHundredPercent() = titleEvidence(false, Locale.GERMANY, 2f, "title-light-200.png")
    @Test fun titleDarkEnglishAtTwoHundredPercent() = titleEvidence(true, Locale.ENGLISH, 2f, "title-dark-200.png")

    @Test fun lightGermanPhone() {
        launch(false,Locale.GERMANY,1f)
        compose.onNodeWithTag("page-history").performClick();capture("az-light-history.png")
        compose.onNodeWithTag("history-visual").performClick();capture("az-light-conversation.png")
        compose.onNodeWithTag("page-settings").performClick();capture("az-light-settings.png")
    }
    @Test fun darkEnglishPhone() {
        launch(true,Locale.ENGLISH,1f)
        compose.onNodeWithTag("page-history").performClick();compose.onNodeWithTag("history-visual").performClick()
        compose.onNodeWithTag("conversation-start").assertIsDisplayed();capture("az-dark-conversation.png")
        compose.onNodeWithTag("export-chat").performClick();capture("az-dark-export.png")
        compose.onNodeWithText("Cancel").performClick()
    }
    @Test fun largeGermanTextKeepsStartAndNavigationReachable() {
        launch(false,Locale.GERMANY,2f)
        compose.onNodeWithTag("page-history").assertIsDisplayed().performClick()
        compose.onNodeWithTag("conversation-start").assertIsDisplayed();capture("az-large-history.png")
        compose.onNodeWithTag("page-settings").performClick()
        compose.onNodeWithTag("home-list").performScrollToNode(hasTestTag("api-key-input"))
        compose.onNodeWithTag("api-key-input").assertIsDisplayed();capture("az-large-settings.png")
    }
}

// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.app

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import de.azentrius.lautwerk.core.ai.*
import de.azentrius.lautwerk.core.i18n.AppLanguage
import org.junit.*
import org.junit.Assert.*
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit

class LocalizationLifecycleTest {
    @get:Rule val compose=createEmptyComposeRule()
    private val f=HomeTestFixture()
    private val old=AppLanguage.preferences
    private var scenario:ActivityScenario<LifecycleProbeActivity>?=null
    @Before fun setup() {
        LifecycleProbeActivity.locale=Locale.GERMAN;LifecycleProbeActivity.starts=0
        AppLanguage.preferences={listOf(LifecycleProbeActivity.locale)}
        LifecycleProbeActivity.factory={ HomeViewModel(f.selections,f.keys,f.archive,f.exports,f.findHistory).also { f.home=it } }
    }
    @After fun cleanup() {
        scenario?.close();LifecycleProbeActivity.factory=null;AppLanguage.preferences=old;f.close()
    }
    private fun launch() {
        scenario=ActivityScenario.launch(LifecycleProbeActivity::class.java)
        ready()
    }
    private fun ready() { compose.waitUntil(5000) { !f.home.state.loading && !f.home.state.historySearching } }
    private fun fake()=Conversation("fake","Erfundener Chat",Provider.CLAUDE,ModelCatalog.default(Provider.CLAUDE),0,
        (0 until 100).map { Turn("user",if(it==4) "Suchfund: nur Testdaten" else "Test $it") })
    private fun search() {
        compose.onNodeWithTag("page-history").performClick()
        compose.onNodeWithTag("history-search").performTextInput("Suchfund")
        compose.onNodeWithTag("history-search").performImeAction();ready()
        compose.onNodeWithTag("history-filter-CLAUDE").performClick();ready()
    }
    @Test fun recreationAndLanguageChangeRetainSearchFilterAndDoNotStartConversation() {
        f.archive.items=listOf(fake());launch();search()
        val retained=f.home
        scenario!!.recreate();ready();assertSame(retained,f.home)
        compose.onNodeWithTag("history-search").assertTextContains("Suchfund")
        assertEquals(Provider.CLAUDE,f.home.state.historyProvider)
        LifecycleProbeActivity.locale=Locale.ENGLISH;scenario!!.recreate();ready()
        compose.onNodeWithText("History").assertIsDisplayed()
        compose.onNodeWithText("Start conversation").assertIsDisplayed()
        compose.onNodeWithTag("history-search").assertTextContains("Suchfund")
        compose.onNodeWithText("Reset search").assertIsDisplayed()
        screenshot("localization-en.png")
        LifecycleProbeActivity.locale=Locale.GERMAN;scenario!!.recreate();ready()
        compose.onNodeWithText("Gespräch starten").assertIsDisplayed();screenshot("localization-de.png")
        assertEquals(0,LifecycleProbeActivity.starts);assertEquals(0,f.keys.reads.get())
    }
    @Test fun selectedOlderMessageAndScrollSurviveRecreation() {
        f.archive.items=listOf(fake());launch();search()
        compose.onNodeWithTag("home-list").performScrollToNode(hasTestTag("history-fake"))
        compose.onNodeWithTag("history-fake").performClick()
        compose.waitUntil(5000) { f.home.state.historyFocus==null }
        compose.onNodeWithTag("chat-turn-4").assertIsDisplayed()
        scenario!!.recreate();ready()
        assertEquals("fake",f.home.state.selectedId)
        compose.onNodeWithTag("chat-turn-4").assertIsDisplayed()
        assertEquals(0,LifecycleProbeActivity.starts)
    }
    @Test fun slowStartCannotLaunchAfterBackgroundAndFreshStartRunsOnce() {
        launch();val gate=HomeTestFixture.Gate();f.keys.beforeRead={gate.block()}
        try {
            compose.onNodeWithTag("conversation-start").performClick()
            assertTrue(gate.entered.await(3,TimeUnit.SECONDS))
            scenario!!.moveToState(Lifecycle.State.CREATED);gate.open()
            scenario!!.moveToState(Lifecycle.State.RESUMED);ready()
            assertEquals(0,LifecycleProbeActivity.starts)
            f.keys.beforeRead=null
            compose.onNodeWithTag("conversation-start").performClick()
            compose.waitUntil(5000) { LifecycleProbeActivity.starts==1 }
            scenario!!.recreate();ready();assertEquals(1,LifecycleProbeActivity.starts)
        } finally { gate.open() }
    }
    @Test fun englishAndFallbackResourcesCoverSafetyDialogsAndLabels() {
        val base=InstrumentationRegistry.getInstrumentation().targetContext
        for(locale in listOf(Locale.ENGLISH,Locale.JAPANESE)) {
            val context=base.createConfigurationContext(Configuration(base.resources.configuration).apply { setLocale(locale) })
            assertEquals("Conversation",context.getString(R.string.conversation))
            assertEquals("Start conversation",context.getString(R.string.conversation_start))
            assertTrue(context.getString(R.string.export_warning).contains("unencrypted"))
            assertEquals("Voice and speed",context.getString(R.string.voice_settings))
        }
    }
    private fun screenshot(name:String) {
        val i=InstrumentationRegistry.getInstrumentation();i.waitForIdleSync()
        i.uiAutomation.takeScreenshot()?.let { image ->
            File(i.targetContext.cacheDir,name).outputStream().use { image.compress(Bitmap.CompressFormat.PNG,100,it) }
            image.recycle()
        }
    }
}

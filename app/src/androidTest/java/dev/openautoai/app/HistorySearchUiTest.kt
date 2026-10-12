package dev.openautoai.app

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.openautoai.core.ai.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class HistorySearchUiTest {
    @get:Rule val compose = createComposeRule()
    private val f = HomeTestFixture()
    private var starts = 0
    @After fun cleanup() { f.close() }
    private fun render(chats: List<Conversation>) {
        f.archive.items = chats; f.create()
        compose.setContent { MaterialTheme { MainContent(f.home, { starts++ }, {
            AppState.update(false,AppState.provider,AppState.modelId,AppState.sessionId,"Bereit")
        }) } }
        ready(); compose.onNodeWithTag("page-history").performClick()
    }
    private fun ready() { compose.waitUntil(5000) { !f.home.state.loading && !f.home.state.historySearching } }
    private fun chat(id: String, provider: Provider = Provider.OPENAI, count: Int = 0, hit: Int = -1) =
        Conversation(id,"Erfundenes Gespräch $id",provider,ModelCatalog.default(provider),0,
            (0 until count).map { Turn(if (it % 2 == 0) "user" else "assistant", if (it == hit) "Besonderer Suchfund $it" else "Nachricht $it") })
    private fun query(text: String) {
        compose.onNodeWithTag("home-list").performScrollToNode(hasTestTag("history-search"))
        compose.onNodeWithTag("history-search").performTextReplacement(text)
        compose.onNodeWithTag("history-search").performImeAction()
        ready()
    }
    private fun scroll(tag: String) { compose.onNodeWithTag("home-list").performScrollToNode(hasTestTag(tag)) }

    @Test fun searchAndProviderFilterShowCountsAndResetRestoresHistoryWithoutStartingVoice() {
        render(listOf(chat("a"),chat("b",Provider.CLAUDE)))
        query("Gespräch")
        compose.onNodeWithTag("history-filter-CLAUDE").performClick(); ready()
        scroll("history-count"); compose.onNodeWithText("1 von 2 Gesprächen").assertIsDisplayed()
        scroll("history-b"); compose.onNodeWithTag("history-b").assertIsDisplayed()
        compose.onNodeWithTag("history-a").assertDoesNotExist()
        query("unauffindbar")
        scroll("history-count"); compose.onNodeWithText("Keine passenden Gespräche. Suche ändern oder zurücksetzen.").assertIsDisplayed()
        scroll("history-search"); compose.onNodeWithText("Suche zurücksetzen").performClick(); ready()
        scroll("history-count"); compose.onNodeWithText("2 von 2 Gesprächen").assertIsDisplayed()
        assertEquals(0,starts); assertEquals(0,f.keys.reads.get())
    }

    @Test fun openingOldMessageHitExpandsHistoryAndJumpsDirectlyToMessage() {
        render(listOf(chat("long",count=140,hit=3)))
        query("Suchfund")
        scroll("history-long"); compose.onNodeWithText("Treffer in Nachricht 4").assertIsDisplayed()
        compose.onNodeWithTag("history-long").performClick()
        compose.waitUntil(5000) { f.home.state.historyFocus == null }
        compose.onNodeWithTag("chat-turn-3").assertIsDisplayed()
        compose.onNodeWithTag("conversation-start").assertIsDisplayed()
        assertEquals(140,f.archive.items.single().turns.size); assertEquals(0,starts)
        compose.onNodeWithTag("page-history").performClick(); ready()
        compose.onNodeWithTag("history-search").assertTextContains("Suchfund")
    }

    @Test fun hitNearEndInRetiredModelKeepsReadOnlyStartAndVisibleMessage() {
        render(listOf(chat("old",Provider.CLAUDE,count=100,hit=95).copy(modelId="retired-model")))
        query("Suchfund"); scroll("history-old"); compose.onNodeWithTag("history-old").performClick()
        compose.waitUntil(5000) { f.home.state.historyFocus == null }
        compose.onNodeWithTag("chat-turn-95").assertIsDisplayed()
        compose.onNodeWithTag("conversation-start").assertIsNotEnabled()
        assertEquals(0,starts)
    }

    @Test fun filteringDuringVoiceLeavesStopVisibleAndCannotSwitchChat() {
        val a=chat("a"); val b=chat("b",Provider.CLAUDE)
        render(listOf(a,b))
        compose.runOnIdle { AppState.update(true,a.provider,a.modelId,a.id,"Test") }
        ready(); query("Gespräch")
        compose.onNodeWithTag("history-filter-CLAUDE").performClick(); ready()
        scroll("history-b"); compose.onNodeWithTag("history-b").assertIsNotEnabled()
        compose.onNodeWithTag("conversation-stop").assertIsDisplayed().performClick()
        assertFalse(AppState.active); assertEquals(0,starts)
    }

    @Test fun searchViewCanBeReviewedWithInventedConversationsOnly() {
        render(listOf(chat("Urlaub",Provider.CLAUDE,count=4,hit=2),chat("Ideen")))
        query("Suchfund")
        compose.onNodeWithTag("history-filter-CLAUDE").performClick(); ready()
        compose.onNodeWithTag("conversation-start").assertIsDisplayed()
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync(); Thread.sleep(500)
        instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
            File(instrumentation.targetContext.cacheDir,"history-search-test.png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG,100,it)
            }
            bitmap.recycle()
        }
        assertEquals(0,starts)
    }
}

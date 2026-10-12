package dev.openautoai.app

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.openautoai.core.ai.*
import dev.openautoai.core.storage.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.TimeUnit

/** Isolated preferences and invented chats/keys only. Never starts the microphone/service/API. */
@RunWith(AndroidJUnit4::class)
class StorageUiTest {
    @get:Rule val compose = createComposeRule()
    private val f = HomeTestFixture()
    private var starts = 0
    private var stops = 0
    private var exportPicks = 0
    @After fun tearDown() { f.close() }
    private fun render() {
        f.create()
        compose.setContent { MaterialTheme { MainContent(f.home, { starts++ }, {
            stops++
            AppState.update(false, AppState.provider, AppState.modelId, AppState.sessionId, "Gespräch beendet")
        }, onExportDocument = { exportPicks++ }) } }
        compose.waitUntil(5000) { !f.home.state.loading && !f.home.state.historySearching }
    }
    private fun page(name: String) { compose.onNodeWithTag("page-$name").performClick() }
    private fun scroll(text: String) { compose.onNodeWithTag("home-list").performScrollToNode(hasText(text)) }
    private fun chat(id: String, turns: List<Turn> = emptyList()) =
        Conversation(id, "Testgespräch $id", Provider.CLAUDE, ModelCatalog.default(Provider.CLAUDE), 0, turns)

    @Test fun unreadableArchiveShowsRetryAndDisablesStartUntilRecovery() {
        f.archive.failRead = true; render()
        compose.onNodeWithTag("conversation-start").assertIsNotEnabled()
        compose.onNodeWithText(StorageError.ARCHIVE_READ.userMessage).assertExists()
        f.archive.failRead = false
        compose.onNodeWithText("Verlauf erneut laden").performClick()
        compose.waitUntil(5000) { !f.home.state.loading && f.home.state.archiveError == null }
        compose.onNodeWithTag("conversation-start").assertIsEnabled()
        assertEquals(0, starts); assertEquals(0, f.archive.creates.get())
    }

    @Test fun keyReadAndClearErrorsStayInUiAndDoNotStartVoice() {
        f.keys.failRead = true; f.keys.failClear = true; render()
        compose.onNodeWithTag("conversation-start").performClick()
        compose.waitUntil(5000) { f.home.state.message == StorageError.CREDENTIAL_READ.userMessage }
        compose.onNodeWithText(StorageError.CREDENTIAL_READ.userMessage).assertExists()
        page("Einstellungen"); scroll("Entfernen")
        compose.onNodeWithText("Entfernen").performClick()
        compose.waitUntil(5000) { f.home.state.message == StorageError.CREDENTIAL_WRITE.userMessage }
        compose.onNodeWithText(StorageError.CREDENTIAL_WRITE.userMessage).assertExists()
        assertEquals(0, starts); assertEquals(0, f.archive.creates.get())
    }

    @Test fun retiredModelHistoryRemainsReadableButCannotBeStarted() {
        f.archive.items = listOf(chat("old", listOf(Turn("user", "Erhaltene Nachricht"))).copy(modelId = "retired-model"))
        render(); page("Verlauf")
        compose.onNodeWithTag("history-old").performClick()
        scroll("Du: Erhaltene Nachricht")
        compose.onNodeWithText("Du: Erhaltene Nachricht").assertExists()
        compose.onNodeWithText("Gespräch fortsetzen").assertIsNotEnabled()
        assertEquals(0, starts)
    }

    @Test fun longHistoryAndOlderMessagesRemainReachableWithStartAlwaysVisible() {
        val turns = (0..119).map { Turn("user", "Testnachricht $it") }
        f.archive.items = (0..199).map { chat(it.toString(), if (it == 199) turns else emptyList()) }
        render(); page("Verlauf")
        compose.onNodeWithTag("home-list").performScrollToNode(hasTestTag("history-199"))
        compose.onNodeWithTag("conversation-start").assertIsDisplayed()
        compose.onNodeWithTag("history-199").performClick()
        assertEquals(Provider.CLAUDE, f.home.state.selection.provider)
        assertEquals("199", f.home.state.selectedId)
        scroll("Du: Testnachricht 119")
        compose.onNodeWithText("Du: Testnachricht 119").assertIsDisplayed()
        compose.onNodeWithTag("conversation-start").assertIsDisplayed()
        scroll("Ältere Nachrichten anzeigen"); compose.onNodeWithText("Ältere Nachrichten anzeigen").performClick()
        scroll("Ältere Nachrichten anzeigen"); compose.onNodeWithText("Ältere Nachrichten anzeigen").performClick()
        scroll("Du: Testnachricht 0"); compose.onNodeWithText("Du: Testnachricht 0").assertIsDisplayed()
        assertEquals(120, f.archive.items.last().turns.size)
    }

    @Test fun stopRemainsVisibleWhileScrollingAndSettingsCannotMutateActiveSession() {
        f.archive.items = (0..99).map { chat(it.toString()) }; render()
        compose.runOnIdle { AppState.update(true, Provider.CLAUDE, ModelCatalog.default(Provider.CLAUDE), "0", "Ich höre zu") }
        page("Verlauf")
        compose.onNodeWithTag("home-list").performScrollToNode(hasTestTag("history-99"))
        compose.onNodeWithTag("conversation-stop").assertIsDisplayed()
        page("Einstellungen"); scroll("Entfernen")
        compose.onNodeWithText("Entfernen").assertIsNotEnabled()
        compose.onNodeWithTag("conversation-stop").assertIsDisplayed().performClick()
        assertEquals(1, stops)
        compose.onNodeWithTag("conversation-start").assertIsDisplayed()
        assertEquals(0, f.keys.clears.get())
    }

    @Test fun slowKeyWriteDoesNotBlockNavigationAndCannotBeSubmittedTwice() {
        val gate = HomeTestFixture.Gate(); f.keys.beforeSave = gate::block
        try {
            render(); page("Einstellungen"); compose.onNodeWithTag("home-list").performScrollToNode(hasTestTag("api-key-input"))
            compose.onNodeWithTag("api-key-input").performTextInput("dummy-new-key")
            scroll("Schlüssel speichern"); compose.onNodeWithText("Schlüssel speichern").performClick()
            assertTrue(gate.entered.await(3, TimeUnit.SECONDS))
            compose.onNodeWithText("Schlüssel speichern").assertIsNotEnabled()
            page("Verlauf"); compose.onNodeWithText("Noch keine gespeicherten Gespräche.").assertExists()
            compose.onNodeWithTag("conversation-start").assertIsNotEnabled()
            gate.open()
            compose.waitUntil(5000) { f.home.state.operation == null && !f.home.state.loading }
            assertEquals(1, f.keys.saves.get())
            assertEquals(Provider.OPENAI, f.keys.savedProvider)
        } finally { gate.open() }
    }

    @Test fun pendingStartCanBeCancelledBeforeSlowCredentialReadReturns() {
        val gate = HomeTestFixture.Gate(); f.keys.beforeRead = gate::block
        try {
            render(); compose.onNodeWithTag("conversation-start").performClick()
            assertTrue(gate.entered.await(3, TimeUnit.SECONDS))
            compose.onNodeWithTag("start-cancel").assertIsDisplayed().performClick()
            gate.open()
            compose.waitUntil(5000) { !f.home.state.loading && f.home.state.operation == null }
            compose.onNodeWithTag("conversation-start").assertIsDisplayed().assertIsEnabled()
            assertEquals(0, starts); assertEquals(0, f.archive.creates.get())
        } finally { gate.open() }
    }

    @Test fun deleteRequiresConfirmationAndCancellingKeepsTheChat() {
        f.archive.items = listOf(chat("keep")); render(); page("Verlauf")
        compose.onNodeWithTag("history-keep").performClick(); scroll("Diesen Chat löschen")
        compose.onNodeWithText("Diesen Chat löschen").performClick()
        compose.onNodeWithText("Abbrechen").performClick()
        assertEquals(0, f.archive.deletes.get())
        compose.onNodeWithText("Diesen Chat löschen").performClick()
        compose.onNodeWithText("Löschen", useUnmergedTree = true).performClick()
        compose.waitUntil(5000) { !f.home.state.loading && f.home.state.selectedId == null }
        assertEquals(1, f.archive.deletes.get())
        assertTrue(f.archive.items.isEmpty()); assertEquals(0, starts)
    }

    @Test fun exportDisclosureCanCancelAndConfirmedExportRequestsOnePickerOnly() {
        f.archive.items = listOf(chat("export")); render(); page("Verlauf")
        compose.onNodeWithTag("history-export").performClick(); scroll("Als Textdatei exportieren")
        compose.onNodeWithTag("export-chat").performClick()
        compose.onNodeWithText("Chat als Textdatei exportieren?").assertIsDisplayed()
        compose.onNodeWithText("unverschlüsselten Text", substring = true).assertExists()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        Thread.sleep(500) // Allow the platform dialog-window animation to finish before capture.
        instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
            File(instrumentation.targetContext.cacheDir, "export-disclosure-test.png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            bitmap.recycle()
        }
        compose.onNodeWithText("Abbrechen").performClick()
        assertEquals(0, exportPicks); assertEquals(0, f.exports.writes.get())
        compose.onNodeWithTag("export-chat").performClick()
        compose.onNodeWithText("Speicherort wählen").performClick()
        compose.waitUntil(5000) { exportPicks == 1 }
        page("Einstellungen"); page("Gespräch")
        assertEquals(1, exportPicks); assertEquals(0, f.keys.reads.get())
        compose.runOnIdle { f.home.finishExportSelection(null) }
        compose.waitUntil(5000) { !f.home.state.loading }
        compose.onNodeWithTag("conversation-start").assertIsEnabled()
        assertEquals(0, starts); assertEquals(0, f.exports.writes.get())
    }

    @Test fun exportButtonIsDisabledDuringVoiceAndRetiredChatsCanStillExport() {
        val old = chat("old").copy(modelId = "retired")
        f.archive.items = listOf(old); render(); page("Verlauf")
        compose.onNodeWithTag("history-old").performClick(); scroll("Als Textdatei exportieren")
        compose.onNodeWithTag("conversation-start").assertIsNotEnabled()
        compose.onNodeWithTag("export-chat").assertIsEnabled()
        compose.runOnIdle { AppState.update(true, old.provider, old.modelId, old.id, "Test") }
        scroll("Als Textdatei exportieren")
        compose.onNodeWithTag("export-chat").assertIsNotEnabled()
        compose.onNodeWithTag("conversation-stop").assertIsDisplayed()
        assertEquals(0, exportPicks)
    }

    @Test fun conversationScreenShowsControlsAndCanBeReviewedWithInventedDataOnly() {
        f.archive.items = listOf(chat("preview", listOf(Turn("user", "Was testen wir heute?"),
            Turn("assistant", "Wir prüfen die neue Gesprächsansicht und ihre Bedienung."))))
        render(); page("Verlauf"); compose.onNodeWithTag("history-preview").performClick()
        compose.onNodeWithTag("conversation-start").assertIsDisplayed()
        compose.onNodeWithText("Stimme und Tempo").assertIsDisplayed()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
            File(instrumentation.targetContext.cacheDir, "home-screen-test.png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            bitmap.recycle()
        }
    }
}

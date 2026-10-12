package dev.openautoai.app

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.openautoai.core.ai.*
import dev.openautoai.core.storage.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ChatExportTest {
    private val f = HomeTestFixture()
    private val uri = Uri.parse("content://invented.test/output")
    private fun chat(id: String = "a", provider: Provider = Provider.OPENAI) = Conversation(id, "Fiktiv", provider,
        "retired-model", 0, (0..119).map { Turn("user", "Erfundene Nachricht $it") })
    private fun open(c: Conversation = chat()) {
        f.archive.items = listOf(c); f.create(); f.await { !f.home.state.loading }
        f.main { f.home.openChat(c.id) }
    }
    private fun prepare(id: String = "a") {
        f.main { assertTrue(f.home.prepareExport(id)) }
        f.await { f.home.state.exportPickerPending }
        assertEquals(ExportPresentation.PREPARING,f.home.state.exportPresentation)
    }
    @After fun cleanup() { f.close() }

    @Test fun bothProvidersExportAllTurnsOnceWithoutReadingKeysOrChangingArchive() {
        open()
        for (provider in Provider.entries) {
            val c = chat(provider.name, provider); f.archive.items = listOf(c)
            f.main { f.home.refresh() }; f.await { !f.home.state.loading }
            f.main { f.home.openChat(c.id) }; prepare(c.id)
            f.main {
                assertEquals(ConversationTextExport.FILE_NAME, f.home.takeExportFileName())
                assertNull(f.home.takeExportFileName())
                assertFalse(f.home.prepareStart()); assertFalse(f.home.prepareExport(c.id))
                assertFalse(f.home.deleteSelected()); assertFalse(f.home.saveKey("dummy"))
                f.home.finishExportSelection(uri)
                f.home.finishExportSelection(uri) // Duplicate result cannot write twice.
            }
            f.await { f.home.state.operation == null && !f.home.state.loading }
            assertEquals(c, f.exports.chat); assertEquals(listOf(c), f.archive.items)
            assertTrue(f.home.state.message!!.startsWith("Textdatei gespeichert"))
            assertEquals(ExportPresentation.DONE,f.home.state.exportPresentation)
        }
        assertEquals(2, f.exports.writes.get()); assertEquals(0, f.keys.reads.get())
        assertEquals(0, f.archive.creates.get()); assertEquals(0, f.archive.deletes.get())
    }

    @Test fun pickerCancellationAndLaunchFailureReleaseUiWithoutWritingAnything() {
        open(); prepare()
        f.main { f.home.takeExportFileName(); f.home.finishExportSelection(null) }
        f.await { !f.home.state.loading }
        assertEquals("Export abgebrochen", f.home.state.message)
        assertEquals(ExportPresentation.CANCELLED,f.home.state.exportPresentation)
        prepare(); f.main { f.home.exportPickerFailed() }; f.await { !f.home.state.loading }
        assertNull(f.home.state.operation); assertFalse(f.home.state.exportPickerPending)
        assertEquals(0, f.exports.writes.get()); assertEquals(0, f.keys.reads.get())
    }

    @Test fun slowWriteReportsSuccessOnlyAfterCompletionAndCapturesExactSnapshot() {
        val gate = HomeTestFixture.Gate(); f.exports.beforeWrite = gate::block
        try {
            val original = chat(); open(original); prepare()
            f.main { f.home.takeExportFileName(); f.home.finishExportSelection(uri) }
            assertTrue(gate.entered.await(3, TimeUnit.SECONDS))
            f.archive.items = listOf(original.copy(title = "Later"))
            f.main { assertNull(f.home.state.message); f.home.newChat(); assertFalse(f.home.clearKey()) }
            assertEquals(HomeOperation.WRITE_EXPORT, f.home.state.operation)
            assertEquals(ExportPresentation.WRITING,f.home.state.exportPresentation)
            gate.open(); f.await { f.home.state.operation == null && !f.home.state.loading }
            assertEquals(original, f.exports.chat)
        } finally { gate.open() }
    }

    @Test fun arbitraryProviderErrorsNeverLeakPathsOrChatTextAndRetryWorks() {
        open(); f.exports.failure = IOException("private-path-and-chat")
        prepare(); f.main { f.home.finishExportSelection(uri) }
        f.await { f.home.state.operation == null && !f.home.state.loading }
        assertEquals(HomeViewModel.EXPORT_FAILED, f.home.state.message)
        assertEquals(ExportPresentation.FAILED,f.home.state.exportPresentation)
        f.exports.failure = null; prepare(); f.main { f.home.finishExportSelection(uri) }
        f.await { f.home.state.operation == null && !f.home.state.loading }
        assertTrue(f.home.state.message!!.startsWith("Textdatei gespeichert"))
            assertEquals(ExportPresentation.DONE,f.home.state.exportPresentation)
    }

    @Test fun staleConfirmationAndMissingChatCannotExportAnotherChat() {
        open(); f.archive.items = listOf(chat(), chat("b"))
        f.main { f.home.refresh() }; f.await { !f.home.state.loading }
        f.main { f.home.openChat("b"); assertFalse(f.home.prepareExport("a")) }
        f.archive.items = emptyList()
        f.main { assertTrue(f.home.prepareExport("b")) }
        f.await { f.home.state.operation == null && !f.home.state.loading }
        assertEquals(StorageError.CONVERSATION_MISSING.userMessage, f.home.state.message)
        assertFalse(f.home.state.exportPickerPending); assertEquals(0, f.exports.writes.get())
    }

    @Test fun cancelledSlowPreparationNeverOpensPickerOrWrites() {
        val gate = HomeTestFixture.Gate()
        try {
            open(); f.archive.beforeGet = gate::block
            f.main { f.home.prepareExport("a") }; assertTrue(gate.entered.await(3, TimeUnit.SECONDS))
            f.main { f.home.cancelExportPreparation() }; gate.open()
            f.await { f.home.state.operation == null && !f.home.state.loading }
            assertFalse(f.home.state.exportPickerPending); assertNull(f.home.takeExportFileName())
            assertEquals(0, f.exports.writes.get())
        } finally { gate.open() }
    }

    @Test fun resultWithoutRetainedExportAfterProcessLossCannotWriteSelectedOrNewChat() {
        open(); f.main { f.home.finishExportSelection(uri) }
        assertEquals(HomeViewModel.EXPORT_INTERRUPTED, f.home.state.message)
        assertEquals(0, f.exports.writes.get()); assertEquals(0, f.keys.reads.get())
    }

    @Test fun activeVoiceAndUnreadableArchiveBlockExport() {
        open()
        f.main {
            AppState.update(true, Provider.OPENAI, "model", "a", "Test")
            assertFalse(f.home.prepareExport("a"))
            AppState.update(false, Provider.OPENAI, "model", "a", "Bereit")
            f.archive.failRead = true; f.home.refresh()
        }
        f.await { !f.home.state.loading }
        f.main { assertFalse(f.home.prepareExport("a")) }
        assertEquals(0, f.exports.writes.get())
    }
}

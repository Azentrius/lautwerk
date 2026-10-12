// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.app

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.openautoai.core.ai.*
import dev.openautoai.core.storage.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.*

@RunWith(AndroidJUnit4::class)
class DocumentConversationExporterTest {
    private val uri = Uri.parse("content://invented.test/output")
    private val chat = Conversation("a", "Test ä", Provider.CLAUDE, "model", 0,
        listOf(Turn("user", "Nur erfundener Text 🌍")))

    @Test fun androidCreateDocumentContractUsesTextMimeAndSafeFilenameAndHandlesCancel() {
        val contract = CreateChatDocument()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = contract.createIntent(context, ConversationTextExport.FILE_NAME)
        assertEquals(Intent.ACTION_CREATE_DOCUMENT, intent.action)
        assertEquals("text/plain", intent.type); assertTrue(intent.hasCategory(Intent.CATEGORY_OPENABLE))
        assertEquals("OpenAutoAI-Chat.txt", intent.getStringExtra(Intent.EXTRA_TITLE))
        assertNull(contract.parseResult(android.app.Activity.RESULT_CANCELED, Intent().setData(uri)))
        assertEquals(uri, contract.parseResult(android.app.Activity.RESULT_OK, Intent().setData(uri)))
    }

    @Test fun writesUtf8AndClosesActualTestFileWithoutTouchingArchiveOrCredentials() {
        val file = File.createTempFile("export-test-", ".txt", InstrumentationRegistry.getInstrumentation().targetContext.cacheDir)
        var closed = false
        try {
            val exporter = DocumentConversationExporter { destination ->
                assertEquals(uri, destination)
                object : FilterOutputStream(file.outputStream()) {
                    override fun close() { super.close(); closed = true }
                }
            }
            exporter.write(uri, chat, RequestCancellation.NONE)
            assertTrue(closed)
            assertTrue(file.readText(Charsets.UTF_8).contains(chat.turns.single().content))
        } finally { file.delete() }
    }

    @Test fun nullStreamOpenWriteFlushAndCloseFailuresPropagate() {
        assertThrows(IOException::class.java) { DocumentConversationExporter { null }.write(uri, chat, RequestCancellation.NONE) }
        assertThrows(SecurityException::class.java) { DocumentConversationExporter { throw SecurityException("denied") }.write(uri, chat, RequestCancellation.NONE) }
        for (stage in listOf("write", "flush", "close")) {
            var closed = false
            val exporter = DocumentConversationExporter {
                object : OutputStream() {
                    override fun write(b: Int) { if (stage == "write") throw IOException("write") }
                    override fun flush() { if (stage == "flush") throw IOException("flush") }
                    override fun close() { closed = true; if (stage == "close") throw IOException("close") }
                }
            }
            assertThrows(IOException::class.java) { exporter.write(uri, chat, RequestCancellation.NONE) }
            assertTrue(closed)
        }
    }

    @Test fun cancellationAndNonContentUriCannotOpenAnyDestination() {
        var opened = 0
        val exporter = DocumentConversationExporter { opened++; ByteArrayOutputStream() }
        assertThrows(ChatFailure::class.java) { exporter.write(uri, chat, CancellationSource().apply { cancel() }) }
        assertThrows(IllegalArgumentException::class.java) { exporter.write(Uri.parse("file:///invented"), chat, RequestCancellation.NONE) }
        assertEquals(0, opened)
    }
}

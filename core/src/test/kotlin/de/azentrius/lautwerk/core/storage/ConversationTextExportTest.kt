// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.core.storage

import de.azentrius.lautwerk.core.ai.*
import org.junit.Assert.*
import org.junit.Test
import java.io.StringWriter
import java.io.Writer
import java.io.IOException

class ConversationTextExportTest {
    private fun chat(provider: Provider = Provider.OPENAI) = Conversation("internal-id", "Grüße 🦊\nZweite Zeile",
        provider, "retired-model", 0, listOf(Turn("user", "Hallo\n<script>nur Text</script>\tä"),
            Turn("assistant", "Antwort 🌍\r\nMehr Text")))

    @Test fun bothProvidersKeepUnicodeContentOrderAndMetadataWithoutInternalId() {
        for (provider in Provider.entries) {
            val c = chat(provider); val out = StringWriter()
            ConversationTextExport.write(c, out)
            val text = out.toString()
            assertTrue(text.contains(c.title)); assertTrue(text.contains(provider.displayName))
            assertTrue(text.contains("Modell: retired-model")); assertTrue(text.contains("1970-01-01T00:00:00Z"))
            assertTrue(text.contains("Nachrichten: 2"))
            assertTrue(text.contains("Nachricht 1 · Du")); assertTrue(text.contains("Nachricht 2 · KI"))
            assertTrue(text.indexOf(c.turns[0].content) < text.indexOf(c.turns[1].content))
            assertFalse(text.contains(c.id))
        }
    }

    @Test fun exportsEveryMessageBeyondTheFiftyMessageUiWindowAndDoesNotModifyChat() {
        val c = chat().copy(turns = (0..120).map { Turn("user", "Eindeutiger Inhalt [$it]") })
        val out = StringWriter(); ConversationTextExport.write(c, out)
        c.turns.forEach { assertTrue(out.toString().contains(it.content)) }
        assertEquals(121, c.turns.size); assertTrue(out.toString().contains("Nachricht 121 · Du"))
    }

    @Test fun emptyConversationStillHasAUsefulHeaderAndFileNameContainsNoChatData() {
        val out = StringWriter(); ConversationTextExport.write(chat().copy(turns = emptyList()), out)
        assertTrue(out.toString().contains("Nachrichten: 0"))
        assertEquals("OpenAutoAI-Chat.txt", ConversationTextExport.FILE_NAME)
        assertEquals("text/plain", ConversationTextExport.MIME_TYPE)
    }

    @Test fun preCancelledExportWritesNothing() {
        val out = StringWriter(); val cancel = CancellationSource().apply { cancel() }
        assertThrows(ChatFailure::class.java) { ConversationTextExport.write(chat(), out, cancel) }
        assertEquals("", out.toString())
    }

    @Test fun hugeMessageIsWrittenInChunksAndCancellationStopsItEarly() {
        var largest = 0; var total = 0
        val cancel = CancellationSource()
        val out = object : Writer() {
            override fun write(cbuf: CharArray, off: Int, len: Int) {
                largest = maxOf(largest, len); total += len
                if (total > 9000) cancel.cancel()
            }
            override fun flush() {}; override fun close() {}
        }
        val c = chat().copy(turns = listOf(Turn("user", "x".repeat(100_000))))
        assertThrows(ChatFailure::class.java) { ConversationTextExport.write(c, out, cancel) }
        assertTrue(largest <= 4096); assertTrue(total < 20_000)
    }

    @Test fun writerFailurePropagatesAndFormatterDoesNotCloseCallerOwnedWriter() {
        var closed = false
        val out = object : Writer() {
            override fun write(cbuf: CharArray, off: Int, len: Int) { throw IOException("test") }
            override fun flush() {}; override fun close() { closed = true }
        }
        assertThrows(IOException::class.java) { ConversationTextExport.write(chat(), out) }
        assertFalse(closed)
    }
}

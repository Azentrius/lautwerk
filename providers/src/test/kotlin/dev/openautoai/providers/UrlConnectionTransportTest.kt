package dev.openautoai.providers

import dev.openautoai.core.ai.ChatError
import dev.openautoai.core.ai.ChatFailure
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class UrlConnectionTransportTest {
    private val request = HttpRequest("https://api.openai.com/v1/responses", mapOf("Authorization" to "Bearer test-key"), "Grüße")

    private class Connection(private val code: Int = 200, body: String = "Antwort") : HttpURLConnection(URL("https://api.openai.com/v1/responses")) {
        var disconnected = false
        var inputClosed = false
        var outputClosed = false
        var failWrite = false
        val sent = object : ByteArrayOutputStream() {
            override fun close() { outputClosed = true; super.close() }
        }
        private val source = object : ByteArrayInputStream(body.toByteArray(Charsets.UTF_8)) {
            override fun close() { inputClosed = true; super.close() }
        }
        override fun connect() = Unit
        override fun disconnect() { disconnected = true }
        override fun usingProxy() = false
        override fun getResponseCode() = code
        override fun getOutputStream(): ByteArrayOutputStream {
            if (failWrite) throw IOException("write failed")
            return sent
        }
        override fun getInputStream(): InputStream {
            check(code in 200..299)
            return source
        }
        override fun getErrorStream(): InputStream? = if (code !in 200..299) source else null
    }

    @Test fun usesUtf8PostTimeoutsAndClosesConnectionAndStreams() {
        val connection = Connection(body = "Grüße zurück")
        val transport = UrlConnectionTransport { connection }
        assertEquals("Grüße zurück", transport.post(request).body)
        assertEquals("POST", connection.requestMethod)
        assertEquals("Grüße", connection.sent.toString("UTF-8"))
        assertEquals("Bearer test-key", connection.getRequestProperty("Authorization"))
        assertEquals(15_000, connection.connectTimeout)
        assertEquals(50_000, connection.readTimeout)
        assertFalse(connection.instanceFollowRedirects)
        assertFalse(connection.useCaches)
        assertTrue(connection.disconnected && connection.inputClosed && connection.outputClosed)
    }

    @Test fun errorAndRedirectBodiesAreReadWithoutFollowingRedirects() {
        for (status in listOf(401, 429, 503, 302, 307)) {
            val connection = Connection(status, "safe test error")
            var opens = 0
            val response = UrlConnectionTransport { opens++; connection }.post(request)
            assertEquals(status, response.statusCode)
            assertEquals("safe test error", response.body)
            assertFalse(connection.instanceFollowRedirects)
            assertTrue(connection.inputClosed && connection.disconnected)
            assertEquals(1, opens)
        }
    }

    @Test fun disconnectsOnWriteFailureAndRejectsOversizedResponses() {
        val failed = Connection().apply { failWrite = true }
        assertThrows(IOException::class.java) { UrlConnectionTransport { failed }.post(request) }
        assertTrue(failed.disconnected)
        val huge = Connection(body = "x".repeat(UrlConnectionTransport.MAX_RESPONSE_BYTES + 1))
        val error = assertThrows(ChatFailure::class.java) { UrlConnectionTransport { huge }.post(request) }
        assertEquals(ChatError.INVALID_RESPONSE, error.kind)
        assertTrue(huge.inputClosed && huge.disconnected)
    }

    @Test fun rejectsOtherEndpointsBeforeOpeningAnyConnection() {
        var opens = 0
        val transport = UrlConnectionTransport { opens++; Connection() }
        for (url in listOf("http://api.openai.com/v1/responses", "https://other.example/", "https://api.openai.com/v1/responses?extra=true")) {
            val error = assertThrows(ChatFailure::class.java) { transport.post(HttpRequest(url, request.headers, request.body)) }
            assertEquals(ChatError.INVALID_REQUEST, error.kind)
        }
        assertEquals(0, opens)
    }

    @Test fun interruptedWorkDoesNotOpenConnectionOrClearTheInterruptFlag() {
        var opens = 0
        Thread.currentThread().interrupt()
        try {
            val error = assertThrows(ChatFailure::class.java) { UrlConnectionTransport { opens++; Connection() }.post(request) }
            assertEquals(ChatError.CANCELLED, error.kind)
            assertTrue(Thread.currentThread().isInterrupted)
            assertEquals(0, opens)
        } finally { Thread.interrupted() }
    }
}

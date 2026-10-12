package dev.openautoai.providers

import dev.openautoai.core.ai.*
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Loopback sockets and dummy credentials only. The production endpoint allowlist stays intact. */
class OkHttpTransportTest {
    private fun request(cancel: RequestCancellation = RequestCancellation.NONE) =
        HttpRequest("https://api.openai.com/v1/responses", mapOf("Authorization" to "Bearer test-key"), "Grüße", cancel)

    private fun transport(server: MockWebServer, listener: EventListener = object : EventListener() {}) =
        OkHttpTransport(Call.Factory { original ->
            OkHttpTransport.client.newBuilder().eventListener(listener).build()
                .newCall(original.newBuilder().url(server.url("/test")).build())
        })

    @Test fun utf8PostUsesHeadersAndBoundedTimeoutsWithoutRetriesOrRedirects() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("Grüße zurück"))
            assertEquals("Grüße zurück", transport(server).post(request()).body)
            val sent = server.takeRequest(3, TimeUnit.SECONDS)!!
            assertEquals("POST", sent.method)
            assertEquals("Grüße", sent.body.readUtf8())
            assertEquals("Bearer test-key", sent.getHeader("Authorization"))
            assertEquals("application/json; charset=utf-8", sent.getHeader("Content-Type"))
        }
        val client = OkHttpTransport.client
        assertEquals(15000, client.connectTimeoutMillis)
        assertEquals(50000, client.readTimeoutMillis)
        assertEquals(50000, client.writeTimeoutMillis)
        assertEquals(60000, client.callTimeoutMillis)
        assertFalse(client.followRedirects)
        assertFalse(client.followSslRedirects)
        assertFalse(client.retryOnConnectionFailure)
        assertNull(client.cache)
    }

    @Test fun httpErrorsRedirectsAndRetryAfterNeverResendPaidRequests() {
        for (status in listOf(401, 408, 429, 503, 302, 307)) {
            MockWebServer().use { server ->
                server.enqueue(MockResponse().setResponseCode(status).setHeader("Retry-After", "0")
                    .setHeader("Location", "/other").setBody("safe test error"))
                server.enqueue(MockResponse().setBody("MUST NOT RETRY"))
                val response = transport(server).post(request())
                assertEquals(status, response.statusCode)
                assertEquals("safe test error", response.body)
                assertEquals(1, server.requestCount)
            }
        }
    }

    @Test fun oversizedBodyIsRejectedAndItsConnectionDoesNotPoisonTheNextRequest() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("x".repeat(OkHttpTransport.MAX_RESPONSE_BYTES + 1)))
            server.enqueue(MockResponse().setBody("next"))
            val transport = transport(server)
            assertEquals(ChatError.INVALID_RESPONSE, assertThrows(ChatFailure::class.java) { transport.post(request()) }.kind)
            assertEquals("next", transport.post(request()).body)
        }
    }

    @Test fun disallowedEndpointsAndPreCancelledRequestsNeverCreateACall() {
        val transport = OkHttpTransport(Call.Factory { error("must not create a call") })
        for (url in listOf("http://api.openai.com/v1/responses", "https://other.example/", "https://api.openai.com/v1/responses?extra=true")) {
            assertEquals(ChatError.INVALID_REQUEST, assertThrows(ChatFailure::class.java) {
                transport.post(HttpRequest(url, emptyMap(), "test"))
            }.kind)
        }
        val source = CancellationSource().apply { cancel() }
        assertEquals(ChatError.CANCELLED, assertThrows(ChatFailure::class.java) { transport.post(request(source)) }.kind)
        Thread.currentThread().interrupt()
        try {
            assertEquals(ChatError.CANCELLED, assertThrows(ChatFailure::class.java) { transport.post(request()) }.kind)
            assertTrue(Thread.currentThread().isInterrupted)
        } finally { Thread.interrupted() }
    }

    @Test fun cancelClosesSocketWaitingForHeadersWithoutThreadInterruption() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            val worker = Executors.newSingleThreadExecutor()
            val source = CancellationSource()
            try {
                val result = worker.submit<ChatError> {
                    try { transport(server).post(request(source)); error("late success") }
                    catch (failure: ChatFailure) { failure.kind }
                }
                assertNotNull(server.takeRequest(3, TimeUnit.SECONDS))
                source.cancel() // Deliberately no Future.cancel/Thread.interrupt.
                assertEquals(ChatError.CANCELLED, result.get(3, TimeUnit.SECONDS))
            } finally { source.cancel(); worker.shutdownNow() }
        }
    }

    @Test fun cancelClosesSocketDuringBodyReadAndFreshRequestStillCompletes() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("ab").throttleBody(1, 2, TimeUnit.SECONDS))
            server.enqueue(MockResponse().setBody("fresh reply"))
            val reading = CountDownLatch(1)
            val transport = transport(server, object : EventListener() {
                override fun responseBodyStart(call: Call) { reading.countDown() }
            })
            val source = CancellationSource()
            val worker = Executors.newSingleThreadExecutor()
            try {
                val result = worker.submit<ChatError> {
                    // MockWebServer throttles request bodies too; use an empty body to isolate response reads.
                    try { transport.post(HttpRequest(request().endpoint, emptyMap(), "", source)); error("late success") }
                    catch (failure: ChatFailure) { failure.kind }
                }
                assertTrue(reading.await(3, TimeUnit.SECONDS))
                source.cancel()
                assertEquals(ChatError.CANCELLED, result.get(1, TimeUnit.SECONDS))
                assertEquals("fresh reply", transport.post(request()).body)
                source.cancel()
                assertEquals(2, server.requestCount)
            } finally { source.cancel(); worker.shutdownNow() }
        }
    }

    @Test fun cancellationBetweenCallCreationAndRegistrationPreventsTransmission() {
        MockWebServer().use { server ->
            val source = CancellationSource()
            lateinit var call: Call
            val transport = OkHttpTransport(Call.Factory { original ->
                call = OkHttpTransport.client.newCall(original.newBuilder().url(server.url("/test")).build())
                source.cancel()
                call
            })
            assertEquals(ChatError.CANCELLED, assertThrows(ChatFailure::class.java) { transport.post(request(source)) }.kind)
            assertTrue(call.isCanceled())
            assertEquals(0, server.requestCount)
        }
    }

    @Test fun bothProviderAdaptersForwardCancellationAndSuppressLateResponses() {
        for (provider in Provider.entries) {
            val source = CancellationSource()
            val client = ProviderChatClient(CredentialSource { "dummy-key" }, HttpTransport {
                assertSame(source, it.cancellation)
                source.cancel()
                HttpResponse(200, "must not parse this late reply")
            })
            val request = ChatRequest(ModelSelection(provider, ModelCatalog.default(provider)), listOf(ChatMessage("user", "Test")), source)
            assertEquals(ChatError.CANCELLED, assertThrows(ChatFailure::class.java) { client.reply(request) }.kind)
        }
    }
}

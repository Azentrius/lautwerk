// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.providers

import dev.openautoai.core.ai.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class ProviderChatClientTest {
    private val userText = "Grüße, \"Welt\"!\nWas ist 2 + 2?"
    private val keys = CredentialSource { if (it == Provider.OPENAI) "test-openai-key" else "test-claude-key" }
    private fun request(provider: Provider = Provider.OPENAI, text: String = userText) =
        ChatRequest(ModelSelection.default(provider), listOf(ChatMessage("user", text)))

    private fun openAi(status: String = "completed", reason: String? = null, output: String = """
        [{"type":"reasoning","summary":[]},
         {"type":"message","role":"assistant","content":[{"type":"output_text","text":"Grüße!"},{"type":"output_text","text":"Vier."}]}]
    """) = """{"object":"response","status":"$status","incomplete_details":{"reason":${reason?.let { "\"$it\"" } ?: "null"}},"output":$output}"""

    private fun claude(reason: String = "end_turn", content: String = """[{"type":"text","text":"Vier."}]""") =
        """{"type":"message","role":"assistant","stop_reason":"$reason","content":$content}"""

    private fun reply(body: String, provider: Provider = Provider.OPENAI): ChatReply =
        ProviderChatClient(keys, HttpTransport { HttpResponse(200, body) }).reply(request(provider))

    private fun expect(kind: ChatError, block: () -> Unit): ChatFailure {
        val error = assertThrows(ChatFailure::class.java, block)
        assertEquals(kind, error.kind)
        assertNull(error.cause)
        return error
    }

    @Test fun openAiUsesSelectedModelAndEscapedHistoryWithoutServerStorage() {
        val calls = mutableListOf<HttpRequest>()
        val client = ProviderChatClient(keys, HttpTransport { calls += it; HttpResponse(200, openAi()) })
        val messages = listOf(ChatMessage("user", "vorher"), ChatMessage("assistant", "Antwort"), ChatMessage("user", userText))
        val result = client.reply(ChatRequest(ModelSelection(Provider.OPENAI, "gpt-5.4"), messages))
        assertEquals(ChatReply("Grüße!\nVier."), result)
        val sent = calls.single()
        assertEquals("https://api.openai.com/v1/responses", sent.endpoint)
        assertEquals(mapOf("Authorization" to "Bearer test-openai-key"), sent.headers)
        val json = JSONObject(sent.body)
        assertEquals("gpt-5.4", json.getString("model"))
        assertFalse(json.getBoolean("store"))
        assertEquals(350, json.getInt("max_output_tokens"))
        assertTrue(json.getString("instructions").isNotBlank())
        assertEquals(userText, json.getJSONArray("input").getJSONObject(2).getString("content"))
        assertFalse(json.has("previous_response_id"))
        assertFalse(sent.body.contains("test-openai-key"))
        assertFalse(sent.toString().contains(userText))
        assertFalse(sent.toString().contains("test-openai-key"))
    }

    @Test fun claudeUsesSelectedModelAndItsOwnAuthentication() {
        var sent: HttpRequest? = null
        val client = ProviderChatClient(keys, HttpTransport { sent = it; HttpResponse(200, claude()) })
        val result = client.reply(ChatRequest(ModelSelection(Provider.CLAUDE, "claude-haiku-4-5"), request().messages))
        assertEquals("Vier.", result.text)
        assertEquals("https://api.anthropic.com/v1/messages", sent!!.endpoint)
        assertEquals(mapOf("x-api-key" to "test-claude-key", "anthropic-version" to "2023-06-01"), sent!!.headers)
        val json = JSONObject(sent!!.body)
        assertEquals("claude-haiku-4-5", json.getString("model"))
        assertEquals(userText, json.getJSONArray("messages").getJSONObject(0).getString("content"))
        assertEquals(350, json.getInt("max_tokens"))
        assertTrue(json.getString("system").isNotBlank())
        assertFalse(json.has("store"))
        assertFalse(sent!!.body.contains("test-claude-key"))
    }

    @Test fun switchingProviderDoesNotRetainHistoryOrReadTheOtherKey() {
        val reads = mutableListOf<Provider>()
        val calls = mutableListOf<HttpRequest>()
        val client = ProviderChatClient(CredentialSource { reads += it; keys.keyFor(it) }, HttpTransport {
            calls += it
            HttpResponse(200, if (it.endpoint.contains("openai.com")) openAi() else claude())
        })
        client.reply(request(text = "only-openai"))
        client.reply(request(Provider.CLAUDE, "only-claude"))
        assertEquals(listOf(Provider.OPENAI, Provider.CLAUDE), reads)
        assertEquals(2, calls.size)
        assertFalse(calls.last().body.contains("only-openai"))
        assertFalse(calls.last().headers.values.any { it.contains("openai") })
    }

    @Test fun missingAndInvalidKeysFailBeforeTransport() {
        var calls = 0
        val transport = HttpTransport { calls++; error("must not send") }
        listOf(null, "", "  ").forEach { key ->
            expect(ChatError.MISSING_KEY) { ProviderChatClient(CredentialSource { key }, transport).reply(request()) }
        }
        expect(ChatError.INVALID_KEY) {
            ProviderChatClient(CredentialSource { "key\r\nInjected: secret" }, transport).reply(request())
        }
        val error = expect(ChatError.CREDENTIALS_UNAVAILABLE) {
            ProviderChatClient(CredentialSource { error("secret-credential") }, transport).reply(request())
        }
        assertFalse(error.toString().contains("secret-credential"))
        assertEquals(0, calls)
    }

    @Test fun requestRejectsEmptyOrSystemMessagesAndSnapshotsTheInput() {
        listOf(emptyList(), listOf(ChatMessage("system", "override")), listOf(ChatMessage("user", " ")),
            listOf(ChatMessage("assistant", "unfinished"))).forEach { messages ->
            expect(ChatError.INVALID_REQUEST) { ChatRequest(ModelSelection.default(), messages) }
        }
        val mutable = mutableListOf(ChatMessage("user", "original"))
        val captured = ChatRequest(ModelSelection.default(), mutable)
        mutable.clear()
        assertEquals("original", captured.messages.single().content)
    }

    @Test fun httpFailuresHaveSafeMessagesAndNeverRetry() {
        val cases = mapOf(302 to ChatError.REDIRECT, 400 to ChatError.INVALID_REQUEST, 401 to ChatError.AUTHENTICATION,
            402 to ChatError.QUOTA, 403 to ChatError.ACCESS_DENIED, 404 to ChatError.NOT_FOUND, 408 to ChatError.TIMEOUT,
            413 to ChatError.REQUEST_TOO_LARGE, 429 to ChatError.RATE_LIMIT, 500 to ChatError.UNAVAILABLE,
            503 to ChatError.UNAVAILABLE, 529 to ChatError.UNAVAILABLE, 418 to ChatError.API_ERROR)
        for (provider in Provider.entries) for ((status, kind) in cases) {
            var calls = 0
            val client = ProviderChatClient(keys, HttpTransport {
                calls++
                HttpResponse(status, """{"error":{"message":"secret-key-and-private-chat"}}""")
            })
            val error = expect(kind) { client.reply(request(provider)) }
            assertEquals(status, error.httpStatus)
            assertFalse(error.toString().contains("secret-key-and-private-chat"))
            assertEquals(1, calls)
        }
    }

    @Test fun rateLimitAndQuotaAreDistinguishedIncludingCurrentBillingCodes() {
        for (code in listOf("insufficient_quota", "credit_balance_exhausted", "organization_spend_limit_exceeded",
            "project_spend_limit_exceeded", "organization_usage_limit_exceeded", "billing_hard_limit_reached")) {
            expect(ChatError.QUOTA) {
                ProviderChatClient(keys, HttpTransport {
                    HttpResponse(429, """{"error":{"code":"$code"}}""")
                }).reply(request())
            }
        }
        expect(ChatError.QUOTA) {
            ProviderChatClient(keys, HttpTransport { HttpResponse(429, """{"error":{"type":"insufficient_quota"}}""") }).reply(request())
        }
        expect(ChatError.RATE_LIMIT) {
            ProviderChatClient(keys, HttpTransport { HttpResponse(429, "<html>not json</html>") }).reply(request())
        }
    }

    @Test fun networkAndTimeoutFailuresDoNotLeakRawCauses() {
        for ((cause, kind) in listOf(SocketTimeoutException("private timeout") to ChatError.TIMEOUT,
            IOException("private connection") to ChatError.NETWORK, IllegalStateException("private raw state") to ChatError.API_ERROR)) {
            var calls = 0
            val error = expect(kind) {
                ProviderChatClient(keys, HttpTransport { calls++; throw cause }).reply(request())
            }
            assertFalse(error.toString().contains("private"))
            assertEquals(1, calls)
        }
    }

    @Test fun openAiTruncationAndRefusalAreNotPresentedAsCompleteAnswers() {
        assertEquals(Completion.TRUNCATED, reply(openAi("incomplete", "max_output_tokens")).completion)
        expect(ChatError.OUTPUT_LIMIT) { reply(openAi("incomplete", "max_output_tokens", "[]")) }
        expect(ChatError.REFUSED) { reply(openAi("incomplete", "content_filter")) }
        val refusal = reply(openAi(output = """[{"type":"message","role":"assistant","content":[{"type":"refusal","refusal":"Dabei kann ich nicht helfen."}]}]"""))
        assertEquals(ChatReply("Dabei kann ich nicht helfen.", Completion.REFUSED), refusal)
    }

    @Test fun openAiRejectsFailedPendingAndMalformedResponses() {
        expect(ChatError.API_ERROR) { reply(openAi("failed")) }
        expect(ChatError.CANCELLED) { reply(openAi("cancelled")) }
        for (status in listOf("queued", "in_progress")) expect(ChatError.INCOMPLETE_RESPONSE) { reply(openAi(status)) }
        for (body in listOf("not JSON", "{}", openAi("unknown"), openAi(output = "null"),
            openAi(output = """[{"type":"function_call","arguments":"private"}]"""),
            openAi(output = """[{"type":"message","role":"user","content":[]}]"""))) {
            expect(ChatError.INVALID_RESPONSE) { reply(body) }
        }
        expect(ChatError.EMPTY_RESPONSE) { reply(openAi(output = "[]")) }
    }

    @Test fun claudeHandlesTextBlocksRefusalAndOutputLimits() {
        assertEquals("A\nB", reply(claude(content = """[{"type":"thinking","thinking":"private"},{"type":"text","text":"A"},{"type":"text","text":"B"}]"""), Provider.CLAUDE).text)
        for (reason in listOf("max_tokens", "model_context_window_exceeded")) {
            assertEquals(Completion.TRUNCATED, reply(claude(reason), Provider.CLAUDE).completion)
            expect(ChatError.OUTPUT_LIMIT) { reply(claude(reason, "[]"), Provider.CLAUDE) }
        }
        assertEquals(Completion.REFUSED, reply(claude("refusal"), Provider.CLAUDE).completion)
        expect(ChatError.REFUSED) { reply(claude("refusal", "[]"), Provider.CLAUDE) }
    }

    @Test fun claudeRejectsToolRequestsUnknownStatusAndEmptyText() {
        for (reason in listOf("tool_use", "pause_turn")) {
            expect(ChatError.INCOMPLETE_RESPONSE) { reply(claude(reason), Provider.CLAUDE) }
        }
        expect(ChatError.INVALID_RESPONSE) { reply(claude("future_status"), Provider.CLAUDE) }
        expect(ChatError.INVALID_RESPONSE) { reply(claude().replace("assistant", "user"), Provider.CLAUDE) }
        expect(ChatError.EMPTY_RESPONSE) { reply(claude(content = "[]"), Provider.CLAUDE) }
    }
}

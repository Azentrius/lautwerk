// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.openautoai.core.ai.*
import dev.openautoai.providers.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises Android's real org.json and the existing archive/voice bridge with fake HTTP only. */
@RunWith(AndroidJUnit4::class)
class ProviderAndroidSmokeTest {
    @Test fun bothAdaptersAcceptExistingTurnsOnAndroidWithoutNetworkOrKeys() {
        for (provider in Provider.entries) {
            var sent: HttpRequest? = null
            val body = if (provider == Provider.OPENAI)
                """{"object":"response","status":"completed","output":[{"type":"message","role":"assistant","content":[{"type":"output_text","text":"Grüße zurück!"}]}]}"""
            else """{"type":"message","role":"assistant","stop_reason":"end_turn","content":[{"type":"text","text":"Grüße zurück!"}]}"""
            val client = ProviderChatClient(CredentialSource { "dummy-test-key" }, HttpTransport {
                sent = it
                HttpResponse(200, body)
            })
            val reply = ChatApi(client).reply(provider, ModelCatalog.default(provider), listOf(Turn("user", "Grüße \"KI\"")))
            assertEquals("Grüße zurück!", reply.text)
            val arrayName = if (provider == Provider.OPENAI) "input" else "messages"
            assertEquals("Grüße \"KI\"", JSONObject(sent!!.body).getJSONArray(arrayName).getJSONObject(0).getString("content"))
        }
    }

    @Test fun mismatchedArchiveModelCannotReachAProvider() {
        var calls = 0
        val api = ChatApi(ChatClient { calls++; ChatReply("must not happen") })
        val error = assertThrows(ChatFailure::class.java) {
            api.reply(Provider.OPENAI, "claude-haiku-4-5", listOf(Turn("user", "Hallo")))
        }
        assertEquals(ChatError.INVALID_REQUEST, error.kind)
        assertEquals(0, calls)
    }

    @Test fun productionTransportInitializesOnAndroidAndPreCancelledWorkNeverReadsCredentials() {
        val client = ProviderChatClient(CredentialSource { error("must not read a key") })
        for (provider in Provider.entries) {
            val cancellation = CancellationSource().apply { cancel() }
            val request = ChatRequest(ModelSelection.default(provider), listOf(ChatMessage("user", "Test")), cancellation)
            assertEquals(ChatError.CANCELLED, assertThrows(ChatFailure::class.java) { client.reply(request) }.kind)
        }
    }
}

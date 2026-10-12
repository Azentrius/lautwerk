package dev.openautoai.providers

import dev.openautoai.core.ai.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException

/** One request to the explicitly selected provider. No retry, fallback or retained chat history. */
class ProviderChatClient(
    private val credentials: CredentialSource,
    private val transport: HttpTransport = OkHttpTransport()
) : ChatClient {
    private val adapters = mapOf(Provider.OPENAI to OpenAiAdapter(), Provider.CLAUDE to ClaudeAdapter())

    override fun reply(request: ChatRequest): ChatReply {
        request.cancellation.check()
        val key = try { credentials.keyFor(request.selection.provider)?.trim() }
            catch (_: Exception) { throw ChatFailure(ChatError.CREDENTIALS_UNAVAILABLE) }
        if (key.isNullOrBlank()) throw ChatFailure(ChatError.MISSING_KEY)
        if (key.any { it.isWhitespace() || it.code < 32 || it.code == 127 }) throw ChatFailure(ChatError.INVALID_KEY)
        val adapter = adapters.getValue(request.selection.provider)
        try {
            request.cancellation.check()
            val response = transport.post(adapter.request(request, key))
            request.cancellation.check()
            if (response.statusCode !in 200..299) throw httpFailure(response)
            return adapter.parse(JSONObject(response.body)).also { request.cancellation.check() }
        } catch (error: ChatFailure) {
            throw error
        } catch (_: SocketTimeoutException) {
            request.cancellation.check()
            throw ChatFailure(ChatError.TIMEOUT)
        } catch (_: InterruptedIOException) {
            request.cancellation.check()
            throw ChatFailure(ChatError.TIMEOUT)
        } catch (_: IOException) {
            request.cancellation.check()
            throw ChatFailure(ChatError.NETWORK)
        } catch (_: JSONException) {
            throw ChatFailure(ChatError.INVALID_RESPONSE)
        } catch (_: Exception) {
            throw ChatFailure(ChatError.API_ERROR)
        }
    }

    private fun httpFailure(response: HttpResponse): ChatFailure {
        // Only known machine-readable codes affect classification. Never surface raw server messages.
        val error = try { JSONObject(response.body).optJSONObject("error") } catch (_: JSONException) { null }
        val quotaCodes = setOf("insufficient_quota", "billing_hard_limit_reached", "credit_balance_exhausted",
            "organization_spend_limit_exceeded", "project_spend_limit_exceeded", "organization_usage_limit_exceeded")
        val kind = when (response.statusCode) {
            in 300..399 -> ChatError.REDIRECT
            400, 422 -> ChatError.INVALID_REQUEST
            401 -> ChatError.AUTHENTICATION
            402 -> ChatError.QUOTA
            403 -> ChatError.ACCESS_DENIED
            404 -> ChatError.NOT_FOUND
            408 -> ChatError.TIMEOUT
            413 -> ChatError.REQUEST_TOO_LARGE
            429 -> if (error?.stringOrNull("code") in quotaCodes || error?.stringOrNull("type") == "insufficient_quota")
                ChatError.QUOTA else ChatError.RATE_LIMIT
            in 500..599 -> ChatError.UNAVAILABLE
            else -> ChatError.API_ERROR
        }
        return ChatFailure(kind, response.statusCode)
    }
}

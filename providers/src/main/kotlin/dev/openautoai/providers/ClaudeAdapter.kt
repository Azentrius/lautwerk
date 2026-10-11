package dev.openautoai.providers

import dev.openautoai.core.ai.*
import org.json.JSONObject

internal class ClaudeAdapter : ProviderAdapter {
    override fun request(request: ChatRequest, key: String) = HttpRequest(
        "https://api.anthropic.com/v1/messages",
        mapOf("x-api-key" to key, "anthropic-version" to "2023-06-01"),
        JSONObject().put("model", request.selection.modelId)
            .put("system", VOICE_INSTRUCTIONS)
            .put("messages", request.messagesJson())
            .put("max_tokens", OUTPUT_TOKENS).toString()
    )

    override fun parse(response: JSONObject): ChatReply {
        if (response.stringOrNull("type") != "message" || response.stringOrNull("role") != "assistant") {
            throw ChatFailure(ChatError.INVALID_RESPONSE)
        }
        val completion = when (response.stringOrNull("stop_reason")) {
            "end_turn", "stop_sequence" -> Completion.COMPLETE
            "max_tokens", "model_context_window_exceeded" -> Completion.TRUNCATED
            "refusal" -> Completion.REFUSED
            "tool_use", "pause_turn" -> throw ChatFailure(ChatError.INCOMPLETE_RESPONSE)
            else -> throw ChatFailure(ChatError.INVALID_RESPONSE)
        }
        val content = response.getJSONArray("content")
        val text = mutableListOf<String>()
        for (i in 0 until content.length()) {
            val part = content.getJSONObject(i)
            when (part.stringOrNull("type")) {
                "text" -> part.stringOrNull("text")?.takeIf { it.isNotBlank() }?.let(text::add)
                "thinking", "redacted_thinking" -> Unit
                else -> throw ChatFailure(ChatError.INVALID_RESPONSE)
            }
        }
        if (text.isEmpty()) throw ChatFailure(when (completion) {
            Completion.TRUNCATED -> ChatError.OUTPUT_LIMIT
            Completion.REFUSED -> ChatError.REFUSED
            Completion.COMPLETE -> ChatError.EMPTY_RESPONSE
        })
        return ChatReply(text.joinToString("\n"), completion)
    }
}

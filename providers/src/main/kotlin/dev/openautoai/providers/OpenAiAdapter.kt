// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.providers

import dev.openautoai.core.ai.*
import org.json.JSONObject

internal class OpenAiAdapter : ProviderAdapter {
    override fun request(request: ChatRequest, key: String) = HttpRequest(
        "https://api.openai.com/v1/responses",
        mapOf("Authorization" to "Bearer $key"),
        JSONObject().put("model", request.selection.modelId)
            .put("instructions", VOICE_INSTRUCTIONS)
            .put("input", request.messagesJson())
            .put("store", false)
            .put("max_output_tokens", OUTPUT_TOKENS).toString(),
        request.cancellation
    )

    override fun parse(response: JSONObject): ChatReply {
        if (response.stringOrNull("object") != "response") throw ChatFailure(ChatError.INVALID_RESPONSE)
        val status = response.stringOrNull("status")
        val reason = response.optJSONObject("incomplete_details")?.stringOrNull("reason")
        val completion = when (status) {
            "completed" -> Completion.COMPLETE
            "incomplete" -> when (reason) {
                "max_output_tokens" -> Completion.TRUNCATED
                "content_filter" -> throw ChatFailure(ChatError.REFUSED)
                else -> throw ChatFailure(ChatError.INCOMPLETE_RESPONSE)
            }
            "failed" -> throw ChatFailure(ChatError.API_ERROR)
            "cancelled" -> throw ChatFailure(ChatError.CANCELLED)
            "queued", "in_progress" -> throw ChatFailure(ChatError.INCOMPLETE_RESPONSE)
            else -> throw ChatFailure(ChatError.INVALID_RESPONSE)
        }
        val output = response.optJSONArray("output") ?: throw ChatFailure(ChatError.INVALID_RESPONSE)
        val text = mutableListOf<String>()
        val refusals = mutableListOf<String>()
        for (i in 0 until output.length()) {
            val item = output.getJSONObject(i)
            // Never treat reasoning, tool arguments or echoed input as the assistant's answer.
            if (item.stringOrNull("type") == "reasoning") continue
            if (item.stringOrNull("type") != "message" || item.stringOrNull("role") != "assistant") {
                throw ChatFailure(ChatError.INVALID_RESPONSE)
            }
            val content = item.getJSONArray("content")
            for (j in 0 until content.length()) {
                val part = content.getJSONObject(j)
                when (part.stringOrNull("type")) {
                    "output_text" -> part.stringOrNull("text")?.takeIf { it.isNotBlank() }?.let(text::add)
                    "refusal" -> part.stringOrNull("refusal")?.takeIf { it.isNotBlank() }?.let(refusals::add)
                    else -> throw ChatFailure(ChatError.INVALID_RESPONSE)
                }
            }
        }
        if (refusals.isNotEmpty()) return ChatReply(refusals.joinToString("\n"), Completion.REFUSED)
        if (text.isEmpty()) throw ChatFailure(if (completion == Completion.TRUNCATED) ChatError.OUTPUT_LIMIT else ChatError.EMPTY_RESPONSE)
        return ChatReply(text.joinToString("\n"), completion)
    }
}

package dev.openautoai.providers

import dev.openautoai.core.ai.ChatRequest
import dev.openautoai.core.ai.ChatReply
import org.json.JSONArray
import org.json.JSONObject

internal interface ProviderAdapter {
    fun request(request: ChatRequest, key: String): HttpRequest
    fun parse(response: JSONObject): ChatReply
}

internal fun ChatRequest.messagesJson() = JSONArray().also { array ->
    messages.forEach { array.put(JSONObject().put("role", it.role).put("content", it.content)) }
}

internal fun JSONObject.stringOrNull(key: String): String? = opt(key) as? String

internal const val VOICE_INSTRUCTIONS =
    "Du bist ein hilfreicher deutschsprachiger Sprachassistent für die Nutzung im Auto. " +
        "Antworte kurz, natürlich und ohne Markdown, Tabellen oder Links. " +
        "Unterstütze sicheres Fahren und verlange keine Bedienung am Bildschirm."
// Retain the previous prototype's output budget. No automatic retries or higher-budget continuation.
internal const val OUTPUT_TOKENS = 350

package dev.openautoai.app

import dev.openautoai.core.ai.Provider
import dev.openautoai.core.ai.ModelCatalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Pure provider transport. No secrets are logged or bundled into the application. */
class ChatApi(private val store: SecretStore) {
    fun reply(provider: Provider, modelId: String, turns: List<Turn>): String {
        require(ModelCatalog.valid(provider, modelId)) { "Dieses Modell ist nicht ausgewählt oder nicht verfügbar" }
        val key = store.read(provider) ?: throw IllegalStateException("Kein API-Schlüssel hinterlegt")
        return when (provider) {
            Provider.OPENAI -> openAi(key, modelId, turns)
            Provider.CLAUDE -> claude(key, modelId, turns)
        }
    }

    private fun openAi(key: String, modelId: String, turns: List<Turn>): String {
        val input = JSONArray()
        turns.forEach { turn ->
            input.put(JSONObject().put("role", turn.role).put("content", turn.content))
        }
        val body = JSONObject()
            .put("model", modelId)
            .put("instructions", VOICE_INSTRUCTIONS)
            .put("input", input)
            .put("max_output_tokens", 350)
        val result = post(
            "https://api.openai.com/v1/responses",
            mapOf("Authorization" to "Bearer $key"),
            body
        )
        val output = result.optJSONArray("output") ?: return "Keine Antwort erhalten"
        val parts = mutableListOf<String>()
        for (i in 0 until output.length()) {
            val content = output.optJSONObject(i)?.optJSONArray("content") ?: continue
            for (j in 0 until content.length()) {
                val part = content.optJSONObject(j) ?: continue
                if (part.optString("type") == "output_text") {
                    part.optString("text").takeIf { it.isNotBlank() }?.let(parts::add)
                }
            }
        }
        return parts.joinToString(" ").ifBlank { "Keine Sprachantwort erhalten" }
    }

    private fun claude(key: String, modelId: String, turns: List<Turn>): String {
        val messages = JSONArray()
        turns.forEach { turn ->
            messages.put(JSONObject().put("role", turn.role).put("content", turn.content))
        }
        val body = JSONObject()
            .put("model", modelId)
            .put("system", VOICE_INSTRUCTIONS)
            .put("messages", messages)
            .put("max_tokens", 350)
        val result = post(
            "https://api.anthropic.com/v1/messages",
            mapOf("x-api-key" to key, "anthropic-version" to "2023-06-01"),
            body
        )
        val content = result.optJSONArray("content") ?: return "Keine Antwort erhalten"
        val parts = mutableListOf<String>()
        for (i in 0 until content.length()) {
            val part = content.optJSONObject(i) ?: continue
            if (part.optString("type") == "text") {
                part.optString("text").takeIf { it.isNotBlank() }?.let(parts::add)
            }
        }
        return parts.joinToString(" ").ifBlank { "Keine Sprachantwort erhalten" }
    }

    private fun post(endpoint: String, headers: Map<String, String>, body: JSONObject): JSONObject {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 15_000
            readTimeout = 50_000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            headers.forEach { (k, v) -> setRequestProperty(k, v) }
        }
        try {
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            if (code !in 200..299) {
                throw IllegalStateException("API-Fehler $code. Bitte Schlüssel, Guthaben und Modellzugriff prüfen.")
            }
            val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            return JSONObject(response)
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val VOICE_INSTRUCTIONS =
            "Du bist ein hilfreicher deutschsprachiger Sprachassistent für die Nutzung im Auto. " +
                "Antworte kurz, natürlich und ohne Markdown, Tabellen oder Links. " +
                "Unterstütze sicheres Fahren und verlange keine Bedienung am Bildschirm."
    }
}

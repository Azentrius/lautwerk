package dev.openautoai.providers

import dev.openautoai.core.ai.ChatError
import dev.openautoai.core.ai.ChatFailure
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

// Ordinary classes intentionally do not expose keys or conversation text through data-class toString().
class HttpRequest(val endpoint: String, val headers: Map<String, String>, val body: String) {
    override fun toString() = "HttpRequest(redacted)"
}
class HttpResponse(val statusCode: Int, val body: String) {
    override fun toString() = "HttpResponse(statusCode=$statusCode, body=redacted)"
}

fun interface HttpTransport {
    fun post(request: HttpRequest): HttpResponse
}

class UrlConnectionTransport internal constructor(
    private val openConnection: (URL) -> HttpURLConnection
) : HttpTransport {
    constructor() : this({ it.openConnection() as HttpURLConnection })

    override fun post(request: HttpRequest): HttpResponse {
        // Fixed HTTPS destinations; no credentials on redirects or user-configurable endpoints.
        if (request.endpoint !in ENDPOINTS) throw ChatFailure(ChatError.INVALID_REQUEST)
        checkInterrupted()
        val connection = openConnection(URL(request.endpoint))
        try {
            connection.requestMethod = "POST"
            connection.instanceFollowRedirects = false
            connection.useCaches = false
            connection.doOutput = true
            connection.connectTimeout = 15_000
            connection.readTimeout = 50_000
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            request.headers.forEach { (name, value) -> connection.setRequestProperty(name, value) }
            val bytes = request.body.toByteArray(Charsets.UTF_8)
            connection.setFixedLengthStreamingMode(bytes.size)
            connection.outputStream.use { it.write(bytes) }
            checkInterrupted()
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.use(::readLimited).orEmpty()
            return HttpResponse(status, body)
        } finally {
            connection.disconnect()
        }
    }

    private fun readLimited(stream: InputStream): String {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            checkInterrupted()
            val count = stream.read(buffer)
            if (count == -1) break
            if (output.size() + count > MAX_RESPONSE_BYTES) throw ChatFailure(ChatError.INVALID_RESPONSE)
            output.write(buffer, 0, count)
        }
        return output.toString("UTF-8")
    }

    private fun checkInterrupted() {
        if (Thread.currentThread().isInterrupted) throw ChatFailure(ChatError.CANCELLED)
    }

    companion object {
        internal const val MAX_RESPONSE_BYTES = 1_048_576
        internal val ENDPOINTS = setOf("https://api.openai.com/v1/responses", "https://api.anthropic.com/v1/messages")
    }
}

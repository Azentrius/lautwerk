package dev.openautoai.providers

import dev.openautoai.core.ai.*
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

// Ordinary classes intentionally hide keys and conversation text from toString().
class HttpRequest(val endpoint: String, val headers: Map<String, String>, val body: String,
                  val cancellation: RequestCancellation = RequestCancellation.NONE) {
    override fun toString() = "HttpRequest(redacted)"
}
class HttpResponse(val statusCode: Int, val body: String) {
    override fun toString() = "HttpResponse(statusCode=$statusCode, body=redacted)"
}

fun interface HttpTransport { fun post(request: HttpRequest): HttpResponse }

/** One cancellable call per request. No global cancel-all or shared mutable request handle. */
class OkHttpTransport internal constructor(private val calls: Call.Factory) : HttpTransport {
    constructor() : this(client)

    override fun post(request: HttpRequest): HttpResponse {
        if (request.endpoint !in ENDPOINTS) throw ChatFailure(ChatError.INVALID_REQUEST)
        val cancellation = request.cancellation
        cancellation.check()
        val bytes = request.body.toByteArray(Charsets.UTF_8)
        val body = object : RequestBody() {
            override fun contentType() = "application/json; charset=utf-8".toMediaType()
            override fun contentLength() = bytes.size.toLong()
            // Also prevents HTTP follow-ups (e.g. 503 Retry-After: 0) from resending a paid request.
            override fun isOneShot() = true
            override fun writeTo(sink: BufferedSink) { cancellation.check(); sink.write(bytes) }
        }
        val builder = Request.Builder().url(request.endpoint).post(body).header("Accept", "application/json")
        request.headers.forEach { (name, value) -> builder.header(name, value) }
        val call = calls.newCall(builder.build())
        val registration = cancellation.onCancel { call.cancel() }
        try {
            cancellation.check()
            return call.execute().use { response ->
                val text = response.body.byteStream().use { readLimited(it, cancellation) }
                cancellation.check()
                HttpResponse(response.code, text)
            }
        } catch (error: Exception) {
            cancellation.check() // A cancelled socket is CANCELLED, not NETWORK/TIMEOUT.
            throw error
        } finally { registration.close() }
    }

    private fun readLimited(stream: InputStream, cancellation: RequestCancellation): String {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            cancellation.check()
            val count = stream.read(buffer)
            if (count == -1) break
            if (output.size() + count > MAX_RESPONSE_BYTES) throw ChatFailure(ChatError.INVALID_RESPONSE)
            output.write(buffer, 0, count)
        }
        return output.toString("UTF-8")
    }

    companion object {
        internal const val MAX_RESPONSE_BYTES = 1_048_576
        internal val ENDPOINTS = setOf("https://api.openai.com/v1/responses", "https://api.anthropic.com/v1/messages")
        internal val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS).readTimeout(50, TimeUnit.SECONDS)
            .writeTimeout(50, TimeUnit.SECONDS).callTimeout(60, TimeUnit.SECONDS)
            .followRedirects(false).followSslRedirects(false).retryOnConnectionFailure(false)
            .build()
    }
}

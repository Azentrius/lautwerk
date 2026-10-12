package dev.openautoai.core.ai

import dev.openautoai.core.i18n.Messages

data class ChatMessage(val role: String, val content: String)

/** A snapshot of this conversation only; callers must not mix provider histories. */
class ChatRequest(val selection: ModelSelection, messages: List<ChatMessage>,
                  val cancellation: RequestCancellation = RequestCancellation.NONE) {
    val messages: List<ChatMessage> = messages.toList()

    init {
        if (this.messages.isEmpty() || this.messages.last().role != "user" ||
            this.messages.any { it.role !in setOf("user", "assistant") || it.content.isBlank() }) {
            throw ChatFailure(ChatError.INVALID_REQUEST)
        }
    }
}

enum class Completion { COMPLETE, TRUNCATED, REFUSED }
data class ChatReply(val text: String, val completion: Completion = Completion.COMPLETE)

/** Blocking call: run on a worker, never on a phone or car UI thread. */
fun interface ChatClient {
    fun reply(request: ChatRequest): ChatReply
}

fun interface CredentialSource {
    fun keyFor(provider: Provider): String?
}

/** Fixed messages only. Never put provider response bodies, keys or raw causes in an exception. */
enum class ChatError(private val text: Messages) {
    MISSING_KEY(Messages.CHAT_MISSING_KEY),
    CREDENTIALS_UNAVAILABLE(Messages.CHAT_CREDENTIALS_UNAVAILABLE),
    INVALID_KEY(Messages.CHAT_INVALID_KEY),
    AUTHENTICATION(Messages.CHAT_AUTHENTICATION),
    ACCESS_DENIED(Messages.CHAT_ACCESS_DENIED),
    NOT_FOUND(Messages.CHAT_NOT_FOUND),
    QUOTA(Messages.CHAT_QUOTA),
    RATE_LIMIT(Messages.CHAT_RATE_LIMIT),
    INVALID_REQUEST(Messages.CHAT_INVALID_REQUEST),
    REQUEST_TOO_LARGE(Messages.CHAT_REQUEST_TOO_LARGE),
    TIMEOUT(Messages.CHAT_TIMEOUT),
    NETWORK(Messages.CHAT_NETWORK),
    UNAVAILABLE(Messages.CHAT_UNAVAILABLE),
    REDIRECT(Messages.CHAT_REDIRECT),
    INVALID_RESPONSE(Messages.CHAT_INVALID_RESPONSE),
    EMPTY_RESPONSE(Messages.CHAT_EMPTY_RESPONSE),
    OUTPUT_LIMIT(Messages.CHAT_OUTPUT_LIMIT),
    INCOMPLETE_RESPONSE(Messages.CHAT_INCOMPLETE_RESPONSE),
    REFUSED(Messages.CHAT_REFUSED),
    CANCELLED(Messages.CHAT_CANCELLED),
    API_ERROR(Messages.CHAT_API_ERROR);
    val userMessage: String get() = text.text()
}

class ChatFailure(val kind: ChatError, val httpStatus: Int? = null) : RuntimeException(kind.userMessage)

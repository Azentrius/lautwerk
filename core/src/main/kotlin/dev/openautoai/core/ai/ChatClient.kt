package dev.openautoai.core.ai

data class ChatMessage(val role: String, val content: String)

/** A snapshot of this conversation only; callers must not mix provider histories. */
class ChatRequest(val selection: ModelSelection, messages: List<ChatMessage>) {
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
enum class ChatError(val userMessage: String) {
    MISSING_KEY("Bitte zuerst einen API-Schlüssel für diesen Anbieter hinterlegen."),
    CREDENTIALS_UNAVAILABLE("Der API-Schlüssel konnte nicht gelesen werden. Bitte auf dem Smartphone prüfen."),
    INVALID_KEY("Der API-Schlüssel enthält ungültige Zeichen. Bitte auf dem Smartphone prüfen."),
    AUTHENTICATION("Die API-Anmeldung wurde abgelehnt. Bitte API-Schlüssel und Kontozugang prüfen."),
    ACCESS_DENIED("Der Anbieter erlaubt diesen Zugriff nicht. Bitte Modell- und Kontoberechtigungen prüfen."),
    NOT_FOUND("Modell oder API-Endpunkt nicht gefunden. Bitte Modellzugriff prüfen."),
    QUOTA("API-Guthaben oder Nutzungslimit erreicht. Bitte Abrechnung und Kontolimits prüfen."),
    RATE_LIMIT("Zu viele API-Anfragen. Bitte warten und später erneut versuchen."),
    INVALID_REQUEST("Die Anfrage ist für diesen Anbieter nicht gültig."),
    REQUEST_TOO_LARGE("Der Gesprächskontext ist zu groß. Bitte ein neues Gespräch beginnen."),
    TIMEOUT("Der Anbieter hat nicht rechtzeitig geantwortet. Bitte später erneut versuchen."),
    NETWORK("Keine Verbindung zum Anbieter. Bitte die Internetverbindung prüfen."),
    UNAVAILABLE("Der Anbieter ist vorübergehend nicht verfügbar. Bitte später erneut versuchen."),
    REDIRECT("Eine unerwartete API-Weiterleitung wurde nicht ausgeführt."),
    INVALID_RESPONSE("Die Antwort des Anbieters konnte nicht verarbeitet werden."),
    EMPTY_RESPONSE("Der Anbieter hat keine Textantwort geliefert."),
    OUTPUT_LIMIT("Das Antwortlimit wurde erreicht, bevor eine Textantwort vorlag."),
    INCOMPLETE_RESPONSE("Der Anbieter hat die Antwort nicht abgeschlossen."),
    REFUSED("Der Anbieter hat diese Anfrage abgelehnt."),
    CANCELLED("Die Anfrage wurde abgebrochen."),
    API_ERROR("Die Anfrage beim Anbieter ist fehlgeschlagen.")
}

class ChatFailure(val kind: ChatError, val httpStatus: Int? = null) : RuntimeException(kind.userMessage)

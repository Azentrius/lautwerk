package dev.openautoai.core.storage

import dev.openautoai.core.ai.CredentialSource
import dev.openautoai.core.ai.Provider

data class Turn(val role: String, val content: String)
data class Conversation(
    val id: String,
    val title: String,
    val provider: Provider,
    val modelId: String,
    val updatedAt: Long,
    val turns: List<Turn>
)

/** Synchronous local operations. Implementations serialize read-modify-write across instances. */
interface ConversationRepository {
    fun all(): List<Conversation>
    fun get(id: String): Conversation?
    fun create(provider: Provider, modelId: String): Conversation
    fun append(id: String, turn: Turn)
    fun delete(id: String)
}

/** Missing keys return null. Unreadable or unsaved data must be reported, never treated as absent. */
interface CredentialStore : CredentialSource {
    fun read(provider: Provider): String?
    fun save(provider: Provider, secret: String)
    fun clear(provider: Provider)
    override fun keyFor(provider: Provider): String? = read(provider)
}

enum class StorageError(val userMessage: String) {
    ARCHIVE_READ("Der lokale Chatverlauf konnte nicht gelesen werden. Vorhandene Daten bleiben erhalten."),
    ARCHIVE_WRITE("Der Chatverlauf konnte nicht gespeichert werden. Bitte den Gerätespeicher prüfen."),
    CONVERSATION_MISSING("Dieses Gespräch ist nicht mehr vorhanden. Bitte ein neues Gespräch auswählen."),
    CREDENTIAL_READ("Der gespeicherte API-Schlüssel konnte nicht gelesen werden. Bitte die Zugangsdaten auf dem Smartphone prüfen."),
    CREDENTIAL_WRITE("Die Zugangsdaten konnten nicht gespeichert werden. Bitte den Gerätespeicher und den Geräteschlüssel prüfen.")
}

/** Safe, fixed messages only: no plaintext, ciphertext or underlying exception is exposed to the UI. */
class StorageFailure(val kind: StorageError) : IllegalStateException(kind.userMessage)

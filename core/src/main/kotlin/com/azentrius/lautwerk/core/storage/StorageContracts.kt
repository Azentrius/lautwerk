// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.core.storage

import com.azentrius.lautwerk.core.i18n.Messages
import com.azentrius.lautwerk.core.ai.CredentialSource
import com.azentrius.lautwerk.core.ai.Provider

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

enum class StorageError(private val text: Messages) {
    ARCHIVE_READ(Messages.STORAGE_ARCHIVE_READ),
    ARCHIVE_WRITE(Messages.STORAGE_ARCHIVE_WRITE),
    CONVERSATION_MISSING(Messages.STORAGE_CONVERSATION_MISSING),
    CREDENTIAL_READ(Messages.STORAGE_CREDENTIAL_READ),
    CREDENTIAL_WRITE(Messages.STORAGE_CREDENTIAL_WRITE);
    val userMessage: String get() = text.text()
}

/** Safe, fixed messages only: no plaintext, ciphertext or underlying exception is exposed to the UI. */
class StorageFailure(val kind: StorageError) : IllegalStateException(kind.userMessage)

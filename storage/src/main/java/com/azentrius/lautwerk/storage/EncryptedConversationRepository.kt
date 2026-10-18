// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.storage

import com.azentrius.lautwerk.core.i18n.Messages
import android.content.Context
import com.azentrius.lautwerk.core.ai.ModelCatalog
import com.azentrius.lautwerk.core.ai.Provider
import com.azentrius.lautwerk.core.storage.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class EncryptedConversationRepository internal constructor(
    private val file: ArchiveFile,
    private val cipher: PayloadCipher
) : ConversationRepository {
    constructor(context: Context) : this(
        AtomicArchiveFile(File(context.applicationContext.filesDir, "conversation_archive.dat")),
        AndroidKeystoreCipher("lautwerk-chat-archive-v1")
    )

    private val lock = StorageLocks.forName("archive:${file.identity}")

    private fun load(): MutableList<Conversation> = try {
        val payload = file.read()
        if (payload == null) mutableListOf() else {
            val json = JSONArray(String(cipher.decrypt(payload), Charsets.UTF_8))
            val result = MutableList(json.length()) { index ->
                val item = json.getJSONObject(index)
                val turns = item.getJSONArray("turns")
                Conversation(item.getString("id"), item.getString("title"),
                    Provider.valueOf(item.getString("provider")), item.getString("model"),
                    item.getLong("updated"), List(turns.length()) { i ->
                        turns.getJSONObject(i).let { Turn(it.getString("role"), it.getString("content")) }
                    })
            }
            // Ambiguous IDs or unknown roles must not be silently rewritten.
            require(result.map { it.id }.distinct().size == result.size)
            require(result.all { it.id.isNotBlank() && it.turns.all { turn -> turn.role in setOf("user", "assistant") } })
            result
        }
    } catch (_: Exception) { throw StorageFailure(StorageError.ARCHIVE_READ) }

    private fun save(list: List<Conversation>) {
        try {
            val json = JSONArray()
            list.forEach { conversation ->
                val turns = JSONArray()
                conversation.turns.forEach { turns.put(JSONObject().put("role", it.role).put("content", it.content)) }
                json.put(JSONObject().put("id", conversation.id).put("title", conversation.title)
                    .put("provider", conversation.provider.name).put("model", conversation.modelId)
                    .put("updated", conversation.updatedAt).put("turns", turns))
            }
            // Every mutation loaded and validated the old archive while holding the same lock.
            file.write(cipher.encrypt(json.toString().toByteArray(Charsets.UTF_8), allowKeyCreation = true))
        } catch (_: Exception) { throw StorageFailure(StorageError.ARCHIVE_WRITE) }
    }

    override fun all(): List<Conversation> = synchronized(lock) { load().sortedByDescending { it.updatedAt } }
    override fun get(id: String): Conversation? = synchronized(lock) { load().find { it.id == id } }

    override fun create(provider: Provider, modelId: String): Conversation = synchronized(lock) {
        require(ModelCatalog.valid(provider, modelId)) { "Modell gehört nicht zum gewählten Anbieter" }
        val list = load()
        val conversation = Conversation(UUID.randomUUID().toString(), Messages.NEW_CONVERSATION.text(), provider, modelId,
            System.currentTimeMillis(), emptyList())
        list.add(conversation)
        save(list)
        conversation
    }

    override fun append(id: String, turn: Turn) = synchronized(lock) {
        require(turn.role in setOf("user", "assistant") && turn.content.isNotBlank()) { "Ungültige Nachricht" }
        val list = load()
        val index = list.indexOfFirst { it.id == id }
        if (index < 0) throw StorageFailure(StorageError.CONVERSATION_MISSING)
        val old = list[index]
        val title = if (old.turns.isEmpty() && turn.role == "user") turn.content.take(65) else old.title
        list[index] = old.copy(title = title, turns = old.turns + turn, updatedAt = System.currentTimeMillis())
        save(list)
    }

    override fun delete(id: String) = synchronized(lock) {
        val list = load()
        if (list.removeAll { it.id == id }) save(list)
        Unit
    }
}

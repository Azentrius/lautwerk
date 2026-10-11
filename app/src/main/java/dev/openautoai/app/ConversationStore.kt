package dev.openautoai.app

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class Conversation(
    val id: String,
    val title: String,
    val provider: Provider,
    val modelId: String,
    val updatedAt: Long,
    val turns: List<Turn>
)

/** Private encrypted archive. No external accounts or uploads; Android backup is disabled. */
class ConversationStore(context: Context) {
    private val file = AtomicFile(File(context.filesDir, "conversation_archive.dat"))
    private val keyAlias = "openautoai-chat-archive-v1"

    private fun secretKey(): SecretKey {
        val keystore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keystore.getKey(keyAlias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private fun loadArchive(): MutableList<Conversation> {
        if (!file.baseFile.exists()) return mutableListOf()
        return try {
            val ciphertext = file.openRead().use { it.readBytes() }
            require(ciphertext.size >= 29)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, ciphertext.copyOfRange(0, 12)))
            val json = JSONArray(String(cipher.doFinal(ciphertext.copyOfRange(12, ciphertext.size)), Charsets.UTF_8))
            MutableList(json.length()) { index ->
                val c = json.getJSONObject(index)
                val messages = c.getJSONArray("turns")
                val turns = List(messages.length()) { i ->
                    messages.getJSONObject(i).let { Turn(it.getString("role"), it.getString("content")) }
                }
                Conversation(
                    id = c.getString("id"), title = c.getString("title"),
                    provider = Provider.valueOf(c.getString("provider")),
                    modelId = c.getString("model"), updatedAt = c.getLong("updated"), turns = turns
                )
            }
        } catch (e: Exception) {
            // Do not overwrite unreadable encrypted data; recoverable errors must be visible.
            throw IllegalStateException("Der lokale Chatverlauf konnte nicht entschlüsselt werden.", e)
        }
    }

    private fun saveArchive(list: List<Conversation>) {
        val json = JSONArray()
        list.forEach { c ->
            val turns = JSONArray()
            c.turns.forEach { turns.put(JSONObject().put("role", it.role).put("content", it.content)) }
            json.put(JSONObject()
                .put("id", c.id).put("title", c.title).put("provider", c.provider.name)
                .put("model", c.modelId).put("updated", c.updatedAt).put("turns", turns))
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val bytes = cipher.iv + cipher.doFinal(json.toString().toByteArray(Charsets.UTF_8))
        val stream = file.startWrite()
        try {
            stream.write(bytes)
            file.finishWrite(stream)
        } catch (e: Exception) {
            file.failWrite(stream)
            throw e
        }
    }

    @Synchronized fun all(): List<Conversation> = loadArchive().sortedByDescending { it.updatedAt }
    @Synchronized fun get(id: String): Conversation? = loadArchive().find { it.id == id }

    @Synchronized fun create(provider: Provider, modelId: String): Conversation {
        require(ModelCatalog.valid(provider, modelId))
        val session = Conversation(UUID.randomUUID().toString(), "Neues Gespräch", provider, modelId,
            System.currentTimeMillis(), emptyList())
        val list = loadArchive()
        list.add(session)
        saveArchive(list)
        return session
    }

    @Synchronized fun append(id: String, turn: Turn) {
        val list = loadArchive()
        val i = list.indexOfFirst { it.id == id }
        if (i < 0) return
        val old = list[i]
        val title = if (old.turns.isEmpty() && turn.role == "user") turn.content.take(65) else old.title
        list[i] = old.copy(title = title, turns = old.turns + turn, updatedAt = System.currentTimeMillis())
        saveArchive(list)
    }

    @Synchronized fun delete(id: String) {
        val list = loadArchive()
        if (list.removeAll { it.id == id }) saveArchive(list)
    }
}

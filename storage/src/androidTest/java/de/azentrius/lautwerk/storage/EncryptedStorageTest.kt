// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.storage

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.azentrius.lautwerk.core.ai.ModelCatalog
import de.azentrius.lautwerk.core.ai.Provider
import de.azentrius.lautwerk.core.storage.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.security.KeyStore
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Only the standalone library test app's private files and test-prefixed keystore aliases are used. */
@RunWith(AndroidJUnit4::class)
class EncryptedStorageTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var directory: File
    private lateinit var file: File
    private lateinit var alias: String
    private lateinit var preferences: SharedPreferences
    private lateinit var preferencesName: String
    private lateinit var credentialLock: Any

    @Before fun setUp() {
        val suffix = UUID.randomUUID().toString()
        alias = "lautwerk-storage-test-$suffix"
        directory = File(context.cacheDir, alias).apply { mkdirs() }
        file = File(directory, "conversation_archive.dat")
        preferencesName = alias
        preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        credentialLock = Any()
    }

    @After fun tearDown() {
        // This directory and alias were created by this test, never by the assistant app.
        directory.deleteRecursively()
        context.deleteSharedPreferences(preferencesName)
        keyStore().deleteEntry(alias)
    }

    private fun repository(): EncryptedConversationRepository =
        EncryptedConversationRepository(AtomicArchiveFile(file), AndroidKeystoreCipher(alias))
    private fun credentials(prefs: SharedPreferences = preferences): EncryptedCredentialStore =
        EncryptedCredentialStore(prefs, AndroidKeystoreCipher(alias), credentialLock)
    private fun create(repo: ConversationRepository = repository()) =
        repo.create(Provider.OPENAI, ModelCatalog.default(Provider.OPENAI))
    private fun failure(kind: StorageError, action: () -> Unit): StorageFailure {
        val error = assertThrows(StorageFailure::class.java, action)
        assertEquals(kind, error.kind)
        assertEquals(kind.userMessage, error.message)
        assertNull(error.cause)
        return error
    }
    private fun keyStore() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    // Independent fixture reproduces 0.1.0–0.1.3, not the new production codec.
    private fun legacyEncrypt(text: String): ByteArray {
        val store = keyStore()
        val key = if (store.containsAlias(alias)) store.getKey(alias, null) as SecretKey else {
            KeyGenerator.getInstance("AES", "AndroidKeyStore").apply {
                init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes("GCM").setEncryptionPaddings("NoPadding").build())
            }.generateKey()
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.iv + cipher.doFinal(text.toByteArray(Charsets.UTF_8))
    }
    private fun legacyDecrypt(bytes: ByteArray): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, keyStore().getKey(alias, null), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        return String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)
    }
    private fun oldArchive(provider: String = "CLAUDE") = JSONArray().put(JSONObject()
        .put("id", "legacy-chat").put("title", "Älteres Gespräch").put("provider", provider)
        .put("model", "retired-model-from-original-archive").put("updated", 1720000000000L)
        .put("turns", JSONArray().put(JSONObject().put("role", "user").put("content", "Grüße\n\"Welt\" 🚗"))))

    @Test fun originalArchiveCanBeReadUpdatedAndReadByOriginalCodec() {
        file.writeBytes(legacyEncrypt(oldArchive().toString()))
        val repo = repository()
        val loaded = repo.get("legacy-chat")!!
        assertEquals(Provider.CLAUDE, loaded.provider)
        assertEquals("retired-model-from-original-archive", loaded.modelId)
        assertEquals("Grüße\n\"Welt\" 🚗", loaded.turns.single().content)
        assertEquals(1720000000000L, loaded.updatedAt)
        repo.append(loaded.id, Turn("assistant", "Antwort mit ü und 🚙"))
        val originalReader = JSONArray(legacyDecrypt(file.readBytes())).getJSONObject(0)
        assertEquals("CLAUDE", originalReader.getString("provider"))
        assertEquals(loaded.modelId, originalReader.getString("model"))
        assertEquals(loaded.title, originalReader.getString("title"))
        assertEquals("Antwort mit ü und 🚙", originalReader.getJSONArray("turns").getJSONObject(1).getString("content"))
    }

    @Test fun originalCredentialsStayCompatibleAndProvidersStaySeparate() {
        preferences.edit().putString("openai", Base64.encodeToString(legacyEncrypt("old-dummy-openai"), Base64.NO_WRAP)).commit()
        val store = credentials()
        assertEquals("old-dummy-openai", store.read(Provider.OPENAI))
        store.save(Provider.CLAUDE, "  dummy-claude  ")
        assertEquals("dummy-claude", legacyDecrypt(Base64.decode(preferences.getString("anthropic", null), Base64.NO_WRAP)))
        assertEquals("old-dummy-openai", credentials().keyFor(Provider.OPENAI))
        store.clear(Provider.CLAUDE)
        assertNull(store.read(Provider.CLAUDE))
        assertEquals("old-dummy-openai", store.read(Provider.OPENAI))
        store.save(Provider.OPENAI, " ")
        assertNull(store.read(Provider.OPENAI))
    }

    @Test fun parallelRepositoriesDoNotLoseAppendsOrCreates() {
        val first = create()
        val pool = Executors.newFixedThreadPool(3)
        val gate = CountDownLatch(1)
        try {
            val futures = (0..2).map { writer -> pool.submit {
                val repo = repository()
                gate.await(5, TimeUnit.SECONDS)
                repeat(12) { repo.append(first.id, Turn("user", "$writer:$it")) }
                create(repo)
            } }
            gate.countDown()
            futures.forEach { it.get(60, TimeUnit.SECONDS) }
            assertEquals(4, repository().all().size)
            val contents = repository().get(first.id)!!.turns.map { it.content }
            assertEquals(36, contents.size)
            assertEquals((0..2).flatMap { writer -> (0..11).map { "$writer:$it" } }.toSet(), contents.toSet())
        } finally { pool.shutdownNow() }
    }

    @Test fun parallelFirstWritesShareOneKeyAndKeepBothConversations() {
        val pool = Executors.newFixedThreadPool(2)
        val gate = CountDownLatch(1)
        try {
            val futures = (1..2).map { pool.submit { gate.await(); create() } }
            gate.countDown()
            futures.forEach { it.get(30, TimeUnit.SECONDS) }
            assertEquals(2, repository().all().size)
        } finally { pool.shutdownNow() }
    }

    @Test fun damagedArchiveIsNotOverwrittenByAnyMutation() {
        create()
        val bytes = file.readBytes().also { it[it.lastIndex] = (it.last().toInt() xor 1).toByte() }
        file.writeBytes(bytes)
        failure(StorageError.ARCHIVE_READ) { repository().all() }
        failure(StorageError.ARCHIVE_READ) { create() }
        failure(StorageError.ARCHIVE_READ) { repository().append("x", Turn("user", "test")) }
        failure(StorageError.ARCHIVE_READ) { repository().delete("x") }
        assertArrayEquals(bytes, file.readBytes())
    }

    @Test fun unknownProviderAndDuplicateIdsAreNotSilentlyRewritten() {
        val invalidArchives = listOf(oldArchive("FUTURE_PROVIDER"), oldArchive().put(oldArchive().getJSONObject(0)))
        invalidArchives.forEach { json ->
            val original = legacyEncrypt(json.toString())
            file.writeBytes(original)
            failure(StorageError.ARCHIVE_READ) { create() }
            assertArrayEquals(original, file.readBytes())
        }
    }

    @Test fun missingArchiveKeyDoesNotGetReplacedOrDestroyCiphertext() {
        create()
        val original = file.readBytes()
        keyStore().deleteEntry(alias)
        failure(StorageError.ARCHIVE_READ) { repository().all() }
        failure(StorageError.ARCHIVE_READ) { create() }
        assertFalse(keyStore().containsAlias(alias))
        assertArrayEquals(original, file.readBytes())
    }

    @Test fun missingCredentialKeyBlocksReadAndReplacementUntilExplicitClear() {
        credentials().save(Provider.OPENAI, "dummy-key")
        val original = preferences.getString("openai", null)
        keyStore().deleteEntry(alias)
        failure(StorageError.CREDENTIAL_READ) { credentials().read(Provider.OPENAI) }
        failure(StorageError.CREDENTIAL_WRITE) { credentials().save(Provider.CLAUDE, "other-dummy") }
        assertFalse(keyStore().containsAlias(alias))
        assertEquals(original, preferences.getString("openai", null))
        credentials().clear(Provider.OPENAI)
        credentials().save(Provider.CLAUDE, "new-dummy")
        assertEquals("new-dummy", credentials().read(Provider.CLAUDE))
    }

    @Test fun missingAndUnreadableCredentialsAreDifferent() {
        assertNull(credentials().read(Provider.OPENAI))
        preferences.edit().putString("openai", "not an encrypted key").commit()
        failure(StorageError.CREDENTIAL_READ) { credentials().read(Provider.OPENAI) }
        preferences.edit().putInt("openai", 4).commit()
        failure(StorageError.CREDENTIAL_READ) { credentials().read(Provider.OPENAI) }
    }

    @Test fun backupOnlyArchiveIsRecoveredInsteadOfTreatedAsEmpty() {
        val conversation = create()
        assertTrue(file.renameTo(File(file.path + ".bak")))
        assertFalse(file.exists())
        assertEquals(conversation.id, repository().all().single().id)
        repository().append(conversation.id, Turn("user", "Nach Wiederherstellung"))
        assertEquals(1, repository().get(conversation.id)!!.turns.size)
    }

    @Test fun abortedAtomicWriteRetainsPreviousConversation() {
        val conversation = create()
        val atomic = AtomicFile(file)
        val stream = atomic.startWrite()
        stream.write(byteArrayOf(1, 2, 3))
        atomic.failWrite(stream)
        assertEquals(conversation, repository().get(conversation.id))
    }

    @Test fun diskFailuresAreReportedWithoutRawDetailsAndKeepPriorArchive() {
        val conversation = create()
        val original = file.readBytes()
        val actual = AtomicArchiveFile(file)
        val writeFailure = object : ArchiveFile by actual {
            override fun write(bytes: ByteArray) { throw IOException("sensitive underlying detail") }
        }
        failure(StorageError.ARCHIVE_WRITE) {
            EncryptedConversationRepository(writeFailure, AndroidKeystoreCipher(alias)).append(conversation.id, Turn("user", "dummy"))
        }
        assertArrayEquals(original, file.readBytes())
        val readFailure = object : ArchiveFile by actual {
            override fun read(): ByteArray? = throw IOException("sensitive underlying detail")
        }
        failure(StorageError.ARCHIVE_READ) { EncryptedConversationRepository(readFailure, AndroidKeystoreCipher(alias)).all() }
    }

    @Test fun failedPreferenceCommitRestoresOldEntryAndNeverReportsSuccess() {
        credentials().save(Provider.OPENAI, "old-dummy")
        var failNext = true
        val failing = object : SharedPreferences by preferences {
            override fun edit(): SharedPreferences.Editor {
                val actual = preferences.edit()
                return object : SharedPreferences.Editor by actual {
                    override fun commit(): Boolean {
                        actual.commit() // Simulate the memory change that can precede failure.
                        return if (failNext) { failNext = false; false } else true
                    }
                }
            }
        }
        failure(StorageError.CREDENTIAL_WRITE) { credentials(failing).save(Provider.OPENAI, "new-dummy") }
        assertEquals("old-dummy", credentials().read(Provider.OPENAI))
        failNext = true
        failure(StorageError.CREDENTIAL_WRITE) { credentials(failing).clear(Provider.OPENAI) }
        assertEquals("old-dummy", credentials().read(Provider.OPENAI))
    }

    @Test fun deletedConversationCannotBeAppendedOrResurrected() {
        val conversation = create()
        repository().delete(conversation.id)
        val original = file.readBytes()
        failure(StorageError.CONVERSATION_MISSING) { repository().append(conversation.id, Turn("user", "dummy")) }
        repository().delete(conversation.id)
        assertArrayEquals(original, file.readBytes())
        assertTrue(repository().all().isEmpty())
    }
}

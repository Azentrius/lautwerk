// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import de.azentrius.lautwerk.core.ai.Provider
import de.azentrius.lautwerk.core.storage.CredentialStore
import de.azentrius.lautwerk.core.storage.StorageError
import de.azentrius.lautwerk.core.storage.StorageFailure

class EncryptedCredentialStore internal constructor(
    private val preferences: SharedPreferences,
    private val cipher: PayloadCipher,
    private val lock: Any
) : CredentialStore {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences("api_credentials", Context.MODE_PRIVATE),
        AndroidKeystoreCipher("lautwerk-key-v1"),
        StorageLocks.forName("credentials:${context.applicationContext.packageName}:api_credentials")
    )

    override fun read(provider: Provider): String? = synchronized(lock) {
        try {
            val encoded = preferences.getString(provider.keyId, null) ?: return@synchronized null
            val payload = Base64.decode(encoded, Base64.NO_WRAP)
            String(cipher.decrypt(payload), Charsets.UTF_8).also { check(it.isNotBlank()) }
        } catch (_: Exception) { throw StorageFailure(StorageError.CREDENTIAL_READ) }
    }

    override fun save(provider: Provider, secret: String) = synchronized(lock) {
        if (secret.isBlank()) return@synchronized clear(provider)
        try {
            // Existing ciphertext plus a missing key is an error, not an invitation to replace the key.
            val bytes = cipher.encrypt(secret.trim().toByteArray(Charsets.UTF_8), allowKeyCreation = preferences.all.isEmpty())
            update(provider.keyId, Base64.encodeToString(bytes, Base64.NO_WRAP))
        } catch (_: Exception) { throw StorageFailure(StorageError.CREDENTIAL_WRITE) }
    }

    override fun clear(provider: Provider) = synchronized(lock) {
        try { update(provider.keyId, null) }
        catch (_: Exception) { throw StorageFailure(StorageError.CREDENTIAL_WRITE) }
    }

    private fun update(key: String, encoded: String?) {
        val old = preferences.all[key]
        val editor = preferences.edit()
        if (encoded == null) editor.remove(key) else editor.putString(key, encoded)
        if (!editor.commit()) {
            // commit() may already have changed the in-memory value even when disk persistence failed.
            // Restore the old entry best-effort; never report a failed write as success.
            val rollback = preferences.edit()
            when (old) {
                null -> rollback.remove(key)
                is String -> rollback.putString(key, old)
                is Int -> rollback.putInt(key, old)
                is Long -> rollback.putLong(key, old)
                is Float -> rollback.putFloat(key, old)
                is Boolean -> rollback.putBoolean(key, old)
                is Set<*> -> rollback.putStringSet(key, old.filterIsInstance<String>().toSet())
            }
            rollback.commit()
            throw StorageFailure(StorageError.CREDENTIAL_WRITE)
        }
    }
}

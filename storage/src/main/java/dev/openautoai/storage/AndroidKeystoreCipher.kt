// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.storage

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal interface PayloadCipher {
    fun encrypt(plaintext: ByteArray, allowKeyCreation: Boolean): ByteArray
    fun decrypt(payload: ByteArray): ByteArray
}

/** Compatible with the original payload: 12-byte IV followed by AES/GCM ciphertext and 128-bit tag. */
internal class AndroidKeystoreCipher(private val alias: String) : PayloadCipher {
    private val lock = StorageLocks.forName("keystore:$alias")

    private fun key(allowCreation: Boolean): SecretKey = synchronized(lock) {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = store.getKey(alias, null)
        if (existing is SecretKey) return@synchronized existing
        // A read must never create a replacement key for existing ciphertext.
        check(allowCreation && !store.containsAlias(alias))
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256).build())
        generator.generateKey()
    }

    override fun encrypt(plaintext: ByteArray, allowKeyCreation: Boolean): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key(allowKeyCreation))
        check(cipher.iv.size == 12)
        return cipher.iv + cipher.doFinal(plaintext)
    }

    override fun decrypt(payload: ByteArray): ByteArray {
        require(payload.size >= 28)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(false), GCMParameterSpec(128, payload.copyOfRange(0, 12)))
        return cipher.doFinal(payload.copyOfRange(12, payload.size))
    }
}

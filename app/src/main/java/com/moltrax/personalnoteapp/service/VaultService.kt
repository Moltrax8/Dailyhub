package com.moltrax.personalnoteapp.service

import android.util.Base64
import com.moltrax.personalnoteapp.domain.model.VaultEntry
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

private const val SENTINEL = "VAULT_OK"
private const val ITERATIONS = 100_000
private const val KEY_LENGTH = 256
private const val SALT = "PersonalNoteAppVaultSalt2024"

@Singleton
class VaultService @Inject constructor() {

    private fun deriveKey(pin: String): SecretKeySpec {
        val spec = PBEKeySpec(pin.toCharArray(), SALT.toByteArray(), ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    fun encrypt(plaintext: String, pin: String): Pair<String, String> {
        val key = deriveKey(pin)
        val iv  = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encrypted, Base64.NO_WRAP) to Base64.encodeToString(iv, Base64.NO_WRAP)
    }

    fun decrypt(encryptedBase64: String, ivBase64: String, pin: String): String? {
        return runCatching {
            val key = deriveKey(pin)
            val iv  = Base64.decode(ivBase64, Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
            val decrypted = cipher.doFinal(Base64.decode(encryptedBase64, Base64.NO_WRAP))
            String(decrypted, Charsets.UTF_8)
        }.getOrNull()
    }

    fun verifySentinel(entry: VaultEntry, pin: String): Boolean =
        decrypt(entry.encryptedContent, entry.iv, pin) == SENTINEL

    fun createSentinel(pin: String): VaultEntry {
        val (enc, iv) = encrypt(SENTINEL, pin)
        return VaultEntry(id = "sentinel", title = "__sentinel__", encryptedContent = enc, iv = iv)
    }

    fun createEntry(title: String, content: String, pin: String): VaultEntry {
        val (enc, iv) = encrypt(content, pin)
        return VaultEntry(id = UUID.randomUUID().toString(), title = title, encryptedContent = enc, iv = iv)
    }
}

package com.stealthsms.app.stego

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

enum class StegoMode(val title: String, val description: String) {
    CONTRACTION_SYNONYM(
        "Synonym & Contraction Substitution",
        "Encodes bits into natural word choices (cannot vs can't, start vs begin). 100% normal appearance."
    ),
    WORDLIST_GENERATOR(
        "Deterministic Word-List Generator",
        "Assembles an innocent-looking cover sentence from a shared dictionary. Extremely high capacity."
    ),
    TOKEN_CHAFFING(
        "Visible Token Disguise",
        "Disguises secret data as ordinary tracking numbers or confirmation tokens (#TRK-XXXX, Ref: XXXX)."
    )
}

data class StegoResult(
    val encodedText: String,
    val capacityBits: Int,
    val usedBits: Int,
    val mode: StegoMode,
    val isEncrypted: Boolean
)

data class DecodeResult(
    val success: Boolean,
    val secretMessage: String,
    val mode: StegoMode?,
    val bitsExtracted: String,
    val checksumValid: Boolean,
    val errorMessage: String? = null
)

object CryptoHelper {
    private const val ITERATIONS = 10_000
    private const val KEY_LENGTH = 256
    private const val GCM_TAG_LENGTH = 128
    private const val IV_LENGTH = 12
    private const val SALT_LENGTH = 16

    fun encrypt(plaintext: String, passphrase: String): ByteArray {
        val salt = ByteArray(SALT_LENGTH).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(IV_LENGTH).apply { SecureRandom().nextBytes(this) }

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // Return salt + iv + ciphertext
        return salt + iv + ciphertext
    }

    fun decrypt(encryptedBytes: ByteArray, passphrase: String): String {
        if (encryptedBytes.size < SALT_LENGTH + IV_LENGTH + 16) {
            throw IllegalArgumentException("Payload too short to be valid AES-GCM.")
        }
        val salt = encryptedBytes.copyOfRange(0, SALT_LENGTH)
        val iv = encryptedBytes.copyOfRange(SALT_LENGTH, SALT_LENGTH + IV_LENGTH)
        val ciphertext = encryptedBytes.copyOfRange(SALT_LENGTH + IV_LENGTH, encryptedBytes.size)

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val decrypted = cipher.doFinal(ciphertext)
        return String(decrypted, Charsets.UTF_8)
    }
}

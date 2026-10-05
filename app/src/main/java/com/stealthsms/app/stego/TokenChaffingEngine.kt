package com.stealthsms.app.stego

object TokenChaffingEngine {

    private val TOKEN_REGEX = Regex("#(?:TRK|REF|CONF)-([0-9a-fA-F-]+)")

    fun encode(secretText: String, passphrase: String? = null): StegoResult {
        val payloadBytes = if (!passphrase.isNullOrBlank()) {
            CryptoHelper.encrypt(secretText, passphrase)
        } else {
            secretText.toByteArray(Charsets.UTF_8)
        }

        val hex = payloadBytes.joinToString("") { "%02X".format(it) }
        val chunkedHex = hex.chunked(4).joinToString("-")
        val token = "#TRK-$chunkedHex"

        val message = "Your order status has been updated. Reference number: $token. Please keep this for your records."

        return StegoResult(
            encodedText = message,
            capacityBits = payloadBytes.size * 8,
            usedBits = payloadBytes.size * 8,
            mode = StegoMode.TOKEN_CHAFFING,
            isEncrypted = !passphrase.isNullOrBlank()
        )
    }

    fun decode(text: String, passphrase: String? = null): DecodeResult {
        val match = TOKEN_REGEX.find(text)
        if (match == null) {
            return DecodeResult(
                success = false,
                secretMessage = "",
                mode = StegoMode.TOKEN_CHAFFING,
                bitsExtracted = "",
                checksumValid = false,
                errorMessage = "No tracking or reference token found in text (#TRK-XXXX, #REF-XXXX)."
            )
        }

        val rawHex = match.groupValues[1].replace("-", "")
        if (rawHex.length % 2 != 0) {
            return DecodeResult(
                success = false,
                secretMessage = "",
                mode = StegoMode.TOKEN_CHAFFING,
                bitsExtracted = "",
                checksumValid = false,
                errorMessage = "Invalid hexadecimal token format."
            )
        }

        val bytes = ByteArray(rawHex.length / 2)
        for (i in bytes.indices) {
            bytes[i] = rawHex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }

        val secretText = try {
            if (!passphrase.isNullOrBlank()) {
                CryptoHelper.decrypt(bytes, passphrase)
            } else {
                String(bytes, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            return DecodeResult(
                success = false,
                secretMessage = "",
                mode = StegoMode.TOKEN_CHAFFING,
                bitsExtracted = "${bytes.size * 8} bits",
                checksumValid = false,
                errorMessage = "Decryption error: ${e.message ?: "Invalid passphrase or corrupted token"}"
            )
        }

        return DecodeResult(
            success = true,
            secretMessage = secretText,
            mode = StegoMode.TOKEN_CHAFFING,
            bitsExtracted = "${bytes.size * 8} bits (${bytes.size} bytes)",
            checksumValid = true
        )
    }
}

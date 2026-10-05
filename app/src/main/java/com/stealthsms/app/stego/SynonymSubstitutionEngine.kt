package com.stealthsms.app.stego

import java.util.Locale

object SynonymSubstitutionEngine {

    val PAIRS = listOf(
        Pair("cannot", "can't"),
        Pair("do not", "don't"),
        Pair("will not", "won't"),
        Pair("is not", "isn't"),
        Pair("have not", "haven't"),
        Pair("would not", "wouldn't"),
        Pair("are not", "aren't"),
        Pair("it is", "it's"),
        Pair("does not", "doesn't"),
        Pair("could not", "couldn't"),
        Pair("should not", "shouldn't"),
        Pair("did not", "didn't"),
        Pair("they are", "they're"),
        Pair("we are", "we're"),
        Pair("you are", "you're"),
        Pair("there is", "there's"),
        Pair("i will", "i'll"),
        Pair("i have", "i've"),
        Pair("let us", "let's"),
        Pair("that is", "that's"),
        Pair("who is", "who's"),
        Pair("what is", "what's"),
        Pair("start", "begin"),
        Pair("quick", "fast"),
        Pair("big", "large"),
        Pair("require", "need"),
        Pair("help", "assist"),
        Pair("buy", "purchase"),
        Pair("show", "display"),
        Pair("smart", "clever"),
        Pair("glad", "happy"),
        Pair("make", "create"),
        Pair("reply", "respond"),
        Pair("simple", "easy"),
        Pair("stay", "remain"),
        Pair("talk", "speak"),
        Pair("stop", "halt"),
        Pair("small", "little")
    )

    private const val PREAMBLE = "1010"

    // Default template sentences to generate natural cover texts with rich pair density
    private val CARRIER_TEMPLATES = listOf(
        "I have some updates, and it is going to start soon.",
        "We are ready, so do not hesitate if you require any help.",
        "I will make sure they are informed, that is for sure.",
        "There is no doubt that cannot be resolved in a simple way.",
        "We will talk later, so stay calm and let us begin.",
        "I am glad to see what is happening, and it is a quick turn.",
        "They are coming over, and we are going to show the plan.",
        "You are right that does not need to take a big effort.",
        "I have seen that it is not hard to purchase the supplies.",
        "Who is able to assist if we are not ready on time?",
        "I will reply as soon as there is a fast answer."
    )

    fun encode(secretText: String, customCover: String? = null, passphrase: String? = null): StegoResult {
        val payloadBytes = if (!passphrase.isNullOrBlank()) {
            CryptoHelper.encrypt(secretText, passphrase)
        } else {
            secretText.toByteArray(Charsets.UTF_8)
        }

        // Build bitstream: PREAMBLE(4) + LENGTH_BYTES(8) + PAYLOAD(N*8) + CHECKSUM(4)
        val bitSb = StringBuilder(PREAMBLE)

        // 8-bit length (number of bytes)
        val lenBits = payloadBytes.size.toString(2).padStart(8, '0')
        bitSb.append(lenBits)

        // Payload bits
        var checksumSum = 0
        for (b in payloadBytes) {
            val ubyte = b.toInt() and 0xFF
            checksumSum += ubyte
            bitSb.append(ubyte.toString(2).padStart(8, '0'))
        }

        // 4-bit checksum
        val crc4 = (checksumSum % 15).toString(2).padStart(4, '0')
        bitSb.append(crc4)

        val fullBits = bitSb.toString()
        val neededBits = fullBits.length

        // Choose or generate cover text
        val baseCover = if (!customCover.isNullOrBlank()) {
            customCover
        } else {
            generateCoverParagraph(neededBits)
        }

        // Substitute words in cover text
        val substituted = substituteBitsIntoText(baseCover, fullBits)

        return StegoResult(
            encodedText = substituted,
            capacityBits = countSubstitutionSites(baseCover),
            usedBits = neededBits,
            mode = StegoMode.CONTRACTION_SYNONYM,
            isEncrypted = !passphrase.isNullOrBlank()
        )
    }

    fun decode(text: String, passphrase: String? = null): DecodeResult {
        val bits = extractBitsFromText(text)
        if (bits.length < 16) {
            return DecodeResult(
                success = false,
                secretMessage = "",
                mode = StegoMode.CONTRACTION_SYNONYM,
                bitsExtracted = bits,
                checksumValid = false,
                errorMessage = "Not enough substitution bits found (${bits.length} bits, minimum 16 required)."
            )
        }

        // Check preamble
        if (!bits.startsWith(PREAMBLE)) {
            return DecodeResult(
                success = false,
                secretMessage = "",
                mode = StegoMode.CONTRACTION_SYNONYM,
                bitsExtracted = bits,
                checksumValid = false,
                errorMessage = "Steganography preamble not matched (received ${bits.take(4)}, expected $PREAMBLE)."
            )
        }

        val lenBits = bits.substring(4, 12)
        val byteCount = lenBits.toInt(2)
        val expectedTotalBits = 12 + (byteCount * 8) + 4

        if (bits.length < expectedTotalBits) {
            return DecodeResult(
                success = false,
                secretMessage = "",
                mode = StegoMode.CONTRACTION_SYNONYM,
                bitsExtracted = bits,
                checksumValid = false,
                errorMessage = "Truncated payload: expected $expectedTotalBits bits, found only ${bits.length} bits."
            )
        }

        val payloadBytes = ByteArray(byteCount)
        var sum = 0
        var offset = 12
        for (i in 0 until byteCount) {
            val byteStr = bits.substring(offset, offset + 8)
            val byteVal = byteStr.toInt(2)
            payloadBytes[i] = byteVal.toByte()
            sum += byteVal
            offset += 8
        }

        val crcBits = bits.substring(offset, offset + 4)
        val expectedCrc = (sum % 15).toString(2).padStart(4, '0')
        val checksumOk = (crcBits == expectedCrc)

        val secretText = try {
            if (!passphrase.isNullOrBlank()) {
                CryptoHelper.decrypt(payloadBytes, passphrase)
            } else {
                String(payloadBytes, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            return DecodeResult(
                success = false,
                secretMessage = "",
                mode = StegoMode.CONTRACTION_SYNONYM,
                bitsExtracted = bits,
                checksumValid = checksumOk,
                errorMessage = "Decryption error: ${e.message ?: "Invalid passphrase or corrupted data"}"
            )
        }

        return DecodeResult(
            success = true,
            secretMessage = secretText,
            mode = StegoMode.CONTRACTION_SYNONYM,
            bitsExtracted = bits,
            checksumValid = checksumOk,
            errorMessage = if (!checksumOk) "Warning: Checksum mismatch (possible telecom text alteration)" else null
        )
    }

    private fun generateCoverParagraph(neededBits: Int): String {
        val sb = StringBuilder()
        var currentBits = 0
        var templateIdx = 0

        while (currentBits < neededBits && templateIdx < CARRIER_TEMPLATES.size * 3) {
            val t = CARRIER_TEMPLATES[templateIdx % CARRIER_TEMPLATES.size]
            if (sb.isNotEmpty()) sb.append(" ")
            sb.append(t)
            currentBits = countSubstitutionSites(sb.toString())
            templateIdx++
        }
        return sb.toString()
    }

    private fun countSubstitutionSites(text: String): Int {
        val lowered = text.lowercase(Locale.US)
        var count = 0
        for ((f0, f1) in PAIRS) {
            count += countMatches(lowered, f0) + countMatches(lowered, f1)
        }
        return count
    }

    private fun substituteBitsIntoText(cover: String, bits: String): String {
        var result = cover
        var bitIndex = 0

        // Find candidate sites and their positions
        val sites = mutableListOf<SiteMatch>()
        val lowered = result.lowercase(Locale.US)

        for ((f0, f1) in PAIRS) {
            findAllWordMatches(result, f0).forEach { sites.add(SiteMatch(it, f0, 0, f0, f1)) }
            findAllWordMatches(result, f1).forEach { sites.add(SiteMatch(it, f1, 1, f0, f1)) }
        }

        // Sort sites by appearance order in text
        sites.sortBy { it.index }

        // Perform substitution
        // To avoid index shifts, process in reverse or rebuild
        val sortedSites = sites.distinctBy { it.index }

        val sb = StringBuilder()
        var lastPos = 0

        for (site in sortedSites) {
            if (site.index < lastPos) continue // overlap protection
            sb.append(result.substring(lastPos, site.index))

            val originalWord = result.substring(site.index, site.index + site.matchedWord.length)
            val isCapitalized = originalWord.firstOrNull()?.isUpperCase() ?: false

            val replacement = if (bitIndex < bits.length) {
                val targetBit = bits[bitIndex]
                bitIndex++
                if (targetBit == '0') site.pair0 else site.pair1
            } else {
                site.matchedWord
            }

            val finalWord = if (isCapitalized) {
                replacement.replaceFirstChar { it.titlecase(Locale.US) }
            } else {
                replacement
            }

            sb.append(finalWord)
            lastPos = site.index + site.matchedWord.length
        }

        if (lastPos < result.length) {
            sb.append(result.substring(lastPos))
        }

        return sb.toString()
    }

    private fun extractBitsFromText(text: String): String {
        val sites = mutableListOf<Pair<Int, Char>>()
        for ((f0, f1) in PAIRS) {
            findAllWordMatches(text, f0).forEach { sites.add(Pair(it, '0')) }
            findAllWordMatches(text, f1).forEach { sites.add(Pair(it, '1')) }
        }

        // Sort by text position
        sites.sortBy { it.first }
        val distinctSites = sites.distinctBy { it.first }
        return distinctSites.map { it.second }.joinToString("")
    }

    private fun findAllWordMatches(text: String, word: String): List<Int> {
        val matches = mutableListOf<Int>()
        // Word boundary match regex
        val regex = Regex("\\b${Regex.escape(word)}\\b", RegexOption.IGNORE_CASE)
        for (m in regex.findAll(text)) {
            matches.add(m.range.first)
        }
        return matches
    }

    private fun countMatches(text: String, word: String): Int {
        val regex = Regex("\\b${Regex.escape(word)}\\b", RegexOption.IGNORE_CASE)
        return regex.findAll(text).count()
    }

    private data class SiteMatch(
        val index: Int,
        val matchedWord: String,
        val currentBit: Int,
        val pair0: String,
        val pair1: String
    )
}

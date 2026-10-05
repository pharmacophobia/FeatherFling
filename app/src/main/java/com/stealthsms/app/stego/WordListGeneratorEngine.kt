package com.stealthsms.app.stego

object WordListGeneratorEngine {

    // 256 standard common English words (1 word = 8 bits / 1 byte)
    val WORD_LIST = listOf(
        "abandon", "ability", "able", "about", "above", "absent", "absorb", "abstract",
        "access", "accident", "account", "accuse", "achieve", "acid", "acoustic", "acquire",
        "across", "act", "action", "active", "actor", "actress", "actual", "adapt",
        "add", "addict", "address", "adjust", "admit", "adult", "advance", "advice",
        "aerobic", "affair", "afford", "afraid", "again", "agent", "agree", "ahead",
        "aim", "air", "airport", "aisle", "alarm", "album", "alcohol", "alert",
        "alien", "all", "alley", "allow", "almost", "alone", "alpha", "already",
        "also", "alter", "always", "amateur", "amazing", "among", "amount", "amused",
        "analyst", "anchor", "ancient", "anger", "angle", "angry", "animal", "ankle",
        "announce", "annual", "another", "answer", "antenna", "antique", "anxiety", "any",
        "apart", "apology", "appear", "apple", "approve", "april", "arch", "arctic",
        "area", "arena", "argue", "arm", "armed", "armor", "army", "around",
        "arrange", "arrest", "arrive", "arrow", "art", "artefact", "artist", "artwork",
        "ask", "aspect", "assault", "asset", "assist", "assume", "asthma", "athlete",
        "atom", "attack", "attend", "attitude", "attract", "auction", "audit", "august",
        "aunt", "author", "auto", "autumn", "average", "avocado", "avoid", "awake",
        "aware", "away", "awesome", "awful", "awkward", "axis", "baby", "bachelor",
        "bacon", "badge", "bag", "balance", "balcony", "ball", "bamboo", "banana",
        "banner", "bar", "barely", "bargain", "barrel", "base", "basic", "basket",
        "battle", "beach", "bean", "beauty", "because", "become", "beef", "before",
        "begin", "behave", "behind", "believe", "below", "belt", "bench", "benefit",
        "best", "betray", "better", "between", "beyond", "bicycle", "bid", "bike",
        "bind", "biology", "bird", "birth", "bitter", "black", "blade", "blame",
        "blanket", "blast", "bleak", "bless", "blind", "blood", "blossom", "blouse",
        "blue", "blur", "blush", "board", "boat", "body", "boil", "bomb",
        "bone", "bonus", "book", "boost", "border", "boring", "borrow", "boss",
        "bottom", "bounce", "box", "boy", "bracket", "brain", "brand", "brass",
        "brave", "bread", "breeze", "brick", "bridge", "brief", "bright", "bring",
        "brisk", "broccoli", "broken", "bronze", "broom", "brother", "brown", "brush",
        "bubble", "buddy", "budget", "buffalo", "build", "bulb", "bulk", "bullet",
        "bundle", "bunker", "burden", "burger", "burst", "bus", "business", "busy",
        "butter", "buyer", "buzz", "cabbage", "cabin", "cable", "cactus", "cage"
    )

    private val WORD_TO_INDEX: Map<String, Int> = WORD_LIST.mapIndexed { index, word -> word to index }.toMap()

    private const val PREFIX = "Project dispatch log: "
    private const val SUFFIX = " -- verified."

    fun encode(secretText: String, passphrase: String? = null): StegoResult {
        val payloadBytes = if (!passphrase.isNullOrBlank()) {
            CryptoHelper.encrypt(secretText, passphrase)
        } else {
            secretText.toByteArray(Charsets.UTF_8)
        }

        val words = payloadBytes.map { byte ->
            val index = byte.toInt() and 0xFF
            WORD_LIST[index % WORD_LIST.size]
        }

        val coverSentence = "$PREFIX${words.joinToString(" ")}$SUFFIX"

        return StegoResult(
            encodedText = coverSentence,
            capacityBits = payloadBytes.size * 8,
            usedBits = payloadBytes.size * 8,
            mode = StegoMode.WORDLIST_GENERATOR,
            isEncrypted = !passphrase.isNullOrBlank()
        )
    }

    fun decode(text: String, passphrase: String? = null): DecodeResult {
        val clean = text.trim()
        val content = if (clean.contains(PREFIX)) {
            val afterPrefix = clean.substringAfter(PREFIX)
            if (afterPrefix.contains(SUFFIX)) {
                afterPrefix.substringBefore(SUFFIX)
            } else {
                afterPrefix
            }
        } else {
            clean
        }

        val tokens = content.split(Regex("[^a-zA-Z]+")).filter { it.isNotBlank() }
        val matchedBytes = mutableListOf<Byte>()

        for (token in tokens) {
            val lower = token.lowercase()
            val idx = WORD_TO_INDEX[lower]
            if (idx != null) {
                matchedBytes.add(idx.toByte())
            }
        }

        if (matchedBytes.isEmpty()) {
            return DecodeResult(
                success = false,
                secretMessage = "",
                mode = StegoMode.WORDLIST_GENERATOR,
                bitsExtracted = "",
                checksumValid = false,
                errorMessage = "No recognized dictionary words found in text."
            )
        }

        val byteArray = matchedBytes.toByteArray()
        val secretText = try {
            if (!passphrase.isNullOrBlank()) {
                CryptoHelper.decrypt(byteArray, passphrase)
            } else {
                String(byteArray, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            return DecodeResult(
                success = false,
                secretMessage = "",
                mode = StegoMode.WORDLIST_GENERATOR,
                bitsExtracted = "${matchedBytes.size * 8} bits",
                checksumValid = false,
                errorMessage = "Decryption error: ${e.message ?: "Invalid passphrase or corrupted words"}"
            )
        }

        return DecodeResult(
            success = true,
            secretMessage = secretText,
            mode = StegoMode.WORDLIST_GENERATOR,
            bitsExtracted = "${matchedBytes.size * 8} bits (${matchedBytes.size} words)",
            checksumValid = true
        )
    }
}

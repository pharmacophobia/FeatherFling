package com.stealthsms.app.stego

object StegoManager {

    fun encode(
        secretText: String,
        mode: StegoMode,
        customCover: String? = null,
        passphrase: String? = null
    ): StegoResult {
        return when (mode) {
            StegoMode.CONTRACTION_SYNONYM -> SynonymSubstitutionEngine.encode(secretText, customCover, passphrase)
            StegoMode.WORDLIST_GENERATOR -> WordListGeneratorEngine.encode(secretText, passphrase)
            StegoMode.TOKEN_CHAFFING -> TokenChaffingEngine.encode(secretText, passphrase)
        }
    }

    fun decodeAuto(text: String, passphrase: String? = null): DecodeResult {
        // 1. Try Token Chaffing first (very specific regex match)
        if (text.contains(Regex("#(?:TRK|REF|CONF)-[0-9a-fA-F-]+"))) {
            val res = TokenChaffingEngine.decode(text, passphrase)
            if (res.success) return res
        }

        // 2. Try Word-List Generator if prefix is present
        if (text.contains("Project dispatch log:")) {
            val res = WordListGeneratorEngine.decode(text, passphrase)
            if (res.success) return res
        }

        // 3. Try Synonym & Contraction Substitution
        val synRes = SynonymSubstitutionEngine.decode(text, passphrase)
        if (synRes.success) return synRes

        // 4. Try Word-List fallback
        val wordRes = WordListGeneratorEngine.decode(text, passphrase)
        if (wordRes.success) return wordRes

        // If none succeeded, return the most descriptive error
        return synRes
    }
}

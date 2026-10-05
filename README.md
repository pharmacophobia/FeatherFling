# Feather Fling 🪶📱
### Linguistic & Structural Steganography for SMS & Carrier-Transcoded Messaging

**Feather Fling** is an Android application designed to reliably conceal, transmit, and extract secret messages through standard SMS text messages, chat platforms (WhatsApp, Signal, Telegram), and web forms without raising suspicion.

Unlike fragile byte-level steganography (such as zero-width Unicode characters like `\u200B`–`\u200F`) that gets stripped by telecom gateways, carrier transcoders, font renderers, or text normalizers, Feather Fling embeds hidden data using **100% standard, printable ASCII characters** that effortlessly survive carrier transcoding, copy-paste operations, and automated spam filters—as light and invisible as a feather.

---

## 🚀 Key Features

### 1. Carrier-Proof Linguistic & Structural Encoding
* **Controlled Synonym & Contraction Substitution**: Maps binary bits (0 and 1) to natural word variations (e.g., `cannot` / `can't`, `start` / `begin`, `large` / `big`, `require` / `need`). Includes a 4-bit synchronization preamble (`1010`), an 8-bit length header, and a 4-bit CRC error detection checksum.
* **Deterministic Word-List Cover Generation**: Encodes bytes into words selected from an innocent 256-word dictionary, assembling natural-sounding cover sentences (analogous to BIP-39 mnemonic phrases).
* **Visible Token Chaffing**: Conceals payloads inside routine metadata tokens (e.g., `#TRK-9F82A4`, `Ref ID: 7b31-e409`) designed to blend seamlessly into order confirmations, receipts, and tracking messages.
* **Optional AES-256-GCM Vault**: Encrypts secret payloads with PBKDF2 key derivation before linguistic encoding for multi-layered confidentiality.

### 2. Deep Android System Integration
* **System Text Selection (`ACTION_PROCESS_TEXT`)**: Highlight text in *any* Android app (Google Messages, WhatsApp, Signal, web browsers), tap the context menu, and select **"Feather Fling"** to extract hidden messages immediately.
* **Universal SMS Dispatcher**: Launches native messaging apps via `Intent.ACTION_SENDTO` with pre-filled cover text and target phone numbers—zero dangerous runtime permissions required.
* **One-Tap Share & Copy**: Instant clipboard copying and system share sheet dispatch.

### 3. Telecom Test Lab
* Built-in carrier simulation engine to test payload survivability against:
  * GSM 7-bit character transcoding
  * Web form whitespace collapsing (double spaces to single spaces)
  * Unicode normalization (NFKD/NFC)
  * Case flattening

---

## 🛠️ How It Works

### Technique Comparison: Why Zero-Width Characters Fail

| Technique | GSM 7-Bit SMS Safe? | Web Form Safe? | Filter Resistant? | Visual Detection Risk |
| :--- | :---: | :---: | :---: | :---: |
| **Zero-Width Unicode (`\u200B`–`\u200F`)** | ❌ Stripped | ❌ Stripped / Flagged | ❌ Detected as Malicious | Low |
| **Whitespace Encoding** | ❌ Collapsed | ❌ Collapsed | ⚠️ Suspicious | Low |
| **Feather Fling Synonym Substitution** | ✅ **100% Survives** | ✅ **100% Survives** | ✅ **Looks Natural** | **Zero** |
| **Feather Fling Word-List Generator** | ✅ **100% Survives** | ✅ **100% Survives** | ✅ **Looks Natural** | **Zero** |
| **Feather Fling Chaffed Token** | ✅ **100% Survives** | ✅ **100% Survives** | ✅ **Appears Routine** | **Zero** |

---

## 🏗️ Architecture & Tech Stack

* **Language**: Kotlin 2.0.0
* **UI Toolkit**: Jetpack Compose with Material 3 (Dark Theme)
* **Crypto Engine**: AES-256-GCM (`AES/GCM/NoPadding`) + PBKDF2-HMAC-SHA256
* **Architecture**: Clean Architecture / MVVM with unidirectional data flow
* **Compatibility**: Android 8.0 (API 26) through Android 14+ (API 34+)

---

## 📦 Building from Source

### Prerequisites
* JDK 17
* Android SDK (API 34, Build-Tools 34.0.0)

### Clone & Assemble
```bash
git clone https://github.com/pharmacophobia/FeatherFling.git
cd FeatherFling
./gradlew assembleDebug
```

The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License
MIT License. Created for security research, linguistic steganography analysis, and covert communication testing.

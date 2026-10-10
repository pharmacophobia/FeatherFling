# Feather Fling 🪶📱
### Linguistic & Structural Steganography SMS Messenger with Stealth Arcade Disguise

<div align="center">
  <img src="docs/feather_fling_bird_icon.png" width="160" height="160" alt="Feather Fling Cartoon Bird Icon" style="border-radius: 28px;" />
  <br>
  <b>A full-featured Android SMS messenger with carrier-proof steganography and a stealth decoy game startup screen.</b>
</div>

---

## 📥 Direct APK Download & Install

| Direct 1-Tap Download | Scan QR Code with Phone Camera to Install |
| :---: | :---: |
| [![Download APK](https://img.shields.io/badge/Download-FeatherFling.apk-7C4DFF?style=for-the-badge&logo=android&logoColor=white)](https://github.com/pharmacophobia/FeatherFling/raw/main/FeatherFling.apk)<br><br>👉 **[Click here to download FeatherFling.apk (11.7 MB)](https://github.com/pharmacophobia/FeatherFling/raw/main/FeatherFling.apk)**<br><br>📦 Official GitHub Release: **[v1.2.0](https://github.com/pharmacophobia/FeatherFling/releases/tag/v1.2.0)** | <img src="https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=https://github.com/pharmacophobia/FeatherFling/raw/main/FeatherFling.apk" width="180" height="180" alt="Scan to install APK" /><br>*(Point your phone camera to download)* |

---

## 🚀 Key Features in v1.2.0

### 1. 💬 Native In-App SMS Messenger
* **Everyday SMS Operation**: Acts as your default daily texting app. Reads system SMS conversations, displays message threads, and sends/receives real carrier SMS texts.
* **Real-Time Incoming Texts**: Built-in broadcast receiver immediately detects and displays newly arrived text messages.
* **Zero Suspicion**: Operates and appears identically to a clean, modern messaging app until the stego vault is intentionally unlocked.

### 2. 🪶 Discreet Bird Mascot Trigger
* **Tap the Cartoon Bird to Transform**: A cute cartoon bird avatar is subtly positioned in the top corner of the messenger and chat screens.
* **In-Conversation Stego Mode**:
  * Tapping the bird mascot inside a conversation activates the **Stego Toolbar**.
  * Type your secret message in the compose bar, select your encoding algorithm (Word-List Mnemonic, Synonym Substitution, Chaffed Token), and tap **Fling Secret** to encode and dispatch innocent cover text over SMS!
  * **Instant In-Place Decode**: Any message containing potential hidden payloads displays a 1-tap **🔓 Decode Secret** button to extract the hidden payload directly inside the conversation thread.
* **Stego Vault Suite**: Tapping the bird mascot from the main screen opens the comprehensive stego control center:
  * Manual Encoder & Custom Cover Creator
  * Manual Multi-Engine Decoder with Bitstream Analysis
  * Cellular Telecom Test Lab (GSM 7-bit, whitespace collapse, Unicode normalization)
  * Linguistic Steganography Reference Guide

### 3. 🎮 Stealth Arcade Game Startup Disguise
* **Decoy Game Menu**: When enabled, launching the app presents a retro arcade mobile game title screen: **"FEATHER FLING: SKY QUEST"**.
* **Decoy Actions**: Menu buttons (*Start Game*, *High Scores*, *Settings*, *Exit to System*) immediately close the application, completely concealing the true messenger from prying eyes.
* **Secret Corner Bird Launch**: Tapping the cartoon bird mascot in the top-right corner unlocks and launches the real SMS Messenger!

### 4. 🔐 Biometric Lock on Bird Mascot
* **Fingerprint & Face Authentication**: Enable optional biometric security in settings. Tapping the corner bird mascot on the game screen prompts the Android `BiometricPrompt` (fingerprint, face, or secure screen lock PIN) before granting access.

### 5. 🐦 Custom Cartoon Bird Launcher Icon
* Adorable cartoon bird with glowing cyan, teal, and electric violet plumage.
* Packaged across all Android mipmap densities (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) with adaptive icon support.

---

## 🛠️ Carrier-Proof Steganography Techniques

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
* **Security & Auth**: AndroidX Biometric (`BiometricPrompt`)
* **Telephony**: Android SMS Manager, `Telephony.Sms` ContentProvider, and SMS BroadcastReceiver
* **Crypto Engine**: AES-256-GCM (`AES/GCM/NoPadding`) + PBKDF2-HMAC-SHA256
* **Compatibility**: Android 8.0 (API 26) through Android 14+ (API 34+)

---

## 📦 Building from Source

```bash
git clone https://github.com/pharmacophobia/FeatherFling.git
cd FeatherFling
./gradlew assembleRelease
```

The compiled release APK will be generated at:
```
app/build/outputs/apk/release/app-release.apk
```

---

## 📄 License
MIT License. Created for security research, linguistic steganography analysis, and covert communication testing.

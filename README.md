# 🛡️ ValoMobile

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?logo=android)](https://developer.android.com/jetpack/compose)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-green.svg?logo=android)](https://android.com)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)
[![Security](https://img.shields.io/badge/Security-AES--256%20Hardware%20Encrypted-success.svg)](https://developer.android.com/topic/security/data)

**ValoMobile** is a modern, standalone, and privacy-focused Android application designed for **Valorant** players to check their daily shop rotation, featured bundles, and Night Market offers directly from their mobile device.

---

## ✨ Features

- 🛒 **Daily Store Rotation**: View your 4 daily personal skin offers with base VP prices and high-definition weapon renders.
- 🎁 **Featured Bundles**: Browse active featured skin collections with full bundle items and base standalone pricing.
- 🌙 **Night Market Support**: Automatic detection and display of personal discounted Night Market cards during active events.
- ⭐ **Wishlist & Background Notifications**: Add favorite skins to your wishlist and receive timely push notifications via Android `WorkManager` when they appear in your shop.
- 📖 **Complete Skin Catalog**: Search and inspect any skin tier, edition, and weapon category with metadata powered by `valorant-api.com`.
- 🔒 **Zero-Knowledge Security**:
  - Direct connection to official Riot Games APIs (`auth.riotgames.com`, `pd.eu.a.pvp.net`).
  - Passwords are **never** processed or stored by the app. Authentication uses official Riot OAuth 2.0.
  - Session tokens are encrypted on-device with hardware-backed **AES-256-GCM** via `EncryptedSharedPreferences` and **Android KeyStore**.
- 🚀 **100% Standalone**: No external server, PC companion app, or third-party proxy required.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework**: Modern declarative UI with [Jetpack Compose](https://developer.android.com/jetpack/compose) & [Material Design 3](https://m3.material.io/).
- **Architecture**: Clean Architecture with MVVM, unidirectional data flow (UDF), and reactive Kotlin `StateFlow`.
- **Dependency Injection**: [Dagger Hilt](https://dagger.dev/hilt/).
- **Networking**: [Retrofit 2](https://square.github.io/retrofit/) & [OkHttp 3](https://square.github.io/okhttp/) with strict TLS/HTTPS enforcement.
- **Local Storage**: [Room Database](https://developer.android.com/training/data-storage/room) (SQLite) for wishlist items and [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences) for session storage.
- **Background Processing**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for periodic shop verification and notifications.
- **Image Loading**: [Coil Compose](https://coil-kt.github.io/coil/compose/).

---

## 📥 Getting Started & Building

### Prerequisites
- **Android Studio** Ladybug (or newer)
- **JDK 17** or **JDK 21**
- **Android SDK** API 34+ (minSdk: 24, targetSdk: 35)

### Build Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/ValoMobile.git
   cd ValoMobile
   ```

2. **Open the project in Android Studio** and sync Gradle.

3. **Build the Debug APK**:
   ```bash
   # Windows PowerShell
   .\gradlew.bat assembleDebug

   # macOS / Linux
   ./gradlew assembleDebug
   ```

4. The generated APK will be available in:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## 🛡️ Privacy & Security Policy

1. **Direct Communication**: ValoMobile communicates strictly and directly with official Riot Games servers (`*.riotgames.com`, `*.pvp.net`) and the community API (`valorant-api.com`).
2. **No Data Collection**: There are zero tracking scripts, telemetry, advertisements, or third-party analytics servers.
3. **Open Source**: The code is 100% transparent and open for inspection.

---

## ⚖️ Legal Disclaimer

ValoMobile isn't endorsed by Riot Games and doesn't reflect the views or opinions of Riot Games or anyone officially involved in producing or managing Riot Games properties. Riot Games, and all associated properties are trademarks or registered trademarks of Riot Games, Inc.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

## 🤖 AI
This project was built entirely through vibe coding using AI tools. It is provided as-is, mainly as an experiment and a fun exploration of AI-assisted development.
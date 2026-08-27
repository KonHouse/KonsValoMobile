# 🛡️ ValoMobile

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?logo=android)](https://developer.android.com/jetpack/compose)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-green.svg?logo=android)](https://android.com)
[![Firebase](https://img.shields.io/badge/Firebase-Firestore%20Cloud-FFCA28.svg?logo=firebase)](https://firebase.google.com)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)
[![Security](https://img.shields.io/badge/Security-AES--256%20Hardware%20Encrypted-success.svg)](https://developer.android.com/topic/security/data)
[![Version](https://img.shields.io/badge/Version-1.4.1-red.svg)](https://github.com/KonHouse/KonsValoMobile/releases)

**ValoMobile** is a modern, standalone, and privacy-focused Android application designed for **Valorant** players to check their daily shop rotation, featured bundles, Night Market offers, daily streaks, store history, and share stores with friends directly from their mobile device.

---

## ✨ Features

### 👥 In-App Friends & Live Store Sharing (v1.4.1)
- **Unique Friend Code**: Each player gets a shareable code (e.g. `VALO-7X9K`) with 1-click copy and share to Discord/Messenger.
- **Live Daily Store Sharing**: See what 4 skins your friends rolled in their daily shops today in real time via **Google Firebase Firestore**.
- **Inspect Friend's Store**: Open detailed store preview dialogs to check friends' skin editions, prices, and weapon tiers.
- **Invites & Search**: Search friends by Riot ID, friend code, or specific skin name (e.g. search *"Prime"* to see who has it in their shop).

### 🔥 Daily Streak System & Rewards
- **Daily Visit Tracking**: Earn and maintain your daily check-in streak with an animated flame counter (`🔥 7`) on the top bar.
- **Level-Up Celebrations**: Full-screen celebratory check-in dialog with streak recovery support.
- **Multi-Account Isolation**: Streaks, store caches, and preferences are strictly isolated per Riot account PUUID.

### 📅 Offers History Calendar
- **Interactive Calendar**: Browse all past daily store rotations recorded on your device.
- **Historic Skin Cards**: View weapon renders, prices, and collection dates for any previous day.

### 🛒 Store Rotation, Bundles & Night Market
- **Daily Personal Store**: View your 4 daily personal skin offers with base VP prices and high-definition weapon renders.
- **In-Game Video Previews**: Watch full weapon inspection and finisher animations powered by hardware-accelerated **ExoPlayer**.
- **Featured Bundles**: Browse active featured skin collections with full bundle items and countdown timers.
- **Night Market**: Automatic detection and display of personal discounted Night Market cards during active events.

### 💰 VP Calculator & Live Wallet
- **Real-Time Balances**: Live Valorant Points (VP) and Radianite Points (RP) balances displayed in the top bar.
- **VP Pack Calculator**: Calculate exact real-money cost and optimal VP bundle combinations for your wishlist or shop items (supports EUR, PLN, USD, GBP, CAD, AUD, BRL).

### ⭐ Wishlist & Background Notifications
- **Wishlist Tracking**: Mark favorite skins and receive timely background push notifications via Android **WorkManager** when they appear in your daily store.

### 🔒 Zero-Knowledge Security & Privacy
- **Direct Riot Connection**: Communicates directly with official Riot Games authentication servers (`auth.riotgames.com`) via official OAuth 2.0.
- **Hardware-Backed AES-256 Encryption**: Tokens are stored securely using `EncryptedSharedPreferences` backed by the device's hardware security module (**Android KeyStore / TEE**).
- **Zero Token Leakage to Cloud**: No passwords, tokens, or session cookies are ever sent to Firebase or external servers.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework**: Modern declarative UI with [Jetpack Compose](https://developer.android.com/jetpack/compose) & [Material Design 3](https://m3.material.io/).
- **Adaptive Layouts**: Full responsive support for phones, foldables, and tablets using Compose Adaptive & Navigation 3.
- **Architecture**: Clean Architecture with MVVM, Unidirectional Data Flow (UDF), and reactive Kotlin `StateFlow`.
- **Dependency Injection**: [Dagger Hilt](https://dagger.dev/hilt/).
- **Cloud Backend**: [Google Firebase Firestore](https://firebase.google.com/docs/firestore) for real-time friend codes, invites, and daily store sharing.
- **Local Storage**: [Room Database](https://developer.android.com/training/data-storage/room) (SQLite) for wishlist and store history; [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences) for secure auth tokens.
- **Networking**: [Retrofit 2](https://square.github.io/retrofit/) & [OkHttp 3](https://square.github.io/okhttp/) with strict TLS/HTTPS network security configuration.
- **Media Playback**: [Jetpack Media3 ExoPlayer](https://developer.android.com/media/media3) for in-game skin video previews.
- **Image Loading**: [Coil Compose](https://coil-kt.github.io/coil/compose/).
- **Background Processing**: [Android WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager).

---

## 📥 Getting Started & Building

### Prerequisites
- **Android Studio** Ladybug | 2024.2+ (or newer)
- **JDK 17** or **JDK 21**
- **Android SDK** API 34+ (minSdk: 24, targetSdk: 37)

### Build Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/KonHouse/KonsValoMobile.git
   cd KonsValoMobile
   ```

2. **Open the project in Android Studio** and sync Gradle.

3. **Build Debug APK**:
   ```powershell
   # Windows PowerShell
   .\gradlew.bat assembleDebug

   # macOS / Linux
   ./gradlew assembleDebug
   ```

4. The generated APK will be available in:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## 🛡️ Privacy & Security Policy

1. **Direct Communication**: ValoMobile communicates strictly and directly with official Riot Games servers (`*.riotgames.com`, `*.pvp.net`), the community API (`valorant-api.com`), and Google Firebase Firestore for social store sharing.
2. **Zero Password Handling**: The app never asks for, reads, or stores your Riot Games password. Login is handled directly by Riot's official web authentication flow.
3. **Open Source & Audited**: The code is 100% transparent and verified against security vulnerabilities.

---

## ⚖️ Legal Disclaimer

ValoMobile isn't endorsed by Riot Games and doesn't reflect the views or opinions of Riot Games or anyone officially involved in producing or managing Riot Games properties. Riot Games, and all associated properties are trademarks or registered trademarks of Riot Games, Inc.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

## 🤖 AI

This project was built entirely through vibe coding using AI tools. It is provided as-is, mainly as an experiment and a fun exploration of AI-assisted development.
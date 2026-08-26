# Security Policy — ValoMobile

## 🛡️ Security Architecture & Privacy Overview

ValoMobile is engineered from the ground up with a **Zero-Knowledge** and privacy-first design philosophy:

1. **Zero Password Handling**:
   - The application **never** prompts for, intercepts, processes, or stores your Riot Games password.
   - Authentication is performed strictly via Riot Games' official **OAuth 2.0 (RSO)** protocol.
   - Credentials are exchanged directly with `https://auth.riotgames.com/`.

2. **Hardware-Backed AES-256 Encryption**:
   - Authentication tokens (`access_token`, `entitlements_token`) are stored using `androidx.security.crypto.EncryptedSharedPreferences`.
   - Keys are managed in the device's hardware security module (**Android KeyStore / TEE**).
   - Sensitive auth stores are strictly excluded from Android Cloud Backups (`backup_rules.xml` and `data_extraction_rules.xml`).

3. **Strict Network Security**:
   - `android:usesCleartextTraffic` is **disabled**.
   - All network traffic is exclusively transmitted over encrypted **TLS / HTTPS**.
   - In production releases, HTTP logging is completely disabled (`Level.NONE`) to ensure no auth tokens leak into system logs (`Logcat`).

4. **Cryptographic Release Signing**:
   - The official release builds are signed using an RSA-2048 Keystore with **APK Signature Schemes v1, v2, v3, and v4**.

5. **In-App Friends & Store Sharing Privacy (Firebase Firestore)**:
   - Only **public in-game metadata** (weapon skin names, VP prices, and friend codes) is synchronized with Firebase Firestore.
   - **Zero Token Leakage**: Riot authentication tokens, passwords, session cookies, and private personal identifiers are **never** transmitted to Firebase or stored in the cloud.
   - Friend invitations and store views use non-sensitive, randomly generated **Friend Codes** (`VALO-XXXX`).

---

## 🔒 Reporting a Vulnerability

If you discover any security vulnerability in this project, please open an issue on GitHub or reach out to the maintainers. We take security seriously and will address any verified concern promptly.

---

## ⚖️ Disclaimer

*ValoMobile is a fan-made companion app and is not affiliated with, endorsed, or sponsored by Riot Games, Inc.*

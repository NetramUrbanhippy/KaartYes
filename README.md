# KaartYes — Loyalty Card Wallet

A minimalist Android loyalty card wallet app. Scan or manually enter card barcodes/QR codes and present them digitally in stores.

---

## Features

- **Google Sign-In** (Firebase Authentication)
- **Encrypted local storage** — SQLCipher + Android Keystore (AES-256)
- **Barcode & QR scanning** — ML Kit, auto-detects format
- **Manual card entry** — type card number + store name
- **Virtual card display** — barcode or QR code shown full-screen
- **Customizable tiles** — pick color and nickname per card
- **Notes** — add a free-text note to each card
- **Photos** — store front/back card photos locally
- **Sorting** — A→Z, Z→A, or most used
- **Multi-language** — Dutch (default) and English

---

## Setup before building

### 1. Firebase project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project (e.g. `kaartyes`)
3. Add an Android app with package name `nl.kaartyes.app`
4. Enable **Google Sign-In** under *Authentication → Sign-in method*
5. Download `google-services.json` and place it at `app/google-services.json`

### 2. Web Client ID

1. In Firebase Console → Project settings → General → Your apps → Web client (auto created by Google Sign-In)
2. Copy the **Web client ID** (looks like `XXXXXXX.apps.googleusercontent.com`)
3. Open `app/build.gradle.kts` and replace:
   ```
   "YOUR_WEB_CLIENT_ID_HERE"
   ```
   with your actual Web Client ID.

### 3. SHA-1 fingerprint

Firebase Google Sign-In requires the app's SHA-1 fingerprint:

```bash
# Debug keystore
./gradlew signingReport
```

Add the SHA-1 in Firebase Console → Project settings → Your apps → Android app → Add fingerprint.

---

## Building

```bash
# Debug build
./gradlew assembleDebug

# Release build (configure signing in build.gradle.kts first)
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

---

## Project structure

```
app/src/main/java/nl/kaartyes/app/
├── data/
│   ├── db/              — Room database (SQLCipher encrypted)
│   ├── security/        — Android Keystore key management
│   └── repository/      — CardRepository
├── domain/model/        — Card, BarcodeFormat, SortOrder
├── di/                  — Hilt modules
└── ui/
    ├── theme/           — Colors, typography, Material3 theme
    ├── navigation/      — NavHost + Screen sealed class
    ├── components/      — CardTile, BarcodeDisplay
    └── screens/
        ├── auth/        — Login (Google Sign-In)
        ├── home/        — Card grid (wallet overview)
        ├── carddetail/  — Barcode/QR display + manage options
        ├── addcard/     — Add card flow (scan or manual)
        ├── editcard/    — Edit name, nickname, color
        ├── notes/       — Add/edit note per card
        └── photos/      — Front/back card photos
```

---

## Security design

| Layer | Mechanism |
|---|---|
| Database | SQLCipher (AES-256-CBC) |
| Database key | 32-byte random key, stored in EncryptedSharedPreferences |
| EncryptedSharedPreferences | Android Keystore (AES-256-GCM) |
| Authentication | Firebase Auth + Google Identity |
| Network | HTTPS only, cleartext blocked (network_security_config.xml) |
| Backup | Cloud & device backup disabled for all sensitive data |

---

## Google Play Store compliance

- `targetSdk 35`, `minSdk 26`
- `android:allowBackup="false"` — no accidental cloud leakage
- Camera permission with runtime request and rationale
- No deceptive UI patterns
- Privacy policy required before publishing — add a URL to your store listing

---

## Adding languages

1. Create `app/src/main/res/values-XX/strings.xml` (replace XX with ISO 639-1 code, e.g. `de`, `fr`)
2. Copy all string keys from `values/strings.xml`
3. Translate the values

Currently available: `nl` (Dutch), `en` (English, default fallback)

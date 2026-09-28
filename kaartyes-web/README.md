# KaartYes Web

De web/PWA-versie van KaartYes: dezelfde loyaliteitskaarten-app als de Android-app, maar draaiend op een URL en installeerbaar als tegel op het startscherm (Android en iOS). Login via Google, data privé per gebruiker in Firebase.

## Hoe het werkt

- **Next.js (App Router)**, volledig client-side gerenderd en als statische site geëxporteerd (`output: "export"`) — geen server nodig, kan overal gehost worden.
- **Firebase Auth** (Google Sign-In) voor login. Op een geïnstalleerde PWA (standalone mode) wordt automatisch `signInWithRedirect` gebruikt in plaats van een popup, omdat popups op het startscherm-icoon onbetrouwbaar zijn.
- **Firestore** voor het opslaan van kaarten, met offline-persistentie (`persistentLocalCache`) zodat kaarten ook zonder verbinding zichtbaar blijven.
- **Serwist** (service worker) + `public/manifest.json` maken de site een installeerbare PWA met het KaartYes-icoon.
- Databeveiliging: `firestore.rules` staan lezen/schrijven alléén toe aan de eigenaar (`request.auth.uid`) van een kaart — geen enkele gebruiker kan bij data van een ander.

**Bewust (nog) niet actief:**
- **Kaartfoto's** (voor-/achterkant) — gebruikt Firebase Storage, wat sinds eind 2024 het betaalde Blaze-plan vereist. De "Foto's"-knop is uit het kaartdetailscherm gehaald; alle code (`lib/storage.ts`, `app/card/photos`, `storage.rules`) staat nog klaar voor als je dit later alsnog wilt. Zie "Foto's alsnog activeren" hieronder.
- **Inloggen met Apple** — code staat klaar in `contexts/AuthContext.tsx` (`signInWithApple`) maar de knop is uit de loginpagina gehaald omdat dit een betaald Apple Developer-account vereist. Zie "1b" hieronder om het weer aan te zetten.

## Eenmalige setup (vereist een Google-account — dit kan alleen door jou gedaan worden)

Dit zijn de enige stappen die niet automatisch te scripten zijn, omdat ze een Google-login in een browser vereisen.

### 1. Firebase-project

Gebruik het bestaande Firebase-project van de Android-app (zelfde `google-services.json` project), of maak een nieuw project op https://console.firebase.google.com.

In de Firebase Console:

1. **Project instellingen → Algemeen → Je apps → Web-app toevoegen** (`</>`-icoon). Geef een naam, bijv. "KaartYes Web". Firebase Hosting hoeft je hier niet in te stellen.
2. Kopieer de getoonde `firebaseConfig` waarden.
3. **Authentication → Sign-in method → Google** → inschakelen.
4. **Firestore Database** → maak een database aan (production mode).

### 1b. (Optioneel) Inloggen met Apple weer activeren

Vereist een **betaald Apple Developer-account** (€99/jaar) — sla deze stap over zolang je dat niet hebt.

In het Apple Developer-portaal (https://developer.apple.com/account):

1. **Certificates, Identifiers & Profiles → Identifiers** → maak een App ID met "Sign In with Apple" capability.
2. Maak een **Services ID** aan (dit wordt de "Apple Client ID"), en koppel er een domain + return URL aan: `https://<jouw-firebase-authDomain>/__/auth/handler`.
3. Maak een **Key** aan met "Sign In with Apple" ingeschakeld, download het `.p8`-bestand (kan maar één keer gedownload worden).

In de Firebase Console:

4. **Authentication → Sign-in method → Apple** → inschakelen, en vul in: Services ID, Apple Team ID, Key ID, en de inhoud van het `.p8`-bestand.

In de code: `signInWithApple` bestaat al in `contexts/AuthContext.tsx`. Voeg in `app/login/page.tsx` een knop toe die 'm aanroept (zie git-historie voor de eerder verwijderde knop/SVG als voorbeeld).

### 1c. (Optioneel) Foto's alsnog activeren

Vereist het **Blaze-plan** (pay-as-you-go, met een gratis quotum) voor Firebase Storage:

1. Firebase Console → **rechtsonder "Upgraden" → Blaze-plan** activeren.
2. **Storage** → maak een bucket aan (production mode).
3. Voeg in `firebase.json` de sectie `"storage": { "rules": "storage.rules" }` weer toe.
4. Voeg in `app/card/page.tsx` de "Foto's"-regel weer toe aan de "Beheren"-lijst (`<ManageItem label="Foto's" onClick={() => router.push(\`/card/photos?id=${card.id}\`)} />`).
5. `firebase deploy` om de storage rules te publiceren.

### 2. Lokale configuratie

```bash
cp .env.local.example .env.local
```

Vul de 6 waarden uit stap 1 in `.env.local` in.

### 3. Firebase CLI koppelen (voor deploys)

```bash
npm install -g firebase-tools
firebase login
firebase use --add   # kies het project uit stap 1
```

## Development

```bash
npm install
npm run dev
```

Open http://localhost:3000.

## Bouwen en deployen

```bash
npm run build            # genereert statische site in ./out + service worker
firebase deploy          # publiceert hosting + firestore rules/indexes
```

Na de deploy is de app bereikbaar op `https://<project-id>.web.app` (of een gekoppeld custom domain). Elke gebruiker met een Gmail-account kan daar inloggen; ieders kaarten blijven privé dankzij de Firestore rules.

## Installeren op het startscherm

- **Android (Chrome)**: bezoek de URL → menu (⋮) → "App installeren" / "Toevoegen aan startscherm".
- **iOS (Safari)**: bezoek de URL → deelknop → "Zet op beginscherm".

De app opent daarna fullscreen met het KaartYes-icoon, zonder browserbalk.

## Structuur

- `app/login`, `app/home` — auth en kaartenoverzicht
- `app/card` — kaartdetail (`?id=`), met barcode/QR-weergave
- `app/card/edit`, `app/card/notes` — beheerschermen (`?id=`); `app/card/photos` bestaat maar is nog niet gekoppeld (zie "1c" hierboven)
- `app/card/new`, `app/card/new/scan`, `app/card/new/manual` — kaart toevoegen (scannen of handmatig)
- `lib/firebase.ts` — lazy Firebase-initialisatie (alleen in de browser, nooit tijdens static export)
- `lib/firestore.ts`, `lib/storage.ts` — databank- en fototoegang, altijd gescopet op de ingelogde `userId`

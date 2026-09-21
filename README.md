# Matrix Market — PROG7314 Part 2: App Prototype

**Group 8** — Caleb Keanu Naidoo (ST10084625), Taytem Pillay (ST10441678), Reece Mikhael Moodley (ST10460086)

Matrix Market is a native Android marketplace app built exclusively for university students —
a trusted, campus-verified space to buy, sell, and trade textbooks, tech, and residence
essentials with peers at their own institution. This repository contains the Part 2 prototype:
a fully working Kotlin/Jetpack Compose app talking to a custom-built REST API.

> Full context on the *why* behind this app — the competitive research and the original
> design decisions — lives in `docs/Part1-Research.pdf` and `docs/Part1-Planning-Design.pdf`.

---

## 1. Purpose & Scope

Matrix Market solves a gap identified in our Part 1 research: general marketplaces (Gumtree,
Facebook Marketplace) aren't safe or organised for students, while student-specific platforms
(StudentVIP) are outdated and lack real-time features. This prototype implements every
**required** Part 2 feature plus three custom features we designed in Part 1:

| Requirement | Where it lives |
|---|---|
| SSO sign-in | `android/.../auth/GoogleAuthManager.kt` + Firebase Auth |
| Settings menu | `android/.../ui/screens/settings/` |
| Custom REST API + database | `backend/MatrixMarket.Api/` (ASP.NET Core + SQLite) |
| REST API integration | `android/.../data/remote/` + `data/repository/` |
| **User Defined Feature 1** — ISBN Barcode Scanner | `android/.../ui/screens/scanner/` (ML Kit + CameraX) |
| **User Defined Feature 2** — Trade Meeting Scheduler | `android/.../ui/screens/trademeeting/` (reached via `ui/screens/listingdetail/` → Make Offer) |
| **User Defined Feature 3** — Gamified Student Score & Trust Badges | `android/.../ui/screens/profile/` |

Biometric authentication, offline sync, push notifications, and multi-language support are
explicitly scoped to the **POE-only** final submission per the assignment brief, and are not
part of this Part 2 prototype.

---

## 2. Architecture

```
Android Client (Kotlin + Jetpack Compose, MVVM)
    │  Retrofit/OkHttp over HTTPS/JSON
    ▼
ASP.NET Core Web API  ──►  SQLite Database (swap to Azure SQL for production)
    │
    ├─► Firebase Auth (Google SSO identity verification)
    └─► ML Kit (on-device barcode recognition, no network call needed)
```

- **Android**: Kotlin, Jetpack Compose, MVVM (`ViewModel` + `StateFlow`), Retrofit, DataStore
  (session/token storage), Firebase Auth (Google Sign-In), CameraX + ML Kit (barcode scanning).
- **Backend**: ASP.NET Core 8 Web API, Entity Framework Core, SQLite, JWT bearer authentication,
  BCrypt password hashing, Swagger/OpenAPI docs.

Every layer is separated cleanly (UI → ViewModel → Repository → Retrofit/ApiService) so the
app, local storage, API, and database can evolve independently — this was a stated design goal
in our Part 1 Planning & Design document.

---

## 3. Running it yourself

### Backend
```bash
cd backend/MatrixMarket.Api
dotnet restore
dotnet run
```
The API starts locally (check console for the port, typically `https://localhost:5001`) and
creates `matrixmarket.db` (SQLite) automatically on first run, seeded with 5 academic
categories. Swagger UI is available at `/swagger` for manually inspecting/testing endpoints.

Full endpoint list and deployment instructions: [`backend/README.md`](backend/README.md).

### Android app
1. Open the `android/` folder in Android Studio (Koala or newer) and let it sync — it will
   fetch the Gradle wrapper automatically.
2. **Firebase (for SSO):** already configured — `google-services.json` and the Google SSO
   Web client ID in `GoogleAuthManager.kt` are set up for the `matrix-market` Firebase
   project with the debug signing certificate registered. If you regenerate your debug
   keystore or add a release keystore later, add its SHA-1 in Firebase Console → Project
   Settings → Your apps, and re-download `google-services.json`.
3. **API URL:** the app defaults to `http://10.0.2.2:5000/`, which is the Android emulator's
   alias for your host machine — so running the backend locally + the app on an emulator works
   out of the box. For a physical device, change `API_BASE_URL` in `app/build.gradle.kts` to
   your deployed API URL (see backend README for Azure/Docker deployment).
4. Run on a physical device or emulator (min SDK 26 / Android 8.0+).

### Running the unit tests
```bash
cd android
./gradlew testDebugUnitTest
```
Tests cover the ViewModel logic behind every required and custom feature — see
[`android/app/src/test/`](android/app/src/test/java/com/matrixmarket/app).

---

## 4. Version Control & CI/CD

- `.github/workflows/android-ci.yml` — runs on every push/PR touching `android/`: sets up
  JDK 17, generates the Gradle wrapper if needed, runs all unit tests, builds a debug APK, and
  uploads both the APK and the test report as build artifacts.
- `.github/workflows/backend-ci.yml` — restores and builds the ASP.NET Core API on every push
  touching `backend/`.

Commit history reflects incremental, feature-by-feature development (auth, listings, scanner,
trade meetings, gamification, settings) rather than one large drop, per the marking rubric.

---

## 5. Demonstration Video

_(Add your unlisted YouTube link here once recorded — see Section 6 for a shot list.)_

`https://youtu.be/YOUR_VIDEO_ID`

---

## 6. Demo Video Shot List (for recording)

Record on a **physical device**, with a voice-over, covering in order:
1. **Authentication** — register a new student account, then log out and log back in via
   **Google SSO**.
2. **Settings** — change your display name and toggle notification preferences; show the
   change persisting after navigating away and back.
3. **REST API round-trip** — create a new listing in the app, then show it appearing (a) in
   the app's Current Listings and (b) live in Swagger (`GET /api/v1/listings`) or the SQLite
   DB, proving the round-trip.
4. **User Defined Feature 1** — scan a real textbook's barcode with the ISBN scanner and show
   it auto-filling the listing form.
5. **User Defined Feature 2** — make an offer, schedule a Trade Meeting (pick a campus spot),
   and confirm the trade using the generated 6-digit code.
6. **User Defined Feature 3** — open the Profile screen and show Student Score / Trust Badge
   updating after a confirmed trade.

---

## 7. AI Usage

See [`AI_USAGE.md`](AI_USAGE.md) for a transparent account of how generative AI was used
during this phase.

## 8. Project Documentation

- [`docs/Part1-Research.pdf`](docs/Part1-Research.pdf) — Research Report (Part 1)
- [`docs/Part1-Planning-Design.pdf`](docs/Part1-Planning-Design.pdf) — Planning & Design (Part 1)
- [`backend/README.md`](backend/README.md) — API reference & deployment guide

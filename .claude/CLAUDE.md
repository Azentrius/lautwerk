# Notes for working on this repository

## What this is

OpenAuto AI, an experimental Android app for voice conversations with OpenAI and Claude on the user's
own API keys, plus a minimal Android Auto prototype (Car App Library) tested only in the Desktop Head
Unit (DHU). A prototype: no approved Android Auto category, not for use while driving, not on Google Play.

- Overview and current state: `README.md`
- What comes next: `ROADMAP.md`
- What changed: `CHANGELOG.md`

## Working together

- Big picture first in a few sentences, then details; the recommendation first, then the reasons.
- For anything that touches API keys, conversation content or the car display, say briefly what the user has to understand to answer for it.

## Repository conventions

The same in every Azentrius repository.

- **Language.** Everything in the repository is English: code, comments, docs, commit messages, pull
  requests, test names and test data. The only German is the German translation and the tests that
  check it. User-facing texts always exist in English and German; German addresses the user formally
  (“Sie”). Answer the user in the language they write in.
- **Versions.** Semantic Versioning; the version lives in exactly one place (see “Releases and the
  repository”). Tags are `vX.Y.Z`.
- **Changelog.** `CHANGELOG.md`, newest first. Each version gets `## X.Y.Z – YYYY-MM-DD`, a one-line
  summary, then bullets. The pull request that raises the version adds its section.
- **Pull requests.** `main` is protected: every change goes through a pull request and is
  squash-merged once the `test` check is green; the branch is deleted afterwards. The PR title becomes
  the commit on `main`: short, imperative, English, without version numbers.
- **Authorship.** Commits on `main` are authored by the maintainer (the squash merge takes care of
  that). Commits, pull requests and files carry no co-author trailers and no “generated with” lines.

Existing German docs (README.md, ROADMAP.md, docs/) are translated once the open feature stack is merged; new and changed docs are written in English from now on.

## Layout

```
core/           Kotlin/JVM: Provider, ModelCatalog, ModelSelection, ChatClient contract, ChatError texts
providers/      Kotlin/JVM: OpenAI and Claude adapters, ProviderChatClient, HttpTransport; used by app only
settings/       Android library: ModelSelectionStore (app-private SharedPreferences `model_selection_v1`)
car-ui/         Android library: OpenAutoCarService (HostValidator), MessageTemplate screen with Start
app/            the assistant `dev.openautoai.app`: Compose UI, VoiceService, SecretStore, ConversationStore
prototype-app/  DHU test app, flavors `phone` (no car service) and `dhu` (adds car-ui); device tests in
                src/androidTest and src/androidTestDhu
docs/           build and test report, adapters, model selection, Windows/DHU guide; history/ = old states
.github/        dependabot.yml (Gradle and GitHub Actions)
```

## Commands

```sh
./gradlew :core:test :providers:test                  # JVM unit tests (only these two modules have them)
./gradlew :app:assembleDebug :app:lintDebug
./gradlew :prototype-app:assembleDhuDebug :prototype-app:assemblePhoneDebug :prototype-app:lintDhuDebug
./gradlew :app:connectedDebugAndroidTest              # device: adapters on Android's JSON, fake HTTP
./gradlew :prototype-app:connectedDhuDebugAndroidTest # device: phone UI, selection store, car template
```

On Windows `.\gradlew.bat`, with `JAVA_HOME` on a JDK 17 and `ANDROID_HOME` on the Android SDK. Device
tests need an unlocked phone left alone during the run (`ANDROID_SERIAL` with several devices). Android
Studio: build variant `prototype-app: dhuDebug` (phone UI and car service in one APK). Installing and
the DHU session (`adb forward tcp:5277 tcp:5277`, `desktop-head-unit.exe`): `docs/START_WINDOWS.md`.

## Rules (not negotiable)

1. **API keys stay in `SecretStore`** (AES-GCM under a non-exportable Android Keystore key), entered on
   the phone only. Never in code, Gradle files, tests, logs or exceptions; tests use made-up keys.
2. **Conversation content goes only to the selected provider.** Only the two fixed HTTPS endpoints, no
   redirects, no automatic fallback or retry; a provider switch starts a new chat without the old history.
3. **Fixed error texts only.** Users see and hear `ChatError` messages; raw server responses, keys and
   exception causes are never shown, read aloud or put into an exception.
4. **Not for driving, not for Play.** The POI category in the `dhu` flavor is a debug-only placeholder:
   claim no Android Auto category, keep `dhuRelease` disabled and the HostValidator on, and never work
   around a host's rejection (no installer spoofing, no root patches).
5. **The DHU prototype stays offline:** no INTERNET or RECORD_AUDIO in `prototype-app`/`car-ui`, no API
   or audio calls; `providers` is a dependency of `app` only.
6. **Model IDs only in `ModelCatalog`** (`core`); a selection is a provider/model pair checked against it.
   Never move a stored selection or an archived chat to another model silently.
7. **Stored formats are contracts:** enum names `OPENAI`/`CLAUDE`, key IDs `openai`/`anthropic`, Keystore
   aliases, `conversation_archive.dat` and its JSON fields, `model_selection_v1`. No silent migration.
8. **Claim only what ran.** Keep automated tests, checks the user confirmed on the device or in the DHU,
   and untested parts apart; no live API or voice end-to-end test exists yet.

## Releases and the repository

- The version lives in `versionName` (and `versionCode`) in `app/build.gradle.kts`: `0.1.3-dev` / 4.
  The DHU test app has its own `versionName` in `prototype-app/build.gradle.kts` (`0.1.2-prototype` / 3),
  raised only when that app changes.
- Raise version and `versionCode` and add the `CHANGELOG.md` section in the same pull request. Tags are
  `vX.Y.Z`; there is no release workflow yet, so releases are tagged by the maintainer until one exists.
- No CI workflow on `main` yet; `.github/workflows/android.yml` arrives with the open feature stack.
- Never in Git: APKs/AABs, `local.properties`, keystores, `diagnostics/`. Security reports: `SECURITY.md`.

## Useful knowledge

- **Toolchain:** Gradle 8.13 (pinned wrapper checksum), AGP 8.13.2, Kotlin 2.2.20, JDK 17 (JDK 25 does
  not work with Gradle 8.13), compile/target SDK 36, Build Tools 35.0.0; minSdk 29 in `app`, else 28.
- **Car App Library 1.8.0-rc01** is a deliberately pinned pre-release (Google names a security fix for
  it); do not call it stable. **Compose BOM 2025.08.01**, because 2026.09.00 needs SDK 37 and AGP 9.1+.
- **`providers` is plain Kotlin/JVM:** `org.json` is `compileOnly` (Android supplies it) and a test
  dependency; no JSON jar goes into the APK.
- **Protocols:** Claude `POST /v1/messages` with `anthropic-version: 2023-06-01` (`ClaudeAdapter`); OpenAI
  Responses API with `store=false`; 350 output tokens; 15 s connect / 50 s read timeout; 1 MiB responses.
- **`ChatClient.reply` blocks:** call it on a worker thread, never on a phone or car UI thread.
- **Three packages, three stores:** `dev.openautoai.app`, `.prototype.phone` and `.prototype.dhu` share no
  data; only inside the `dhu` package do the phone UI and the car service share one selection.
- **DHU:** needs Android Auto's developer mode and head unit server on the phone; `libusb-1.0.dll` stays
  next to `desktop-head-unit.exe`. A registered car service does not mean Android Auto shows the app;
  `CarTemplateTest` (TestCarContext) proves neither launcher visibility nor a DHU connection.
- **`INSTALL_FAILED_UPDATE_INCOMPATIBLE`:** another debug key; do not uninstall, that deletes app data.
- **German only on `main`:** user-facing texts (resources, `ChatError` in `core`) are German so far.

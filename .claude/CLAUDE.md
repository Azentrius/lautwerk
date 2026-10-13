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

## Repository baseline

The same in every Azentrius repository; keep it when changing the repository or its settings.

- **Security.** `SECURITY.md` with private vulnerability reporting; secret scanning with push
  protection; Dependabot alerts and security updates; Dependabot version updates for every
  ecosystem in use and for GitHub Actions; CodeQL default setup.
- **Branches and tags.** Ruleset `main`: pull request, squash only, linear history, no deletion or
  force push, required check `test`. Ruleset `release tags`: `v*` cannot be deleted or moved.
- **CI.** The job `test` runs every test and every lint on each push and pull request. Actions are
  pinned by commit SHA with the version as a comment; checkouts use `persist-credentials: false`;
  the token is read-only unless a job needs more. Check tools are pinned as well, so Dependabot keeps
  them current.
- **Releases.** Only the `release` workflow makes releases, on request, from `main`: it checks the
  version against the code and the existing tags, runs all tests, builds the artifact with
  `SHA256SUMS`, signs a build provenance (`gh attestation verify`) and takes the notes from
  `CHANGELOG.md`. Releases are immutable. Files that are not for users stay out of the artifact.
- **Licensing.** Apache-2.0: `LICENSE` in the root, an SPDX header in every source file, `NOTICE`
  for third-party material.

Here: the check tools are Gradle, AGP and Android lint, pinned in the build files and kept current by
Dependabot (Gradle); lint is Android lint of every module, run by the job `test`. The artifact is the
source archive `openauto-ai-VERSION.tar.gz` (`git archive`; `.gitattributes` keeps `.claude/`,
`.github/` and the Git files out), no APKs. SPDX headers: `//` on top of Kotlin and Gradle files,
`<!-- -->` after the XML declaration. Exceptions: the `ic_az_*` icons carry `CC-BY-4.0` (Font Awesome)
or `MIT` (`pending`, PatternFly); the app icon files from the design system stay exactly as generated,
without a header (`NOTICE` lists them); byte-identical upstream font-family XML retains its
original OFL attribution rather than an application SPDX header; DHU profiles (`tools/dhu/*.ini`), license texts and test evidence
are data. New third-party material goes into `NOTICE`, `app/src/main/assets/licenses/` and the list in
`LicenseDialog`.

## Layout

```
core/           Kotlin/JVM: Provider, ModelCatalog, ModelSelection, ChatClient contract, ChatError texts,
                i18n, chat search and text export
providers/      Kotlin/JVM: OpenAI and Claude adapters, ProviderChatClient, OkHttpTransport
voice/          Kotlin/JVM: VoiceController (the conversation flow), worker and audio interfaces,
                PlaybackFocusAudio and backend-free AudioCheck; used by app and car-ui
storage/        Android library: EncryptedConversationRepository, EncryptedCredentialStore
settings/       Android library: ModelSelectionStore (app-private SharedPreferences `model_selection_v1`)
car-ui/         Android library: OpenAutoCarService (HostValidator), MessageTemplate screen with Start
app/            the assistant `dev.openautoai.app`: Compose UI (package `design`), VoiceService, Android
                speech adapters; bundled license texts in src/main/assets/licenses/
prototype-app/  DHU test app, flavors `phone` (no car service) and `dhu` (adds car-ui); device tests in
                src/androidTest and src/androidTestDhu
tools/          release-notes.sh, check-version.sh; dhu/ = DHU profiles and contrast.py
docs/           build and test report, topic docs, evidence/ of device and DHU tests
.github/        workflows android.yml (job test) and release.yml; dependabot.yml (Gradle, GitHub Actions)
```

## Commands

```sh
./gradlew :core:test :providers:test :voice:test      # JVM unit tests (the only modules that have them)
./gradlew :app:assembleDebug :app:lintDebug
./gradlew :prototype-app:assembleDhuDebug :prototype-app:assemblePhoneDebug :prototype-app:lintDhuDebug
./gradlew :app:connectedDebugAndroidTest              # device: assistant UI, storage, voice adapters
./gradlew :prototype-app:connectedDhuDebugAndroidTest # device: phone UI, selection store, car template
sh tools/check-version.sh                             # version in gradle.properties + CHANGELOG section
```

CI runs the full list of tasks in `.github/workflows/android.yml` (lint of every Android module).

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
  around a host's rejection without explicit authorization. The user separately authorized a bounded
  system-installer origin experiment and Play Protect scans for this session's 0.1.17 APKs; preserve
  that installed package and document the limitation. No root patches or disabled host checks.
5. **Explicit audio modes:** since the user's 2026-10-13 voice request, `dhuDebug` may use INTERNET,
   RECORD_AUDIO and the existing provider/storage/voice modules. Output and microphone checks have
   no AI backend. Only explicit Conversation mode calls the selected provider. `phoneDebug` remains
   the offline UI comparison. Preserve the independently working assistant app and the car package.
6. **Model IDs only in `ModelCatalog`** (`core`); a selection is a provider/model pair checked against it.
   Never move a stored selection or an archived chat to another model silently.
7. **Stored formats are contracts:** enum names `OPENAI`/`CLAUDE`, key IDs `openai`/`anthropic`, Keystore
   aliases, `conversation_archive.dat` and its JSON fields, `model_selection_v1`. No silent migration.
8. **Claim only what ran.** Keep automated tests, checks the user confirmed on the device or in the DHU,
   and untested parts apart. Current car audio and conversation evidence: `docs/CAR_VOICE.md`.

## Releases and the repository

- The version lives only in `gradle.properties` (`version=`). Both apps use it as `versionName` (the
  prototype adds `-prototype`) and derive `versionCode` from it in the root `build.gradle.kts`
  (`appVersionCode`): major × 10000 + minor × 100 + patch, so minor and patch stay below 100.
- Raise the version and add the `CHANGELOG.md` section in the same pull request; `tools/check-version.sh`
  fails CI when the section is missing.
- Releases: Actions › release › Run workflow on `main` (or `gh workflow run release.yml --ref main`). Tag
  `vX.Y.Z`, source archive, `SHA256SUMS`, provenance and notes as under “Repository baseline”.
- CI: `.github/workflows/android.yml`, job `test` (version check, JVM tests of `core`, `providers` and
  `voice`, lint of every Android module, all debug and device-test builds) on every pull request and push
  to `main`; `test` is the required check. It installs exactly the SDK platform and Build Tools named
  under “Toolchain”; keep both in step. `release.yml` repeats the same steps: change both together.
- Never in Git: APKs/AABs, `local.properties`, keystores, `diagnostics/`. Security reports: `SECURITY.md`.

## Useful knowledge

- **Toolchain:** Gradle 9.8.1 (pinned distribution checksum), AGP 9.4.1 with built-in Kotlin (no
  `org.jetbrains.kotlin.android` plugin), Kotlin 2.4.21, JDK 17, compile SDK 37 (SDK package
  `platforms;android-37.0`), Build Tools 36.0.0 (the AGP 9.4 default); minSdk 29 in `app`, else 28.
  Target SDK stays 36 until the Android 17 behavior changes are tested on the device.
- **Patched build tooling:** `settings.gradle.kts` lifts vulnerable libraries of the build tooling
  (Bouncy Castle, protobuf, jdom2, jose4j, commons-lang3, httpclient) in the plugin classpath and every project
  configuration. None of them is in an APK. After an AGP or Kotlin upgrade, drop the entries the
  tooling now brings in a patched version by itself. To check, run CI once with a `logger.lifecycle`
  line in `liftBuildTooling` plus `buildEnvironment` and `:<module>:dependencies` for every module;
  with AGP 9.4.1 netty, OpenTelemetry and commons-compress dropped out that way.
- **Car App Library 1.8.0-rc01** is a deliberately pinned pre-release (Google names a security fix for
  it); do not call it stable. **Compose BOM 2026.09.00** needs compile SDK 37 and AGP 9.1+. Its lint
  rejects `LocalContext.current.getString` in composables; use `LocalResources.current`.
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
- **Two languages:** user-facing texts exist in English and German (Android resources, `i18n` in `core`
  for the JVM modules); the first supported device language wins, otherwise English.
- **The design system is private and read-only:** `Azentrius/design-system` is maintained upstream.
  At the maintainer's explicit request, `design/azentrius/` contains complete, unmodified reference
  snapshots, including historical copies. Preserve their bytes, original language and licenses;
  never edit them to resolve a design gap. Authored application documentation stays in English.
  New reference imports require the maintainer's request. See `docs/DESIGN_SYSTEM.md` for the audit.

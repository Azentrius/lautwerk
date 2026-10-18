# OpenAuto AI

An experimental Android app for voice conversations with OpenAI and Claude on your own API keys, and a
minimal Android Auto prototype (Car App Library) tested in the Desktop Head Unit (DHU). It is a
prototype: there is no approved Android Auto category for it, it is not for use while driving and it is
not on Google Play.

The phone app uses one voice flow for both providers: Android speech recognition, the provider's text
adapter and Android text-to-speech. A separately testable conversation controller handles start, stop,
late responses, errors and provider switches. Chats and API keys stay in the existing encrypted format.
The app also searches and exports the chat history, speaks German and English and follows the
Azentrius design.

What changed in each version: [CHANGELOG.md](CHANGELOG.md). What comes next: [ROADMAP.md](ROADMAP.md).

## Status

Current repository version: **0.1.18**. The approved phone design and voice-sample pronunciation
change are merged. The physical Pixel's car tests still use the retained 0.1.17-prototype/code 117
APK; the separate assistant is installed as 0.1.18/code 118.

0.1.17 connects the existing Android Auto package to audio and the shared conversation controller.
The title, app icon, provider/model line, host colors and single action remain. Start runs the mode
chosen on the phone; while active, the same action becomes End.

On the Pixel 10 Pro / Android 17, independent TTS playback and external PCM recognition passed.
In the DHU, CarAudioRecord captured the PC microphone, local recognition succeeded and the
sentence was heard read back correctly. These checks use no AI provider. A later DHU
retest with Claude Haiku 4.5 was confirmed as working overall; Speaking and End remained visible
while the answer played after the host microphone overlay disappeared. See the precise scope in
[CAR_VOICE.md](docs/CAR_VOICE.md). The planned ID.7 audio test, call and headset tests are deferred.
OpenAI car conversation and vehicle audio are not established by these Claude DHU results.

Details: [app icon and installation](docs/APP_IDENTITY.md), [current test report](docs/BUILD_AND_TEST.md),
[phone design](docs/DESIGN_SYSTEM.md),
[design reference](https://github.com/Azentrius/design-system/tree/edd4c67da6d7c606296c3bbd13d7aa5da548f452)
(private repository).

## Sideloading and real cars

Since 2026-10-12 we check distribution outside Google Play, first with native wireless Android Auto
in the ID.7. A manual test then confirmed launcher visibility and opening on the vehicle display
after the approved installation-origin experiment. This is a device-specific observation, not a general distribution guarantee. The tests in
the Kia with AAWireless are deferred. The
[sideloading guide](docs/SIDELOADING.md) and the [car test report](docs/SIDELOAD_TEST_REPORT.md) keep
preparation, observations and open tests apart. Those reports describe the original 0.1.15 baseline;
the current audio work and installation procedure are in [CAR_VOICE.md](docs/CAR_VOICE.md).

## Quick start on Windows

1. Open this project in Android Studio and set the Gradle JDK to **JDK 17**.
2. Install SDK Platform **Android 17 / API 37** and Build Tools **36.0.0** if they are missing.
3. Choose the build variant `prototype-app: dhuDebug`; this APK holds the phone UI AND the car service.
4. Follow [car audio setup and tests](docs/CAR_VOICE.md). The original SDK/DHU setup is in
   [docs/START_WINDOWS.md](docs/START_WINDOWS.md).

On the test app's phone page, choose the provider/model and open **Audio and conversation setup**.
First run Output test, then Microphone and recognition test. Both are independent of the AI backend.
Only Conversation mode sends recognized text to the selected provider using a key entered in this
test app. The old Start button on the phone's selection page remains a UI counter.

## Three separate builds

| Module / variant | Package | Purpose |
|---|---|---|
| `prototype-app:dhuDebug` | `dev.openautoai.prototype.dhu` | Experimental car service, audio diagnostics and conversation setup |
| `prototype-app:phoneDebug` | `dev.openautoai.prototype.phone` | Comparison app without car registration |
| `app:debug` | `dev.openautoai.app` | The phone app continued from 0.1.0, with providers, history and voice |

The test packages do not replace the old app and take over none of its data. Inside
`dev.openautoai.prototype.dhu`, phone and DHU share their selection. The three APKs use the same
selection code but each has its own private storage. The selection does not check actual API account
access. The car test app does not import the assistant's keys, voice preference or chat history.

## Category: a deliberately limited experiment

**OpenAuto AI is not a POI app and has no proven permitted Android Auto category for general AI
conversations.**

Only `dhuDebug` declares `androidx.car.app.category.POI`, as the template registration for this local UI
test. It is a technical test placeholder, not a statement that the product is permitted. No places, no
navigation and no supposedly official AI category are faked. The `dhuRelease` build is disabled. This
test scaffold is not meant for publishing on Play or for use while driving.

Android Auto may reject or hide the app even after a successful build and ADB install. The app's own
HostValidator check stays on and uses the signed host entries of the AndroidX library. Conversely, it
grants no approval by Android Auto. There is no root patch or disabling of host checks. An approved
local installation-origin experiment recorded Google Play as installer through Android's system
installer; this deliberately misrepresents origin and is neither a Play installation nor an official
distribution method. See the bounded procedure and observed limits in [CAR_VOICE.md](docs/CAR_VOICE.md).

According to Google's testing documentation, the "Unknown sources" option explicitly does not unlock
Car App Library apps. Whether the app is technically available outside Google Play is now checked in the
documented car comparison. That does not make it permitted on Play; internal Play tests would not
promise the category either.

## Modules and later work

- `core`: provider and model types, validated selection, chat and storage interfaces, UI test counter.
- `providers`: OpenAI and Claude adapters, replaceable HTTP transport and safe error mapping; shared by
  the assistant and the experimental car conversation.
- `storage`: encrypted chats and API keys, process-wide locks, fixed errors and a compatible archive
  format.
- `settings`: app-private storage of the selection, one model per provider, observing changes while the
  UI lives. No keys or chat data.
- `voice`: the shared conversation controller, worker and audio interfaces, archive and provider binding
  and text-to-speech text splitting.
- `app`: Android speech adapters, the microphone foreground service and the continued Compose app with
  the shared catalog and selection store. The names `OPENAI`/`CLAUDE`, key IDs and archive formats stay.
- `car-ui`: host validation, the existing `MessageTemplate`, phone-only setup, foreground audio service,
  CarAudioRecord input and on-device recognition, local TTS and binding to the shared voice controller.
- `prototype-app`: phone selection and two variants; `phoneDebug` stays offline, `dhuDebug` adds audio
  checks and an explicitly selected conversation mode.

Later work: check cloud destinations of the text export, plan encrypted backup and import and size rules
for very large archives. Call and headset tests stay deferred. A provider switch in the voice service
uses the model remembered for that provider and still starts a new chat without the other history. The
confirmed state of the voice tests is in [VOICE.md](docs/VOICE.md); further planning in
[ROADMAP.md](ROADMAP.md).

## Build tools

Gradle 9.8.1 with the complete wrapper and a checked distribution; AGP 9.4.1 with built-in Kotlin;
Kotlin 2.4.21; JDK 17; compile SDK 37, target SDK 36; Build Tools 36.0.0. Android 17 is tested as the
runtime system; target SDK 37 follows only after a device test of the Android 17 behavior changes.
Car App Library **1.8.0-rc01**, deliberately pinned as a pre-release: Google names a security fix for it
from 2026-08-26. Not called stable. Compose BOM 2026.09.00 (needs compile SDK 37 and AGP 9.1 or newer);
Activity 1.12.0 stays.

See [docs/BUILD_AND_TEST.md](docs/BUILD_AND_TEST.md) for the checks that actually ran and the open limits.

## CI, releases and the repository

[CI](.github/workflows/android.yml) builds the debug variants and runs the JVM tests and Android lint of
every module. No API keys and no live requests in CI. The version lives only in `gradle.properties`;
both apps derive `versionName` and `versionCode` from it. Only the [release](.github/workflows/release.yml)
workflow makes releases: the source archive with `SHA256SUMS` and a signed build provenance
(`gh attestation verify`), notes from [CHANGELOG.md](CHANGELOG.md). Releases contain no APKs.

Sources, wrapper, license, docs and tests belong in the repository. `local.properties`, `.gradle`, build
directories, keys and diagnostic output are excluded; APKs are not part of the Git history. Security
reports: [SECURITY.md](SECURITY.md).

## License

Apache-2.0, see [LICENSE](LICENSE). Fonts, icons and the app icon come with their own terms, see
[NOTICE](NOTICE).

## Official references (checked 2026-10-11)

- [Car App Library and categories](https://developer.android.com/training/cars/apps/library)
- [DHU: ADB tunnel and test mode](https://developer.android.com/training/cars/testing/dhu)
- [Test distribution and the limit of "Unknown sources"](https://developer.android.com/training/cars/testing)
- [AndroidX Car App release notes](https://developer.android.com/jetpack/androidx/releases/car-app)
- [AGP 9.4 compatibility](https://developer.android.com/build/releases/agp-9-4-0-release-notes)

## Optional local Pocket / Alba voice

Version 0.1.19 adds an explicit **Pocket · Alba** choice using the separately installed,
cancellation-fixed engine. German output runs locally; provider requests and recognition
keep their existing behavior. The native runtime/model are not bundled. See
[setup, attribution and limitations](docs/POCKET_TTS.md) before enabling it.

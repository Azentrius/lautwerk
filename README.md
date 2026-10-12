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

The last device check was 0.1.15, which added the approved app icon "speech bubble with a dot" from
design commit `170dd38` to the assistant and the prototype, with its monochrome layer, a native splash
screen on ink and a small notification icon. The 11 Android files match the source byte for byte.

**Assistant 0.1.15-dev (code 15) and DHU 0.1.15-prototype (code 6) are installed on the Pixel.** All 28
selected device tests pass. Build and lint succeed (0 errors). App icon, splash screen, the existing
phone pages, DHU touch and rotary, and six starts without reset were checked.

The DHU UI, B2/B3 and their functions are unchanged; the host still shows the Start action in light blue.
Call, headset and Bluetooth tests stay deferred. The phone comparison variant was built as
0.1.15-prototype (code 5). Voice stays in the assistant app only.

0.1.16 moves the build to the current toolchain and gives both apps one version; the device tests with
the new toolchain are still open.

Details: [app icon and installation](docs/APP_IDENTITY.md), [current test report](docs/BUILD_AND_TEST.md),
[phone design](docs/DESIGN_SYSTEM.md),
[design reference](https://github.com/Azentrius/design-system/tree/170dd38be84a9f107440a730eb29361087fc63ea)
(private repository).

## Sideloading and real cars

Since 2026-10-12, at the user's request, we check distribution outside Google Play. At the user's request
we first test native wireless Android Auto in the ID.7 with the APK that is already installed. The tests in
the Kia with AAWireless are deferred. A working path for OpenAuto AI is not confirmed yet. The
[sideloading guide](docs/SIDELOADING.md) and the [car test report](docs/SIDELOAD_TEST_REPORT.md) keep
preparation, observations and open tests apart. App and DHU stay at 0.1.15 for these tests.

## Quick start on Windows

1. Open this project in Android Studio and set the Gradle JDK to **JDK 17**.
2. Install SDK Platform **Android 17 / API 37** and Build Tools **36.0.0** if they are missing.
3. Choose the build variant `prototype-app: dhuDebug`; this APK holds the phone UI AND the car service.
4. Follow the steps in [docs/START_WINDOWS.md](docs/START_WINDOWS.md).

`OpenAuto AI`, the provider and model choice and `Start` appear on the phone. In the car template, Start
changes the text from "Ready for the UI test." to "Started · Test 1". This test has no network or
microphone permission and makes no API calls.

## Three separate builds

| Module / variant | Package | Purpose |
|---|---|---|
| `prototype-app:dhuDebug` | `dev.openautoai.prototype.dhu` | Minimal phone test plus the experimental car app service |
| `prototype-app:phoneDebug` | `dev.openautoai.prototype.phone` | Comparison app without car registration |
| `app:debug` | `dev.openautoai.app` | The phone app continued from 0.1.0, with providers, history and voice |

The test packages do not replace the old app and take over none of its data. Inside
`dev.openautoai.prototype.dhu`, phone and DHU share their selection. The three APKs use the same
selection code but each has its own private storage. The model catalog holds the five IDs from 0.1.0;
the selection does not check actual API account access. The phone and car test counters are independent
and only in memory; on the phone the counter survives recreating the activity. No persistent test
session.

## Category: a deliberately limited experiment

**OpenAuto AI is not a POI app and has no proven permitted Android Auto category for general AI
conversations.**

Only `dhuDebug` declares `androidx.car.app.category.POI`, as the template registration for this local UI
test. It is a technical test placeholder, not a statement that the product is permitted. No places, no
navigation and no supposedly official AI category are faked. The `dhuRelease` build is disabled. This
test scaffold is not meant for publishing on Play or for use while driving.

Android Auto may reject or hide the app even after a successful build and ADB install. The app's own
HostValidator check stays on and uses the signed host entries of the AndroidX library. Conversely, it
grants no approval by Android Auto. There is no installer spoofing, no root patch and no disabling of
host checks.

According to Google's testing documentation, the "Unknown sources" option explicitly does not unlock
Car App Library apps. Whether the app is technically available outside Google Play is now checked in the
documented car comparison. That does not make it permitted on Play; internal Play tests would not
promise the category either.

## Modules and later work

- `core`: provider and model types, validated selection, chat and storage interfaces, UI test counter.
- `providers`: OpenAI and Claude adapters, replaceable HTTP transport and safe error mapping; used by the
  assistant app only.
- `storage`: encrypted chats and API keys, process-wide locks, fixed errors and a compatible archive
  format.
- `settings`: app-private storage of the selection, one model per provider, observing changes while the
  UI lives. No keys or chat data.
- `voice`: the shared conversation controller, worker and audio interfaces, archive and provider binding
  and text-to-speech text splitting.
- `app`: Android speech adapters, the microphone foreground service and the continued Compose app with
  the shared catalog and selection store. The names `OPENAI`/`CLAUDE`, key IDs and archive formats stay.
- `car-ui`: the car app service with host validation, `MessageTemplate`, the Start action and the current
  selection as text.
- `prototype-app`: phone selection and two test variants; no API or audio calls.

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

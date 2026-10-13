# Design evidence — application 0.1.18 / code 118

Date: 2026-10-13. This directory retains the path requested before the maintainer approved
the corrected version. These are **0.1.18** results, not results from the earlier 0.1.16 release.
Design reference: `edd4c67da6d7c606296c3bbd13d7aa5da548f452`.
Device-tested code baseline: `5a3520b14e77eb1978d3c86e6d6e0d5255b621ac` on main.
Final PR base: `f1982ba8eea276be770a370749e9b79aeb721c0c`; the intervening PR #29 changes
only design-reference documentation and an XML comment, with no runtime changes.

## Design-build checks before the pronunciation follow-up

- Version and CHANGELOG check: passed; shared versionCode 118, both apps.
- JVM tests: 22 core, 21 providers, 57 voice; zero failures/errors.
- All six Android lint tasks: passed, zero errors. Existing tooling/resource warnings remain;
  the explicitly requested font-family XML is unused by Compose, which loads the TTF directly.
- All debug builds: assistant, prototype phone and prototype DHU; their instrumentation APKs built.
- Additional full assistant run on the emulator: **74/78 passed**, four failures; see the comparison below.
- Physical Pixel 10 Pro / Android 17: **78 tests passed**. See `pixel-instrumentation.txt`.
- Isolated Android 14/API 34 emulator: **19 prototype/DHU-template tests passed; two opt-in audio tests skipped**.
  See `emulator-dhu-tests.txt`. The native title and secure setup-background tests additionally passed at 100%/200% in both themes (8 checks).
- No real API requests, speech synthesis playback or microphone capture. No call/headset tests.
- Existing car voice APK on the Pixel was retained; only the separate assistant package was updated.
- The new test checks one semantic text “OpenAuto AI”, no duplicate description, the correct AI span,
  no horizontal/vertical overflow, and the complete final character. Native TypefaceSpan uses the
  actual regular-weight italic file. Voice-row layout checks 56/72 dp and growth at 200%.

Full local build command (JDK 17, installed SDK, Windows `gradlew.bat`):

```text
:core:test :providers:test :voice:test
:app:lintDebug :prototype-app:lintPhoneDebug :prototype-app:lintDhuDebug
:car-ui:lintDebug :settings:lintDebug :storage:lintDebug
:app:assembleDebug :app:assembleDebugAndroidTest
:prototype-app:assemblePhoneDebug :prototype-app:assembleDhuDebug
:prototype-app:assembleDhuDebugAndroidTest :prototype-app:assemblePhoneDebugAndroidTest
```

## Original screenshots

No screenshot was resized, cropped, recolored or otherwise edited in this directory.
Pixel screenshots use the existing isolated debug activity and invented fixtures, with activity-local
theme, locale and fontScale overrides. The user's system font, theme and stored chats were not changed.
Prototype screenshots use the isolated emulator's theme/font settings, restored after capture.

| Surface | Light / 100% | Dark / 100% | Light / 200% | Dark / 200% |
|---|---|---|---|---|
| Assistant, Pixel | [German](title-light-100.png) | [English](title-dark-100.png) | [German](title-light-200.png) | [English](title-dark-200.png) |
| Native prototype phone, emulator | [German](prototype-light-100.png) | [German](prototype-dark-100.png) | [German](prototype-light-200.png) | [German](prototype-dark-200.png) |

All titles were visually checked and remain complete. At 200%, tabs scroll horizontally and page
content scrolls vertically, as before. The visible edge of an offscreen tab is not title truncation.
An audible TalkBack review is still a manual check; automated single-node semantics alone do not
establish how a particular screen reader version pronounces the name.

## Contrast

Calculated using WCAG sRGB relative luminance, with unmodified foreground/background tokens;
full ratios are in `contrast.json`. No translucency is applied to the title.

| Theme | OpenAuto vs bg-page | AI signature vs bg-page | Requirement |
|---|---|---|---|
| Light | 17.49:1 | **5.26:1** | ≥4.5:1, passed |
| Dark | 15.83:1 | **8.80:1** | ≥4.5:1, passed |

Every Compose palette value was compared with tokens.json; supplied identity assets and the new
italic/font-family files were checked byte-for-byte. `UPSTREAM.sha256` verifies all 134 current
reference files. Historical files were restored directly from their original application Git blobs.

## Boundaries and manual review

No actual Android Auto/DHU host session was started for this phone-only rendering change.
Car API negotiation and current host colors therefore have **no new measurement**. The historical
0.1.14 host versions and A1–A11 evidence are distinguished in [the audit](../../DESIGN_SYSTEM.md).
The car template, DHU manifest/theme, localized strings, launcher assets and non-UI logic have no
diff from the final baseline. car-ui only receives the supplied notification icon and a readable
phone setup background; its audio flow and secure-input flag are retained. Template tests do not prove current launcher visibility or host rendering.

Before merge: on the Pixel inspect both themes, 200% font, German/English, voice rows with
descriptions, and optionally read the title with TalkBack. No conversation needs to start.
The native selectors and the newly merged car phone setup still need an upstream mapping;
all design contradictions are listed in the audit.

## Test-run reconciliation

The first pass on the earlier main completed 88 JVM tests, 78 Pixel tests and 12 prototype tests.
After PRs #27 and #28 merged, the branch was rebased and the full CI task list ran again successfully.
The Pixel's automatic screen lock interrupted the first repeat; that run is not counted as passing.
After the maintainer unlocked it, all 78 tests passed on the final build. German car-template tests
now explicitly localize their test context: the first emulator repeat exposed reliance on a cached
English context after application-locale changes. No production localization was changed.
The successful final Pixel and car logs supersede their interrupted attempts. The additional
assistant emulator failures are retained separately below; they are not counted as passes.

The imported current snapshot's Git tree ID is `7487fe48e12d0b633b816cef779523aec395fc0a`, identical
to upstream. This checks file names, bytes and modes, in addition to the SHA-256 manifest.



## Additional assistant emulator failures

The full assistant run on the isolated emulator reported 74 passes and four failures
([raw log](emulator-app-tests.txt)). The same test APK passed all 78 tests on the Pixel.
To check the failures without changing production code, the four cases were repeated after
installing the unmodified main application at `5a3520b`, then again after reinstalling 0.1.18.
Both targeted runs had the same result: three passes and the existing language expectation failure.
See [baseline](emulator-baseline-four.txt) and [final](emulator-final-four.txt).

| Test | Original full-run failure | Targeted baseline / final result |
|---|---|---|
| HistorySearchTest.failureIsSafeAndRetryRecoversWithoutLeakingQueryOrChatText | Test expects a German error, but the emulator's process uses English. | Same failure on both; pre-existing device-language assumption in the test. |
| StorageUiTest.slowKeyWriteDoesNotBlockNavigationAndCannotBeSubmittedTwice | Save gate was not entered within three seconds; an immediate single-test retry also failed. | Pass on both after reinstall. The earlier gate timeout's precise cause is not established; do not call it a confirmed product regression or claim it was fixed. |
| SpeechSettingsScreenTest.catalogLoadsAsAListWithoutStartingAPreview | No local-voice label was present in the current catalog. | Pass on both with the fresh en-US process and installed local English voices. |
| SpeechSettingsTest.realAndroidEngineAcceptsAvailableLocalChoiceAndRejectsMissingOrChangedEngine | No local voice for the process's language. | Pass on both; each log records nine local en-US voices. No synthesis is started. |

This comparison does not turn the original full run green. The mixed-locale emulator suite needs
separate test-environment isolation work. No production language, storage or voice behavior was
changed to accommodate it. The physical Pixel is the successful final full-suite run.


## Requested voice-sample pronunciation follow-up

After reviewing the installed design build, the maintainer reported that the German voice read
the AI suffix as one sound and explicitly requested spelling it as separate letters. The same
unmerged 0.1.18 PR now adds a `TtsSpan.TYPE_VERBATIM` annotation around only those two letters in
the fixed phone sample. The sample's visible text, app name, voice catalog, saved voice/rate,
online-voice confirmation and conversation/car output paths are unchanged.

The full local CI task list above passed again. Seven targeted Pixel tests passed, including the
new German/English annotation check across Android Parcel serialization, the existing real-engine
settings checks, and the voice-settings screen check. See [raw results](pixel-pronunciation.txt).
The earlier full 78-test Pixel run remains evidence for the design build; it was not repeated for
this small follow-up. The current APK hashes are in `apk-sha256.json`, with the preceding build's
hashes retained in `apk-sha256-before-pronunciation.json`.

The updated assistant APK is installed on the Pixel and its Voice and speed page was opened for
the maintainer's local-voice listening check. Audible pronunciation is awaiting that confirmation;
automated span/serialization assertions alone do not prove how a TTS engine sounds. No API request,
microphone, car session, call or headset test is needed for the fixed sample.

Android reference: [TtsSpan.VerbatimBuilder](https://developer.android.com/reference/android/text/style/TtsSpan.VerbatimBuilder).

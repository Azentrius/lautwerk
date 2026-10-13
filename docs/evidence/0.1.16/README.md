# Design evidence — application 0.1.18 / code 118

Date: 2026-10-13. This directory retains the path requested before the maintainer approved
the corrected version. These are **0.1.18** results, not results from the earlier 0.1.16 release.
Design reference: `edd4c67da6d7c606296c3bbd13d7aa5da548f452`.
Baseline: `c8620957616393263d46a3efcd5f800fec41a993` on main.

## Automated checks

- Version and CHANGELOG check: passed; shared versionCode 118, both apps.
- JVM tests: 22 core, 21 providers,
  45 voice; zero failures/errors.
- All six Android lint tasks: passed, zero errors. Existing tooling/resource warnings remain;
  the explicitly requested font-family XML is unused by Compose, which loads the TTF directly.
- All debug builds: assistant, prototype phone and prototype DHU; their instrumentation APKs built.
- Physical Pixel 10 Pro / Android 17: **78 tests passed**. See `pixel-instrumentation.txt`.
- Isolated Android 14/API 34 emulator: **12 prototype/DHU-template tests passed**.
  See `emulator-dhu-tests.txt`. The native title test additionally passed at 100%/200% in both themes.
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
`car-ui/`, the DHU manifest/theme, localized strings, identity assets and non-UI logic have no diff
from the baseline. Template tests do not prove current launcher visibility or host rendering.

Before merge: on the Pixel inspect both themes, 200% font, German/English, voice rows with
descriptions, and optionally read the title with TalkBack. No conversation needs to start.
The native selectors still need an upstream mapping; all design contradictions are listed in the audit.

# Build and test report 0.1.15

Historical report. The 0.1.17 car audio changes and current validation are in [CAR_VOICE.md](CAR_VOICE.md).

As of 2026-10-12. **The assistant and the DHU test app are installed on the Pixel as updates.**
The new speech bubble with a dot, the native splash screen and the small service icon follow the
design system `8eb2321` / O1 `65a48d5` without changes.
[Implementation and installation](APP_IDENTITY.md).

## Environment

| Item | Evidence |
|---|---|
| Device | Pixel 10 Pro, Android 17 / API 37 |
| Assistant installed | 0.1.15-dev / code 15 |
| DHU installed | 0.1.15-prototype / code 6 |
| Phone comparison, only built | 0.1.15-prototype / code 5 |
| Android Auto active | 17.7.663654-release / 177663654 |
| DHU | 2.0-windows, build 2022-03-30-438482292 |
| Host Car API | 8; `10-12 22:20:37.863 I/OpenAutoAI(25368): car.onCreateScreen api=8` |
| Device languages and night mode | `de-DE,en-US`, night mode `yes`; restored and unchanged respectively |

The Android Auto system stub version 1.2.562600-stub, which is also listed, is not the active test
version. Raw data, APK hashes and installed versions:
[verification.json](evidence/0.1.15/verification.json).

## Build and device tests

| Check | Result |
|---|---|
| Assistant, phone comparison, DHU, both test APKs | Final local build successful |
| App lint | 0 errors, 29 warnings, 1 hint |
| DHU lint | 0 errors, 6 warnings |
| Assistant on Pixel | 17 selected tests passed |
| DHU prototype on Pixel | all 11 tests passed |
| Source fidelity | 128 reference files checked by SHA-256 and 11 included XML copies checked byte-identical |
| Packaged resources | adaptive and round icons, API 31/33 splash theme, white service icon and version numbers checked |

The 17 assistant tests are DesignUiTest (9), DesignScreenshotsTest (3), LocalizationLifecycleTest (4)
and SpeechSettingsScreenTest (1). They use made-up histories and replace audio and providers with test
doubles. The eleven prototype tests check the car color contract, template, selection and phone test
page. The final APK with the API 33 splash rule was installed before these 28 tests ran again.
[App log](evidence/0.1.15/app-tests.txt), [DHU log](evidence/0.1.15/dhu-tests.txt).

The 28 tests are a targeted design regression, not a claim of a new full audio/API end-to-end test.
The existing lint warnings are not reported as fixed. The GitHub CI status is shown on the related
pull request.

## Appearance on the Pixel and in the DHU

- Assistant and prototype each started in light and dark device mode. The supplied gold mark appears
  on ink. The prototype's short native transition contains the system's own window animation; no
  custom animation and no artificial start delay were added.
- Light German and dark English conversation with made-up messages checked visually; large font
  covered by the existing screenshot and interaction test.
- New mark fully visible in the DHU launcher and in the `APP_ICON` header. The cut-out bars and the
  dot remain recognizable.
- 800×480 / touch: ready state, six starts 1–6 in the same session without reset, then night view. All
  visible counters match the car log entries.
- 1280×720 with wide profile / rotary: opening from the launcher, visible focus, start in day and
  night. Switching Claude Haiku → OpenAI GPT-5.4 updates the selection live; Test 1 is kept. Another
  night start in a fresh session also passes. After using the phone selection, however, the host loses
  the usable rotary focus; rotating alone then does not reach the button. The check of this special
  case is described separately below.
- The original DHU selection Claude / Anthropic · Claude Haiku 4.5 was restored at the end. The DHU
  was closed normally and the assistant app opened.

The template area stays dark gray, the Start button host light blue. Car app code and B2/B3 color
resources are unchanged from 0.1.14. Samples from the unscaled 800×480 images:

| Mode | Background | Start button | Text | Contrast |
|---|---|---|---|---|
| day | #25262A | #BAD9FC | #0E3253 | 9.01:1 |
| night | #25262A | #BAD9FC | #0E3454 | 8.80:1 |

Method: sRGB linearization, relative luminance and WCAG contrast; dark inner glyph color in the
documented text area. Coordinates and RGB values: [contrast.json](evidence/0.1.15/contrast.json).
[Six starts](evidence/0.1.15/six-starts.json), [car events](evidence/0.1.15/car-events.txt).

The full 64-view acceptance test of 0.1.14 is not claimed as run again; it remains in the
[archived (German) 0.1.14 test report](https://github.com/Azentrius/openauto-ai/blob/24732451155dd8cde52f7437872dd9c6fceff0cc/docs/history/BUILD_AND_TEST-0.1.14.md).

### Open host observation: rotary focus

In the wide 1280×720 profile, the usable focus disappeared in 0.1.15 after the phone selection. The
display updated and kept Test 1, but further rotate and click commands did not trigger a start. A new
session allowed the first start at night again. For comparison, the unchanged 0.1.14 DHU APK was
briefly installed with `install -r -d`: there, too, a focus frame was visible at first, but already the
first later click did not increase the counter; after the phone selection, the focus was missing as
well. The exact trigger was not isolated. This shows a problem that also occurs with the old build,
not a passed rotary test after live selection and not a proven cause. The earlier 0.1.14 acceptance is
not rewritten by this additional finding.

Evidence: [0.1.14 before click](evidence/0.1.15/baseline-focus-night.png),
[0.1.14 after click](evidence/0.1.15/baseline-start-1-night.png),
[0.1.14 after selection](evidence/0.1.15/baseline-after-selection.png),
[0.1.15 new night start](evidence/0.1.15/identity-wide-recheck-start-1-night.png).
Afterwards the same checked 0.1.15 APK was installed again and its version number confirmed. There was
no data deletion and no change to the car app's behavior to work around this finding.

## Evidence and limits

- [Splash screen light](evidence/0.1.15/assistant-light-splash.png),
  [splash screen dark](evidence/0.1.15/assistant-dark-splash.png)
- [Conversation light/German](evidence/0.1.15/az-light-conversation.png),
  [conversation dark/English](evidence/0.1.15/az-dark-conversation.png),
  [large font](evidence/0.1.15/az-large-settings.png)
- [Launcher icon](evidence/0.1.15/dhu-launcher-icon.png),
  [DHU ready](evidence/0.1.15/identity-800-ready-day.png),
  [sixth start](evidence/0.1.15/identity-800-start-6-night.png),
  [rotary focus](evidence/0.1.15/identity-wide-focus-day.png),
  [OpenAI after live switch](evidence/0.1.15/identity-wide-openai-live-night.png)

DHU evidence is cropped to the app. Maps, other notifications and private chat content are not part of
the published evidence.

**Not checked again in practice:** a live conversation and the actual running conversation service
notification, launcher themed icons and older Android versions. For the service icon, the supplied
resource, packaging and `setSmallIcon` were checked; the service was not started with a real
microphone for this. A local shell notification attempt gave no visible evidence and does not count as
a passed visual test. The monochrome layer is included unchanged; the personal launcher configuration
was not switched.

Call, headset and Bluetooth tests remain deferred.
No AI request, no change to API keys, histories or voice logic, and no uninstall or deletion of app
data. No new missing design values were found for this scope.

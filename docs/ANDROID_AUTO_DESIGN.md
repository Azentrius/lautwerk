# Android Auto: Azentrius 0.1.14

The DHU prototype now requests the approved Azentrius color pair for the start button. Font,
shape, spacing and the actual color choice are still drawn by the Android Auto host. This state
is a local debug experiment; "Start" only increments the UI test counter.

## Source and scope

The approved implementation and the 0.1.14 acceptance test are based on [Azentrius/design-system, commit 7fe60bf](https://github.com/Azentrius/design-system/blob/7fe60bfb1b244d9e36464c6e5c590dfa228ee4dc/android-auto.md) (private repository).

The current design reference is now [commit 170dd38](https://github.com/Azentrius/design-system/blob/170dd38be84a9f107440a730eb29361087fc63ea/android-auto.md).
It refines host colors/background based on this acceptance test and contains the O1 decision on
the new app icon from `6a7a951`. B2/B3 are unchanged; nothing needs to be caught up in 0.1.14.
The icon files were then integrated in 0.1.15; see
[App icon and current installation](APP_IDENTITY.md).
The following details still describe the historical state of 0.1.14.

| Requirement | Implementation |
|---|---|
| B1 | The existing MessageTemplate, title, APP_ICON, one message and exactly one action are kept. |
| B2 | DHU flavor only: metadata `androidx.car.app.theme`, CarAppTheme with Primary `#C8A96E` and PrimaryDark `#7D5F22`. No secondary pair and no day/night overrides. |
| B3 | Start action with `CarColor.PRIMARY`; no text color, no icon and no FLAG_PRIMARY. |
| B4 | All existing German/English texts, their order and the full model names are kept. |
| B5 | No further phone tokens, fonts or icons are passed to the host. |

The existing turquoise-blue app icon stays. There is no new recording feature, no conversation
state in the car and no change to the category or HostValidator. `dhuRelease` stays disabled. No
approval or officially supported category for general AI conversations is claimed.

Only the DHU variant moves to **0.1.14-prototype / Code 5**. The separate assistant app stays at
**0.1.13-dev / Code 14**, the phone comparison variant at **0.1.12-prototype / Code 4**.

## Build and installation after the baseline measurement

Before installing, measure the previous DHU version as described in the test report: A3/A4 by day
and night, and A7 with six starts. Otherwise the comparison is missing. The baseline APK saved
here is 0.1.12-prototype / Code 4.

```powershell
Set-Location -LiteralPath 'C:\Users\stefa\Documents\Projekte\Azentrius\OpenAuto AI'
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
.\gradlew.bat :prototype-app:assembleDhuDebug :prototype-app:assembleDhuDebugAndroidTest :prototype-app:lintDhuDebug
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb -s 56161FDCH000H8 install -r '.\prototype-app\build\outputs\apk\dhu\debug\prototype-app-dhu-debug.apk'
& $adb -s 56161FDCH000H8 shell am start -n 'dev.openautoai.prototype.dhu/dev.openautoai.prototype.MainActivity'
```

For the download, replace the APK path with the full path to
`OpenAutoAI-0.1.14-dhu-debug.apk`. Expected: `Success` and version
0.1.14-prototype / Code 5. Do not uninstall or clear app data.

## DHU check

Use the connection and head unit server that are already set up.
The full setup is described in [START_WINDOWS.md](START_WINDOWS.md).
`tools/dhu/` contains eight profiles: four formats, each for touch and rotary.
The DHU console stays open for commands.

```powershell
$dhu = "$env:LOCALAPPDATA\Android\Sdk\extras\google\auto\desktop-head-unit.exe"
& $adb -s 56161FDCH000H8 forward tcp:5277 tcp:5277
& $dhu -c '.\tools\dhu\800x480-touch.ini' -i touch
```

In the DHU console, use `day`, `night` and `screenshot FILENAME.png`.
Close a session normally with `quit`. For rotary, start the corresponding profile with
`-i rotary`; operate it with the arrow keys or `dpad click`.
Do not reset a template or disable a restriction to pass the six-start test. Take screenshots
only inside OpenAuto AI, without including other apps, call data or map positions in published
evidence.

Capture versions and the app log:

```powershell
& $dhu --version
& $adb -s 56161FDCH000H8 shell dumpsys package com.google.android.projection.gearhead | Select-String 'versionCode|versionName'
& $adb -s 56161FDCH000H8 logcat -d -s OpenAutoAI:I '*:S'
```

`car.onCreateScreen api=…` is the negotiated **Car API level of the host**;
`minCarApiLevel=1`, the Android API and the library version do not replace this measurement.
`car.start count=n` must match the visible counter.

## Acceptance test and decisions

The matrix covers 800×480, 1280×720, 1920×1080 and 1280×720 with a 220-pixel vertical margin,
each in German/English, day/night, OpenAI/GPT-5.4 and Claude/Sonnet 4.6, and touch/rotary:
64 combinations.
Criteria A1–A11 and their actual status are listed in the [test report](BUILD_AND_TEST.md).
The English fallback is additionally checked with an unsupported language.

- **A3/A4:** Also measure the previous host default color. For both versions, record the actual
  fill/text pixels, coordinates, screenshot and contrast. Use solid glyph strokes instead of
  anti-aliased edge pixels; at least 4.5:1. Record day and night separately. The configured
  color value does not prove how the host renders it.
- **A7:** Six starts in the same 800×480 session, without a reset. If the baseline version already
  fails, this is, with the user's approval, a known bug that does not block 0.1.14. A newly
  occurring failure is a regression. A comparison that has not been run yet is neither passed
  nor a known bug.
- **O2:** If the contrast is insufficient, the approved fallback is `CarColor.DEFAULT` with a new
  acceptance test. Do not apply it pre-emptively.
- **O3:** If the host truncates provider/model, report the deviation and get a decision on
  display names. Do not invent truncations or design values.

Call and headset/Bluetooth tests remain deferred as requested.

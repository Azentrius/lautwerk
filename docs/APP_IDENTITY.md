# App icon and splash screen 0.1.15

OpenAuto AI uses the approved speech bubble with a dot from the
[design system 8eb2321](https://github.com/Azentrius/design-system/blob/8eb2321c2e0d15ef5f57bc6d83630cd87a6ff2d9/android.md),
O1 from `65a48d5`. The six supplied Android files are taken over byte-identical in the assistant; the
prototype contains the same five launcher files, without the service icon it does not need. The
generator was not run. There are no custom mark, size or color values.

## Implementation

- Both manifests use `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`.
  Background `#0A0A0F`, foreground `#C8A96E`, same geometry for the monochrome layer; `evenOdd` and the
  cut-out bars are kept.
- The native window background uses the same ink color. From API 31 the theme sets only the splash
  screen background color; the launcher icon is used automatically. From API 33,
  `windowSplashScreenBehavior` with `icon_preferred` requests that the icon is also shown for starts
  outside the launcher. `windowSplashScreenAnimatedIcon` stays unset. No custom start window, no
  library, no branding image, no start delay and no custom animation.
  The Android window animation stays under system control.
- Below API 31 the window background is ink as well; no custom splash dialog or second start window
  is introduced.
- The conversation service uses only the supplied white `ic_stat_openauto` for `setSmallIcon`. No gold
  notification color. Actions, texts, channel, permissions and service flow are kept.
- Android Auto takes the adaptive app icon through the existing `Action.APP_ICON` and the launcher.
  CarAppTheme/B2/B3, MessageTemplate, texts, counter, HostValidator and category stay unchanged.

Technical basis: [Android splash screen](https://developer.android.com/develop/ui/views/launch/splash-screen)
and [adaptive icons](https://developer.android.com/develop/ui/compose/system/icon_design_adaptive).
The phone pages keep the confirmed design from 0.1.13. The prototype's minimal phone test stays
functionally and structurally unchanged.

## Build and install

In the project folder with JDK 17, SDK Platform 37 and Build Tools 36.0.0:

```powershell
.\gradlew.bat :app:assembleDebug :prototype-app:assemblePhoneDebug :prototype-app:assembleDhuDebug
```

The packages are separate and keep using their own existing data:

| Variant | Version / code | Package |
|---|---|---|
| Assistant | 0.1.15-dev / 15 | `dev.openautoai.app` |
| DHU test | 0.1.15-prototype / 6 | `dev.openautoai.prototype.dhu` |
| Phone comparison | 0.1.15-prototype / 5 | `dev.openautoai.prototype.phone` |

The assistant and DHU were updated on the Pixel, the phone comparison was only built.
Installation from the project folder; for the downloaded package, replace the two APK paths with
`OpenAutoAI-0.1.15-assistant-debug.apk` and `OpenAutoAI-0.1.15-dhu-debug.apk`:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
& $adb -s 56161FDCH000H8 install -r .\app\build\outputs\apk\debug\app-debug.apk
& $adb -s 56161FDCH000H8 install -r .\prototype-app\build\outputs\apk\dhu\debug\prototype-app-dhu-debug.apk
& $adb -s 56161FDCH000H8 shell am start -n dev.openautoai.app/.MainActivity
```

Expected: `Success` twice; no uninstall and no deletion of app data.
For another device, use its serial from `devices -l`.

## DHU check

Reuse the existing setup from [START_WINDOWS.md](START_WINDOWS.md).
The Android Auto head unit server must be running on the Pixel. In the project folder:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$dhu = "$env:LOCALAPPDATA\Android\Sdk\extras\google\auto\desktop-head-unit.exe"
& $adb -s 56161FDCH000H8 forward tcp:5277 tcp:5277
& $dhu -c .\tools\dhu\800x480-touch.ini -i touch
```

Open "OpenAuto AI" in the launcher. The new mark appears in the launcher and in the template header.
Title, current selection and "Start" stay visible. One tap gives "Started · Test 1". Six starts in a
row must give 1 to 6. `day` and `night` in the DHU console check both modes; `quit` closes the DHU.
On this host the Start button is still shown in light blue.

For the rotary comparison, then start the DHU with
`-c .\tools\dhu\1280x720-wide-rotary.ini -i rotary`. Move the focus with `dpad rotate right` and
trigger it with `dpad click`. A provider choice in the phone test app must appear in the same DHU
session without resetting the counter.

This remains a local UI prototype without an AI request in the car. No new officially supported car
app category is claimed and no host check is bypassed.
The test results and limits are in [BUILD_AND_TEST.md](BUILD_AND_TEST.md).

Observation on the test host: after using the phone, the usable rotary focus can be lost. This anomaly
also showed up in the comparison with 0.1.14; see the test report. Rotating alone did not reliably
restore the focus. A fresh DHU session allowed a start again. The normal
[DHU controls](https://developer.android.com/training/cars/testing/dhu) stay in place; no app or host
restriction is turned off.

# Lautwerk 0.2.0 device checks

The automated device checks passed on a Pixel 10 Pro running Android 17 (API 37).
There were **114 passed tests, 5 deliberately skipped audio tests, and 0 failures**.
The new apps remain installed alongside the old apps. This is not a live conversation,
DHU, or vehicle acceptance result.

## Source and build

- Date: 2026-10-18.
- Tested source: `879d7dddb6a5f959efefbb042fa85586181ebb51`, the merge of PR #37.
- `gradle.properties`: `version=0.2.0`; application version code: `200`.
- Unmodified application and test sources from `main`. Local Gradle overrides were
  temporarily set aside for the build and preserved separately. No application code
  or design reference was changed for these checks.
- Eclipse Adoptium JDK 17.0.19+10 for the Gradle launcher and daemon; Gradle 9.8.1.
- All requested debug/test APKs built successfully, including the phone comparison
  APK, which was built but not installed. The separate storage test APK was also
  built because `EncryptedStorageTest` belongs to the storage library, not either
  of the two application test runners.
- The rename audit found exactly three remaining matches, all for the external
  Pocket engine identity. See [rename-audit.txt](build-checks/rename-audit.txt).
- Build output contains existing deprecation warnings; the build succeeded.

Evidence: [build result](build-checks/result.json), [build output](build-checks/build.txt),
[Gradle/JDK](build-checks/gradle-version.txt), [APK SHA-256 values](build-checks/apk-sha256.json).
JVM tests and lint were not repeated locally in this device-only task; the existing
CI result belongs to the source PR and is not counted as device evidence here.

## Results

| Check | Result | Evidence |
|---|---|---|
| Assistant instrumentation | 81 passed, 2 audio tests skipped, 0 failed | [Result](phone-all/result.json), [raw output](phone-all/instrumentation.txt) |
| Prototype instrumentation | 19 passed, 3 audio tests skipped, 0 failed | [Result](car-all/result.json), [raw output](car-all/instrumentation.txt) |
| `EncryptedStorageTest` | 14 passed, 0 failed, isolated library test package | [Result](storage-all/result.json), [raw output](storage-all/instrumentation.txt) |
| `DesignScreenshotsTest` | 7 passed, including all four title combinations | [Phone result](phone-all/result.json), [screenshots below](#title-review) |
| `PhoneSmokeTest` | 3 passed, including title, selection and UI-only start callback | [Car result](car-all/result.json) |
| `CarTemplateTest` | 4 passed using `TestCarContext` and fake voice controls | [Car result](car-all/result.json) |
| `CarDesignContractTest` | 4 passed | [Car result](car-all/result.json) |
| `DocumentConversationExporterTest` | 4 passed, including the `Lautwerk-Chat.txt` filename contract | [Phone result](phone-all/result.json) |
| Installed application versions | Assistant `0.2.0 / 200`; prototype `0.2.0-prototype / 200` | [Package queries](device/versions-after.json), [installation output](device/installations.json) |
| Car service registration | Exactly one `de.azentrius.lautwerk.car.LautwerkCarService` | [Service query](device/car-service.txt) |
| Device settings | Before and after match for every recorded field | [Before](device/settings-before.json), [restored](device/settings-restored.json) |
| Old packages | Still installed at `0.1.19 / 119` and `0.1.19-prototype / 119`; installer metadata unchanged | [Before](device/versions-before.json), [final](device/versions-final.json) |
| Lautwerk log sample | Fixed template/UI state messages; no live voice session | [Logcat](device/logcat-lautwerk.txt) |

The class rows are subsets of the suite totals, not additional tests.
AndroidJUnitRunner prints `OK (83 tests)` and `OK (22 tests)` for the first two
suites even though these totals include assumptions that skipped audio tests.
The JSON results distinguish status `0` (passed) from status `-4` (skipped).
Neither `audioPlayback`, `externalPcm`, nor `pocketAudio` was supplied.

The log's `car.voice.action=start/stop` entries were produced by template tests
with fake controls. They do not represent an actual voice conversation or a host
connection. No `car.onCreateScreen api=...` host session is claimed by this report.

## Title review

All four full-resolution screenshots were visually inspected. The header shows
`Lautwerk.` completely, including the dot. The light theme uses bronze `#7D5F22`
and the dark theme uses gold `#C8A96E`. The layout tests also assert the title text,
single accessible description `Lautwerk`, dot span color, and absence of horizontal
or vertical text overflow.

| Theme / local test font scale | Screenshot |
|---|---|
| Light / 100%, German | [title-light-100.png](screenshots/title-light-100.png) |
| Dark / 100%, English | [title-dark-100.png](screenshots/title-dark-100.png) |
| Light / 200%, German | [title-light-200.png](screenshots/title-light-200.png) |
| Dark / 200%, English | [title-dark-200.png](screenshots/title-dark-200.png) |

At 200%, the existing horizontal tab strip scrolls. A partially offscreen unselected
tab is expected; the selected tab remains visible. The header itself is not clipped.
The screenshots use the test activity and contain no conversation or credential data.
The device-wide font scale remained `1.3`; tests supplied `1.0` and `2.0` through
their local configuration, without changing the user's global setting.

## Device preservation and scope

The state files retain the flat JSON shape used for 0.1.19, extended with locale,
night mode, and font scale. Initial and final values were German with English as
the second locale, dark mode, font scale `1.3`, Wi-Fi on, Bluetooth on, mobile data
on, airplane mode off, and `com.google.android.tts` as the default TTS engine.
Each suite also has its own before/restored snapshots. No global restoration write
was needed because all recorded values remained unchanged.

All installations used ordinary `adb install -r`, without an installer override.
The new application packages report no Play installer. The pre-existing prototype's
experimental installer metadata was only observed, not applied to the new apps.
Nothing was uninstalled and no package data was cleared. Test-created temporary
fixtures were cleaned up by their own tests; the old apps' data was not accessed.
Device serials and local user paths are omitted from the published evidence.
Text artifacts use UTF-8 with normalized LF line endings.

## Reproduction commands

Use an unlocked device reserved for the tests. Capture the settings before any
installation and compare them after each suite and at the end. Set `ANDROID_SERIAL`
to the selected device from `adb devices -l`; do not publish that serial.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$adb = Join-Path $env:ANDROID_HOME 'platform-tools\adb.exe'

.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :prototype-app:assemblePhoneDebug :prototype-app:assembleDhuDebug :prototype-app:assembleDhuDebugAndroidTest :storage:assembleDebugAndroidTest --console=plain

& $adb install -r app/build/outputs/apk/debug/app-debug.apk
& $adb install -r prototype-app/build/outputs/apk/dhu/debug/prototype-app-dhu-debug.apk
& $adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
& $adb install -r prototype-app/build/outputs/apk/androidTest/dhu/debug/prototype-app-dhu-debug-androidTest.apk
& $adb install -r storage/build/outputs/apk/androidTest/debug/storage-debug-androidTest.apk

& $adb shell am instrument -w -r de.azentrius.lautwerk.test/androidx.test.runner.AndroidJUnitRunner
& $adb shell am instrument -w -r de.azentrius.lautwerk.prototype.dhu.test/androidx.test.runner.AndroidJUnitRunner
& $adb shell am instrument -w -r de.azentrius.lautwerk.storage.test/androidx.test.runner.AndroidJUnitRunner

& $adb shell dumpsys package de.azentrius.lautwerk
& $adb shell dumpsys package de.azentrius.lautwerk.prototype.dhu
& $adb shell cmd package query-services --brief -a androidx.car.app.CarAppService -p de.azentrius.lautwerk.prototype.dhu
& $adb logcat -d -v threadtime 'Lautwerk:V' '*:S'
```

Stop on an installation or signature failure. Do not uninstall, clear data, or add
an installer override. Use `am instrument` directly; do not substitute Gradle's
`connectedAndroidTest` tasks. A successful ADB process exit alone is not proof of
test success: inspect the runner's final summary and individual status codes.

Read the following settings into the JSON snapshots:

```text
settings get global wifi_on
settings get global mobile_data
settings get global airplane_mode_on
settings get global bluetooth_on
settings get secure tts_default_synth
settings get system font_scale
settings get system system_locales
settings get secure ui_night_mode
getprop persist.sys.locale
cmd uimode night
```

The PNGs were copied with `adb exec-out run-as de.azentrius.lautwerk cat cache/<name>.png`,
capturing stdout as bytes in Python and writing those bytes directly. Avoid text
redirection in older PowerShell versions, which can corrupt PNG data.

## Remaining manual acceptance

1. Enter and save each provider's own API key in Lautwerk. The new package IDs do not
   migrate keys or chats from the old apps.
2. Have a short real conversation with Claude and one with OpenAI. Check listening,
   output, stopping during an answer, and restarting.
3. Export a non-sensitive chat through the Android document picker as
   `Lautwerk-Chat.txt`; open the saved file and check its contents.
4. Listen to the pronunciation of `Lautwerk` in German and English.
5. Check the launcher, title, controls, microphone, output, stopping, and restarting
   in a real DHU session. Registration and `TestCarContext` do not prove visibility.
6. Repeat the agreed conversation checks in the stationary ID.7.

Call and headset interruption tests remain deferred. Keep the old apps installed
until the new apps have passed the manual acceptance checks.

# Android Auto design checks, 0.3.1

Date: 2026-10-19. Pixel 10 Pro, Android 17/API 37. Fixed test fixtures only; no provider request,
microphone or audio-playback opt-in. Call, headset and vehicle checks remain deferred.

| Check | Result | Evidence |
| --- | --- | --- |
| JVM suites | 100 passed: core 22, providers 21, voice 57 | `build-result.json` |
| Android lint | Seven targets, zero errors; 33 warnings retained in the reports | `build-result.json` |
| Debug builds | All APK and instrumentation tasks from `android.yml` succeeded with JDK 17 | `build-result.json`, `apk-sha256.txt` |
| Version and changelog | `tools/check-version.sh` returned 0.3.1; `git diff --check` passed | Version in root `gradle.properties` |
| Car action labels | Two tests passed, including all modes, active phases, German/English/fallback and lifecycle updates | `contract-instrumentation.txt` |
| Car template and design contract | Seven tests passed | `contract-result.json`, `contract-instrumentation.txt` |
| Shared prototype phone UI | PhoneSmokeTest (3) and CarPhoneDesignTest (1) passed | `phone-result.json`, `phone-instrumentation.txt` |
| Installed DHU package | 0.3.1-prototype / 301; ordinary `adb install -r` | `package-version.txt`, installation logs |
| Car service | Exactly one service: `com.azentrius.lautwerk.car.LautwerkCarService` | `car-service.txt` |
| Device settings | Unchanged after the device runs | `settings-before.json`, `settings-restored.json` |
| Actual host rendering | Not obtained. DHU transport connected but there was no video focus; host Car API was not negotiated in the observed logs | `dhu-connection.txt`, `environment.json` |

The two device invocations used `adb shell am instrument -w -e class ...` with the installed
`com.azentrius.lautwerk.prototype.dhu.test/androidx.test.runner.AndroidJUnitRunner`.
The APKs remain installed. No app was uninstalled and no user store was cleared. Preference fixtures
used by the contract tests are isolated. The existing phone tests restore their selected model.

The build command was the task list in `.github/workflows/android.yml`: JVM tests of core, providers
and voice; lint of app, both prototype flavors, car-ui, settings, storage and speech-output; all three
debug APKs and assistant/DHU instrumentation APKs. Local build completed successfully in 5m 17s.

No host screenshot, measured color contrast, rotary-focus result or completed A1-A11 matrix is claimed.
The full A7 sequence also needs provider requests, which this task excludes. The filtered Logcat sample
contains only structural Lautwerk markers, if any; template test results are not host-rendering logs.
The design audit and open questions are in `../../ANDROID_AUTO_DESIGN_0.3.1.md`.

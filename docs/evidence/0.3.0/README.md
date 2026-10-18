# Lautwerk 0.3.0 package-change checks

The local CI-equivalent build, JVM tests and Android lint passed. Pixel 10 Pro / Android 17
device checks passed with **114 passed, 5 deliberately skipped audio tests, and 0 failures**.
All three new application packages are installed and were launched on the phone. This does not
establish Android Auto launcher visibility or live voice acceptance for the new IDs.

## Source and build

- Base: `1343bd5c5180eb08ab95efea3ff38c1da4d555c6` from `origin/main`, branch `rename/com-azentrius`.
- Tested source: this PR's package and version changes; no provider, voice, storage or UI behavior change.
- Version 0.3.0, versionCode 300; prototypes use 0.3.0-prototype.
- JDK 17.0.19+10 (Eclipse Adoptium), Gradle 9.8.1; unchanged pinned toolchain and dependencies.
- Every Gradle task from `.github/workflows/android.yml` passed, plus `:storage:assembleDebugAndroidTest`
  for the isolated encrypted-storage test runner. The version/changelog check passed as well.
- Initial local incremental dexing encountered stale artifacts from the earlier checkout location.
  A clean rebuild with the build cache disabled passed. No source workaround or tooling change was made.
- APK manifests have the expected new IDs and versions. All six built APKs were checked for residual
  old project namespace references in DEX; none were found.

See [build summary](build-checks/result.json), [build output](build-checks/build.txt),
[APK hashes](build-checks/apk-sha256.json) and [package audit](build-checks/package-audit.json).
The complete local build output is retained with personal absolute paths replaced by placeholders.
+Line endings and trailing whitespace in the build and service-query output are normalized.

## Results

| Check | Result | Evidence |
|---|---|---|
| JVM suites | 100 tests, no failures | [Summary](build-checks/result.json) |
| Android lint | All seven runs passed; 0 errors and 33 warnings | [Summary](build-checks/result.json) |
| phone-all | 81 passed, 2 skipped, 0 failed | [Result](phone-all/result.json), [runner output](phone-all/instrumentation.txt) |
| car-all | 19 passed, 3 skipped, 0 failed | [Result](car-all/result.json), [runner output](car-all/instrumentation.txt) |
| storage-all | 14 passed, 0 skipped, 0 failed | [Result](storage-all/result.json), [runner output](storage-all/instrumentation.txt) |
| Three application installations and launches | 0.3.0 / 300 and 0.3.0-prototype / 300; all foreground launches confirmed | [Installations](device/installations.json), [versions](device/versions-after.json), [launches](device/launches.json) |
| DHU car service | Exactly one `com.azentrius.lautwerk.car.LautwerkCarService` | [Service query](device/car-service.txt) |
| Settings preservation | Every recorded field unchanged | [Before](device/settings-before.json), [after](device/settings-restored.json) |
| Package replacement | Two installed legacy apps removed as requested; legacy phone variant was already absent | [Removal results](device/uninstallations.json), [final versions](device/versions-final.json) |
| Lautwerk logs | Fixed UI/template events from tests, no live conversation | [Log sample](device/logcat-lautwerk.txt) |

`DesignScreenshotsTest`, `PhoneSmokeTest`, `CarTemplateTest`, `CarDesignContractTest`,
`EncryptedStorageTest` and `DocumentConversationExporterTest` are included in the suite totals.
Audio assumptions are listed separately from successful tests. No `audioPlayback`, `externalPcm`
or `pocketAudio` opt-in was supplied. No microphone, API request, DHU, car, call or headset test ran.

## Title screenshots

The four title screenshots were inspected: `Lautwerk.` is complete, including the dot, in both
themes and at 100% and 200%. The dot remains bronze in light mode and gold in dark mode.
The test also checks the title's accessible name, span color and layout overflow. The larger font
uses the existing horizontal tab scrolling. These are fixture screenshots with invented chat data.
The device-wide font scale remains 1.3; tests override only their local configuration.

- [Light, 100%](screenshots/title-light-100.png)
- [Dark, 100%](screenshots/title-dark-100.png)
- [Light, 200%](screenshots/title-light-200.png)
- [Dark, 200%](screenshots/title-dark-200.png)

## Legacy-name audit

There are **zero unexpected matches**. Every allowed match is individually listed by file and line
in [package-audit.json](build-checks/package-audit.json). The only allowed locations are:

1. `docs/evidence/0.2.0/**`, historical device records.
2. `design/azentrius/upstream-*`, immutable design snapshots.
3. Existing `CHANGELOG.md` entries predating 0.3.0.
4. The package-removal notice in the new 0.3.0 changelog section.
5. Exactly the three uninstall commands in `docs/SIDELOADING.md` under "Installing 0.3.0".

The 116 moved source files were compared with their base versions: only the requested namespace
replacement changed their contents. All 545 protected evidence/design files are byte-identical;
the earlier changelog entries are unchanged. No old namespace directory remains in the source sets.
Legacy devices in this report are identified by role to avoid duplicating their package names
outside the two approved removal notices. Those notices define the exact uninstall targets.

## Device handling and limits

The two installed 0.2.0 application packages were explicitly uninstalled, including their local data.
There is no migration. The three new packages and three test APKs were installed with ordinary
`adb install -r`, without a Google Play installer override. The old phone variant was absent.
The older OpenAuto test apps, Pocket engine and separate Alba comparison app were retained; their
version and installer metadata are unchanged. Their keys, conversations and model files were not read.

All suites ran with `adb shell am instrument -w -r` against installed APKs, not `connectedAndroidTest`.
The new apps remain installed. Settings use the same JSON shape as 0.2.0 and 0.1.19, including
language, theme, font size, Wi-Fi, Bluetooth and default TTS engine. Device identifiers are omitted.
Template tests use fake voice controls; their start/stop log lines are not real voice sessions.

## Reproduction

Follow [Installing 0.3.0](../../SIDELOADING.md#installing-030) for the explicitly destructive legacy
removal and the three new APK installations. Use the commands from the CI workflow for build,
JVM tests and lint; additionally build and install the storage instrumentation APK.

```sh
adb shell am instrument -w -r com.azentrius.lautwerk.test/androidx.test.runner.AndroidJUnitRunner
adb shell am instrument -w -r com.azentrius.lautwerk.prototype.dhu.test/androidx.test.runner.AndroidJUnitRunner
adb shell am instrument -w -r com.azentrius.lautwerk.storage.test/androidx.test.runner.AndroidJUnitRunner
adb shell cmd package query-services --brief -a androidx.car.app.CarAppService -p com.azentrius.lautwerk.prototype.dhu
```

Capture settings before installation, after every suite and at the end. Inspect final runner status
and individual results; an ADB exit code alone is insufficient. Retrieve fixture screenshots with
`adb exec-out run-as com.azentrius.lautwerk cat cache/<name>.png`, preserving binary stdout.

## Remaining manual checks

Enter each provider's keys directly in the new app, select the intended voice, then check conversations
with both providers, `Lautwerk-Chat.txt` export and German/English pronunciation. Recheck the DHU and
the parked ID.7 separately for the new package identity. Previous host acceptance does not transfer.
The previously observed Alba source-audio issue was not changed by this namespace-only PR. Its
comparison test remains available separately; call and headset tests remain deferred.

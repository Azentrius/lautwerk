# Lautwerk via sideload on the car display

## Updating to 0.3.2

Use the ordinary `adb install -r` commands below to update existing `com.azentrius.lautwerk` apps.
Expected versions are 0.3.2 / 302 and 0.3.2-prototype / 302. Do not uninstall an existing 0.3.x app.
Speech output uses the phone's Android TTS service; no separate speech companion is required.
Former companion selections return to Android default. Existing device voices and speed remain
saved, and credentials and chat histories are untouched. In the car app, output uses an installed
local voice from the system-default service. Other installed engines and diagnostic apps remain
under the user's control; this update neither uninstalls them nor removes their data.

## Installing 0.3.0

The new packages are `com.azentrius.lautwerk`, `com.azentrius.lautwerk.prototype.dhu` and
`com.azentrius.lautwerk.prototype.phone`. Android installs them as new apps, not updates of 0.2.0.
Before removing an old app, export any chats you want to keep as text. Uninstalling deletes its local
keys, chat archive and settings. Android Keystore access is tied to the app identity, so no keys or
histories are migrated and the new apps cannot import the text exports. Enter keys only in each new
app, then select its provider, model and voice again.

With exactly the intended phone selected via `ANDROID_SERIAL`, remove only the three previous app
packages that are installed. Skip any package that is absent:

```sh
adb uninstall de.azentrius.lautwerk
adb uninstall de.azentrius.lautwerk.prototype.dhu
adb uninstall de.azentrius.lautwerk.prototype.phone
```

Build with JDK 17 as described in [START_WINDOWS.md](START_WINDOWS.md), then install from the project root:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r prototype-app/build/outputs/apk/dhu/debug/prototype-app-dhu-debug.apk
adb install -r prototype-app/build/outputs/apk/phone/debug/prototype-app-phone-debug.apk
adb shell am start -n com.azentrius.lautwerk/com.azentrius.lautwerk.app.MainActivity
adb shell am start -n com.azentrius.lautwerk.prototype.dhu/com.azentrius.lautwerk.prototype.MainActivity
adb shell am start -n com.azentrius.lautwerk.prototype.phone/com.azentrius.lautwerk.prototype.MainActivity
adb shell cmd package query-services --brief -a androidx.car.app.CarAppService -p com.azentrius.lautwerk.prototype.dhu
```

Expected: version 0.3.0 (assistant) or 0.3.0-prototype (both prototypes), code 300, and exactly one
`com.azentrius.lautwerk.car.LautwerkCarService` in the DHU package. A registered service and successful
phone launch do not establish Android Auto launcher visibility. Installer metadata and previous host
acceptance do not transfer from the old package. These are ordinary ADB installations, without a
Google Play installer override. Stop on a signature conflict rather than deleting data from a new app.

Other installed speech services, model files and separate diagnostic apps are not removed by these
commands. The historical experiments below are not acceptance evidence for the new IDs. See the
[0.3.0 automated device report](evidence/0.3.0/README.md) for the actual test scope.

## Historical sideload baseline

Historical 0.1.15 baseline below. A later manual test confirmed visibility/opening on the vehicle
display after an approved installation-origin experiment. The 0.1.17 installation and
audio evidence are in [CAR_VOICE.md](CAR_VOICE.md); its Start action no longer runs a car UI counter.

As of 2026-10-12. The goal is an APK distributed through GitHub that users can install on their phone
and open in the car. To get there, we first check the unchanged UI prototype 0.1.15 in a stationary
car. A confirmed path for Lautwerk on real head units does not exist yet.

**Active attempt: ID.7 with native wireless connection, without AAWireless.**
The Kia/adapter attempts are deferred. First, the unchanged state is measured in
the ID.7; the adapter notes stay documented as a later option and are not instructions for the ID.7's
native connection.

The test app (DHU variant of `prototype-app`) shows the title, provider, model and a Start test counter.
The actual AI conversations currently run only in the assistant app (`app`). A successful visibility test
would therefore be the first step towards the car app, not yet a working AI conversation in the car.

## Paths we check

| Path | Assessment for Lautwerk |
| --- | --- |
| Normal APK installation and native Android Auto connection | Baseline test in the ID.7. Installation alone does not prove launcher visibility. |
| Normal APK installation and AAWireless | Baseline test in the Kia with the previous settings recorded. |
| AAWireless with its built-in developer mode | Concrete experimental candidate. Other projects mention this path; whether it carries over to our Car App Library and Android 17 is open. |
| AAAD | The tool's maker itself calls compatibility from Android 14 on uncertain. No evidence for our APK; currently not part of the test. |
| KingInstaller | Changes the reported installation source. Not a normal APK installation path and no evidence of current Car App Library compatibility; currently not used. |
| Internal Play distribution | Option for comparison, but not a sideload path independent of Google Play. |

Google's "Unknown sources" option in Android Auto explicitly does not unlock Car App Library apps. The
developer mode of the AAWireless adapter is a different setting. A corresponding attempt is an
unofficial compatibility test, not a function guaranteed by Google. A positive Kia result does not
carry over to the ID.7 with its native wireless connection.

The POI declaration remains a disclosed debug test placeholder. It is not a claimed AI category. App,
HostValidator, templates and permissions are not changed for this comparison. Root, installer spoofing,
third-party patches and approvals for use while driving are not part of this test series. Call,
headset and Bluetooth audio interruption tests remain deferred.

## Test package and installation

For the first car tests we use exactly the already confirmed `prototype-app:dhuDebug` build from
0.1.15. If you build it yourself, follow [Build and set up the DHU](START_WINDOWS.md). Version, SHA-256
and results are in the [car test report](SIDELOAD_TEST_REPORT.md).

PowerShell in the project folder, Android SDK installed and phone connected via USB:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
$serial = 'SERIAL_FROM_ADB_DEVICES'
$apk = Join-Path (Get-Location) 'prototype-app\build\outputs\apk\dhu\debug\prototype-app-dhu-debug.apk'
Get-FileHash -Algorithm SHA256 -LiteralPath $apk
& $adb -s $serial install -r $apk
& $adb -s $serial shell cmd package query-services --brief -a androidx.car.app.CarAppService -p com.azentrius.lautwerk.prototype.dhu
```

Expected are `Success` and `com.azentrius.lautwerk.car.LautwerkCarService`. If the same test version is
already installed, skip the reinstall. If the signature differs, do not uninstall or clear package
data: that would not be an update and could lose data. No Play installer ID is set for the comparison.

## Car comparison while parked

Before each run, note the time and time zone, APK version, Android and Android Auto versions, head
unit version and connection type. For the Kia, also record the adapter model, exact firmware, version
of the AAWireless phone app and the current values of developer mode and passthrough. Firmware and
phone app version are separate values. No firmware update during a comparison pair.

1. Close a running DHU normally. Connect the phone normally to exactly one car.
2. Look for "Lautwerk" in the Android Auto launcher. If the app is only offered under
   "Customize launcher" but is deselected, note this and select it.
3. If visible: open the app, check the title and provider/model. Tap "Start" three times. A rising
   test counter is expected. No microphone and no AI request.
4. Disconnect Android Auto normally and reconnect. Repeat opening and Start. The test counter is not
   persisted and may start over after a new car session.
5. Record the results separately: launcher entry present, UI opened, Start responds, reconnect works.
   Note error messages verbatim.

### Kia and AAWireless as a deferred option

First test with unchanged settings and record their values. This only counts as a comparison without
developer mode if developer mode was actually off.

For the second run, look for the existing "Developer mode" option in the connected AAWireless app.
Write down the original values first. If the app requires passthrough to be turned off for this
option, record this dependency as well. Only the existing adapter option is used; no firmware is
replaced and no host identity is edited manually. If the option is missing, record the run as not
feasible. The menu position depends on the app and firmware.

After the change, reconnect normally and repeat the same five steps. Then restore the original values
and repeat the baseline test. Only this comparison pair can show a link to the setting. Even a success
only proves this combination of devices and versions.

### ID.7 with native wireless connection

Connect directly to the ID.7 without AAWireless and run the same steps. There is no adapter option
here that could be carried over from the Kia. If the app stays invisible, this is recorded as a
negative result for this combination, not as a reason to clear data or reinstall repeatedly.

An additional adapter would be a separate test path and requires a suitable USB port with wired
Android Auto. This has to be checked on the specific car; native wireless support does not prove it.

## Technical evidence

On the PC, package versions and the service registration can be read before and after a run as shown
above. For car logs, note the start and end of the run; existing DHU lines must not count as car
evidence.

```powershell
& $adb -s $serial shell dumpsys package com.google.android.projection.gearhead |
    Select-String '^\s*version(Name|Code)=' | Select-Object -First 2
& $adb -s $serial logcat -d -v time 'Lautwerk:I' '*:S' |
    Select-String 'car\.(createHostValidator|onCreateSession|onCreateScreen|onGetTemplate|start)'
```

`car.onCreateScreen api=…` gives the negotiated Car API level. If the line is missing for the actual
car run, the value is "not measured". Version 8 from the earlier DHU test is no substitute. Old logs
already dropped from the ring buffer cannot be recovered afterwards.

A missing service call alone does not prove a specific cause of rejection. Do not upload full bug
reports, private app data, serial numbers or notification contents to GitHub. For the report,
versions, observations and the technical car events that belong to the run are enough.

## When this becomes a guide for users

A working path must be confirmed with the same APK in several fresh connections and after a phone
restart. Then check the installation on another phone without an old DHU session. Only then does the
combination count as supported in the compatibility table; other combinations stay open.

Distribution to users then needs a dedicated, permanently backed-up release signing key, signed APKs
in GitHub Releases with SHA-256 and certificate fingerprint, and a tested update without data loss.
Installation through the browser must be checked separately from ADB. Neither such a release APK nor
its function in the car is approved yet.

Only after access to the car UI is confirmed will the existing conversation control be connected. The
resulting audio and lifecycle questions are a separate development step.

## Sources

- [Google: real vehicles and the limit of unknown sources](https://developer.android.com/training/cars/testing#test-in-real-vehicles)
- [Google: APK distribution outside Google Play](https://developer.android.com/distribute/marketing-tools/alternative-distribution)
- [Fermata developer: AAWireless developer mode on Android 16](https://github.com/AndreyPavlenko/Fermata/discussions/756) – statement about Fermata, not a test of Lautwerk or Android 17.
- [AAAD: known limitations from Android 14 on](https://github.com/shmykelsa/AAAD)
- [KingInstaller: described installation source](https://github.com/fcaronte/KingInstaller)

The sources describe possible paths and limits. Our compatibility statements are based on the
separately recorded results in the test report.

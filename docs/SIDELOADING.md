# Sideloading OpenAuto AI onto a vehicle display

As of 2026-10-13. The goal is an APK users can obtain from GitHub, install on their
phone and open in their car. We first test the unchanged UI prototype 0.1.15.
**No successful OpenAuto AI vehicle run is confirmed yet.**

The active target is **native wireless Android Auto in the ID.7, without AAWireless**.
Kia/adapter tests are deferred at the user's request. After a negative baseline,
the current experiment uses the same APK with a changed installation origin.
Adapter options below are future alternatives, not settings for the native ID.7 connection.

`dev.openautoai.prototype.dhu` shows the title, provider, model and a Start test
counter. Actual AI conversations currently run only in `dev.openautoai.app`.
Vehicle visibility would establish access to the UI, not working AI voice in the car.

## Candidate installation paths

| Method | Status for OpenAuto AI |
| --- | --- |
| Ordinary APK install and native Android Auto | Negative user observation in the ID.7; phone-side package denial subsequently captured. Installation alone does not prove visibility. |
| Ordinary APK install and AAWireless | Deferred Kia baseline, preserving existing settings. |
| AAWireless built-in developer mode | Experimental candidate reported by other projects. Applicability to our Car App Library app and Android 17 is unproven. |
| AAAD | Its project reports uncertain compatibility from Android 14 onward. Not used; no evidence for our APK. |
| System installer with changed origin, following the KingInstaller principle | On our Pixel 10 Pro / Android 17, OpenAuto AI now appears in Android Auto's phone-side launcher settings; DHU passes again. APK byte-identical. ID.7 still pending. [Exact experiment](SIDELOAD_INSTALL_EXPERIMENT.md). |
| Internal Play distribution | A comparison option, not a Google Play-independent sideload method. |

Google's Unknown sources option explicitly does not enable Car App Library apps.
AAWireless developer mode is a different setting. Testing it would be an unofficial
compatibility experiment, not a Google guarantee. A positive Kia result would not
establish compatibility with the native wireless ID.7 connection.

POI remains a disclosed debug placeholder, not an asserted AI category. App code,
HostValidator, templates and app permissions are unchanged. No root, host patches
or changes to driving restrictions are part of these tests. Call, headset and
Bluetooth-audio interruption tests remain deferred.

Following the measured launcher denial, the user separately authorized one bounded
origin experiment: update the identical APK through the existing Android system
installer while claiming Google Play as its installer. This is explicitly installer
spoofing. The [test report](SIDELOAD_TEST_REPORT.md) records its scope and results;
it is not yet a recommendation for users. Neither KingInstaller nor Shizuku was
installed, and neither Android Auto nor the car-app code was modified.

## Baseline APK and ordinary installation

**While the origin experiment is active, do not run the ordinary ADB installation
below:** it can change the origin again. Preserve the vehicle-test baseline and
use the [separate experiment guide](SIDELOAD_INSTALL_EXPERIMENT.md).

The initial comparison uses the previously accepted `prototype-app:dhuDebug` build
of 0.1.15. [Windows build and DHU setup](START_WINDOWS.md).
[Versions, hashes and measurements](SIDELOAD_TEST_REPORT.md).

For the ordinary baseline, PowerShell in the project directory with Android SDK
installed and the phone connected:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
$serial = 'SERIAL_FROM_ADB_DEVICES'
$apk = Join-Path (Get-Location) 'prototype-app\build\outputs\apk\dhu\debug\prototype-app-dhu-debug.apk'
Get-FileHash -Algorithm SHA256 -LiteralPath $apk
& $adb -s $serial install -r $apk
& $adb -s $serial shell cmd package query-services --brief -a androidx.car.app.CarAppService -p dev.openautoai.prototype.dhu
```

Expect `Success` and `dev.openautoai.car.OpenAutoCarService`. Skip reinstallation
when the same test version is already present. On a signature mismatch, do not
uninstall or delete data: that is not an update and can lose data. No Play installer
ID is supplied for this ordinary baseline.

## Parked vehicle comparison

Record time/timezone, APK, Android and Android Auto versions, infotainment version
and connection type. For an adapter record its exact hardware/firmware, phone-app
version and current developer-mode/passthrough settings. Do not update firmware
between paired comparisons.

1. Close the DHU normally. Connect the phone normally to exactly one vehicle.
2. Look for OpenAuto AI in Android Auto. If Customize launcher offers it unchecked,
   record that and select it. An absent entry cannot be enabled there.
3. If visible, open it, check title and provider/model, and tap Start three times.
   Expect an increasing counter. No microphone or AI request is involved.
4. Disconnect Android Auto normally, reconnect, and repeat Open and Start. The
   in-memory counter may restart with a new car session.
5. Record visibility, Open, Start and Reconnect separately; preserve exact error text.

### ID.7 native wireless

Connect directly without AAWireless. No adapter setting carries over to this path.
A missing app is a negative result for that combination, not a reason to clear data
or repeatedly reinstall unchanged software. The current origin experiment provides
a concrete changed baseline, unlike another blind repeat of the earlier test.

An additional adapter would be a separate experiment and would require a suitable
USB port with wired Android Auto on this particular vehicle. Native wireless
support alone does not prove wired support.

### Deferred Kia / AAWireless option

First test unchanged settings and record their values; call it “developer mode off”
only if that is actually observed. Then inspect the connected AAWireless app for its
built-in developer-mode option. Record original settings before changing anything.
If the option requires passthrough off, record that dependency. Use only the built-in
option, with no firmware replacement or manual host-identity edits. If absent, mark
the experiment unavailable. Menu location depends on app/firmware versions.

Reconnect normally and repeat the same checks. Restore original values and repeat
the baseline. Only that comparison can support an effect of the setting, and only
for the tested hardware/software combination. These steps are currently deferred.

## Technical evidence

Check package versions and service registration before and after runs. Record
actual car-session start/end times; old DHU events are not vehicle evidence.

```powershell
& $adb -s $serial shell dumpsys package com.google.android.projection.gearhead |
    Select-String '^\s*version(Name|Code)=' | Select-Object -First 2
& $adb -s $serial logcat -d -v time 'OpenAutoAI:I' '*:S' |
    Select-String 'car\.(createHostValidator|onCreateSession|onCreateScreen|onGetTemplate|start)'
```

`car.onCreateScreen api=…` identifies the negotiated Car API. If absent for the
actual vehicle run, record “not measured”; the DHU's API 8 is not a substitute.
Logs already overwritten in Android's ring buffer cannot be recovered later.
The [tested bounded capture helper](../tools/sideload/README.md) continues across
USB disconnection and limits collection to relevant messages.

A missing service invocation alone does not prove a particular cause. Do not publish
complete bugreports, private app data, serials or notifications. Versions, observations
and sanitized car events suffice for the public report.

## Requirements before recommending distribution to users

Confirm the same APK across multiple fresh connections and a phone restart. Then
test another phone with no prior DHU session. Only then mark the tested combination
supported in a compatibility table; other combinations stay unverified.

User distribution also needs a dedicated, safely backed-up release signing key,
signed GitHub Release APKs with SHA-256 and certificate fingerprint, and a tested
update preserving data. Browser installation must be checked separately from ADB.
No such release APK or vehicle compatibility is approved yet.

Connecting the existing conversation controller is a later step after vehicle UI
access is confirmed. Its audio and lifecycle behavior needs separate testing.

## Sources

- [Google: real vehicles and limits of Unknown sources](https://developer.android.com/training/cars/testing#test-in-real-vehicles).
- [Google: APK distribution outside Play](https://developer.android.com/distribute/marketing-tools/alternative-distribution).
- [Fermata developer on AAWireless developer mode under Android 16](https://github.com/AndreyPavlenko/Fermata/discussions/756): evidence about Fermata, not OpenAuto AI or Android 17.
- [AAAD compatibility caveats](https://github.com/shmykelsa/AAAD).
- [KingInstaller origin mechanism](https://github.com/fcaronte/KingInstaller).
- [Vela #179, particularly the 2026-09-21 update](https://github.com/PimpinPumpkin/Vela/issues/179): a reported stock Pixel 9 success; no test of our app or car. Later reports supersede that issue's earlier conclusion that it could never work.

Sources identify candidates and constraints. Our compatibility claims rely on the
separately recorded measurements in the test report.

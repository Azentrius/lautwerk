# Native-installer origin experiment

As of 2026-10-13. **An experiment, not an approved distribution method.** On the
Pixel 10 Pro with Android 17 and Android Auto 17.7.663654-release, this procedure
made OpenAuto AI appear in Android Auto's phone-side launcher settings. Opening
and Start in the DHU also pass. The native wireless ID.7 result is still pending.
See the [measurements and limitations](SIDELOAD_TEST_REPORT.md).

This procedure claims Google Play as the installer although the APK comes from
the PC. The user explicitly authorized that limited experiment. It is installer
origin spoofing, not an ordinary sideload or an official Android Auto approval.
It installs neither KingInstaller nor Shizuku, requires no root, and changes
neither Android Auto nor the APK. The POI category remains a disclosed test placeholder.

## Exact tested baseline

- Package: `dev.openautoai.prototype.dhu`, 0.1.15-prototype, version code 6.
- Update the existing installation; do not uninstall it.
- APK SHA-256: `F580E27ACEC2395AE0097E0A8E7ACE4F7B62288B1CEAD6B6698042A6505F5D70`.
- Certificate SHA-256: `687cf415a6b6d4f45a8a92a55a9fcd91c463bc0d0d9d8a77ce95c9afbe63e696`.
- System installer: `com.google.android.packageinstaller/com.android.packageinstaller.InstallStart`.
- Android Auto developer mode and Unknown sources were already on and remained unchanged.

Unknown sources alone had not made the app visible. Other APKs, a fresh installation,
another phone, and browser-only installation have not been tested with this method.
The checksum below deliberately stops on a different APK. A local rebuild can
produce a different hash because of signing or build differences; it would require
a separate experiment rather than silently dropping this check.

## Install on the connected Pixel

Run PowerShell in the existing project directory. Unlock the Pixel, authorize USB
debugging, and close any DHU session normally.

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$serial = 'SERIAL_FROM_ADB_DEVICES'
$apk = Join-Path (Get-Location) 'prototype-app\build\outputs\apk\dhu\debug\prototype-app-dhu-debug.apk'
$expectedHash = 'F580E27ACEC2395AE0097E0A8E7ACE4F7B62288B1CEAD6B6698042A6505F5D70'
if ((Get-FileHash -Algorithm SHA256 -LiteralPath $apk).Hash -ne $expectedHash) {
    throw 'Not the APK used in the documented experiment; do not install.'
}
& $adb -s $serial shell dumpsys package dev.openautoai.prototype.dhu |
    Select-String 'versionCode=|versionName=|installerPackageName=|initiatingPackageName='
& $adb -s $serial push $apk /sdcard/Download/OpenAutoAI-0.1.15-dhu-sideload-test.apk
$uri = 'content://com.android.externalstorage.documents/document/primary%3ADownload%2FOpenAutoAI-0.1.15-dhu-sideload-test.apk'
& $adb -s $serial shell am start -a android.intent.action.INSTALL_PACKAGE -n com.google.android.packageinstaller/com.android.packageinstaller.InstallStart -d $uri -t application/vnd.android.package-archive -f 0x10000001 --es android.intent.extra.INSTALLER_PACKAGE_NAME com.android.vending --ez android.intent.extra.NOT_UNKNOWN_SOURCE true --es android.intent.extra.REFERRER_NAME android-app://com.android.vending --ei android.content.pm.extra.INSTALL_REASON 1
```

The observed German dialog read “Diese App aktualisieren?” and named OpenAuto AI.
Choose “Aktualisieren”. If the package differs or the signature is incompatible,
stop; do not substitute an uninstall or data reset.

Google Play Protect then requested a scan on this Pixel. “App scannen” can send the
APK to Google and was separately authorized by the user. Decline the installation
if that transfer is unwanted. In this run Google reported “Diese App ist anscheinend
sicher”; only then was “Installieren” confirmed. Play Protect was not disabled and
no warning was skipped.

## Verify each outcome separately

```powershell
& $adb -s $serial shell dumpsys package dev.openautoai.prototype.dhu |
    Select-String 'versionCode=|versionName=|installerPackageName=|initiatingPackageName=|lastUpdateTime='
& $adb -s $serial shell cmd package query-services --brief -a androidx.car.app.CarAppService -p dev.openautoai.prototype.dhu
```

The successful metadata comparison returned:

```text
versionCode=6
versionName=0.1.15-prototype
installerPackageName=com.android.vending
initiatingPackageName=com.google.android.packageinstaller
```

Those values alone do not prove compatibility. With no car or DHU session running:

```powershell
& $adb -s $serial shell am force-stop com.google.android.projection.gearhead
& $adb -s $serial shell am start -a com.google.android.projection.gearhead.SETTINGS -p com.google.android.projection.gearhead
```

In Customize launcher (“Launcher anpassen”), OpenAuto AI was now present and
already checked. Restarting the process does not clear Android Auto data, but does
interrupt sessions; run it only while disconnected. Do not additionally reset
Google Play services, clear caches, or delete app data.

The corresponding DHU control passed. The ID.7 requires two parked wireless
connections with Open and Start, plus the [bounded log capture](../tools/sideload/README.md).
Only that will establish the vehicle result. Reboot, update and additional-phone
tests would follow before claiming a repeatable user distribution method.

## Updates and returning to ordinary installation

A subsequent ADB or in-app update may change the origin again. Recheck both origin
fields and launcher visibility after each update; do not assume permanent eligibility.

To end the experiment, the same unchanged, equally signed APK can be updated through
the ordinary ADB path, followed by another origin inspection:

```powershell
& $adb -s $serial install -r $apk
& $adb -s $serial shell dumpsys package dev.openautoai.prototype.dhu |
    Select-String 'installerPackageName=|initiatingPackageName='
```

This return path has not been executed in this run, to preserve the positive
baseline for the vehicle test. It does not promise an exact reset of every piece
of installation metadata. There are no app-code changes to revert for the DHU.

## Basis for the experiment

- [Google: real vehicles and the limits of Unknown sources](https://developer.android.com/training/cars/testing#test-in-real-vehicles).
- [KingInstaller](https://github.com/fcaronte/KingInstaller): its public source uses the system installer with a changed installer declaration. The intent extras above were inspected there; the tool itself was not installed.
- [Vela #179](https://github.com/PimpinPumpkin/Vela/issues/179): later developer reports include success on a stock Pixel 9. This motivated the experiment; it is not evidence for our app or vehicle.

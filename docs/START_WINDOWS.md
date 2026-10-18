# Windows: build, install and check in the DHU

Project state 0.1.14: DHU UI 0.1.14-prototype with the requested Azentrius colors; the phone comparison
variant stays at 0.1.12-prototype. The German/English flow is unchanged.
Car design, baseline measurement and acceptance: [ANDROID_AUTO_DESIGN.md](ANDROID_AUTO_DESIGN.md).
The assistant app 0.1.13-dev is separate; its installation and the hands-on API and voice test are in
[VOICE.md](VOICE.md). The current design update with installation steps is in
[DESIGN_SYSTEM.md](DESIGN_SYSTEM.md).

## 1. Build the project

Android Studio: open the project folder. Under Settings > Build, Execution, Deployment > Build Tools >
Gradle, choose an installed **JDK 17**.
This project uses Gradle 9.8.1 and AGP 9.4.1 and is built and checked with JDK 17; JDK 25, which is
also installed on this PC, has not been tried for it. The JAVA_HOME value found on this PC pointed to a
JDK folder that does not exist and was not changed globally.

PowerShell on this PC:

```powershell
Set-Location -LiteralPath 'C:\Users\stefa\Documents\Projekte\Azentrius\Lautwerk'
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat :prototype-app:assembleDhuDebug :prototype-app:assemblePhoneDebug :app:assembleDebug
```

Other computers: set JAVA_HOME to the JDK 17 installed there and ANDROID_HOME to the Android SDK folder
there. SDK Platform 37 (Android 17) and Build Tools 36.0.0 are required. The first build downloads
public Maven dependencies.

The most important APK is then here:
`prototype-app\build\outputs\apk\dhu\debug\prototype-app-dhu-debug.apk`.
A separately supplied DHU debug APK is the same test variant.
The phone comparison variant `phoneDebug` deliberately registers NO car app.

## 2. Install only the DHU prototype

Unlock the Pixel, enable USB debugging, confirm the RSA prompt if asked.

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
```

The Pixel must be listed as `device`. `unauthorized`: confirm on the Pixel. `offline`: check the
USB/ADB connection. With several devices, replace `SERIAL_FROM_ADB_DEVICES` in the following block
with the Pixel's serial number.

```powershell
$serial = 'SERIAL_FROM_ADB_DEVICES'
$apk = Join-Path (Get-Location) 'prototype-app\build\outputs\apk\dhu\debug\prototype-app-dhu-debug.apk'
& $adb -s $serial install -r $apk
& $adb -s $serial shell am start -n 'de.azentrius.lautwerk.prototype.dhu/de.azentrius.lautwerk.prototype.MainActivity'
```

For the downloaded APK, set `$apk` to its full path instead. With only one connected device,
`-s $serial` can be left out everywhere.
Expected: `Success`; on the phone the title "Lautwerk." and "Start".
One tap must show "Started · Test 1". This does not yet prove visibility in the car.
Also choose a provider and model; the selection is saved.
For 0.1.2, run the [selection test](MODEL_SELECTION.md#checking-in-the-real-dhu): choose
Claude / Haiku, reopen the app, check the selection in the DHU and then switch to OpenAI / GPT-5.4.
The template shows the selection below the test status.

`INSTALL_FAILED_UPDATE_INCOMPATIBLE` means the package was built with a different debug certificate.
Do not uninstall automatically: that would delete package data. Either use the same signing key or
decide on the path deliberately. A fresh local build uses your local Android debug key.
The assistant package `de.azentrius.lautwerk` is not affected by these commands.

## 3. Check the service registration

```powershell
& $adb -s $serial shell pm path de.azentrius.lautwerk.prototype.dhu
& $adb -s $serial shell cmd package query-services --brief -a androidx.car.app.CarAppService -p de.azentrius.lautwerk.prototype.dhu
& $adb -s $serial shell dumpsys package de.azentrius.lautwerk.prototype.dhu
```

Expected service: `de.azentrius.lautwerk.car.LautwerkCarService`.
If the service is missing, check the package and build variant first. An existing service
registration only proves that Android knows the service, not that Android Auto allows it or shows it
in the launcher.

## 4. Reuse the existing DHU setup

1. After installing, close an already running DHU session normally.
2. On the Pixel, open the Android Auto settings. Android Auto's own developer mode must be active (in
   addition to Android USB debugging).
3. In the Android Auto menu, choose "Start head unit server" if it is not running yet.
4. Under previously connected cars, adding new cars must be allowed.
5. Leave the Pixel unlocked and connected to USB.

PowerShell:

```powershell
& $adb -s $serial forward tcp:5277 tcp:5277
Push-Location "$env:LOCALAPPDATA\Android\Sdk\extras\google\auto"
try { .\desktop-head-unit.exe } finally { Pop-Location }
```

`libusb-1.0.dll` must stay next to the DHU program. Confirm any first-time setup dialogs on the Pixel
itself. Do not delete existing AA data.

6. In the DHU, open the app launcher and look for **Lautwerk**.
7. If present: open it, check the title "Lautwerk", tap "Start" once.
8. Expected: "Started · Test 1". Tapping again increases the local test counter.
9. Close the DHU normally. Optionally remove the port forwarding:

```powershell
& $adb -s $serial forward --remove tcp:5277
```

Android Auto decides the screen layout and button position; the app supplies a `MessageTemplate`, not
a freely drawn car activity.

## 5. If Lautwerk is missing

- Phone APK installed successfully, but only the phone icon visible:
  check step 3 and `dhuDebug` first.
- Look under "Customize launcher" in Android Auto and turn the entry on
  **if Android Auto already offers it**. An entry that is missing there cannot be forced this way.
- Test a fresh DHU connection. No repeated reinstalls or data resets.
- Save the current version and the app's own logs:

```powershell
& $adb -s $serial shell dumpsys package com.google.android.projection.gearhead | Select-String 'versionName='
& $adb -s $serial logcat -d -v time 'Lautwerk:V' 'AndroidRuntime:E' '*:S'
```

Reading the app logs:

| Observation | What it shows |
|---|---|
| `phone.onCreate` | Phone activity started |
| registered service, but no car log | Android Auto may not have bound the service yet; no proof of a specific cause of rejection |
| `car.createHostValidator` | Host connection attempt; host check follows |
| `car.onCreateSession` / `car.onCreateScreen` | Car session was set up |
| `car.onGetTemplate` | Host requested the template |
| `car.start` | Car button was triggered |

Without a matching host error message, the cause of a missing icon stays open: category, installation
source, host version and connection are separate factors. The POI declaration is only the declared
debug test placeholder; the assistant provides no POI product function. "Unknown sources" is not a
general approval for this kind of library. Do not use a faked Play installer ID, root tools or patches.

If the host blocks the app, that is a valid negative test result. The next steps are checking a
product scope that is actually permitted and, if needed, official test distribution, not bypassing
the rejection. An internal Play test alone does not make a non-permitted category permitted.

## 6. Automated UI and template checks

```powershell
.\gradlew.bat :prototype-app:lintDhuDebug :prototype-app:connectedDhuDebugAndroidTest
```

Before UI tests, close the DHU normally and leave the Pixel unlocked. An active Android Auto/Maps
session can push the test activity aside.

This installs the test app and the test package on the connected Android device.
The tests check the phone button including state restoration, and the real car template and its click
callback. These tests run with a TestCarContext and **prove neither launcher visibility nor a DHU
connection**. With several connected devices, first choose the device with `ANDROID_SERIAL` or
disconnect the other test devices.

## 7. Existing assistant app

`:app:assembleDebug` produces `app\build\outputs\apk\debug\app-debug.apk`.
This module has its own package `de.azentrius.lautwerk`.
It was not connected to the car service for the DHU visibility test.
Local storage, Keystore and error cases are checked with isolated test data; details in
[LOCAL_STORAGE.md](LOCAL_STORAGE.md). Instructions and the confirmed state of the new voice feature
are in [VOICE.md](VOICE.md).

[German/English and resuming](LOCALIZATION.md).

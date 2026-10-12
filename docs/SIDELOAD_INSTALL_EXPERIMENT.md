# Herkunftsversuch mit dem Android-Systeminstaller

Stand 13.10.2026. **Experiment, kein freigegebener Verteilungsweg.** Auf dem
Pixel 10 Pro mit Android 17 und Android Auto 17.7.663654-release erscheint
OpenAuto AI nach diesem Verfahren in „Launcher anpassen“; Öffnen und Starten im
DHU funktionieren. Das Ergebnis im nativ drahtlos verbundenen ID.7 ist noch offen.
[Messbericht und Grenzen](SIDELOAD_TEST_REPORT.md).

Dieses Verfahren gibt Google Play als Installationsherkunft an, obwohl die APK
vom PC kommt. Es wurde nach ausdrücklicher Nutzerfreigabe getestet. Es ist kein
normaler Sideload und keine offizielle Android-Auto-Zulassung. Es installiert
weder KingInstaller noch Shizuku, benötigt kein Root und verändert weder Android
Auto noch die APK. Die POI-Kategorie bleibt der offengelegte Testplatzhalter.

## Genau getestete Ausgangslage

- Paket: `dev.openautoai.prototype.dhu`, 0.1.15-prototype, Versionscode 6.
- Vorhandene Installation wird aktualisiert, nicht deinstalliert.
- APK SHA-256: `F580E27ACEC2395AE0097E0A8E7ACE4F7B62288B1CEAD6B6698042A6505F5D70`.
- Zertifikat SHA-256: `687cf415a6b6d4f45a8a92a55a9fcd91c463bc0d0d9d8a77ce95c9afbe63e696`.
- Systeminstaller: `com.google.android.packageinstaller/com.android.packageinstaller.InstallStart`.
- Android-Auto-Entwicklermodus und „Unbekannte Quellen“ waren schon an und bleiben unverändert.

Das unbekannte-Quellen-Häkchen allein hatte die App vorher nicht sichtbar gemacht.
Ein anderer Build, eine frische Installation, ein anderes Telefon oder eine
Installation allein über den Browser ist noch nicht mit diesem Verfahren geprüft.
Die folgende Hashprüfung stoppt bewusst bei anderen APKs. Ein eigener Neubau kann
wegen Signierung oder Build-Unterschieden einen anderen Hash besitzen und fällt
nicht unter diesen exakt reproduzierten Versuch.

## Installation am verbundenen Pixel

PowerShell im bestehenden Projektordner. Pixel entsperren, USB-Debugging
bestätigen und einen laufenden DHU regulär beenden.

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$serial = 'SERIENNUMMER_AUS_ADB_DEVICES'
$apk = Join-Path (Get-Location) 'prototype-app\build\outputs\apk\dhu\debug\prototype-app-dhu-debug.apk'
$expectedHash = 'F580E27ACEC2395AE0097E0A8E7ACE4F7B62288B1CEAD6B6698042A6505F5D70'
if ((Get-FileHash -Algorithm SHA256 -LiteralPath $apk).Hash -ne $expectedHash) {
    throw 'Andere APK als im dokumentierten Versuch; nicht installieren.'
}
& $adb -s $serial shell dumpsys package dev.openautoai.prototype.dhu |
    Select-String 'versionCode=|versionName=|installerPackageName=|initiatingPackageName='
& $adb -s $serial push $apk /sdcard/Download/OpenAutoAI-0.1.15-dhu-sideload-test.apk
$uri = 'content://com.android.externalstorage.documents/document/primary%3ADownload%2FOpenAutoAI-0.1.15-dhu-sideload-test.apk'
& $adb -s $serial shell am start -a android.intent.action.INSTALL_PACKAGE -n com.google.android.packageinstaller/com.android.packageinstaller.InstallStart -d $uri -t application/vnd.android.package-archive -f 0x10000001 --es android.intent.extra.INSTALLER_PACKAGE_NAME com.android.vending --ez android.intent.extra.NOT_UNKNOWN_SOURCE true --es android.intent.extra.REFERRER_NAME android-app://com.android.vending --ei android.content.pm.extra.INSTALL_REASON 1
```

Der tatsächlich geprüfte Dialog hieß „Diese App aktualisieren?“ und nannte
„OpenAuto AI“. Dort „Aktualisieren“ wählen. Bei abweichendem Paketnamen oder
Signaturfehler abbrechen; nicht als Ersatz deinstallieren oder Daten löschen.

Auf diesem Pixel verlangte Google Play Protect anschließend eine App-Prüfung.
„App scannen“ kann die APK an Google übertragen und wurde dafür gesondert erlaubt.
Wer diese Übermittlung nicht möchte, bricht den Vorgang ab. Im Versuch meldete
Google danach „Diese App ist anscheinend sicher“; erst dann wurde „Installieren“
bestätigt. Play Protect wurde nicht deaktiviert und keine Warnung übersprungen.

## Ergebnis getrennt prüfen

```powershell
& $adb -s $serial shell dumpsys package dev.openautoai.prototype.dhu |
    Select-String 'versionCode=|versionName=|installerPackageName=|initiatingPackageName=|lastUpdateTime='
& $adb -s $serial shell cmd package query-services --brief -a androidx.car.app.CarAppService -p dev.openautoai.prototype.dhu
```

Im erfolgreichen Metadatenvergleich standen:

```text
versionCode=6
versionName=0.1.15-prototype
installerPackageName=com.android.vending
initiatingPackageName=com.google.android.packageinstaller
```

Das allein genügt noch nicht. Wenn keine Fahrzeug- oder DHU-Verbindung läuft:

```powershell
& $adb -s $serial shell am force-stop com.google.android.projection.gearhead
& $adb -s $serial shell am start -a com.google.android.projection.gearhead.SETTINGS -p com.google.android.projection.gearhead
```

Unter „Launcher anpassen“ war OpenAuto AI danach vorhanden und bereits angehakt.
Der Prozessneustart löscht keine Android-Auto-Daten, beendet aber laufende
Sitzungen; daher nur getrennt vom Auto ausführen. Nicht zusätzlich Google Play-
Dienste zurücksetzen oder Cache/Daten löschen.

Der zugehörige DHU-Kontrolllauf ist bestanden. Für den ID.7 folgen zwei normale
Funkverbindungen im Stand mit Öffnen und Starten sowie die
[begrenzte Log-Aufzeichnung](../tools/sideload/README.md). Erst das bestätigt den
Fahrzeugweg; anschließend wären Neustart-, Update- und weitere Telefon-Tests nötig.

## Aktualisierungen und Rückkehr zum normalen Installationsweg

Ein späteres normales ADB-/App-Update kann die Herkunft wieder ändern. Deshalb
bei jedem Update die beiden Herkunftsfelder und die Launcher-Sichtbarkeit neu
prüfen. Nicht stillschweigend annehmen, dass diese Freigabe dauerhaft gilt.

Wer den Versuch beenden möchte, kann die unveränderte, gleich signierte APK wieder
über den gewöhnlichen ADB-Weg aktualisieren und anschließend die Herkunft auslesen:

```powershell
& $adb -s $serial install -r $apk
& $adb -s $serial shell dumpsys package dev.openautoai.prototype.dhu |
    Select-String 'installerPackageName=|initiatingPackageName='
```

Diese Rückkehr wurde in diesem Lauf noch nicht ausgeführt, um die positive
Ausgangslage für den Fahrzeugtest zu erhalten. Eine bytegenaue Rücksetzung aller
Installationsmetadaten wird damit nicht zugesichert. Unabhängig davon gibt es keine
Änderungen am App-Code, die für den DHU zurückgenommen werden müssten.

## Herkunft des Versuchs

- [Google: reale Fahrzeuge und Grenzen von „Unbekannte Quellen“](https://developer.android.com/training/cars/testing#test-in-real-vehicles).
- [KingInstaller](https://github.com/fcaronte/KingInstaller): dessen offener Quellcode nutzt den Systeminstaller mit einer geänderten Installer-Angabe. Die hier verwendeten Intent-Extras wurden dort nachvollzogen; das Tool selbst wurde nicht installiert.
- [Vela #179](https://github.com/PimpinPumpkin/Vela/issues/179): spätere Entwicklerberichte nennen unter anderem einen Erfolg auf einem Stock-Pixel 9. Das motivierte den Versuch, ersetzt aber keinen Test unserer App.

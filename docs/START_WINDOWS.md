# Windows: Bauen, Installieren und DHU prüfen

Projektstand 0.1.14: DHU-Oberfläche 0.1.14-prototype mit Azentrius-Farbwunsch;
Phone-Vergleichsvariante weiterhin 0.1.12-prototype. Ablauf mit Deutsch/Englisch unverändert.
Auto-Design, Ausgangsmessung und Abnahme: [ANDROID_AUTO_DESIGN.md](ANDROID_AUTO_DESIGN.md).
Die Assistenten-App 0.1.13-dev ist separat; ihre Installation und der
praktische API-/Sprachtest stehen in [VOICE.md](VOICE.md). Das aktuelle Design-Update
mit Installationsschritten steht in [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md).

## 1. Projekt bauen

Android Studio: Projektordner öffnen. Unter Settings > Build, Execution,
Deployment > Build Tools > Gradle ein installiertes **JDK 17** wählen.
Dieses Projekt nutzt Gradle 8.13; das auf diesem PC ebenfalls vorhandene
JDK 25 ist für diese Kombination nicht geeignet. Der vorgefundene JAVA_HOME-Wert
zeigte auf ein nicht vorhandenes JDK-Verzeichnis und wurde nicht global geändert.

PowerShell auf diesem PC:

```powershell
Set-Location -LiteralPath 'C:\Users\stefa\Documents\Projekte\Azentrius\OpenAuto AI'
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat :prototype-app:assembleDhuDebug :prototype-app:assemblePhoneDebug :app:assembleDebug
```

Andere Rechner: JAVA_HOME auf das dort installierte JDK 17 und ANDROID_HOME
auf den dortigen Android-SDK-Ordner setzen. SDK API 36 und Build Tools 35.0.0
werden benötigt. Der erste Build lädt öffentliche Maven-Abhängigkeiten.

Das wichtigste APK liegt anschließend hier:
`prototype-app\build\outputs\apk\dhu\debug\prototype-app-dhu-debug.apk`.
Die separat gelieferte Datei `OpenAutoAI-0.1.14-dhu-debug.apk` ist dieselbe Testvariante.
Die Smartphone-Vergleichsvariante `phoneDebug` registriert absichtlich KEINE Auto-App.

## 2. Nur den DHU-Prototypen installieren

Pixel entsperren, USB-Debugging aktivieren, RSA-Abfrage bei Bedarf bestätigen.

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
```

Es muss `device` hinter dem Pixel stehen. `unauthorized`: am Pixel bestätigen.
`offline`: USB/ADB-Verbindung prüfen. Bei mehreren Geräten im folgenden Block
`SERIENNUMMER_AUS_ADB_DEVICES` durch die Seriennummer des Pixel ersetzen.

```powershell
$serial = 'SERIENNUMMER_AUS_ADB_DEVICES'
$apk = Join-Path (Get-Location) 'prototype-app\build\outputs\apk\dhu\debug\prototype-app-dhu-debug.apk'
& $adb -s $serial install -r $apk
& $adb -s $serial shell am start -n 'dev.openautoai.prototype.dhu/dev.openautoai.prototype.MainActivity'
```

Für das heruntergeladene APK stattdessen `$apk` auf dessen vollständigen Pfad setzen.
Bei nur einem verbundenen Gerät darf `-s $serial` überall entfallen.
Erwartet: `Success`; auf dem Handy „OpenAuto AI“ und „Starten“.
Ein Tippen muss „Gestartet · Test 1“ anzeigen. Das beweist noch keine Auto-Sichtbarkeit.
Zusätzlich Anbieter und Modell wählen; die Auswahl bleibt gespeichert.
Für 0.1.2 den [Auswahltest](MODEL_SELECTION.md#prüfung-im-echten-dhu) durchführen:
Claude / Haiku wählen, App neu öffnen, Auswahl im DHU prüfen und anschließend
auf OpenAI / GPT-5.4 wechseln. Das Template zeigt die Auswahl unter dem Teststatus.

Bei `INSTALL_FAILED_UPDATE_INCOMPATIBLE` wurde das Paket mit einem anderen
Debug-Zertifikat gebaut. Nicht automatisch deinstallieren: das würde Paketdaten
löschen. Entweder denselben Signierschlüssel verwenden oder den Weg bewusst entscheiden.
Bei einem frischen lokalen Build wird dein lokaler Android-Debug-Schlüssel verwendet.
Das bestehende Paket `dev.openautoai.app` bleibt von diesen Befehlen unberührt.

## 3. Service-Registrierung prüfen

```powershell
& $adb -s $serial shell pm path dev.openautoai.prototype.dhu
& $adb -s $serial shell cmd package query-services --brief -a androidx.car.app.CarAppService -p dev.openautoai.prototype.dhu
& $adb -s $serial shell dumpsys package dev.openautoai.prototype.dhu
```

Erwarteter Dienst: `dev.openautoai.car.OpenAutoCarService`.
Wenn der Dienst fehlt, zuerst Paket/Build-Variante prüfen. Eine vorhandene
Service-Registrierung beweist nur, dass Android den Dienst kennt; nicht, dass
Android Auto ihn zulässt oder im Launcher anzeigt.

## 4. Bestehende DHU-Einrichtung weiterverwenden

1. Eine bereits laufende DHU-Sitzung nach Installation regulär schließen.
2. Auf dem Pixel Android-Auto-Einstellungen öffnen. Der Android-Auto-eigene
   Entwicklermodus muss aktiv sein (zusätzlich zum Android-USB-Debugging).
3. Im Android-Auto-Menü „Head-Unit-Server starten“ auswählen, falls er noch nicht läuft.
4. Unter zuvor verbundenen Autos muss das Hinzufügen neuer Autos erlaubt sein.
5. Pixel entsperrt am USB-Anschluss lassen.

PowerShell:

```powershell
& $adb -s $serial forward tcp:5277 tcp:5277
Push-Location "$env:LOCALAPPDATA\Android\Sdk\extras\google\auto"
try { .\desktop-head-unit.exe } finally { Pop-Location }
```

`libusb-1.0.dll` muss beim DHU-Programm bleiben. Eventuelle Ersteinrichtungsdialoge
am Pixel selbst bestätigen. Keine vorhandenen AA-Daten löschen.

6. Im DHU den App-Launcher öffnen und nach **OpenAuto AI** suchen.
7. Falls vorhanden: öffnen, Titel „OpenAuto AI“ prüfen, einmal „Starten“ tippen.
8. Erwartet: „Gestartet · Test 1“. Erneutes Tippen erhöht den lokalen Testzähler.
9. DHU regulär beenden. Portfreigabe optional entfernen:

```powershell
& $adb -s $serial forward --remove tcp:5277
```

Die Bildschirmaufteilung und Buttonposition bestimmt Android Auto; die App
liefert ein `MessageTemplate`, keine beliebig gezeichnete Auto-Activity.

## 5. Wenn OpenAuto AI fehlt

- Telefon-APK erfolgreich installiert, aber nur Handy-Icon sichtbar:
  zunächst Schritt 3 und `dhuDebug` prüfen.
- In Android Auto „Launcher anpassen“ nachsehen und den Eintrag einschalten,
  **falls Android Auto ihn bereits anbietet**. Ein dort fehlender Eintrag kann
  damit nicht erzwungen werden.
- Eine frische DHU-Verbindung testen. Keine dauernden Neuinstallationen oder Datenresets.
- Aktuelle Version und App-eigene Logs sichern:

```powershell
& $adb -s $serial shell dumpsys package com.google.android.projection.gearhead | Select-String 'versionName='
& $adb -s $serial logcat -d -v time 'OpenAutoAI:V' 'AndroidRuntime:E' '*:S'
```

Interpretation der App-Logs:

| Beobachtung | Was sie belegt |
|---|---|
| `phone.onCreate` | Smartphone-Activity gestartet |
| registrierter Dienst, aber kein Car-Log | Android Auto hat den Dienst möglicherweise noch nicht gebunden; kein Beweis einer bestimmten Ablehnungsursache |
| `car.createHostValidator` | Host-Verbindungsversuch; Hostprüfung folgt |
| `car.onCreateSession` / `car.onCreateScreen` | Car-Session wurde aufgebaut |
| `car.onGetTemplate` | Host hat die Vorlage angefordert |
| `car.start` | Auto-Button wurde ausgelöst |

Without a matching host error, category, installation origin, host version and
connection remain separate possible causes. POI is only the disclosed debug
placeholder; the assistant does not implement a POI product. Unknown sources is
not a general unlock for this library. This ordinary DHU procedure needs no
changed installer declaration, root tools or patches.

A host rejection is a valid negative test result. An internal Play test alone
does not make an unsupported category eligible. The separately authorized
[origin experiment of 2026-10-13](SIDELOAD_INSTALL_EXPERIMENT.md) is documented
elsewhere: the phone-side launcher entry and DHU work; the ID.7 result remains
pending. This is not an official product approval.

**While the origin experiment is active:** ordinary installation or the device
tests below can change the origin again. Do not run them between the prepared
baseline and the vehicle comparison.

## 6. Automatisierte UI-/Template-Prüfungen

```powershell
.\gradlew.bat :prototype-app:lintDhuDebug :prototype-app:connectedDhuDebugAndroidTest
```

Vor UI-Tests den DHU regulär beenden und das Pixel entsperrt lassen. Eine aktive
Android-Auto/Maps-Sitzung kann die Test-Activity verdrängen.

Das installiert Test-App und Testpaket auf dem angeschlossenen Android-Gerät.
Die Tests prüfen die Smartphone-Schaltfläche samt Zustandswiederherstellung sowie
das echte Car-Template und seinen Klick-Callback. Diese Tests laufen mit einem
TestCarContext und **beweisen weder Launcher-Sichtbarkeit noch eine DHU-Verbindung**.
Bei mehreren angeschlossenen Geräten vorher das gewünschte Gerät per `ANDROID_SERIAL`
wählen oder übrige Testgeräte trennen.

## 7. Bisherige Assistant-App

`:app:assembleDebug` erzeugt `app\build\outputs\apk\debug\app-debug.apk`.
Dieses Modul trägt weiterhin das ursprüngliche Paket `dev.openautoai.app`.
Es wurde für den DHU-Sichtbarkeitstest nicht mit dem Car-Service verbunden.
Lokale Speicherung, Keystore und Fehlerfälle sind mit isolierten Testdaten geprüft;
Details in [LOCAL_STORAGE.md](LOCAL_STORAGE.md). Anleitung und bestätigter Stand
der neuen Sprachfunktion stehen in [VOICE.md](VOICE.md).

[Deutsch/Englisch und Wiederaufnahme](LOCALIZATION.md).

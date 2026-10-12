# Android Auto: Azentrius 0.1.14

Der DHU-Prototyp fordert jetzt das freigegebene Azentrius-Farbpaar für die
Start-Schaltfläche an. Schrift, Form, Abstände und tatsächliche Farbwahl zeichnet
weiterhin der Android-Auto-Host. Der Stand ist ein lokales Debug-Experiment;
„Starten“ erhöht ausschließlich den UI-Testzähler.

## Quelle und Umfang

Grundlage der freigegebenen Umsetzung und Abnahme 0.1.14 ist [Azentrius/design-system, Commit eb827c8](https://github.com/Azentrius/design-system/blob/eb827c82f5a19f91fb673cac68215e5d8c2824e4/android-auto.md).
Eine unveränderte Kopie liegt in `design/azentrius/upstream-eb827c8/`.

Die aktuelle Design-Referenz ist inzwischen [Commit 9ed4032](../design/azentrius/upstream-9ed4032/android-auto.md).
Sie präzisiert Host-Farben/Hintergrund anhand dieser Abnahme und enthält die
O1-Entscheidung zum neuen App-Symbol aus `007f587`. B2/B3 bleiben unverändert;
an 0.1.14 ist nichts nachzuziehen. Die neuen Symbol-Dateien sind als Referenz
vorhanden, aber noch nicht in die App eingebunden. Herkunft und historische
Kopien: [design/azentrius/README.md](../design/azentrius/README.md).

| Vorgabe | Umsetzung |
|---|---|
| B1 | Vorhandenes MessageTemplate, Titel, APP_ICON, eine Nachricht und genau eine Aktion bleiben erhalten. |
| B2 | Nur im DHU-Flavor: Metadatum `androidx.car.app.theme`, CarAppTheme mit Primary `#C8A96E` und PrimaryDark `#7D5F22`. Kein Sekundärpaar und keine Tag-/Nacht-Overrides. |
| B3 | Start-Aktion mit `CarColor.PRIMARY`; keine Textfarbe, kein Icon und kein FLAG_PRIMARY. |
| B4 | Alle vorhandenen deutschen/englischen Texte, Reihenfolge und vollständigen Modellnamen bleiben erhalten. |
| B5 | Keine weiteren Smartphone-Tokens, Schriften oder Icons werden an den Host übergeben. |

Das bestehende türkis-blaue App-Symbol bleibt. Es gibt keine neue Aufnahmefunktion,
keinen Gesprächszustand im Auto und keine Änderung an Kategorie oder HostValidator.
`dhuRelease` bleibt deaktiviert. Eine Zulassung oder offiziell unterstützte
Kategorie für allgemeine KI-Gespräche wird nicht behauptet.

Nur die DHU-Variante steigt auf **0.1.14-prototype / Code 5**. Die separate
Assistenten-App bleibt **0.1.13-dev / Code 14**, die Phone-Vergleichsvariante
**0.1.12-prototype / Code 4**.

## Build und Installation nach der Ausgangsmessung

Vor der Installation die bisherige DHU-Version nach dem Testbericht messen:
A3/A4 bei Tag und Nacht und A7 mit sechs Starts. Andernfalls fehlt der Vergleich.
Die hier gesicherte Ausgangs-APK ist 0.1.12-prototype / Code 4.

```powershell
Set-Location -LiteralPath 'C:\Users\stefa\Documents\Projekte\Azentrius\OpenAuto AI'
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
.\gradlew.bat :prototype-app:assembleDhuDebug :prototype-app:assembleDhuDebugAndroidTest :prototype-app:lintDhuDebug
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb -s 56161FDCH000H8 install -r '.\prototype-app\build\outputs\apk\dhu\debug\prototype-app-dhu-debug.apk'
& $adb -s 56161FDCH000H8 shell am start -n 'dev.openautoai.prototype.dhu/dev.openautoai.prototype.MainActivity'
```

Für den Download den APK-Pfad durch den vollständigen Pfad zu
`OpenAutoAI-0.1.14-dhu-debug.apk` ersetzen. Erwartet: `Success` und Version
0.1.14-prototype / Code 5. Nicht deinstallieren oder App-Daten löschen.

## DHU-Prüfung

Die bereits eingerichtete Verbindung und den Head-Unit-Server verwenden.
Die vollständige Einrichtung steht in [START_WINDOWS.md](START_WINDOWS.md).
Unter `tools/dhu/` liegen acht Profile: vier Formate, jeweils Touch und Rotary.
Die DHU-Konsole bleibt für Befehle offen.

```powershell
$dhu = "$env:LOCALAPPDATA\Android\Sdk\extras\google\auto\desktop-head-unit.exe"
& $adb -s 56161FDCH000H8 forward tcp:5277 tcp:5277
& $dhu -c '.\tools\dhu\800x480-touch.ini' -i touch
```

In der DHU-Konsole `day`, `night` und `screenshot DATEINAME.png` verwenden.
Eine Sitzung regulär mit `quit` schließen. Für Rotary das entsprechende Profil
mit `-i rotary` starten; mit Pfeiltasten bzw. `dpad click` bedienen.
Keine Vorlage zurücksetzen oder Restriktion abschalten, um den Sechs-Start-Test
zu bestehen. Screenshots erst innerhalb von OpenAuto AI aufnehmen, ohne fremde
Apps, Anrufdaten oder Kartenpositionen in veröffentlichte Belege zu übernehmen.

Versionen und App-Log erfassen:

```powershell
& $dhu --version
& $adb -s 56161FDCH000H8 shell dumpsys package com.google.android.projection.gearhead | Select-String 'versionCode|versionName'
& $adb -s 56161FDCH000H8 logcat -d -s OpenAutoAI:I '*:S'
```

`car.onCreateScreen api=…` ist die ausgehandelte **Car-API-Stufe des Hosts**;
`minCarApiLevel=1`, Android API und Bibliotheksversion ersetzen diese Messung nicht.
`car.start count=n` muss zum sichtbaren Zähler passen.

## Abnahme und Entscheidungen

Die Matrix umfasst 800×480, 1280×720, 1920×1080 und 1280×720 mit 220 Pixeln
Höhenrand, jeweils Deutsch/Englisch, Tag/Nacht, OpenAI/GPT-5.4 und
Claude/Sonnet 4.6 sowie Touch/Rotary: 64 Kombinationen.
Die Kriterien A1–A11 und ihr tatsächlicher Status stehen im [Testbericht](BUILD_AND_TEST.md).
Englischer Fallback wird zusätzlich mit einer nicht unterstützten Sprache geprüft.

- **A3/A4:** Auch die bisherige Host-Standardfarbe messen. Für beide Versionen
  tatsächliche Flächen-/Textpixel, Koordinaten, Screenshot und Kontrast notieren.
  Volle Schriftstriche statt geglätteter Randpixel verwenden; mindestens 4,5:1.
  Tag und Nacht getrennt erfassen. Der gesetzte Farbwert beweist keine Host-Darstellung.
- **A7:** In derselben 800×480-Sitzung sechs Starts, ohne Reset. Scheitert bereits
  die Ausgangsversion, ist das nach Nutzerfreigabe ein bekannter, für 0.1.14
  nicht blockierender Fehler. Ein neu auftretendes Scheitern ist eine Regression.
  Ein noch nicht ausgeführter Vergleich ist weder bestanden noch ein bekannter Fehler.
- **O2:** Bei unzureichendem Kontrast ist der freigegebene Rückfall
  `CarColor.DEFAULT` mit erneuter Abnahme vorgesehen. Nicht vorsorglich anwenden.
- **O3:** Kürzt der Host Anbieter/Modell, Abweichung melden und eine Entscheidung
  zu Anzeigenamen einholen. Keine selbst erfundenen Kürzungen oder Designwerte.

Anruf- und Headset-/Bluetooth-Tests bleiben auf Wunsch zurückgestellt.

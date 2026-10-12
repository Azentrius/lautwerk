# OpenAuto AI per Sideload auf dem Fahrzeugdisplay

Stand 13.10.2026. Ziel ist eine über GitHub verteilbare APK, die Nutzer auf dem
Telefon installieren und im Auto öffnen können. Dafür prüfen wir zuerst den
unveränderten UI-Prototyp 0.1.15 im stehenden Fahrzeug. Ein bestätigter Weg für
OpenAuto AI auf realen Head Units liegt noch nicht vor.

**Aktiver Versuch: ID.7 mit nativer drahtloser Verbindung, ohne AAWireless.**
Die Kia-/Adapter-Versuche sind auf Nutzerwunsch zurückgestellt. Nach dem negativen
Ausgangsbefund folgt jetzt die unveränderte APK mit geänderter Installationsherkunft.
Die Adapterhinweise bleiben als spätere Option dokumentiert und sind keine
Anleitung für die native ID.7-Verbindung.

Die Test-App `dev.openautoai.prototype.dhu` zeigt Titel, Anbieter, Modell und
einen Starten-Testzähler. Die eigentlichen KI-Gespräche laufen derzeit nur in
`dev.openautoai.app`. Ein erfolgreicher Sichtbarkeitstest wäre deshalb der erste
Schritt zur Auto-App, noch kein funktionierendes KI-Gespräch im Auto.

## Welche Wege wir prüfen

| Weg | Einordnung für OpenAuto AI |
| --- | --- |
| Normale APK-Installation und native Android-Auto-Verbindung | Ausgangstest im ID.7. Installation allein beweist keine Launcher-Sichtbarkeit. |
| Normale APK-Installation und AAWireless | Ausgangstest im Kia mit protokollierten bisherigen Einstellungen. |
| AAWireless mit eingebautem Entwicklermodus | Konkreter experimenteller Kandidat. Andere Projekte nennen diesen Weg; Übertragbarkeit auf unsere Car App Library und Android 17 ist offen. |
| AAAD | Der Hersteller des Tools nennt die Kompatibilität ab Android 14 selbst unsicher. Kein Nachweis für unser APK; derzeit nicht Bestandteil des Tests. |
| Systeminstaller mit geänderter Herkunft (Prinzip von KingInstaller) | Auf unserem Pixel 10 Pro / Android 17 ist OpenAuto AI danach im Smartphone-Launcher auswählbar; DHU erneut bestanden. APK bytegleich. ID.7 noch offen; [genauer Versuch](SIDELOAD_INSTALL_EXPERIMENT.md). |
| Interne Play-Verteilung | Vergleichsoption, aber kein von Google Play unabhängiger Sideload-Weg. |

Googles Option „Unbekannte Quellen“ in Android Auto gilt ausdrücklich nicht als
Freischaltung für Car-App-Library-Apps. Der Entwicklermodus des AAWireless-Adapters
ist eine andere Einstellung. Ein entsprechender Versuch ist ein inoffizieller
Kompatibilitätstest, keine von Google zugesicherte Funktion. Ein positives
Kia-Ergebnis lässt sich nicht auf den nativ drahtlos verbundenen ID.7 übertragen.

Die POI-Deklaration bleibt ein offengelegter Debug-Testplatzhalter. Sie ist keine
behauptete KI-Kategorie. App, HostValidator, Templates und App-Berechtigungen werden für
diesen Vergleich nicht verändert. Root, fremde Patches und
Fahrfreigaben sind nicht Teil dieser Testreihe. Anruf-, Headset- und
Bluetooth-Audio-Unterbrechungstests bleiben zurückgestellt.

Nach der gemessenen Ablehnung in der Launcher-Auswahl wurde ein eng begrenzter
Herkunftsversuch separat freigegeben: Die identische APK wird über den vorhandenen
Android-Systeminstaller mit Google Play als behaupteter Installationsherkunft
aktualisiert. Das ist ausdrücklich Installer-Spoofing. Der aktuelle Messstand
steht im [Testbericht](SIDELOAD_TEST_REPORT.md); daraus folgt noch keine Empfehlung
für Nutzer. Der Versuch installiert weder KingInstaller noch Shizuku und ändert
weder Android Auto noch den Car-App-Code.

## Testpaket und Installation

**Aktueller Herkunftsversuch:** Die folgende normale ADB-Installation beschreibt
den Ausgangsweg. Sie nicht erneut ausführen, wenn die geänderte Herkunft für den
ID.7-Test erhalten bleiben soll. Dafür gilt der [gesonderte Versuchsablauf](SIDELOAD_INSTALL_EXPERIMENT.md).

Für die ersten Fahrzeugtests verwenden wir exakt den bereits bestätigten
`prototype-app:dhuDebug`-Build aus 0.1.15. Wer selbst baut, folgt
[Bauen und DHU einrichten](START_WINDOWS.md). Versionsstand, SHA-256 und Ergebnisse
stehen im [Fahrzeugtestbericht](SIDELOAD_TEST_REPORT.md).

PowerShell im Projektordner, Android SDK installiert und Telefon per USB verbunden:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
$serial = 'SERIENNUMMER_AUS_ADB_DEVICES'
$apk = Join-Path (Get-Location) 'prototype-app\build\outputs\apk\dhu\debug\prototype-app-dhu-debug.apk'
Get-FileHash -Algorithm SHA256 -LiteralPath $apk
& $adb -s $serial install -r $apk
& $adb -s $serial shell cmd package query-services --brief -a androidx.car.app.CarAppService -p dev.openautoai.prototype.dhu
```

Erwartet werden `Success` und `dev.openautoai.car.OpenAutoCarService`. Ist dieselbe
Testversion bereits installiert, entfällt die Neuinstallation. Bei abweichender
Signatur nicht deinstallieren oder Paketdaten löschen: Das wäre kein Update und
könnte Daten verlieren. Für den Vergleich wird keine Play-Installer-ID angegeben.

## Fahrzeugvergleich im Stand

Vor jedem Lauf Uhrzeit und Zeitzone, APK-Version, Android-/Android-Auto-Version,
Head-Unit-Version und Verbindungsart notieren. Beim Kia zusätzlich Adaptermodell,
genaue Firmware, Version der AAWireless-Handy-App und die aktuellen Werte von
Entwicklermodus und Passthrough festhalten. Firmware und Handy-App-Version sind
verschiedene Angaben. Kein Firmware-Update während eines Vergleichspaares.

1. Einen laufenden DHU regulär beenden. Telefon normal mit genau einem Auto verbinden.
2. Im Android-Auto-Launcher nach „OpenAuto AI“ suchen. Falls die App nur unter
   „Launcher anpassen“ angeboten, aber abgewählt ist, dies notieren und sie auswählen.
3. Falls sichtbar: App öffnen, Titel und Anbieter/Modell prüfen. Dreimal „Starten“
   antippen. Erwartet ist ein ansteigender Testzähler. Kein Mikrofon und keine KI-Anfrage.
4. Android Auto regulär trennen und erneut verbinden. Öffnen und Starten wiederholen.
   Der nicht persistierte Testzähler darf nach einer neuen Car-Session neu beginnen.
5. Ergebnis getrennt erfassen: Launcher vorhanden, Oberfläche geöffnet, Starten
   reagiert, erneute Verbindung funktioniert. Fehlermeldungen wörtlich notieren.

### Kia und AAWireless als zurückgestellte Option

Zuerst mit unveränderten Einstellungen testen und deren Werte erfassen. Das ist
nur dann ein Vergleich ohne Entwicklermodus, wenn dieser tatsächlich aus war.

Für den zweiten Lauf in der verbundenen AAWireless-App nach der vorhandenen Option
„Developer mode/Entwicklermodus“ sehen. Originalwerte vorher aufschreiben. Wenn die
App für diese Option verlangt, Passthrough auszuschalten, diese Abhängigkeit ebenfalls
protokollieren. Es wird ausschließlich die vorhandene Adapteroption verwendet;
keine Firmware ersetzt und keine Host-Identität manuell bearbeitet. Fehlt die Option,
den Lauf als nicht durchführbar erfassen. Die Menüposition hängt von App/Firmware ab.

Nach der Änderung regulär neu verbinden und dieselben fünf Schritte wiederholen.
Anschließend ursprüngliche Werte wiederherstellen und den Ausgangstest wiederholen.
Erst dieses Vergleichspaar kann einen Zusammenhang mit der Einstellung belegen.
Auch ein Erfolg belegt nur diese Geräte-/Versionskombination.

### ID.7 mit nativer drahtloser Verbindung

Ohne AAWireless direkt mit dem ID.7 verbinden und dieselben Schritte durchführen.
Es gibt dabei keine Adapteroption, die aus dem Kia übernommen werden könnte.
Bleibt die App unsichtbar, wird das als negatives Ergebnis dieser Kombination
erfasst, nicht als Anlass für Datenlöschung oder wiederholte Neuinstallation.

Ein zusätzlicher Adapter wäre ein eigener Testweg und setzt einen geeigneten
USB-Anschluss mit kabelgebundenem Android Auto voraus. Das muss am konkreten
Fahrzeug geprüft werden; native Funkunterstützung belegt es nicht.

## Technische Belege

Am PC können vor und nach einem Lauf Paketversionen und Service-Registrierung
wie oben ausgelesen werden. Für Car-Logs Start und Ende des Laufs notieren;
vorhandene DHU-Zeilen dürfen nicht als Fahrzeugbeleg zählen.

```powershell
& $adb -s $serial shell dumpsys package com.google.android.projection.gearhead |
    Select-String '^\s*version(Name|Code)=' | Select-Object -First 2
& $adb -s $serial logcat -d -v time 'OpenAutoAI:I' '*:S' |
    Select-String 'car\.(createHostValidator|onCreateSession|onCreateScreen|onGetTemplate|start)'
```

`car.onCreateScreen api=…` liefert die ausgehandelte Car-API-Stufe. Fehlt die Zeile
für den tatsächlichen Fahrzeuglauf, lautet der Wert „nicht gemessen“. Version 8
aus dem früheren DHU-Test ist kein Ersatz. Alte, bereits aus dem Ringpuffer entfernte
Logs lassen sich nicht nachträglich wiederherstellen.

Für eine während der USB-Trennung weiterlaufende, begrenzte Aufnahme gibt es die
[auf dem Pixel geprüfte Diagnosehilfe](../tools/sideload/README.md).

Ein fehlender Dienstaufruf beweist allein keine bestimmte Ablehnungsursache.
Keine vollständigen Bugreports, privaten App-Daten, Seriennummern oder
Benachrichtigungsinhalte auf GitHub hochladen. Für den Bericht genügen Versionen,
Beobachtungen und zum Lauf gehörende technische Car-Ereignisse.

## Wann daraus eine Anleitung für Nutzer wird

Ein funktionierender Weg muss mit derselben APK in mehreren frischen Verbindungen
und nach Telefonneustart bestätigt werden. Anschließend Installation auf einem
weiteren Telefon ohne alte DHU-Sitzung prüfen. Erst danach gilt die entsprechende
Kombination in der Kompatibilitätstabelle als unterstützt; andere bleiben offen.

Für eine Verteilung an Nutzer folgen ein eigener dauerhaft gesicherter
Release-Signierschlüssel, signierte APKs in GitHub Releases mit SHA-256 und
Zertifikatsfingerabdruck sowie ein getestetes Update ohne Datenverlust. Die
Installation über den Browser muss getrennt von ADB geprüft werden. Noch ist
weder eine solche Release-APK noch deren Funktion im Auto freigegeben.

Erst nach bestätigtem Zugriff auf die Auto-Oberfläche wird die vorhandene
Gesprächssteuerung angebunden. Die daraus entstehenden Audio- und
Lebenszyklusfragen sind ein eigener Entwicklungsschritt.

## Quellen

- [Google: reale Fahrzeuge und Grenze unbekannter Quellen](https://developer.android.com/training/cars/testing#test-in-real-vehicles)
- [Google: APK-Verteilung außerhalb von Google Play](https://developer.android.com/distribute/marketing-tools/alternative-distribution)
- [Fermata-Entwickler: AAWireless-Entwicklermodus unter Android 16](https://github.com/AndreyPavlenko/Fermata/discussions/756) – Aussage zu Fermata, kein Test von OpenAuto AI oder Android 17.
- [AAAD: bekannte Einschränkungen ab Android 14](https://github.com/shmykelsa/AAAD)
- [KingInstaller: beschriebene Installationsherkunft](https://github.com/fcaronte/KingInstaller)
- [Vela: aktuelle Installationsberichte, insbesondere das Update vom 21.09.2026](https://github.com/PimpinPumpkin/Vela/issues/179) – Entwicklerbericht eines Erfolgs auf einem Stock-Pixel 9; kein Test unserer App oder unseres Fahrzeugs. Die ältere Schlussfolgerung desselben Issues, es könne grundsätzlich nie funktionieren, wurde durch die späteren Berichte überholt.

Die Quellen beschreiben mögliche Wege und Grenzen. Maßgeblich für unsere
Kompatibilitätsaussagen sind die getrennt protokollierten Ergebnisse im Testbericht.

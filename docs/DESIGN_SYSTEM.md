# Azentrius in OpenAuto AI 0.1.13

Die bestehende Smartphone-App verwendet das freigegebene Azentrius-Design:
systemabhängig hell/dunkel, DM Sans, die gelieferten PatternFly-Icons, feste
Farb-Tokens und Bausteine ohne Ripple. Dynamische Material-Farben sind aus.
Die DHU-Oberfläche bleibt in ihren bisherigen Android-Auto-Vorlagen.

## Aktuelle Referenz und Quellen der Umsetzung

Aktuelle Design-Referenz: [170dd38](https://github.com/Azentrius/design-system/tree/170dd38be84a9f107440a730eb29361087fc63ea)
im privaten Repository Azentrius/design-system.
Sie enthält die DHU-Präzisierungen und das neue App-Symbol aus `6a7a951`.
Der ursprüngliche Import war ein reines Quellen-Update. In 0.1.15 wurden Symbol,
Startbild und Dienstsymbol eingebunden; siehe [APP_IDENTITY.md](APP_IDENTITY.md).
Das bestätigte Seiten-Design bleibt erhalten.

Die folgenden historischen Quellen dokumentieren die Umsetzung von 0.1.13:

- Ursprüngliches Paket (ZIP), unverändert importiert; die Kopie ist inzwischen aus
  diesem Repository entfernt. ZIP-SHA256: `fdf2805c9690584e6ea12b36032972bb021285070758245c666ac42b873d984f`.
- Ergänzung: [Design-System 7601a14](https://github.com/Azentrius/design-system/tree/7601a14763258e8d0b7929efd4a3689e10d31cab).
  Die dortige Android-Regel hat Vorrang vor dem Android-Abschnitt des ZIP.
- Farbwerte und Android-Assets stimmen mit beiden Quellen überein. Vier TTFs,
  die XML-Schriftfamilie und 30 Vector-Drawables sind original übernommen.
  DM Serif Display ist mitgeliefert, aber auf diesen Seiten nicht eingesetzt.
- OFL- und MIT-Lizenzen liegen in der App und sind unter Einstellungen →
  Lizenzen ohne Netzwerkzugriff lesbar.

Es wurden keine neuen Farben, Icons oder Größen für das Design erfunden.
Die zunächst fehlende Regel für schmale Reiter wurde vom Nutzer im Design-System
ergänzt und erst danach umgesetzt. Es sind keine weiteren Vorgaben offen.

## Darstellung und bestehendes Verhalten

`design/AzentriusTheme.kt`, `AzPalette.kt`, `AzComponents.kt` und `AzScreens.kt`
kapseln die Darstellung. Schriftgrößen bleiben in sp, Abstände in dp.
Die drei Reiter messen ihre längste Beschriftung bei aktueller Schriftgröße:
nebeneinander, darunter oder waagerecht verschiebbar. Alle wechseln gemeinsam;
bei verschiebbaren Reitern wird die Auswahl ohne Animation ganz sichtbar gerückt.
Beschriftungen bleiben einzeilig, ungetrennt und vollständig. Die allgemeine
Typografie deaktiviert Silbentrennung. Aktionen stapeln sich nach Platzbedarf.

Gespräch, Einstellungen, Verlauf und die eigenständige Seite Stimme und Tempo
behalten ihre Funktionen. Nutzertexte erscheinen als rechte Blase, Antworten
in voller Breite. Verlaufseinträge zeigen die letzte Änderung, Anbieter und
Modell; vorhandene Nachrichtentreffer bleiben unverändert. Es gibt keine neuen
Ausschnitte, Markierungen, Dauerangaben oder vorläufige Spracherkennung.

Die untere Leiste zeigt Gesprächsphase und Export getrennt. Ihre Darstellung
verwendet typisierte Zustände aus der vorhandenen Steuerung. Starten, Fortsetzen,
Beenden sowie Start-/Exportabbruch behalten ihre bisherigen Voraussetzungen.
Export-Ergebnisse bleiben bis zur nächsten Vorbereitung bzw. zum nächsten
Gesprächsstart sichtbar. Es gibt keinen zusätzlichen Speicherbutton und keinen
Abbruch während des Dateischreibens.

Die Stimmenliste verwendet Optionsfelder. Jede ausdrückliche Online-Auswahl
öffnet die bestehende Bestätigung; Abbrechen ändert die Auswahl nicht. Es gibt
keinen gespeicherten Zustimmungsstatus. Android-Standard bleibt ungekennzeichnet.
Die beiden Hörprobe-Buttons bleiben sichtbar und spielen nur die gewählte Stimme.
Anbieter, Modellkatalog, Speicherung, API-Aufrufe und Audioablauf bleiben erhalten.

## Update auf diesem Windows-PC

Projekt: `C:\Users\stefa\Documents\Projekte\Azentrius\OpenAuto AI`.

```powershell
Set-Location -LiteralPath 'C:\Users\stefa\Documents\Projekte\Azentrius\OpenAuto AI'
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
.\gradlew.bat :app:assembleDebug
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
& $adb -s 56161FDCH000H8 install -r '.\app\build\outputs\apk\debug\app-debug.apk'
& $adb -s 56161FDCH000H8 shell am start -n 'dev.openautoai.app/.MainActivity'
```

Für den Download denselben Installationsbefehl mit dem vollständigen Pfad zu
`OpenAutoAI-0.1.13-assistant-debug.apk` verwenden. Erwartet: `Success`;
Version `0.1.13-dev`, Code 14. Das Update verwendet dieselbe Debug-Signatur und
erhält die App-Daten. Nicht deinstallieren, wenn Android eine andere Signatur meldet.

Die DHU-/Phone-Variante bleibt `0.1.12-prototype`, Code 4. Ihre exakten Installations-
und DHU-Verbindungsschritte stehen weiter in [START_WINDOWS.md](START_WINDOWS.md).
Der Smartphone-Assistent wird durch dieses Update nicht zu einer zugelassenen
Android-Auto-Kategorie. Die experimentelle Registrierung und Hostprüfung ändern sich nicht.

## Sichtprüfung durch den Nutzer

Gespräch, Einstellungen, Verlauf und Stimme und Tempo in hell/dunkel ansehen.
Bei großer Systemschrift bleiben die Reiter vollständig und lassen sich verschieben.
Ein bestehender Chat lässt sich öffnen; Export- und Online-Stimmen-Dialog können
abgebrochen werden. Die bisherigen Deutsch-/Englisch-Einstellungen bleiben erhalten.
Für eine reine Designprüfung sind keine API-Anfragen oder Hörproben nötig.

Automatisierte und visuelle Ergebnisse: [BUILD_AND_TEST.md](BUILD_AND_TEST.md).
Anruf- und Headset-/Bluetooth-Praxistests bleiben ausdrücklich zurückgestellt.

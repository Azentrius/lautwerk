# Build- und Testbericht 0.1.8

Stand: 12.10.2026. **90 automatisierte Prüfungen bestanden:** 69 JVM-Tests und
21 Gerätetests (14 Assistant, 7 DHU/Auswahl). Assistant **0.1.8-dev / Code 9**
ist als Update auf dem Pixel 10 Pro / Android 17 installiert. Keine Datenlöschung.
DHU-/Phone-Oberfläche bleibt **0.1.2-prototype / Code 3**.

## Änderung und Prüfung

Beenden reicht eine Abbruchquelle pro Auftrag vom Android-Worker bis zum einzelnen
OkHttp-Aufruf durch. Der Transport verwendet OkHttp 5.3.2 statt HttpURLConnection.
Automatische Wiederholungen, Redirects und Request-Wiederverwendung sind deaktiviert;
Endpoint-Allowlist, UTF-8, Antwortgrößenlimit und Fehlertexte bleiben erhalten.
Stimme, Modellkatalog, API-Protokolle, Archivformat und Schlüsselkennungen bleiben gleich.

Der Sprachdienst beobachtet Display-/Sperrzustand, Android-Audiomodus und verbundene
externe Ausgabegeräte. Ein erkanntes Ereignis beendet die Sitzung; es gibt keine
automatische Wiederaufnahme. Alte Beobachter werden entfernt, ihre nachlaufenden
Ereignisse wirken nicht auf einen Neustart. Kein eigener Fokus während der Erkennung.

| Prüfung | Ergebnis |
|---|---|
| `:core:test` | 5 bestanden, darunter 100 parallele Registrierungs-/Abbruchrennen in einem Test |
| `:providers:test` | 20 bestanden, davon 8 Transporttests mit lokalen Sockets/erfundenen Zugangsdaten |
| `:voice:test` | 44 bestanden: beide Anbieter, alle Gesprächsphasen, verspätete Rückmeldungen, Unterbrechungen, Fokus und Archivierung |
| Assistant-Gerätetests | 14 bestanden auf Pixel 10 Pro / Android 17 |
| DHU-/Auswahl-Gerätetests | 7 bestanden; Titel, Starten, Auswahl und Speicherung |
| Assistant-/DHU-Debug- und Test-APKs | erfolgreich gebaut und als Updates verwendet |
| `:app:lintDebug` | 0 Fehler, 9 bestehende Warnungen (Versionen, Backup-Metadaten, ungenutzter String, App-Icon, 2 KTX-Vorschläge) |

Die Socket-Tests brechen sowohl das Warten auf Antwort-Header als auch den Empfang
eines teilweise gelieferten Bodys ohne `Thread.interrupt()` ab. Die Futures enden
innerhalb der Testgrenzen (3 s bzw. 1 s); danach gelingt eine frische Anfrage.
Vorab abgebrochene Arbeit öffnet keine Verbindung. Auch `503 / Retry-After: 0`
wird nicht wiederholt. Die Tests verbinden sich nur zu einem lokalen Testserver;
die Produktions-Allowlist wird für Tests nicht erweitert.

Neue Gerätetests prüfen echte Android-Beobachter auf Hauptthread/Freigabe, den
Abbruch eines blockierten Workers mit anschließendem Neustart sowie die Initialisierung
des Produktions-Netzwerkbausteins ohne Schlüsselzugriff oder Netzwerkaufruf.
Bestehende Stimmwahl-, TTS-Konfigurations- und Oberflächentests bestehen weiterhin.
Keine automatischen Mikrofonaufnahmen, hörbaren Hörproben oder echten Anbieteranfragen.
Unveränderte Speichermodul-Tests wurden nicht wiederholt.

## Grenzen und manueller Stand

Der Nutzer hat den Sprachablauf für OpenAI und Claude in 0.1.6 bestätigt; die
Stimmwahl von 0.1.7 klingt laut Rückmeldung deutlich besser. Für 0.1.8 bestätigt
der Nutzer am 12.10.2026 auf dem Pixel für **beide Anbieter**: „Gespräch stoppen
und neu starten per Button und Display aus klappt bei beiden Anbietern.“
Damit sind Stoppen/Neustarten über die Schaltfläche und Beenden bei ausgeschaltetem
Display praktisch bestätigt. Die Rückmeldung enthält keine separate Bestätigung
für Anrufe oder Headset-/Bluetooth-Wechsel. Diese Tests sind auf Wunsch des Nutzers
am 12.10.2026 **zurückgestellt und weiterhin ungeprüft**. Ebenso ist damit nicht jede
denkbare zeitliche Überschneidung oder Gesprächsphase manuell nachgewiesen.

Anrufe werden über den gemeldeten Android-Audiomodus erkannt; fremde VoIP-Apps
ohne passendes Signal sind nicht garantiert abgedeckt. Überwacht werden Anschluss
und Trennung von Audio-Ausgabegeräten, nicht jeder interne Routingwechsel.
Für Android 10/11 dient die Zustandsprüfung alle 500 ms als Fallback. Diese älteren
Android-Versionen wurden nicht auf einem Gerät getestet. DNS-Auflösung und bereits
laufende Serververarbeitung/Abrechnung sind nicht garantiert sofort abbrechbar;
ein begonnener atomarer Archivschreibvorgang kann noch abschließen.

`prototype-app`, `car-ui`, `storage` und `settings` haben keine Quelländerungen.
`core` ergänzt nur den Abbruchvertrag im KI-Bereich; Katalog und DHU-Verhalten
bleiben gleich. Keine zusätzlichen DHU-Rechte, Kategoriebehauptung oder Host-Ausnahme.
Manuelle DHU-Sichtbarkeit/Starten ist seit 0.1.2 vom Nutzer bestätigt; dieser Lauf
prüft die UI automatisiert. Auto-Audio und reale Fahrzeugnutzung bleiben offen.

## Wiederholen

JDK 17/SDK gemäß [START_WINDOWS.md](../START_WINDOWS.md) setzen; DHU für UI-Tests
regulär beenden und Pixel entsperrt lassen.

```powershell
.\gradlew.bat :core:test :providers:test :voice:test
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
.\gradlew.bat :prototype-app:assembleDhuDebug :prototype-app:assembleDhuDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :prototype-app:connectedDhuDebugAndroidTest
```

Die gebauten Testpakete wurden mit `adb shell am instrument -w` ausgeführt:
`OK (14 tests)` und `OK (7 tests)`. Installation und konkrete manuelle Prüfschritte:
[VOICE.md](../VOICE.md). Historische Berichte: [0.1.7](BUILD_AND_TEST-0.1.7.md),
[0.1.6](BUILD_AND_TEST-0.1.6.md), [0.1.5](BUILD_AND_TEST-0.1.5.md).

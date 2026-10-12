# Build- und Testbericht 0.1.14

Stand: 12.10.2026. Umsetzung nach Design-System `eb827c8` und Nutzerfreigabe
einschließlich Ausgangsmessung A3/A4, Versionsnachweisen und A7-Regressionsregel.
Details: [ANDROID_AUTO_DESIGN.md](ANDROID_AUTO_DESIGN.md).

**Status: gebaut; Installation und DHU-Abnahme stehen noch aus.**
Beim Aufbau der Verbindung zeigte der DHU ein laufendes Telefonat. Die Verbindung
wurde regulär beendet. Das Pixel wurde für die weiteren Gerätetests angefragt;
bis zur Freigabe bleibt die Ausgangsversion auf dem Gerät installiert.
Es wurde kein Anruf- oder Headset-Test durchgeführt.

## Umgebung und Versionen

| Angabe | Tatsächlich erfasster Wert |
|---|---|
| Telefon | Pixel 10 Pro, Android 17 |
| Android Auto, aktive Version | 17.7.663654-release / Code 177663654 |
| Zusätzlich gelistetes System-Stub-Paket | 1.2.562600-stub / Code 12562600; nicht die aktive Testversion |
| DHU | 2.0-windows, Build 2022-03-30-438482292 (`--version`) |
| Car App Library | 1.8.0-rc01 |
| Host-Car-API | **Noch nicht erfasst.** `car.onCreateScreen api=…` bei realer App-Sitzung nachtragen. |
| Deklarierte Mindest-Car-API | 1, unverändert; kein Nachweis der Host-API |
| Ausgangsversion, installiert | DHU 0.1.12-prototype / Code 4 |
| Neuer Build, noch nicht installiert | DHU 0.1.14-prototype / Code 5 |
| Separate Smartphone-App | 0.1.13-dev / Code 14, unverändert |

## Automatisierte und statische Prüfungen

| Prüfung | Ergebnis |
|---|---|
| DHU-Debug-APK | Build erfolgreich; Paketversion mit aapt2 bestätigt |
| DHU-Test-APK | Erfolgreich kompiliert; 11 Gerätetests vorbereitet, noch nicht ausgeführt |
| Phone-Debug-Vergleichsvariante | Build erfolgreich, unverändert/up-to-date |
| DHU-Lint | 0 Fehler, 6 Warnungen |
| Core-JVM | 22 bestanden aus unverändertem, lokal up-to-date-Teststand |
| Gepacktes CarAppTheme | Genau zwei Einträge: #C8A96E und #7D5F22; kein Sekundärpaar |
| Smartphone/Voice/Anbieter-Code | Keine Änderungen; kein neuer End-to-End-Test behauptet |

Die drei zusätzlichen Gerätetests prüfen das tatsächlich gepackte Theme in
beiden Nachtmodus-Konfigurationen, unveränderte Template-Struktur mit genau einer
einfachen Primärfarb-Aktion und den englischen Fallback für Französisch.
Die acht vorhandenen Tests zu Auswahl, Starten, Lebenszyklus und englischen Texten
bleiben enthalten. Modelltests beweisen weder Host-Farbe noch Zeilenkürzung oder
das Vorlagenlimit eines echten Hosts.

Die sechs Lint-Warnungen betreffen drei neuere Testbibliotheksversionen,
den absichtlich exportierten Car-App-Service (HostValidator bleibt aktiv),
die bestehende Backup-Deklaration und Overdraw im unveränderten Phone-Layout.

## A3/A4: verpflichtender Vorher-/Nachher-Vergleich

| Build | Modus | Fläche | Text | Kontrast | Screenshot | Ergebnis |
|---|---|---|---|---|---|---|
| Ausgangsversion, Host-Standardfarbe | Tag | offen | offen | nicht gemessen | offen | ausstehend |
| Ausgangsversion, Host-Standardfarbe | Nacht | offen | offen | nicht gemessen | offen | ausstehend |
| 0.1.14 | Tag | offen | offen | nicht gemessen | offen | ausstehend |
| 0.1.14 | Nacht | offen | offen | nicht gemessen | offen | ausstehend |

Keine Werte aus der Spezifikation werden als tatsächliche Host-Messung ausgegeben.
Bei späteren Messungen Pixelkoordinaten und Dateinamen mit erfassen.

## DHU-Abnahme A1–A11

Für die vollständige Matrix gelten vier Formate × zwei Sprachen × Tag/Nacht ×
zwei Auswahlen × Touch/Rotary = 64 Kombinationen. Bisher keine dieser Kombinationen
vollständig abgenommen. Profile sind unter `tools/dhu/` vorbereitet.

| Kriterium | Ergebnis |
|---|---|
| A1 Kopf und App-Symbol | ausstehend |
| A2 Start-Schaltfläche vollständig | ausstehend |
| A3 tatsächliche Akzentfarbe | Ausgangsversion und 0.1.14 ausstehend |
| A4 gemessener Kontrast mindestens 4,5:1 | Ausgangsversion und 0.1.14 ausstehend |
| A5 vollständiger Status/Anbieter/Modell | ausstehend; keine Kürzungen eingeführt |
| A6 Touch/Rotary und passender Log-Zähler | ausstehend |
| A7 sechs Starts auf 800×480 | Ausgangsversion und 0.1.14 ausstehend; noch keine Fehlerklassifikation |
| A8 erreichbarer, sichtbarer Rotary-Fokus | ausstehend |
| A9 Deutsch/Englisch/Fallback | Modelltest kompiliert, Host-Abnahme ausstehend |
| A10 Auswahl live, Status bleibt | bestehender Modelltest enthalten, Host-Abnahme ausstehend |
| A11 keine falschen Gesprächs-/Zulassungsaussagen | Quellvergleich unauffällig; Host-Sichtprüfung ausstehend |

**A7-Regressionsregel:** Scheitert es schon im Ausgangszustand, gilt es als bekannter
Fehler und blockiert 0.1.14 nicht. Nur neues Scheitern ist eine Regression.
Es gibt keine versteckte Zählerrücksetzung und keine Umgehung der Host-Beschränkung.
Die fehlende Messung wird nicht als bekannter Fehler umgedeutet.

## Zurückgestellt und Historie

Anruf- und Headset-/Bluetooth-Tests bleiben zurückgestellt. Keine KI-Anfrage,
kein Mikrofon und keine Hörprobe wurden für dieses Design-Update gestartet.
Die frühere Smartphone-Abnahme bleibt im [Bericht 0.1.13](history/BUILD_AND_TEST-0.1.13.md) erhalten.

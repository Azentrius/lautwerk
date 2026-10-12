# Build- und Testbericht 0.1.14

Stand: 12.10.2026. Umsetzung nach Design-System `eb827c8` und Nutzerfreigabe
einschließlich Ausgangsmessung A3/A4, Versionsnachweisen und A7-Regressionsregel.
Details: [ANDROID_AUTO_DESIGN.md](../ANDROID_AUTO_DESIGN.md).

**Status: 0.1.14 ist auf dem Pixel installiert; die DHU-Abnahme ist abgeschlossen.**
64 Ansichten aus vier Formaten, zwei Sprachen, Tag/Nacht, zwei Auswahlen und
Touch/Rotary wurden geprüft. Alle elf Prototype-Gerätetests bestehen.
Ausgangsversion und Update bestehen den Sechs-Start-Vergleich A7 ohne Reset.
Der Host ersetzt die angeforderte Farbe durch gut lesbares Hellblau.
Ein französischer Gerätesprachtest bestätigt zusätzlich den englischen Fallback.
Anruf- und Headset-Tests bleiben zurückgestellt.

## Umgebung und Versionen

| Angabe | Tatsächlich erfasster Wert |
|---|---|
| Telefon | Pixel 10 Pro, Android 17 |
| Android Auto, aktive Version | 17.7.663654-release / Code 177663654 |
| Zusätzlich gelistetes System-Stub-Paket | 1.2.562600-stub / Code 12562600; nicht die aktive Testversion |
| DHU | 2.0-windows, Build 2022-03-30-438482292 (`--version`) |
| Car App Library | 1.8.0-rc01 |
| Host-Car-API | **8**, Ausgangsversion: `10:59:01.026 car.onCreateScreen api=8`; 0.1.14: `11:04:37.254 car.onCreateScreen api=8` |
| Deklarierte Mindest-Car-API | 1, unverändert; kein Nachweis der Host-API |
| Ausgangsversion, vor Update getestet | DHU 0.1.12-prototype / Code 4 |
| Neuer Build, per `install -r` installiert | DHU 0.1.14-prototype / Code 5 |
| Separate Smartphone-App | 0.1.13-dev / Code 14, unverändert |

## Automatisierte und statische Prüfungen

| Prüfung | Ergebnis |
|---|---|
| DHU-Debug-APK | Build und Update-Installation erfolgreich; Paketversion mit aapt2 und am Pixel bestätigt |
| DHU-Test-APK | **Alle 11 Tests auf dem Pixel bestanden**, siehe [Geräteprotokoll](../evidence/0.1.14/device-tests.txt) |
| Phone-Debug-Vergleichsvariante | Build erfolgreich, unverändert/up-to-date |
| DHU-Lint | 0 Fehler, 6 Warnungen |
| Core-JVM | 22 bestanden aus unverändertem, lokal up-to-date-Teststand |
| Gepacktes CarAppTheme | Genau zwei Einträge: #C8A96E und #7D5F22; kein Sekundärpaar |
| Smartphone/Voice/Anbieter-Code | Keine Änderungen; kein neuer End-to-End-Test behauptet |
| GitHub-CI | [Build für 7ba155f](https://github.com/Azentrius/openauto-ai/actions/runs/38218928425) erfolgreich: JVM-Prüfungen, Lint und Debug-/Test-Builds |

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
| Ausgangsversion, Host-Standard | Tag | #3B3E46 | #DFE2EA | **8,25:1** | baseline/a3-a4-day.png | bestanden |
| Ausgangsversion, Host-Standard | Nacht | #3B3E46 | #DFE2EA | **8,25:1** | baseline/a3-a4-night.png | bestanden |
| 0.1.14, Host ersetzt durch Hellblau | Tag | #BAD9FC | #0B3453 | **8,84:1** | candidate/a3-a4-day.png | bestanden |
| 0.1.14, Host ersetzt durch Hellblau | Nacht | #BAD9FC | #0D3353 | **8,92:1** | candidate/a3-a4-night.png | bestanden |

Gemessen in den unskalierten 800×480-DHU-Screenshots. Flächenprobe (300,298),
Textprobe vorher (362,289), nachher Tag (367,288), nachher Nacht (408,290).
RGB aus dem dekodierten DHU-Videobild, sRGB-Linearisierung, relative Helligkeit
und `(Lhell + 0,05) / (Ldunkel + 0,05)`. Repräsentative volle Schriftstriche,
keine geglätteten Randpixel. Kleine RGB-Unterschiede können durch die
Videoübertragung entstehen; sie beweisen keinen eigenen Nachtfarbwert.
Rohmessungen und Koordinaten: [evidence/0.1.14/contrast.json](../evidence/0.1.14/contrast.json).

**Beobachtung für das Design-System:** Dieser Host zeigt trotz des korrekt
gepackten Farbpaares Hellblau statt Gold/Bronze. A3 erlaubt diese Host-Ersetzung.
Die Template-Fläche ist in beiden Builds und Modi dunkelgrau (Probe (100,100):
#25262A), nicht rein schwarz. Deshalb gilt der gemessene Kontrast, nicht die
theoretische Tabelle auf schwarzem Grund. Titel/Nachricht bleiben sichtbar
unverändert. O2 wird nicht benötigt; es wurden keine Ersatzwerte erfunden.

## DHU-Abnahme A1–A11

Die vollständige Matrix umfasst vier Formate × zwei Sprachen × Tag/Nacht ×
zwei Auswahlen × Touch/Rotary = **64 geprüfte Kombinationen**. Profile liegen unter
`tools/dhu/`; Einzelbelege, Messwerte und passende Start-Logzeilen stehen unter
[matrix.json](../evidence/0.1.14/matrix.json) und
[matrix-contrast.json](../evidence/0.1.14/matrix-contrast.json).

| Kriterium | Ergebnis |
|---|---|
| A1 Kopf und App-Symbol | In allen 64 Ansichten vollständig sichtbar. |
| A2 Start-Schaltfläche vollständig | „Starten“/„Start“ in allen Ansichten sichtbar. |
| A3 tatsächliche Akzentfarbe | Ausgangsversion grau, 0.1.14 Host-Hellblau; bei Tag und Nacht. |
| A4 gemessener Kontrast mindestens 4,5:1 | Vorher 8,25:1; neue 64 Ansichten **8,47–8,98:1**. Titel/Nachricht unverändert lesbar. |
| A5 vollständiger Status/Anbieter/Modell | Alle 64 Ansichten bestanden, einschließlich längster Kombination auf 800×480. Keine textliche Kürzung beobachtet. |
| A6 Start und passender Log-Zähler | Touch und Rotary bestanden, sichtbare Zähler stimmen mit `car.start count=n` überein; Ablauf und Belegumfang siehe unten. |
| A7 sechs Starts auf 800×480 | **Beide Versionen bestanden**, jeweils 1–6 in derselben Sitzung ohne Reset; kein Ausgangsfehler und keine Regression beobachtet. |
| A8 erreichbarer, sichtbarer Rotary-Fokus | In allen 32 Rotary-Kombinationen erreichbar; Host-Fokusrahmen bei Tag/Nacht aufgenommen. |
| A9 Deutsch/Englisch/Fallback | Deutsch und Englisch in der gesamten Matrix; Französisch am Gerät ergibt englisches „Ready…“, „Start“, „Started · Test 1“ im DHU (800×480/Touch). |
| A10 Auswahl live, Status bleibt | Modell/Anbieter am Smartphone geändert, während dieselbe DHU-Sitzung läuft. Status bleibt; separat Haiku → Sonnet → OpenAI bei Test 6, nächster Start 7. |
| A11 keine falschen Gesprächs-/Zulassungsaussagen | Quellvergleich und alle geprüften App-Ansichten bestanden; weiterhin UI-Test, keine neue Gesprächsfunktion. |

**Ablauf und Grenzen:** Pro Format/Sprache/Eingabe wurde eine Sitzung verwendet,
in der die zwei Modelle und Tag/Nacht gewechselt wurden. 60 Kombinationen haben
je eine dokumentierte Betätigung mit Zählerabgleich. Die vier deutschen
800×480-Touch-Ansichten stammen aus der manuellen Vergleichssitzung: Sonnet bei
Test 6, danach OpenAI bei Test 7/8. Sie sind keine vier separaten Erststarttests.
„Bereit → Test 1“ wurde beim Sitzungsstart geprüft; A7 ist ein separater Test mit
sechs Starts auf 800×480, Deutsch/Touch, Claude Haiku 4.5. Nicht sechs Starts pro
Matrixfeld und kein allgemeiner Nachweis für andere Android-Auto-Hosts.

Bei Rotary legt der Host den Fokus nach einer Textänderung wieder auf die
Nachricht. Vor dem nächsten Klick wurde der Start-Button erneut fokussiert.
Der Rahmen um die Nachricht berührt die äußeren Zeichen; beim Fokus auf dem
Start-Button sind die vollständigen Zeichen ohne diesen Rahmen sichtbar.
Es wurden keine Anzeigenamen gekürzt, Zähler zurückgesetzt oder Host-Limits deaktiviert.

Die Ausgangsserie `baseline/a7-1.png` bis `a7-6.png` zeigt jeden Zwischenstand.
Der zuletzt gesicherte Baseline-Logpuffer enthält 2–6; API 8 wurde vorher direkt
ausgelesen. Das kontinuierliche neue Log enthält Sitzung, API 8 und alle Starts 1–8.
Die genaue Umgebung steht in [environment.json](../evidence/0.1.14/environment.json).

Eine USB-Unterbrechung vor App-Start und eine fremde Host-Benachrichtigung während
einer Rotary-Serie unterbrachen den Testaufbau. Die betroffenen Serien wurden
regulär beendet und erfolgreich wiederholt. Keine Nachricht wurde versendet.
Diese Unterbrechungen sind keine A7-Ergebnisse und keine bestandenen Einzeltests.

**A7-Regressionsregel:** Scheitert es schon im Ausgangszustand, gilt es als bekannter
Fehler und blockiert 0.1.14 nicht. Nur neues Scheitern ist eine Regression.
Hier haben beide Vergleichsläufe bestanden; O4 wird auf diesem Host nicht ausgelöst.
Auch O2/O3 sind nach den Messungen nicht erforderlich. Das bestehende Symbol (O1)
bleibt bis zu einer gesonderten Designentscheidung erhalten.

## Belege und Reproduzierbarkeit

Das Download-Paket `OpenAutoAI-0.1.14-DHU-Testbelege.zip` enthält die 64 App-Ansichten,
32 zusätzliche Fokusansichten, Vorher-/Nachher-Kontrast, beide A7-Serien,
Sprachfallback und gezielte App-Logs. Größere DHU-Ansichten sind auf das vollständige
OpenAuto-Panel zugeschnitten, ohne Skalierung oder Änderung seiner Pixel.
Karten, Launcher und fremde Benachrichtigungen sind nicht enthalten.
Zu jedem Ausschnitt sind ursprüngliche Bildgröße und Ausschnittkoordinaten gespeichert.
Die Mess-JSONs beziehen sich auf die ursprünglichen Koordinaten; für Ausschnitte
deren linken/oberen Ursprung abziehen.

Mit Python und Pillow kann eine Pixelmessung nachvollzogen werden:

```powershell
python tools/dhu/contrast.py candidate/a3-a4-day.png 367 288 300 298
```

Die dokumentierte Fläche und volle Textstriche verwenden, keine geglätteten Kanten.
Das Video wird vom DHU dekodiert; geringfügige RGB-Abweichungen zwischen Bildern
sind möglich. Die Farbwerte sind Messungen dieses Hosts, keine neuen Design-Tokens.

Nach den Tests wurden die ursprüngliche Sprachliste `de-DE,en-US` mit Deutsch an
erster Stelle und die aktuelle Prototype-Auswahl Claude / Anthropic · Claude Haiku 4.5
wiederhergestellt und geprüft. DHU und Teststeuerung sind regulär beendet.

## Zurückgestellt und Historie

Anruf- und Headset-/Bluetooth-Tests bleiben zurückgestellt. OpenAuto AI hat bei
diesen Tests keine KI-Anfrage, Mikrofonaufnahme oder Hörprobe gestartet. Die
separate Assistenten-App und ihre Nutzerdaten wurden nicht verändert.
Die frühere Smartphone-Abnahme bleibt im [Bericht 0.1.13](../history/BUILD_AND_TEST-0.1.13.md) erhalten.

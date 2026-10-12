# Build- und Testbericht 0.1.15

Stand: 12.10.2026. **Assistent und DHU-Test sind als Update auf dem Pixel installiert.**
Die neue Sprechblase mit Punkt, das native Startbild und das kleine Dienstsymbol
folgen unverändert dem Design-System `9ed4032` / O1 `007f587`.
[Umsetzung und Installation](APP_IDENTITY.md).

## Umgebung

| Angabe | Nachweis |
|---|---|
| Gerät | Pixel 10 Pro, Android 17 / API 37 |
| Assistent installiert | 0.1.15-dev / Code 15 |
| DHU installiert | 0.1.15-prototype / Code 6 |
| Phone-Vergleich nur gebaut | 0.1.15-prototype / Code 5 |
| Android Auto aktiv | 17.7.663654-release / 177663654 |
| DHU | 2.0-windows, Build 2022-03-30-438482292 |
| Host-Car-API | 8; `10-12 22:20:37.863 I/OpenAutoAI(25368): car.onCreateScreen api=8` |
| Gerätesprachen und Nachtmodus | `de-DE,en-US`, Nachtmodus `yes`; wiederhergestellt bzw. unverändert |

Die zusätzlich gelistete Android-Auto-System-Stub-Version 1.2.562600-stub ist nicht
die aktive Testversion. Rohdaten, APK-Hashes und installierte Versionen:
[verification.json](evidence/0.1.15/verification.json).

## Build und Gerätetests

| Prüfung | Ergebnis |
|---|---|
| Assistent, Phone-Vergleich, DHU, beide Test-APKs | Abschließender lokaler Build erfolgreich |
| App-Lint | 0 Fehler, 29 Warnungen, 1 Hinweis |
| DHU-Lint | 0 Fehler, 6 Warnungen |
| Assistent auf Pixel | 17 ausgewählte Tests bestanden |
| DHU-Prototyp auf Pixel | alle 11 Tests bestanden |
| Quelltreue | 128 Referenzdateien per SHA-256 und 11 eingebundene XML-Kopien bytegleich geprüft |
| Gepackte Ressourcen | adaptive und runde Symbole, API-31/33-Starttheme, weißes Dienstsymbol und Versionsnummern geprüft |

Die 17 Assistententests umfassen DesignUiTest (9), DesignScreenshotsTest (3),
LocalizationLifecycleTest (4) und SpeechSettingsScreenTest (1). Sie verwenden
erfundene Verläufe und ersetzen Audio/Anbieter durch Testobjekte. Die elf
Prototyptests prüfen Car-Farbvertrag, Template, Auswahl und Smartphone-Testseite.
Die finale APK mit API-33-Startregel wurde installiert, bevor diese 28 Tests
erneut liefen. [App-Protokoll](evidence/0.1.15/app-tests.txt),
[DHU-Protokoll](evidence/0.1.15/dhu-tests.txt).

Die 28 Tests sind eine gezielte Design-Regression, keine Behauptung eines neuen
vollständigen Audio-/API-End-to-End-Tests. Die bisherigen Lint-Warnungen sind nicht
als behoben ausgewiesen. Der GitHub-CI-Status wird am zugehörigen Pull Request angezeigt.

## Darstellung am Pixel und im DHU

- Assistent und Prototyp jeweils in hellem und dunklem Gerätemodus gestartet.
  Das gelieferte goldene Zeichen erscheint auf Tinte. Der kurze native Übergang
  des Prototyps enthält die systemeigene Fensteranimation; keine eigene Animation
  und keine künstliche Startverzögerung wurden ergänzt.
- Helles deutsches und dunkles englisches Gespräch mit erfundenen Nachrichten
  visuell geprüft; große Schrift durch den bestehenden Screenshot-/Bedientest erfasst.
- Neues Zeichen im DHU-Launcher und im `APP_ICON`-Kopf vollständig sichtbar.
  Die ausgesparten Balken und der Punkt bleiben erkennbar.
- 800×480 / Touch: Bereitschaft, sechs Starts 1–6 in derselben Sitzung ohne Reset,
  danach Nachtansicht. Alle sichtbaren Zähler passen zu den Car-Logeinträgen.
- 1280×720 mit breitem Profil / Rotary: Öffnen über Launcher, sichtbarer Fokus,
  Start bei Tag und Nacht. Wechsel Claude Haiku → OpenAI GPT-5.4 aktualisiert die
  Auswahl live; Test 1 bleibt erhalten. Ein erneuter Nachtstart in einer frischen
  Sitzung besteht ebenfalls. Nach Bedienung der Smartphone-Auswahl verliert der
  Host jedoch den nutzbaren Rotary-Fokus; Drehen allein erreicht die Taste dann
  nicht. Die Prüfung dieses Sonderfalls ist separat unten beschrieben.
- Die ursprüngliche DHU-Auswahl Claude / Anthropic · Claude Haiku 4.5 wurde am Ende
  wiederhergestellt. Der DHU wurde regulär beendet und die Assistenten-App geöffnet.

Die Template-Fläche bleibt dunkelgrau, die Starttaste Host-Hellblau. Car-App-Code
und B2/B3-Farbressourcen sind gegenüber 0.1.14 unverändert. Stichproben aus den
unskalierten 800×480-Bildern:

| Modus | Hintergrund | Starttaste | Text | Kontrast |
|---|---|---|---|---|
| day | #25262A | #BAD9FC | #0E3253 | 9.01:1 |
| night | #25262A | #BAD9FC | #0E3454 | 8.80:1 |

Messmethode: sRGB-Linearisierung, relative Luminanz und WCAG-Kontrast; dunkle
Glypheninnenfarbe im dokumentierten Textbereich. Koordinaten und RGB-Werte:
[contrast.json](evidence/0.1.15/contrast.json). [Sechs Starts](evidence/0.1.15/six-starts.json),
[Car-Ereignisse](evidence/0.1.15/car-events.txt).

Die vollständige 64-Ansichten-Abnahme von 0.1.14 wird nicht als neu ausgeführt
behauptet; sie bleibt im [historischen Testbericht](history/BUILD_AND_TEST-0.1.14.md).

### Offene Host-Beobachtung: Rotary-Fokus

Im breiten 1280×720-Profil verschwand bei 0.1.15 nach der Smartphone-Auswahl der
nutzbare Fokus. Die Anzeige aktualisierte sich und behielt Test 1, weitere
Dreh-/Klickbefehle lösten aber keinen Start aus. Eine neue Sitzung erlaubte wieder
den ersten Start bei Nacht. Zur Einordnung wurde die unveränderte 0.1.14-DHU-APK
kurz mit `install -r -d` eingesetzt: Auch dort war ein Fokusrahmen zunächst
sichtbar, doch schon der erste spätere Klick erhöhte den Zähler nicht; nach der
Smartphone-Auswahl blieb der Fokus ebenfalls aus. Der genaue Auslöser wurde
nicht isoliert. Dies belegt ein auch mit dem alten Build auftretendes Problem,
keinen bestandenen Rotary-Test nach Live-Auswahl und keine nachgewiesene Ursache.
Die frühere 0.1.14-Abnahme wird durch diesen zusätzlichen Befund nicht umgeschrieben.

Belege: [0.1.14 vor Klick](evidence/0.1.15/baseline-focus-night.png),
[0.1.14 nach Klick](evidence/0.1.15/baseline-start-1-night.png),
[0.1.14 nach Auswahl](evidence/0.1.15/baseline-after-selection.png),
[0.1.15 neuer Nachtstart](evidence/0.1.15/identity-wide-recheck-start-1-night.png).
Anschließend wurde dieselbe geprüfte 0.1.15-APK erneut installiert und ihre
Versionsnummer bestätigt. Es gab weder Datenlöschung noch eine Änderung am
Car-App-Verhalten zur Umgehung dieses Befunds.

## Belege und Grenzen

- [Startbild hell](evidence/0.1.15/assistant-light-splash.png),
  [Startbild dunkel](evidence/0.1.15/assistant-dark-splash.png)
- [Gespräch hell/deutsch](evidence/0.1.15/az-light-conversation.png),
  [Gespräch dunkel/englisch](evidence/0.1.15/az-dark-conversation.png),
  [große Schrift](evidence/0.1.15/az-large-settings.png)
- [Launcher-Symbol](evidence/0.1.15/dhu-launcher-icon.png),
  [DHU bereit](evidence/0.1.15/identity-800-ready-day.png),
  [sechster Start](evidence/0.1.15/identity-800-start-6-night.png),
  [Rotary-Fokus](evidence/0.1.15/identity-wide-focus-day.png),
  [OpenAI nach Live-Wechsel](evidence/0.1.15/identity-wide-openai-live-night.png)

DHU-Belege sind auf die App zugeschnitten. Karten, andere Benachrichtigungen und
private Chatinhalte gehören nicht zu den veröffentlichten Belegen.

**Nicht neu praktisch geprüft:** Live-Gespräch und tatsächliche laufende
Gesprächsdienst-Benachrichtigung, Launcher-Themensymbole und ältere Android-Versionen.
Für das Dienstsymbol sind die gelieferte Ressource, Paketierung und `setSmallIcon`
geprüft; der Dienst wurde dafür nicht mit echtem Mikrofon gestartet. Ein lokaler
Shell-Benachrichtigungsversuch lieferte keinen sichtbaren Beleg und zählt nicht
als bestandener visueller Test. Die Monochrom-Ebene ist unverändert enthalten;
die persönliche Launcher-Konfiguration wurde nicht umgestellt.

Anruf-, Headset- und Bluetooth-Tests bleiben auf Nutzerwunsch zurückgestellt.
Keine KI-Anfrage, keine Änderung von Schlüsseln, Verläufen oder Sprachlogik und
keine Deinstallation/Löschung von App-Daten. Neue fehlende Designwerte wurden
für diesen Umfang nicht festgestellt.

# Sprache und Wiederaufnahme – 0.1.12

Die App nennt den Vorgang jetzt **Gespräch**: **Gespräch starten**, **Gespräch
fortsetzen** und **Gespräch beenden**. Auf Englisch heißen diese Schaltflächen
**Start conversation**, **Continue conversation** und **End conversation**.

## Automatische Sprache

Android wählt Deutsch oder Englisch anhand der Sprachpräferenzen des Geräts.
Die erste unterstützte Sprache der Liste wird verwendet; fehlt beides, gilt Englisch.
Es gibt keinen zusätzlichen Sprachschalter in der App und keine Änderung der
systemweiten Gerätesprache durch OpenAuto AI.

Übersetzt sind die Smartphone-Oberfläche, Dialoge, fest vorgegebene Status- und
Fehlermeldungen, Benachrichtigung, Stimmeinstellungen, Hörprobe und Beschriftungen
des Textexports. Die DHU-/Phone-Test-App erhält dieselben zwei Sprachen. Marken-
und Modellnamen bleiben unverändert. Bestehende Titel und Chatinhalte werden weder
übersetzt noch umgeschrieben; neue leere Gespräche bekommen einen passenden Titel.

Auch Spracherkennung, Android-Sprachausgabe und die vorhandene Anweisung an OpenAI
und Claude folgen der unterstützten Sprache. Eine tatsächliche Modellantwort ist
dadurch nicht garantiert. Die Anweisungen sind offline geprüft; der Nutzer bestätigt
unter 0.1.12 den Gesprächsablauf für beide Anbieter und die deutsche/englische Oberfläche. Endpunkte, Modelle und Antwortbudget bleiben gleich.

Beim Zuhören werden die bisherigen deutschen Befehle sowie **Stop**, **End
conversation**, **Switch to Claude**, **Switch to OpenAI** und **Switch to ChatGPT**
erkannt. Diese exakten Befehle schalten lokal um bzw. beenden das Gespräch.

## Stimmen

Die explizite Stimmwahl wird für Deutsch und Englisch getrennt gespeichert.
Ohne eigene Auswahl gilt Android-Standard für die jeweilige Sprache. Das Tempo
gilt weiterhin gemeinsam. Ein Sprachwechsel überschreibt keine bekannte Stimmwahl.

Eine bisher gespeicherte Stimme wird anhand ihrer vom Android-Sprachdienst
gemeldeten Sprache zugeordnet. Alte Werte werden nicht gelöscht. Ist die alte
Stimme oder der zugehörige Dienst nicht verfügbar, bleibt der konkrete Fehler
sichtbar, bis eine neue Auswahl erfolgt. Es wird keine beliebige Ersatzstimme
ausgesucht. Online-Stimmen benötigen weiterhin die vorhandene ausdrückliche Auswahl.

Verfügbare Stimmen und Erkennungssprachen hängen vom installierten Android-
Sprachdienst ab. Eine fehlende Sprache wird als Fehler gemeldet; die App lädt
keine Sprachpakete ungefragt nach.

## Drehen, Wechseln und erneutes Öffnen

- Bei Neuerstellung der Oberfläche bleiben im selben Prozess geöffneter Chat,
  Suchtext, Anbieterfilter und die angezeigte Nachrichtenposition erhalten.
- Ein fertig vorbereiteter Start oder Dateidialog wird nur an eine aktive,
  fortgesetzte Oberfläche übergeben und nur einmal verbraucht.
- Verlässt die Oberfläche den Vordergrund, wird ein noch vorbereiteter
  Gesprächsstart abgebrochen. Ein verspätetes Ergebnis startet keine Aufnahme.
- Ein offener Berechtigungsauftrag bleibt nur für eine Konfigurationsänderung
  im ViewModel erhalten, niemals als Startauftrag in einem gespeicherten Bundle.
  Nach Abbruch werden seine späteren Rückmeldungen verworfen; sie erlauben keinen
  neu begonnenen Auftrag. Benachrichtigungsfreigabe bleibt optional.
- Ein neuer Prozess startet kein Gespräch automatisch. Archiv und Einstellungen
  bleiben gespeichert; Suchtext/Filter werden nicht auf Datenträger gesichert.
- Ein Sprachwechsel beendet ein aktives Gespräch. Es muss ausdrücklich neu
  gestartet werden, damit Mikrofon und Stimme dieselbe Sprache verwenden.

Anruf- und Headset-/Bluetooth-Praxistests bleiben ausdrücklich zurückgestellt.
Die bisherige Behandlung von Display aus und anderen Unterbrechungen bleibt erhalten.

## Prüfen auf dem Pixel

1. In Deutsch die drei Bereiche **Gespräch / Einstellungen / Verlauf** und die
   Schaltfläche **Gespräch starten** prüfen.
2. Optional in Android die Gerätesprache auf Englisch stellen, die App erneut
   öffnen und **Conversation / Settings / History** prüfen. Vorher ein Gespräch beenden.
3. Nach dem Zurückwechseln die deutsche Stimmwahl unter **Stimme und Tempo** prüfen.
4. Einen erfundenen oder unkritischen Treffer im Verlauf öffnen und das Gerät
   drehen. Es darf keine zusätzliche Anfrage oder Aufnahme starten.
5. Je ein kurzes Gespräch mit OpenAI und Claude starten, Antwort anhören und
   beenden. Echte API-Aufrufe können Kosten verursachen; Schlüssel nur in der App.

Die automatisierten Sprach- und Oberflächenprüfungen verwenden ausschließlich
erfundene Inhalte. Die eigene Gerätesprache wurde dafür nicht geändert.
Ein vollständiger Prozessabbruch mit echtem Betriebssystem-Berechtigungsdialog
und hörbares englisches Vorlesen wurden nicht automatisiert ausgeführt.

## GitHub-Prüfungen

Der neue [Workflow](../.github/workflows/android.yml) prüft JVM-Tests, Android-Lint
und die Debug-Builds auf Pull Requests und nach Änderungen an `main`.
JDK 17, SDK Platform 37 und Build Tools 36.0.0 sind festgelegt. Die verwendeten offiziellen
Actions sind auf vollständige Git-Revisionen fixiert. Der Workflow verwendet keine
API-Schlüssel, führt keine Live-Gespräche aus und veröffentlicht keine Anwendung.
Testberichte werden 14 Tage aufbewahrt. Gerätetests laufen separat auf dem Pixel.

Grundlagen: [Android-Lokalisierung](https://developer.android.com/guide/topics/resources/localization),
[Activity-Lebenszyklus](https://developer.android.com/guide/components/activities/activity-lifecycle),
[OpenAI-Anweisungen](https://developers.openai.com/api/docs/guides/prompt-engineering).
Für die eng begrenzte Anpassung der Antwortsprache wurde OpenAI Docs verwendet.

[Bedienung](PHONE_UI.md) · [Testbericht](BUILD_AND_TEST.md) · [Installation](VOICE.md)

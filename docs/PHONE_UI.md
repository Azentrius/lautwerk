# Smartphone-Bedienung – 0.1.10

Die Hauptseite hat drei Bereiche. Der Gesprächsknopf und der aktuelle Status
bleiben am unteren Rand erreichbar, auch wenn die Liste gescrollt wird.

| Bereich | Inhalt |
|---|---|
| Gespräch | Aktueller Anbieter/Modell, Stimme und Tempo, geöffnetes Gespräch und Nachrichten |
| Einstellungen | Anbieter-/Modellauswahl sowie Eingabe und Verwaltung des API-Schlüssels |
| Verlauf | Vorhandene Chats öffnen oder ein neues Gespräch auswählen |

Ein Chat wird durch Öffnen nur angezeigt. Erst „Gespräch fortsetzen“ startet die
Sprachfunktion mit dem ursprünglichen Anbieter und Modell. Alte, nicht mehr
gelistete Modelle bleiben lesbar, dürfen aber nicht unbemerkt unter einem anderen
Modell fortgesetzt werden. „Neues Gespräch auswählen“ legt erst beim Start einen
neuen Chat an. Startvorbereitung zeigt „Start abbrechen“; eine laufende Sitzung
zeigt immer „Gespräch beenden“. Während einer Sitzung sind Anbieter-/Modellwechsel
per Einstellungen, Schlüsseländerungen und Löschen gesperrt; die bestehenden
ausdrücklichen Sprachbefehle zum Anbieterwechsel bleiben verfügbar.

Bei langen Chats werden zunächst die letzten 50 Nachrichten angeboten. „Ältere
Nachrichten anzeigen“ erweitert um jeweils 50. Alle gespeicherten Nachrichten
bleiben erhalten. Das Löschen eines Chats benötigt eine Bestätigung; Abbrechen
belässt ihn unverändert. Die Bestätigung gilt nur für die ursprünglich gewählte Chat-ID.

## Chat als Textdatei exportieren

Unter dem Titel des geöffneten Chats **Als Textdatei exportieren** wählen. Nach dem
Hinweis auf die unverschlüsselte Kopie führt **Speicherort wählen** in Androids
Dateidialog. Die Datei enthält alle Nachrichten, auch ältere ausgeblendete, und
keine gespeicherten API-Zugangsdaten. Eingetippte persönliche Angaben im Chattext
bleiben enthalten. Für eine lokale Kopie einen lokalen Ordner wählen; Cloud-Ziele
übertragen die Datei an den gewählten Dienst. Export ist während Voice gesperrt.

Erst die Meldung **Textdatei gespeichert** bestätigt den Abschluss. Bei Fehlern
kann eine unvollständige Datei zurückbleiben. Der Export verändert den Chat nicht
und ist kein Backup-/Importformat. Details: [CHAT_EXPORT.md](CHAT_EXPORT.md).

## Reaktion bei langsamem Speicher

Archiv lesen, Schlüssel prüfen/speichern/entfernen und Chats erstellen/löschen
laufen außerhalb des Hauptthreads. Ein Ladevorgang zeigt seinen Zustand, doppelte
Aufträge sind gesperrt. Ein fehlgeschlagener Zugriff erscheint als fester Hinweis;
das Archiv wird bei Lesefehlern nicht als leer behandelt. Wiederholte Änderungsmeldungen
werden gebündelt, überholte Ergebnisse nicht als neuer Stand angezeigt.

Der Zustand bleibt in einem ViewModel über eine Bildschirm-Neuerstellung erhalten.
Ein noch vorbereiteter Sprachstart wird beim Verlassen des sichtbaren Bildschirms
abgebrochen. Es wird kein Activity-Objekt als Hintergrund-Rückruf festgehalten.
Ein bereits begonnener atomarer Schreibvorgang kann trotzdem abschließen; z. B.
kann ein leerer neuer Chat erhalten bleiben, ohne dass die Aufnahme gestartet wird.

Das Schlüsselfeld ist ein Passwortfeld. Es speichert die Eingabe weder im ViewModel
noch im gespeicherten Activity-Zustand und wird nach Annahme des Speicherauftrags
geleert. Ein Erfolgshinweis bestätigt das Speichern; bei Fehlern neu eingeben.
Gespeicherte Schlüssel werden nie wieder als Klartext ins Feld geladen.

Die Ansicht nutzt eine bedarfsgerecht gezeichnete Liste; das vorhandene verschlüsselte
Archiv wird weiterhin vollständig im Hintergrund gelesen. Das ist keine Migration
zu einer Datenbank und kein garantiert begrenzter Speicherbedarf für beliebig große
Archive. Format, Dateinamen und Keystore-Aliase bleiben erhalten.
Grundlagen: [Lazy Lists](https://developer.android.com/develop/ui/compose/lists),
[Scaffold](https://developer.android.com/develop/ui/compose/components/scaffold),
[ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel).

## Praktisch prüfen

1. Einen vorhandenen Chat unter „Verlauf“ öffnen. Anbieter/Modell und alte Nachrichten
   prüfen, ohne einen neuen Schlüssel einzugeben. Öffnen allein löst keinen API-Aufruf aus.
2. Zwischen den drei Bereichen wechseln und scrollen. Starten/Beenden muss erreichbar bleiben.
3. Bei Gelegenheit den Chat fortsetzen, eine kurze Frage stellen und per Knopf beenden.
   Erneutes Starten ebenfalls prüfen. Echte Anbieteraufrufe können Kosten verursachen.
4. Für den zweiten Anbieter bei Gelegenheit wiederholen. Nur entbehrliche Testchats
   zum Prüfen der Löschbestätigung verwenden.

Anruf- und Headset-/Bluetooth-Tests bleiben auf Nutzerwunsch zurückgestellt.
Die neue Oberfläche wurde automatisiert und mit einem Screenshot ausschließlich
erfundener Daten geprüft; der erneute manuelle Sprachtest seit 0.1.9 und der vollständige
Systemdialog-/Dateidienst-Test des Exports stehen noch aus.
Keine Änderung an der bestätigten DHU-Oberfläche 0.1.2-prototype.

Bauen/Installieren: [VOICE.md](VOICE.md). DHU: [START_WINDOWS.md](START_WINDOWS.md).
Ergebnisse: [BUILD_AND_TEST.md](BUILD_AND_TEST.md).

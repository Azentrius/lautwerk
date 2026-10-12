# Einzelne Chats als Textdatei speichern – 0.1.11

Der Textexport speichert einen ausgewählten Chat als lesbare UTF-8-Datei. Er enthält
Titel, Anbieter, Modell, letzten Änderungszeitpunkt in UTC und **alle Nachrichten**
in ihrer gespeicherten Reihenfolge. Auch ältere, in der Ansicht zunächst ausgeblendete
Nachrichten werden exportiert. Auch nach dem Öffnen über die Verlaufssuche wird
der gesamte Chat exportiert, nicht nur passende Nachrichten. Ein alter Modellname
verhindert den Export nicht.

## Bedienung

1. Unter **Verlauf** einen Chat öffnen und das Sprachgespräch gegebenenfalls beenden.
2. Unter dem Chattitel auf **Als Textdatei exportieren** tippen.
3. Den Hinweis zur unverschlüsselten Kopie lesen. **Abbrechen** belässt alles unverändert.
4. **Speicherort wählen** öffnet Androids Speicherdialog. Vorgeschlagen wird
   `OpenAutoAI-Chat.txt`, ohne einen Teil deines Chattexts im Dateinamen.
5. Für eine lokale Kopie einen lokalen Ordner, z. B. **Downloads**, wählen und speichern.
   Bei einem Cloud-Dienst wird die Datei durch diesen Dienst dorthin übertragen.
6. Erst **Textdatei gespeichert** bestätigt den abgeschlossenen Schreibvorgang.
   Die Textdatei anschließend im Dateimanager öffnen und prüfen.

Das Öffnen und Exportieren benötigt keine KI-Anfrage, keinen API-Schlüssel und kein
Mikrofon. Es wird keine Nachricht versendet. Gespeicherte API-Zugangsdaten werden
nicht gelesen oder exportiert. **Persönliche Angaben oder versehentlich in eine
Nachricht geschriebene Geheimnisse bleiben Teil des Chattexts.** Es gibt keine
automatische Redaktion. Die gewählte Datei ist unverschlüsselt und unabhängig vom
verschlüsselten App-Archiv. Sie bleibt auch nach dem Löschen des Chats bestehen.

Der Export ist eine Textkopie, kein verschlüsseltes Backup und kein Importformat.
Er sichert keine Schlüssel, Einstellungen oder Sprachnachrichten und kann nicht
automatisch in OpenAuto AI zurückgespielt werden. Das App-Archiv wird nicht geändert.

## Abbruch und Fehler

- Während der Vorbereitung ist **Export abbrechen** verfügbar. Auch das Verlassen
  des Bildschirms bricht eine noch laufende Vorbereitung ab.
- Im Android-Speicherdialog mit Zurück/Abbrechen beenden: keine Chatdaten werden geschrieben.
- Während Vorbereitung, Auswahl und Schreiben sind weitere Starts, Exporte,
  Schlüsseländerungen und Löschvorgänge gesperrt. Die übrigen Bereiche bleiben erreichbar.
- Schreib-, Zugriffs-, Flush- und Schließfehler führen zu einem festen Fehlerhinweis,
  nicht zu einer Erfolgsmeldung. Am Ziel kann eine leere oder unvollständige Datei
  zurückbleiben; dort prüfen oder löschen und den Export neu beginnen.
- Bei Prozessende während der Auswahl wird kein Chat aus der aktuellen Auswahl
  erraten. Ein verspätetes Ergebnis schreibt nichts; eine vom Dateidienst bereits
  angelegte leere Datei kann bestehen bleiben. Während des Schreibens ist die Ausgabe
  nicht atomar, besonders bei Cloud-Dateidiensten. Ein unterbrochener Export muss geprüft werden.

Eine Aktivitätsneuerstellung behält den vorbereiteten Schnappschuss im ViewModel.
Der Dialogauftrag wird nur einmal entnommen. Chattexte und Zieladressen werden nicht
in den gespeicherten Activity-Zustand, Protokolle oder dauerhafte URI-Freigaben geschrieben.
Ein echter Rotationstest mit geöffnetem Systemdialog steht noch aus.

## Technischer Umfang und Prüfung

Die App verwendet Androids `ACTION_CREATE_DOCUMENT` mit `text/plain` und
`CATEGORY_OPENABLE`; nur die zurückgegebene `content:`-Adresse wird beschrieben.
Dateiauswahl und Berechtigung vergibt Android. Es gibt keine neue Speicherberechtigung,
keinen Zugriff auf beliebige Ordner und keinen automatischen Upload.
Grundlage: [Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files).

Ein separater Formatter schreibt den vollständigen Chat abschnittsweise, ohne einen
zweiten vollständigen Ausgabetext im Speicher zu bauen. Die vorhandene Archivdatei
wird weiterhin komplett gelesen; dies ist keine Datenbankmigration oder Seitennachladung.

Automatisiert geprüft: beide Anbieter, alte Modelle, Unicode, 121 Nachrichten,
große Einzeltexte, Abbruch, doppelte Dialogrückmeldungen, fehlende Chats, Speicherfehler,
UTF-8-Ausgabe in eine echte temporäre Testdatei und die Bestätigung in der Oberfläche.
Die Tests verwenden ausschließlich erfundene Inhalte. Am 12.10.2026 bestätigt der
Nutzer nach dem Testauftrag für einen unkritischen Chat und das lokale Ziel Downloads:
**„Export und Datei funktionieren.“** Damit sind der lokale Export, das Öffnen und
der vollständige Inhalt dieses getesteten Chats praktisch bestätigt. Cloud-Ziele
und Rotation während der Auswahl sind dadurch nicht bestätigt.

Anruf- und Headset-/Bluetooth-Tests bleiben zurückgestellt. Die DHU-Oberfläche
0.1.2-prototype bleibt erhalten und bietet keinen Export.

[Bedienung](PHONE_UI.md) · [Installation](VOICE.md) · [Testbericht](BUILD_AND_TEST.md)

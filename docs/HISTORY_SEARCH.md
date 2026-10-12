# Chats durchsuchen und filtern – 0.1.12

Unter **Verlauf** lassen sich gespeicherte Chats nach Text durchsuchen und auf einen
Anbieter eingrenzen. Der Nutzer bestätigt am 12.10.2026 nach dem Testauftrag für
Suche in älteren Nachrichten, Anbieterfilter, Öffnen eines Treffers und Zurücksetzen:
**„Suche, Filter und Treffer funktionieren.“**

## Bedienung

1. **Verlauf** öffnen und im Feld **Chats durchsuchen** ein Wort oder eine Textfolge
   eingeben. Titel, Anbietername, Modell-ID und alle Nachrichten werden durchsucht.
2. Optional **OpenAI** oder **Claude** wählen. **Alle** entfernt nur den Anbieterfilter.
   Suchtext und Anbieterfilter gelten gemeinsam; die Gesprächs-/Modellauswahl ändert
   sich dadurch nicht.
3. Die Anzeige **x von y Gesprächen** zählt passende Chats, nicht einzelne Textstellen.
   Jeder Chat erscheint einmal in der bisherigen Archiv-Reihenfolge.
4. Einen Treffer öffnen. Wird der Text nur in Nachrichten gefunden, steht am Eintrag
   **Treffer in Nachricht …**; die App springt zur ersten passenden Nachricht.
   Ältere Nachrichten werden dafür automatisch in der Ansicht verfügbar gemacht.
   Bei einem Treffer schon im Titel/Anbieter/Modell öffnet sich der Chat am Anfang
   der Gesprächsansicht. Es startet kein Gespräch und keine KI-Anfrage.
5. Über **Verlauf** zurückkehren: Suchtext und Filter bleiben für diese Sitzung erhalten.
   **Suche zurücksetzen** leert beides und zeigt wieder alle vorhandenen Chats.

Die Suche arbeitet als wörtliche Teilstringsuche ohne Beachtung der Groß-/Kleinschreibung.
Leerzeichen am Anfang und Ende des Suchtexts werden ignoriert. Die Eingabe ist auf
200 Zeichen begrenzt. Es gibt keine regulären Ausdrücke, Wortstammerkennung, unscharfe
Suche oder automatische Umwandlung von `ä` in `ae`. Ein leerer Suchtext zeigt alle
Chats des gewählten Anbieters. Das Archiv und Nachrichtentexte bleiben unverändert.

## Vorhandene Funktionen bleiben erhalten

- Starten/Beenden bleibt in der festen unteren Leiste erreichbar. Während Voice darf
  die Liste durchsucht werden; das Öffnen eines anderen Chats bleibt gesperrt.
- Chats mit nicht mehr gelisteten Modellen bleiben lesbar und exportierbar. Die Suche
  wechselt ihren Anbieter oder ihr Modell nicht. Fortsetzen bleibt für alte Modelle gesperrt.
- Der [Textexport](CHAT_EXPORT.md) enthält weiterhin **alle Nachrichten des geöffneten
  Chats**, unabhängig von Suchtext, Filter und angezeigtem Ausschnitt.
- Löschen verwendet weiterhin die Bestätigung für die konkrete Chat-ID. Nach einer
  Archivaktualisierung wird die Suche erneut auf dem aktuellen Stand ausgeführt.
- Die DHU-Oberfläche erhält englische Texte in 0.1.12-prototype; sie bekommt
  keine Suchoberfläche, keine neue Berechtigung und keine Sprachfunktion.

## Reaktion und Datenhaltung

Textsuche und Filter laufen auf dem bestehenden Hintergrund-Worker. Bei Texteingabe
wartet die App kurz (200 ms), um nicht für jeden Tastendruck eine komplette Suche
zu starten. Überholte Aufträge werden abgebrochen und ihre Ergebnisse verworfen.
Das gilt auch nach Änderung oder erneutem Laden des Archivs. Währenddessen werden
keine veralteten Treffer zum Öffnen angeboten.

Bei Lesefehlern wird das Archiv nicht als leer behandelt. Vorherige Suchergebnisse
verschwinden; ein vorhandener Speicherfehler bleibt sichtbar. Ein eigener Suchfehler
bietet **Suche erneut versuchen** und zeigt keine internen Daten oder Suchtexte als
Fehlermeldung. Kein automatisches Löschen oder Überschreiben.

Suche und Filter bleiben ausschließlich im ViewModel-Speicher. Es gibt keinen
zusätzlichen Klartextindex, keine Suchhistorie in Dateien/Einstellungen und keinen
Suchtext im gespeicherten Activity-Zustand. Nach einem Prozessneustart beginnt die
Suche leer. Die Suche verwendet keinen API-Schlüssel und sendet nichts an KI-Anbieter.
Die Tastatur unterliegt weiterhin den Einstellungen des gewählten Android-Tastaturdiensts.

Das vorhandene verschlüsselte Archiv wird weiterhin vollständig eingelesen. Die neue
Suche ist kein Datenbankindex und keine Seitennachladung von Dateien. Sehr große
Archive benötigen weiterhin Speicher und Zeit. Lange Einzeltexte werden abschnittsweise
durchsucht, sodass auch darin ein Abbruch geprüft wird.

## Prüfung

Automatisiert geprüft sind unter anderem beide Anbieter, alte Modelle, Umlaute/Emoji,
wörtliche Sonderzeichen, 1.000 künstliche Chats, Suche über eine Textabschnittsgrenze,
Abbruch in einer langen Nachricht, verspätete Ergebnisse und Archivfehler.
Die Oberflächentests öffnen Treffer in Nachricht 4 von 140 und Nachricht 96 von 100
direkt, ohne vorheriges manuelles Einblenden oder Scrollen. Suchansicht mit erfundenen
Chats visuell geprüft. Der Praxistest des Nutzers bestätigt die Kernbedienung.

Activity-Neuerstellung und Sprachwechsel sind seit 0.1.12 mit behaltenem Suchtext,
Filter und älterem Treffer geprüft. Ein vollständiger Prozessabbruch wurde nicht
als End-to-End-Gerätetest ausgeführt. Die Lebensdauer bleibt an das ViewModel gebunden. Anruf- und
Headset-/Bluetooth-Tests bleiben auf Nutzerwunsch ausdrücklich zurückgestellt.

Grundlagen: [Compose-Listen und Scrollposition](https://developer.android.com/develop/ui/compose/lists#controlling-scroll-position)
und [LaunchedEffect](https://developer.android.com/develop/ui/compose/side-effects#launchedeffect).

[Bedienung](PHONE_UI.md) · [Installation](VOICE.md) · [Testbericht](BUILD_AND_TEST.md)

Seit 0.1.12: [Deutsch/Englisch und Wiederaufnahme](LOCALIZATION.md).

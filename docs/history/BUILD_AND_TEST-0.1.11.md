# Build- und Testbericht 0.1.11

Stand: 12.10.2026. **78 automatisierte Prüfungen bestanden: 18 Core-JVM,
53 Assistant-Gerätetests und 7 DHU-/Auswahltests.** Assistant **0.1.11-dev / Code 12**
als Update auf Pixel 10 Pro / Android 17 installiert und geöffnet. Keine Datenlöschung.
DHU-/Phone-Oberfläche bleibt **0.1.2-prototype / Code 3**.

## Änderung und praktische Bestätigung

Der Verlauf kann nach Titel, Anbietername, Modell und allen Nachrichten durchsucht
und nach Anbieter gefiltert werden. Die Suche läuft auf dem bestehenden Worker;
veraltete Ergebnisse nach Texteingabe oder Archivaktualisierung werden verworfen.
Treffer in Nachrichten führen direkt zur ersten passenden Stelle, auch außerhalb
der zunächst angezeigten letzten 50 Nachrichten. Keine neue Datei, Berechtigung,
API-Anfrage oder Änderung des Speicherformats. Keine gespeicherte Suchhistorie.

Der Nutzer bestätigt am 12.10.2026 nach dem Testauftrag für Suche in einer älteren
Nachricht, Anbieterfilter, Öffnen eines Treffers und Zurücksetzen:
**„Suche, Filter und Treffer funktionieren.“** Der lokale Textexport wurde bereits
unter 0.1.10 bestätigt; diese Rückmeldung wird nicht als neuer Exporttest ausgegeben.

## Ausgeführte Prüfungen

| Prüfung | Ergebnis |
|---|---|
| Assistant- und DHU-Debug-App samt Test-APKs | erfolgreich gebaut und als Updates installiert |
| `:app:lintDebug` | 0 Fehler, 10 bestehende Warnungen und 1 KTX-Hinweis |
| Core-JVM | 18 bestanden: 11 bisherige Prüfungen und 7 neue Suchtests |
| `ConversationSearchTest` | Beide Anbieter, leere Suche, Reihenfolge, Unicode, wörtliche Sonderzeichen, 1.000 künstliche Chats, alte Nachrichten, Textabschnittsgrenzen und Abbruch in langer Einzelnachricht |
| `HistorySearchTest` | 7 bestanden: Suche ohne Archiv-/Schlüsseländerung, ältere Ergebnisse, Entwertung bei Archivaktualisierung, Lesefehler/Wiederholen, aktive Voice-Sitzung, sichere Suchfehler und Ende der ViewModel-Lebensdauer |
| `HistorySearchUiTest` | 5 bestanden: Filter/Zähler/Zurücksetzen, direkter Sprung zu Nachricht 4 von 140 und 96 von 100, alte Modelle, sichtbares Beenden während Suche und Vorschau |
| Bisherige Assistant-Tests | 41 bestanden, einschließlich Export, Speicher-UI, Home-Zustand, Anbieter/Android, Worker, Unterbrechungsbeobachter, Stimme und Einstellungen |
| DHU-/Auswahltests | 7 bestanden: Car-Template, Starten, Auswahl und Smartphone-Oberfläche |
| Visuelle Prüfung | Suchansicht mit ausschließlich erfundenen Chats auf dem Pixel geprüft; Filter, Treffer und feste Steuerung sichtbar |

Die Tests verwenden ausschließlich erfundene Inhalte, Speicherattrappen, eigene
Testdateien und isolierte Einstellungen. Keine persönlichen Chats oder Schlüssel
ausgelesen, keine Mikrofonaufnahme, hörbare TTS oder echte API-Anfrage gestartet.
Die Nutzerbestätigung erfolgte getrennt davon auf dem eigenen Gerät; Chatinhalte
wurden dazu nicht angefordert oder ausgelesen.

## Erhaltener Stand und offene Grenzen

`providers`, `voice`, `storage`, `settings`, `prototype-app` und `car-ui` bleiben
quellseitig unverändert. `core` erhält eine unabhängige Suchfunktion und Tests;
`app` erhält die UI-/Worker-Anbindung. Kein geänderter Modellkatalog, keine Migration,
keine neue Car-Kategorie und keine geänderte Hostprüfung. API-Schlüssel, Archiv und
gewählte Stimme bleiben erhalten. Export liest weiterhin den vollständigen gewählten Chat.

Die 64 JVM-Prüfungen der unveränderten Anbieter-/Sprachmodule bestanden zuletzt unter
0.1.8 und wurden nicht wiederholt. Das unveränderte Speichermodul wurde nicht erneut
separat getestet. DHU-Template-Tests belegen keine neue reale Launcher-Verbindung;
die bisherige Launcher-/Starten-Bestätigung des Nutzers gilt für 0.1.2-prototype.

Die Suche arbeitet auf dem vollständig eingelesenen Archiv. Sie ist keine Datenbank-
Seitennachladung und keine Garantie für beliebig große Archive bei begrenztem RAM.
Sie sucht wörtliche Teiltexte ohne Beachtung der Groß-/Kleinschreibung; keine unscharfe
Suche oder Akzentnormalisierung. Jeder Chat erscheint einmal, mit der ersten passenden
Nachricht als Sprungziel, sofern nicht bereits die Metadaten passen.

Rotation/Prozessneustart wurden für die neue Suche nicht separat als End-to-End-
Gerätetests ausgeführt. Suchtext/Filter verbleiben im ViewModel und werden nicht
in Dateien, Einstellungen oder den gespeicherten Activity-Zustand geschrieben.
Cloud-Dateidienste und Rotation beim Export bleiben ebenfalls ungeprüft.

Der erneute Sprachtest der Smartphone-Bedienung seit 0.1.9 steht aus. **Anruf- und
Headset-/Bluetooth-Tests bleiben ausdrücklich zurückgestellt.** Auto-Audio und
reale Fahrzeugnutzung sind weiterhin nicht bestätigt.

## Wiederholen

JDK 17/SDK gemäß [START_WINDOWS.md](../START_WINDOWS.md) setzen. Für UI-Tests das Pixel
entsperrt und unbedient lassen, eine aktive DHU-Sitzung zuvor regulär beenden.

```powershell
.\gradlew.bat :core:test :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :prototype-app:connectedDhuDebugAndroidTest
```

In diesem Lauf gebaut/installiert und über `adb shell am instrument -w` ausgeführt:
Assistant `OK (53 tests)`, DHU `OK (7 tests)`; Core-XML-Berichte: 18 Tests, 0 Fehler.

[Suchanleitung](../HISTORY_SEARCH.md) · [Bedienung](../PHONE_UI.md) · [Installation](../VOICE.md)
Vorheriger Bericht: [0.1.10](BUILD_AND_TEST-0.1.10.md).

# Build- und Testbericht 0.1.10

Stand: 12.10.2026. **59 automatisierte Prüfungen bestanden: 11 Core-JVM,
41 Assistant-Gerätetests und 7 DHU-/Auswahltests.** Assistant **0.1.10-dev / Code 11**
als Update auf Pixel 10 Pro / Android 17 installiert. Keine Datenlöschung.
DHU-/Phone-Oberfläche bleibt **0.1.2-prototype / Code 3**.

## Änderung

Einzelne Chats können nach einem Klartexthinweis über Androids Dateidialog als
UTF-8-Text exportiert werden. Alle Nachrichten werden übernommen; keine UI-Begrenzung
auf 50 Nachrichten im Export. Die Bestätigung gilt nur für den gewählten Chat.
Vorbereitung und Schreiben laufen im Hintergrund, weitere Mutationen sind gesperrt.
Eine feste Kopie wird bis zur Zielauswahl nur im ViewModel gehalten; Schlüsselstore,
Sprachdienst und API-Adapter sind am Export nicht beteiligt. Keine neuen Berechtigungen.

## Ausgeführte Prüfungen

| Prüfung | Ergebnis |
|---|---|
| Assistant-Debug-App und Test-APK | erfolgreich gebaut und installiert |
| DHU-Debug-App und Test-APK | neu gebaut und als Update installiert; Quellen der DHU-Oberfläche unverändert |
| `:app:lintDebug` | 0 Fehler, 10 bestehende Warnungen und 1 KTX-Hinweis |
| Core-JVM | 11 bestanden: 5 bestehende und 6 Exportprüfungen für beide Anbieter, Unicode, Vollständigkeit, große Texte, Abbruch und Schreibfehler |
| `ChatExportTest` | 8 bestanden: Hintergrundarbeit, beide Anbieter/alte Modelle, keine Schlüsselzugriffe, unverändertes Archiv, Abbruch, Doppelrückmeldungen, fester Schnappschuss, fehlende/geänderte Auswahl, Speicherfehler und fehlender Zustand nach Prozessverlust |
| `DocumentConversationExporterTest` | 4 bestanden: tatsächlicher App-Intentvertrag, UTF-8-Ausgabe in temporäre Datei, Ressourcenfreigabe, Zugriffs-/Schreib-/Flush-/Schließfehler, Abbruch und URI-Prüfung |
| `StorageUiTest` | 11 bestanden, darunter 2 neue Exportprüfungen für Offenlegung/Abbruch/einmalige Auswahl und Sperre während Voice |
| Bestehende übrige Assistant-Tests | 18 bestanden: Home-Zustand/Worker, Anbieter/Android, Unterbrechungsbeobachter, Stimmwahl und Einstellungen |
| DHU-/Auswahltests | 7 bestanden: echtes Car-Template mit TestCarContext, Starten, Auswahl und Smartphone-Oberfläche |
| Visuelle Prüfung | Gesprächsseite und Klartexthinweis mit ausschließlich erfundenen Chats auf dem Pixel geprüft |

Die erste Geräteserie zeigte eine fehlende `CATEGORY_OPENABLE`-Kategorie im
Standard-CreateDocument-Vertrag. Die App setzt diese jetzt ausdrücklich; der
Test prüft den tatsächlich verwendeten App-Vertrag. Die abschließende Serie besteht.

Die Tests verwenden ausschließlich erfundene Chats, Attrappen, isolierte Einstellungen
und eigene temporäre Dateien. Persönliche Chats und Schlüssel werden nicht ausgelesen,
keine echten Exporte in Nutzerordner vorgenommen, keine Mikrofonaufnahme, hörbare TTS
oder echte API-Anfrage gestartet. Die Dateischreibprüfung verwendet einen eingespeisten
Stream in eine eigene Testdatei; sie ist kein End-to-End-Test eines DocumentsProvider.

## Erhaltener Stand und offene Grenzen

`providers`, `voice`, `storage`, `settings`, `prototype-app` und `car-ui` bleiben
quellseitig unverändert. `core` erhält nur den unabhängigen Textformatter und dessen
Tests. Keine Migration, Änderung am Modellkatalog, neue Car-Kategorie oder geänderte
Hostprüfung. Schlüssel, Chats und gewählte Stimme bleiben erhalten.

Die 64 JVM-Prüfungen der unveränderten Anbieter-/Sprachmodule bestanden zuletzt unter
0.1.8 und wurden nicht wiederholt. Das unveränderte Speichermodul wurde nicht erneut
separat getestet. DHU-Template-Tests belegen keine neue reale Launcher-Verbindung;
die bisherige Launcher-/Starten-Bestätigung des Nutzers gilt für 0.1.2-prototype.

Der echte Android-Dateidialog mit einem lokalen/Cloud-Dateidienst sowie Rotation mit
geöffnetem Dialog müssen noch praktisch geprüft werden. Der Test für Prozessverlust
simuliert ein Ergebnis ohne zurückbehaltenen Auftrag; er beendet nicht den echten Prozess.
Ein Ausgabefehler oder Prozessende kann eine leere/teilweise Textdatei zurücklassen.
Cloud-Synchronisation wird nicht bestätigt, nur das erfolgreiche Schreiben/Schließen
des angebotenen Streams. Der Textexport ist kein verschlüsseltes Backup oder Importformat.
Große Archive werden weiterhin vollständig gelesen; Ausgabe erfolgt stückweise.

Die neue Smartphone-Bedienung seit 0.1.9 ist noch nicht manuell mit realen Gesprächen
bestätigt. Anruf- und Headset-/Bluetooth-Tests bleiben ausdrücklich zurückgestellt;
Auto-Audio und reale Fahrzeugnutzung sind weiterhin nicht bestätigt.

## Wiederholen

JDK 17/SDK gemäß [START_WINDOWS.md](START_WINDOWS.md) setzen. Für UI-Tests das Pixel
entsperrt und unbedient lassen, eine aktive DHU-Sitzung zuvor regulär beenden.

```powershell
.\gradlew.bat :core:test :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :prototype-app:connectedDhuDebugAndroidTest
```

In diesem Lauf gebaut/installiert und über `adb shell am instrument -w` ausgeführt:
Assistant `OK (41 tests)`, DHU `OK (7 tests)`.

[Export-Anleitung](CHAT_EXPORT.md) · [Bedienung](PHONE_UI.md) · [Installation](VOICE.md)
Vorheriger Bericht: [0.1.9](history/BUILD_AND_TEST-0.1.9.md).

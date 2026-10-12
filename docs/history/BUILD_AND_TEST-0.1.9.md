# Build- und Testbericht 0.1.9

Stand: 12.10.2026. **34 Gerätetests bestanden: 27 Assistant, 7 DHU/Auswahl.**
Assistant **0.1.9-dev / Code 10** als Update auf Pixel 10 Pro / Android 17 installiert.
Keine Datenlöschung. DHU-/Phone-Oberfläche bleibt **0.1.2-prototype / Code 3**.

## Änderung

Die Smartphone-Oberfläche trennt Gespräch, Einstellungen und Verlauf. Status und
Starten/Beenden stehen in einer festen unteren Leiste. Verschlüsselte Speicherzugriffe
laufen über einen seriellen Hintergrund-Worker. Ein ViewModel hält Auswahl und
Auftragszustand; Schlüsseltexte sind nicht Teil seines Zustands. Gleichzeitige
Schreibaufträge werden verhindert, Aktualisierungen gebündelt und veraltete Lese-
oder Startresultate abgefangen. Nicht mehr vorhandene oder anders zugeordnete Chats
werden nicht automatisch durch einen neuen Chat ersetzt.

Lange Listen werden nur im sichtbaren Bereich gezeichnet. Ein Chat zeigt zunächst
die letzten 50 Nachrichten und kann um jeweils 50 ältere erweitert werden. Keine
Nachrichten werden abgeschnitten oder aus dem Archiv gelöscht. Löschen benötigt
eine an die ursprüngliche Chat-ID gebundene Bestätigung.

## Ausgeführte Prüfungen

| Prüfung | Ergebnis |
|---|---|
| `:app:assembleDebug`, `:app:assembleDebugAndroidTest` | erfolgreich |
| `:app:lintDebug` | 0 Fehler, 10 Warnungen und 1 KTX-Hinweis; zusätzlicher Versionshinweis durch explizite Deklaration des bereits verwendeten ViewModel 2.9.4 |
| `HomeViewModelTest` | 7 bestanden: beide Anbieter, Arbeit außerhalb Hauptthread, Doppelstart, gebündelte Aktualisierung, veraltete Leseergebnisse, Startabbruch beim Schließen, fehlende/geänderte Chats und alte Löschbestätigungen |
| `StorageUiTest` | 9 bestanden: Speicherfehler/Wiederholen, alte Modelle, 200 Chats und 120 Nachrichten, ältere Nachrichten, sichtbare Steuerung, langsames Schlüsselspeichern, Startabbruch, Löschbestätigung und Vorschau |
| Bestehende Assistant-Tests | 11 bestanden: Anbieter/Android, Unterbrechungsbeobachter, Worker, Stimmwahl, TTS-Konfiguration, Einstellungsseite |
| DHU-/Auswahltests | 7 bestanden: Car-Template, Auswahl und Starten |
| Visuelle Prüfung | Pixel-Screenshot der Gesprächsansicht mit ausschließlich erfundenen Chats; Steuerung und Bereiche sichtbar |

Die Tests verwenden isolierte Einstellungen und Speicherattrappen. Sie starten
weder Mikrofon, hörbare Sprachausgabe noch echte Anbieteranfragen und lesen keine
persönlichen Chats oder Zugangsdaten. Ein Test markiert jeden Speicheraufruf auf
dem Hauptthread als Fehler; alle geprüften Aufrufe erfolgten im Hintergrund.

Die 69 JVM-Prüfungen der unveränderten Module `core`, `providers` und `voice`
bestanden zuletzt unter 0.1.8 und wurden hier nicht wiederholt. Auch das
unveränderte Speichermodul wurde nicht erneut separat getestet. Änderungen betreffen
die Smartphone-Anbindung und deren Tests, nicht Datei-/Verschlüsselungsformate.

## Erhaltener Stand und offene Grenzen

Quellen in `core`, `providers`, `voice`, `storage`, `settings`, `prototype-app` und
`car-ui` bleiben gegenüber 0.1.8 unverändert. Keine neuen Berechtigungen, keine
Car-App-Kategoriebehauptung und keine geänderte Hostprüfung. API-Schlüssel, Chats,
Stimme und Modellauswahl werden nicht migriert. Der Nutzer bestätigte unter 0.1.8
Stoppen/Neustarten per Knopf und Display-aus für beide Anbieter.

Der praktische Sprachtest mit der neuen 0.1.9-Oberfläche steht noch aus. Anruf-
und Headset-/Bluetooth-Tests bleiben auf Nutzerwunsch zurückgestellt und ungeprüft.
Auto-Audio und reale Fahrzeugnutzung sind weiterhin nicht bestätigt.

Das Archiv wird weiterhin komplett im Hintergrund gelesen und bei Änderungen
neu verschlüsselt; große Dateien können Aufträge verzögern und Speicher belegen.
Die begrenzte Ansicht ist keine Datenbank-Paginierung. Ein begonnener atomarer
Schreibvorgang kann trotz Startabbruch noch abschließen. Activity-Neuerstellung
wird über ViewModel-Zustand unterstützt; ein echter Rotationstest während eines
Schreibvorgangs wurde in diesem Lauf nicht durchgeführt.

## Wiederholen

JDK 17/SDK gemäß [START_WINDOWS.md](../START_WINDOWS.md) setzen. Für UI-Tests DHU
regulär beenden und das Pixel entsperrt lassen.

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :prototype-app:connectedDhuDebugAndroidTest
```

Gebautes Testpaket über `adb shell am instrument -w`: `OK (27 tests)`.
Unveränderte installierte DHU-Testpakete: `OK (7 tests)`.
Bedienung: [PHONE_UI.md](../PHONE_UI.md). Installation: [VOICE.md](../VOICE.md).
Vorheriger Bericht einschließlich Nutzerbestätigung: [0.1.8](BUILD_AND_TEST-0.1.8.md).

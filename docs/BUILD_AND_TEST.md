# Build- und Testbericht 0.1.6

Stand: 12.10.2026. **57 JVM-Tests bestanden** (38 Voice, 17 Anbieter, 2 Kern).
Assistant- und DHU-Debug-APK sind gebaut. Die DHU-/Phone-Oberfläche bleibt bei
**0.1.2-prototype / Code 3**, die Assistenten-App ist **0.1.6-dev / Code 7**.

Die Assistenten-App **0.1.6-dev / Code 7** wurde als Update auf dem Pixel 10 Pro / Android 17 installiert; die Paketversion und ein erfolgreicher Kaltstart sind geprüft. **14 Gerätetests bestehen erneut** (7 Assistant, 7 DHU/Auswahl), insgesamt mit den JVM-Tests **71 automatisierte Prüfungen**. Bestehende App-Daten wurden nicht gelöscht.

## Fehlerbefund und Änderung

0.1.5 scheiterte beim Nutzer wiederholt mit „Ein anderer Audiovorgang hat Vorrang“. Die Ursache ist im Geräteprotokoll nachvollzogen und in 0.1.6 korrigiert. Ein neuer praktischer Sprachtest mit OpenAI und Claude steht noch aus; Erkennung, hörbare Antwort und Beenden sind für 0.1.6 noch nicht vom Nutzer bestätigt.

Der technische Fokusverlauf zeigte bei mehreren Starts: OpenAuto AI fordert
Fokus an; anschließend übernimmt der Google-Spracherkennungsdienst Fokus;
OpenAuto AI erhält `AUDIOFOCUS_LOSS_TRANSIENT` und beendet die Aufnahme.
Die Korrektur begrenzt den eigenen Fokus auf das Vorlesen. Initialisierung,
Erkennung und API-Wartezeit benötigen keinen eigenen Wiedergabefokus.
Fokusverweigerung und tatsächliche Unterbrechungen während der Ausgabe werden
weiterhin beachtet. Keine Aufnahme-/Schlüsseldaten wurden zur Diagnose gelesen.

## Ausgeführte Prüfungen

| Prüfung | Ergebnis |
|---|---|
| `PlaybackFocusAudioTest` | 11 neue Tests: kein Fokus bei Vorbereitung/Erkennung, Anforderung vor TTS, Freigabe vor nächster Aufnahme, Ablehnung, Verlust, Stoppen, Schließen, veraltete Rückmeldungen, synchrone Rückmeldungen, TTS-Fehler |
| `VoiceControllerTest` | 17 Tests; alle verwenden jetzt die Fokus-Schicht, darunter 2 neue Anbieter-Regressionsfälle |
| Übrige Voice-Tests | 8 Archiv-/Anbieteranbindung und 2 TTS-Textaufteilung bestanden |
| `:providers:test` / `:core:test` | 17 / 2 bestanden |
| `:app:assembleDebug` / `:app:assembleDebugAndroidTest` | Erfolgreich |
| `:prototype-app:assembleDhuDebug` | Erfolgreich |
| Assistant-Gerätetests | 7 bestanden |
| DHU-/Auswahl-Gerätetests | 7 bestanden |
| `:app:lintDebug` | 0 Fehler, 7 bestehende Warnungen |
| `:prototype-app:lintDhuDebug` | 0 Fehler, 6 bestehende Warnungen |

Die automatisierten Voice-Tests simulieren Audio und Fokus. Sie belegen nicht
den vollständigen Ablauf mit realem Mikrofon, hörbarem TTS oder einem echten API-Konto.
Die 7 Assistant-Tests (`ProviderAndroidSmokeTest`, `StorageUiTest`, `AndroidVoiceTasksTest`)
und 7 DHU-/Auswahltests (`CarTemplateTest`, `ModelSelectionStoreTest`, `PhoneSmokeTest`)
wurden mit `adb shell am instrument -w` erneut ausgeführt: jeweils `OK (7 tests)`.
Die Tests verwenden isolierte Testdaten; sie starten keine echte Mikrofonaufnahme oder KI-Anfrage.
`am start -W` bestätigt anschließend den erfolgreichen Kaltstart der Assistenten-App (507 ms).
Die DHU-App bleibt auf 0.1.2-prototype / Code 3. Dies ist keine neue manuelle DHU-Sitzung.
Die unveränderten 14 Speichertests wurden für diese reine Audiofokus-Korrektur nicht wiederholt.

## Erhaltener Stand und Grenzen

Produktivcode und Konfiguration von `prototype-app`, `car-ui`, `core`, `providers`,
`storage` und `settings` sind gegenüber 0.1.5 unverändert. Die DHU-App erhält
keine Netzwerk-/Mikrofonberechtigung. Hostprüfung und deaktivierter DHU-Release
bleiben bestehen. Die bisherige manuelle DHU-Bestätigung gilt für 0.1.2.

Auto-Audio, Bluetooth-Routing und Betrieb bei gesperrtem Gerät bleiben ungeprüft.
Der HTTP-Transport garantiert keinen sofortigen Abbruch einer bereits begonnenen
Anfrage. Die übrige Speicheroberfläche arbeitet weiterhin synchron. Die App
beansprucht keine offiziell unterstützte Kategorie für allgemeine KI-Gespräche.

## Wiederholen

JDK 17 und Android SDK wie in [START_WINDOWS.md](START_WINDOWS.md) setzen.
Keine SDK-/Bibliotheksmigration und keine Datenmigration in dieser Korrektur.

```powershell
.\gradlew.bat :voice:test :core:test :providers:test
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
.\gradlew.bat :prototype-app:assembleDhuDebug :prototype-app:lintDhuDebug
# Mit verbundenem, entsperrtem Pixel und regulär beendeter DHU-Sitzung:
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :prototype-app:connectedDhuDebugAndroidTest
```

Installation und manueller Test: [VOICE.md](VOICE.md).
Historische Berichte: [0.1.5](history/BUILD_AND_TEST-0.1.5.md),
[0.1.4](history/BUILD_AND_TEST-0.1.4.md), [0.1.3](history/BUILD_AND_TEST-0.1.3.md),
[0.1.2](history/BUILD_AND_TEST-0.1.2.md).

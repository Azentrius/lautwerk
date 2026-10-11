# Build- und Testbericht 0.1.5

Stand: 12.10.2026. **72 automatisierte Tests bestanden:** 44 JVM-Tests und
28 Gerätetests auf Pixel 10 Pro / Android 17. Alle drei Debug-APKs sind gebaut.
Die Assistenten-App **0.1.5-dev / Code 6** ist installiert und startet erfolgreich.
Die DHU-/Phone-Testoberflächen bleiben auf **0.1.2-prototype / Code 3**.

| Prüfung | Ergebnis |
|---|---|
| `:voice:test` | 25 neue Tests: 15 Gesprächssteuerung, 8 Archiv-/Anbieteranbindung, 2 TTS-Textaufteilung |
| `:core:test` | 2 bestehende Tests erfolgreich |
| `:providers:test` | 17 bestehende Adapter-/Transporttests erfolgreich |
| `AndroidVoiceTasksTest` | 2 neue Tests: blockierende Arbeit außerhalb des Hauptthreads, Rückmeldung auf dem Hauptthread, keine späten Rückmeldungen nach Abbruch |
| `ProviderAndroidSmokeTest` / `StorageUiTest` | 5 bestehende Tests erfolgreich |
| Phone-, SelectionStore-, CarTemplate-Tests | 7 bestehende Tests erfolgreich; Auswahl und Starten bleiben funktionsfähig |
| `EncryptedStorageTest` | 14 bestehende Tests erfolgreich, mit separaten Testdaten und Keystore-Aliasen |
| Assistenten-, Phone-, DHU-Debug-APK | Build erfolgreich |
| `:app:lintDebug` | 0 Fehler, 7 Warnungen |
| `:prototype-app:lintDhuDebug` | 0 Fehler, 6 Warnungen |

Die 25 neuen JVM-Tests prüfen beide Anbieter, vollständigen Zuhören/Anfrage/Vorlesen-
Ablauf, doppelte Rückmeldungen, Stoppen und Neustart, verspätete Antworten, TTS-/
Erkennungsfehler, zweimalige Stille, genaue Sprachbefehle, getrennte Anbieter-Chats,
fehlende Schlüssel, Schreibfehler, Kontextbegrenzung, sichere Fehlertexte,
Zeitlimits und lange Texte mit Emoji. Die Android-Tests prüfen zusätzlich die
tatsächliche Thread-Grenze. Alle Anbieteraufrufe in Tests verwenden Fake-Transport
oder Fake-Client; Androids Mikrofon und TTS werden von den Tests nicht gestartet.

Eine ADB-Installation meldete zunächst einen Fehler ohne erklärenden Text.
Die direkte Paketprüfung zeigte anschließend bereits Version 0.1.5-dev / Code 6.
Nach regulärem Beenden der wieder aktiven DHU-Sitzung durch den Nutzer liefen
die Gerätetests erfolgreich. `am start -W` bestätigt einen erfolgreichen Kaltstart
der Assistenten-Activity. Keine Deinstallation und keine Löschung von Nutzerdaten.

## Praktischer Sprachtest

Noch keine Rückmeldung zum echten Sprachtest. Mikrofonaufnahme, hörbare Ausgabe, API-Kontozugriff und Stoppen während echter Audiowiedergabe sind noch nicht praktisch bestätigt.

Die ausführbaren manuellen Schritte, Grenzen von lokalem Stoppen und Datenfluss
stehen in [VOICE.md](../VOICE.md). Reales Audio-Routing, Nutzung bei gesperrtem Gerät
und Android-Auto-Audio sind nicht bestätigt. Die gültige Kategorie für allgemeine
KI-Gespräche in Android Auto ist weiterhin ungeklärt.

## Erhaltener Stand

`prototype-app`, `car-ui`, Modellkatalog, `providers`, `storage` und `settings`
bleiben im Produktivcode unverändert. Nur die Assistenten-App verwendet `voice`.
Im DHU-Paket gibt es weiterhin keine INTERNET-/RECORD_AUDIO-Berechtigung.
Signierte Hostprüfung, erklärter POI-Debug-Platzhalter und deaktivierter DHU-Release
bleiben bestehen. Die manuelle DHU-Bestätigung des Nutzers gilt für 0.1.2;
dieser Schritt bestätigt automatisierte Regression, keine neue manuelle DHU-Sitzung.

Historische Berichte: [0.1.4](BUILD_AND_TEST-0.1.4.md),
[0.1.3](BUILD_AND_TEST-0.1.3.md), [0.1.2](BUILD_AND_TEST-0.1.2.md).

## Lint und offene Punkte

Assistant: Versionshinweise, Backup-/Gerätetransfer-Regeln, unbenutzter app_name
und fehlendes Launcher-Icon. DHU: unverändert Versionshinweise, exportierter
Car-Service, Backup und Hintergrundfarbe. Ein Android-TTS-Callback erzeugt noch
eine Compilerwarnung zur Veraltung. Keine Lint-Fehler.

Der vorhandene HTTP-Transport garantiert keinen sofortigen Netzwerkabbruch.
Schlüssel-/Verlaufsbedienung außerhalb des Sprachdiensts bleibt synchron;
große Archive sowie Backup-/Gerätetransfer sind offen. Audiofokus- und Android-
Sprachadapter benötigen weiterhin den praktischen Gerätest. Der Dienst ist kein
Always-on-Assistent: begrenzte Sitzung, kein automatischer Neustart.

## Wiederholen

JDK 17 und Android SDK setzen wie in [START_WINDOWS.md](../START_WINDOWS.md).
Gradle 8.13, AGP 8.13.2, Kotlin 2.2.20 und compile/target SDK 36 bleiben erhalten.
DHU vor den UI-Tests regulär beenden, Pixel entsperrt und unbedient lassen.

```powershell
.\gradlew.bat :core:test :providers:test :voice:test :app:assembleDebug :app:lintDebug
.\gradlew.bat :prototype-app:assembleDhuDebug :prototype-app:assemblePhoneDebug :prototype-app:lintDhuDebug
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :prototype-app:connectedDhuDebugAndroidTest
.\gradlew.bat :storage:connectedDebugAndroidTest
```

In diesem Lauf wurden die APKs mit Gradle gebaut und die drei Gerätetestpakete
über `adb shell am instrument -w` ausgeführt. Die Ergebnisse waren `OK (7 tests)`,
`OK (7 tests)` und `OK (14 tests)`. Der ADB-Exitcode allein ist kein Testnachweis.

Nachtrag vom 12.10.2026: Der manuelle Sprachtest scheiterte wiederholt mit der Audiofokus-Meldung. Der Pixel-Fokusverlauf zeigt eine Kollision mit der gestarteten Google-Spracherkennung. Korrektur in 0.1.6; siehe [aktuellen Bericht](../BUILD_AND_TEST.md).

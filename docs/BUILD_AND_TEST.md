# Build- und Testbericht 0.1.2 – 11.10.2026

## Ergebnis

Gemeinsame Anbieter-/Modellauswahl auf dem bestehenden Projekt umgesetzt.
Alle drei Debug-APKs wurden gebaut; zwei JVM-Tests und sieben Gerätetests bestehen.
Die DHU-Test-APK 0.1.2 ist auf dem Pixel 10 Pro über 0.1.1 installiert.
Die neue Smartphone-Darstellung wurde per Screenshot geprüft. Der Nutzer hat
anschließend für 0.1.2 im echten DHU bestätigt: **„Ja, Auswahl und Starten funktionieren“**
(Claude / Anthropic, Claude Haiku 4.5 und „Gestartet · Test 1“).

| Prüfung | Ergebnis / Aussagegrenze |
|---|---|
| `:prototype-app:assembleDhuDebug` | Erfolgreich |
| `:prototype-app:assemblePhoneDebug` | Erfolgreich |
| `:app:assembleDebug` | Erfolgreich; bestehende App mit gemeinsamen Typen |
| `:core:test` | 2 Tests bestanden: stabile IDs/Defaults, ungültige Anbieter-/Modellpaare |
| `:prototype-app:assembleDhuDebugAndroidTest` | Erfolgreich |
| `:prototype-app:lintDhuDebug` | 0 Fehler, 6 Warnungen |
| ADB-Installation DHU-App und Testpaket | Jeweils `Success` |
| Instrumentierung auf Pixel 10 Pro / Android 17 | `OK (7 tests)`, Laufzeit 2,983 s |
| PhoneSmokeTest (2) | Starten/Neuerstellung; Modellwahl, Anbieterwechsel, Wiederherstellung bei Activity-Neuerstellung und erneutem Öffnen |
| ModelSelectionStoreTest (3) | Modelle je Anbieter, neuer Store; ungültige gespeicherte Werte; atomare/abmeldbare Beobachter |
| CarTemplateTest (2) | Titel, eine Starten-Aktion, echter ClickDelegate, Auswahltext; automatisches Aktualisieren und Rückkehr nach gestopptem Screen |
| Car-Service-Auflösung | `dev.openautoai.car.OpenAutoCarService` vorhanden |
| Smartphone-Darstellung | Auf dem Pixel geöffnet und sichtbar geprüft |
| Manueller Auswahl-/Starten-Test im echten DHU | Nutzer bestätigt: „Ja, Auswahl und Starten funktionieren“ |
| API-/Voice-End-to-End | Nicht ausgeführt; keine Schlüssel verwendet, keine API-Anfragen |

Die Tests isolieren ihre Preference-Daten bzw. stellen die vorherige Smartphone-
Auswahl wieder her. Das bereits bestehende Paket `dev.openautoai.app` wurde gebaut,
aber für diesen Schritt nicht installiert oder mit Daten/Sprachzugriff verwendet.

## Erhaltener DHU-Stand

Der Nutzer hat für **0.1.1** Launcher-Sichtbarkeit, Titel und „Gestartet · Test 1“
nach Starten im echten DHU bestätigt. Der vollständige ursprüngliche Bericht liegt
in [history/BUILD_AND_TEST-0.1.1.md](history/BUILD_AND_TEST-0.1.1.md).
In 0.1.2 bleiben Manifest-Registrierung, Paketname, Host-Signaturprüfung,
Car-App-Library-Version und deaktivierter DHU-Release-Build erhalten.
Die bestehenden Template-/Starten-Tests wurden um die Auswahl ergänzt.

Das DHU-Paket enthält weiterhin keine INTERNET- oder RECORD_AUDIO-Berechtigung.
Der POI-Testplatzhalter ist unverändert und keine Produkttyp-Zulassung.

## Umgebung und Warnungen

Windows; Pixel 10 Pro, Android 17; Android Auto `17.7.663654-release`.
JDK Temurin 17.0.19; Gradle 8.13; AGP 8.13.2; Kotlin 2.2.20;
compile/target SDK 36, Build Tools 35.0.0; Car App Library 1.8.0-rc01.
Lint meldet dieselben sechs Warnungen zu Testbibliotheksversionen, exportiertem
Car-Service, Backup-Regeln und doppelter Hintergrundfarbe. Im Assistant-Modul
bleiben bestehende Warnungen zu Permission-/TTS-Callbacks. Keine Buildfehler.
Ein Typfehler im neuen Testcode wurde vor dem erfolgreichen Lauf korrigiert.

## Wiederholen

Projektordner öffnen; JDK 17 / Android SDK setzen wie in [START_WINDOWS.md](START_WINDOWS.md).

```powershell
.\gradlew.bat :prototype-app:assembleDhuDebug :prototype-app:assemblePhoneDebug :app:assembleDebug
.\gradlew.bat :core:test :prototype-app:lintDhuDebug :prototype-app:connectedDhuDebugAndroidTest
```

Während UI-Tests das Pixel entsperrt lassen und keine andere App öffnen.
Der manuelle Auswahltest ist in [MODEL_SELECTION.md](MODEL_SELECTION.md) beschrieben.

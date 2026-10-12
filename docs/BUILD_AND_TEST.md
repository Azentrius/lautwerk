# Build- und Testbericht 0.1.4

Stand: 12.10.2026. **45 automatisierte Tests bestanden:** 19 JVM-Tests und
26 Gerätetests auf dem Pixel 10 Pro mit Android 17. Alle drei Debug-Varianten
sind gebaut. Die Assistenten-App 0.1.4-dev / Code 5 ist als Update installiert.
Die DHU-Test-App bleibt 0.1.2-prototype / Code 3.

| Prüfung | Ergebnis |
|---|---|
| `:core:test` | 2 Modell-/Provider-Tests bestanden |
| `:providers:test` | 17 bestehende Adapter-/Transporttests bestanden, ausschließlich simuliertes HTTP |
| `EncryptedStorageTest` | 14 Tests auf Android bestanden: altes Archiv-/Schlüsselformat, echte Keystore-Verschlüsselung, parallele Instanzen, beschädigte Daten, fehlende Schlüssel, Backup-Wiederherstellung, Schreibabbrüche, Fehlerbehandlung und gelöschte Chats |
| `ProviderAndroidSmokeTest` | 2 Tests bestanden: Android-JSON und bestehende Anbieter-Brücke |
| `StorageUiTest` | 3 Tests bestanden: Archivfehler und erneutes Laden, Schlüsselfehler ohne Voice-Start, lesbarer Altchat mit nicht mehr gelistetem Modell |
| Phone-, SelectionStore- und CarTemplate-Tests | 7 bestehende Gerätetests bestanden; Auswahl, Wiederherstellung und Starten geprüft |
| Assistenten-, Phone- und DHU-Debug-APK | Erfolgreich gebaut |
| `:app:lintDebug` | 0 Fehler, 8 Warnungen |
| `:storage:lintDebug` | 0 Fehler, 3 Warnungen |
| `:prototype-app:lintDhuDebug` | 0 Fehler, 6 Warnungen |
| Echte Anbieter-/Voice-End-to-End-Aufrufe | Nicht ausgeführt |

Die Speichertests laufen in `dev.openautoai.storage.test`, getrennt von der
Assistenten-App. Testdateien, Zugangsdaten und Keystore-Aliase sind künstlich und
werden danach entfernt. Die Oberflächentests injizieren Attrappen und starten
keinen Dienst. Die vorhandenen DHU-Auswahlwerte werden von den Tests gesichert
und danach wiederhergestellt. Es wurden keine persönlichen API-Schlüssel gelesen.

## Testumgebung und behobene Testprobleme

Die neuen Compose-Tests scheiterten zunächst in einer transitiven älteren
Espresso-Version an `InputManager.getInstance`. Das Assistenten-Testpaket verwendet
jetzt Espresso 3.7.0, Runner 1.7.0 und JUnit-Erweiterung 1.3.0. Die produktive
Compose-Version bleibt gleich. Google dokumentiert die Umstellung auf öffentliche
InputManager-/TestLooperManager-Schnittstellen in
[AndroidX Test 3.7.0](https://developer.android.com/jetpack/androidx/releases/test#espresso-3.7.0).

Ein erster DHU-Regressionslauf wurde von der aktiven Maps/Android-Auto-Sitzung
gestört: ein Auswahlvergleich schlug fehl, eine neu erstellte Activity blieb
gestoppt. Nach regulärem Beenden des DHU durch den Nutzer und mit entsperrtem
Pixel wurden die Gerätetests erneut erfolgreich ausgeführt. Keine Android-Auto-
Daten wurden gelöscht und keine Host-/Plattformsicherheitsprüfung abgeschaltet.

## DHU bleibt beim bestätigten Stand

Die Smartphone-/Car-Testoberflächen, Registrierung und Modellliste sind gegenüber
0.1.2 unverändert. `storage` und `providers` sind nur in `app` eingebunden.
Das DHU-Paket bleibt ohne INTERNET/RECORD_AUDIO. Titel, Auswahl und Starten hat
der Nutzer im vorherigen Schritt im echten DHU bestätigt. Dieser Schritt prüft
die Regression automatisiert; eine neue manuelle DHU-Bestätigung wird nicht behauptet.

POI-Debug-Platzhalter, signierte Hostprüfung und deaktivierter DHU-Release bleiben
erhalten. Es gibt weiterhin keine nachgewiesene zulässige Android-Auto-Kategorie
für einen allgemeinen KI-Assistenten. Historische Teststände:
[0.1.3](history/BUILD_AND_TEST-0.1.3.md), [0.1.2](history/BUILD_AND_TEST-0.1.2.md),
[0.1.1](history/BUILD_AND_TEST-0.1.1.md).

## Bekannte Grenzen

Lint meldet weiterhin Versionshinweise, Backup-/Gerätetransfer-Regeln, ein fehlendes
Assistant-Launcher-Icon, einen unbenutzten Namen und eine überflüssige SDK-Abfrage.
Im Speichermodul ist die Warnung zu synchronem `commit()` bewusst nachvollziehbar:
die Rückmeldung wird geprüft. Die zwei anderen Speicherwarnungen betreffen
Testbibliotheksversionen. DHU: Versionshinweise, exportierter Car-Service, Backup
und Hintergrundfarbe. Bestehende Permission-/TTS-Callbacks erzeugen Compilerwarnungen.

Speicherzugriff bleibt synchron und prozesslokal. Vollständiger API-/Voice-Ablauf,
Gerätewechsel, große Archive und Wiederherstellung bei Schlüsselverlust sind nicht
validiert. Details: [LOCAL_STORAGE.md](LOCAL_STORAGE.md) und
[PROVIDER_ADAPTERS.md](PROVIDER_ADAPTERS.md).

## Wiederholen

JDK 17 / Android SDK wie in [START_WINDOWS.md](START_WINDOWS.md) setzen. Das Projekt
verwendet weiterhin Gradle 8.13, AGP 8.13.2, Kotlin 2.2.20 und compile/target SDK 36.
Pixel entsperrt lassen, DHU regulär schließen und während UI-Tests nicht bedienen.
Bei mehreren Geräten `ANDROID_SERIAL` auf das gewünschte Gerät setzen.

```powershell
.\gradlew.bat :core:test :providers:test :app:assembleDebug :app:lintDebug :storage:lintDebug
.\gradlew.bat :prototype-app:assembleDhuDebug :prototype-app:assemblePhoneDebug :prototype-app:lintDhuDebug
.\gradlew.bat :storage:connectedDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :prototype-app:connectedDhuDebugAndroidTest
```

In diesem Lauf wurden die APKs mit Gradle gebaut, per `adb install -r` installiert
und die Instrumentierungen mit `adb shell am instrument -w` ausgeführt. Prüfen,
dass tatsächlich `OK (14 tests)`, `OK (5 tests)` und `OK (7 tests)` erscheinen;
ein ADB-Exitcode 0 allein belegt keinen erfolgreichen Testlauf.

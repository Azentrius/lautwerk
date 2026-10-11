# Build- und Testbericht 0.1.3

## Ergebnis

**28 automatisierte Tests bestanden:** 19 auf der JVM, 9 auf dem Pixel 10 Pro / Android 17.
Alle drei Debug-Varianten gebaut. Die Assistenten-App 0.1.3-dev und die DHU-Test-App
0.1.2-prototype wurden mit `adb install -r` installiert. Vorhandene Auswahlwerte
werden von den DHU-Gerätetests gesichert und danach wiederhergestellt.

| Prüfung | Ergebnis |
|---|---|
| `:providers:test` | 17 Tests bestanden: Serialisierung/Routing, Schlüsseltrennung, keine gespeicherte Provider-Historie, sichere HTTP-/Netzwerkfehler, Antwortstatus, UTF-8, Timeouts, Weiterleitungen, Größenlimit, Ressourcenfreigabe |
| `:core:test` | 2 bestehende Modell-/Provider-Tests bestanden |
| `:app:assembleDebug` | Erfolgreich, Version 0.1.3-dev / Code 4 |
| `:prototype-app:assembleDhuDebug` | Erfolgreich, weiterhin 0.1.2-prototype / Code 3 |
| `:prototype-app:assemblePhoneDebug` | Erfolgreich, weiterhin 0.1.2-prototype / Code 3 |
| Beide Android-Test-APKs | Erfolgreich gebaut und installiert |
| `ProviderAndroidSmokeTest` | `OK (2 tests)`, 0,022 s: beide Adapter mit Android-JSON und bestehendem Turn-Format; falsche Modell-/Provider-Paare abgefangen |
| Phone-, SelectionStore- und CarTemplate-Test | `OK (7 tests)`, 2,644 s: unveränderte Auswahl-/DHU-Regression erfolgreich |
| `:app:lintDebug` | 0 Fehler, 12 Warnungen |
| `:prototype-app:lintDhuDebug` | 0 Fehler, 6 Warnungen |
| Echte Anbieter-/Voice-End-to-End-Aufrufe | Nicht ausgeführt; nur Dummy-Schlüssel und simulierte Antworten |

Die API-Tests injizieren einen Fake-Transport; die Transporttests verwenden künstliche
Verbindungsobjekte. Sie prüfen Fehlerfälle und HTTP-Konfiguration ohne Netzwerkverkehr.
Die Gerätetests starten keine Aufnahme und verwenden keine gespeicherten API-Schlüssel.
Live-TLS, Kontozugriff, tatsächlich verfügbare Modelle und der Voice-Ablauf sind damit
nicht bestätigt. Grenzen und manueller Test: [PROVIDER_ADAPTERS.md](PROVIDER_ADAPTERS.md).

## DHU bleibt beim bestätigten Stand

Die Smartphone-/Car-Oberflächen und ihre Manifest-Registrierung wurden gegenüber
0.1.2 nicht geändert. `providers` ist nur eine Abhängigkeit von `app`, nicht von
`prototype-app` oder `car-ui`. Das DHU-Paket bleibt ohne INTERNET/RECORD_AUDIO.
Titel, Auswahl und Starten wurden im vorherigen Schritt vom Nutzer im echten DHU
bestätigt. In diesem Schritt wurden diese Funktionen automatisiert erneut geprüft;
es wird kein weiterer manueller Test behauptet.

Historische Bestätigungen: [0.1.2](history/BUILD_AND_TEST-0.1.2.md),
[0.1.1](history/BUILD_AND_TEST-0.1.1.md).
POI-Testplatzhalter, aktive Hostprüfung und deaktivierter DHU-Release bleiben erhalten.

## Lint / bekannte Grenzen

Assistant-Warnungen: neuere Gradle-/Bibliotheksversionen, Backup-Regeln, überflüssige
SDK-Abfrage, bislang ungenutzter app_name-Eintrag, fehlendes Launcher-Icon sowie
drei KTX-Hinweise. DHU: unverändert sechs Hinweise zu Testbibliotheken, exportiertem
Car-Service, Backup und Hintergrundfarbe. Die bestehenden Permission-/TTS-Callbacks
erzeugen weiterhin Compilerwarnungen. Kein SDK-/Bibliotheksupgrade in diesem Schritt.

Die Android-Varianten verwenden JDK 17, Gradle 8.13, AGP 8.13.2, Kotlin 2.2.20
und compile/target SDK 36. `providers` benötigt beim ersten Build zusätzlich
`org.json:json:20240303` zur JVM-Kompilierung und für Tests; Android liefert seine
eigene JSON-Laufzeit. Keine API-Schlüssel oder lokalen SDK-Dateien gehören ins ZIP.

## Wiederholen

JDK/SDK setzen wie in [START_WINDOWS.md](START_WINDOWS.md), dann:

```powershell
.\gradlew.bat :core:test :providers:test :app:assembleDebug :app:lintDebug
.\gradlew.bat :prototype-app:assembleDhuDebug :prototype-app:assemblePhoneDebug :prototype-app:lintDhuDebug
.\gradlew.bat :app:connectedDebugAndroidTest :prototype-app:connectedDhuDebugAndroidTest
```

Pixel entsperrt lassen und während der UI-Tests keine andere App öffnen.

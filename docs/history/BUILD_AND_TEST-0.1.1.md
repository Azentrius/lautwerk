# Build- und Testbericht – 11.10.2026

## Ergebnis

Der Nutzer hat nach Installation der DHU-Test-App bestätigt:
**„Ja, sichtbar“** auf die Frage nach „OpenAuto AI“ im DHU-App-Launcher.
Zusätzlich hat der Nutzer das Öffnen der App, den korrekten Titel und den Wechsel
zu „Gestartet · Test 1“ nach Antippen von „Starten“ bestätigt: **„Ja, beides funktioniert“**.
Damit sind Launcher-Sichtbarkeit und der einfache UI-Ablauf im echten DHU manuell bestätigt.
Das ist keine Freigabe für reale Fahrzeuge oder für eine allgemeine KI-App-Kategorie.

| Prüfung | Ergebnis / Aussagegrenze |
|---|---|
| `prototype-app:assembleDhuDebug` | Erfolgreich; signiertes Debug-APK erstellt |
| `prototype-app:assemblePhoneDebug` | Erfolgreich; Smartphone-Vergleichs-APK erstellt |
| `app:assembleDebug` | Erfolgreich; bestehende Assistant-App aus 0.1.0 weiter baubar |
| `prototype-app:assembleDhuDebugAndroidTest` | Erfolgreich |
| `prototype-app:lintDhuDebug` | 0 Fehler, 6 Warnungen |
| ADB-Installation `dev.openautoai.prototype.dhu` | `Success` auf Pixel 10 Pro |
| Android-Dienstauflösung | `dev.openautoai.car.OpenAutoCarService` gefunden |
| Smartphone-Oberfläche | Auf dem Pixel gestartet und per Screenshot sichtbar geprüft |
| PhoneSmokeTest | Bestanden: Starten ändert Status; Zustand nach Activity-Neuerstellung erhalten |
| CarTemplateTest | Bestanden: Titel, Starten-Aktion und Statuswechsel über echten ClickDelegate |
| Instrumentierung | `OK (2 tests)`, Laufzeit 62,22 s; auf realem Pixel ausgeführt |
| Echte DHU-Launcher-Sichtbarkeit | Vom Nutzer bestätigt: „Ja, sichtbar“ |
| Template-Öffnen und Starten im echten DHU | Vom Nutzer bestätigt: Titel korrekt, Starten wechselt zu „Gestartet · Test 1“ |
| API-/Sprach-End-to-End des Assistant-Moduls | Nicht getestet; keine API-Schlüssel eingegeben, keine bezahlten Aufrufe |

## Umgebung

- Windows, Android Studio und Android SDK vorhanden.
- Pixel 10 Pro, Android 17; Android Auto `17.7.663654-release`.
- compileSdk / targetSdk 36; Build Tools 35.0.0; JDK Temurin 17.0.19.
- Gradle 8.13, Android Gradle Plugin 8.13.2, Kotlin 2.2.20.
- Car App Library 1.8.0-rc01 (explizite Vorabversion mit dokumentiertem Sicherheitsfix).
- Eine DHU-Sitzung war bereits eingerichtet. Launcher, Titel und Starten-Statuswechsel wurden vom Nutzer bestätigt.

Die neuen Testpakete sind unabhängig von `dev.openautoai.app`. Die alte Assistant-App
wurde gebaut, aber nicht auf dem Pixel installiert oder mit Nutzerdaten verwendet.
Das separat installierte Android-Testpaket lautet `dev.openautoai.prototype.dhu.test`.
Es kann bei Bedarf über die normalen App-Einstellungen entfernt werden.

## Lint und bekannte technische Grenzen

Die sechs Lint-Warnungen betreffen drei neuere verfügbare Testbibliotheksversionen,
den bewusst exportierten Car-App-Service, Backup-Regeln und eine doppelte
Hintergrundfarbe. Der Service braucht den Export für den Android-Auto-Host;
seine signaturbasierte HostValidator-Prüfung bleibt aktiv. Im Sichtbarkeitsprototyp
werden keine Schlüssel, Mikrofonaufnahmen oder Chatverläufe gespeichert.

Im Altmodul bleiben Warnungen zu veralteten Permission-/TTS-Callbacks bestehen.
Eine native Compose-Bibliothek wird mit ihren vorhandenen Debug-Symbolen verpackt.
Das sind keine Buildfehler. Die Voice-/Provider-Funktionen wurden nicht fachlich erweitert.

Frühe Testläufe zeigten einen Robolectric-Konflikt in den Gerätetest-Abhängigkeiten.
Robolectric wurde aus dieser Instrumentierungs-Konfiguration ausgeschlossen; der
anschließende vollständige Gerätelauf bestand. Die Tests dürfen nicht als
Android-Auto-Produktzulassung interpretiert werden.

## Wiederholen

Projektordner in PowerShell öffnen und JDK 17 / Android SDK setzen, dann:

```powershell
.\gradlew.bat :prototype-app:assembleDhuDebug :prototype-app:assemblePhoneDebug :app:assembleDebug
.\gradlew.bat :prototype-app:lintDhuDebug :prototype-app:connectedDhuDebugAndroidTest
```

Während des UI-Tests das Pixel entsperrt lassen und keine anderen Seiten öffnen.
ADB-/DHU-Schritte und Fehlerinterpretation stehen in [START_WINDOWS.md](../START_WINDOWS.md).

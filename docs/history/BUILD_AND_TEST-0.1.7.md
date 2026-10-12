# Build- und Testbericht 0.1.7

Stand: 12.10.2026. **75 automatisierte Prüfungen bestanden:** 57 JVM-Tests und
18 Gerätetests (11 Assistant, 7 DHU/Auswahl). Assistant **0.1.7-dev / Code 8**
ist als Update auf dem Pixel 10 Pro / Android 17 installiert. Keine Datenlöschung.
DHU-/Phone-Oberfläche bleibt **0.1.2-prototype / Code 3**.

## Bestätigter Ausgangspunkt und neue Funktion

Der Nutzer hat Erkennung, Vorlesen und Beenden für OpenAI und Claude unter 0.1.6 bestätigt: „Es funktioniert alles, die Stimme ist allerdings furchtbar.“ Für die Stimmwahl unter 0.1.7 meldet der Nutzer: „Klingt definitiv besser.“ Er fragt nach ähnlich klingenden Einträgen; die Stimmenkennungen zeigen vier lokale/Online-Variantenpaare. Eine gesonderte Bestätigung aller Vorschau-Abbruchfälle liegt noch nicht vor.

Die neue Smartphone-Seite bietet Stimmwahl, drei Sprechgeschwindigkeiten und eine
abbrechbare Hörprobe mit festem Text. Die Auswahl gilt für beide Anbieter und ist
in separaten nicht geheimen App-Einstellungen gespeichert. Systemweite TTS-Einstellungen,
API-Schlüssel und Chatverläufe werden nicht verändert. Online-Stimmen werden vor
dem Speichern ausdrücklich bestätigt. Fehlende Stimmen werden vor Gesprächsbeginn gemeldet.

## Ausgeführte Prüfungen

| Prüfung | Ergebnis |
|---|---|
| `:voice:test` | 38 bestanden; Gesprächsablauf, Fokus, Abbruch, Anbieteranbindung, Textaufteilung |
| `:providers:test` / `:core:test` | 17 / 2 bestanden |
| Bestehende Assistant-Gerätetests | 7 bestanden |
| `SpeechSettingsTest` | 3 bestanden: getrennte Speicherung und ungültige Raten; Sprach-/Installationsfilter; echte lokale TTS-Stimme setzen und fehlende Stimme/falschen Dienst ablehnen |
| `SpeechSettingsScreenTest` | 1 bestanden: Seite lädt und öffnet Stimmwahl, ohne Hörprobe zu starten oder Auswahl zu ändern |
| DHU-/Auswahl-Gerätetests | 7 bestanden; Car-Template, Auswahl, Starten |
| Debug-APK / Test-APK | Build erfolgreich, Update installiert |
| `:app:lintDebug` | 0 Fehler, 9 Warnungen: 7 bestehende Hinweise plus 2 KTX-Vorschläge für SharedPreferences |

Der echte Google-Sprachdienst meldet auf dem Pixel **5 lokale und 4 Online-Stimmen
für de-DE**. Das Setzen einer lokalen Stimme wurde am echten TTS-Objekt geprüft.
Es wurde dabei nichts hörbar abgespielt. Die Kennungen `dea`, `deb`, `deg` und `nfh` erscheinen jeweils als lokale und Netzwerkvariante; der neunte Eintrag ist `de-DE-language`. Der Nutzer hat den Klang der Stimmwahl als deutlich besser bestätigt.
Die automatisierten Tests bewerten keine subjektive Klangqualität. Die automatisierten KI-Tests verwenden
künstliche Antworten. Unveränderte Speichertests wurden nicht wiederholt.

Der PC stürzte während der Arbeit ab. Das erste Protokoll mit zehn erfolgreichen
Assistant-Tests war vollständig erhalten. Danach wurden Build und Gerätetests
einschließlich des neuen Oberflächentests abgeschlossen. Der Quellcode wurde
weiterverwendet; kein Projekt wurde neu erzeugt und keine App-Daten zurückgesetzt.

## Erhaltener Stand und Grenzen

`prototype-app`, `car-ui`, `core`, `providers`, `storage` und `settings` bleiben
gegenüber 0.1.6 unverändert. Keine neuen DHU-Berechtigungen, keine geänderte
Hostprüfung oder behauptete KI-Kategorie. Die manuelle DHU-Bestätigung gilt für
0.1.2; dieser Lauf prüft die vorhandenen Templates automatisiert.

Fokus wird weiterhin nur beim Vorlesen beansprucht. Vorschau und Gespräch
verwenden dieselbe Stimmenkonfiguration; die Vorschau hat weder Mikrofon- noch
Anbieteranbindung. Beim Hintergrundwechsel wird die Vorschau gestoppt. Online-
Stimmen und Android-Standard sind keine Offline-Zusage. Klang und Verfügbarkeit
hängen vom Android-Sprachdienst ab; es wurde keine zusätzliche Cloud-Sprach-API eingebaut.

Auto-Audio, Bluetooth-Routing und gesperrtes Gerät sind weiterhin ungeprüft.
Der bestehende HTTP-Transport garantiert keinen sofortigen Abbruch bereits
begonnener Anfragen. Die übrige Speicheroberfläche arbeitet weiter synchron.

## Wiederholen

JDK 17 und SDK wie in [START_WINDOWS.md](../START_WINDOWS.md) setzen.
Für UI-Tests DHU regulär beenden und Pixel entsperrt lassen.

```powershell
.\gradlew.bat :voice:test :providers:test :core:test
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :prototype-app:connectedDhuDebugAndroidTest
```

In diesem Lauf wurden die gebauten Testpakete mit `adb shell am instrument -w`
ausgeführt: `OK (11 tests)` und `OK (7 tests)`.
Stimmwahl, Installation und manueller Test: [VOICE.md](../VOICE.md).
Historische Berichte: [0.1.6](BUILD_AND_TEST-0.1.6.md),
[0.1.5](BUILD_AND_TEST-0.1.5.md), [0.1.4](BUILD_AND_TEST-0.1.4.md).

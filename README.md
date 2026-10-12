# OpenAuto AI 0.1.15 – App-Symbol und Startbild

Aufbauend auf dem gelieferten Projekt und dem bestätigten DHU-Prototyp verwendet
die Smartphone-App jetzt einen gemeinsamen Sprachablauf für beide Anbieter:
Android-Spracherkennung, vorhandener Text-Adapter und Android-Sprachausgabe.
Starten, Stoppen, verspätete Rückmeldungen, Fehler und Anbieterwechsel werden
von einer getrennt testbaren Gesprächssteuerung behandelt. Chats und Schlüssel
bleiben im bestehenden verschlüsselten Format erhalten.

**Neu in 0.1.15:** Das freigegebene App-Symbol „Sprechblase mit Punkt“ aus
Design-Commit `9ed4032` ist jetzt in Assistent und Prototyp eingebunden,
einschließlich Monochrom-Ebene, nativem Startbild auf Tinte und kleinem
Benachrichtigungssymbol. Die 11 Android-Dateikopien entsprechen der Quelle bytegenau.

**Assistent 0.1.15-dev / Code 15 und DHU 0.1.15-prototype / Code 6 sind auf dem
Pixel installiert.** Alle 28 ausgewählten Gerätetests bestehen. Build und Lint
sind erfolgreich (0 Fehler). App-Symbol, Startbild, vorhandene Smartphone-Seiten
sowie DHU-Touch/Rotary und sechs Starts ohne Reset sind geprüft.

Die DHU-Oberfläche, B2/B3 und ihre Funktionen bleiben erhalten; der Host stellt
die Startaktion weiterhin hellblau dar. Anruf-, Headset- und Bluetooth-Tests
bleiben zurückgestellt. Die Phone-Vergleichsvariante wurde als 0.1.15-prototype /
Code 5 gebaut. Voice bleibt ausschließlich in der Assistenten-App.

Details: [App-Symbol und Installation](docs/APP_IDENTITY.md),
[aktueller Testbericht](docs/BUILD_AND_TEST.md),
[DHU-Abnahme 0.1.14](docs/history/BUILD_AND_TEST-0.1.14.md),
[Smartphone-Design](docs/DESIGN_SYSTEM.md),
[Design-Referenz](design/azentrius/README.md).

## Sideload und reale Fahrzeuge

Seit 12.10.2026 prüfen wir auf Nutzerwunsch eine Verteilung außerhalb von Google Play.
Auf Nutzerwunsch testen wir zuerst die native drahtlose Verbindung im ID.7 mit der
bereits installierten APK. Die Versuche im Kia mit AAWireless sind zurückgestellt.
Ein funktionierender Weg für OpenAuto AI ist noch nicht bestätigt.
Die [Sideload-Anleitung](docs/SIDELOADING.md) und der
[Fahrzeugtestbericht](docs/SIDELOAD_TEST_REPORT.md) unterscheiden Vorbereitung,
Beobachtungen und offene Versuche. App und DHU bleiben auf 0.1.15.

## Schnellstart unter Windows

1. Dieses Projekt in Android Studio öffnen, Gradle-JDK auf **JDK 17** stellen.
2. SDK Platform **Android 17 / API 37** und Build Tools **36.0.0** installieren, falls sie fehlen.
3. Build-Variante `prototype-app: dhuDebug` wählen; diese APK enthält Smartphone-UI UND Auto-Service.
4. Die Schritte in [docs/START_WINDOWS.md](docs/START_WINDOWS.md) ausführen.

`OpenAuto AI`, Anbieter-/Modellauswahl und `Starten` erscheinen auf dem Smartphone. Im Auto-Template
ändert Starten den Text von „Bereit für den UI-Test.“ zu „Gestartet · Test 1“.
Es gibt in diesem Test keine Netzwerk- oder Mikrofonberechtigung und keine API-Aufrufe.

## Drei getrennte Ergebnisse

| Modul / Variante | Paket | Zweck |
|---|---|---|
| `prototype-app:dhuDebug` | `dev.openautoai.prototype.dhu` | Minimaler Smartphone-Test plus experimenteller Car-App-Service |
| `prototype-app:phoneDebug` | `dev.openautoai.prototype.phone` | Vergleichs-App ohne Auto-Registrierung |
| `app:debug` | `dev.openautoai.app` | Fortgeführte Smartphone-App aus 0.1.0 mit Anbietern, Verlauf und Voice |

Die Testpakete ersetzen die alte App nicht und übernehmen keine ihrer Daten.
Smartphone und DHU innerhalb von `dev.openautoai.prototype.dhu` teilen ihre
Auswahl. Die drei getrennten APKs nutzen denselben Auswahlcode, haben aber
jeweils einen eigenen privaten Speicher. Der Modellkatalog enthält die fünf
IDs aus 0.1.0; die Auswahl prüft keinen tatsächlichen API-Kontozugriff.
Phone- und Auto-Testzähler sind unabhängig, nur im Speicher; auf dem Smartphone
bleibt der Testzähler bei Activity-Neuerstellung erhalten. Keine persistente Testsitzung.

## Kategorie: bewusst begrenztes Experiment

**OpenAuto AI ist keine POI-App und hat keine nachgewiesene zulässige
Android-Auto-Kategorie für allgemeine KI-Gespräche.**

Ausschließlich `dhuDebug` deklariert `androidx.car.app.category.POI` als
Template-Registrierung für diesen lokalen UI-Test. Das ist ein technischer
Testplatzhalter, keine Aussage über Produktzulässigkeit. Es werden keine Orte,
Navigation oder eine vermeintlich offizielle KI-Kategorie vorgetäuscht.
Der Build `dhuRelease` ist deaktiviert. Dieses Testgerüst ist nicht für eine
Play-Veröffentlichung oder Nutzung während der Fahrt bestimmt.

Android Auto kann die App trotz erfolgreichem Build und ADB-Installation
ablehnen oder ausblenden. Die eigene HostValidator-Prüfung bleibt aktiviert
und nutzt die signierten Host-Einträge der AndroidX-Bibliothek. Sie gewährt
umgekehrt keine Zulassung durch Android Auto. Es gibt weder Installer-Spoofing,
Root-Patches noch Abschaltung von Host-Prüfungen.

Googles Option „Unbekannte Quellen“ gilt laut Testdokumentation ausdrücklich
nicht als Freischaltung für Car-App-Library-Apps. Die technische Verfügbarkeit
außerhalb von Google Play wird nun im dokumentierten Fahrzeugvergleich geprüft.
Das begründet keine Play-Zulässigkeit; interne Play-Tests wären ebenfalls keine
Zusage der Kategoriezulässigkeit.

## Bausteine und spätere Arbeit

- `core`: Provider-/Modelltypen, validierte Auswahl, Chat-/Speicherschnittstellen und UI-Testzähler.
- `providers`: OpenAI-/Claude-Adapter, austauschbarer HTTP-Transport und sichere Fehlerzuordnung; nur in der Assistenten-App eingebunden.
- `storage`: verschlüsselte Chats und Zugangsdaten, prozessweite Sperren, feste Fehler und kompatibles Archivformat.
- `settings`: app-private Speicherung der Auswahl, pro Anbieter ein Modell,
  Beobachtung von Änderungen während der UI-Lebensdauer. Keine Schlüssel/Chatdaten.
- `voice`: gemeinsame Gesprächssteuerung, Worker-/Audio-Schnittstellen, Archiv-/Anbieteranbindung und TTS-Textaufteilung.
- `app`: Android-Sprachadapter, Mikrofon-Vordergrunddienst und fortgeführte Compose-App mit gemeinsamem Katalog und Auswahlstore.
  Die Namen `OPENAI`/`CLAUDE`, Schlüsselkennungen und Archivformate bleiben erhalten.
- `car-ui`: bestehender Car-App-Service mit Hostprüfung, MessageTemplate,
  Starten-Aktion und aktueller Auswahl als Text.
- `prototype-app`: Smartphone-Auswahl und zwei Testvarianten; keine API-/Audio-Aufrufe.

Weitere Arbeit: Cloud-Ziele des Textexports prüfen, verschlüsseltes Backup/Import und
Größenregeln für sehr große Archive planen. Anruf- und Headset-Tests bleiben zurückgestellt.
Ein Anbieterwechsel im bisherigen VoiceService verwendet jetzt das pro Anbieter
gemerkte Modell und beginnt weiterhin einen neuen Chat ohne fremden Verlauf.
Den jeweils bestätigten Sprachteststand beschreibt [VOICE.md](docs/VOICE.md). Weitere Planung: [ROADMAP.md](ROADMAP.md).

## Build-Werkzeuge

Gradle 9.8.1 mit vollständigem Wrapper und geprüfter Distribution; AGP 9.4.1 mit
eingebauter Kotlin-Unterstützung; Kotlin 2.4.21; JDK 17; compile SDK 37, target SDK 36;
Build Tools 36.0.0. Android 17 wird als Laufzeitsystem getestet; target SDK 37 folgt
erst nach einem Gerätetest der Android-17-Verhaltensänderungen.
Car App Library **1.8.0-rc01**, bewusst als Vorabversion fest angeheftet:
Google nennt dafür einen Sicherheitsfix vom 26.08.2026. Nicht als Stable bezeichnet.
Compose-BOM 2026.09.00 (verlangt compile SDK 37 und AGP 9.1 oder neuer); Activity 1.12.0 bleibt erhalten.

Siehe [docs/BUILD_AND_TEST.md](docs/BUILD_AND_TEST.md) für tatsächlich ausgeführte
Prüfungen und offene Grenzen, sowie [docs/CHANGES_FROM_0.1.0.md](docs/CHANGES_FROM_0.1.0.md).
Die ursprünglichen README-/Roadmap-Dateien liegen als historische, nicht aktuelle
Beschreibungen unter `docs/history/`.

## GitHub

[Automatische Prüfungen](.github/workflows/android.yml) bauen die Debug-Varianten,
führen JVM-Tests und Lint aus. Keine API-Schlüssel oder Live-Anfragen in CI.

Quellen, Wrapper, Lizenz, Dokumentation und Tests gehören ins Repository.
`local.properties`, `.gradle`, Build-Verzeichnisse, Schlüssel und Diagnoseausgaben
sind ausgeschlossen. Zielrepository: [Azentrius/openauto-ai](https://github.com/Azentrius/openauto-ai).
Der Standardbranch ist `main`; APKs sind nicht Bestandteil der Git-Historie.
Die bereits gelieferte Apache-2.0-Lizenz bleibt erhalten; siehe [LICENSE](LICENSE).

## Offizielle Referenzen (geprüft am 11.10.2026)

- [Car App Library und Kategorien](https://developer.android.com/training/cars/apps/library)
- [DHU: ADB-Tunnel und Testbetrieb](https://developer.android.com/training/cars/testing/dhu)
- [Testverteilung und Grenze von „Unbekannte Quellen“](https://developer.android.com/training/cars/testing)
- [AndroidX Car App Release Notes](https://developer.android.com/jetpack/androidx/releases/car-app)
- [AGP 9.4-Kompatibilität](https://developer.android.com/build/releases/agp-9-4-0-release-notes)

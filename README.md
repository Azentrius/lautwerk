# OpenAuto AI 0.1.1 – DHU-Sichtbarkeitstest

Fortsetzung des gelieferten `OpenAutoAI_0.1.0_Quellcode.zip` vom 11.10.2026.
Die sieben ursprünglichen Kotlin-Dateien bleiben im Modul `app` erhalten.
Der kleine Sichtbarkeitstest liegt separat in `prototype-app`; `car-ui` liefert
CarAppService, Session und die echte Car-App-Library-Template-Oberfläche.

**Bestätigter Teststand:** Alle drei Debug-APKs bauen; beide Gerätetests bestehen.
Der Nutzer bestätigt im echten DHU Launcher-Sichtbarkeit, korrekten Titel und den
Statuswechsel nach „Starten“. Siehe [Testbericht](docs/BUILD_AND_TEST.md).

## Schnellstart unter Windows

1. Dieses Projekt in Android Studio öffnen, Gradle-JDK auf **JDK 17** stellen.
2. SDK Platform **Android 16 / API 36** und Build Tools **35.0.0** installieren, falls sie fehlen.
3. Build-Variante `prototype-app: dhuDebug` wählen; diese APK enthält Smartphone-UI UND Auto-Service.
4. Die Schritte in [docs/START_WINDOWS.md](docs/START_WINDOWS.md) ausführen.

`OpenAuto AI` und `Starten` erscheinen auf dem Smartphone. Im Auto-Template
ändert Starten den Text von „Bereit für den UI-Test.“ zu „Gestartet · Test 1“.
Es gibt in diesem Test keine Netzwerk- oder Mikrofonberechtigung und keine API-Aufrufe.

## Drei getrennte Ergebnisse

| Modul / Variante | Paket | Zweck |
|---|---|---|
| `prototype-app:dhuDebug` | `dev.openautoai.prototype.dhu` | Minimaler Smartphone-Test plus experimenteller Car-App-Service |
| `prototype-app:phoneDebug` | `dev.openautoai.prototype.phone` | Vergleichs-App ohne Auto-Registrierung |
| `app:debug` | `dev.openautoai.app` | Fortgeführte Smartphone-App aus 0.1.0 mit Anbietern, Verlauf und Voice |

Die neuen Testpakete ersetzen die alte App nicht und übernehmen keine ihrer Daten.
Phone- und Auto-Testzustand sind unabhängig, nur im Speicher; auf dem Smartphone
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
nicht als Freischaltung für Car-App-Library-Apps. Ein zulässiger zukünftiger
Produktumfang muss zuerst geklärt werden. Interne Play-Tests wären danach ein
offizieller Verteilungsweg, aber keine Zusage der Kategoriezulässigkeit.

## Bausteine und spätere Arbeit

- `app`: vorhandene `Provider`, `ChatApi`, `SecretStore`, `ConversationStore`,
  `VoiceService`, `AppState` und Compose-`MainActivity` bleiben zusammen erhalten.
- `core`: kleiner unabhängiger Kotlin-Zustand für den UI-Test.
- `car-ui`: Car-App-Service, Hostprüfung, Session, MessageTemplate und Starten-Aktion.
- `prototype-app`: Zusammenbau und zwei Testvarianten.

Nächster Architektur-Schritt nach einem nachgewiesenen DHU-Ergebnis:
`Provider`/Modelle als gemeinsame Domänentypen auslagern; `ChatApi` hinter
Anbieter-Adaptern (OpenAI/Claude), Verlauf hinter einem Repository und Voice
hinter einer eigenen Schnittstelle integrieren. Die jetzige Providerliste ist
unverifiziert aus 0.1.0 übernommen. Keine API-/Audio-Kompatibilität wird durch
den UI-Test bestätigt. Einzelheiten: [ROADMAP.md](ROADMAP.md).

## Build-Werkzeuge

Gradle 8.13 mit vollständigem Wrapper und geprüfter Distribution; AGP 8.13.2;
Kotlin 2.2.20; JDK 17; compile/target SDK 36. Android 17 wird als Laufzeitsystem
getestet; dafür muss dieser Prototyp keine API-37-Funktion verwenden.
Car App Library **1.8.0-rc01**, bewusst als Vorabversion fest angeheftet:
Google nennt dafür einen Sicherheitsfix vom 26.08.2026. Nicht als Stable bezeichnet.
Für den vorhandenen SDK-36-Build wird Compose-BOM 2025.08.01 verwendet; Activity 1.12.0 bleibt erhalten. Die ursprüngliche BOM 2026.09.00 verlangt SDK 37/AGP 9.1 oder neuer.

Siehe [docs/BUILD_AND_TEST.md](docs/BUILD_AND_TEST.md) für tatsächlich ausgeführte
Prüfungen und offene Grenzen, sowie [docs/CHANGES_FROM_0.1.0.md](docs/CHANGES_FROM_0.1.0.md).
Die ursprünglichen README-/Roadmap-Dateien liegen als historische, nicht aktuelle
Beschreibungen unter `docs/history/`.

## GitHub

Quellen, Wrapper, Lizenz, Dokumentation und Tests gehören ins Repository.
`local.properties`, `.gradle`, Build-Verzeichnisse, Schlüssel und Diagnoseausgaben
sind ausgeschlossen. Zielrepository: [Azentrius/openauto-ai](https://github.com/Azentrius/openauto-ai).
Der Quellcode liegt auf `main`; APKs sind nicht Bestandteil der Git-Historie.
Die bereits gelieferte Apache-2.0-Lizenz bleibt erhalten; siehe [LICENSE](LICENSE).

## Offizielle Referenzen (geprüft am 11.10.2026)

- [Car App Library und Kategorien](https://developer.android.com/training/cars/apps/library)
- [DHU: ADB-Tunnel und Testbetrieb](https://developer.android.com/training/cars/testing/dhu)
- [Testverteilung und Grenze von „Unbekannte Quellen“](https://developer.android.com/training/cars/testing)
- [AndroidX Car App Release Notes](https://developer.android.com/jetpack/androidx/releases/car-app)
- [AGP 8.13-Kompatibilität](https://developer.android.com/build/releases/agp-8-13-0-release-notes)

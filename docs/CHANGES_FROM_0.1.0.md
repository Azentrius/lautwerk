# Änderungen gegenüber 0.1.0

## Herkunft

Ausgangsdatei: `OpenAutoAI_0.1.0_Quellcode.zip`, vom Nutzer am 11.10.2026 bereitgestellt.
SHA-256: `01ac3818fe2d174461c8a5303e4b1a5afe15e6f9a7c97bf61737b1deca0010b8`.

Der ursprüngliche Chat lieferte zunächst nur Inhaltsreferenzen ohne Dateien.
Sobald das ZIP vorlag, wurde der Altbestand untersucht und in dieses Projekt
übernommen. Das vorhandene Modul heißt weiterhin `app`, Paket `dev.openautoai.app`.
Es ist kein Ersatz der alten App durch eine reine Hello-World-App.

## Erhaltener Kotlin-Code

Hashvergleich mit den ZIP-Dateien:

| Datei in `app/src/main/java/dev/openautoai/app/` | Ergebnis |
|---|---|
| AppState.kt | Bytegenau unverändert |
| ChatApi.kt | Bytegenau unverändert |
| ConversationStore.kt | Bytegenau unverändert |
| Provider.kt | Bytegenau unverändert |
| SecretStore.kt | Bytegenau unverändert |
| VoiceService.kt | Bytegenau unverändert |
| MainActivity.kt | Zwei Kompilierkorrekturen, sonst erhalten |

MainActivity-Korrekturen:
1. Defektes Anführungszeichen im Text mit dem Sprachbefehl „Stopp“ repariert.
2. `permissions: Array<out String>` auf `Array<String>` an die Signatur der
   verwendeten AndroidX ComponentActivity angepasst.

Keystore-Aliase, Archivformat, API-Endpunkte, Anbieterlogik und Sprachablauf wurden
nicht geändert. Das ist eine Bestandswahrung, keine Validierung der Anbieter-APIs
oder eine vollständige Sicherheitsprüfung des Altstands.

## Build und Projektstruktur

- Fehlenden vollständigen Gradle-Wrapper ergänzt, inklusive JAR, `.bat`, Shell-Skript
  und festem SHA-256 der Gradle-Distribution.
- Reproduzierbare Toolchain: Gradle 8.13 / AGP 8.13.2 / Kotlin 2.2.20 / JDK 17.
- SDK 36 statt des im Original geforderten SDK 37: Auf diesem Rechner ist API 36
  installiert; der Quellcode benötigt keine API-37-Funktion. Ziel-SDK bleibt 36.
- Die Compose-BOM 2026.09.00 fordert SDK 37 und AGP >= 9.1. Sie wurde deshalb für
  diesen SDK-36-Build durch BOM 2025.08.01 ersetzt; Activity Compose bleibt 1.12.0.
- Version des Assistant-Moduls: `0.1.1-dev`, versionCode 2; ursprüngliche applicationId erhalten.
- Zusätzliche Module `prototype-app`, `car-ui` und `core`; klar getrennte phone-/dhu-Varianten.
- `dhuRelease` deaktiviert; POI-Deklaration ausschließlich im erklärten Debug-Testgerüst.
- Tests für Smartphone-Start und Wiederherstellung sowie Car-Template/Klick-Callback.
- Apache-2.0-Lizenz übernommen, ursprüngliche Dokumentation unter `docs/history/` erhalten.

## Nicht geändert bzw. noch offen

- Keine KI-/Voice-Integration in den Auto-Sichtbarkeitstest.
- Keine verifizierte Kategorie für einen allgemeinen KI-Assistenten.
- Keine Veröffentlichung, keine Google-Play-Einreichung, kein GitHub-Remote/Push.
- Keine Behauptung, dass der erfolgreiche DHU-Launcher-Test reale Fahrzeugfreigabe bedeutet.

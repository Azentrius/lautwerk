# Build- und Testbericht 0.1.13

Stand: 12.10.2026. Assistant **0.1.13-dev / Code 14** auf dem Pixel 10 Pro mit
Android 17 installiert. DHU-/Phone-Prototyp unverändert **0.1.12-prototype / Code 4**.
Die Umsetzung folgt dem freigegebenen Design-Paket und der Reiter-Ergänzung
`Azentrius/design-system@ab59a45`; siehe [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md).

## Ergebnisse

| Prüfung | Ergebnis |
|---|---|
| Core-JVM | 22 bestanden; unveränderter Code, lokaler Gradle-Lauf up-to-date |
| Anbieter-JVM | 21 bestanden; unveränderter Code, lokaler Gradle-Lauf up-to-date |
| Gesprächs-JVM | 45 bestanden; unveränderter Code, lokaler Gradle-Lauf up-to-date |
| Assistant auf Pixel | 73 verschiedene Tests im abschließenden Gesamtlauf bestanden |
| DHU-/Auswahltests auf Pixel | 8 bestanden: Auswahl, Titel, Start-Aktion, Activity-Neuerstellung und englisches Template |
| Builds | Assistant-Debug, Assistant-Test, DHU-Debug, DHU-Test und Phone-Debug erfolgreich |
| Android-Lint | 0 Fehler, 30 Warnungen, 1 Hinweis |
| Visuelle Prüfung | Helles Deutsch, dunkles Englisch, Exportdialog, Stimmwahl und 200 % Schrift mit Testdaten |

Zusammen **169 verschiedene automatisierte Tests**. Wiederholte Läufe zählen nicht
zusätzlich. Die ersten 71 Smartphone-Tests bestanden. Ein ergänzter Reiter-Test
beanstandete zunächst Compose-Rundung zwischen Fließkomma-Textbreite und ganzzahliger
Layoutgröße. Der abschließende Test vergleicht den verfügbaren Platz, vollständige
Zeilenenden und unbeschnittene Reitergrenzen. Alle drei Anordnungen bestehen.
Eine weitere Prüfung bestätigt das Scrollen und Schließen der lokalen Lizenzen.

Die 12 zusätzlichen Tests sichern Präsentation mit Start/Beenden, Exportphasen,
deaktivierten Aktionen, Online-Stimmen-Bestätigung, Screenreader-Nachrichten,
Datumsformaten, Reiter-Anordnung, Lizenzen und drei visuellen Szenarien ab.
Bestehende Exporttests prüfen zusätzlich die neuen Anzeigen. Die 19 zusätzlichen
Lint-Warnungen betreffen importierte, noch ungenutzte Design-Ressourcen und zwei
Compose-Modifier-Konventionen. Bestehende Warnungen bleiben dokumentiert; keine Lint-Fehler.

## Isolation und Grenzen

Tests verwenden erfundene Chats/Schlüssel und getrennte Speicherattrappen.
Kein echtes Anbieter-Konto, Mikrofon, hörbare TTS, Anruf oder Headset-Test gestartet.
Die Gerätesprache und globale Schriftgröße wurden nicht verändert: der Debug-Testhost
verwendet eigene Konfigurationen. Ein Speichervorschlag des Passwortmanagers für eine erfundene Testeingabe wurde
ohne Speichern geschlossen; die Stimmenseite wurde anschließend separat geprüft.
Die Produktions-App und die Systemeinstellung des Passwortmanagers bleiben unverändert.

Build/Tests der Car-App sind keine neue Sichtprüfung einer echten DHU-Verbindung.
Das bestätigte Car-Template, seine Aktionen, Kategorie-Deklaration und Hostprüfung
sind gegenüber 0.1.12 unverändert. Auto-Audio wird nicht eingeführt.

Ein realer Gesprächsdurchlauf mit beiden Anbietern wurde für 0.1.12 vom Nutzer
bestätigt, für 0.1.13 noch nicht erneut manuell bestätigt. TalkBack/D-Pad und
Rotation im externen Dateidialog sind nicht vollständig praktisch abgenommen.
Cloud-Exportziele sowie **Anruf- und Headset-/Bluetooth-Tests bleiben zurückgestellt**.

Der vorherige Testbericht liegt unter [history/BUILD_AND_TEST_0.1.12.md](history/BUILD_AND_TEST_0.1.12.md).

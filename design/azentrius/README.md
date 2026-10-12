# Azentrius: lokale Design-Referenz

Aktueller Referenzstand ist [Commit 9ed4032](https://github.com/Azentrius/design-system/tree/9ed403273e10662d21a6af1cd1fd08e965c846e0)
vom Repository Azentrius/design-system. Die vollständige, unveränderte Kopie liegt
unter [upstream-9ed4032/](upstream-9ed4032/README.md). Herkunft und Dateiprüfsummen:
[UPSTREAM.json](UPSTREAM.json), [UPSTREAM.sha256](UPSTREAM.sha256).

## Aktuelle Vorgaben und Dateien

- [Android Auto](upstream-9ed4032/android-auto.md): Host-Farben und Hintergrund
  sowie die Messungen der DHU-Abnahme 0.1.14; B2/B3 sind unverändert.
- [Android](upstream-9ed4032/android.md): einschließlich App-Symbol, Startbild und
  Benachrichtigung. O1 wurde mit [007f587](https://github.com/Azentrius/design-system/commit/007f587384ffe53e21c61ff1accfbe053dcf1309)
  entschieden: Sprechblase mit Punkt.
- Die gelieferten Android-Ressourcen liegen in
  `upstream-9ed4032/android/apps/openauto-ai/res/`, SVG-/PNG-Vorlagen in
  [assets/App-Icons/](upstream-9ed4032/assets/App-Icons/README.md).
  Der zugehörige Generator ist unverändert mitkopiert und wurde nicht ausgeführt.
- Die Rechte- und Lizenzhinweise der Quelle liegen in
  [LICENSE](upstream-9ed4032/LICENSE) und `upstream-9ed4032/LICENSES/`.

## Verhältnis zur App und zur Historie

Der Import auf `9ed4032` war zunächst ein reines Referenz-Update. Die gesonderte
App-Version 0.1.15 bindet das neue Symbol einschließlich Startbild und kleinem
Benachrichtigungssymbol ein; siehe [APP_IDENTITY.md](../../docs/APP_IDENTITY.md).
Die Referenzdateien bleiben unverändert. Die Präzisierungen zu Host-Farben
erfordern keine Nacharbeit an 0.1.14; B2/B3 bleiben auch in 0.1.15 unverändert.

Die älteren Dateien bleiben als historische Quellen erhalten:

- `azentrius-*.md`, `azentrius-tokens.json`, `android/`, `icons-svg/` und
  `PACKAGE.sha256`: ursprünglicher ZIP-Import für 0.1.13.
- `upstream-ab59a45/`: ergänzte Reiter-Regeln für 0.1.13.
- `upstream-eb827c8/`: freigegebene Grundlage der Umsetzung und Abnahme 0.1.14.

Für neue Designabgleiche gilt `upstream-9ed4032/`; die älteren Kopien beschreiben
die damaligen Umsetzungsstände. Der [Testbericht 0.1.14](../../docs/history/BUILD_AND_TEST-0.1.14.md)
bleibt ein historischer Nachweis der tatsächlich geprüften App.

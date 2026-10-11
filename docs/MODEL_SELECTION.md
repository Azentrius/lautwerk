# Anbieter- und Modellauswahl – 0.1.2

## Nutzung

In der DHU-Test-App auf dem Smartphone zuerst den Anbieter und dann das Modell wählen.
Die Auswahl wird sofort lokal gespeichert. Beim Wechsel zurück zu einem Anbieter
erscheint dessen zuletzt gewähltes Modell. Beim erneuten Öffnen bleibt die Auswahl erhalten.
Das Car-Template zeigt Anbieter/Modell unter „Bereit für den UI-Test.“ bzw.
„Gestartet · Test N“. Änderungen werden bei sichtbarem Template aktualisiert;
beim Zurückkehren liest es den aktuellen Stand. „Starten“ bleibt ein reiner UI-Test.

Auch die fortgeführte Assistenten-App verwendet diese Auswahl. Dort sind die
Anbieter-/Modellschalter während eines laufenden Sprachgesprächs deaktiviert.
Ihr vorhandener Sprachbefehl zum Anbieterwechsel nutzt das zuletzt gewählte Modell
des Zielanbieters und erzeugt weiterhin einen neuen Chat ohne vorherigen Verlauf.

## Speicher und Modulgrenzen

`core` enthält `Provider`, `ModelOption`, `ModelCatalog` und `ModelSelection`.
Ein `ModelSelection`-Objekt akzeptiert nur ein zum Anbieter gehörendes Modell.
`settings` hängt von `core` ab und implementiert `ModelSelectionStore` mit
app-privaten SharedPreferences (`model_selection_v1`). Es speichert `provider`
als Enum-Namen und `model.openai` / `model.anthropic` als Modell-ID.
Änderungen beider Werte werden gemeinsam sichtbar. Ungültige oder beschädigte
Einstellungen fallen auf einen passenden Standard zurück, ohne Archive umzuschreiben.

Phone-Activity und CarAppService der DHU-APK laufen im selben Prozess und verwenden
diesen Store. Beide melden ihre Beobachter beim Verlassen der sichtbaren Oberfläche ab.
Die separat installierten Pakete `dev.openautoai.app`, `dev.openautoai.prototype.phone`
und `dev.openautoai.prototype.dhu` haben jeweils ihren eigenen Speicher.
Es gibt keinen Austausch von Daten oder Schlüsseln zwischen diesen APKs.

`ChatApi`, `ConversationStore` und `SecretStore` nutzen die ausgelagerten Typen.
Enum-Namen (`OPENAI`, `CLAUDE`), Schlüsselkennungen (`openai`, `anthropic`),
Archivdatei, JSON-Felder und Keystore-Aliase ändern sich nicht.
Ein ausgewählter historischer Chat wird nur mit exakt seinem Anbieter/Modell fortgesetzt;
ein unbekanntes Archivmodell wird nicht stillschweigend durch ein anderes ersetzt.

## Bewusst beibehaltener Startkatalog

| Anbieter | Modell-ID | Anzeige | Standard |
|---|---|---|---|
| OpenAI | `gpt-5.4-mini` | GPT-5.4 mini | Ja |
| OpenAI | `gpt-5.4` | GPT-5.4 | |
| Claude / Anthropic | `claude-sonnet-4-6` | Claude Sonnet 4.6 | Ja |
| Claude / Anthropic | `claude-opus-4-6` | Claude Opus 4.6 | |
| Claude / Anthropic | `claude-haiku-4-5` | Claude Haiku 4.5 | |

Dieser Katalog übernimmt die IDs aus 0.1.0; er ist keine vollständige oder automatisch
aktualisierte Modellliste. Die IDs wurden am 11.10.2026 mit den offiziellen Modellseiten
abgeglichen. Kontozugriff und API-Verhalten sind damit nicht getestet. Es erfolgte
keine Modellmigration und kein kostenpflichtiger Aufruf.

- [OpenAI GPT-5.4 mini](https://developers.openai.com/api/docs/models/gpt-5.4-mini)
- [OpenAI GPT-5.4](https://developers.openai.com/api/docs/models/gpt-5.4)
- [Claude Sonnet 4.6](https://platform.claude.com/docs/en/models/sonnet-4-6/overview)
- [Claude Opus 4.6](https://platform.claude.com/docs/en/models/opus-4-6/overview)
- [Claude Haiku 4.5](https://platform.claude.com/docs/en/models/haiku-4-5/overview)
- [Claude Modell-IDs und Aliase](https://platform.claude.com/docs/en/about-claude/models/model-ids-and-versions)

## Prüfung im echten DHU

1. 0.1.2 mit `adb install -r` über die vorhandene DHU-App installieren;
   [vollständige Windows-Schritte](START_WINDOWS.md).
2. Auf dem Handy in genau dieser App Claude / Anthropic und Claude Haiku 4.5 wählen.
3. App schließen und wieder öffnen: Auswahl muss erhalten bleiben.
4. Im verbundenen DHU OpenAuto AI öffnen: Titel, Anbieter und Modell prüfen.
5. Starten tippen: „Gestartet · Test 1“ muss zusammen mit der Auswahl erscheinen.
6. Auf dem Handy OpenAI / GPT-5.4 wählen: Die sichtbare DHU-Auswahl soll sich ändern.
7. Zurück zu Claude wechseln: Claude Haiku 4.5 muss wieder ausgewählt sein.

Die Kategorie-/Sideload-Grenzen von 0.1.1 gelten unverändert. Der diagnostische
POI-Eintrag, der aktive HostValidator und die deaktivierte DHU-Release-Variante
wurden nicht verändert. Keine offiziell unterstützte KI-Car-App-Kategorie wird behauptet.

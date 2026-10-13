# Button

Auslöser für Aktionen, immer pillenförmig. Welche Art an welcher Stelle steht, ist in allen Addons und Web-Oberflächen gleich. Für native Android-Apps gilt der Abschnitt „Android“.

Entspricht PatternFly `Button` (primary, secondary, link, plain, danger).

```html
<button class="az-btn az-btn--primary" type="button"><svg class="az-icon">…sync-alt…</svg>Aktualisieren</button>
<button class="az-btn az-btn--secondary az-btn--sm" type="button"><svg class="az-icon">…plus…</svg>Job hinzufügen</button>
<button class="az-btn az-btn--plain az-btn--sm" type="button" aria-label="Bearbeiten" title="Bearbeiten"><svg class="az-icon">…pencil-alt…</svg></button>
<button class="az-btn az-btn--link" type="button">Abbrechen</button>
<button class="az-btn az-btn--danger" type="button">Löschen</button>
```

## Welcher Button wo

| Stelle | Art | Icon |
|---|---|---|
| Seitenkopf | „Aktualisieren“, `az-btn--primary`, normale Größe | `sync-alt`, links |
| Kartenkopf, z. B. „Job hinzufügen“ | `az-btn--secondary az-btn--sm` | immer, links |
| Tabellenzeile, Aktion mit Folgen, z. B. „Update“ | `az-btn--secondary az-btn--sm` mit Text | immer, links |
| Tabellenzeile, Werkzeug, z. B. ausführen, bearbeiten, erledigt, weitere Aktionen | `az-btn--plain az-btn--sm`, nur Icon | mit `aria-label` und `title` |
| Dialog, Hauptaktion | `az-btn--primary`, beim Löschen `az-btn--danger` | keines |
| Dialog, Abbrechen | `az-btn--link` | keines |

- **Nur ein gefüllter Button je Seite:** Auf der Seite ist „Aktualisieren“ im Kopf der einzige `az-btn--primary`, im Dunkeln Gold, im Hellen Tinte. Alles andere ist umrandet oder nur ein Icon. In einem offenen Dialog ist dessen Hauptaktion gefüllt.
- **Text-Buttons auf der Seite tragen immer ein Icon** aus der Gruppe „Icons“, links vom Text. Ohne passendes Icon wird eines aus demselben PatternFly-Satz ergänzt, nie ein Button ohne Icon gesetzt.
- **Icon-Buttons** nur für Werkzeuge, die in jeder Zeile wiederkehren und deren Icon eindeutig ist. „Weitere Aktionen“ (`ellipsis-v`) steht immer zuletzt.
- `az-btn--secondary` hat einen Rahmen in `primary` und Schrift in `primary-text`.
- `az-btn--danger` nur für das endgültige Bestätigen im Löschdialog, nie in einer Tabellenzeile. Dort steht ein Papierkorb als Icon-Button.
- Beschriftung ist ein Verb plus Objekt („Job hinzufügen“). Feste Ausnahmen, weil sie überall gleich heißen: „Aktualisieren“, „Update“, „Abbrechen“.
- `az-btn--sm` in Kartenköpfen und Tabellen, die normale Größe im Seitenkopf und in Dialogen.

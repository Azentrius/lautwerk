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
| Link in einer Tabellenzelle oder Hinweiszeile, z. B. „Läufe ansehen“ | `az-btn--link` oder Textlink | keines; führt er aus der Seite heraus, `external-link-alt` rechts vom Text |
| Projektseite, Haupt-Link im Hero | `az-btn--lg`, einer `az-btn--primary`, die übrigen `az-btn--secondary` | darf fehlen |

- **Nur ein gefüllter Button je Seite:** Auf der Seite ist „Aktualisieren“ im Kopf der einzige `az-btn--primary`, im Dunkeln Gold, im Hellen Tinte. Alles andere ist umrandet oder nur ein Icon. In einem offenen Dialog ist dessen Hauptaktion gefüllt.
- **Text-Buttons auf der Seite tragen immer ein Icon** aus der Gruppe „Icons“, links vom Text. Ohne passendes Icon wird eines aus demselben Paket ergänzt (Herkunft und Lizenz siehe README der Gruppe „Icons“), nie ein Button ohne Icon gesetzt. Einzige Ausnahme sind die Haupt-Links auf Projektseiten (Abschnitt „Projektseiten“).
- **Links sind keine Text-Buttons:** `az-btn--link` und Links in Tabellenzellen und Hinweiszeilen, etwa „Läufe erfassen“ oder „Läufe ansehen“, brauchen kein Icon, so wie „Abbrechen“ im Dialog. Führt ein Link aus der Seite heraus, trägt er `external-link-alt` rechts vom Text.
- **Icon-Buttons** nur für Werkzeuge, die in jeder Zeile wiederkehren und deren Icon eindeutig ist. „Weitere Aktionen“ (`ellipsis-v`) steht immer zuletzt.
- `az-btn--secondary` hat einen Rahmen in `primary` und Schrift in `primary-text`.
- `az-btn--danger` nur für das endgültige Bestätigen im Löschdialog, nie in einer Tabellenzeile. Dort steht ein Papierkorb als Icon-Button.
- Beschriftung ist ein Verb plus Objekt („Job hinzufügen“). Feste Ausnahmen, weil sie überall gleich heißen: „Aktualisieren“, „Update“, „Abbrechen“. Auf Entscheidungskarten, auf denen jemand eine Anfrage beantwortet (etwa eine Freigabe in Stellwerk), heißen die Antworten fest „Erlauben“, „Ablehnen“ und „In der App“: Worum es geht, steht auf der Karte.
- `az-btn--sm` in Kartenköpfen und Tabellen, die normale Größe im Seitenkopf und in Dialogen, `az-btn--lg` nur für Haupt-Links auf Projektseiten. Auf einer Projektseite ist der gefüllte Button der erste Haupt-Link im Hero.

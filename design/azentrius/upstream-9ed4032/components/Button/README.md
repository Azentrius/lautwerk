# Button

Auslöser für Aktionen, immer pillenförmig wie in Cockpit; die Farbe sagt, welche Aktion die wichtigste ist.

Entspricht PatternFly `Button` (primary, secondary, link, plain, danger).

```html
<button class="az-btn az-btn--primary" type="button">Job hinzufügen</button>
<button class="az-btn az-btn--secondary" type="button">Neu laden</button>
<button class="az-btn az-btn--link" type="button">Abbrechen</button>
<button class="az-btn az-btn--danger" type="button">Löschen</button>
<button class="az-btn az-btn--plain" type="button" aria-label="Weitere Aktionen"><svg class="az-icon">…</svg></button>
```

- Pro Bereich höchstens ein `az-btn--primary`. Er trägt `primary` mit `on-primary` – in Cockpit Blau, in eigenen Apps Gold.
- `az-btn--danger` nur für das endgültige Bestätigen im Löschdialog, nie in einer Tabellenzeile. Dort steht ein Papierkorb als `az-btn--plain`.
- `az-btn--secondary` für die zweite Aktion eines Bereichs (z. B. „Neu laden“): Rahmen in `primary`, Schrift in `primary-text`.
- Abbrechen ist immer `az-btn--link`.
- Beschriftung ist ein Verb plus Objekt: „Job hinzufügen“, nicht „Hinzufügen“ oder „OK“.
- `az-btn--sm` in Kartenköpfen und Tabellen, die normale Größe in Seitenkopf und Dialogen.
- Icon-Buttons brauchen ein `aria-label`.

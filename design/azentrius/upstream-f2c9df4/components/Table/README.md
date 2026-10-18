# Table

Listen gleichartiger Einträge mit Spalten – Jobs, Container, Updates.

Entspricht PatternFly `Table` (compact nicht nötig).

```html
<div class="az-card__body--flush az-table-wrap">
  <table class="az-table az-table--stack">
    <thead><tr><th>Zeitplan</th><th>Befehl</th><th><span class="az-sr">Aktionen</span></th></tr></thead>
    <tbody>
      <tr class="is-off">
        <td data-label="Zeitplan">Montags um 05:00<span class="az-cell-sub az-mono">0 5 * * 1</span></td>
        <td data-label="Befehl"><span class="az-mono">tar -zcf …</span></td>
        <td class="az-cell-actions">…Icon-Buttons…</td>
      </tr>
    </tbody>
  </table>
</div>
```

- Jede Zelle bekommt `data-label` mit ihrer Spaltenüberschrift. Mit `az-table--stack` wird jede Zeile unter 640 px zu einem Block aus Beschriftung und Wert.
- Lesbares zuerst, Technisches darunter: „Montags um 05:00“ in `body`, `0 5 * * 1` als `az-cell-sub az-mono`.
- Befehle, Pfade, Digests und Image-Namen immer in `az-mono`; sie brechen überall um, statt die Tabelle zu verbreitern.
- Kurze Spalten (Name, Datei, Benutzer, Zeitplan) bekommen `az-nowrap` an der Zelle; dann nimmt die lange Spalte (Befehl, Image) die Breite und bricht selbst um, statt kurze Bezeichner mitten im Wort zu trennen.
- Deaktivierte Zeilen bekommen `is-off` (Text in `text-disabled`). Nicht durchstreichen.
- Zeilenaktionen als `az-btn--plain az-btn--sm` in der letzten Spalte `az-cell-actions`.

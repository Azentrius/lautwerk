# Switch

Ein- und Ausschalter für etwas, das sofort wirkt, etwa einen Cron-Job aktivieren.

Entspricht PatternFly `Switch`.

```html
<label class="az-switch">
  <input type="checkbox" id="job-3" checked>
  <span class="az-switch__track"></span>
  <span class="az-switch__label">Aktiv</span>
</label>
```

- Nur für Änderungen, die ohne Speichern-Button sofort gelten. In Formularen mit „Speichern“ ist es eine Checkbox.
- In Tabellen ohne sichtbaren Text, dann mit `<span class="az-sr">Deaktivieren</span>` für Screenreader.
- An: `primary` mit `on-primary`-Knopf. Aus: `bg-control` mit `line-control`-Rahmen.
- Paketverwaltete, nicht änderbare Einträge zeigen den Schalter `disabled`, nicht versteckt.

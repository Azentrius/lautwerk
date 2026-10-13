# Label

Ein bis zwei Wörter Status neben einem Eintrag; Farbe, Icon und Wort sagen dasselbe.

Entspricht PatternFly `Label` (filled).

```html
<span class="az-label az-label--success"><svg class="az-icon">…check-circle…</svg>Aktuell</span>
<span class="az-label az-label--info"><svg class="az-icon">…info-circle…</svg>Update verfügbar</span>
```

| Bedeutung | Klasse | Icon | Beispiele |
|---|---|---|---|
| ohne Wertung | `az-label` | `ban` oder keins | Deaktiviert, Unbekannt, root |
| gut | `az-label--success` | `check-circle` | Aktuell, Läuft, OK |
| Hinweis | `az-label--info` | `info-circle` | Update verfügbar, Neu |
| Achtung | `az-label--warning` | `exclamation-triangle` | Neustart nötig |
| Fehler | `az-label--danger` | `exclamation-circle` | Fehlgeschlagen |

- Immer Icon plus Wort, nie Farbe allein.
- `az-label--strong` (volle Statusfläche) nur allein, etwa als Gesamtstatus eines Hosts. Nie in einer Reihe mit getönten Labels und nie in Tabellen.
- Gold ist nie ein Status. Die Markenfarbe bleibt der Signatur und den Aktionen in eigenen Apps vorbehalten.

# EmptyState

Was eine Karte oder Seite zeigt, wenn es noch nichts gibt – mit dem Weg zum ersten Eintrag.

Entspricht PatternFly `EmptyState`.

```html
<div class="az-empty">
  <span class="az-empty__icon"><svg class="az-icon">…</svg></span>
  <h2 class="az-empty__title">Keine Jobs in der root-Crontab</h2>
  <p class="az-empty__body">Ein bis zwei Sätze, warum es leer ist.</p>
  <div class="az-empty__actions"><button class="az-btn az-btn--secondary"><svg class="az-icon">…plus…</svg>Job hinzufügen</button></div>
</div>
```

- Titel benennt den Zustand konkret, nicht „Keine Daten“.
- Der Text erklärt den Grund, wenn er nicht offensichtlich ist (Diun hat noch nicht gescannt, die Datei fehlt).
- Höchstens eine Aktion, als umrandeter Button mit Icon (`az-btn--secondary`). Gefüllt ist auf der Seite nur „Aktualisieren“ im Seitenkopf.

# Card

Die Grundeinheit jeder Seite: ein abgegrenzter Bereich mit Titel, optionalen Aktionen und Inhalt.

Entspricht PatternFly `Card`.

```html
<section class="az-card" aria-labelledby="root-title">
  <div class="az-card__header">
    <div>
      <h2 class="az-card__title" id="root-title">root-Crontab</h2>
      <p class="az-card__desc">Ein Satz Kontext.</p>
    </div>
    <div class="az-card__actions">…</div>
  </div>
  <div class="az-card__body">…</div>
</section>
```

- Karten liegen auf `bg-page`, haben `bg-card`, einen `line`-Rahmen und `radius-medium`. Zwischen Karten steht `space-lg`.
- Eine Tabelle füllt die Karte bis zum Rand: `az-card__body--flush` statt `az-card__body`.
- Keine Karte in der Karte. Keine farbigen Ränder oder Akzentstreifen.
- Projektkarten auf Projektseiten sind größer: `radius-large`, innen `space-2xl`, Titel in `page-card-title` (Abschnitt „Projektseiten“).
- Titel im Stil `card-title`, als `h2`.
- Aktionen im Kartenkopf sind umrandete Buttons mit Icon (`az-btn--secondary az-btn--sm`), nie gefüllt. Gefüllt ist auf der Seite nur „Aktualisieren“ im Seitenkopf.

# Tabs

Reiter für die Bereiche einer Seite, direkt unter dem Seitenkopf. Der aktive Reiter steht in `primary-text` mit einer 2 px starken Linie in `signature`: im Dunkeln beides Gold, im Hellen Tinte mit bronzener Linie.

Entspricht PatternFly `Tabs` (Standard, nicht „box“).

```html
<nav class="az-tabs" role="tablist" aria-label="Bereiche">
  <button class="az-tab" role="tab" id="tab-overview" aria-controls="panel-overview" aria-selected="true" type="button">Übersicht</button>
  <button class="az-tab" role="tab" id="tab-timers" aria-controls="panel-timers" aria-selected="false" tabindex="-1" type="button">systemd-Timer</button>
</nav>
<section id="panel-overview" role="tabpanel" aria-labelledby="tab-overview">…</section>
<section id="panel-timers" role="tabpanel" aria-labelledby="tab-timers" hidden>…</section>
```

- Reiter nur, wenn eine Seite mehrere Bereiche hat. Gibt es nur einen, endet der Rahmen mit der Linie des Seitenkopfs.
- Beschriftung: ein bis zwei Wörter, ohne Zähler und ohne Icon.
- Der erste Reiter ist der, mit dem man am häufigsten arbeitet. Er ist beim Öffnen aktiv.
- Jeder Reiter ist mindestens 44 px hoch. Reicht die Breite nicht, brechen die Reiter in eine zweite Zeile um.
- Tastatur nach dem WAI-ARIA-Muster für Reiter: Pfeiltasten wechseln zwischen den Reitern, nur der aktive ist mit Tab erreichbar (`tabindex="-1"` an den anderen).
- Der Wechsel lädt nichts neu und ändert die Adresse nicht. „Aktualisieren“ lässt den aktiven Reiter stehen.
- Hinweise und Meldungen, die für die ganze Seite gelten, stehen direkt unter der Reiterleiste, nicht über den Reitern, und sind in jedem Reiter sichtbar.
- Ausgeblendete Bereiche tragen das Attribut `hidden`; `[role="tabpanel"][hidden]` steht dafür in `bundle.css`.

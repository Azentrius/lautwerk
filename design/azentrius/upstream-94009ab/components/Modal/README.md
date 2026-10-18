# Modal

Ein Dialog für eine abgeschlossene Aufgabe: Job bearbeiten, Löschen bestätigen.

Entspricht PatternFly `Modal` (Größe small).

```html
<div class="az-backdrop" hidden>
  <div class="az-modal" role="dialog" aria-modal="true" aria-labelledby="dlg-title">
    <div class="az-modal__header"><h2 class="az-modal__title" id="dlg-title">Job bearbeiten</h2>…Schließen…</div>
    <div class="az-modal__body">…Felder…</div>
    <div class="az-modal__footer">
      <button class="az-btn az-btn--primary">Speichern</button>
      <button class="az-btn az-btn--link">Abbrechen</button>
    </div>
  </div>
</div>
```

- Die Hauptaktion steht zuerst, links. Abbrechen folgt als Link-Button. So machen es alle Cockpit-Dialoge; die bisherige Anordnung rechts in cronctl wechselt dazu.
- Der Titel nennt die Aufgabe als Verb plus Objekt. Löschdialoge nennen das Objekt: „Job löschen?“ und im Text den Befehl.
- Im Löschdialog ist die Hauptaktion `az-btn--danger`.
- Escape und der Schließen-Button schließen, ein Klick auf den Hintergrund nicht, solange Eingaben ungespeichert sind.
- Ausblenden über das Attribut `hidden`. `.az-backdrop[hidden]` steht ausdrücklich in bundle.css, weil `display: grid` sonst `hidden` überschreibt – genau der Fehler, den cronctl im Juni hatte.

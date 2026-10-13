# PageHeader

Der Kopf jeder Seite: Titel, ein Satz Beschreibung und die Aktionen, die für die ganze Seite gelten.

Entspricht in Cockpit dem PatternFly-Seitenabschnitt mit `Title` und `Toolbar`.

```html
<div class="az-page">
  <header class="az-page-header">
    <div>
      <h1 class="az-page-title">Cron Jobs</h1>
      <p class="az-page-desc">Geplante Aufgaben auf public-host anzeigen und verwalten.</p>
    </div>
    <div class="az-page-actions">…Buttons…</div>
  </header>
  …Karten…
</div>
```

- Genau ein `az-page-title` pro Seite, im Stil `page-title`. Der Titel ist der Menüname aus `manifest.json`.
- Die Beschreibung ist ein Satz, der sagt, was man hier tun kann. Kein „Willkommen“.
- Aktionen rechts, die Hauptaktion zuletzt und als einzige `az-btn--primary`. Neu laden ist ein Icon-Button (`az-btn--plain`) mit `aria-label`.
- Zeitangaben wie „Geladen um 23:04“ stehen als `az-meta` vor den Buttons.

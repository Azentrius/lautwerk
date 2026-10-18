# PageHeader

Der Kopf jeder Seite, in allen Addons und Web-Oberflächen gleich: eine Zeile mit dem Produktnamen links, dem Stand der Daten und „Aktualisieren“ rechts, darunter eine Linie. Hat die Seite mehrere Bereiche, folgen direkt darunter die Reiter (Baustein `Tabs`). Den Titel mit Namenszusatz oder Punkt übernehmen auch die Apps; für Android steht das im Abschnitt „Android“.

Entspricht in Cockpit dem PatternFly-Seitenabschnitt mit `Title` und `Toolbar`.

```html
<div class="az-page">
  <header class="az-page-header">
    <h1 class="az-page-title">Image Update <span class="az-page-title__suffix">Monitor</span></h1>
    <div class="az-page-actions">
      <span class="az-meta">21 Updates · 6 neue Versionen · Radar vor 2 h</span>
      <button class="az-btn az-btn--primary" type="button"><svg class="az-icon">…sync-alt…</svg>Aktualisieren</button>
    </div>
  </header>
  <nav class="az-tabs" role="tablist">…</nav>
  …Inhalt…
  <footer class="az-signature">…</footer>
</div>
```

- **Titel:** genau ein `az-page-title` pro Seite, 24 px, Gewicht 500. Er ist der Produktname, so wie im Menü.
- **Namenszusatz:** Hat der Name einen Zusatz („Image Update Monitor“, „Design System“), steht der Zusatz in `az-page-title__suffix`: DM Serif Display kursiv in `signature`, also Gold im Dunkeln und Bronze im Hellen.
- **Punkt:** Ein Name ohne Zusatz („Cron Jobs“) trägt stattdessen den Punkt der Wortmarke: `<h1 class="az-page-title az-page-title--dot">Cron Jobs</h1>`. Der Punkt kommt aus CSS, in `signature`. Im Cockpit-Menü steht der Name ohne Punkt (Grundlagen, Marke).
- **Keine Beschreibung im Kopf.** Der Kopf ist eine Zeile. Der Host steht in der Signatur im Fuß.
- **Stand:** rechts als `az-meta`, nur Fakten zum Stand der Daten: „Geladen um 22:17 · Serverzeit UTC“ oder „21 Updates · 6 neue Versionen · 31 Container · Radar vor 2 h“.
- **Aktion:** genau ein Button, „Aktualisieren“ (englisch „Refresh“): `az-btn--primary` in normaler Größe, Icon `sync-alt` links. In jedem Addon gleich, auch wenn er nur neu lädt. Während des Ladens ist er deaktiviert und heißt „Lädt …“. Er ist der einzige gefüllte Button der Seite, im Dunkeln Gold, im Hellen Tinte (siehe `Button`).
- Weitere Aktionen gehören nicht in den Kopf, sondern in die Karte, für die sie gelten.
- Auf schmalen Bildschirmen bricht der rechte Teil unter den Titel um.

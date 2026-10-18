# Cockpit-Addons

So wird das System in einem Cockpit-Addon ohne Build-Schritt eingesetzt. Grundlage: Cockpit 360 und 362 auf den vier Hosts, beide mit PatternFly 6.

Seit dem 12. Oktober 2026 tragen die Addons die Azentrius-Themen: „Azentrius hell“, wenn Cockpit hell ist, „Azentrius dunkel“, wenn Cockpit dunkel ist. Vorbild war Image Update Monitor. Die Themen „Cockpit hell“ und „Cockpit dunkel“ werden in den Addons nicht mehr verwendet.

## Dateien je Addon

```
/usr/share/cockpit/<addon>/
  manifest.json
  index.html
  azentrius.css                  ← Tokens (hell + dunkel) und alle az-Bausteine, für jedes Addon identisch
  azentrius-theme.js             ← folgt Cockpits Hell/Dunkel-Schalter, für jedes Addon identisch
  DMSans.woff2                   ← Schrift der Oberfläche, lateinische Teilmenge, Gewichte 400–600
  DMSerifDisplay-Regular.woff2   ← nur die Zeichen der Signatur „Azentrius.“
  DMSerifDisplay-Italic.woff2    ← nur die Zeichen des Namenszusatzes, falls das Addon einen hat
  app.js / app.css               ← was nur dieses Addon braucht
```

Die Icons stehen als Pfade im Code des Addons (`app.js` oder eine eigene Datei). Darüber steht ihre Herkunft: Font Awesome Free 5 von Fonticons, Inc. unter CC BY 4.0, übernommen über `@patternfly/react-icons` 6.4.0; nutzt das Addon `pending`, zusätzlich PatternFly unter MIT. Die Lizenzhinweise aus `LICENSES/` liegen dem Addon bei.

`azentrius.css` entsteht aus `tokens.json` und `components/bundle.css` dieses Systems: die Werte von „Azentrius hell“ auf `:root`, die von „Azentrius dunkel“ auf `:root.pf-v6-theme-dark`, DM Sans als Schrift der Oberfläche. Sie ist in allen Addons byte-gleich und wird per SHA-256 geprüft. `azentrius.css` und `azentrius-theme.js` stehen wie die Addons unter Apache-2.0 (Grundlagen, Lizenz).

## index.html

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Image Update Monitor</title>
  <script src="azentrius-theme.js"></script>
  <link rel="stylesheet" href="azentrius.css">
  <link rel="stylesheet" href="app.css">
  <script src="../base1/cockpit.js"></script>
  <script src="../base1/po.js"></script>
</head>
<body>
  <div class="az-page">
    <header class="az-page-header">
      <h1 class="az-page-title">Image Update <span class="az-page-title__suffix">Monitor</span></h1>
      <div class="az-page-actions">
        <span class="az-meta">…Stand…</span>
        <button class="az-btn az-btn--primary" type="button">…sync-alt…Aktualisieren</button>
      </div>
    </header>
    <nav class="az-tabs" role="tablist">…Reiter…</nav>
    <section role="tabpanel">…Hinweise und Karten…</section>
    <footer class="az-signature">…</footer>
  </div>
  <script src="app.js"></script>
</body>
</html>
```

`azentrius-theme.js` steht vor dem Stylesheet, damit die Seite nicht kurz in der falschen Helligkeit aufblitzt. `../base1/po.js` lädt Cockpits Übersetzung für die Sitzungssprache; erst danach kennt `cockpit.language` die Sprache. Die Texte des Addons selbst kommen aus seinen eigenen Tabellen für Englisch und Deutsch, nicht über `cockpit.gettext` (Grundlagen, Sprache). `../base1/cockpit.css` gibt es seit PatternFly 6 nicht mehr.

## Seitenrahmen

Jedes Addon hat denselben Rahmen. Die Einzelheiten stehen in den Bausteinen `PageHeader`, `Tabs`, `Signature` und `Button`.

- **Kopf:** eine Zeile. Links der Produktname, ein Namenszusatz kursiv in DM Serif Display, ein Name ohne Zusatz mit dem Punkt der Wortmarke. Rechts der Stand der Daten und „Aktualisieren“ als einziger gefüllter Button der Seite, mit Icon `sync-alt`. Darunter eine Linie. Keine Beschreibung.
- **Reiter:** direkt unter dem Kopf, wenn das Addon mehrere Bereiche hat. Der aktive Reiter in `primary-text` mit Linie in `signature`.
- **Inhalt:** Hinweise und Karten des aktiven Reiters. Aktionen einer Karte sind umrandete Buttons mit Icon; in Tabellenzeilen stehen Werkzeuge als Icon-Buttons.
- **Fuß:** die Signatur mit Produktname, Version und Host.

## Schriften

DM Sans und DM Serif Display bringt das Addon selbst mit, jeweils nur als Teilmenge. Red Hat Mono für Code liefert Cockpit unter `/usr/share/cockpit/static/fonts/` aus:

```css
@font-face { font-family: "DM Sans"; src: url("DMSans.woff2") format("woff2"); font-weight: 400 600; }
@font-face { font-family: "DM Serif Display"; src: url("DMSerifDisplay-Regular.woff2") format("woff2"); }
@font-face { font-family: "DM Serif Display"; src: url("DMSerifDisplay-Italic.woff2") format("woff2"); font-style: italic; }
@font-face { font-family: "Red Hat Mono"; src: url("../../static/fonts/RedHatMono/RedHatMonoVF.woff2") format("woff2"); font-weight: 300 700; }
```

Alles bleibt lokal; die Standard-CSP von Cockpit muss nicht gelockert werden. Den Pfad zu Red Hat Mono vor dem Ausrollen auf einem neuen Host prüfen.

## Hell und Dunkel

Cockpit merkt sich die Wahl aus dem Benutzermenü („Stil“) im Browser unter `shell:style` (`auto`, `light`, `dark`) und setzt in seinen eigenen Seiten die Klasse `pf-v6-theme-dark`. Ein Addon muss dem selbst folgen; `prefers-color-scheme` allein reicht nicht, weil es die Einstellung in Cockpit ignoriert. `azentrius-theme.js`:

```js
(function () {
    function apply(style) {
        style = style || localStorage.getItem("shell:style") || "auto";
        var dark = style === "dark" ||
            (style === "auto" && window.matchMedia("(prefers-color-scheme: dark)").matches);
        document.documentElement.classList.toggle("pf-v6-theme-dark", dark);
    }
    window.addEventListener("storage", function (e) { if (e.key === "shell:style") apply(); });
    window.addEventListener("cockpit-style", function (e) { apply(e.detail && e.detail.style); });
    window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", function () { apply(); });
    apply();
})();
```

Das entspricht dem Verhalten von Cockpits eigenem `cockpit-dark-theme` (LGPL-2.1), ist aber unabhängig davon geschrieben.

## Was sich an den beiden Addons ändert

**Cron Jobs** nutzt `azentrius.css` schon.

- Kopf: Die Beschreibung entfällt. „Neu laden“ wird zu „Aktualisieren“ (englisch „Refresh“), primär und mit Icon `sync-alt`.
- Neue Reiter, in dieser Reihenfolge:
  - „Übersicht“ mit der 24-Stunden-Zeitleiste und der root-Crontab
  - „systemd-Timer“
  - „System“ mit `/etc/crontab`, `/etc/cron.d` und den periodischen Skripten

  Hinweise und Meldungen, etwa „Ausgaben von Cron-Jobs gehen verloren“ oder ein Fehler nach „Aktualisieren“, stehen direkt unter der Reiterleiste und sind in jedem Reiter sichtbar.
- „Job hinzufügen“ wird ein umrandeter Button (`az-btn--secondary az-btn--sm`) mit Icon `plus`.
- Die Signatur bleibt, wie sie ist.

**Image Update Monitor** hatte bis dahin eigenes CSS: nur dunkel, Schriften nicht mitgeliefert (der Zusatz erschien deshalb in Georgia).

- Auf `azentrius.css`, `azentrius-theme.js` und die Schriftdateien umstellen. Damit folgt das Addon Cockpits Hell/Dunkel.
- Kopf und Reiter behalten ihre Form. „Aktualisieren“ wird zur Pille mit Icon `sync-alt`.
- In den Zeilen wird „Update“ ein umrandeter Button mit Icon `arrow-circle-up`; „Erledigt“ wird ein Icon-Button mit `check`.
- Status über die Statussprache, jeweils mit ihrem festen Icon: „Update verfügbar“ und „Neue Version“ als Hinweis (`az-label--info`, `info-circle`), „Aktuell“ als gut (`az-label--success`, `check-circle`), Unbekanntes ohne Wertung (`az-label`, `ban`). Gold verschwindet aus dem Status.
- Neu: die Signatur im Fuß, „Image Update Monitor“ mit Version und Host.
- Die schmale Ansicht als Blöcke bleibt und wird zu `az-table--stack`.

## Namen (16. Oktober 2026)

Produktnamen werden nicht mehr übersetzt (Grundlagen, Marke). Für die beiden Addons heißt das:

- **Cron Jobs:** Der Seitenkopf trägt den Punkt (`az-page-title--dot`). Menü, Signatur und AppStream bleiben „Cron Jobs“.
- **Image Update Monitor:** Menü, `<title>`, Signatur und AppStream heißen in jeder Sprache „Image Update Monitor“, der Seitenkopf „Image Update *Monitor*“. Das Paket heißt wie das Repo `cockpit-image-update-monitor`; das alte Paket `cockpit-image-updates` wird auf den Hosts entfernt. Die Schriftdatei `DMSerifDisplay-Italic.woff2` braucht die Zeichen von „Monitor“.
- Beide übernehmen die neue `azentrius.css` mit `az-page-title--dot`, `az-btn--lg` und `az-eyebrow`, wieder byte-gleich.

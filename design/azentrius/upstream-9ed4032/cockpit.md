# Cockpit-Addons

So wird das System in einem Cockpit-Addon ohne Build-Schritt eingesetzt. Grundlage: Cockpit 360 und 362 auf den vier Hosts, beide mit PatternFly 6.

## Dateien je Addon

```
/usr/share/cockpit/<addon>/
  manifest.json
  index.html
  azentrius.css        ← Tokens (hell + dunkel) und alle az-Bausteine, für jedes Addon identisch
  azentrius-theme.js   ← folgt Cockpits Hell/Dunkel-Schalter, für jedes Addon identisch
  DMSerifDisplay-Regular.woff2   ← nur für die Signatur
  app.js / app.css     ← was nur dieses Addon braucht
```

`azentrius.css` entsteht aus `tokens.json` und `components/bundle.css` dieses Systems: die Werte von „Cockpit hell“ auf `:root`, die von „Cockpit dunkel“ auf `:root.pf-v6-theme-dark`. Sie ist in allen Addons byte-gleich und wird wie bisher per SHA-256 geprüft.

## index.html

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Cron Jobs</title>
  <script src="azentrius-theme.js"></script>
  <link rel="stylesheet" href="azentrius.css">
  <link rel="stylesheet" href="app.css">
  <script src="../base1/cockpit.js"></script>
</head>
<body>
  <div class="az-page">
    <header class="az-page-header">…</header>
    …Karten…
    <footer class="az-signature">…</footer>
  </div>
  <script src="app.js"></script>
</body>
</html>
```

`azentrius-theme.js` steht vor dem Stylesheet, damit die Seite nicht kurz hell aufblitzt. `../base1/cockpit.css` gibt es seit PatternFly 6 nicht mehr; der Link in cronctl läuft heute ins Leere und entfällt.

## Schriften

Red Hat Text, Display und Mono liefert Cockpit unter `/usr/share/cockpit/static/fonts/` aus. Ein Addon bindet sie über den relativen Pfad ein, den auch Cockpits eigene Oberfläche benutzt:

```css
@font-face { font-family: "Red Hat Text";    src: url("../../static/fonts/RedHatText/RedHatTextVF.woff2") format("woff2"); font-weight: 300 700; }
@font-face { font-family: "Red Hat Display"; src: url("../../static/fonts/RedHatDisplay/RedHatDisplayVF.woff2") format("woff2"); font-weight: 300 900; }
@font-face { font-family: "Red Hat Mono";    src: url("../../static/fonts/RedHatMono/RedHatMonoVF.woff2") format("woff2"); font-weight: 300 700; }
@font-face { font-family: "DM Serif Display"; src: url("DMSerifDisplay-Regular.woff2") format("woff2"); }
```

Nur DM Serif Display für die Signatur bringt das Addon selbst mit (24 KB). Alles bleibt lokal; die Standard-CSP von Cockpit muss nicht gelockert werden.

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

**cronctl (Cron Jobs)** hat heute eigene Variablen mit Blau `#0066cc` und folgt nur `prefers-color-scheme`.

- Variablen und Bausteine durch `azentrius.css` ersetzen; Karten, Tabelle, Schalter und Dialog auf die `az-`-Klassen umstellen.
- Dialog: „Speichern“ zuerst und links, „Abbrechen“ als Link-Button danach.
- Deaktivierte Jobs nicht mehr durchstreichen, sondern `is-off`.
- Zeitplan lesbar zuerst, Rohzeile darunter in Mono.
- Signatur am Seitenende.

**diun (Image-Updates)** ist heute immer dunkel mit Gold-Buttons und goldenen Status-Badges.

- Hell und Dunkel nach Cockpit-Einstellung statt fest dunkel.
- „Aktualisieren“ als Cockpit-blauer Button statt Gold.
- Status über die Statussprache: „Update verfügbar“ als `az-label--info`, „Aktuell“ als `az-label--success`, unbekannt als neutrales Label. Gold verschwindet aus dem Status.
- Seitentitel „Image-Updates“ ohne das kursive „monitor“; die Marke wandert in die Signatur.
- Die schmale Ansicht als Blöcke bleibt und wird zu `az-table--stack`.

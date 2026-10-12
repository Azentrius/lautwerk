# Grundlagen

Azentrius baut Werkzeuge, die in fremden Oberflächen leben – Cockpit-Addons, später Nextcloud-Apps – und eigene Apps wie Sanctum. Dieses System sorgt dafür, dass beides zusammengehört, ohne dass ein Addon im Gastgeber fremd wirkt.

## Der Grundsatz: Zu Gast nativ, zu Hause Azentrius

- **Zu Gast** (Cockpit, Nextcloud): Das Addon sieht aus wie ein Teil des Gastgebers. Farben, Schriften, Abstände, Radien, Hell/Dunkel und Satzbau kommen vom Gastgeber. Wer die Seite öffnet, soll nicht merken, dass sie nachgerüstet ist.
- **Zu Hause** (eigene Apps, azentrius.de): Die Azentrius-Themen gelten: Tinte, Papier und Gold, DM Sans als Schrift. Im Dunkeln ist Gold die Aktionsfarbe, im Hellen Tinte mit goldenen Akzenten.
- **Überall gleich** bleiben die Dinge, an denen man Azentrius wiedererkennt: dieselben Bausteine mit denselben Namen (`az-…`), dieselbe Statussprache, derselbe knappe Ton und die Signatur im Seitenfuß.

Die vier Themen setzen das um. Wähle das Thema nach dem Ort, nie nach Geschmack:

| Thema | gilt in | Aktionsfarbe |
|---|---|---|
| Cockpit hell / Cockpit dunkel | jedem Cockpit-Addon, je nach Cockpit-Einstellung | `primary` = Cockpit-Blau |
| Azentrius dunkel / Azentrius hell | eigenen Apps, je nach Systemeinstellung | `primary` = Gold auf Tinte / Tinte auf Papier |

Die Cockpit-Themen übernehmen PatternFly 6, die Grundlage von Cockpit seit Version 337, Wert für Wert. Ändere dort nichts nach Gefühl: Ein Addon, das ein eigenes Blau oder eigene Radien mitbringt, sieht neben Cockpits eigenen Seiten sofort falsch aus.

## Farbe

- Baue jede Seite aus drei Ebenen: `bg-page` als Grund, darauf `bg-card` für Karten und Tabellen, darüber `bg-floating` für Dialoge und Menüs.
- Text steht in `text`, Nebensächliches in `text-subtle`. Beide sind auf allen drei Flächen in allen vier Themen mindestens 4,5:1 lesbar.
- `primary` ist die eine Aktionsfarbe: Hauptbutton, aktiver Schalter, Auswahl. Text auf `primary` ist immer `on-primary`, nie fest Weiß – im Dunkeln steht dunkler Text auf hellem Blau oder Gold. Text und Rahmen in Aktionsfarbe (Secondary-Button) nehmen `primary-text`.
- Im Thema „Azentrius hell“ ist `primary` Tinte (`#0a0a0f`), nicht Gold: Gold ist auf Weiß als Schrift unlesbar, und abgedunkeltes Gold wird zu Olivbraun. Gold bleibt dort Akzent – Links, Fokusring, der Punkt der Wortmarke.
- Status trägt eigene Farben (`status-success`, `status-warning`, `status-danger`, `status-info` und die getönten `tint-…` für Labels). Status steht nie allein in Farbe: immer mit Icon und Wort.
- `signature` ist das Gold der Marke als Akzent: im hellen Thema ein dunkles Bronze (`#7d5f22`), im dunklen das Markengold (`#c8a96e`). In Cockpit erscheint es genau einmal: im Punkt der Wortmarke in der Signatur.
- `brand-gold`, `brand-ink` und `brand-paper` sind feste Markenflächen für App-Icon, Splash und Projektseiten. Gold auf Weiß ist als Text mit 2,2:1 unlesbar; im Hellen ist Gold darum Fläche, nie Schrift.
- Gold ist nie ein Status. Es sagt „das ist Azentrius“ oder „das ist die Hauptaktion“ (nur in eigenen Apps), nichts anderes.

Bekannte Ausnahme: `status-warning` im Thema „Cockpit hell“ ist der PatternFly-Originalwert `#dca614` mit 2,2:1 auf Weiß. Er bleibt, damit das Gelb dem von Cockpit gleicht. Warnungen tragen deshalb immer das Dreieck-Icon und einen Titel.

## Schrift

- In Cockpit: Red Hat Text für alles, Red Hat Display für Titel, Red Hat Mono für Befehle, Pfade und Digests. Grundgröße 14 px (`body`). Cockpit liefert diese Schriften selbst mit; ein Addon lädt sie von dort und bringt keine eigenen mit.
- In eigenen Apps: DM Sans in `app-title`, `app-dialog-title`, `app-section`, `app-body` (15 px), `app-control` und `app-label`.
- DM Serif Display ist der Markenton. Er erscheint in der Signatur (`signature`) und in großen Markenmomenten (`brand-display`), nie in Bedienelementen.
- Schriften kommen immer aus eigenen Dateien. Kein Google Fonts, kein Bunny Fonts, keine externe Anfrage – Cockpits Content-Security-Policy würde sie ohnehin blockieren, und es ist das DSGVO-Versprechen der Marke.
- Technisches zuerst lesbar, dann roh: „Montags um 03:00“ in `body`, darunter `0 3 * * 1` in `code`.

## Raster, Radien, Tiefe

- Abstände nur aus `space-xs` (4) · `space-sm` (8) · `space-md` (16) · `space-lg` (24) · `space-xl` (32) · `space-2xl` (48). Karten und Dialoge haben innen `space-lg`, Karten untereinander `space-lg`.
- Alles Bedienbare ist eine Pille (`radius-pill`): Buttons, Labels, Schalter. Karten und Hinweise haben `radius-medium` (16), Dialoge `radius-large` (24), Eingabefelder `radius-small` (6).
- Karten liegen flach mit `line`-Rahmen. Schatten nur für Schwebendes: `shadow-md` für Menüs, `shadow-lg` für Dialoge.
- Keine Verläufe, keine farbigen Randstreifen, keine Akzentlinien an Karten.

## Statussprache

Dieselben fünf Zustände in jedem Addon und jeder App, jeweils mit festem Icon:

| Zustand | Label | Icon | Wörter |
|---|---|---|---|
| ohne Wertung | `az-label` | `ban` | Deaktiviert, Unbekannt |
| gut | `az-label--success` | `check-circle` | Aktuell, Läuft, Gespeichert |
| Hinweis | `az-label--info` | `info-circle` | Update verfügbar, Neu |
| Achtung | `az-label--warning` | `exclamation-triangle` | Neustart nötig, Läuft bald ab |
| Fehler | `az-label--danger` | `exclamation-circle` | Fehlgeschlagen, Nicht erreichbar |

Ein Diun-Update ist ein Hinweis, kein Fehler. Ein Backup, das seit 36 Stunden fehlt, ist ein Fehler.

Getönte Labels sind der Normalfall. Ein kräftiges Label (`az-label--strong`) steht immer allein, nie in einer Reihe mit getönten – gemischte Gewichte wirken unruhig.

## Sprache

- Ansprache folgt dem Gastgeber: in Cockpit „Sie“, wie Cockpits eigene deutsche Übersetzung; in eigenen Apps „du“, wie azentrius.de.
- Quelltexte im Code auf Englisch, Deutsch als erste Übersetzung über `cockpit.gettext`. So funktionieren die Addons für jede Cockpit-Sprache und lassen sich veröffentlichen.
- Buttons sind Verb plus Objekt: „Job hinzufügen“, „Neu laden“, „Job löschen“. Nie „OK“, „Absenden“, „Hier klicken“.
- Fehlermeldungen sagen, was passiert ist und wie man es behebt: „Die Crontab konnte nicht geschrieben werden. Für Änderungen braucht diese Sitzung administrativen Zugriff.“ Keine Entschuldigungen, keine Ausrufezeichen, keine Emojis.
- Satzanfang groß, sonst normale Rechtschreibung. Keine Großbuchstaben-Überschriften.
- Zahlen und Zeiten wie im Alltag: „vor 3 Stunden“, „Montags um 03:00“, „1,67 GiB“.

## Icons

- Die PatternFly-Icons, die auch Cockpit verwendet. Die Gruppe „Icons“ enthält die bisher gebrauchten; weitere kommen aus demselben Satz (`@patternfly/react-icons`, MIT).
- In der Oberfläche als Inline-SVG mit `class="az-icon"` und `fill: currentColor`, damit sie die Textfarbe übernehmen. Die Dateien selbst sind in `#151515` gezeichnet, nur zur Ansicht.
- 16 px neben Text, 48 px im leeren Zustand. Icon-Buttons immer mit `aria-label`.

## Marke

- Die Wortmarke ist „Azentrius.“ in DM Serif Display: das Wort in `text`, der Punkt in `signature`. Ein Bildzeichen gibt es in diesem System noch nicht.
- Das rote Hexagon und die rote Palette von azentrius.de gehören nicht zu diesem System. Wenn die Website auf Gold umgestellt wird, kommt das Zeichen in neuer Farbe hierher.
- In Cockpit erscheint die Marke genau einmal: in der Signatur am Seitenende (Baustein `Signature`).

## Barrierefreiheit

- Text 4,5:1, Bedienrahmen, Fokus und Icons 3:1 auf jeder Fläche, in allen vier Themen. Die Werte sind geprüft; Ausnahme siehe Farbe.
- Fokus ist ein 2 px durchgehender Ring in `focus`, 2 px Abstand zum Element.
- Bewegung nur unter 150 ms und mit `prefers-reduced-motion` abschaltbar.
- Jede Tabelle wird unter 640 px zu Blöcken (`az-table--stack`). Bedienelemente sind am Desktop mindestens 29 px hoch, unter 640 px mindestens 44 px – Cockpit wird auch vom Handy aus bedient. In nativen Android-Apps gilt stattdessen 48 × 48 dp (Abschnitt „Android“).

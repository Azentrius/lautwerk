# Grundlagen

Azentrius baut Werkzeuge, die in fremden Oberflächen leben – Cockpit-Addons, später Nextcloud-Apps – und eigene Apps wie Sanctum. Dieses System sorgt dafür, dass beides zusammengehört, ohne dass ein Addon im Gastgeber fremd wirkt. Dazu kommen die Projektseiten, die Azentrius nach außen zeigen: azentrius.de und das Profil der GitHub-Organisation.

## Der Grundsatz: Azentrius erkennbar, der Gastgeber bestimmt Hell und Dunkel

- **Cockpit-Addons, eigene Apps und Projektseiten** tragen die Azentrius-Themen: Tinte, Papier und Gold, DM Sans als Schrift. Im Dunkeln ist Gold die Aktionsfarbe, im Hellen Tinte mit goldenen Akzenten. Hell oder Dunkel folgt in Cockpit dessen Einstellung, in Apps und auf azentrius.de der des Geräts, im GitHub-Profil der von GitHub. So gilt es seit dem 12. Oktober 2026; vorher trugen die Addons Cockpits eigene Farben.
- **Zu Gast in Nextcloud** sieht eine App aus wie ein Teil des Gastgebers: Farben, Hell/Dunkel und Kontrast kommen von Nextcloud (Abschnitt „Weitere Addons und Apps“).
- **Überall gleich** bleiben die Dinge, an denen man Azentrius wiedererkennt: dieselben Bausteine mit denselben Namen (`az-…`), derselbe Seitenrahmen aus Kopf, Reitern und Signatur, dieselbe Statussprache und derselbe knappe Ton.
- **Projektseiten** stellen Azentrius und seine Projekte vor und dürfen dafür größer und markenbetonter auftreten als eine Bedienoberfläche. Ihre Ausnahmen stehen gesammelt im Abschnitt „Projektseiten“; alles andere gilt dort unverändert.

Die vier Themen setzen das um. Wähle das Thema nach dem Ort, nie nach Geschmack:

| Thema | gilt in | Aktionsfarbe |
|---|---|---|
| Azentrius dunkel / Azentrius hell | Cockpit-Addons nach Cockpits Einstellung, eigene Apps und Projektseiten nach Systemeinstellung | `primary` = Gold auf Tinte / Tinte auf Papier |
| Cockpit hell / Cockpit dunkel | derzeit keiner Oberfläche; sie bleiben für Ansichten, die bewusst wie Cockpit selbst aussehen müssen | `primary` = Cockpit-Blau |

Die Cockpit-Themen übernehmen PatternFly 6, die Grundlage von Cockpit seit Version 337, Wert für Wert. Wer sie einsetzt, ändert dort nichts nach Gefühl.

## Farbe

- Baue jede Seite aus drei Ebenen: `bg-page` als Grund, darauf `bg-card` für Karten und Tabellen, darüber `bg-floating` für Dialoge und Menüs.
- Text steht in `text`, Nebensächliches in `text-subtle`. Beide sind auf allen drei Flächen in allen vier Themen mindestens 4,5:1 lesbar.
- `primary` ist die eine Aktionsfarbe: Hauptbutton, aktiver Schalter, Auswahl. Text auf `primary` ist immer `on-primary`, nie fest Weiß – im Dunkeln steht dunkler Text auf hellem Blau oder Gold. Text und Rahmen in Aktionsfarbe (Secondary-Button) nehmen `primary-text`.
- Im Thema „Azentrius hell“ ist `primary` Tinte (`#0a0a0f`), nicht Gold: Gold ist auf Weiß als Schrift unlesbar, und abgedunkeltes Gold wird zu Olivbraun. Gold bleibt dort Akzent – Links, Fokusring, der Punkt der Wortmarke.
- Status trägt eigene Farben (`status-success`, `status-warning`, `status-danger`, `status-info` und die getönten `tint-…` für Labels). Status steht nie allein in Farbe: immer mit Icon und Wort.
- `signature` ist das Gold der Marke als Akzent: im hellen Thema ein dunkles Bronze (`#7d5f22`), im dunklen das Markengold (`#c8a96e`). Es erscheint im Punkt der Wortmarke, im Punkt eines Produktnamens ohne Zusatz, im Namenszusatz und in der Linie unter dem aktiven Reiter. Auf Projektseiten und Covern ist es außerdem Markenakzent: in Eyebrows, in Aufzählungspunkten und im Schlusspunkt der Hero-Überschrift.
- `brand-gold`, `brand-ink` und `brand-paper` sind feste Markenflächen für App-Icon, Splash und Projektseiten. Gold auf Weiß ist als Text mit 2,2:1 unlesbar; im Hellen ist Gold darum Fläche, nie Schrift.
- Gold ist nie ein Status. Es sagt „das ist Azentrius“ oder „das ist die Hauptaktion“, nichts anderes.

Bekannte Ausnahme: `status-warning` im Thema „Cockpit hell“ ist der PatternFly-Originalwert `#dca614` mit 2,2:1 auf Weiß. Er bleibt, damit das Gelb dem von Cockpit gleicht. Warnungen tragen deshalb immer das Dreieck-Icon und einen Titel.

## Schrift

- In Cockpit-Addons und eigenen Apps: DM Sans für die Oberfläche, Grundgröße 15 px; in Apps in `app-title`, `app-dialog-title`, `app-section`, `app-body`, `app-control` und `app-label`. Befehle, Pfade und Digests stehen in Red Hat Mono, das Cockpit selbst mitliefert.
- Auf Projektseiten gilt die Schriftgruppe „Projektseite“: Fließtext 17 px, dazu Hero, Abschnittstitel, Eyebrow, Kennzahl, Einleitung, Kartentitel und Wortmarke im Kopf.
- Red Hat Text und Red Hat Display gehören nur zu den Cockpit-Themen.
- DM Serif Display ist der Markenton. Er erscheint in der Signatur (`signature`), kursiv im Namenszusatz, in großen Markenmomenten (`brand-display`) und auf Projektseiten in Überschriften, Kennzahlen und Kartentiteln, nie in Bedienelementen.
- Kursiv ist allein der Namenszusatz (siehe Marke), überall dort, wo der Name gestaltet erscheint: im Seitenkopf der Addons, im Kopf der Apps, auf Projektseiten und Covern. Sonst steht nichts kursiv, auch keine Hervorhebung.
- Schriften kommen immer aus eigenen Dateien. Kein Google Fonts, kein Bunny Fonts, keine externe Anfrage – Cockpits Content-Security-Policy würde sie ohnehin blockieren, und es ist das DSGVO-Versprechen der Marke.
- Technisches zuerst lesbar, dann roh: „Montags um 03:00“ in `body`, darunter `0 3 * * 1` in `code`.

## Raster, Radien, Tiefe

- Abstände nur aus `space-xs` (4) · `space-sm` (8) · `space-md` (16) · `space-lg` (24) · `space-xl` (32) · `space-2xl` (48). Karten und Dialoge haben innen `space-lg`, Karten untereinander `space-lg`. Projektseiten bekommen für ihre großen Abschnitte zusätzlich `space-section` (112), `space-hero-end` (88), `space-columns` (80) und `space-cards` (40).
- Alles Bedienbare ist eine Pille (`radius-pill`): Buttons, Labels, Schalter. Karten und Hinweise haben `radius-medium` (16), Dialoge `radius-large` (24), Eingabefelder `radius-small` (6). Projektkarten auf Projektseiten haben ebenfalls `radius-large`.
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

Ein Reifegrad wie „Prototyp“ ist kein Status, sondern eine Kategorie: ein neutrales `az-label` ohne Icon, nie in Gold.

## Sprache

- Ansprache folgt dem Gastgeber: in Cockpit „Sie“, wie Cockpits eigene deutsche Übersetzung; in eigenen Apps und auf Projektseiten „du“, wie azentrius.de.
- Quelltexte im Code auf Englisch, Deutsch als erste Übersetzung. Die Addons führen dafür eigene Übersetzungstabellen für Englisch und Deutsch statt `cockpit.gettext`; andere Sitzungssprachen sehen Englisch. Diese Ausnahme gilt seit dem 16. Oktober 2026: Bei zwei Sprachen bringt der Umstieg auf `cockpit.gettext` wenig. Kommt eine dritte Sprache dazu, wird er neu entschieden.
- Buttons sind Verb plus Objekt: „Job hinzufügen“, „Job löschen“. Feste Ausnahmen, die überall gleich heißen: „Aktualisieren“, „Update“, „Abbrechen“ und auf Entscheidungskarten „Erlauben“, „Ablehnen“ und „In der App“. Eine Entscheidungskarte zeigt eine Anfrage, die jemand beantwortet, etwa eine Freigabe in Stellwerk; worum es geht, steht auf der Karte, darum braucht die Antwort kein Objekt. Nie „OK“, „Absenden“, „Hier klicken“.
- Fehlermeldungen sagen, was passiert ist und wie man es behebt: „Die Crontab konnte nicht geschrieben werden. Für Änderungen braucht diese Sitzung administrativen Zugriff.“ Keine Entschuldigungen, keine Ausrufezeichen, keine Emojis.
- Satzanfang groß, sonst normale Rechtschreibung. Keine Großbuchstaben-Überschriften. Die einzige Ausnahme sind Eyebrows auf Projektseiten und Covern (Abschnitt „Projektseiten“), nie in Bedienoberflächen.
- Zahlen und Zeiten wie im Alltag: „vor 3 Stunden“, „Montags um 03:00“, „1,67 GiB“.

## Icons

- Der Icon-Satz aus `@patternfly/react-icons` 6.4.0, den auch Cockpit verwendet. Die Gruppe „Icons“ enthält die bisher gebrauchten; weitere kommen aus demselben Paket.
- Herkunft und Lizenz: Fast alle Icons dieses Pakets sind Font Awesome Free 5 (Fonticons, Inc.) unter CC BY 4.0, mit Pflicht zur Namensnennung. Nur PatternFlys eigene Icons, hier `pending`, stehen unter MIT. Welches Icon woher stammt, steht in der README der Gruppe „Icons“; jede App und jedes Addon nennt die Herkunft der Icons, die es übernimmt.
- In der Oberfläche als Inline-SVG mit `class="az-icon"` und `fill: currentColor`, damit sie die Textfarbe übernehmen. Die Dateien selbst sind in `#151515` gezeichnet, nur zur Ansicht.
- Neben Text ist ein Icon so groß wie die Schrift daneben (`az-icon` hat 1em): 14 px in Buttons, 12 px in Labels. Ein Icon, das frei steht, hat 16 px, im leeren Zustand 48 px. Icon-Buttons immer mit `aria-label`.

## Marke

- Die Wortmarke ist „Azentrius.“ in DM Serif Display: das Wort in `text`, der Punkt in `signature`.
- Das Zeichen für Azentrius selbst ist „A.“: das A der Wortmarke in `brand-paper` auf `brand-ink`, der Punkt in `brand-gold` (Gruppe „Marke“). Es steht dort, wo die Wortmarke keinen Platz hat: als Favicon von azentrius.de und als Bild der GitHub-Organisation. Es wird nie nachgezeichnet, nur die Datei verwendet.
- Jede eigene App hat ein eigenes Zeichen in `brand-gold` auf `brand-ink`, das den Punkt der Wortmarke trägt. Bisher gibt es zwei: Lautwerk, eine Sprechblase mit Punkt, und Stellwerk, ein Formsignal auf Fahrt mit Punkt (Gruppe „App-Icons“). Der Aufbau steht im Abschnitt „Android“ und gilt für Web-Apps genauso.
- **Produktnamen** werden nicht übersetzt und heißen in jeder Sprache gleich. Jeder Name trägt ein Markenzeichen: Hat er einen Zusatz, steht der Hauptname in der Schrift des Orts in `text` und der Zusatz in DM Serif Display kursiv in `signature`. Hat er keinen, trägt er den Punkt der Wortmarke in `signature`.

  | Produkt | gestaltet | in reinem Text | Repo und Paket |
  |---|---|---|---|
  | Lautwerk | Lautwerk. (Punkt in `signature`) | Lautwerk | `lautwerk` |
  | Image Update Monitor | Image Update *Monitor* | Image Update Monitor | `cockpit-image-update-monitor` |
  | Cron Jobs | Cron Jobs. (Punkt in `signature`) | Cron Jobs | `cockpit-cronjobs` |
  | Stellwerk | Stellwerk. (Punkt in `signature`) | Stellwerk | `stellwerk` |
  | Design System | Design *System* | Design System | `design-system` |

- Gestaltet erscheint ein Name im Seitenkopf (Baustein `PageHeader`), im Kopf der Apps, auf Projektseiten, auf Covern und Bannern. In reinem Text steht er ohne Kursiv und ohne Punkt, sonst liest sich der Punkt als Satzzeichen: im Cockpit-Menü, in AppStream und Paketbeschreibungen, in README-Überschriften und in der Signatur („Cron Jobs 2.1.0“).
- „for Cockpit“ und ähnliche Zusätze gehören nicht zum Namen. Dass etwas ein Cockpit-Add-on ist, sagt ein Eyebrow oder die Beschreibung. Im deutschen Fließtext bleibt „Designsystem“ ein gewöhnliches Wort.
- Die Wortmarke „Azentrius.“ steht in Bedienoberflächen genau einmal: in der Signatur am Seitenende (Baustein `Signature`). Auf Projektseiten steht sie im Kopf, als Link zur Startseite, und im Fuß. Ein Cover oder Banner darf sie oben tragen; sie zählt dann als die eine Wortmarke der Seite.

## Barrierefreiheit

- Text 4,5:1, Bedienrahmen, Fokus und Icons 3:1 auf jeder Fläche, in allen vier Themen. Die Werte sind geprüft; Ausnahme siehe Farbe.
- Fokus ist ein 2 px durchgehender Ring in `focus`, 2 px Abstand zum Element.
- Bewegung nur unter 150 ms und mit `prefers-reduced-motion` abschaltbar.
- Jede Tabelle wird unter 640 px zu Blöcken (`az-table--stack`). Bedienelemente sind am Desktop mindestens 29 px hoch, unter 640 px mindestens 44 px – Cockpit wird auch vom Handy aus bedient. Das gilt auch auf Projektseiten. In nativen Android-Apps gilt stattdessen 48 × 48 dp (Abschnitt „Android“).

## Lizenz

- Der Code dieses Systems steht unter Apache-2.0, wie die Addons, die ihn ausliefern: `tokens.json`, `tokens.css`, `components/bundle.css`, die daraus erzeugte `azentrius.css` und `azentrius-theme.js`.
- Alle Rechte vorbehalten bleiben für die Marke: die Wortmarke „Azentrius.“, das Zeichen „A.“, die Zeichen der Apps, die Produktnamen und die Grafiken. Ebenso für die Texte dieses Systems.
- Bestandteile Dritter behalten ihre Lizenzen: DM Sans, DM Serif Display und die Red-Hat-Schriften unter der SIL Open Font License 1.1, die Icons unter CC BY 4.0 (Font Awesome Free) und MIT (PatternFly `pending`).

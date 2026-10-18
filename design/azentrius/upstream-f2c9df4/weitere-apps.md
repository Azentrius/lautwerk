# Weitere Addons und Apps

Dieselbe Regel wie in Cockpit: Wo Azentrius zu Gast ist, gelten die Werte des Gastgebers; die Bausteine, die Statussprache und die Signatur bleiben.

## Nextcloud-Apps

Nextcloud ist ein Gastgeber wie Cockpit, mit eigenem, vom Admin einstellbarem Theming. Eine Azentrius-App für Nextcloud setzt die Bedeutungsnamen dieses Systems auf Nextclouds CSS-Variablen, statt eigene Farben mitzubringen. So übernimmt sie Akzentfarbe, Hell/Dunkel und Kontrastmodus der jeweiligen Instanz.

| Azentrius | Nextcloud |
|---|---|
| `bg-page`, `bg-card` | `--color-main-background` |
| `bg-hover` | `--color-background-hover` |
| `text`, `text-subtle` | `--color-main-text`, `--color-text-maxcontrast` |
| `line` | `--color-border` |
| `primary`, `on-primary` | `--color-primary-element`, `--color-primary-element-text` |
| `status-success`, `-warning`, `-danger`, `-info` | `--color-success`, `--color-warning`, `--color-error`, `--color-info` |

Die Namen auf der rechten Seite vor der ersten Nextcloud-App gegen die laufende Version (Nextcloud 34) prüfen. Die Signatur steht dort im Einstellungsbereich der App, nicht in der Hauptansicht.

## Eigene Apps (Sanctum und folgende)

Hier gelten die Themen „Azentrius dunkel“ und „Azentrius hell“, umgeschaltet nach der Systemeinstellung des Geräts.

- `primary` ist im Dunkeln Gold `#c8a96e` mit Tinte darauf, im Hellen Tinte `#0a0a0f` mit Weiß darauf. Gold bleibt im Hellen Akzent: Links, Fokusring, der Punkt der Wortmarke.
- Schrift DM Sans (`app-title`, `app-body`, `app-label`), Markenmomente in DM Serif Display (`brand-display`). Ein Namenszusatz im App-Namen steht kursiv in DM Serif Display und `signature`; ein Name ohne Zusatz trägt den Punkt der Wortmarke in `signature`, wie „Lautwerk.“ (Grundlagen, Marke).
- Geometrie wie in Cockpit: Pillen-Buttons, Karten mit 16 px Radius, Dialoge mit 24 px. Wer zwischen Cockpit-Addon und App wechselt, findet dieselben Formen.
- Für Ionic: `--ion-color-primary` = `primary`, `--ion-color-primary-contrast` = `on-primary`, `--ion-background-color` = `bg-page`, `--ion-text-color` = `text`, `--ion-font-family` = DM Sans. Die Android-Darstellung (`mode: "md"`) bleibt, nur die Werte kommen von hier.
- App-Symbol und Startbild: `brand-ink` als Grund, darauf das Zeichen der App in `brand-gold`, mit dem Punkt der Wortmarke. Aufbau, Startbild und Benachrichtigung für Android stehen im Abschnitt „Android“.
- Native Android-Apps (Jetpack Compose oder Views): Einheiten, Berührungsflächen, Bausteine mit Zuständen sowie Schrift- und Icon-Dateien stehen im Abschnitt „Android“.
- Android Auto ist ein Gastgeber eigener Art: Der Host zeichnet die Vorlagen, die App liefert Inhalte und fordert ein Akzentfarbpaar an. Welche Farben und welcher Hintergrund erscheinen, entscheidet der Host. Regeln stehen im Abschnitt „Android Auto“.

## Ein neuer Gastgeber

Für jede weitere Plattform zuerst zwei Fragen klären: Welche Variablen setzt der Gastgeber für Hell/Dunkel und Akzent? Wo darf eine Signatur stehen, ohne zu stören? Dann eine Zuordnung wie oben anlegen und als eigenes Themenpaar hier ergänzen.

# Home Assistant

Home Assistant ist ein Gastgeber eigener Art: Hier trägt kein Addon die Azentrius-Themen, sondern die ganze Oberfläche. Das Azentrius Theme färbt Home Assistant in Tinte, Papier und Gold und setzt DM Sans als Schrift. Aufbau, Größen, Abstände, Icons und die Farben der Gerätezustände bleiben beim Gastgeber. Grundlage ist Home Assistant 2026.10 mit dem Frontend 20260930.4.

## Themen

- Ein Theme mit zwei Modi: `light` trägt die Werte von „Azentrius hell“, `dark` die von „Azentrius dunkel“. Welcher Modus gilt, bestimmt Home Assistant nach der Einstellung im Profil („Automatisch“, „Hell“, „Dunkel“), so wie Cockpit für die Addons.
- `primary` ist wie überall im Hellen Tinte mit Weiß darauf, im Dunkeln Gold mit Tinte darauf: Buttons, Schalter, Schieberegler, Reiter und der gewählte Eintrag der Seitenleiste. Im Hellen bleibt Gold Akzent, in Links und im Fokusring.
- Kopf und Seitenleiste liegen auf `bg-page`, die Karten auf `bg-card` mit `line`-Rahmen und `radius-medium`, flach und ohne Schatten. Dialoge haben `bg-floating` und `radius-large`.
- Eingabefelder im üblichen Stil mit Unterlinie haben eine eigene Fläche: im Hellen `bg-hover`, beim Überfahren `tint-neutral`; im Dunkeln liegen sie vertieft auf `bg-page`, beim Überfahren `bg-card`. Im Dunkeln ist `bg-hover` so hell wie `bg-floating`, ein Feld im Dialog wäre dort nur an der Unterlinie zu erkennen. `tint-neutral` hebt sich zwar ab, trägt aber die Beschriftung in `text-subtle` nur mit 4,35:1.
- Die Initialen im Benutzer-Avatar unten in der Seitenleiste stehen in `on-tint-neutral` auf `tint-neutral`. Ohne eigenen Wert nähme Home Assistant `#212121`, im Dunkeln auf `#26262f` kaum zu sehen.
- Der Zähler und der Punkt in der Seitenleiste, die auf neue Meldungen hinweisen, stehen in `status-info` mit `on-info`. Home Assistant färbt sie mit seiner Akzentfarbe; Gold ist nie ein Status, darum ist es hier nicht Gold.

## Dateien

```
/config/
  configuration.yaml
  themes/
    azentrius.yaml            ← das Theme
  www/azentrius/
    azentrius.js              ← lädt die Schriften
    DMSans-VF.woff2           ← aus fonts/
    RedHatMono-VF.woff2       ← aus fonts/
    OFL-DM-Sans.txt           ← Lizenz der Schrift
    OFL-Red-Hat-Fonts.txt     ← Lizenz der Schrift
```

`themes/azentrius.yaml` und `www/azentrius/azentrius.js` liegen in diesem System unter `home-assistant/`. Beide entstehen mit `scripts/home-assistant-theme.py` aus `tokens.json` und werden nie von Hand geändert. Das Skript prüft dabei die Kontraste aller Farbpaare, die das Theme neu bildet, in beiden Modi, auch die Paare, die Home Assistant aus zwei Variablen des Themes zusammensetzt, etwa die Initialen im Avatar und die Beschriftung auf einem Eingabefeld. Mit `--config <ordner>` legt es den ganzen Baum oben an, samt Schriften und Lizenztexten.

Alles unter `www/` liefert Home Assistant ohne Anmeldung unter `/local/` aus. Dort liegen deshalb nur das Skript, die Schriften und ihre Lizenzen.

Über HACS installiert liegt das Theme unter `themes/azentrius/azentrius.yaml` (Abschnitt „Veröffentlichung“); Home Assistant liest beide Orte.

## Einbinden

In `configuration.yaml`:

```yaml
frontend:
  themes: !include_dir_merge_named themes
  extra_module_url:
    - /local/azentrius/azentrius.js
```

- `themes` lädt jede Datei im Ordner `themes/`. Ein neues oder geändertes Theme übernimmt die Aktion „Themes neu laden“ (`frontend.reload_themes`) ohne Neustart.
- `extra_module_url` lädt `azentrius.js` auf jeder Seite, auch in den Einstellungen und in der App. Das Skript meldet DM Sans und Red Hat Mono aus `/local/azentrius/` an, ohne externe Anfrage. Es wirkt erst nach einem Neustart von Home Assistant; bis dahin steht die Oberfläche in der Ersatzschrift `system-ui`.
- Auswählen: für die eigene Person im Profil unter „Theme“, für alle als Standard mit der Aktion `frontend.set_theme` und `name: Azentrius`. Eine Wahl im Profil geht dem Standard vor.

## Zuordnung

| Azentrius | Home Assistant |
|---|---|
| `bg-page` | `primary-background-color`, `app-header-background-color`, `sidebar-background-color` |
| `bg-card` | `card-background-color`, `ha-card-background` |
| `bg-floating` | `ha-dialog-surface-background`, `ha-color-surface-default` |
| `bg-hover`, `tint-neutral` | `secondary-background-color`, die ruhigen Füllungen `ha-color-fill-…-quiet-…` und `-normal-…` |
| im Hellen `bg-hover`, `tint-neutral`, im Dunkeln `bg-page`, `bg-card` | `ha-color-form-background`, `ha-color-form-background-hover` |
| `tint-neutral`, `on-tint-neutral` | `light-primary-color`, `text-light-primary-color` (Initialen im Avatar) |
| `text`, `text-subtle`, `text-disabled` | `primary-text-color`, `secondary-text-color`, `disabled-text-color` und `ha-color-text-…` |
| `line`, `line-control` | `divider-color`, `ha-card-border-color`, `ha-color-border-neutral-…` |
| `primary`, `primary-hover`, `on-primary` | `primary-color`, `ha-color-fill-primary-loud-…`, `ha-color-on-primary-loud`, `ha-switch-checked-…` |
| `primary-text` | `sidebar-selected-icon-color`, `ha-color-on-primary-normal`, `ha-color-on-primary-quiet` |
| `link`, `focus` | `ha-color-text-link`, `ha-color-focus` |
| `status-…`, `tint-…`, `on-…` | `success-color`, `warning-color`, `error-color`, `info-color` und `ha-color-fill-{success,warning,danger}-…` |
| `status-info`, `on-info` | `accent-color`, `text-accent-color` |
| `shadow-sm`, `shadow-md`, `shadow-lg` | `ha-box-shadow-s`, `-m`, `-l` |
| `radius-medium`, `radius-large` | `ha-card-border-radius`, `ha-dialog-border-radius` |
| DM Sans, Red Hat Mono | `ha-font-family-body`, `-heading`, `-longform`, `-code`; Gewicht der Titel wie `app-title` |

Die vollständige Zuordnung, gut 150 Werte je Modus, steht in `home-assistant/themes/azentrius.yaml`. Home Assistant bildet zu jedem Hex-Wert selbst die `rgb-…`-Variante.

## Was beim Gastgeber bleibt

- **Gerätezustände.** Bernstein für eingeschaltetes Licht, Orange fürs Heizen, Blau fürs Kühlen und alle anderen `state-…`-Farben bleiben die von Home Assistant. Sie sind seine Sprache für Geräte, nicht die Statussprache dieses Systems. Gold wird nie dazu. Einige liegen im hellen Modus unter 3:1, etwa Bernstein auf Weiß mit 1,6:1; Home Assistant zeigt den Zustand dort zusätzlich als Text und getönte Fläche. Wie `status-warning` in „Cockpit hell“ ist das eine bewusste Ausnahme zugunsten des Gastgebers.
- **Symbole von Entitäten ohne eigene Zustandsfarbe**, etwa Sensoren, stehen in `text-subtle` statt in Home Assistants Blaugrau.
- **Größen und Abstände.** Die Grundgröße bleibt 14 px, ebenso die Abstände und die Radien der Eingabefelder. Buttons sind in Home Assistant schon Pillen. Das Theme ändert nur die Schriftart und das Gewicht der Titel.
- **Eingabefelder** im umrandeten Stil haben in Ruhe einen Rahmen in `line`, wie Home Assistant selbst unter 3:1; beim Überfahren `line-control`, im Fokus `primary`. Die üblichen Felder mit Unterstrich erreichen 3:1 mit `text-subtle`.
- **Icons, Texte und Ansprache** kommen von Home Assistant, ebenso die Material Design Icons.
- **Keine Signatur.** Home Assistant hat keinen Fuß, der zu Azentrius gehört, und das Theme ändert nur Werte, keine Inhalte. Die Wortmarke erscheint dort nicht.

## Veröffentlichung

Das Theme ist öffentlich: `Azentrius/home-assistant-theme` auf GitHub, unter Apache-2.0 und auf Englisch. Es enthält dieselben Werte, erzeugt mit `scripts/home-assistant-theme.py --lang en --config <ordner des Repos>`, dazu die Schriften mit ihren Lizenzen, Prüfskripte und einen Release-Workflow. Jedes Release trägt das Archiv für den Konfigurationsordner, `azentrius.yaml` einzeln und `SHA256SUMS`, mit signierter Herkunft.

- **Mit HACS:** das Repo als benutzerdefiniertes Repository vom Typ „Theme“ eintragen und „Azentrius“ herunterladen. HACS legt alle Dateien des Releases nach `/config/themes/azentrius/`, Home Assistant liest daraus nur `azentrius.yaml`. Die Schriften installiert HACS nicht; sie kommen aus dem Archiv nach `www/azentrius/`.
- **Name:** README und Repo-Beschreibung nennen das Produkt „Azentrius Theme“ (Grundlagen, Marke). In HACS und in der Auswahl im Profil heißt das Theme weiter „Azentrius“.
- **Warum `azentrius.yaml` einzeln im Release liegt:** HACS lädt bei Themes alle Dateien eines Releases statt des Ordners `themes/`. Ein `zip_release` scheitert bei Themes, weil HACS die Adresse mit `tags/` bildet und GitHub mit 404 antwortet (HACS im Oktober 2026).
- **Änderungen** entstehen in diesem System, nie im öffentlichen Repo: Tokens oder Zuordnung ändern, das Theme neu erzeugen, als neue Version ins öffentliche Repo übertragen und dort den Release-Workflow starten.
- Name und Zeichen „Azentrius“ fallen nicht unter die Apache-Lizenz; das sagen `NOTICE` und README des Repos.

## Pflege

Home Assistant sagt als Schnittstelle für Themes nur `primary-color`, `accent-color` und die `state-…`-Farben zu. Alle anderen Namen können sich mit einem Frontend-Update ändern, und seit 2026.5 heißen zum Beispiel die Schalter-Variablen `ha-switch-…` statt `switch-…`. Nach jedem monatlichen Update deshalb in beiden Modi ansehen: Schalter, Buttons, die Seitenleiste samt Avatar und ein Eingabefeld in einem Dialog. Ändert sich ein Name, wird die Zuordnung im Skript angepasst und das Theme neu erzeugt, nie die YAML-Datei von Hand.

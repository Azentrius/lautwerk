# Icons

Icons aus dem Paket `@patternfly/react-icons` 6.4.0, demselben Satz, den Cockpit verwendet. Die Dateien sind einfarbig in `#151515` gezeichnet, nur zur Ansicht hier. In der Oberfläche wird der Pfad als Inline-SVG mit `class="az-icon"` eingesetzt; die Füllung ist dann `currentColor` und folgt der Textfarbe.

## Herkunft und Lizenz

Das Paket enthält Icons aus zwei Quellen mit verschiedenen Lizenzen. Geprüft Pfad für Pfad gegen Font Awesome Free 5.15.4 und `@patternfly/react-icons` 6.4.0:

| Icons | Herkunft | Lizenz | Nachweis |
|---|---|---|---|
| alle außer `pending` (34) | Font Awesome Free 5.15.4, Stil Solid, von Fonticons, Inc., unverändert von PatternFly übernommen | CC BY 4.0, Namensnennung Pflicht | `LICENSES/CC-BY-4.0-Font-Awesome.txt` |
| `pending` | PatternFly (Red Hat, Inc.) | MIT | `LICENSES/MIT-PatternFly-Icons.txt` |

- Die MIT-Lizenz des Pakets gilt für PatternFlys eigenen Code und eigene Icons, nicht für die Font-Awesome-Icons darin.
- Wer Icons übernimmt, nennt die Herkunft der übernommenen: im Web etwa als Kommentar in der Datei mit den Pfaden und in einer Lizenzdatei, unter Android im Kopfkommentar der Drawables und auf der Seite „Lizenzen“ der App.
- Ein neues Icon kommt aus demselben Paket. Steht es auch in Font Awesome Free 5, gilt CC BY 4.0, sonst MIT; die Tabelle oben wird ergänzt.

## Bedeutung

| Datei | Bedeutung |
|---|---|
| `check-circle.svg` | Erfolg, aktuell, läuft |
| `info-circle.svg` | Hinweis, Update verfügbar |
| `exclamation-triangle.svg` | Achtung |
| `exclamation-circle.svg` | Fehler |
| `ban.svg` | deaktiviert |
| `plus.svg` | hinzufügen |
| `sync-alt.svg` | „Aktualisieren“ im Seitenkopf, neu laden |
| `arrow-circle-up.svg` | „Update“: einen Container auf die neue Version bringen |
| `pencil-alt.svg` | bearbeiten |
| `trash.svg` | löschen |
| `ellipsis-v.svg` | weitere Aktionen |
| `times.svg` | schließen |
| `clock.svg` | Zeitplan, Cron |
| `cube.svg` | Container, Image |
| `external-link-alt.svg` | öffnet außerhalb dieser Seite: eine andere Website, ein anderes Fenster oder eine andere Cockpit-Seite |
| `search.svg` | suchen, filtern |
| `comments.svg` | Reiter „Gespräch“ |
| `cog.svg` | Reiter „Einstellungen“ |
| `history.svg` | Reiter „Verlauf“, leerer Verlauf |
| `microphone.svg` | Gespräch starten oder fortsetzen, Zustand „Hört zu“ |
| `microphone-slash.svg` | Zustand „Bereit“ (Mikrofon zu) |
| `pending.svg` | Zustand „Wird vorbereitet“ |
| `comment-dots.svg` | Zustand „Anbieter antwortet“ |
| `volume-up.svg` | Zustand „Spricht“ |
| `stop-circle.svg` | Zustand „Beendet“ |
| `stop.svg` | Gespräch beenden, Hörprobe stoppen |
| `play.svg` | Hörprobe abspielen (gewählte Stimme) |
| `check.svg` | ausgewählter Chip, aktuelle Option im Menü, „Erledigt“ als Icon-Button |
| `angle-right.svg` | öffnet einen Eintrag (Verlauf) |
| `angle-down.svg` | öffnet die Liste eines Auswahlfeldes |
| `arrow-left.svg` | zurück |
| `file-export.svg` | Gespräch exportieren |
| `copy.svg` | kopieren, z. B. eine Ausgabe |
| `redo.svg` | erneut ausführen |
| `exchange-alt.svg` | umwandeln: etwas in eine andere Form bringen und zurück |

Für Android liegen alle Icons zusätzlich als Vector-Drawable unter `android/res/drawable/ic_az_<name>.xml` (Bindestriche als Unterstriche).

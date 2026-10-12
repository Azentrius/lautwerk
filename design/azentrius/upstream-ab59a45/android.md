# Android

So werden die Themen „Azentrius dunkel“ und „Azentrius hell“ in nativen Android-Apps umgesetzt, zuerst in der Gesprächs-App. Diese Seite legt nur fest, wie die Werte aus `tokens.json` auf Android übertragen werden. Neue Farben gibt es hier nicht. Wo hier ein Wert steht, gilt er verbindlich; wo nichts steht, gilt der Rest dieses Systems.

Das Thema folgt der Systemeinstellung des Geräts. Dynamische Farben (Material You) bleiben aus.

**Das Design ändert kein Verhalten.** Es legt fest, wie vorhandene Funktionen aussehen, nicht, welche es gibt oder wann sie greifen. Widerspricht eine Regel hier einer bestehenden Funktion, gilt die Funktion; die Regel wird hier korrigiert, nicht die App.

## Einheiten

- **1 px dieses Systems = 1 dp.** Das gilt für Abstände, Radien, Rahmen, Icon- und Bauteilgrößen. `space-md` (16 px) ist auf Android 16 dp.
- **Schriftgrößen und Zeilenhöhen in sp**, nie in dp. So folgen sie der Schriftgröße, die im System eingestellt ist.
- Rahmen 1 dp. Fokusring 2 dp in `focus`, 2 dp Abstand zum Element.
- `radius-pill` ist eine volle Rundung (`CircleShape` bzw. `RoundedCornerShape(50)`), nicht 999 dp.
- Farben mit Deckkraft: `tokens.json` schreibt CSS-Reihenfolge `#RRGGBBAA`, Android erwartet `0xAARRGGBB`. Beispiel `backdrop` dunkel: `#0a0a0fcc` → `0xCC0A0A0F`.

## Schrift

Schriftdateien liegen unter `android/res/font/`. Auf Android gelten die gerundeten Zeilenhöhen dieser Tabelle.

| Stil | Größe | Zeilenhöhe | Datei | Wofür |
|---|---|---|---|---|
| `app-title` | 22 sp | 28 sp | `dm_sans_semibold` | Bildschirmtitel |
| `app-dialog-title` | 20 sp | 26 sp | `dm_sans_semibold` | Dialogtitel, Titel eines leeren Zustands |
| `app-section` | 17 sp | 22 sp | `dm_sans_semibold` | Abschnittstitel innerhalb eines Bildschirms („Stimme“, „Anbieter“) |
| `app-body` | 15 sp | 22 sp | `dm_sans_regular` | Nachrichten, Fließtext, Listentitel, Menüeinträge, Eingaben |
| `app-control` | 15 sp | 20 sp | `dm_sans_medium` | Beschriftung von Buttons, Chips und Reitern |
| `app-label` | 13 sp | 18 sp | `dm_sans_medium` | Status-Labels, Feldbeschriftungen, Hilfetexte, Metadaten, Zeitstempel |
| `brand-display` | 48 sp | 50 sp | `dm_serif_display_regular` | nur Splash und Über-Seite |

- Buchstabenabstand überall 0. Material 3 setzt eigene Werte (z. B. 0,5 sp); die werden überschrieben.
- In Compose `PlatformTextStyle(includeFontPadding = false)`, damit Zeilenhöhen exakt wirken.
- Kein Text in fester Höhe. Bauteile haben Mindesthöhen und wachsen mit. Bei 200 % Schriftgröße bricht Text um, er wird nicht abgeschnitten. Zwei Buttons nebeneinander stapeln sich dann untereinander, die Hauptaktion oben.
- Keine Großbuchstaben-Beschriftungen, keine Kursivschrift.
- Keine Silbentrennung (`Hyphens.None`). In Bedienelementen bricht ein Wort nie mitten im Wort um; reicht der Platz nicht, ändert sich die Anordnung des Bausteins (Reiter: siehe Stufen, Buttons: untereinander).

## Berührungsflächen

- **Jede bedienbare Fläche ist mindestens 48 × 48 dp groß.** Das ist die Android-Regel. Die 44 px aus dem Web-Teil gelten nur im Browser.
- Die sichtbare Form darf kleiner sein (Chip 32 dp hoch, Icon 24 dp). Die Berührungsfläche wird dann auf 48 dp erweitert. Material-Bausteine tun das selbst; eigene `clickable`-Flächen bekommen `Modifier.minimumInteractiveComponentSize()`.
- Berührungsflächen überlappen sich nicht. Zwischen zwei sichtbaren Bedienelementen stehen mindestens 8 dp (`space-sm`).

## Raster und Flächen

| | Telefon (unter 600 dp Breite) | ab 600 dp |
|---|---|---|
| Bildschirmrand | 16 dp (`space-md`) | 24 dp (`space-lg`) |
| Innenabstand Karte | 16 dp | 24 dp |
| Abstand zwischen Karten | 16 dp | 24 dp |
| Innenabstand Dialog | 24 dp | 24 dp |

- Drei Ebenen wie im Web: `bg-page` als Grund, `bg-card` mit 1 dp `line`-Rahmen für Karten und Listen, `bg-floating` für Menüs und Dialoge.
- Karten liegen flach, ohne Schatten.
- Schatten nur für Schwebendes: Menüs `shadow-md` = 6 dp Elevation, Dialoge `shadow-lg` = 12 dp Elevation.
- Tonale Elevation ist 0. Material 3 färbt erhöhte Flächen sonst mit der Akzentfarbe ein, im Dunkeln also golden.
- Edge-to-edge: Status- und Navigationsleiste liegen über `bg-page`; ihre Icons hell im dunklen Thema, dunkel im hellen. Inhalte und die untere Leiste halten Abstand über die System-Insets.

### Zuordnung zu Material 3

Falls Material-3-Bausteine verwendet werden, bekommt das `ColorScheme` diese Werte, getrennt für hell und dunkel:

| Material 3 | Token |
|---|---|
| `primary`, `onPrimary` | `primary`, `on-primary` |
| `background`, `surface` | `bg-page` |
| `onBackground`, `onSurface` | `text` |
| `onSurfaceVariant` | `text-subtle` |
| `surfaceContainerLow`, `surfaceContainer` | `bg-card` |
| `surfaceContainerHigh`, `surfaceContainerHighest` | `bg-floating` |
| `surfaceTint` | `bg-floating` (wirkt nicht, weil tonale Elevation 0 ist) |
| `outline` | `line-control` |
| `outlineVariant` | `line` |
| `error`, `onError` | `status-danger`, `on-danger` |
| `scrim` | `backdrop` |

## Zustände

Für alle Bausteine gelten dieselben fünf Zustände:

- **Gedrückt** ersetzt das Hover aus dem Web: Die Fläche wechselt sofort auf den `…-hover`-Wert bzw. auf `bg-hover`. Kein Ripple.
- **Ausgewählt** heißt `primary`: Fläche `primary` mit `on-primary` darauf, oder Text in `primary-text`.
- **Fokussiert** (Tastatur, D-Pad): 2 dp Ring in `focus`, 2 dp Abstand. Bei Touch-Bedienung erscheint kein Ring.
- **Deaktiviert**: Fläche `tint-neutral`, kein Rahmen, Schrift und Icon in `text-disabled`. Text- und Icon-Buttons behalten ihre durchsichtige Fläche.
- Übergänge höchstens 150 ms. Ist „Animationen entfernen“ eingeschaltet, gibt es keine.

## Bausteine

### Buttons

Alle Buttons sind Pillen, sichtbar 48 dp hoch, 24 dp Innenabstand links und rechts, Beschriftung `app-control`. Icon im Button 20 dp, 8 dp vor dem Text. Eine kleine Variante gibt es auf Android nicht.

| | Normal | Gedrückt | Deaktiviert |
|---|---|---|---|
| **Primär** | Fläche `primary`, Text `on-primary` | Fläche `primary-hover` | Fläche `tint-neutral`, Text `text-disabled` |
| **Sekundär** | durchsichtig, Rahmen 1 dp `primary`, Text `primary-text` | Fläche `bg-hover`, Rahmen 2 dp `primary-hover` | Fläche `tint-neutral`, kein Rahmen, Text `text-disabled` |
| **Löschen** | Fläche `status-danger`, Text `on-danger` | darüber 8 % Schwarz (`#14000000`) | Fläche `tint-neutral`, Text `text-disabled` |
| **Text** (Abbrechen) | durchsichtig, Text `link`, Innenabstand 8 dp, mindestens 48 dp breit | Text `link-hover` | Text `text-disabled` |
| **Icon** | Kreis 48 dp, durchsichtig, Icon 24 dp in `text-subtle` | Kreis `bg-hover`, Icon `text` | Icon `text-disabled` |

- Fokussiert: alle Buttons mit dem Ring aus „Zustände“. Einen Zustand „ausgewählt“ haben Buttons nicht.
- Pro Bildschirm bzw. Leiste höchstens ein Primär-Button.
- Der Löschen-Button erscheint nur als Bestätigung im Löschdialog.
- Icon-Buttons haben immer eine `contentDescription` als Verb plus Objekt: „Gespräch exportieren“, „Eintrag löschen“.

### Eingabefelder

- Beschriftung über dem Feld in `app-label`, Farbe `text`, 4 dp Abstand zum Feld. Keine schwebende Beschriftung im Feld, kein Platzhalter als Ersatz für die Beschriftung.
- Feld mindestens 48 dp hoch, 16 dp Innenabstand links und rechts, Radius `radius-small` (6 dp), Fläche `bg-control`, Rahmen 1 dp `line-control`, Text `app-body` in `text`, Platzhalter `text-subtle`.
- Hilfetext darunter in `app-label`, `text-subtle`, 4 dp Abstand.

| | Rahmen | Fläche | Text |
|---|---|---|---|
| Normal | 1 dp `line-control` | `bg-control` | `text` |
| Fokussiert (Eingabe aktiv) | 2 dp `focus` | `bg-control` | `text` |
| Fehler | 2 dp `status-danger` | `bg-control` | `text` |
| Deaktiviert | 1 dp `line` | `tint-neutral` | `text-disabled` |

Gedrückt und ausgewählt gibt es beim Feld nicht: Ein Tipp fokussiert es.

Fehlermeldung unter dem Feld statt des Hilfetexts: Icon `exclamation-circle` 16 dp und Text `app-label` in `danger-text`, als Anweisung formuliert. Erst nach Verlassen des Feldes oder beim Speichern.

### Auswahlchips

Für kurze Auswahlen, die sofort wirken (Filter, wenige Optionen mit kurzen Namen).

- Sichtbar 32 dp hoch, Berührungsfläche 48 dp, Pille, 16 dp Innenabstand, Beschriftung `app-control`.
- Ausgewählt mit Icon `check` (18 dp) vor dem Text, 8 dp Abstand.
- Chips stehen mit 8 dp Abstand nebeneinander und brechen in die nächste Zeile um.

| | Fläche | Rahmen | Text, Icon |
|---|---|---|---|
| Normal | durchsichtig | 1 dp `line-control` | `text` |
| Gedrückt | `bg-hover` | 1 dp `line-control` | `text` |
| Ausgewählt | `primary` | keiner | `on-primary` |
| Ausgewählt, gedrückt | `primary-hover` | keiner | `on-primary` |
| Deaktiviert | `tint-neutral` | keiner | `text-disabled` |

### Reiter

- Reiterleiste auf `bg-page`, darunter 1 dp `line` über die volle Breite.
- **Eine Beschriftung bricht nie innerhalb eines Wortes um, wird nie getrennt und nie mit Auslassungspunkten gekürzt.** Sie steht immer in einer Zeile. Reicht der Platz nicht, wechselt die Anordnung nach den drei Stufen unten.
- Die Stufe richtet sich nach dem tatsächlichen Platz bei der aktuellen Schriftgröße, nicht nach dem Gerät. Maßgeblich ist die längste Beschriftung. Alle Reiter einer Leiste stehen immer in derselben Stufe.

| Stufe | Wann | Anordnung |
|---|---|---|
| 1 nebeneinander | die längste Beschriftung passt in gleicher Breite neben ihr Icon | Icon 24 dp und Beschriftung `app-control` nebeneinander, 8 dp Abstand, 16 dp Innenabstand links und rechts, mindestens 48 dp hoch. Gleiche Breite für alle Reiter. |
| 2 untereinander | Stufe 1 passt nicht | Icon 24 dp oben, darunter mit 4 dp Abstand die Beschriftung in `app-label` (13 sp). 8 dp Innenabstand links und rechts, 8 dp oben und unten, mindestens 64 dp hoch. Gleiche Breite für alle Reiter. |
| 3 verschiebbar | auch Stufe 2 passt nicht, etwa bei sehr großer Schrift | Anordnung wie Stufe 2, aber jeder Reiter so breit wie sein Inhalt plus 16 dp links und rechts. Die Leiste lässt sich waagerecht verschieben, 16 dp Rand zum Bildschirm. Der gewählte Reiter wird immer ganz sichtbar gerückt. |

- Auf Telefonen ist Stufe 2 der Normalfall: Bei drei Reitern bleiben dort nebeneinander nur 56 bis 85 dp für den Text, „Einstellungen“ braucht in `app-control` 94 dp. Untereinander passt es bis etwa 130 % Schriftgröße (360 dp breit) bzw. 150 % (412 dp breit); darüber gilt Stufe 3.
- In Compose wird die Breite der längsten Beschriftung mit `rememberTextMeasurer()` gemessen; die Beschriftung selbst hat `maxLines = 1` und `softWrap = false`.
- **Normal:** Icon und Text in `text-subtle`.
- **Gedrückt:** Fläche `bg-hover`.
- **Ausgewählt:** Icon und Text in `primary-text`, darunter eine 3 dp hohe Linie in `primary-text` mit runden Enden, auf der Trennlinie. In Stufe 1 ist sie so breit wie Icon und Text zusammen, in Stufe 2 und 3 so breit wie die Beschriftung.
- **Deaktiviert:** `text-disabled`.
- Wechsel ohne Gleitanimation.

### Menüs

- Fläche `bg-floating`, Radius `radius-medium` (16 dp), Rahmen 1 dp `line`, Schatten `shadow-md` (6 dp). Oben und unten 8 dp Innenabstand.
- Breite nach dem längsten Eintrag, höchstens 280 dp.
- Einträge mindestens 48 dp hoch, 16 dp Innenabstand links und rechts, Text `app-body` in `text`. Optionales Icon 24 dp in `text-subtle` davor, 16 dp Abstand.
- **Gedrückt:** Fläche `bg-hover`. **Ausgewählt** (aktuelle Option): Icon `check` 24 dp in `primary-text` am rechten Rand, Text bleibt `text`. **Fokussiert:** Fläche `bg-hover`. **Deaktiviert:** Text und Icon `text-disabled`.
- Ein Löschen-Eintrag hat Text und Icon in `danger-text` und steht als letzter Eintrag.

### Dialoge

- Fläche `bg-floating`, Radius `radius-large` (24 dp), Schatten `shadow-lg` (12 dp), dahinter `backdrop`.
- Breite: Bildschirm minus 2 × 16 dp, höchstens 560 dp. Innenabstand 24 dp.
- Titel `app-dialog-title` in `text`, als Verb plus Objekt („Gespräch löschen?“). 16 dp darunter der Inhalt in `app-body`, 24 dp darunter die Buttons.
- Buttons: Hauptaktion zuerst und links, „Abbrechen“ als Text-Button danach, 16 dp Abstand. Passen sie nicht nebeneinander, stehen sie untereinander in voller Breite, die Hauptaktion oben. Das weicht bewusst von Material 3 ab und entspricht dem Rest dieses Systems.
- Im Löschdialog ist die Hauptaktion der Löschen-Button.
- Zurück-Geste und Zurück-Taste schließen den Dialog. Ein Tipp auf den Hintergrund schließt nicht, solange Eingaben ungespeichert sind.

### Status-Labels

- Pille, 4 dp oben und unten, 8 dp links und rechts, Text `app-label`, Icon 16 dp, 4 dp Abstand.
- Farben wie im Web: getönt `tint-…` mit `on-tint-…`, kräftig `status-…` mit `on-…`.
- Nicht bedienbar, daher ohne 48-dp-Fläche.

### Listen

- Liste in einer Karte (`bg-card`, `line`, `radius-medium`). Einträge 16 dp Innenabstand links und rechts.
- Einzeilig mindestens 56 dp hoch, zweizeilig mindestens 72 dp.
- Zwischen Einträgen 1 dp `line`, links um 16 dp eingerückt. Der ganze Eintrag ist die Berührungsfläche. Gedrückt: Fläche `bg-hover`.

### Schalter, Optionsfelder, Schieberegler

- **Schalter:** Bahn 48 × 28 dp, Pille; Knopf 20 dp. An: Bahn `primary`, Knopf `on-primary`. Aus: Bahn `bg-control` mit 1 dp `line-control`, Knopf `line-control`. Deaktiviert: alles mit 45 % Deckkraft. Die ganze Zeile mit Beschriftung schaltet.
- **Optionsfeld:** Kreis 20 dp. Aus: 2 dp Ring `line-control`. An: 2 dp Ring `primary`, innen Punkt 10 dp `primary`. Die ganze Zeile wählt aus.
- **Schieberegler:** Spur 4 dp hoch, Pille; links vom Knopf `primary`, rechts davon `line-control`. Knopf 20 dp, `primary`. Berührungsfläche 48 dp hoch über die ganze Breite. Gedrückt: Knopf 24 dp. Feste Stufen erscheinen als 4-dp-Punkte auf der Spur. Deaktiviert: alles mit 45 % Deckkraft.

## Die Gesprächs-App

Anordnung und Funktionen bleiben, wie sie sind. Hier steht, welcher Baustein und welcher Stil für jeden vorhandenen Bereich gilt.

### Gespräch, Einstellungen, Verlauf

- Die drei Bereiche sind **Reiter** (siehe oben), in der heutigen Reihenfolge, mit den Icons `comments`, `cog` und `history`.
- Wo heute ein Bildschirmtitel steht, ist er `app-title`.
- Abschnittstitel innerhalb eines Bereichs, etwa die Gruppen in den Einstellungen, sind `app-section` in `text`: 24 dp Abstand nach oben, 8 dp nach unten.
- Dialogtitel sind `app-dialog-title`.
- **„Schlüssel speichern“** ist die einzige manuelle Speicheraktion. Sie bleibt auf der Seite, auf der der Schlüssel heute eingegeben wird, als Primär-Button ohne Icon direkt unter dem Eingabefeld. In der unteren Leiste erscheint sie nicht.

### Untere Status- und Aktionsleiste

- Fläche `bg-card`, oben 1 dp `line`. Innenabstand 16 dp, darunter der Abstand der System-Navigationsleiste.
- Die Leiste hat zwei Teile: **Status** und **Aktionen**. Passen beide in eine Zeile, steht der Status links und die Aktionen rechts. Sonst steht der Status oben und die Aktionen darunter, 8 dp Abstand.
- **Status:** zuerst immer der Gesprächszustand. Läuft gerade ein Export, oder ist einer gerade abgeschlossen, folgt dessen Label mit 8 dp Abstand (siehe Export). Labels brechen in die nächste Zeile um, sie werden nicht gekürzt.
- **Aktionen,** von links nach rechts:
  - „Exportieren“ als Icon-Button mit `file-export`. Ist Export gerade nicht möglich, bleibt der Button sichtbar und ist deaktiviert.
  - Ganz rechts die Hauptaktion als einziger Primär-Button (siehe die folgende Tabelle).
- **Hauptaktion:** Welche es ist, hängt davon ab, ob ein Gespräch aktiv ist und ob ein vorhandener Chat ausgewählt ist, nicht vom angezeigten Zustandswort.

| Lage | Hauptaktion |
|---|---|
| kein Gespräch aktiv, kein vorhandener Chat ausgewählt | „Gespräch starten“, Icon `microphone` |
| kein Gespräch aktiv, vorhandener Chat ausgewählt | „Gespräch fortsetzen“, Icon `microphone` |
| anfängliche Startvorbereitung | „Start abbrechen“ als Text-Button an derselben Stelle |
| Gespräch aktiv, in jedem Zustand, auch „Wird vorbereitet“ (z. B. beim Anbieterwechsel) | „Gespräch beenden“, Icon `stop` |

- **Abbrechen** ist immer ein Text-Button und steht an der Stelle der Aktion, die er abbricht. Abbrechen erscheint nur dort, wo die App heute abbrechen kann:
  - „Start abbrechen“ ersetzt nur während der anfänglichen Startvorbereitung den Primär-Button. Zeigt ein bereits aktives Gespräch „Wird vorbereitet“, bleibt „Gespräch beenden“.
  - „Export abbrechen“ ersetzt während der Exportvorbereitung den Exportieren-Button (siehe Export).
- **Kein Speichern-Button.** Gespräche werden automatisch gespeichert; die Leiste zeigt dafür weder einen Button noch ein eigenes Label.

### Export

Export ist ein Vorgang, kein Gesprächszustand. Er hat ein eigenes Label, das neben dem Gesprächszustand steht und ihn nie ersetzt. Die Phasen folgen dem, was die App heute tut:

| Phase | Label | Icon | In der Leiste statt „Exportieren“ |
|---|---|---|---|
| Vorbereitung | „Export wird vorbereitet“, getönt neutral | `pending` | Text-Button „Export abbrechen“ |
| Android-Dateidialog offen | unverändert „Export wird vorbereitet“ | `pending` | nichts; abgebrochen wird im Dateidialog selbst |
| Datei wird geschrieben | „Wird exportiert“, getönt neutral | `pending` | Exportieren-Button deaktiviert, **kein** Abbrechen |
| Fertig | „Exportiert“, getönt Erfolg (`tint-success`, `on-tint-success`) | `check-circle` | Exportieren-Button wieder aktiv |
| Fehler | „Export fehlgeschlagen“, getönt Fehler (`tint-danger`, `on-tint-danger`) | `exclamation-circle` | Exportieren-Button wieder aktiv |
| Abgebrochen (Vorbereitung oder Dateidialog) | „Export abgebrochen“, getönt neutral | `ban` | Exportieren-Button wieder aktiv |

- Während die Datei geschrieben wird, gibt es keinen Abbruch, weil die App ihn nicht anbietet. Eine Abbruchfunktion für diese Phase ist nicht vorgesehen.
- „Exportiert“, „Export fehlgeschlagen“ und „Export abgebrochen“ bleiben stehen, bis der nächste Export beginnt oder ein neues Gespräch startet. Sie verschwinden nicht nach einer Zeit.
- Es gibt höchstens ein Export-Label.

### Nachrichten

- **Nutzer:** rechtsbündig in einer Blase. Fläche `tint-neutral`, Text `app-body` in `on-tint-neutral`. Radius 16 dp, unten rechts 6 dp. Innenabstand 8 dp oben und unten, 16 dp links und rechts. Höchstens 80 % der verfügbaren Breite.
- **Assistent:** linksbündig, ohne Blase, volle Breite, Text `app-body` in `text`.
- Abstand zwischen zwei Nachrichten derselben Seite 8 dp, beim Wechsel zwischen Nutzer und Assistent 24 dp.
- Zeitstempel, falls angezeigt, darunter in `app-label`, `text-subtle`, 4 dp Abstand, auf derselben Seite wie die Nachricht.
- Zeigt die App später eine vorläufige Erkennung während „Hört zu“, steht sie in einer Blase ohne Fläche: 1 dp Rahmen `line-control`, Text `text-subtle` auf `bg-page`. Steht der Text fest, wird daraus die normale Nutzerblase. Vorläufig ist also Umriss, fertig ist Fläche. Bisher zeigt die App das nicht.
- Für Screenreader ist jede Nachricht ein Element und beginnt mit „Du:“ bzw. „Assistent:“.

### Verlaufseinträge

- Einträge als zweizeilige Liste (siehe Listen). Zeile 1: Titel des Gesprächs in `app-body`, `text`, einzeilig mit Auslassungspunkten.
- Zeile 2 in `app-label`, `text-subtle`, einzeilig: der Zeitpunkt der letzten Änderung, dann Anbieter, dann Modell, getrennt durch „ · “. Beispiel: „Heute, 08:14 · Anbieter · Modell“. Wird es zu lang, kürzen Auslassungspunkte am Ende; der Zeitpunkt bleibt so immer sichtbar. Vollständig stehen Anbieter und Modell in der Ansicht des Gesprächs.
- Zeitpunkt: „Heute, 08:14“ und „Gestern, 19:02“; davor im laufenden Jahr „Mo., 5. Okt.“, in früheren Jahren „5. Okt. 2025“. Eine Dauer wird nicht angezeigt, weil die App sie nicht speichert.
- Bei einer Suche zeigt ein Eintrag, wie heute, eine dritte Zeile „Treffer in Nachricht …“, aber nur, wenn der Treffer tatsächlich in einer Nachricht liegt. Der Text dieser Zeile bleibt genau wie heute; einen Textausschnitt oder eine Hervorhebung des Suchworts gibt es nicht. Die Zeile steht in `app-label`, `text-subtle`; der Eintrag ist dann mindestens 88 dp hoch.
- Treffer nur in Titel, Anbieter oder Modell haben keine dritte Zeile und keine eigene Markierung.
- Am rechten Rand Icon `angle-right`, 24 dp, `text-subtle`, senkrecht mittig.
- Werden Einträge nach Tagen gruppiert, steht über jeder Gruppe der Tag in `app-label`, `text-subtle`: 24 dp Abstand nach oben, 8 dp nach unten.
- Ist der Verlauf leer: Icon `history` 48 dp in `text-subtle`, Titel `app-dialog-title` („Noch keine Gespräche“), ein Satz in `app-body`, `text-subtle`.
- Löschen eines Eintrags, falls vorhanden, immer mit Löschdialog.

### Stimme und Tempo

- „Stimme und Tempo“ bleibt eine **eigene Seite**. Sie wird wie heute aus dem Reiter „Gespräch“ geöffnet; der Zugang bleibt an seiner Stelle und in seiner Form und bekommt den passenden Baustein dieses Abschnitts. In den Einstellungen gibt es keinen zusätzlichen Zugang.
- Auf der Seite: Titel `app-title` „Stimme und Tempo“; ein vorhandener Zurück-Button ist ein Icon-Button mit `arrow-left`. Darunter die Abschnitte „Stimme“ und „Tempo“, jeweils mit Titel `app-section`.
- **Stimme:** Jede Stimme ist ein Listeneintrag mit **Optionsfeld** links, 16 dp Abstand zum Text. Name in `app-body`, `text`; Zeile 2 in `app-label`, `text-subtle` mit Beschreibung, falls vorhanden.
- Stimmen, die die App als Online-Stimmen führt, tragen in Zeile 2 vorn das Wort „Online“, z. B. „Online · Anbieter“. So sieht man vor dem Antippen, dass Text das Gerät verlässt. Das ist reine Anzeige; einen gespeicherten Zustimmungsstatus gibt es nicht. „Android-Standard“ trägt das Wort nicht, weil die App nicht prüft, ob die vom System gewählte Stimme online arbeitet.
- Die Auswahl gilt sofort, ohne Speichern-Button. Die gewählte Zeile bekommt keine eigene Fläche; das Optionsfeld zeigt die Wahl.
- **Tempo** behält sein heutiges Bedienelement. Ist es ein Schieberegler, gilt der Schieberegler unter „Schalter, Optionsfelder, Schieberegler“. Der aktuelle Wert steht als Text daneben, in `app-body`, z. B. „1,0 ×“.

#### Bestätigung bei Online-Stimmen

Das Verhalten bleibt, wie es ist: Bei **jeder** ausdrücklichen Auswahl einer Online-Stimme erscheint die Bestätigung. Gespeichert wird nur die gewählte Stimme.

- Die Bestätigung ist ein Dialog (siehe Dialoge) mit den heutigen Texten. Hauptaktion ist der Primär-Button „Online-Stimme wählen“, danach „Abbrechen“ als Text-Button.
- Bis zur Bestätigung zeigt das Optionsfeld weiter die bisherige Stimme. Erst „Online-Stimme wählen“ setzt die neue Auswahl.
- „Abbrechen“, Zurück-Geste oder Zurück-Taste lassen die bisherige Stimme gewählt.

#### Hörprobe

Das Verhalten bleibt, wie es ist: Die Hörprobe spielt **nur die gewählte Stimme** ab. Deshalb gibt es keine Probe-Buttons in den einzelnen Stimmeneinträgen und keine eigene Bestätigung für eine Probe.

- Zur Bestätigung gilt genau, was die App heute tut: Eine ausdrücklich ausgewählte Online-Stimme wurde bei ihrer Auswahl bestätigt. Für „Android-Standard“ fragt die App nicht, auch nicht vor einer Probe, weil sie nicht prüft, ob das System eine Online-Stimme verwendet. Das Design ergänzt dafür nichts.
- Es bleiben die zwei getrennten Buttons an ihrer heutigen Stelle: „Hörprobe abspielen“ mit Icon `play` und „Hörprobe stoppen“ mit Icon `stop`. Beide sind Sekundär-Buttons, nebeneinander mit 8 dp Abstand; passen sie nicht, untereinander.
- Beide bleiben immer sichtbar. Welcher aktiv ist, entscheidet die App wie heute; der jeweils andere ist deaktiviert (siehe Zustände).
- Eine Probe ändert die Auswahl nie.

## Gesprächszustände

Jeder Zustand ist ein Status-Label mit festem Icon und festem Wort. Er steht genau einmal sichtbar, in der unteren Leiste.

| Zustand | Label | Icon |
|---|---|---|
| Bereit | getönt neutral (`tint-neutral`, `on-tint-neutral`) | `microphone-slash` |
| Wird vorbereitet | getönt neutral | `pending` |
| Hört zu | **kräftig** Info (`status-info`, `on-info`) | `microphone` |
| Anbieter antwortet | getönt Info (`tint-info`, `on-tint-info`) | `comment-dots` |
| Spricht | getönt Info | `volume-up` |
| Beendet | getönt neutral | `stop-circle` |
| Fehler (jeder) | getönt Fehler (`tint-danger`, `on-tint-danger`) | `exclamation-circle` |

- Nur „Hört zu“ ist kräftig. Ist das Mikrofon offen, muss man es aus dem Augenwinkel sehen. Das Iconpaar `microphone-slash` / `microphone` zeigt jederzeit, ob die App zuhört.
- Info-Farbe heißt hier „gerade passiert etwas“, neutral heißt „wartet“. Grün kommt nicht vor, denn ein laufendes Gespräch ist kein Erfolg. Gold ist nie ein Status.
- Bei einem Fehler nennt das Wort, was passiert ist („Verbindung unterbrochen“, „Mikrofon nicht erlaubt“), nicht „Fehler“.
- Zustandswechsel ohne Animation, kein Pulsieren, kein Dauerspinner. Für Screenreader ist das Label eine höfliche Live-Region (`LiveRegionMode.Polite`).
- Android Auto (DHU) behält seine Vorlagen. Zeigt die Auto-Oberfläche einen Zustand, verwendet sie dieselben Wörter und Icons; Farben und Formen bestimmt dort die Vorlage.

## Dateien

Im Ordner `android/` dieses Systems:

| Pfad | Inhalt |
|---|---|
| `res/font/dm_sans_regular.ttf`, `dm_sans_medium.ttf`, `dm_sans_semibold.ttf` | DM Sans 400, 500, 600 als statische TTF, aus derselben Datei wie im Web erzeugt |
| `res/font/dm_serif_display_regular.ttf` | DM Serif Display für `brand-display` |
| `res/font/dm_sans.xml` | Schriftfamilie für XML-Layouts |
| `res/drawable/ic_az_*.xml` | alle Icons der Gruppe „Icons“ als Vector-Drawable, 24 dp, 2 dp Rand wie Material-Icons, Füllung schwarz zum Einfärben |
| `LICENSES/OFL-DM-Sans.txt`, `LICENSES/OFL-DM-Serif-Display.txt` | SIL Open Font License 1.1 |
| `LICENSES/MIT-PatternFly-Icons.txt` | MIT-Lizenz der Icons (Red Hat) |

- Die Lizenzdateien gehören in die App, etwa auf eine Seite „Lizenzen“ in den Einstellungen.
- Die Schriften enthalten den lateinischen Grundumfang mit allen deutschen Zeichen. Fehlende Zeichen, etwa Emojis, übernimmt die Systemschrift.
- Icons werden über ihre Füllung in der Farbe des Zusammenhangs eingefärbt (`Icon(tint = …)`), nie in einer festen Farbe.

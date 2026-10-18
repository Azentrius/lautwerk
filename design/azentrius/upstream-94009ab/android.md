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
| `brand-display` | 48 sp | 50 sp | `dm_serif_display_regular` | nur Über-Seite; das Startbild zeigt keinen Text |
| Namenszusatz | wie der Name davor | wie der Name davor | `dm_serif_display_italic` | der kursive Zusatz in einem App-Namen, falls er einen hat; heute hat keine Android-App einen (Grundlagen, Marke) |

- Buchstabenabstand überall 0. Material 3 setzt eigene Werte (z. B. 0,5 sp); die werden überschrieben.
- In Compose `PlatformTextStyle(includeFontPadding = false)`, damit Zeilenhöhen exakt wirken.
- Kein Text in fester Höhe. Bauteile haben Mindesthöhen und wachsen mit. Bei 200 % Schriftgröße bricht Text um, er wird nicht abgeschnitten. Zwei Buttons nebeneinander stapeln sich dann untereinander, die Hauptaktion oben.
- Keine Großbuchstaben-Beschriftungen.
- **Keine Kursivschrift, mit genau einer Ausnahme:** dem Namenszusatz im Kopf einer App, falls ihr Name einen hat, in DM Serif Display kursiv (Zeile „Namenszusatz“ oben). Heute hat keine Android-App einen. Sonst ist nichts kursiv: keine Hervorhebung, kein Zitat, kein Hinweis, kein Bedienelement, kein Status. Die Ausnahme gilt nur für den Zusatz selbst; der Name davor bleibt aufrecht. Kursiv wird nie durch schräggestellte DM Sans erzeugt, sondern nur mit der Datei `dm_serif_display_italic`.
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

**Fehler am Feld** gibt es nur, wenn eine App einen Fehler selbst einem bestimmten Feld zuordnet. Dann gilt die Zeile „Fehler“ der Tabelle, und unter dem Feld steht statt des Hilfetexts Icon `exclamation-circle` 16 dp und der Fehlertext in `app-label`, `danger-text`, als Anweisung formuliert. Ein Screenreader liest den Fehler zusammen mit dem Feld (in Compose `isError` und `semantics { error(…) }`). Wann geprüft wird, legt die App fest; für Lautwerk steht es unter „Fehler am Schlüsselfeld“.

Ordnet eine App einen Fehler keinem Feld zu, bleibt jedes Feld in seinem normalen Zustand. Der Fehler erscheint dann als Meldung auf der Seite (siehe Meldungen).

### Auswahlfelder

Ein Feld, das eine Liste aufklappt, aus der genau eine Option gewählt wird. In Views ist das der native `Spinner` im Modus `dropdown`, in Compose ein `ExposedDropdownMenuBox`. Das Design legt fest, wie Feld und Liste aussehen. Wie die Auswahl abläuft und wann sie gespeichert wird, bleibt Sache der App: Ein Tipp öffnet die Liste, ein Tipp auf eine Option wählt sie und schließt die Liste, Zurück oder ein Tipp daneben schließt ohne Änderung.

**Geschlossenes Feld**

- Beschriftung über dem Feld in `app-label`, Farbe `text`, 4 dp Abstand, wie bei Eingabefeldern. Sie ist mit dem Feld verknüpft (`labelFor` bzw. Semantik), damit ein Screenreader Beschriftung und Wert zusammen liest.
- Feld mindestens 48 dp hoch, Radius `radius-small` (6 dp), Fläche `bg-control`, Rahmen 1 dp `line-control`. Innenabstand links 16 dp, oben und unten 12 dp, rechts 12 dp.
- Links der gewählte Wert in `app-body`, `text`. Rechts Icon `angle-down` 24 dp in `text-subtle`, 8 dp Abstand zum Wert, senkrecht mittig.
- Der Wert bricht bei Bedarf um und wird nie mit Auslassungspunkten gekürzt; das Feld wächst mit. Das Icon dreht sich beim Öffnen nicht.

| | Rahmen | Fläche | Wert | Icon |
|---|---|---|---|---|
| Normal | 1 dp `line-control` | `bg-control` | `text` | `text-subtle` |
| Gedrückt | 1 dp `line-control` | `bg-hover` | `text` | `text` |
| Geöffnet (Liste sichtbar) | 2 dp `focus` | `bg-control` | `text` | `text` |
| Fokussiert (Tastatur, D-Pad) | 2 dp `focus` | `bg-control` | `text` | `text` |
| Deaktiviert | 1 dp `line` | `tint-neutral` | `text-disabled` | `text-disabled` |

Einen Fehlerzustand hat das Auswahlfeld nur, wenn die App einer Auswahl selbst einen Fehler zuordnet; dann gilt dasselbe wie bei Eingabefeldern.

**Aufgeklappte Liste**

- Wie ein Menü: Fläche `bg-floating`, Radius `radius-medium` (16 dp), Rahmen 1 dp `line`, Schatten `shadow-md` (6 dp), oben und unten 8 dp Innenabstand.
- So breit wie das Feld, nicht nach dem längsten Eintrag. Sie beginnt 4 dp unter dem Feld. Reicht der Platz darunter nicht, setzt das System sie darüber oder begrenzt ihre Höhe; dann lässt sie sich verschieben. Die gewählte Option ist beim Öffnen sichtbar.
- Einträge mindestens 48 dp hoch, Innenabstand 16 dp links und rechts, 12 dp oben und unten. Text `app-body` in `text`, bricht um statt zu kürzen; der Eintrag wächst mit.
- **Gewählte Option:** Icon `check` 24 dp in `primary-text` am rechten Rand, 16 dp Abstand zum Text. Der Text bleibt `text` und bekommt keine eigene Fläche.
- **Gedrückt** und **fokussiert:** Fläche `bg-hover`. **Deaktiviert:** Text `text-disabled`.
- Keine eigene Animation beim Öffnen und Schließen. Ist „Animationen entfernen“ eingeschaltet, erscheint die Liste ohne Übergang.

**Große Systemschrift:** Bei 200 % wachsen Feld und Einträge in der Höhe; Beschriftung, Wert und Optionen brechen um und bleiben vollständig. Keine feste Höhe, kein `singleLine`, kein `ellipsize`.

**Umsetzung mit `Spinner`:** Die Standardlayouts `simple_spinner_item` und `simple_spinner_dropdown_item` sind einzeilig und kürzen. Für Feld und Einträge kommen deshalb eigene Layouts mit den Werten oben zum Einsatz, für das Feld ein eigener Hintergrund und für die Liste `android:popupBackground`. Adapter, Auswahl-Callbacks und Speichern bleiben unverändert.

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

### Meldungen

Eine Meldung gibt die Rückmeldung der App zu einem Vorgang auf der Seite wieder: ein Fehler beim Speichern, Lesen oder Exportieren, eine Bestätigung, ein Hinweis. Sie entspricht dem Baustein `Alert` im Web. Das Design legt nur fest, wie eine vorhandene Meldung aussieht. Welche Meldungen es gibt, ihr Text, wann sie erscheint und wann sie verschwindet, bleibt, wie die App es heute macht.

- Karte: Fläche `bg-card`, Rahmen 2 dp in der Farbe der Art, Radius `radius-medium` (16 dp), Innenabstand 16 dp, volle Breite des Inhalts.
- Links Icon 20 dp in der Farbe der Art, oben bündig mit der ersten Textzeile, 12 dp Abstand zum Text. Daneben der Text der App in `app-body`, `text`, ohne zusätzlichen Titel. Er bricht um und wird nie gekürzt.
- Bietet die App zu einer Meldung schon heute eine Aktion an (etwa „Neu laden“ oder „Erneut versuchen“), steht sie 8 dp unter dem Text als Sekundär-Button mit Icon `sync-alt`, in der Spalte des Textes. Neue Aktionen kommen nicht hinzu.
- Meldungen stehen an der Stelle, an der die App sie heute zeigt. Gilt eine Meldung für die ganze Seite, steht sie direkt unter der Reiterleiste und bleibt in jedem Reiter sichtbar, wie bei den Addons. Gehört sie zu einem Bereich, etwa zur Suche oder zur Hörprobe, steht sie dort. Mehrere Meldungen stehen untereinander, 8 dp Abstand.
- Für Screenreader ist eine Meldung eine höfliche Live-Region (`LiveRegionMode.Polite`).

| Art | Farbe von Rahmen und Icon | Icon | Wofür |
|---|---|---|---|
| Fehler | `status-danger` | `exclamation-circle` | Ein Vorgang ist gescheitert: Speichern, Entfernen, Löschen, Lesen, Suchen, Export, Öffnen, Gespräch. |
| Erfolg | `status-success` | `check-circle` | Ein ausdrücklich ausgelöster Vorgang ist fertig, etwa „Schlüssel … gespeichert“ oder „Gespräch gelöscht“. |
| Hinweis | `status-info` | `info-circle` | Etwas ist zu beachten, ohne dass etwas gescheitert ist, etwa ein Modell, das nicht mehr im Katalog steht, oder eine gekürzte Antwort. |
| ohne Wertung | `line-control` | `ban` | Ein Vorgang wurde abgebrochen, etwa „Start abgebrochen“. |

- Die Art richtet sich nach dem Text der App, nicht nach dem Weg, auf dem er auf die Seite kommt: Derselbe Text hat überall dieselbe Art. Weiß die App bei einem Text nicht, ob es ein Fehler ist, gilt „Hinweis“.
- **Laufende Vorgänge** wie „Schlüssel wird gespeichert …“ oder „Verlauf wird geladen“ sind keine Meldung: eine Textzeile in `app-body`, `text-subtle`, mit Icon `pending` 20 dp davor, ohne Rahmen, ohne Spinner.
- Fehler an einem Eingabefeld sind keine Meldung; dafür gilt „Fehler am Feld“ unter Eingabefelder.

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

### Kopf der App

Wo die App ihren Namen als Titel zeigt, steht er wie im Seitenkopf der Addons, mit dem Punkt der Wortmarke:

- „Lautwerk“ in `app-title` (DM Sans, 22 sp, 600) in `text`.
- Direkt dahinter, ohne Leerzeichen, der Punkt in derselben Schrift und Größe in `signature`: im Dunkeln Gold `#c8a96e` (8,80:1 auf `bg-page`), im Hellen Bronze `#7d5f22` (5,26:1 auf `bg-page`).
- Beides ist ein Textelement, in Compose ein `AnnotatedString` mit einem `SpanStyle` für den Punkt. Der Screenreader liest nur „Lautwerk“: Die Semantik des Titels setzt dafür `contentDescription = "Lautwerk"`, damit der Punkt nicht als Satzzeichen erscheint.
- Eine Dachzeile oder Beschreibung steht nicht im Kopf. Die Dachzeile „A voice box for your AI“ gehört auf Projektseite und README.
- In Android Auto bleibt der Titel reiner Text, den der Host zeichnet (Abschnitt „Android Auto“, B4).
- Am Verhalten ändert sich nichts. Zeigt die App ihren Namen heute nirgends als Titel, kommt dafür auch keiner hinzu.

### Gespräch, Einstellungen, Verlauf

- Die drei Bereiche sind **Reiter** (siehe oben), in der heutigen Reihenfolge, mit den Icons `comments`, `cog` und `history`.
- Wo heute ein Bildschirmtitel steht, ist er `app-title`.
- Abschnittstitel innerhalb eines Bereichs, etwa die Gruppen in den Einstellungen, sind `app-section` in `text`: 24 dp Abstand nach oben, 8 dp nach unten.
- Dialogtitel sind `app-dialog-title`.
- **„Schlüssel speichern“** ist die einzige manuelle Speicheraktion. Sie bleibt auf der Seite, auf der der Schlüssel heute eingegeben wird, als Primär-Button ohne Icon direkt unter dem Eingabefeld. In der unteren Leiste erscheint sie nicht.

### Meldungen der App

Bestätigungen, Hinweise und die meisten Fehler zeigt die App als Meldung auf der Seite. Zwei Ausnahmen sind am 14. Oktober 2026 entschieden: Fehler beim Speichern oder Entfernen des Schlüssels stehen am Feld (V2, siehe „Fehler am Schlüsselfeld“), und ein Text, der heute zugleich in der unteren Leiste steht, steht nur noch dort (V1). Jede übrige Meldung wird zum Baustein „Meldungen“, an ihrer heutigen Stelle:

| Stelle heute | Text der App | Art |
|---|---|---|
| oben, unter der Reiterleiste, in jedem Reiter | Gespräch konnte nicht geöffnet, Gespräch nicht gelöscht, Export nicht vorbereitet, Speicherauswahl nicht geöffnet, Export fehlgeschlagen oder unterbrochen | Fehler |
| ebenda | „Schlüssel für … verschlüsselt gespeichert“, „Schlüssel für … entfernt“, „Gespräch gelöscht“, „Textdatei gespeichert. …“ | Erfolg |
| ebenda | „Start abgebrochen“ nach „Start abbrechen“ | ohne Wertung |
| ebenda | Modell des Chats nicht mehr im Katalog | Hinweis |
| ebenda, Gesprächsmeldung, nur solange das Gespräch läuft | „Die Antwort wurde am Limit gekürzt.“, „Der Anbieter hat diese Anfrage abgelehnt.“ | Hinweis |
| ebenda | „Der lokale Chatverlauf konnte nicht gelesen werden.“ mit „Verlauf erneut laden“ | Fehler mit Aktion |
| Reiter „Verlauf“, unter der Suche | „Der Verlauf konnte nicht durchsucht werden. …“ mit „Suche erneut versuchen“ | Fehler mit Aktion |
| Stimme und Tempo, unter der Hörprobe | Stimme oder Sprechtempo gespeichert | Erfolg |
| ebenda | Hörprobe fehlgeschlagen, nicht gestartet oder zu lang, Sprachausgabe antwortet nicht oder ist nicht verfügbar, gewählte Stimme nicht mehr verfügbar, Antwort konnte nicht vorgelesen werden, anderer Audiovorgang hat Vorrang | Fehler |
| Stimme und Tempo, an ihrer Stelle | „Bitte zuerst das laufende Gespräch beenden.“, „Gespeicherte Stimme nicht verfügbar“, „Keine zusätzlichen Stimmen … gemeldet. …“ | Hinweis |

- Laufende Vorgänge, etwa „Schlüssel wird gespeichert …“, „Verlauf wird geladen …“, „Verlauf wird durchsucht …“ und „Stimmen werden geladen …“, sind Textzeilen mit Icon `pending` (siehe Meldungen).
- **Nur in der Leiste (V1):** Die Gesprächsmeldung steht auf der Seite nur, solange das Gespräch läuft. Endet es mit einer Meldung, steht sie nur noch im Label der Leiste (siehe Gesprächszustände); das gilt auch für „Start abgebrochen“, wenn die App einen begonnenen Start beim Verlassen abbricht. „Export abgebrochen“ steht nur noch als Export-Label in der Leiste.
- Beim Export bleiben die Meldungen mit eigenem, längerem Text neben dem Export-Label stehen, etwa „Textdatei gespeichert. Die exportierte Kopie ist unverschlüsselt.“ oder „Export fehlgeschlagen. Am gewählten Speicherort …“; sie sagen mehr als das Label.
- Wann eine Meldung erscheint und wann sie verschwindet, bleibt sonst, wie die App es heute macht.

### Fehler am Schlüsselfeld

Entschieden am 14. Oktober 2026 (V2). Das gilt für das Schlüsselfeld im Reiter „Einstellungen“ und für das Schlüsselfeld auf „Audio und Gespräch einrichten“ im Prototyp. Die Suche im Verlauf bekommt keinen Feldfehler: Scheitert die Suche, ist das kein Eingabefehler, und die Meldung mit „Suche erneut versuchen“ bleibt.

- **Wann geprüft wird:** nur beim Tippen auf „Schlüssel speichern“ bzw. „Entfernen“ / „Schlüssel löschen“. Nicht beim Tippen ins Feld und nicht beim Verlassen des Feldes.
- **Was geprüft wird:** vor dem Speichern dieselbe Regel, die die App heute erst beim Gesprächsstart anwendet: keine Leer- und keine Steuerzeichen im Schlüssel. Scheitert die Prüfung, wird nichts gespeichert, und die Eingabe bleibt im Feld, damit man sie korrigieren kann.
- **Welche Fehler am Feld stehen:**

| Fehler | Text | Wo |
|---|---|---|
| Leer- oder Steuerzeichen im Schlüssel (neu) | „Der Schlüssel darf keine Leer- oder Steuerzeichen enthalten.“ / „The key must not contain spaces or control characters.“ | beide Felder |
| Speichern gescheitert | „Schlüssel konnte nicht gespeichert werden.“ | Einstellungen |
| Entfernen gescheitert | „Schlüssel konnte nicht entfernt werden.“ | Einstellungen |
| leeres Feld beim Speichern | „Bitte geben Sie einen Schlüssel ein. …“ | Einrichtung (im Assistenten ist „Schlüssel speichern“ bei leerem Feld deaktiviert, wie heute) |
| Speichern oder Löschen gescheitert | „Der Schlüssel konnte nicht gespeichert oder gelöscht werden. …“ | Einrichtung |

- **Wann er verschwindet:** sobald im Feld etwas geändert wird, und nach einem erfolgreichen Speichern oder Entfernen.
- **Darstellung:** Rahmen 2 dp `status-danger`; unter dem Feld statt des Hilfetexts Icon `exclamation-circle` 16 dp, 4 dp Abstand, Text `app-label` in `danger-text`. Die Beschriftung über dem Feld bleibt `text`.
- Bestätigungen bleiben, wo sie heute stehen: im Assistenten als Meldung oben auf der Seite, auf der Einrichtungsseite in der Zeile unter dem Feld.
- Ein ungültiger Schlüssel, der schon gespeichert ist, fällt weiter erst beim Gesprächsstart auf. Dieser Fehler steht dann nach V1 im Label der Leiste, nicht am Feld, weil das Feld auf einem anderen Reiter liegt.

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

Jeder Zustand ist ein Status-Label mit festem Icon und festem Wort. **Das Label steht genau einmal sichtbar, in der unteren Leiste.** „Genau einmal“ gilt für das Status-Label: Es gibt kein zweites Zustandslabel, und außerhalb der Leiste erscheint der Gesprächszustand nicht als Label. Eine Meldung auf der Seite (siehe Meldungen) ist kein zweiter Zustand, sondern die Rückmeldung der App zu einem Vorgang.

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
- Bei einem Fehler nennt das Wort, was passiert ist („Verbindung unterbrochen“, „Mikrofon nicht erlaubt“), nicht „Fehler“. Zeigt die App dafür heute ihren vorhandenen Meldungstext, auch als ganzen Satz, steht dieser Text im Label; er bricht um und wird nicht gekürzt. Neue, kürzere Texte führt das Design nicht ein.
- **Meldung nach dem Gesprächsende (V1, entschieden am 14. Oktober 2026):** Endet ein Gespräch mit einer Meldung, steht sie nur im Label der Leiste, an Stelle von „Beendet“. Auf der Seite erscheint sie dann nicht. Das Label hat die Art des Textes (siehe Meldungen der App): ein Fehler getönt Fehler mit `exclamation-circle` wie in der Tabelle, eine gekürzte oder abgelehnte Antwort getönt Info mit `info-circle`, „Start abgebrochen“ getönt neutral mit `ban`.
- Erscheint während eines laufenden Gesprächs eine Meldung, steht sie nur auf der Seite; die Leiste zeigt weiter den Zustand. Endet das Gespräch, wandert sie in die Leiste.
- Bis die App V1 umsetzt, zeigt sie den Text nach dem Ende noch an beiden Stellen. Das gilt bis dahin nicht als Verstoß gegen „genau einmal“.
- Zustandswechsel ohne Animation, kein Pulsieren, kein Dauerspinner. Für Screenreader ist das Label eine höfliche Live-Region (`LiveRegionMode.Polite`).
- Für Android Auto gilt der eigene Abschnitt „Android Auto“ (`android-auto.md`). Dort zeichnet der Host die Oberfläche; der Prototyp zeigt seine Zustände als Text in der ersten Zeile der Nachricht, ohne Label, ohne Icon und ohne die Farben dieser Tabelle.

## Der Android-Auto-Prototyp am Telefon

Der Prototyp (`prototype-app`) hat am Telefon eigene Seiten aus nativen Android-Views. Für sie gelten die Bausteine oben. Anordnung, Berechtigungen, Schlüsselverwaltung und Abläufe bleiben, wie sie sind; hier steht, welcher Baustein für welches vorhandene Element gilt. Geändert werden nur die Beschriftung „Test starten“ (V3) und die Fehler am Schlüsselfeld (V2), beide am 14. Oktober 2026 entschieden. Was im Auto erscheint, regelt `android-auto.md`.

Für beide Seiten gilt:

- Bildschirmrand nach „Raster und Flächen“: 16 dp, ab 600 dp Breite 24 dp. Das ersetzt die bisherigen 28 dp der Einrichtungsseite.
- Grund `bg-page`, Hell und Dunkel nach dem Gerät. Das Startfenster bleibt `brand-ink` (siehe Startbild).
- Bei großer Systemschrift bricht alles um und wächst mit; nichts wird gekürzt.

### Telefonseite

Von oben nach unten, wie heute:

| Element | Baustein und Stil | Abstand nach oben |
|---|---|---|
| App-Name | wie „Kopf der App“: „Lautwerk“ in `app-title`, der Punkt in `signature` | — |
| Hinweis zum UI-Test | `app-body` in `text-subtle` | 16 dp |
| „KI-Anbieter“ mit Auswahl | Auswahlfeld (siehe Bausteine) | 24 dp |
| „Modell“ mit Auswahl | Auswahlfeld | 16 dp |
| „Auswahl gespeichert: …“ | `app-label` in `text-subtle`, höfliche Live-Region | 16 dp |
| „Bereit für den UI-Test.“ bzw. „Gestartet · Test n“ | `app-body` in `text`, höfliche Live-Region, kein Status-Label | 24 dp |
| „Test starten“ (bisher „Starten“) | Primär-Button in voller Breite, ohne Icon | 16 dp |
| „Audio und Gespräch einrichten“, nur im DHU-Build | Sekundär-Button in voller Breite, ohne Icon | 16 dp |
| Hinweis zur Variante | `app-label` in `text-subtle` | 24 dp |

- **Anbieter und Modell** bleiben native Auswahlfelder (`Spinner`). Sie werden nicht durch Chips ersetzt, wie sie der Assistent verwendet. Eine Wahl gilt sofort und wird ohne Speichern-Button gespeichert; wechselt der Anbieter, zeigt das Modellfeld dessen Modelle. Darstellung von Feld und Liste: Baustein „Auswahlfelder“.
- **„Test starten“** zählt auf dieser Seite weiter den UI-Test hoch, wie heute. Er heißt nicht mehr „Starten“, damit er sich vom Start im Auto unterscheidet (V3). Er ist der einzige Primär-Button der Seite.
- **Texte, die den Button nennen,** folgen der neuen Beschriftung:

| Text | Deutsch | Englisch (auch Standard) |
|---|---|---|
| Button (`start`) | „Test starten“ | „Start test“ |
| Hinweis zum UI-Test (`prototype_note`) | „Wähle Anbieter und Modell für den UI-Test. Die Auswahl bleibt gespeichert; „Test starten“ führt noch keine KI-Anfrage aus.“ | „Choose a provider and model for the UI test. Your selection is saved; Start test does not send an AI request yet.“ |
| Hinweis zur Variante im DHU-Build (`dhu_note`) | „Die Auswahl gilt auch für Android Auto. Richten Sie unten Audiotests und Gespräch ein. „Test starten“ hier bleibt der UI-Zähler; in Android Auto startet „Gespräch starten“ bzw. „Audiotest starten“ den gewählten Modus.“ | „Selection is shared with Android Auto. Configure audio tests and conversation below. Start test here remains the UI counter; in Android Auto, Start conversation or Start audio check runs the chosen mode.“ |

- **Der Zugang „Audio und Gespräch einrichten“** ist ein Sekundär-Button, direkt unter „Test starten“, in derselben Breite. Er öffnet die Einrichtungsseite wie heute. Im Telefon-Build ohne DHU gibt es ihn nicht, und er kommt dort auch nicht hinzu.

### Seite „Audio und Gespräch einrichten“

Die Seite öffnet sich über den Zugang auf der Telefonseite. Einen sichtbaren Titel hat sie heute nicht, und es kommt keiner hinzu; „Audio und Gespräch einrichten“ bleibt der Fenstertitel, den ein Screenreader ansagt. Zurück geht es wie heute mit der Zurück-Geste.

Von oben nach unten, wie heute:

| Nr. | Element | Baustein und Stil | Abstand nach oben |
|---|---|---|---|
| 1 | Erklärung „Testen Sie zuerst die Ausgabe …“ | `app-body` in `text` | — |
| 2 | Drei Testmodi | Liste in einer Karte mit Optionsfeldern | 16 dp |
| 3 | „Mikrofon und Benachrichtigungen erlauben“ | Sekundär-Button in voller Breite, ohne Icon | 16 dp |
| 4 | „API-Schlüssel für …“ und Eingabe | Eingabefeld | 24 dp |
| 5 | Zeile unter dem Feld | Hilfetext des Feldes bzw. Rückmeldung (siehe unten) | 4 dp |
| 6 | „Schlüssel speichern“, „Schlüssel löschen“ | zwei Sekundär-Buttons untereinander in voller Breite, 8 dp Abstand, ohne Icon | 16 dp |
| 7 | Status des Telefontests | `app-body` in `text`, höfliche Live-Region | 24 dp |
| 8 | „Auf dem Telefon starten“ bzw. „Beenden“ | Primär-Button in voller Breite, ohne Icon | 16 dp |
| 9 | Erklärung zum Telefontest | `app-body` in `text-subtle` | 16 dp |

- **Testmodi:** Jede Option ist ein Listeneintrag mit Optionsfeld links, 16 dp Abstand zum Text in `app-body`, `text`, mindestens 56 dp hoch. Der Text bleibt, wie er ist, mit Nummer und Gedankenstrich; er bricht bei Bedarf um. Die ganze Zeile wählt. Die Wahl gilt sofort und wird ohne Speichern-Button gespeichert, wie heute. Zwischen den Einträgen 1 dp `line`, links um 16 dp eingerückt.
- **Berechtigungen:** Der Button öffnet wie heute den Android-Dialog für die Berechtigungen, die noch fehlen. Er bleibt immer sichtbar. Einen eigenen Berechtigungsstatus zeigt die Seite nicht, und es kommt keiner hinzu.
- **Schlüsselfeld:** ein Passwortfeld wie heute, mit Beschriftung darüber und dem Platzhalter „Schlüssel nur auf diesem Telefon eingeben“ in `text-subtle`. Zustände nach der Tabelle unter „Eingabefelder“. Fehler stehen am Feld, wie unter „Fehler am Schlüsselfeld“ beschrieben (V2): Rahmen 2 dp `status-danger`, der Fehlertext in der Zeile darunter. Nach einem erfolgreichen Speichern wird das Feld wie heute geleert; bei Leer- oder Steuerzeichen bleibt die Eingabe stehen.
- **Zeile unter dem Feld:** Sie zeigt zuerst den Hinweis zum getrennten Schlüsselspeicher und nach „Speichern“ oder „Löschen“ an derselben Stelle die Rückmeldung der App. Für Screenreader ist sie eine höfliche Live-Region.

| Text der App | Darstellung |
|---|---|
| Hinweis zum getrennten Schlüsselspeicher | `app-label` in `text-subtle`, ohne Icon |
| „Schlüssel verschlüsselt gespeichert.“, „Schlüssel gelöscht.“ | Icon `check-circle` 16 dp in `status-success`, 4 dp Abstand, Text `app-label` in `text` |
| „Bitte geben Sie einen Schlüssel ein. …“, „Der Schlüssel konnte nicht gespeichert oder gelöscht werden. …“, „Der Schlüssel darf keine Leer- oder Steuerzeichen enthalten.“ | Fehler am Feld: Rahmen 2 dp `status-danger`, Icon `exclamation-circle` 16 dp, 4 dp Abstand, Icon und Text `app-label` in `danger-text` |

- **Schlüssel-Buttons:** Beide sind Sekundär-Buttons, „Schlüssel speichern“ oben. Beide bleiben aktiv wie heute; auf ein leeres Feld antwortet die App mit ihrem Hinweis. „Schlüssel löschen“ ist kein roter Löschen-Button, weil es keinen Löschdialog gibt, und das Design ergänzt keinen.
- **Telefontest:** Der Status zeigt den Text der App als Textzeile, etwa „Bereit.“, im Gespräch „Ich höre zu – Claude / Anthropic“ oder eine Fehlermeldung. Er ist kein Status-Label: Die Zeile mischt Zustände und Meldungen in einem Text, und eine Zuordnung zu den festen Labels der Gesprächszustände gibt es hier nicht. Kein Icon, keine Farbe nach Zustand.
- **Primäraktion:** „Auf dem Telefon starten“, während eines Tests „Beenden“, an derselben Stelle wie heute. Sie ist der einzige Primär-Button der Seite und trägt kein Icon, weil der gewählte Modus entscheidet, ob das Mikrofon öffnet. Im Assistenten ist „Schlüssel speichern“ primär, weil „Gespräch starten“ dort in der eigenen unteren Leiste steht; hier stehen beide Aktionen in einer Spalte, und primär bleibt die Aktion, die etwas startet.

## Verhaltensentscheidungen

Diese Punkte ändern, was die App tut oder anzeigt, und werden deshalb nicht vom Design entschieden, sondern von Stefan. Was hier als entschieden steht, ist eine freigegebene Änderung der App; die Abschnitte oben beschreiben dann das Ziel. Bis die App sie umsetzt, gilt dort das heutige Verhalten.

- **V1 Gleicher Text an zwei Stellen, entschieden am 14. Oktober 2026: nur in der Leiste.** Endet ein Gespräch mit einer Meldung, steht sie nur im Label der Leiste, nicht mehr zusätzlich auf der Seite. „Export abgebrochen“ steht nur noch als Export-Label. Während eines Gesprächs bleibt die Gesprächsmeldung auf der Seite (siehe Gesprächszustände, Meldungen der App).
- **V2 Fehler an Feldern, entschieden am 14. Oktober 2026: direkt am Feld.** Fehler beim Speichern oder Entfernen des Schlüssels stehen am Schlüsselfeld, im Assistenten wie im Prototyp. Neu ist die Prüfung auf Leer- und Steuerzeichen beim Speichern, mit einem neuen Text (siehe Fehler am Schlüsselfeld).
- **V3 Beschriftung der Startaktionen, entschieden am 14. Oktober 2026:** Auf der Telefonseite des Prototyps heißt der Button für den UI-Test „Test starten“. Im Auto heißt die Aktion „Gespräch starten“, wenn am Telefon der Modus Gespräch gewählt ist, und in den beiden Audiotests „Audiotest starten“, damit kein Button ein Gespräch behauptet, das nicht stattfindet. Einzelheiten und die Texte, die die Aktion nennen, stehen in `android-auto.md` (B4, Beschriftung der Startaktion).

O4 (Vorlagengrenze) und O5 (gespeichertes Ergebnis nach Sprachwechsel) sind ebenfalls am 14. Oktober 2026 entschieden; sie stehen in `android-auto.md` unter „Entscheidungen“.

## App-Symbol, Startbild und Benachrichtigung

Das gilt für jede eigene Android-App. Die fertigen Dateien für Lautwerk stehen am Ende dieses Abschnitts.

### Aufbau des App-Symbols

- **Adaptives Symbol aus drei Ebenen** im Raster 108 × 108 dp:
  - Hintergrund: vollflächig `brand-ink` (`#0A0A0F`).
  - Vordergrund: das Zeichen der App in `brand-gold` (`#C8A96E`), sonst nichts.
  - Monochrom: dasselbe Zeichen in einer Farbe. Android färbt es für Themensymbole ab Android 13 selbst ein.
- **Sicherheitszone:** Das Zeichen liegt vollständig im Kreis mit 66 dp Durchmesser um die Mitte. Android zeigt nur die inneren 72 dp und schneidet je nach Gerät zu Kreis, Squircle oder abgerundetem Quadrat zu; außerhalb der 66 dp kann etwas abgeschnitten werden.
- **Das Zeichen trägt den Punkt der Wortmarke.** Daran erkennt man die Apps als Familie, wie am Punkt in „Azentrius.“.
- Aussparungen im Zeichen sind echte Löcher (Füllregel `evenOdd`), keine Flächen in Tinte. Nur so bleiben sie in der Monochrom-Ebene und in der Benachrichtigung sichtbar.
- Kein Schatten, kein Verlauf, keine zweite Farbe.
- Das runde Symbol (`android:roundIcon`) ist dieselbe Datei. Alle Ebenen sind Vektoren; ab `minSdk` 26 braucht es keine PNG-Dateien je Auflösung.
- Die Zeile `<monochrome>` setzt `compileSdk` 33 oder höher voraus. Ältere Android-Versionen ignorieren sie.

### Startbild

- Grund `brand-ink`, in der Mitte das Zeichen. Die Maske des App-Symbols ist unsichtbar, weil sein Grund dieselbe Farbe hat. Kein Text, kein Markenbild am unteren Rand, keine Animation.
- Es gilt auf allen unterstützten Android-Versionen gleich. Beim Start blitzt nie eine weiße Fläche auf.
- Ab Android 12 zeichnet das System das Startbild selbst: `windowSplashScreenBackground` = `@color/ic_launcher_background`, das Symbol bleibt ungesetzt, dann nimmt das System das App-Symbol. Vor Android 12 zeigt die App beim Start den Fensterhintergrund ihres Themas; der ist dann ebenfalls `brand-ink`.
- Wie die App das technisch umsetzt, etwa mit `androidx.core:core-splashscreen`, legt der Umstellungsplan fest, nicht dieses System.

### Benachrichtigung

- Kleines Symbol (`setSmallIcon`): die Silhouette des Zeichens, 24 dp, weiß. Das Zeichen füllt 22 dp, 1 dp Rand wie bei Material-Symbolen. Android färbt es selbst ein.
- Keine Benachrichtigungsfarbe in Gold (`setColor`). Je nach Android-Version stünde dann Weiß auf Gold (2,24:1) oder Gold auf hellem Grund (2,2:1); beides unterschreitet 3:1.

### Android Auto und Play Store

- Android Auto zeigt das Launcher-Symbol der App: im Kopf der Vorlage (`Action.APP_ICON`) und in der Startleiste. Ein eigenes Symbol für das Auto gibt es nicht. Alles Weitere im Abschnitt „Android Auto“.
- Nur bei einer Veröffentlichung: Play-Store-Symbol mit 512 × 512 px als PNG, quadratisch und vollflächig; die Ecken rundet Google selbst. Es zeigt den sichtbaren Ausschnitt des App-Symbols, also die inneren 72 dp. Titelgrafik und Screenshots legt dieses System erst fest, wenn eine Veröffentlichung ansteht.

### Lautwerk: Sprechblase mit Punkt

- **Zeichen:** eine Sprechblase mit dem Schwanz unten links. Die Sprachwelle aus vier abgerundeten Balken ist aus ihr ausgespart. Rechts daneben steht auf der Unterkante der Blase der Punkt.
- **Maße:** 47,8 × 37,9 dp im 108-dp-Raster, Halbdiagonale 30,5 dp, also innerhalb der Sicherheitszone (33 dp).
- Das Symbol löst das bisherige türkis-blaue „A“ (`ic_openauto`) des Prototyps ab, auch in Android Auto. In der Benachrichtigung des Gesprächsdienstes ersetzt es das Mikrofon-Symbol von Android (`ic_btn_speak_now`).
- Am Verhalten ändert sich nichts: Es wechseln nur Bilddateien und die Startfarbe.

Dateien im Ordner `android/apps/lautwerk/res/`:

| Datei | Inhalt | gehört zu |
|---|---|---|
| `mipmap-anydpi/ic_launcher.xml` | adaptives Symbol | `android:icon` |
| `mipmap-anydpi/ic_launcher_round.xml` | dasselbe Symbol | `android:roundIcon` |
| `drawable/ic_launcher_foreground.xml` | Vordergrund, Gold | beide Symbole |
| `drawable/ic_launcher_monochrome.xml` | Monochrom-Ebene | beide Symbole |
| `values/ic_launcher_background.xml` | Farbe `ic_launcher_background` = `brand-ink` | beide Symbole und das Startbild |
| `drawable/ic_stat_lautwerk.xml` | Benachrichtigungssymbol, 24 dp, weiß | `setSmallIcon` |

- Die Vorlagen als SVG und das Play-Store-Symbol `lautwerk-play-512.png` liegen in der Gruppe „App-Icons“.
- Alle Dateien erzeugt `scripts/app-icon-lautwerk.py` im Repo. Keine davon wird von Hand geändert.

## Dateien

Im Ordner `android/` dieses Systems:

| Pfad | Inhalt |
|---|---|
| `res/font/dm_sans_regular.ttf`, `dm_sans_medium.ttf`, `dm_sans_semibold.ttf` | DM Sans 400, 500, 600 als statische TTF, aus derselben Datei wie im Web erzeugt |
| `res/font/dm_serif_display_regular.ttf` | DM Serif Display für `brand-display` |
| `res/font/dm_serif_display_italic.ttf` | DM Serif Display kursiv für den Namenszusatz im Kopf der App |
| `res/font/dm_sans.xml`, `res/font/dm_serif_display.xml` | Schriftfamilien für XML-Layouts |
| `res/drawable/ic_az_*.xml` | alle Icons der Gruppe „Icons“ als Vector-Drawable, 24 dp, 2 dp Rand wie Material-Icons, Füllung schwarz zum Einfärben; Herkunft und Lizenz im Kopfkommentar jeder Datei |
| `apps/lautwerk/res/…` | App-Symbol, Startfarbe und Benachrichtigungssymbol von Lautwerk (siehe oben); nur für diese App |
| `LICENSES/OFL-DM-Sans.txt`, `LICENSES/OFL-DM-Serif-Display.txt` | SIL Open Font License 1.1 |
| `LICENSES/CC-BY-4.0-Font-Awesome.txt` | Namensnennung und Lizenz der Font-Awesome-Icons (CC BY 4.0), also aller `ic_az_*.xml` außer `ic_az_pending.xml` |
| `LICENSES/MIT-PatternFly-Icons.txt` | MIT-Lizenz von PatternFly (Red Hat) für `ic_az_pending.xml` |

- Die Lizenzdateien gehören in die App, etwa auf eine Seite „Lizenzen“ in den Einstellungen. Die Namensnennung für Font Awesome ist Pflicht der CC BY 4.0; sie bleibt auch im Kopfkommentar jeder Datei stehen.
- Übernimmt eine App nur einen Teil der Icons, nennt sie die Herkunft der übernommenen: Font Awesome Free 5 unter CC BY 4.0 bzw. PatternFly unter MIT.
- Die Schriften enthalten den lateinischen Grundumfang mit allen deutschen Zeichen. Fehlende Zeichen, etwa Emojis, übernimmt die Systemschrift.
- Icons werden über ihre Füllung in der Farbe des Zusammenhangs eingefärbt (`Icon(tint = …)`), nie in einer festen Farbe.

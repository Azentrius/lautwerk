# Android Auto

So wird das Azentrius-Designsystem auf den OpenAuto-AI-Prototyp im Desktop Head Unit Emulator (DHU) übertragen. Die Regeln im Abschnitt „Android“ gelten weiter für das Smartphone; hier gelten sie nicht.

**Stand des Prototyps.** Der DHU-Build ist ein lokales Experiment. Die Kategorie, die im DHU-Manifest eingetragen ist, ist dort selbst als reine Debug-Sonde gekennzeichnet. Dieses Designsystem behauptet weder eine offiziell unterstützte Kategorie für allgemeine KI-Gespräche noch eine Zulassung für die Veröffentlichung oder den Einsatz im Fahrzeug. „Starten“ ist ein UI-Test und startet kein Gespräch.

Grundlage ist der Code im Branch `feature/azentrius-design` von `Azentrius/openauto-ai` (Car App Library 1.8.0-rc01, `minCarApiLevel` 1).

## Grundsatz

- **Das Design ändert kein Verhalten.** Es legt fest, wie die vorhandene Oberfläche aussieht, nicht, was sie kann.
- **Android Auto zeichnet die Oberfläche.** Die App liefert Inhalte und wenige Farbwünsche an eine Vorlage der Car App Library. Schriftfamilie, Schriftgrößen, Formen, Abstände, Hintergrund, Grundlayout und die tatsächlich dargestellten Farben bestimmt der Host.
- **Das Smartphone-Design wird nicht übertragen.** DM Sans, die Textstile, Radien, Abstände, Flächenfarben, Reiter, die untere Leiste und die Status-Labels aus „Android“ kommen im Auto nicht vor. Übernommen werden nur zwei Farbwerte (siehe Farben) und die Sprache der Texte.

## Gestaltungsspielraum

Der Prototyp verwendet ein `MessageTemplate` mit Titel, App-Symbol im Kopf, einer Nachricht und einer Aktion. Für genau diese Vorlage gilt:

| Eigenschaft | wer bestimmt | Spielraum der App im MessageTemplate |
|---|---|---|
| Schriftfamilie und Schriftgrößen | Host | keiner |
| Layout, Abstände, Formen, Positionen | Host | keiner |
| Hintergrund | Host | keiner. Der Farbton ist nicht festgelegt; im getesteten DHU war die Vorlagenfläche dunkelgrau, nicht schwarz (siehe „Ergebnis der Abnahme 0.1.14“). |
| Textfarbe von Titel und Nachricht | Host | keiner. Farb-Spans werden in der Nachricht nicht unterstützt und ignoriert; der Titel erlaubt nur Distanz-, Dauer- und Timer-Spans. |
| Zeilen und Kürzung der Nachricht | Host | keiner. Die Vorlage sieht bis zu 2 Zeilen umbrechenden Text vor. |
| Kopfbereich | Host zeichnet | App wählt `Action.APP_ICON` oder `Action.BACK`; `APP_ICON` zeigt das Launcher-Symbol der App und ist nicht bedienbar. |
| Titel | App | Text. Die Darstellung bestimmt der Host. |
| Nachricht | App | Text. Die Darstellung bestimmt der Host. |
| Aktionen im Inhalt | App | bis zu 2, davon eine als primär markierbar (`FLAG_PRIMARY`, ab Car API 4) |
| Button-Form, -Größe, -Position, Rückmeldung beim Drücken und Fokus | Host | keiner |
| Button-Hintergrund | App wünscht, Host entscheidet | `Action.Builder.setBackgroundColor(CarColor)`; der Host darf die Farbe ignorieren oder ersetzen |
| Button-Beschriftungsfarbe | App wünscht, Host entscheidet | nur über `ForegroundCarColorSpan` im Titel der Aktion |
| Akzentfarben der App | App | `carColorPrimary` und `carColorPrimaryDark`, `carColorSecondary` und `carColorSecondaryDark` über die Metadaten `androidx.car.app.theme`. Das sind Wünsche: Der Host darf sie ersetzen; die Dokumentation nennt zu wenig Kontrast als einen Fall dafür. |
| Icon im Nachrichtenbereich | App | optional, Zielgröße 128 × 128 dp; schließt die Ladeanzeige aus |
| Icon im Button | App | optional, Zielgröße 88 × 88 dp, Tönung über `CarColor` |
| Ladeanzeige | Host zeichnet | App schaltet sie ein (`setLoading`, ab Car API 2); im Prototyp nicht genutzt |
| Sprache der Texte | App | Der Host zeigt Texte ohne Übersetzung; die App muss sie lokalisiert liefern. |

## Verbindliche App-Vorgaben

Diese Punkte legt die App fest. Die Nummern dienen als Bezug im Umstellungsplan.

- **B1 Vorlage:** Es bleibt beim `MessageTemplate` mit Titel, `Action.APP_ICON` im Kopf, einer Nachricht und genau einer Aktion. Es kommen keine Aktionsleiste, keine zweite Aktion, kein Icon im Nachrichtenbereich und keine Ladeanzeige hinzu.
- **B2 Akzentfarbe:** Die App deklariert für das Auto genau ein Farbpaar: `carColorPrimary` = `#c8a96e`, `carColorPrimaryDark` = `#7d5f22`. Ein Sekundärpaar wird nicht gesetzt.
- **B3 Startaktion:** Hintergrund `CarColor.PRIMARY`. Die Beschriftung bekommt keine eigene Farbe (kein `ForegroundCarColorSpan`), kein Icon und keine Markierung `FLAG_PRIMARY`. Die Primär-Markierung ist laut Vorgabe für den Fall von zwei Aktionen gedacht; hier gibt es nur eine.
- **B4 Texte:** Titel, Nachricht und Aktion verwenden die vorhandenen Texte unverändert, in derselben Reihenfolge (siehe Inhalte). Die App kürzt nichts und lässt nichts weg.
- **B5 Keine Smartphone-Werte:** Außer den zwei Farbwerten aus B2 werden keine Azentrius-Tokens an den Host übergeben, auch keine Schrift. Das einzige Bild ist das App-Symbol, das der Host selbst als Launcher-Symbol zeigt (siehe Icons).

## Farben

### Zuordnung

Hintergrund und tatsächlich dargestellte Farben bestimmt der Host. Android Auto zeigt Inhalte auf dunklem Grund; welcher Ton das ist, legt der Host fest, und er kann je nach Host und Version abweichen. Die angeforderten Werte kommen deshalb aus dem Thema „Azentrius dunkel“ und aus der Markenfarbe.

**Gold und Bronze sind angefordert, nicht zugesichert.** Die App meldet das Farbpaar an; ob und wo es erscheint, entscheidet der Host. Ersetzt er es durch eine eigene Farbe, ist das nach A3 zulässig und kein Fehler der App.

| Angefordert über | Wert | Azentrius-Token | Rolle laut Car App Library |
|---|---|---|---|
| `carColorPrimary` | `#c8a96e` | `primary` in „Azentrius dunkel“, `brand-gold` | helle Variante, zum Beispiel für Farbe auf dunklem Grund |
| `carColorPrimaryDark` | `#7d5f22` | `signature` bzw. `focus` in „Azentrius hell“ | dunkle Variante, zum Beispiel als Fläche unter hellem Text |
| `carColorSecondary`, `carColorSecondaryDark` | nicht gesetzt | — | Azentrius hat nur eine Aktionsfarbe; ohne beide Werte nimmt der Host seine Standardvariante. |
| `CarColor.PRIMARY` | das Paar oben | — | Hintergrund der Startaktion (B3) |

So gehört das in die App, unter das `application`-Element des DHU-Builds:

```xml
<meta-data android:name="androidx.car.app.theme" android:resource="@style/CarAppTheme" />

<style name="CarAppTheme">
    <item name="carColorPrimary">#C8A96E</item>
    <item name="carColorPrimaryDark">#7D5F22</item>
</style>
```

### Tag und Nacht

- **Es gibt kein eigenes Farbpaar für Tag und Nacht.** Die beiden Varianten einer `CarColor` sind keine Tag- und Nachtwerte. Der Host wählt sie nach Verwendung: die dunkle zum Beispiel als Fläche unter hellem Text, die helle für Farbe auf dunklem Grund.
- Ob ein Host im Nachtmodus andere Varianten oder Farben wählt, ist nicht dokumentiert. In der Abnahme 0.1.14 zeigte der getestete Host bei Tag und Nacht dieselbe Ersatzfarbe.
- Abweichungen von wenigen RGB-Stufen zwischen Tag und Nacht stammen möglicherweise aus der komprimierten Videoübertragung des DHU. Sie belegen keine eigene Nachtpalette.
- Der DHU unterscheidet einen Tagmodus („hohe Helligkeit, volle Farbe“) und einen Nachtmodus („geringe Helligkeit, hoher Kontrast“). Beide zeichnet der Host.

### Wo der Host vorgibt oder überschreibt

- Hintergrund, Titel- und Nachrichtenfarbe gibt der Host vor. Laut Gestaltungsrichtlinien zeichnet er weißen Text mit 88 %, 72 % oder 56 % Deckkraft, nachts mit 96 %. Das sind dokumentierte Werte, keine Messung.
- Akzentfarben aus dem Thema darf der Host ersetzen. Die Dokumentation nennt unzureichenden Kontrast als einen Grund, schließt andere aber nicht aus. Warum der Host in der Abnahme 0.1.14 Gold und Bronze ersetzt hat, ist nicht nachgewiesen; dieses System nimmt keinen Grund an.
- Den Button-Hintergrund darf der Host ignorieren oder das Fahrzeugsystem ersetzen.
- Ohne App-Farben verwendet Android Auto laut Dokumentation seinen eigenen blauen Akzent („car accent“).

### Kontrast

Android Auto verlangt 4,5:1 zwischen Vordergrund (Text, Icons) und Hintergrund.

**Die folgende Tabelle ist eine theoretische Berechnung** der angeforderten Werte auf reinem Schwarz. Sie sagt nicht, was ein Host tatsächlich zeigt: Er kann einen anderen Grund wählen und die Farben ersetzen. Maßgeblich ist die Messung der tatsächlich dargestellten Vorder- und Hintergrundfarben in der Abnahme (A4). Berechnet sind alle Kombinationen, die mit B2 und B3 entstehen können:

| Kombination | Kontrast | Ergebnis |
|---|---|---|
| Gold `#c8a96e` als Farbe auf Schwarz | 9,36:1 | erfüllt |
| Weiß auf Bronze `#7d5f22` (Button-Fläche) | 5,95:1 | erfüllt |
| Weiß 96 % (Nacht) auf Bronze | 5,65:1 | erfüllt |
| Weiß 88 % auf Bronze | 5,04:1 | erfüllt |
| Tinte `#0a0a0f` oder Schwarz auf Gold | 8,80:1 bzw. 9,36:1 | erfüllt |
| Weiß 88 %, 72 %, 56 % auf Schwarz (Host-Text) | 15,91 · 10,59 · 6,49:1 | erfüllt |
| **Weiß auf Gold** | **2,24:1** | **nicht erfüllt** |
| **Bronze als Farbe auf Schwarz** | **3,53:1** | **nicht erfüllt** |

Die beiden nicht erfüllten Kombinationen entstehen nur, wenn der Host die Varianten gegen ihre dokumentierte Rolle einsetzt. Ob das passiert, prüft die Abnahme (A4). Tritt eine davon auf, ist die Abnahme nicht bestanden, und die Entscheidung O2 wird fällig. In der Abnahme 0.1.14 trat keine davon auf, weil der Host weder Gold noch Bronze zeigte.

Der Grund verändert die Werte: Auf der in der Abnahme 0.1.14 beobachteten Fläche `#25262A` ergäbe Gold als Farbe 6,73:1 und Bronze als Farbe 2,54:1. Auch das ist nur gerechnet.

## Icons

Belegt wird nur, was vorhanden ist und eine Position hat, die der Prototyp schon nutzt.

| Position | Belegung | Begründung |
|---|---|---|
| Kopf, `Action.APP_ICON` | das App-Symbol „Sprechblase mit Punkt“ (`@mipmap/ic_launcher` aus `android/apps/openauto-ai/res/`) | Der Host zeichnet das Launcher-Symbol der App. Es löst das bisherige `ic_openauto` ab (O1). |
| Nachrichtenbereich (`setIcon`) | keines | Die Azentrius-Icons für Gesprächszustände (`microphone-slash`, `microphone` und andere) würden ein echtes Gespräch behaupten. Für „UI-Test bereit“ gibt es kein passendes vorhandenes Icon. |
| Startaktion | keines | `microphone` steht im System für ein offenes Mikrofon, `play` ist der Hörprobe vorbehalten. Keines passt zu einem UI-Test. |
| Aktionsleiste, Kopfaktionen am Ende | nicht verwendet | Diese Positionen nutzt der Prototyp nicht (B1). |

**Lücke:** Für „UI-Test starten“ gibt es kein Icon im System.

Das App-Symbol ist im Auto dasselbe wie auf dem Smartphone. Aufbau und Dateien stehen im Abschnitt „Android“ unter „App-Symbol, Startbild und Benachrichtigung“. Im Auto liegt es auf dem Grund, den der Host wählt; auch die Maske zeichnet der Host.

**Für eine spätere Nutzung von Azentrius-Icons im Auto:** Die Dateien `ic_az_*.xml` sind schwarz gefüllt. Im Auto brauchen sie immer eine Tönung über `CarIcon.Builder.setTint(CarColor)`, sonst erscheint Schwarz auf dunklem Grund. Eine Tönung über `IconCompat` allein wendet der Host nicht sicher an.

## Inhalte

| Element | Deutsch | Englisch (auch Standard) | Darstellung |
|---|---|---|---|
| Titel | OpenAuto AI | OpenAuto AI | im Kopf neben dem App-Symbol |
| Nachricht, Zeile 1 (Status) | „Bereit für den UI-Test.“ bzw. „Gestartet · Test n“ | „Ready for the UI test.“ bzw. „Started · Test n“ | erste Zeile der Nachricht |
| Nachricht, Zeile 2 | „Anbieter · Modell“ | gleich | zweite Zeile, Trenner „ · “ |
| Startaktion | Starten | Start | einziger Button |

- **Reihenfolge:** zuerst der Status, dann Anbieter und Modell. Der Status ist das, was nach einem Tipp auf „Starten“ sichtbar wechselt. Muss der Host kürzen, trifft es dann das Ende von Zeile 2 und nicht den Status.
- **Längen heute:** Zeile 1 hat höchstens 23 Zeichen, Zeile 2 höchstens 38 Zeichen („Claude / Anthropic · Claude Sonnet 4.6“). Der Titel hat 11 Zeichen, der Button 7 („Starten“) bzw. 5 („Start“).
- **Lange Modellnamen:** Die App kürzt nicht und lässt Anbieter oder Modell nicht weg. Ob der Host Zeile 2 bei kleinen Bildschirmen kürzt, prüft A5. Textvarianten verschiedener Länge (`CarText.addVariant`, ab Car API 2) sind für die Nachricht im `MessageTemplate` nicht dokumentiert; darauf baut diese Regel nicht.
- **Sprache:** Alle vier Texte kommen aus derselben Sprachdatei: `values-de` für Deutsch, `values-en` und `values` für Englisch. Gemischte Sprachen auf einem Bildschirm sind ein Fehler.
- **Was nicht vorkommt:** Kein Text behauptet ein laufendes Gespräch, eine Zulassung oder eine Kategorie.

## Zustände und Bedienung

Es gibt genau die Zustände, die der Prototyp heute hat:

| Zustand | Auslöser | Nachricht, Zeile 1 |
|---|---|---|
| Bereit | neue Auto-Sitzung, noch kein Tipp | „Bereit für den UI-Test.“ / „Ready for the UI test.“ |
| Gestartet | jeder Tipp auf „Starten“; der Zähler n steigt um 1 | „Gestartet · Test n“ / „Started · Test n“ |
| Auswahl geändert | Anbieter oder Modell wird am Smartphone geändert | Zeile 2 zeigt die neue Auswahl, Zeile 1 bleibt |

- Rückmeldung beim Drücken und den Fokusrahmen bei Dreh-Drück-Steller oder Touchpad zeichnet der Host. Die App ergänzt nichts.
- Die einzige Rückmeldung der App ist der geänderte Text.
- Es gibt keinen Lade-, Fehler- oder Gesprächszustand im Auto. Die Gesprächszustände aus „Android“ gelten hier nicht.
- **Prüfpunkt, kein Designthema:** Laut Car App Library gilt ein `MessageTemplate` nur dann als Auffrischung, wenn Titel und Nachricht gleich bleiben. Jeder Tipp auf „Starten“ ändert die Nachricht und zählt damit gegen die Grenze von 5 Vorlagen pro Aufgabe. Ist sie erschöpft, zeigt der Host eine Fehlermeldung und schließt die App. A7 prüft, ob das im DHU eintritt. Eine Lösung wäre eine Verhaltensänderung und gehört nicht in dieses Designsystem.

## Abnahme im DHU

### Aufbau

- **Build:** Flavor `dhu`, Debug-Build, Smartphone per DHU verbunden.
- **Bildschirme:** drei Konfigurationen im Abschnitt `[general]`:
  - `resolution = 800x480`, `dpi = 160` (Standard)
  - `resolution = 1280x720`
  - `resolution = 1920x1080`
  - zusätzlich `resolution = 1280x720` mit `marginheight = 220` als breites, flaches Format
- **Modus:** Befehle `day` und `night` in der DHU-Konsole.
- **Eingabe:** Touch und `inputmode = rotary`, also Pfeiltasten und `dpad click` bzw. Eingabetaste.
- **Sprache:** Smartphone auf Deutsch und auf Englisch.
- **Auswahl:** längste Kombination „Claude / Anthropic · Claude Sonnet 4.6“ und kürzeste „ChatGPT / OpenAI · GPT-5.4“.
- **Host-Car-API:** Die tatsächlich ausgehandelte Stufe steht im Log der laufenden Sitzung (`car.onCreateScreen api=n`). Sie ist nicht `minCarApiLevel` und wird mit jedem Ergebnis notiert.
- **Farbmessung:** per Pipette im unskalierten DHU-Screenshot, an vollen Schriftstrichen statt geglätteter Randpixel, Kontrast aus der relativen Helligkeit wie bei WCAG. Die Werte stammen aus dem dekodierten Videobild des DHU; Unterschiede von wenigen RGB-Stufen können daher Messunschärfe sein.

### Kriterien

Jedes Kriterium gilt für jede Kombination aus Bildschirm, Modus und Sprache, sofern nicht anders angegeben. Belege sind Screenshots und Logcat.

| Nr. | Kriterium | bestanden, wenn |
|---|---|---|
| A1 | Kopf | App-Symbol und Titel „OpenAuto AI“ sind vollständig sichtbar. |
| A2 | Startaktion sichtbar | Der Button zeigt „Starten“ bzw. „Start“ vollständig. |
| A3 | Akzent | Der Button-Hintergrund ist Gold oder Bronze aus B2, oder der Host hat ihn sichtbar durch eine eigene Farbe ersetzt. Die tatsächlich dargestellte Farbe wird bei Tag und bei Nacht notiert. Eine Ersetzung durch den Host ist kein Fehler der App. |
| A4 | Lesbarkeit | Button-Text zu Button-Fläche mindestens 4,5:1, gemessen an den tatsächlich dargestellten Farben (siehe Farbmessung), bei Tag und Nacht, nie an den angeforderten Werten. Titel und Nachricht sind lesbar und nicht durch App-Farben verändert. |
| A5 | Vollständige Inhalte | Status, Anbieter und Modell sind vollständig sichtbar, auch bei 800×480 mit der längsten Kombination, in beiden Sprachen. Kürzt der Host etwas davon, ist A5 nicht bestanden und O3 wird fällig. |
| A6 | Startaktion funktioniert | Ein Tipp (Touch) und `dpad click` (Dreh-Drück-Steller) wechseln von „Bereit …“ zu „Gestartet · Test 1“, jeder weitere zu Test 2, 3 …. Logcat zeigt `car.start count=n` mit demselben n. |
| A7 | Viele Starts | 6 Tipps hintereinander auf 800×480: Der Zähler steigt bis 6, ohne Fehlermeldung des Hosts und ohne dass die App geschlossen wird. Das Ergebnis wird festgehalten, auch wenn es fehlschlägt. |
| A8 | Fokus | Im Rotary-Modus ist der Button erreichbar, und der Fokusrahmen des Hosts ist bei Tag und Nacht sichtbar. |
| A9 | Sprache | Alle Texte erscheinen in der Sprache des Smartphones, ohne Mischung; Englisch auch bei einer anderen Systemsprache. |
| A10 | Auswahl live | Eine Änderung von Anbieter oder Modell am Smartphone erscheint in Zeile 2, ohne dass das Auto neu verbunden wird. |
| A11 | Keine falschen Aussagen | Kein Text und kein Icon deutet ein laufendes Gespräch, eine Kategorie oder eine Zulassung an. |

### Ergebnis der Abnahme 0.1.14 (12. Oktober 2026)

**Beobachtung dieser einen Testumgebung.** Die Werte sind keine Design-Tokens und keine allgemeingültigen Host-Farben. Ein anderer Host, eine andere Android-Auto-Version oder ein Fahrzeug kann anders darstellen.

- **Testumgebung:** Pixel 10 Pro mit Android 17; Android Auto `17.7.663654-release` (Versionscode `177663654`); DHU `2.0-windows`, Build `2022-03-30-438482292`; ausgehandelte Host-Car-API 8, belegt durch `car.onCreateScreen api=8`.
- **App:** liefert B2 und B3 unverändert (`carColorPrimary` = `#C8A96E`, `carColorPrimaryDark` = `#7D5F22`, Startaktion mit `CarColor.PRIMARY`). Das gepackte Theme und seine Zuordnung im Manifest sind geprüft.

Startaktion auf 800×480:

| Stand | Modus | Button-Fläche | Button-Text | Kontrast |
|---|---|---|---|---|
| Ausgangsversion 0.1.12, ohne App-Farben | Tag und Nacht | `#3B3E46` | `#DFE2EA` | 8,25:1 |
| 0.1.14 mit B2 und B3 | Tag | `#BAD9FC` | `#0B3453` | 8,84:1 |
| 0.1.14 mit B2 und B3 | Nacht | `#BAD9FC` | `#0D3353` | 8,92:1 |

- **Akzent (A3):** Der Host zeigte Hellblau statt Gold oder Bronze, bei Tag und bei Nacht. Das ist nach A3 zulässig.
- **Ursache:** nicht nachgewiesen. Dieses System behauptet nicht, der Host habe die Farben wegen zu geringen Kontrasts verworfen.
- **Vorlagenfläche:** dunkelgrau `#25262A`, nicht schwarz, in beiden Versionen und beiden Modi.
- **Lesbarkeit (A4):** In 64 Kombinationen aus vier Bildschirmformaten, Deutsch und Englisch, Tag und Nacht, beiden Anbietern sowie Touch und Rotary lag der Button-Kontrast zwischen 8,47:1 und 8,98:1. Titel und Nachricht blieben unverändert lesbar.
- **Tag und Nacht:** Die Textwerte `#0B3453` und `#0D3353` unterscheiden sich nur um wenige RGB-Stufen. Das kann aus der Videoübertragung stammen und belegt keine eigene Nachtpalette.
- **Folgen:** B2 und B3 bleiben unverändert. O2 ist nicht eingetreten; ein Rückfall auf `CarColor.DEFAULT` ist nicht nötig.

Die übrigen Kriterien, die Rohmessungen mit Koordinaten und alle Belege stehen im Testbericht (siehe Quellen).

## Offene Entscheidungen

- **O1 App-Symbol, entschieden am 12. Oktober 2026:** Die App bekommt das Symbol „Sprechblase mit Punkt“ im Azentrius-Stil. Es ersetzt das türkis-blaue „A“ auf dem Smartphone und im Auto.
- **O2 Akzent nicht lesbar:** Zeigt A4 Weiß auf Gold oder Bronze als Textfarbe auf dem dunklen Grund, wird B3 auf `CarColor.DEFAULT` zurückgenommen. Alternativ werden andere vorhandene Werte gesucht. Erfunden wird kein neuer Wert. In der Abnahme 0.1.14 nicht eingetreten.
- **O3 Gekürzte Zeile 2:** Kürzt der Host Anbieter oder Modell, braucht es eine Entscheidung über kürzere Anzeigenamen im Modellkatalog. Das ist eine Änderung an Inhalten, nicht am Design.
- **O4 Vorlagengrenze:** Tritt bei A7 der Fehler auf, entscheidet die App-Seite, ob „Starten“ anders umgesetzt wird. Das Design bleibt davon unberührt.

## Für den Umstellungsplan

| Vorgabe | betroffene Stelle im App-Repo |
|---|---|
| B2 | `prototype-app/src/dhu/AndroidManifest.xml` (Metadaten) und eine neue Stil-Ressource für `CarAppTheme` im DHU-Flavor |
| B3 | `car-ui/src/main/java/dev/openautoai/car/OpenAutoScreen.kt`, `Action.Builder` der Startaktion |
| O1 App-Symbol | Manifest von `prototype-app` (heute `@drawable/ic_openauto`) und `app/src/main/AndroidManifest.xml` (heute ohne Symbol): `android:icon="@mipmap/ic_launcher"`, `android:roundIcon="@mipmap/ic_launcher_round"`; die Dateien aus `android/apps/openauto-ai/res/` kommen in das jeweilige Modul |
| B1, B4, B5 | `OpenAutoScreen.kt` und `car-ui/src/main/res/values*/strings.xml`: bleiben inhaltlich unverändert |
| A1–A11 | manuelle DHU-Abnahme. `prototype-app/src/androidTestDhu/…/CarTemplateTest.kt` prüft Teile von A6, A9 und A10 schon heute (englische Texte, Zähler, Übernahme der Auswahl) und kann B3 ergänzend prüfen (`CarColor.PRIMARY` als Hintergrund der Aktion). Farben und Kürzungen zeigt nur der DHU. |

## Quellen

- Android for Cars, Customize your app: <https://developers.google.com/cars/design/create-apps/apps-for-drivers/customize-app>. Die Seite verweist darauf, dass Aktualisierungen seit dem 11. März 2026 unter docs.partner.android.com/drivingux erscheinen.
- Message template: <https://developers.google.com/cars/design/create-apps/apps-for-drivers/templates/message-template>
- Button: <https://developers.google.com/cars/design/create-apps/apps-for-drivers/components/button>
- Color: <https://developers.google.com/cars/design/android-auto/design-system/color>
- Desktop Head Unit: <https://developer.android.com/training/cars/testing/dhu>
- Abnahme 0.1.14, Umsetzung und vollständiger Testbericht: <https://github.com/Azentrius/openauto-ai/pull/14>, dort `docs/BUILD_AND_TEST.md` und die Messwerte unter `docs/evidence/0.1.14/`
- Car App Library, Quelltext-Dokumentation zu `MessageTemplate`, `Action`, `CarColor`, `CarIcon`, `CarText`, `Header`, `Screen` und den `constraints`: <https://github.com/androidx/androidx/tree/androidx-main/car/app/app/src/main/java/androidx/car/app>

# Android Auto

So wird das Azentrius-Designsystem auf den Android-Auto-Prototyp von Lautwerk im Desktop Head Unit Emulator (DHU) übertragen. Die Regeln im Abschnitt „Android“ gelten weiter für das Smartphone; hier gelten sie nicht.

**Stand des Prototyps.** Der DHU-Build ist ein lokales Experiment. Die Kategorie, die im DHU-Manifest eingetragen ist, ist dort selbst als reine Debug-Sonde gekennzeichnet. Dieses Designsystem behauptet weder eine offiziell unterstützte Kategorie für allgemeine KI-Gespräche noch eine Zulassung für die Veröffentlichung oder den Einsatz im Fahrzeug.

**Was die Startaktion im Auto tut.** Seit 0.1.17 startet die eine Aktion den Modus, der am Telefon unter „Audio und Gespräch einrichten“ gewählt ist: den Ausgabetest (ohne Mikrofon und KI), den Mikrofon- und Erkennungstest (ohne KI) oder ein Gespräch mit dem gewählten Anbieter. Nach V3 (entschieden am 14. Oktober 2026) heißt sie im Modus Gespräch „Gespräch starten“ und in den beiden Audiotests „Audiotest starten“; bisher hieß sie „Starten“. Während einer Sitzung heißt sie „Beenden“. In diesem Abschnitt heißt sie kurz „Startaktion“. Getestet wird im Stand. Den früheren Offline-Zähler „Gestartet · Test n“ gibt es im Auto nicht mehr; er besteht nur noch auf der Telefonseite des Prototyps (siehe „Android“, Der Android-Auto-Prototyp am Telefon).

Grundlage ist `main` von `Azentrius/openauto-ai` bei `c262a3a` (0.1.18) mit dem Designabgleich in `docs/DESIGN_SYSTEM.md` (Car App Library 1.9.0-alpha03, `minCarApiLevel` 1). Die Telefonseiten des Prototyps, auch „Audio und Gespräch einrichten“, regelt der Abschnitt „Android“. Ab 0.2.0 heißt die App Lautwerk (Repo `Azentrius/lautwerk`), seit 0.3.0 mit den Paketen `com.azentrius.lautwerk.*` (in 0.2.0 `de.azentrius.lautwerk.*`); Pfade und Namen unten folgen dem neuen Stand, Links und Commits bleiben, wie sie waren.

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

- **B1 Vorlage:** Es bleibt beim `MessageTemplate` mit Titel, `Action.APP_ICON` im Kopf, einer Nachricht und genau einer Aktion. Die Aktion heißt je nach Lage „Gespräch starten“, „Audiotest starten“ oder „Beenden“; es bleibt dieselbe eine Aktion. Es kommen keine Aktionsleiste, keine zweite Aktion, kein Icon im Nachrichtenbereich und keine Ladeanzeige hinzu.
- **B2 Akzentfarbe:** Die App deklariert für das Auto genau ein Farbpaar: `carColorPrimary` = `#c8a96e`, `carColorPrimaryDark` = `#7d5f22`. Ein Sekundärpaar wird nicht gesetzt.
- **B3 Aktion:** Hintergrund `CarColor.PRIMARY`, bei jeder Beschriftung. Die Beschriftung bekommt keine eigene Farbe (kein `ForegroundCarColorSpan`), kein Icon und keine Markierung `FLAG_PRIMARY`. Die Primär-Markierung ist laut Vorgabe für den Fall von zwei Aktionen gedacht; hier gibt es nur eine.
- **B4 Texte:** Titel, Nachricht und Aktion verwenden die vorhandenen Texte der App unverändert, in der heutigen Reihenfolge: zuerst der Zustand der Sitzung, dann Anbieter und Modell (siehe Inhalte und Zustände). Der Titel bleibt reiner Text ohne Formatierung. Die App kürzt nichts und lässt nichts weg. Ausnahme nach V3: die Beschriftung der Startaktion und die Texte, die sie nennen (siehe Inhalte).
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
| `CarColor.PRIMARY` | das Paar oben | — | Hintergrund der einen Aktion, bei jeder Beschriftung (B3) |

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
| Kopf, `Action.APP_ICON` | das App-Symbol „Sprechblase mit Punkt“ (`@mipmap/ic_launcher` aus `android/apps/lautwerk/res/`) | Der Host zeichnet das Launcher-Symbol der App. Es löst das bisherige `ic_openauto` ab (O1). |
| Nachrichtenbereich (`setIcon`) | keines | B1 schließt es aus. Den Zustand zeigt der Text in Zeile 1; ein wechselndes Zustandsicon käme als neues Element hinzu. |
| Startaktion bzw. „Beenden“ | keines | B3 schließt es aus. `microphone` passte auch nicht immer: Der Ausgabetest öffnet kein Mikrofon. |
| Aktionsleiste, Kopfaktionen am Ende | nicht verwendet | Diese Positionen nutzt der Prototyp nicht (B1). |

Eine Icon-Lücke gibt es nicht, solange B1 und B3 gelten.

Das App-Symbol ist im Auto dasselbe wie auf dem Smartphone. Aufbau und Dateien stehen im Abschnitt „Android“ unter „App-Symbol, Startbild und Benachrichtigung“. Im Auto liegt es auf dem Grund, den der Host wählt; auch die Maske zeichnet der Host.

**Für eine spätere Nutzung von Azentrius-Icons im Auto:** Die Dateien `ic_az_*.xml` sind schwarz gefüllt. Im Auto brauchen sie immer eine Tönung über `CarIcon.Builder.setTint(CarColor)`, sonst erscheint Schwarz auf dunklem Grund. Eine Tönung über `IconCompat` allein wendet der Host nicht sicher an.

## Inhalte

| Element | Deutsch | Englisch (auch Standard) | Darstellung |
|---|---|---|---|
| Titel | Lautwerk | Lautwerk | im Kopf neben dem App-Symbol |
| Nachricht, Zeile 1 (Zustand) | ein Text aus der Tabelle unter „Zustände“, z. B. „Bereit.“ oder „Hört zu“ | z. B. „Ready.“ oder „Listening“ | erste Zeile der Nachricht |
| Nachricht, Zeile 2 | „Anbieter · Modell“ | gleich | zweite Zeile, Trenner „ · “ |
| Aktion | „Gespräch starten“ im Modus Gespräch, „Audiotest starten“ im Ausgabe- und im Mikrofontest; während einer Sitzung „Beenden“ | „Start conversation“, „Start audio check“ bzw. „End“ | einziger Button |

- **Reihenfolge:** zuerst der Zustand, dann Anbieter und Modell. Der Zustand ist das, was sich während einer Sitzung ändert. Muss der Host kürzen, trifft es dann das Ende von Zeile 2 und nicht den Zustand.
- **Längen heute:** Zeile 1 hat höchstens 29 Zeichen auf Deutsch („Mikrofon am Telefon erlauben.“) und 30 auf Englisch („Allow microphone on the phone.“). Zeile 2 hat höchstens 38 Zeichen („Claude / Anthropic · Claude Sonnet 4.6“). Der Titel hat 11 Zeichen, die Aktion höchstens 17 („Audiotest starten“) bzw. 18 („Start conversation“). Die neuen Beschriftungen sind länger als „Starten“; ob der Host sie vollständig zeigt, prüft A2.
- **Lange Modellnamen:** Die App kürzt nicht und lässt Anbieter oder Modell nicht weg. Ob der Host Zeile 2 bei kleinen Bildschirmen kürzt, prüft A5. Textvarianten verschiedener Länge (`CarText.addVariant`, ab Car API 2) sind für die Nachricht im `MessageTemplate` nicht dokumentiert; darauf baut diese Regel nicht.
- **Sprache:** Alle Texte im Auto kommen aus denselben Sprachdateien der App: `values-de` für Deutsch, `values-en` und `values` für Englisch. Gemischte Sprachen auf einem Bildschirm sind ein Fehler.
- **Was nicht vorkommt:** Kein Text behauptet eine Kategorie oder eine Zulassung. Zustände wie „Hört zu“ oder „Spricht“ sind erlaubt, weil die App diese Vorgänge tatsächlich ausführt; sie stehen nur da, solange sie zutreffen.

### Beschriftung der Startaktion (V3)

- Die Beschriftung folgt dem Modus, der am Telefon gewählt ist: „Gespräch starten“ nur im Modus Gespräch, sonst „Audiotest starten“. So behauptet kein Button ein Gespräch, das nicht stattfindet (A11).
- Wird der Modus am Telefon geändert, während das Auto verbunden ist, zeigt die Startaktion die neue Beschriftung, ohne neue Verbindung.
- Texte, die bisher „Starten“ nannten, sprechen vom Starten, ohne einen Button zu nennen, weil sie im Auto wie am Telefon erscheinen:

| Text | Deutsch | Englisch (auch Standard) |
|---|---|---|
| `voice_permission` | „Erlauben Sie den Mikrofonzugriff am Telefon unter „Audio und Gespräch einrichten“ und starten Sie erneut.“ | „Allow microphone access on the phone under Audio and conversation setup, then start again.“ |
| `voice_disconnected` | „Audiodienst getrennt. Starten Sie erneut, um es noch einmal zu versuchen.“ | „Audio service disconnected. Start again to retry.“ |
| `voice_service_wait` | „Audiodienst verbindet sich. Bitte starten Sie erneut.“ | „Audio service is connecting. Please start again.“ |
| `voice_setup_help`, zweiter Satz | „Der gewählte Modus gilt für den Start in Android Auto und für den Telefontest.“ | „The selected mode applies to starting in Android Auto and to the phone test.“ |
| `voice_phone_help`, zweiter Satz | „Für das Automikrofon wählen Sie hier den Modus, öffnen Lautwerk in Android Auto und tippen dort auf „Gespräch starten“ bzw. „Audiotest starten“.“ | „To test the car microphone, choose the mode here, open Lautwerk in Android Auto and press Start conversation or Start audio check there.“ |

Alle übrigen Sätze dieser Texte bleiben unverändert. „Auf dem Telefon starten“ auf der Einrichtungsseite bleibt, wie es ist.

## Zustände und Bedienung

Es gibt genau die Zustände, die der Prototyp heute hat. Zeile 1 zeigt den Text der App, ohne Label, Icon oder eigene Farbe; die Gesprächszustände aus „Android“ mit ihren Labels gelten hier nicht.

| Zustand | wann | Zeile 1, Deutsch / Englisch | Aktion |
|---|---|---|---|
| Bereit | keine Sitzung aktiv, noch kein Ergebnis gespeichert | „Bereit.“ / „Ready.“ | Startaktion |
| Mikrofon nicht erlaubt | Start ohne Mikrofonberechtigung, die Anfrage wurde abgelehnt | „Mikrofon am Telefon erlauben.“ / „Allow microphone on the phone.“ | Startaktion |
| Wird vorbereitet | nach der Startaktion, bis Audio bereit ist; im Gespräch auch zwischen zwei Runden | „Audio wird vorbereitet…“ / „Preparing audio…“ | Beenden |
| Hört zu | das Mikrofon nimmt auf: Mikrofontest und Gespräch | „Hört zu“ / „Listening“ | Beenden |
| Antwort wird vorbereitet | Gespräch: die Anfrage liegt beim Anbieter | „Antwort wird vorbereitet.“ / „Preparing the reply.“ | Beenden |
| Spricht | die Ausgabe läuft: Testsatz, Rücklesen im Mikrofontest, Antwort im Gespräch | „Spricht“ / „Speaking“ | Beenden |
| Ergebnis | eine Sitzung ist zu Ende | „Beendet“, „Audiotest beendet.“, „Keine Sprache erkannt.“ oder „Fehler. Details am Telefon.“ / „Ended“, „Audio check ended.“, „No speech detected.“, „Error. Details on the phone.“ | Startaktion |

- **Ergebnis:** Es bleibt stehen, auch nach einer neuen Verbindung, bis die nächste Sitzung beginnt. Die Einzelheiten eines Fehlers stehen am Telefon, nicht im Auto. Heute speichert die App das Ergebnis als fertigen Text; nach einem Sprachwechsel steht es deshalb bis zur nächsten Sitzung in der alten Sprache. Nach O5 speichert sie künftig nur die Art des Ergebnisses und zeigt den Text immer in der aktuellen Sprache.
- **Verbindet sich der Audiodienst** nach einem Tipp auf die Startaktion noch, zeigt Zeile 1 ebenfalls „Audio wird vorbereitet…“, die Aktion bleibt die Startaktion, wie die App es heute macht.
- **Auswahl:** Ohne laufende Sitzung zeigt Zeile 2 die Auswahl vom Telefon und folgt ihr sofort. Während einer Sitzung zeigt Zeile 2 Anbieter und Modell dieser Sitzung.
- **Sprachwelle des Hosts:** Während das Automikrofon aufnimmt, zeigt Android Auto seine eigene Mikrofon-Sprachwelle. Sie gehört dem Host und verschwindet nach der Aufnahme. Während die Antwort gesprochen wird, bleiben „Spricht“ und „Beenden“ sichtbar. Das ist das erwartete Bild, im DHU bestätigt. Verlangt werden weder eine dauerhaft sichtbare Sprachwelle noch eine zusätzliche Aufnahme.
- **„Beenden“** stoppt in jedem Zustand Erkennung, Ausgabe und offene Anfragen; danach zeigt Zeile 1 „Beendet“, die Aktion wieder die Startaktion.
- Rückmeldung beim Drücken und den Fokusrahmen bei Dreh-Drück-Steller oder Touchpad zeichnet der Host. Die App ergänzt nichts.
- **Prüfpunkt, kein Designthema:** Laut Car App Library gilt ein `MessageTemplate` nur dann als Auffrischung, wenn Titel und Nachricht gleich bleiben. Jeder Wechsel von Zeile 1 oder 2 zählt damit gegen die Grenze von 5 Vorlagen pro Aufgabe, und eine Sitzung wechselt Zeile 1 mehrmals. Ob der Host deshalb eine Fehlermeldung zeigt oder die App schließt, prüft A7 im Vergleich mit einer Ausgangsmessung. Was die App dann tut, regelt O4.

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
- **Testmodi:** am Telefon unter „Audio und Gespräch einrichten“ gewählt. Für das Automikrofon braucht die App laut ihrer eigenen Meldung Car API 5, Android 13 oder neuer und eine installierte lokale Spracherkennung. Für das Gespräch ist ein API-Schlüssel des gewählten Anbieters gespeichert; ein Gespräch sendet erkannten Text an den Anbieter und verbraucht API-Guthaben. Getestet wird im Stand.
- **Ausgangsmessung:** Wo nach einer Regression gefragt wird (A7), läuft derselbe Ablauf zuerst mit dem Vergleichs-Build, also dem Stand vor der geprüften Änderung, in derselben Testumgebung. Fehler, die schon dort auftreten, sind bekannte Ausgangsfehler und keine neue Regression. Sie werden mit beiden Builds festgehalten.
- **Zurückgestellt:** Tests im Fahrzeug (ID.7), mit eingehendem Anruf und mit Headset gehören nicht zu dieser Abnahme.

### Kriterien

Jedes Kriterium gilt für jede Kombination aus Bildschirm, Modus (Tag, Nacht) und Sprache, sofern nicht anders angegeben. Für A1 bis A5, A8 und A9 genügt in jeder Kombination der Ausgabetest. A6, A7, A10 und A11 laufen mit allen drei Testmodi auf 800×480. Belege sind Screenshots und Logcat.

| Nr. | Kriterium | bestanden, wenn |
|---|---|---|
| A1 | Kopf | App-Symbol und Titel „Lautwerk“ sind vollständig sichtbar. |
| A2 | Aktion sichtbar | Der Button zeigt die Startaktion vollständig, in beiden Sprachen und in beiden Fassungen („Gespräch starten“, „Audiotest starten“ bzw. „Start conversation“, „Start audio check“), während einer Sitzung „Beenden“ bzw. „End“. |
| A3 | Akzent | Der Button-Hintergrund ist Gold oder Bronze aus B2, oder der Host hat ihn sichtbar durch eine eigene Farbe ersetzt, bei beiden Beschriftungen. Die tatsächlich dargestellte Farbe wird bei Tag und bei Nacht notiert. Eine Ersetzung durch den Host ist kein Fehler der App. |
| A4 | Lesbarkeit | Button-Text zu Button-Fläche mindestens 4,5:1, gemessen an den tatsächlich dargestellten Farben (siehe Farbmessung), bei Tag und Nacht, nie an den angeforderten Werten. Titel und Nachricht sind lesbar und nicht durch App-Farben verändert. |
| A5 | Vollständige Inhalte | Zustand, Anbieter und Modell sind vollständig sichtbar, auch bei 800×480 mit der längsten Kombination und dem längsten Zustandstext („Mikrofon am Telefon erlauben.“ bzw. „Allow microphone on the phone.“), in beiden Sprachen. Kürzt der Host etwas davon, ist A5 nicht bestanden und O3 wird fällig. |
| A6 | Aktion funktioniert | In jedem Testmodus startet ein Tipp (Touch) wie ein `dpad click` (Dreh-Drück-Steller) die Sitzung: Zeile 1 wechselt zu „Audio wird vorbereitet…“ und dann durch die Zustände des Modus (siehe Zustände), die Aktion heißt „Beenden“. Ausgabe- und Mikrofontest enden von selbst mit ihrem Ergebnis. „Beenden“ beendet jede Sitzung in jedem Zustand mit „Beendet“, auch während „Spricht“; danach zeigt der Button wieder die Startaktion des gewählten Modus. Logcat zeigt `car.voice.action=start` bzw. `car.voice.action=stop`, nach einem Start `voice.session.start mode=…` und die Phasen (`voice.check.phase=…` bzw. `voice.conversation.phase=…`) in derselben Reihenfolge wie Zeile 1. Weil die App vor jedem Start eine alte Sitzung beendet, kann vor „Audio wird vorbereitet…“ kurz „Beendet“ erscheinen; das ist heutiges Verhalten. |
| A7 | Wiederholte Sitzungen | Ablauf auf 800×480 mit Touch, ohne neue Verbindung: dreimal Ausgabetest bis zum Ergebnis, dreimal Mikrofontest mit je einem Satz bis zum Ergebnis, einmal ein Gespräch mit mindestens zwei Runden (Hört zu, Antwort wird vorbereitet, Spricht), dann „Beenden“. Je Sitzung notiert: Zahl der Vorlagen (`car.onGetTemplate` im Logcat), jede Fehlermeldung des Hosts, jedes Schließen der App, Zeile 1 am Ende. Derselbe Ablauf läuft zuerst als Ausgangsmessung (siehe Aufbau). Bestanden, wenn der geprüfte Build keine Fehlermeldung, kein Schließen und keinen hängenden Zustand zeigt, die in der Ausgangsmessung nicht auftraten. Bekannte Ausgangsfehler sind keine Regression; für sie gilt O4. |
| A8 | Fokus | Im Rotary-Modus ist der Button bei jeder Beschriftung erreichbar, und der Fokusrahmen des Hosts ist bei Tag und Nacht sichtbar. |
| A9 | Sprache | Alle Texte, auch die Zustände in Zeile 1, erscheinen in der Sprache des Smartphones, ohne Mischung; Englisch auch bei einer anderen Systemsprache. Ein gespeichertes Ergebnis aus der Zeit vor einem Sprachwechsel wird notiert; bis O5 umgesetzt ist, ist es ein bekannter Ausgangsbefund, keine Regression. |
| A10 | Auswahl live | Ohne laufende Sitzung erscheint eine Änderung von Anbieter oder Modell am Smartphone in Zeile 2, ohne dass das Auto neu verbunden wird. Während einer Sitzung zeigt Zeile 2 Anbieter und Modell dieser Sitzung. Ein Wechsel des Testmodus am Telefon ändert ohne neue Verbindung die Beschriftung der Startaktion. |
| A11 | Keine falschen Aussagen | Kein Text und kein Icon deutet eine Kategorie oder eine Zulassung an. Zeile 1 zeigt einen Zustand nur, solange er zutrifft: „Hört zu“ nur während der Aufnahme, „Spricht“ nur während der Ausgabe, nach dem Ende das Ergebnis. Die Mikrofon-Sprachwelle des Hosts erscheint während der Aufnahme und verschwindet danach; während „Spricht“ bleiben „Spricht“ und „Beenden“ sichtbar. Eine dauerhaft sichtbare Sprachwelle oder eine zusätzliche Aufnahme wird nicht verlangt. |

### Ergebnis der Abnahme 0.1.14 (12. Oktober 2026)

**Beobachtung dieser einen Testumgebung.** Die Werte sind keine Design-Tokens und keine allgemeingültigen Host-Farben. Ein anderer Host, eine andere Android-Auto-Version oder ein Fahrzeug kann anders darstellen. Der damalige Stand hatte im Auto noch den UI-Zähler; A2, A5, A6, A7, A10 und A11 sind seitdem an das Audio- und Gesprächsverhalten angepasst. Die Farbmessungen gelten unverändert als Beobachtung.

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

### Teiltest zu 0.1.18

- **Bestätigt im DHU:** Die Mikrofon-Sprachwelle des Hosts verschwindet nach der Aufnahme. Während die Antwort gesprochen wird (getestet mit Claude), bleiben „Spricht“ und „Beenden“ sichtbar. Beides ist das erwartete Bild (siehe Zustände und A11).
- Dieser Teiltest ersetzt keine vollständige Abnahme A1 bis A11. Insbesondere stehen die Ausgangsmessung und der Wiederholungstest nach A7 noch aus.
- Host-Farben und Hintergrund wurden nicht neu gemessen. Es gelten weiter die Beobachtungen aus 0.1.14; sie sind keine zugesicherten App-Farben.
- Zurückgestellt: Tests im ID.7, mit Anruf und mit Headset.

## Entscheidungen

- **O1 App-Symbol, entschieden am 12. Oktober 2026:** Die App bekommt das Symbol „Sprechblase mit Punkt“ im Azentrius-Stil. Es ersetzt das türkis-blaue „A“ auf dem Smartphone und im Auto.
- **O2 Akzent nicht lesbar:** Zeigt A4 Weiß auf Gold oder Bronze als Textfarbe auf dem dunklen Grund, wird B3 auf `CarColor.DEFAULT` zurückgenommen. Alternativ werden andere vorhandene Werte gesucht. Erfunden wird kein neuer Wert. In der Abnahme 0.1.14 nicht eingetreten.
- **O3 Gekürzte Nachricht:** Kürzt der Host Anbieter oder Modell, braucht es eine Entscheidung über kürzere Anzeigenamen im Modellkatalog; kürzt er einen Zustandstext in Zeile 1, eine über kürzere Zustandstexte. Beides ist eine Änderung an Inhalten, nicht am Design.
- **O4 Vorlagengrenze, entschieden am 14. Oktober 2026:** in drei Stufen.
  1. Zuerst läuft A7 mit Ausgangsmessung auf 0.1.18. Zeigt der Host dabei keine Fehlermeldung und schließt er die App nicht, ändert sich nichts.
  2. Bricht der Host ab oder schließt er die App, zeichnet die App weniger Zwischenstände: Das kurze „Beendet“ vor jedem Start entfällt; es entsteht nur, weil die App vor dem Start eine alte Sitzung beendet. Zustände, die kürzer als 0,3 s dauern, werden nicht einzeln gezeichnet; die Vorlage zeigt immer den aktuellen Zustand, spätestens nach 0,3 s. Texte, Reihenfolge, MessageTemplate und B1 bis B5 bleiben. Die Mikrofon-Sprachwelle des Hosts zeigt eine Aufnahme ohnehin sofort.
  3. Reicht das nicht, ist eine andere Vorlage eine neue Entscheidung, weil sie B1 ändert.

  Ein Fehler, der mit dem geprüften Build auftritt, aber nicht in der Ausgangsmessung, ist eine Regression dieses Builds.
- **O5 Gespeichertes Ergebnis nach Sprachwechsel, entschieden am 14. Oktober 2026:** Die App speichert statt des fertigen Textes nur die Art des Ergebnisses: beendet, Audiotest beendet, keine Sprache erkannt oder Fehler. Den Text setzt sie erst beim Anzeigen ein, in der aktuellen Sprache.
  - Im Auto zeigt Zeile 1 damit immer die Sprache des Smartphones („Beendet“, „Audiotest beendet.“, „Keine Sprache erkannt.“ bzw. „Fehler. Details am Telefon.“).
  - Am Telefon zeigt die Statuszeile der Einrichtungsseite die genaue Fehlermeldung, solange die Sprache gleich ist. Nach einem Sprachwechsel steht dort für einen früheren Fehler der allgemeine Text „Die letzte Sitzung endete mit einem Fehler.“ / „The last session ended with an error.“ (neu), weil Meldungen der Anbieter schon übersetzt gespeichert sind.
  - Bis zur Umsetzung bleibt der heutige Befund in A9 ein bekannter Ausgangsbefund.

## Für den Umstellungsplan

| Vorgabe | betroffene Stelle im App-Repo |
|---|---|
| B2 | `prototype-app/src/dhu/AndroidManifest.xml` (Metadaten) und eine neue Stil-Ressource für `CarAppTheme` im DHU-Flavor |
| B3 | `car-ui/src/main/java/com/azentrius/lautwerk/car/LautwerkScreen.kt`, `Action.Builder` der einen Aktion |
| O1 App-Symbol | Manifest von `prototype-app` (heute `@drawable/ic_openauto`) und `app/src/main/AndroidManifest.xml` (heute ohne Symbol): `android:icon="@mipmap/ic_launcher"`, `android:roundIcon="@mipmap/ic_launcher_round"`; die Dateien aus `android/apps/lautwerk/res/` kommen in das jeweilige Modul |
| B1, B4, B5 | `LautwerkScreen.kt`, `car-ui/src/main/res/values*/strings.xml` und `voice_strings.xml`: bleiben inhaltlich unverändert, außer nach V3. Der Zustand in Zeile 1 kommt aus `car-ui/…/voice/CarVoiceService.kt` und `VoiceConnection.kt`. |
| V3 | `LautwerkScreen.kt`: Beschriftung der Startaktion nach `VoicePreferences.mode`, neu gezeichnet, wenn sich der Modus ändert; `car_start` wird durch zwei Texte ersetzt; die Texte aus „Texte, die die Startaktion nennen“ in `voice_strings.xml`; `prototype-app/src/main/res/values*/strings.xml` und `prototype-app/src/dhu/res/values*/voice_note.xml` (siehe „Android“, Telefonseite); `CarTemplateTest.kt` erwartet die neuen Beschriftungen. |
| A1–A11 | manuelle DHU-Abnahme. `prototype-app/src/androidTestDhu/…/CarTemplateTest.kt` prüft Teile von A2, A6, A9 und A10 mit einer Audio-Attrappe: Startaktion und „Beenden“ behalten ihre Absicht, englische und deutsche Texte, Übernahme der Auswahl. Er kann B3 ergänzend prüfen (`CarColor.PRIMARY` als Hintergrund der Aktion). Farben, Kürzungen, die Sprachwelle und die Vorlagengrenze zeigt nur der DHU. |

## Quellen

- Android for Cars, Customize your app: <https://developers.google.com/cars/design/create-apps/apps-for-drivers/customize-app>. Die Seite verweist darauf, dass Aktualisierungen seit dem 11. März 2026 unter docs.partner.android.com/drivingux erscheinen.
- Message template: <https://developers.google.com/cars/design/create-apps/apps-for-drivers/templates/message-template>
- Button: <https://developers.google.com/cars/design/create-apps/apps-for-drivers/components/button>
- Color: <https://developers.google.com/cars/design/android-auto/design-system/color>
- Desktop Head Unit: <https://developer.android.com/training/cars/testing/dhu>
- Designabgleich der App zu 0.1.18 mit den offenen Punkten an dieses System: <https://github.com/Azentrius/openauto-ai/blob/21e393079a6e26bea8707983638242daccdada24/docs/DESIGN_SYSTEM.md>
- Audio und Gespräch im Auto: <https://github.com/Azentrius/openauto-ai/pull/27>
- Abnahme 0.1.14, Umsetzung und vollständiger Testbericht: <https://github.com/Azentrius/openauto-ai/pull/14>, dort `docs/BUILD_AND_TEST.md` und die Messwerte unter `docs/evidence/0.1.14/`
- Car App Library, Quelltext-Dokumentation zu `MessageTemplate`, `Action`, `CarColor`, `CarIcon`, `CarText`, `Header`, `Screen` und den `constraints`: <https://github.com/androidx/androidx/tree/androidx-main/car/app/app/src/main/java/androidx/car/app>

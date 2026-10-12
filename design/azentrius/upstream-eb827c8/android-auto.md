# Android Auto

So wird das Azentrius-Designsystem auf den OpenAuto-AI-Prototyp im Desktop Head Unit Emulator (DHU) übertragen. Die Regeln im Abschnitt „Android“ gelten weiter für das Smartphone; hier gelten sie nicht.

**Stand des Prototyps.** Der DHU-Build ist ein lokales Experiment. Die Kategorie, die im DHU-Manifest eingetragen ist, ist dort selbst als reine Debug-Sonde gekennzeichnet. Dieses Designsystem behauptet weder eine offiziell unterstützte Kategorie für allgemeine KI-Gespräche noch eine Zulassung für die Veröffentlichung oder den Einsatz im Fahrzeug. „Starten“ ist ein UI-Test und startet kein Gespräch.

Grundlage ist der Code im Branch `feature/azentrius-design` von `Azentrius/openauto-ai` (Car App Library 1.8.0-rc01, `minCarApiLevel` 1).

## Grundsatz

- **Das Design ändert kein Verhalten.** Es legt fest, wie die vorhandene Oberfläche aussieht, nicht, was sie kann.
- **Android Auto zeichnet die Oberfläche.** Die App liefert Inhalte und wenige Farbwünsche an eine Vorlage der Car App Library; Schriftfamilie, Schriftgrößen, Formen, Abstände, Hintergrund und Grundlayout bestimmt der Host.
- **Das Smartphone-Design wird nicht übertragen.** DM Sans, die Textstile, Radien, Abstände, Flächenfarben, Reiter, die untere Leiste und die Status-Labels aus „Android“ kommen im Auto nicht vor. Übernommen werden nur zwei Farbwerte (siehe Farben) und die Sprache der Texte.

## Gestaltungsspielraum

Der Prototyp verwendet ein `MessageTemplate` mit Titel, App-Symbol im Kopf, einer Nachricht und einer Aktion. Für genau diese Vorlage gilt:

| Eigenschaft | wer bestimmt | Spielraum der App im MessageTemplate |
|---|---|---|
| Schriftfamilie und Schriftgrößen | Host | keiner |
| Layout, Abstände, Formen, Positionen | Host | keiner |
| Hintergrund | Host: Schwarz, bei Tag und Nacht | keiner |
| Textfarbe von Titel und Nachricht | Host | keiner. Farb-Spans werden in der Nachricht nicht unterstützt und ignoriert; der Titel erlaubt nur Distanz-, Dauer- und Timer-Spans. |
| Zeilen und Kürzung der Nachricht | Host | keiner. Die Vorlage sieht bis zu 2 Zeilen umbrechenden Text vor. |
| Kopfbereich | Host zeichnet | App wählt `Action.APP_ICON` oder `Action.BACK`; `APP_ICON` zeigt das Launcher-Symbol der App und ist nicht bedienbar. |
| Titel | App | Text. Die Darstellung bestimmt der Host. |
| Nachricht | App | Text. Die Darstellung bestimmt der Host. |
| Aktionen im Inhalt | App | bis zu 2, davon eine als primär markierbar (`FLAG_PRIMARY`, ab Car API 4) |
| Button-Form, -Größe, -Position, Rückmeldung beim Drücken und Fokus | Host | keiner |
| Button-Hintergrund | App wünscht, Host entscheidet | `Action.Builder.setBackgroundColor(CarColor)`; der Host darf die Farbe ignorieren oder ersetzen |
| Button-Beschriftungsfarbe | App wünscht, Host entscheidet | nur über `ForegroundCarColorSpan` im Titel der Aktion |
| Akzentfarben der App | App | `carColorPrimary` und `carColorPrimaryDark`, `carColorSecondary` und `carColorSecondaryDark` über die Metadaten `androidx.car.app.theme`; der Host darf bei zu wenig Kontrast Standardfarben verwenden |
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
- **B5 Keine Smartphone-Werte:** Außer den zwei Farbwerten aus B2 werden keine Azentrius-Tokens an den Host übergeben, auch keine Schrift und keine Icons.

## Farben

### Zuordnung

Android Auto stellt alle Inhalte auf schwarzen Grund, bei Tag wie bei Nacht. Die passenden Werte kommen deshalb aus dem Thema „Azentrius dunkel“ und aus der Markenfarbe.

| Android Auto | Wert | Azentrius-Token | Rolle laut Car App Library |
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
- Ob der Host im Nachtmodus andere Varianten wählt, ist nicht dokumentiert. Das klärt die Abnahme (A3).
- Der DHU unterscheidet einen Tagmodus („hohe Helligkeit, volle Farbe“) und einen Nachtmodus („geringe Helligkeit, hoher Kontrast“). Beide zeichnet der Host.

### Wo der Host vorgibt oder überschreibt

- Hintergrund, Titel- und Nachrichtenfarbe gibt der Host vor. Er zeichnet weißen Text mit 88 %, 72 % oder 56 % Deckkraft, nachts mit 96 %.
- Akzentfarben aus dem Thema darf der Host durch eine Standardfarbe ersetzen, wenn sie den Kontrast nicht erfüllen.
- Den Button-Hintergrund darf der Host ignorieren oder das Fahrzeugsystem ersetzen.
- Ohne App-Farben verwendet Android Auto seinen eigenen blauen Akzent („car accent“).

### Kontrast

Android Auto verlangt 4,5:1 zwischen Vordergrund (Text, Icons) und Hintergrund. Geprüft sind alle Kombinationen, die mit B2 und B3 entstehen können:

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

Die beiden nicht erfüllten Kombinationen entstehen nur, wenn der Host die Varianten gegen ihre dokumentierte Rolle einsetzt. Ob das passiert, prüft die Abnahme (A4). Tritt eine davon auf, ist die Abnahme nicht bestanden, und die Entscheidung O2 wird fällig.

## Icons

Belegt wird nur, was vorhanden ist und eine Position hat, die der Prototyp schon nutzt.

| Position | Belegung | Begründung |
|---|---|---|
| Kopf, `Action.APP_ICON` | das bestehende App-Symbol `ic_openauto` aus dem App-Repo, unverändert | Der Host zeichnet das Launcher-Symbol der App. |
| Nachrichtenbereich (`setIcon`) | keines | Die Azentrius-Icons für Gesprächszustände (`microphone-slash`, `microphone` und andere) würden ein echtes Gespräch behaupten. Für „UI-Test bereit“ gibt es kein passendes vorhandenes Icon. |
| Startaktion | keines | `microphone` steht im System für ein offenes Mikrofon, `play` ist der Hörprobe vorbehalten. Keines passt zu einem UI-Test. |
| Aktionsleiste, Kopfaktionen am Ende | nicht verwendet | Diese Positionen nutzt der Prototyp nicht (B1). |

**Lücken:**

- Das App-Symbol folgt nicht dem Azentrius-Stil. Es ist ein türkises „A“ (`#63DDC1`) auf Dunkelblau (`#123040`). Ein Azentrius-Symbol für die App gibt es nicht; es wird hier auch nicht erfunden (O1).
- Für „UI-Test starten“ gibt es kein Icon im System.

**Für eine spätere Nutzung von Azentrius-Icons im Auto:** Die Dateien `ic_az_*.xml` sind schwarz gefüllt. Im Auto brauchen sie immer eine Tönung über `CarIcon.Builder.setTint(CarColor)`, sonst erscheint Schwarz auf Schwarz. Eine Tönung über `IconCompat` allein wendet der Host nicht sicher an.

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

### Kriterien

Jedes Kriterium gilt für jede Kombination aus Bildschirm, Modus und Sprache, sofern nicht anders angegeben. Belege sind Screenshots und Logcat.

| Nr. | Kriterium | bestanden, wenn |
|---|---|---|
| A1 | Kopf | App-Symbol und Titel „OpenAuto AI“ sind vollständig sichtbar. |
| A2 | Startaktion sichtbar | Der Button zeigt „Starten“ bzw. „Start“ vollständig. |
| A3 | Akzent | Der Button-Hintergrund ist Gold oder Bronze aus B2, oder der Host hat ihn sichtbar durch seine Standardfarbe ersetzt. Welche Variante bei Tag und bei Nacht erscheint, wird notiert. |
| A4 | Lesbarkeit | Button-Text zu Button-Fläche mindestens 4,5:1, gemessen per Pipette im Screenshot, bei Tag und Nacht. Titel und Nachricht sind lesbar und nicht durch App-Farben verändert. |
| A5 | Vollständige Inhalte | Status, Anbieter und Modell sind vollständig sichtbar, auch bei 800×480 mit der längsten Kombination, in beiden Sprachen. Kürzt der Host etwas davon, ist A5 nicht bestanden und O3 wird fällig. |
| A6 | Startaktion funktioniert | Ein Tipp (Touch) und `dpad click` (Dreh-Drück-Steller) wechseln von „Bereit …“ zu „Gestartet · Test 1“, jeder weitere zu Test 2, 3 …. Logcat zeigt `car.start count=n` mit demselben n. |
| A7 | Viele Starts | 6 Tipps hintereinander auf 800×480: Der Zähler steigt bis 6, ohne Fehlermeldung des Hosts und ohne dass die App geschlossen wird. Das Ergebnis wird festgehalten, auch wenn es fehlschlägt. |
| A8 | Fokus | Im Rotary-Modus ist der Button erreichbar, und der Fokusrahmen des Hosts ist bei Tag und Nacht sichtbar. |
| A9 | Sprache | Alle Texte erscheinen in der Sprache des Smartphones, ohne Mischung; Englisch auch bei einer anderen Systemsprache. |
| A10 | Auswahl live | Eine Änderung von Anbieter oder Modell am Smartphone erscheint in Zeile 2, ohne dass das Auto neu verbunden wird. |
| A11 | Keine falschen Aussagen | Kein Text und kein Icon deutet ein laufendes Gespräch, eine Kategorie oder eine Zulassung an. |

## Offene Entscheidungen

- **O1 App-Symbol:** Bleibt das türkis-blaue „A“, oder bekommt die App ein Symbol im Azentrius-Stil? Ein neues Symbol entsteht nur auf ausdrückliche Entscheidung. Bis dahin gilt das bestehende.
- **O2 Akzent nicht lesbar:** Zeigt A4 Weiß auf Gold oder Bronze als Textfarbe auf Schwarz, wird B3 auf `CarColor.DEFAULT` zurückgenommen. Alternativ werden andere vorhandene Werte gesucht. Erfunden wird kein neuer Wert.
- **O3 Gekürzte Zeile 2:** Kürzt der Host Anbieter oder Modell, braucht es eine Entscheidung über kürzere Anzeigenamen im Modellkatalog. Das ist eine Änderung an Inhalten, nicht am Design.
- **O4 Vorlagengrenze:** Tritt bei A7 der Fehler auf, entscheidet die App-Seite, ob „Starten“ anders umgesetzt wird. Das Design bleibt davon unberührt.

## Für den Umstellungsplan

| Vorgabe | betroffene Stelle im App-Repo |
|---|---|
| B2 | `prototype-app/src/dhu/AndroidManifest.xml` (Metadaten) und eine neue Stil-Ressource für `CarAppTheme` im DHU-Flavor |
| B3 | `car-ui/src/main/java/dev/openautoai/car/OpenAutoScreen.kt`, `Action.Builder` der Startaktion |
| B1, B4, B5 | `OpenAutoScreen.kt` und `car-ui/src/main/res/values*/strings.xml`: bleiben inhaltlich unverändert |
| A1–A11 | manuelle DHU-Abnahme. `prototype-app/src/androidTestDhu/…/CarTemplateTest.kt` prüft Teile von A6, A9 und A10 schon heute (englische Texte, Zähler, Übernahme der Auswahl) und kann B3 ergänzend prüfen (`CarColor.PRIMARY` als Hintergrund der Aktion). Farben und Kürzungen zeigt nur der DHU. |

## Quellen

- Android for Cars, Customize your app: <https://developers.google.com/cars/design/create-apps/apps-for-drivers/customize-app>. Die Seite verweist darauf, dass Aktualisierungen seit dem 11. März 2026 unter docs.partner.android.com/drivingux erscheinen.
- Message template: <https://developers.google.com/cars/design/create-apps/apps-for-drivers/templates/message-template>
- Button: <https://developers.google.com/cars/design/create-apps/apps-for-drivers/components/button>
- Color: <https://developers.google.com/cars/design/android-auto/design-system/color>
- Desktop Head Unit: <https://developer.android.com/training/cars/testing/dhu>
- Car App Library, Quelltext-Dokumentation zu `MessageTemplate`, `Action`, `CarColor`, `CarIcon`, `CarText`, `Header`, `Screen` und den `constraints`: <https://github.com/androidx/androidx/tree/androidx-main/car/app/app/src/main/java/androidx/car/app>

# App-Icons

Die Zeichen der eigenen Apps: Gold `brand-gold` auf Tinte `brand-ink`, jedes mit dem Punkt der Wortmarke. Die Dateien hier sind Vorlagen zur Ansicht und für andere Plattformen. Die einsetzbaren Android-Ressourcen liegen unter `android/apps/<App>/res/`; Aufbau und Regeln stehen im Abschnitt „Android“ unter „App-Symbol, Startbild und Benachrichtigung“.

| Datei | Inhalt |
|---|---|
| `openauto-ai.svg` | OpenAuto AI, Sprechblase mit Punkt: so, wie das Symbol auf dem Startbildschirm erscheint (die inneren 72 dp, ohne Maske) |
| `openauto-ai-foreground.svg` | Vordergrund-Ebene im 108-dp-Raster, Gold auf transparentem Grund |
| `openauto-ai-monochrome.svg` | Monochrom-Ebene für Themensymbole |
| `openauto-ai-notification.svg` | Benachrichtigungssymbol, 24 dp; hier zur Ansicht in `#151515`, in der App weiß |
| `openauto-ai-play-512.png` | Play-Store-Symbol, 512 × 512 px, nur für eine Veröffentlichung |

Alle Dateien erzeugt `scripts/app-icon-openauto.py` im Repo. Keine davon wird von Hand geändert.

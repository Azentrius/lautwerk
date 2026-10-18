# App-Icons

Die Zeichen der eigenen Apps: Gold `brand-gold` auf Tinte `brand-ink`, jedes mit dem Punkt der Wortmarke. Die Dateien hier sind Vorlagen zur Ansicht und für andere Plattformen. Die einsetzbaren Android-Ressourcen liegen unter `android/apps/<App>/res/`; Aufbau und Regeln stehen im Abschnitt „Android“ unter „App-Symbol, Startbild und Benachrichtigung“. Sie gelten für Web-Apps genauso: 108-dp-Raster, Sicherheitszone, Punkt, Aussparungen als echte Löcher.

| Datei | Inhalt |
|---|---|
| `lautwerk.svg` | Lautwerk, Sprechblase mit Punkt: so, wie das Symbol auf dem Startbildschirm erscheint (die inneren 72 dp, ohne Maske) |
| `lautwerk-foreground.svg` | Vordergrund-Ebene im 108-dp-Raster, Gold auf transparentem Grund |
| `lautwerk-monochrome.svg` | Monochrom-Ebene für Themensymbole |
| `lautwerk-notification.svg` | Benachrichtigungssymbol, 24 dp; hier zur Ansicht in `#151515`, in der App weiß |
| `lautwerk-play-512.png` | Play-Store-Symbol, 512 × 512 px, nur für eine Veröffentlichung |
| `stellwerk.svg` | Stellwerk, Formsignal mit Punkt: die inneren 72 dp, ohne Maske |
| `stellwerk-favicon.svg` | dasselbe auf einer Tintenfläche mit runden Ecken wie beim Zeichen „A.“, 32 × 32: das Favicon von Stellwerk (`ui/icon.svg`) |

## Stellwerk: Formsignal mit Punkt

- **Zeichen:** ein Formsignal auf Fahrt. Der Mast steht auf einem Fuß und ragt über den Flügel hinaus, der Flügel zeigt 45° nach rechts oben und endet in einer runden Scheibe mit Loch. Rechts neben dem Fuß steht auf der Grundlinie der Punkt, so groß wie bei Lautwerk.
- **Maße:** 32,8 × 51,8 dp im 108-dp-Raster, Halbdiagonale 30,6 dp, also innerhalb der Sicherheitszone (33 dp).
- Es löst das bisherige Favicon ab, eine goldene Raute an grauem Mast ohne Punkt.

Die Dateien von Lautwerk erzeugt `scripts/app-icon-lautwerk.py` im Repo, die von Stellwerk `scripts/app-icon-stellwerk.py`. Keine davon wird von Hand geändert.

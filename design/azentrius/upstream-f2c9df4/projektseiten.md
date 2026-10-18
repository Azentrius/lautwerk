# Projektseiten

Projektseiten stellen Azentrius und seine Projekte vor: azentrius.de mit Impressum und Datenschutz und das Profil der GitHub-Organisation. Sie sind keine Bedienoberfläche. Darum dürfen sie größer und markenbetonter auftreten; die Ausnahmen stehen hier. Alles, was hier nicht steht, gilt unverändert: Farben und Themen, Statussprache, Fokusring, 44 px unter 640 px, keine Randstreifen, keine Verläufe.

## Themen

„Azentrius dunkel“ und „Azentrius hell“. azentrius.de folgt der Einstellung des Geräts. Das GitHub-Profil folgt der von GitHub: jede Grafik gibt es dort hell und dunkel, eingebunden über `<picture>` mit `prefers-color-scheme`.

## Schrift

Die Schriftgruppe „Projektseite“ in `tokens.json`, mit den Werten von azentrius.de:

| Stil | Wert | wofür |
|---|---|---|
| `page-hero` | DM Serif Display 96 px, fließend ab 48 px | die eine Hero-Überschrift, Schlusspunkt in `signature` |
| `page-section` | DM Serif Display 52 px, fließend ab 36 px | Abschnittstitel |
| `page-eyebrow` | DM Sans 13 px, 500, Laufweite 0,16 em, Versalien | kleine Zeile über einem Abschnittstitel |
| `page-lead` | DM Sans 19 px, unter 640 px 17 px | Einleitung unter dem Hero, in `text-subtle` |
| `page-body` | DM Sans 17 px | Fließtext |
| `page-fact` | DM Serif Display 44 px, unter 640 px 32 px | Kennzahl in der Leiste unter dem Hero |
| `page-card-title` | DM Serif Display 40 px | Titel einer Projektkarte |
| `page-wordmark` | DM Serif Display 26 px, im Fuß 22 px | Wortmarke im Kopf |

## Eyebrows

Die kleine Zeile über einem Abschnittstitel steht in Versalien (`az-eyebrow`): 13 px, Gewicht 500, Laufweite 0,16 em, in `signature`. Sie ist die einzige Ausnahme von „keine Großbuchstaben-Überschriften“ und gilt nur auf Projektseiten und Covern. Überschriften selbst, auch die Abschnitte von Impressum und Datenschutz, stehen in normaler Schreibung.

## Gold als Markenakzent

Auf Projektseiten darf `signature` mehr als in Bedienoberflächen, aber immer als Marke, nie als Status:

- in Eyebrows,
- als Aufzählungspunkt (6 px, rund) vor Funktionslisten,
- als Schlusspunkt der Hero-Überschrift,
- als Punkt eines Produktnamens ohne Zusatz und im kursiven Namenszusatz (Grundlagen, Marke).

Ein Reifegrad wie „Prototyp“ ist kein Akzent, sondern ein neutrales `az-label`.

## Abstände

Innerhalb von Karten, Listen und Kopf gilt die Skala von 4 bis 48. Für die großen Abschnitte gibt es vier eigene Abstände:

| Token | Wert | wofür |
|---|---|---|
| `space-section` | 112 px, unter 640 px 72 px | oben und unten in jedem Abschnitt und über dem Hero (unter 640 px 56 px) |
| `space-hero-end` | 88 px, unter 640 px 64 px | unter dem Hero bis zur Kennzahlenleiste |
| `space-columns` | 80 px | Spaltenabstand zweispaltiger Abschnitte, beim Umbruch 56 px zwischen den Zeilen |
| `space-cards` | 40 px | zwischen Projektkarten |

Der Inhalt ist höchstens 1120 px breit, mit `space-lg` Rand, unter 640 px 20 px.

## Projektkarten

Wie `Card`, aber größer: `bg-card` mit `line`-Rahmen und `radius-large`, innen `space-2xl`, auf schmalen Bildschirmen bis `space-lg`. In der Karte von oben: Titel in `page-card-title` mit dem gestalteten Produktnamen, Beschreibung in `text-subtle`, Technik als neutrale `az-label`, Funktionen mit goldenem Aufzählungspunkt über einer `line`-Linie, ein Link zum Repository.

## Buttons

Haupt-Links (CTAs) im Hero sind große Pillen, `az-btn--lg` (48 px hoch): einer `az-btn--primary`, die übrigen `az-btn--secondary`. Sie dürfen ohne Icon stehen; die Beschriftung bleibt Verb plus Objekt („Projekte entdecken“, „Repository öffnen“). Unter 640 px gilt wie überall 44 px Mindesthöhe, auch für die Links in den Projektkarten.

## Wortmarke und Fuß

- azentrius.de: die Wortmarke im Kopf, in `page-wordmark` als Link zur Startseite, und im Fuß in 22 px. Der Fuß trägt statt Version und Host die Links zu Impressum, Datenschutz und Kontakt.
- GitHub-Profil und andere Seiten mit Banner: Das Banner trägt die Wortmarke oben; sie zählt als die eine Wortmarke der Seite.
- Das Zeichen „A.“ ist Favicon und Bild der GitHub-Organisation (Gruppe „Marke“), nie Ersatz für die Wortmarke auf der Seite selbst.

## Grafiken im GitHub-Profil

GitHub zeigt SVG-Dateien als Bilder, und Bilder laden keine Webfonts. Text in Banner, Karten und Überschriften steht darum als Pfad, erzeugt aus den Schriftdateien dieses Systems (Repo `Azentrius/.github`, `scripts/render_profile.py`). Jede Grafik hat einen Alternativtext, der ihren Inhalt sagt.

#!/usr/bin/env python3
"""Erzeugt das App-Zeichen „Formsignal mit Punkt“ für Stellwerk.

Aufruf aus dem Wurzelverzeichnis des Repos:  python3 scripts/app-icon-stellwerk.py
Schreibt die Vorlagen nach assets/App-Icons/.

Aufbau wie bei Lautwerk im 108-dp-Raster: Das Zeichen wird in dp gezeichnet und mittig gesetzt; seine
Halbdiagonale bleibt unter 33 dp, damit liegt es in der Sicherheitszone (Kreis mit 66 dp Durchmesser).
Der Punkt hat Größe und Abstand wie bei Lautwerk und steht rechts neben dem Fuß auf der Grundlinie.
Alles ist EIN Pfad mit Füllregel evenOdd: Mast, Flügel und Fuß sind ein Umriss, das Loch in der Scheibe
ist eine echte Aussparung.
"""
import math
import os

INK, GOLD = "#0A0A0F", "#C8A96E"          # brand-ink, brand-gold
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "assets", "App-Icons")


# ---------- Entwurf in dp, Drehpunkt des Flügels im Ursprung ----------
MAST = (7, 17, 27)                        # Breite, Länge über dem Drehpunkt, Länge bis zur Grundlinie
WING = (7, 23, 8.5, 3.5)                  # Breite, Länge bis zur Mitte der Scheibe, Radius der Scheibe, Loch
FOOT = (16, 4)                            # Breite, Höhe; die Enden sind rund
DOT_GAP, DOT_R = 1.648, 4.12              # wie bei Lautwerk

R2 = math.sqrt(0.5)
DIR = (R2, -R2)                           # Flügel auf Fahrt: 45° nach rechts oben
UP, DOWN = (-R2, -R2), (R2, R2)           # Normalen zur oberen und unteren Flügelkante


def geometry():
    mw, above, below = MAST
    ww, length, disc, hole = WING
    fw, fh = FOOT
    h = mw / 2

    def edge(n, t):                       # Punkt auf einer Flügelkante, t entlang des Flügels
        return (n[0] * ww / 2 + t * DIR[0], n[1] * ww / 2 + t * DIR[1])

    def at_mast(n):                       # wo die Flügelkante die rechte Mastkante schneidet
        return edge(n, (h - n[0] * ww / 2) / DIR[0])

    meet = length - math.sqrt(disc ** 2 - (ww / 2) ** 2)    # wo die Flügelkanten auf die Scheibe treffen
    g = dict(h=h, top=-above, base=below, foot=(fw / 2, fh), disc=disc, hole=hole,
             c=(length * DIR[0], length * DIR[1]),
             top_join=at_mast(UP), low_join=at_mast(DOWN), wing_up=edge(UP, meet), wing_down=edge(DOWN, meet),
             dot=(fw / 2 + DOT_GAP + DOT_R, below - DOT_R))
    assert g["top_join"][1] > g["top"] + h, "der Flügel trifft die Rundung der Mastspitze"
    assert g["low_join"][1] < below - fh, "der Flügel reicht bis zum Fuß"
    return g


def bbox(g):
    (cx, cy), r = g["c"], g["disc"]
    return -g["foot"][0], min(g["top"], cy - r), max(cx + r, g["dot"][0] + DOT_R), g["base"]


def shape(g, T):
    """Pfad in Zielkoordinaten. T verschiebt einen Entwurfspunkt."""
    def p(x, y):
        X, Y = T(x, y)
        return f"{X:.3f},{Y:.3f}"

    def arc(r, x, y, large=0, sweep=1):
        return f"A{r:.3f},{r:.3f} 0 {large} {sweep} {p(x, y)}"

    def circle(cx, cy, r):
        return f"M{p(cx - r, cy)}{arc(r, cx + r, cy, 1, 0)}{arc(r, cx - r, cy, 1, 0)}Z"

    h, top, base = g["h"], g["top"], g["base"]
    f, fh = g["foot"]
    r = fh / 2
    d = (f"M{p(-h, top + h)}{arc(h, h, top + h)}"                        # runde Mastspitze
         f"L{p(*g['top_join'])}L{p(*g['wing_up'])}"                     # Oberkante des Flügels
         f"{arc(g['disc'], *g['wing_down'], large=1)}"                  # Scheibe am Flügelende
         f"L{p(*g['low_join'])}L{p(h, base - fh)}"                      # Unterkante des Flügels, Mast
         f"L{p(f - r, base - fh)}{arc(r, f - r, base)}"                 # Fuß
         f"L{p(-f + r, base)}{arc(r, -f + r, base - fh)}L{p(-h, base - fh)}Z")
    d += circle(*g["c"], g["hole"])                                      # Loch in der Scheibe
    d += circle(*g["dot"], DOT_R)                                        # Punkt der Wortmarke
    return d


G = geometry()
x0, y0, x1, y1 = bbox(G)
W, H = x1 - x0, y1 - y0
ox, oy = 54 - (x0 + W / 2), 54 - (y0 + H / 2)
D108 = shape(G, lambda x, y: (x + ox, y + oy))
assert math.hypot(W, H) / 2 < 33, "das Zeichen ragt aus der Sicherheitszone"


def svg(size, rx=0):
    """Die inneren 72 dp, so wie das Zeichen erscheint; rx rundet die Tintenfläche."""
    corner = f' rx="{rx:g}"' if rx else ""
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="18 18 72 72" width="{size}" height="{size}">'
            f'<rect x="18" y="18" width="72" height="72"{corner} fill="{INK}"/>'
            f'<path d="{D108}" fill="{GOLD}" fill-rule="evenodd"/></svg>\n')


def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(text)


if __name__ == "__main__":
    write(f"{ASSETS}/stellwerk.svg", svg(512))
    write(f"{ASSETS}/stellwerk-favicon.svg", svg(32, rx=72 * 7 / 32))     # Ecken wie beim Zeichen „A.“
    print(f"Zeichen {W:.2f} × {H:.2f} dp, Halbdiagonale {math.hypot(W, H) / 2:.2f} dp")

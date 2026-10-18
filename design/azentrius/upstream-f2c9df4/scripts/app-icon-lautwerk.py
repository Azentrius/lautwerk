#!/usr/bin/env python3
"""Erzeugt das App-Symbol „Sprechblase mit Punkt“ für Lautwerk.

Aufruf aus dem Wurzelverzeichnis des Repos:  python3 scripts/app-icon-lautwerk.py
Schreibt die Android-Ressourcen nach android/apps/lautwerk/res/ und die Vorlagen nach assets/App-Icons/.
Das Play-Store-Bild (PNG) entsteht nur, wenn cairosvg installiert ist.

Aufbau im 108-dp-Raster eines adaptiven Icons: Die Form wird in einem Entwurfsraster gezeichnet,
auf eine Halbdiagonale von 30,5 dp skaliert und mittig gesetzt. Damit liegt sie in der Sicherheitszone
(Kreis mit 66 dp Durchmesser). Alles ist EIN Pfad mit Füllregel evenOdd: Die Sprachwelle ist eine echte
Aussparung, auch in der einfarbigen Ebene und im Benachrichtigungssymbol.
"""
import math
import os

INK, GOLD = "#0A0A0F", "#C8A96E"          # brand-ink, brand-gold
HALF_DIAG = 30.5                          # dp, Sicherheitszone: 33
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "android", "apps", "lautwerk", "res")
ASSETS = os.path.join(ROOT, "assets", "App-Icons")


# ---------- Entwurf (Rastereinheiten vor der Skalierung) ----------
BODY = (30, 32, 46, 34, 12)               # x, y, Breite, Höhe, Eckradius der Blase
TAIL = ((52, 66), (37, 78))               # Schwanz: Ansatz rechts an der Unterkante, Spitze
BARS = (4.5, 8, (9, 19, 13, 7))           # Balkenbreite, Abstand der Mitten, Höhen
DOT_GAP, DOT_R = 2, 5                     # Punkt rechts neben der Blase, auf ihrer Grundlinie


def shape(T, S):
    """Pfad in Zielkoordinaten. T bildet einen Entwurfspunkt ab, S eine Länge."""
    def p(x, y):
        X, Y = T(x, y)
        return f"{X:.3f},{Y:.3f}"

    def arc(r, x, y, large=0, sweep=1):
        R = S(r)
        return f"A{R:.3f},{R:.3f} 0 {large} {sweep} {p(x, y)}"

    def rrect(x, y, w, h, r):
        r = min(r, w / 2, h / 2)
        return (f"M{p(x + r, y)}L{p(x + w - r, y)}{arc(r, x + w, y + r)}L{p(x + w, y + h - r)}"
                f"{arc(r, x + w - r, y + h)}L{p(x + r, y + h)}{arc(r, x, y + h - r)}L{p(x, y + r)}"
                f"{arc(r, x + r, y)}Z")

    x0, y0, w, h, r = BODY
    x1, y1 = x0 + w, y0 + h
    (tx, ty), (sx, sy) = TAIL
    d = (f"M{p(x0 + r, y0)}L{p(x1 - r, y0)}{arc(r, x1, y0 + r)}L{p(x1, y1 - r)}{arc(r, x1 - r, y1)}"
         f"L{p(tx, ty)}L{p(sx, sy)}L{p(x0 + r, y1)}{arc(r, x0, y1 - r)}L{p(x0, y0 + r)}{arc(r, x0 + r, y0)}Z")
    bw, pitch, heights = BARS
    cy = y0 + h / 2
    cx0 = x0 + w / 2 - pitch * (len(heights) - 1) / 2
    for i, bh in enumerate(heights):
        d += rrect(cx0 + i * pitch - bw / 2, cy - bh / 2, bw, bh, bw / 2)
    dcx, dcy = x1 + DOT_GAP + DOT_R, y1 - DOT_R
    d += f"M{p(dcx - DOT_R, dcy)}{arc(DOT_R, dcx + DOT_R, dcy, 1, 0)}{arc(DOT_R, dcx - DOT_R, dcy, 1, 0)}Z"
    return d


def bbox():
    x0, y0, w, h, _ = BODY
    return x0, y0, x0 + w + DOT_GAP + 2 * DOT_R, TAIL[1][1]


# ---------- Skalierung ins 108-dp-Raster ----------
bx0, by0, bx1, by1 = bbox()
bw_, bh_ = bx1 - bx0, by1 - by0
s = HALF_DIAG / (math.hypot(bw_, bh_) / 2)
ox, oy = 54 - (bx0 + bw_ / 2) * s, 54 - (by0 + bh_ / 2) * s
T108 = lambda x, y: (x * s + ox, y * s + oy)
S108 = lambda r: r * s
D108 = shape(T108, S108)
W, H = bw_ * s, bh_ * s                    # Größe des Zeichens in dp

# Benachrichtigung: das Zeichen füllt 22 von 24 dp (Material: 1 dp Rand)
side = max(W, H) / 22 * 24
k = 24 / side
T24 = lambda x, y: ((T108(x, y)[0] - (54 - side / 2)) * k, (T108(x, y)[1] - (54 - side / 2)) * k)
S24 = lambda r: r * s * k
D24 = shape(T24, S24)

HEADER = "<!-- Azentrius Designsystem · Lautwerk · erzeugt von scripts/app-icon-lautwerk.py, nicht von Hand ändern -->\n"


def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(text)


def vector(size, viewport, d, color):
    return (f'<?xml version="1.0" encoding="utf-8"?>\n{HEADER}'
            f'<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
            f'    android:width="{size}dp"\n    android:height="{size}dp"\n'
            f'    android:viewportWidth="{viewport}"\n    android:viewportHeight="{viewport}">\n'
            f'    <path\n        android:fillColor="{color}"\n        android:fillType="evenOdd"\n'
            f'        android:pathData="{d}" />\n</vector>\n')


ADAPTIVE = (f'<?xml version="1.0" encoding="utf-8"?>\n{HEADER}'
            '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
            '    <background android:drawable="@color/ic_launcher_background" />\n'
            '    <foreground android:drawable="@drawable/ic_launcher_foreground" />\n'
            '    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />\n'
            '</adaptive-icon>\n')

# Android-Ressourcen
write(f"{RES}/drawable/ic_launcher_foreground.xml", vector(108, 108, D108, "#FF" + GOLD[1:]))
write(f"{RES}/drawable/ic_launcher_monochrome.xml", vector(108, 108, D108, "#FF000000"))
write(f"{RES}/drawable/ic_stat_lautwerk.xml", vector(24, 24, D24, "#FFFFFFFF"))
write(f"{RES}/mipmap-anydpi/ic_launcher.xml", ADAPTIVE)
write(f"{RES}/mipmap-anydpi/ic_launcher_round.xml", ADAPTIVE)
write(f"{RES}/values/ic_launcher_background.xml",
      f'<?xml version="1.0" encoding="utf-8"?>\n{HEADER}<resources>\n'
      f'    <!-- brand-ink: Grund des App-Symbols und des Startbilds -->\n'
      f'    <color name="ic_launcher_background">#FF{INK[1:]}</color>\n</resources>\n')


# Vorlagen als SVG
def svg(viewbox, size, d, fill, bg=None):
    rect = f'<rect x="0" y="0" width="108" height="108" fill="{bg}"/>' if bg else ""
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="{viewbox}" width="{size}" height="{size}">'
            f'{rect}<path d="{d}" fill="{fill}" fill-rule="evenodd"/></svg>\n')


ICON = svg("18 18 72 72", 512, D108, GOLD, INK)          # so wie auf dem Startbildschirm, ohne Maske
write(f"{ASSETS}/lautwerk.svg", ICON)
write(f"{ASSETS}/lautwerk-foreground.svg", svg("0 0 108 108", 108, D108, GOLD))
write(f"{ASSETS}/lautwerk-monochrome.svg", svg("0 0 108 108", 108, D108, "#000000"))
write(f"{ASSETS}/lautwerk-notification.svg", svg("0 0 24 24", 24, D24, "#151515"))

try:
    import cairosvg
    cairosvg.svg2png(bytestring=ICON.encode(), write_to=f"{ASSETS}/lautwerk-play-512.png",
                     output_width=512, output_height=512)
except ImportError:
    print("cairosvg fehlt: Play-Store-Bild nicht erzeugt")

print(f"Zeichen {W:.2f} × {H:.2f} dp, Halbdiagonale {math.hypot(W, H) / 2:.2f} dp, "
      f"Benachrichtigung: Ausschnitt {side:.2f} dp → 24 dp")

#!/usr/bin/env python3
"""Erzeugt tokens.css aus tokens.json.

tokens.css wird nie von Hand bearbeitet. Nach jeder Änderung an tokens.json:

    python3 scripts/tokens-css.py

Das Ergebnis entspricht Byte für Byte der Datei, die das Designsystem in Claude
aus derselben tokens.json erzeugt. Nur Python 3, keine Abhängigkeiten.
"""
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / "tokens.json"
OUT = pathlib.Path(sys.argv[2]) if len(sys.argv) > 2 else ROOT / "tokens.css"
TITLE = sys.argv[3] if len(sys.argv) > 3 else "Azentrius Designsystem"

t = json.loads(SRC.read_text(encoding="utf-8"))
lines = [f"/* {TITLE} — generated from tokens.json */"]

# Schriften, Pfade relativ zu tokens.css
for f in t["type"]["fonts"]:
    style = f.get("style", "normal")
    lines.append(
        f'@font-face {{ font-family: "{f["family"]}"; src: url("{f["file"]}") format("woff2"); '
        f'font-weight: {f["weight"]}; font-style: {style}; font-display: swap; }}'
    )

# Farben und Schatten je Thema; das erste Thema gilt auch auf :root
themes = [th["id"] for th in t["color"]["themes"]]
first = themes[0]
themed = list(t["color"]["tokens"]) + list(t.get("shadow", {}).get("tokens", []))
for i, theme in enumerate(themes):
    selector = f':root, [data-theme="{theme}"]' if i == 0 else f'[data-theme="{theme}"]'
    lines.append(selector + " {")
    for token in themed:
        value = token["value"]
        if isinstance(value, str) and i > 0:
            continue  # themenunabhängige Werte (brand-…) stehen nur im ersten Block
        if not isinstance(value, str):
            value = value.get(theme, value.get(first))
        lines.append(f"  --{token['name']}: {value};")
    lines.append("}")

# Abstände, Radien, Schriftfamilien
lines.append(":root {")
for family in ("spacing", "radius"):
    for token in t[family]["tokens"]:
        lines.append(f"  --{token['name']}: {token['value']};")
for name, stack in t["type"]["families"].items():
    lines.append(f"  --font-{name}: {stack};")
lines.append("}")

# Textstile als Klassen
for group in t["type"]["groups"]:
    for s in group["styles"]:
        family = s.get("family", group.get("family"))
        lines.append(
            f".{s['name']} {{ font-family: var(--font-{family}); font-size: {s['fontSize']}; "
            f"line-height: {s['lineHeight']}; font-weight: {s['fontWeight']}; }}"
        )

OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
print(f"{OUT.name} aus {SRC.name} erzeugt")

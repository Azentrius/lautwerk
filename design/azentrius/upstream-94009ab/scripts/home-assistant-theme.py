#!/usr/bin/env python3
"""Erzeugt das Home-Assistant-Theme „Azentrius“ aus tokens.json.

Die Dateien unter home-assistant/ werden nie von Hand bearbeitet. Nach jeder
Änderung an tokens.json oder an der Zuordnung unten:

    python3 scripts/home-assistant-theme.py

Mit --config <ordner> entsteht zusätzlich ein fertiger Ordner im Aufbau des
Home-Assistant-Konfigurationsordners, samt Schriftdateien und deren Lizenztexten:

    python3 scripts/home-assistant-theme.py --config /pfad/zu/ha-config
      → themes/azentrius.yaml
      → www/azentrius/azentrius.js, DMSans-VF.woff2, RedHatMono-VF.woff2,
        OFL-DM-Sans.txt, OFL-Red-Hat-Fonts.txt

Für das öffentliche Repo Azentrius/home-assistant-theme schreibt --lang en
dieselben Werte mit englischen Kommentaren, etwa:

    python3 scripts/home-assistant-theme.py --lang en --config ../home-assistant-theme

Das Skript prüft die Kontraste der Farbpaare, die das Theme neu bildet, auch
der Paare, die Home Assistant aus zwei Variablen des Themes zusammensetzt, und
bricht ab, wenn eines unter seiner Grenze liegt. Nur Python 3, keine
Abhängigkeiten. Geprüft gegen Home Assistant 2026.10 (Frontend 20260930.4).
"""
import argparse
import json
import pathlib
import shutil
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "home-assistant"
NAME = "Azentrius"

# Home Assistant hat nur Hell und Dunkel; jedes bekommt sein Azentrius-Thema.
MODES = {"light": "azentrius-hell", "dark": "azentrius-dunkel"}

# Schriftdateien, die das Theme mitbringt (Oberfläche und Code), mit ihren Lizenztexten.
FONTS = [
    ("DM Sans", "fonts/DMSans-VF.woff2", "LICENSES/OFL-DM-Sans.txt"),
    ("Red Hat Mono", "fonts/RedHatMono-VF.woff2", "LICENSES/OFL-Red-Hat-Fonts.txt"),
]

# Werte für beide Modi. ("family", x): Schriftstapel aus type.families,
# ("radius", x): Radius-Token, ("weight", x): Gewicht eines Textstils,
# ein einfacher String: fester Wert.
SHARED = [
    ("Schrift: DM Sans für die Oberfläche, Red Hat Mono für Code", [
        ("ha-font-family-body", ("family", "app")),
        ("ha-font-family-heading", ("family", "app")),
        ("ha-font-family-longform", ("family", "app")),
        ("ha-font-family-code", ("family", "mono")),
        ("ha-font-weight-bold", ("weight", "app-title")),
    ]),
    ("Form: Karten flach mit Rahmen, Dialoge mit großem Radius", [
        ("ha-card-border-radius", ("radius", "radius-medium")),
        ("ha-card-border-width", "1px"),
        ("ha-card-box-shadow", "none"),
        ("ha-dialog-border-radius", ("radius", "radius-large")),
    ]),
]

# Werte je Modus: Name eines Farb- oder Schatten-Tokens, aufgelöst im Thema
# des Modus.
PER_MODE = [
    ("Flächen: bg-page als Grund, bg-card für Karten, bg-floating für Schwebendes", [
        ("primary-background-color", "bg-page"),
        ("secondary-background-color", "bg-hover"),
        ("card-background-color", "bg-card"),
        ("ha-card-background", "bg-card"),
        ("ha-dialog-surface-background", "bg-floating"),
        ("ha-color-surface-default", "bg-floating"),
        ("ha-color-surface-low", {"light": "bg-page", "dark": "bg-card"}),
        ("ha-color-surface-lower", {"light": "tint-neutral", "dark": "bg-page"}),
        ("ha-color-on-surface-default", "text"),
        ("mdc-dialog-scrim-color", "backdrop"),
    ]),
    ("Kopf und Seitenleiste liegen auf dem Seitengrund, wie der Seitenkopf der Addons", [
        ("app-header-background-color", "bg-page"),
        ("app-header-text-color", "text"),
        ("app-theme-color", "bg-page"),
        ("sidebar-background-color", "bg-page"),
        ("sidebar-text-color", "text"),
        ("sidebar-icon-color", "text-subtle"),
        ("sidebar-selected-icon-color", "primary-text"),
        ("sidebar-selected-text-color", "primary-text"),
    ]),
    ("Text", [
        ("primary-text-color", "text"),
        ("secondary-text-color", "text-subtle"),
        ("disabled-text-color", "text-disabled"),
        ("text-primary-color", "on-primary"),
        ("ha-color-text-primary", "text"),
        ("ha-color-text-secondary", "text-subtle"),
        ("ha-color-text-disabled", "text-disabled"),
        ("ha-color-text-link", "link"),
    ]),
    ("Linien und Fokus", [
        ("divider-color", "line"),
        ("outline-color", "line"),
        ("outline-hover-color", "line-control"),
        ("ha-card-border-color", "line"),
        ("ha-color-border-neutral-quiet", "line"),
        ("ha-color-border-neutral-normal", "line-control"),
        ("ha-color-border-neutral-loud", "text-subtle"),
        ("ha-color-focus", "focus"),
    ]),
    ("Aktionsfarbe: im Hellen Tinte, im Dunkeln Gold", [
        ("primary-color", "primary"),
        ("dark-primary-color", "primary-hover"),
        ("darker-primary-color", "primary-hover"),
        ("light-primary-color", "tint-neutral"),
        ("text-light-primary-color", "on-tint-neutral"),
        ("ha-color-primary-30", "primary-hover"),
        ("ha-color-primary-40", "primary"),
        ("ha-color-primary-50", "primary"),
        ("ha-color-fill-primary-loud-resting", "primary"),
        ("ha-color-fill-primary-loud-hover", "primary-hover"),
        ("ha-color-fill-primary-loud-active", "primary"),
        ("ha-color-on-primary-loud", "on-primary"),
        ("ha-color-fill-primary-normal-resting", "tint-neutral"),
        ("ha-color-fill-primary-normal-hover", "line"),
        ("ha-color-fill-primary-normal-active", "tint-neutral"),
        ("ha-color-fill-primary-quiet-resting", "bg-hover"),
        ("ha-color-fill-primary-quiet-hover", "tint-neutral"),
        ("ha-color-fill-primary-quiet-active", "bg-hover"),
        ("ha-color-on-primary-normal", "primary-text"),
        ("ha-color-on-primary-quiet", "primary-text"),
        ("ha-color-border-primary-loud", "primary"),
        ("ha-color-border-primary-normal", "primary"),
        ("ha-color-border-primary-quiet", "line"),
    ]),
    ("Schalter: an in primary mit on-primary-Knopf, aus in bg-control mit line-control-Rahmen", [
        ("ha-switch-background-color", "bg-control"),
        ("ha-switch-background-color-hover", "bg-hover"),
        ("ha-switch-border-color", "line-control"),
        ("ha-switch-thumb-background-color", "line-control"),
        ("ha-switch-thumb-border-color", "line-control"),
        ("ha-switch-checked-background-color", "primary"),
        ("ha-switch-checked-background-color-hover", "primary-hover"),
        ("ha-switch-checked-border-color", "primary"),
        ("ha-switch-checked-border-color-hover", "primary-hover"),
        ("ha-switch-checked-thumb-background-color", "on-primary"),
        ("ha-switch-checked-thumb-border-color", "on-primary"),
    ]),
    ("Neutrale Flächen und deaktivierte Elemente", [
        ("ha-color-fill-neutral-quiet-resting", "bg-hover"),
        ("ha-color-fill-neutral-quiet-hover", "tint-neutral"),
        ("ha-color-fill-neutral-quiet-active", "bg-hover"),
        ("ha-color-fill-neutral-normal-resting", "tint-neutral"),
        ("ha-color-fill-neutral-normal-hover", "line"),
        ("ha-color-fill-neutral-normal-active", "tint-neutral"),
        ("ha-color-fill-neutral-loud-resting", "text-subtle"),
        ("ha-color-fill-neutral-loud-hover", "text"),
        ("ha-color-fill-neutral-loud-active", "text-subtle"),
        ("ha-color-on-neutral-loud", "on-primary"),
        ("ha-color-on-neutral-normal", "on-tint-neutral"),
        ("ha-color-on-neutral-quiet", "text-subtle"),
        ("ha-color-fill-disabled-quiet-resting", "bg-hover"),
        ("ha-color-fill-disabled-quiet-hover", "tint-neutral"),
        ("ha-color-fill-disabled-normal-resting", "bg-hover"),
        ("ha-color-fill-disabled-normal-hover", "tint-neutral"),
        ("ha-color-fill-disabled-loud-resting", "tint-neutral"),
        ("ha-color-fill-disabled-loud-hover", "line"),
        ("ha-color-on-disabled-quiet", "text-disabled"),
        ("ha-color-on-disabled-normal", "text-disabled"),
        ("ha-color-on-disabled-loud", "text-disabled"),
    ]),
    ("Eingabefelder: im Hellen bg-hover, im Dunkeln bg-page, damit sie sich von Dialog und Karte abheben", [
        ("ha-color-form-background", {"light": "bg-hover", "dark": "bg-page"}),
        ("ha-color-form-background-hover", {"light": "tint-neutral", "dark": "bg-card"}),
        ("ha-color-form-background-disabled", "bg-page"),
        ("input-fill-color", "bg-hover"),
        ("input-ink-color", "text"),
        ("input-label-ink-color", "text-subtle"),
        ("input-idle-line-color", "text-subtle"),
        ("input-outlined-idle-border-color", "line-control"),
    ]),
    ("Status wie in der Statussprache; Hinweise und Zähler der Seitenleiste als Hinweis, nie in Gold", [
        ("success-color", "status-success"),
        ("warning-color", "status-warning"),
        ("error-color", "status-danger"),
        ("info-color", "status-info"),
        ("accent-color", "status-info"),
        ("text-accent-color", "on-info"),
    ]),
    ("Schatten nur für Schwebendes", [
        ("ha-box-shadow-s", "shadow-sm"),
        ("ha-box-shadow-m", "shadow-md"),
        ("ha-box-shadow-l", "shadow-lg"),
    ]),
    ("Symbole der Entitäten ohne eigene Zustandsfarbe; Gerätefarben wie Bernstein für Licht bleiben die von Home Assistant", [
        ("state-icon-color", "text-subtle"),
    ]),
]

# Statusfamilien in Home Assistants Namen: (HA-Name, Azentrius-Kürzel).
STATUS = [("danger", "danger"), ("warning", "warning"), ("success", "success")]


def status_group():
    """Füllungen, Rahmen und Schrift der Statusfarben für Buttons, Hinweise und Labels."""
    entries = []
    for ha, az in STATUS:
        for state in ("resting", "hover", "active"):
            entries.append((f"ha-color-fill-{ha}-loud-{state}", f"status-{az}"))
            entries.append((f"ha-color-fill-{ha}-normal-{state}", f"tint-{az}"))
            entries.append((f"ha-color-fill-{ha}-quiet-{state}", f"tint-{az}"))
        entries += [
            (f"ha-color-on-{ha}-loud", f"on-{az}"),
            (f"ha-color-on-{ha}-normal", f"on-tint-{az}"),
            (f"ha-color-on-{ha}-quiet", f"on-tint-{az}"),
            (f"ha-color-border-{ha}-loud", f"status-{az}"),
            (f"ha-color-border-{ha}-normal", f"status-{az}"),
            (f"ha-color-border-{ha}-quiet", f"tint-{az}"),
        ]
    return ("Statusfarben für Buttons, Hinweise und Labels: getönt mit on-tint-Schrift, kräftig mit on-Schrift", entries)


# Kommentare für das öffentliche, englische Repo (--lang en); die Werte sind dieselben.
TITLES_EN = {
    "Schrift: DM Sans für die Oberfläche, Red Hat Mono für Code": "Type: DM Sans for the interface, Red Hat Mono for code",
    "Form: Karten flach mit Rahmen, Dialoge mit großem Radius": "Shape: flat cards with a border, dialogs with a large radius",
    "Flächen: bg-page als Grund, bg-card für Karten, bg-floating für Schwebendes": "Surfaces: bg-page as the ground, bg-card for cards, bg-floating for what floats",
    "Kopf und Seitenleiste liegen auf dem Seitengrund, wie der Seitenkopf der Addons": "Header and sidebar sit on the page ground, like the page header of the add-ons",
    "Text": "Text",
    "Linien und Fokus": "Lines and focus",
    "Aktionsfarbe: im Hellen Tinte, im Dunkeln Gold": "Action colour: ink in light mode, gold in dark mode",
    "Schalter: an in primary mit on-primary-Knopf, aus in bg-control mit line-control-Rahmen": "Switches: on in primary with an on-primary knob, off in bg-control with a line-control border",
    "Neutrale Flächen und deaktivierte Elemente": "Neutral fills and disabled elements",
    "Eingabefelder: im Hellen bg-hover, im Dunkeln bg-page, damit sie sich von Dialog und Karte abheben": "Input fields: bg-hover in light mode, bg-page in dark mode, so they stand out from dialogs and cards",
    "Status wie in der Statussprache; Hinweise und Zähler der Seitenleiste als Hinweis, nie in Gold": "Status as in the status language; sidebar badges count as information, never gold",
    "Schatten nur für Schwebendes": "Shadows only for what floats",
    "Symbole der Entitäten ohne eigene Zustandsfarbe; Gerätefarben wie Bernstein für Licht bleiben die von Home Assistant": "Icons of entities without a state colour; device colours such as amber for lights stay Home Assistant's",
    "Statusfarben für Buttons, Hinweise und Labels: getönt mit on-tint-Schrift, kräftig mit on-Schrift": "Status colours for buttons, alerts and labels: tinted with on-tint text, strong with on text",
}

HEADER = {
    "de": [
        f"# {NAME} für Home Assistant, erzeugt aus tokens.json des Azentrius Design System.",
        "# Nicht von Hand ändern: python3 scripts/home-assistant-theme.py",
        "# Hell folgt „Azentrius hell“, Dunkel „Azentrius dunkel“; Home Assistant wählt nach seiner Einstellung.",
        "# Geprüft gegen Home Assistant 2026.10 (Frontend 20260930.4).",
    ],
    "en": [
        f"# {NAME} for Home Assistant, generated from the tokens of the Azentrius Design System.",
        "# Do not edit by hand: changes come from the design system.",
        "# Light mode follows \"Azentrius hell\", dark mode \"Azentrius dunkel\"; Home Assistant picks the mode.",
        "# Tested with Home Assistant 2026.10 (frontend 20260930.4).",
    ],
}

JS_HEADER = {
    "de": [
        "// Azentrius für Home Assistant: Schriften aus /local/azentrius/, ohne externe Anfrage.",
        "// Erzeugt von scripts/home-assistant-theme.py. Nicht von Hand ändern.",
        "// Einbinden über frontend: extra_module_url: [/local/azentrius/azentrius.js]",
    ],
    "en": [
        "// Azentrius for Home Assistant: fonts from /local/azentrius/, no external requests.",
        "// Generated from the Azentrius Design System. Do not edit by hand.",
        "// Load it with frontend: extra_module_url: [/local/azentrius/azentrius.js]",
    ],
}


# Farbpaare, die das Theme neu bildet: (Vordergrund, Hintergrund, Mindestkontrast).
CHECKS = [
    ("text", "bg-page", 4.5), ("text", "bg-card", 4.5), ("text", "bg-floating", 4.5),
    ("text-subtle", "bg-page", 4.5), ("text-subtle", "bg-card", 4.5), ("text-subtle", "bg-hover", 4.5),
    ("link", "bg-page", 4.5), ("link", "bg-card", 4.5),
    ("primary-text", "bg-page", 4.5), ("primary-text", "bg-hover", 4.5),
    ("primary-text", "tint-neutral", 4.5), ("primary-text", "line", 4.5),
    ("on-primary", "primary", 4.5), ("on-primary", "primary-hover", 4.5),
    ("on-primary", "text-subtle", 4.5), ("on-primary", "text", 4.5),
    ("on-tint-neutral", "tint-neutral", 4.5), ("on-tint-neutral", "line", 4.5),
    ("on-info", "status-info", 4.5),
    ("on-danger", "status-danger", 4.5), ("on-success", "status-success", 4.5),
    ("on-warning", "status-warning", 4.5),
    ("on-tint-danger", "tint-danger", 4.5), ("on-tint-warning", "tint-warning", 4.5),
    ("on-tint-success", "tint-success", 4.5),
    ("primary", "bg-page", 3), ("primary", "bg-card", 3), ("primary", "bg-control", 3),
    ("line-control", "bg-control", 3), ("line-control", "bg-card", 3),
    ("focus", "bg-page", 3), ("focus", "bg-card", 3), ("focus", "bg-floating", 3),
]

# Paare, die Home Assistant aus zwei Variablen des Themes bildet, geprüft mit den
# Werten nach der Zuordnung oben: die Initialen im Benutzer-Avatar der Seitenleiste
# (ha-user-badge) und auf Eingabefeldern in Ruhe und beim Überfahren die Eingabe,
# die Beschriftung, die Beschriftung bei Fehler und im Fokus und die Unterlinie.
HA_CHECKS = [("text-light-primary-color", "light-primary-color", 4.5)] + [
    (fg, bg, minimum)
    for bg in ("ha-color-form-background", "ha-color-form-background-hover")
    for fg, minimum in (
        ("primary-text-color", 4.5), ("secondary-text-color", 4.5),
        ("ha-color-fill-danger-loud-resting", 4.5), ("primary-color", 4.5),
        ("ha-color-border-neutral-loud", 3),
    )
]


def luminance(hex_value):
    h = hex_value.lstrip("#")[:6]
    channels = [int(h[i:i + 2], 16) / 255 for i in (0, 2, 4)]
    linear = [c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4 for c in channels]
    return 0.2126 * linear[0] + 0.7152 * linear[1] + 0.0722 * linear[2]


def contrast(a, b):
    high, low = sorted((luminance(a), luminance(b)), reverse=True)
    return (high + 0.05) / (low + 0.05)


def load_tokens(path):
    t = json.loads(path.read_text(encoding="utf-8"))
    themed = {tok["name"]: tok["value"] for tok in t["color"]["tokens"] + t.get("shadow", {}).get("tokens", [])}
    radius = {tok["name"]: tok["value"] for tok in t["radius"]["tokens"]}
    styles = {s["name"]: s for g in t["type"]["groups"] for s in g["styles"]}
    return t, themed, radius, styles


def resolve_color(themed, name, theme, seen=()):
    value = themed[name]
    if isinstance(value, dict):
        value = value[theme]
    if value.startswith("{") and value.endswith("}"):
        alias = value[1:-1]
        if alias in seen:
            sys.exit(f"Alias-Schleife bei {name}")
        return resolve_color(themed, alias, theme, seen + (name,))
    return value


def yaml_str(value):
    if '"' in value:
        return "'" + value.replace("'", "''") + "'"
    return '"' + value + '"'


def build(tokens_path, lang):
    t, themed, radius, styles = load_tokens(tokens_path)

    def title(text):
        return TITLES_EN[text] if lang == "en" else text

    def shared_value(source):
        if isinstance(source, str):
            return source
        kind, key = source
        if kind == "family":
            return t["type"]["families"][key]
        if kind == "radius":
            return radius[key]
        if kind == "weight":
            return str(styles[key]["fontWeight"])
        sys.exit(f"Unbekannte Quelle {source}")

    groups = PER_MODE + [status_group()]
    seen = set()
    for _, entries in SHARED + groups:
        for var, _ in entries:
            if var in seen:
                sys.exit(f"{var} ist doppelt zugeordnet")
            seen.add(var)

    lines = HEADER[lang] + [
        "# SPDX-License-Identifier: Apache-2.0",
        f"{NAME}:",
    ]
    for text, entries in SHARED:
        lines.append(f"  # {title(text)}")
        for var, source in entries:
            lines.append(f"  {var}: {yaml_str(shared_value(source))}")
    lines.append("  modes:")
    values = {}
    for mode, theme in MODES.items():
        lines.append(f"    {mode}:")
        values[mode] = {}
        for text, entries in groups:
            lines.append(f"      # {title(text)}")
            for var, source in entries:
                token = source[mode] if isinstance(source, dict) else source
                values[mode][var] = resolve_color(themed, token, theme)
                lines.append(f"      {var}: {yaml_str(values[mode][var])}")
    yaml_text = "\n".join(lines) + "\n"

    failures = [f"{var} fehlt in der Zuordnung, Home Assistant nähme seinen eigenen Wert"
                for var in dict.fromkeys(v for fg, bg, _ in HA_CHECKS for v in (fg, bg)) if var not in seen]
    if failures:
        return yaml_text, failures
    for mode, theme in MODES.items():
        pairs = [(fg, bg, minimum, resolve_color(themed, fg, theme), resolve_color(themed, bg, theme))
                 for fg, bg, minimum in CHECKS]
        pairs += [(fg, bg, minimum, values[mode][fg], values[mode][bg]) for fg, bg, minimum in HA_CHECKS]
        for fg, bg, minimum, fg_value, bg_value in pairs:
            ratio = contrast(fg_value, bg_value)
            if ratio < minimum:
                failures.append(f"{theme}: {fg} auf {bg} {ratio:.2f}:1, nötig {minimum}:1")
    return yaml_text, failures


def build_js(weights, lang):
    faces = []
    for family, file, _ in FONTS:
        name = pathlib.PurePosixPath(file).name
        weight = weights[family]
        faces.append(
            f'@font-face {{ font-family: "{family}"; src: url("${{base}}{name}") format("woff2"); '
            f"font-weight: {weight}; font-style: normal; font-display: swap; }}"
        )
    return (
        "\n".join(JS_HEADER[lang]) + "\n"
        "// SPDX-License-Identifier: Apache-2.0\n"
        "const base = new URL(\".\", import.meta.url).href;\n"
        "const style = document.createElement(\"style\");\n"
        "style.id = \"azentrius-fonts\";\n"
        "style.textContent = `\n"
        + "\n".join(faces)
        + "\n"
        "body { font-family: var(--ha-font-family-body); }\n"
        "`;\n"
        "document.head.append(style);\n"
    )


def font_weights(tokens_path):
    t = json.loads(tokens_path.read_text(encoding="utf-8"))
    return {f["family"]: f["weight"] for f in t["type"]["fonts"] if f.get("style", "normal") == "normal"}


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--tokens", type=pathlib.Path, default=ROOT / "tokens.json")
    parser.add_argument("--out", type=pathlib.Path, default=OUT, help="Zielordner im Repo")
    parser.add_argument("--config", type=pathlib.Path, help="zusätzlich als Home-Assistant-Konfigurationsordner mit Schriften")
    parser.add_argument("--lang", choices=("de", "en"), default="de",
                        help="Sprache der Kommentare; en schreibt nur nach --config, nie ins Repo")
    args = parser.parse_args()
    if args.lang == "en" and not args.config:
        sys.exit("--lang en braucht --config")

    yaml_text, failures = build(args.tokens, args.lang)
    if failures:
        print("Kontrast zu gering:", *failures, sep="\n  ")
        sys.exit(1)

    weights = font_weights(args.tokens)
    for family, _, _ in FONTS:
        if family not in weights:
            sys.exit(f"{family} fehlt in tokens.json type.fonts")
    js_text = build_js(weights, args.lang)

    targets = ([args.out] if args.lang == "de" else []) + ([args.config] if args.config else [])
    for target in targets:
        theme_file = target / "themes" / "azentrius.yaml"
        js_file = target / "www" / "azentrius" / "azentrius.js"
        theme_file.parent.mkdir(parents=True, exist_ok=True)
        js_file.parent.mkdir(parents=True, exist_ok=True)
        theme_file.write_bytes(yaml_text.encode("utf-8"))
        js_file.write_bytes(js_text.encode("utf-8"))
        print(f"{theme_file.relative_to(target)} und {js_file.relative_to(target)} in {target} erzeugt")
    if args.config:
        folder = args.config / "www" / "azentrius"
        for _, file, license_file in FONTS:
            for source in (file, license_file):
                shutil.copyfile(ROOT / source, folder / pathlib.PurePosixPath(source).name)
        print(f"Schriften und ihre Lizenztexte nach {folder} kopiert")


if __name__ == "__main__":
    main()

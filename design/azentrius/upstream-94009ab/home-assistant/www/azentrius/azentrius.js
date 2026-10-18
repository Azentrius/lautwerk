// Azentrius für Home Assistant: Schriften aus /local/azentrius/, ohne externe Anfrage.
// Erzeugt von scripts/home-assistant-theme.py. Nicht von Hand ändern.
// Einbinden über frontend: extra_module_url: [/local/azentrius/azentrius.js]
// SPDX-License-Identifier: Apache-2.0
const base = new URL(".", import.meta.url).href;
const style = document.createElement("style");
style.id = "azentrius-fonts";
style.textContent = `
@font-face { font-family: "DM Sans"; src: url("${base}DMSans-VF.woff2") format("woff2"); font-weight: 100 1000; font-style: normal; font-display: swap; }
@font-face { font-family: "Red Hat Mono"; src: url("${base}RedHatMono-VF.woff2") format("woff2"); font-weight: 300 700; font-style: normal; font-display: swap; }
body { font-family: var(--ha-font-family-body); }
`;
document.head.append(style);

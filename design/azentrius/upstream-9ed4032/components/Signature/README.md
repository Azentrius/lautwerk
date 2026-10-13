# Signature

Die Azentrius-Signatur im Fuß jeder Seite: die Wortmarke „Azentrius.“ mit goldenem Punkt, daneben Name und Version des Addons.

Kein Gegenstück in PatternFly. Sie ist die einzige Stelle, an der die Marke in Cockpit sichtbar wird.

```html
<footer class="az-signature">
  <a class="az-signature__mark" href="https://azentrius.de">Azentrius</a>
  <span class="az-signature__meta"><span>Cron Jobs 1.0.0</span><a href="…">Quellcode</a></span>
</footer>
```

- Am Ende des Seiteninhalts, nach `space-2xl` Abstand zur letzten Karte. Nicht fixiert, nicht im Kopf.
- Die Wortmarke steht in DM Serif Display im Stil `signature`: das Wort in `text`, der Punkt aus CSS in `signature`. So wie auf dem Cover. Das Wort ist immer „Azentrius“.
- Rechts davon nur Fakten: Addon-Name und Version, Link zum Quellcode, bei Bedarf der Hostname.
- Kein Logo-Bild. Das rote Hexagon von azentrius.de gehört nicht zu diesem System.

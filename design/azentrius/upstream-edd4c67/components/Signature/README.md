# Signature

Die Azentrius-Signatur im Fuß jeder Seite: die Wortmarke „Azentrius.“ mit goldenem Punkt, daneben Name, Version und Host. In allen Addons und Web-Oberflächen gleich.

Kein Gegenstück in PatternFly.

```html
<footer class="az-signature">
  <a class="az-signature__mark" href="https://azentrius.de">Azentrius</a>
  <span class="az-signature__meta"><span>Cron Jobs 2.1.0</span><span>public-host</span></span>
</footer>
```

- Am Ende des Seiteninhalts, nach `space-lg` Abstand zur letzten Karte, mit einer Linie darüber. Nicht fixiert, nicht im Kopf. Bei Reitern steht sie unter dem Inhalt jedes Reiters, also immer am Seitenende.
- Die Wortmarke steht in DM Serif Display im Stil `signature`: das Wort in `text`, der Punkt aus CSS in `signature`. So wie auf dem Cover. Das Wort ist immer „Azentrius“.
- Rechts davon nur Fakten, in dieser Reihenfolge: der volle Produktname mit Version („Image-Updates monitor 0.6.2“), dann der Host. Ein Link zum Quellcode darf folgen.
- Kein Logo-Bild. Das rote Hexagon von azentrius.de gehört nicht zu diesem System.

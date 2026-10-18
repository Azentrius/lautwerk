# Signature

Die Azentrius-Signatur im Fuß jeder Seite: die Wortmarke „Azentrius.“ mit goldenem Punkt, daneben Name, Version und Host. In allen Addons und Web-Oberflächen gleich.

Kein Gegenstück in PatternFly.

```html
<footer class="az-signature">
  <a class="az-signature__mark" href="https://azentrius.com">Azentrius</a>
  <span class="az-signature__meta"><span>Cron Jobs 2.1.0</span><span>public-host</span></span>
</footer>
```

- Am Ende des Seiteninhalts, nach `space-lg` Abstand zur letzten Karte, mit einer Linie darüber. Nicht fixiert, nicht im Kopf. Bei Reitern steht sie unter dem Inhalt jedes Reiters, also immer am Seitenende.
- Die Wortmarke steht in DM Serif Display im Stil `signature`: das Wort in `text`, der Punkt aus CSS in `signature`. So wie auf dem Cover. Das Wort ist immer „Azentrius“.
- Rechts davon nur Fakten, in dieser Reihenfolge: der volle Produktname in reinem Text mit Version („Image Update Monitor 0.6.2“, „Cron Jobs 2.1.0“, ohne Kursiv und ohne Punkt), dann der Host. Ein Link zum Quellcode darf folgen.
- Kein Logo-Bild, auch nicht das Zeichen „A.“.
- Auf Projektseiten steht die Wortmarke im Kopf und im Fuß; der Fuß trägt dort statt Version und Host die Links zu Impressum, Datenschutz und Kontakt (Abschnitt „Projektseiten“).

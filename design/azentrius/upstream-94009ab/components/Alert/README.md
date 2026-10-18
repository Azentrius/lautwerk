# Alert

Eine Rückmeldung im Seitenfluss: Erfolg nach dem Speichern, ein Fehler, ein Hinweis zur ganzen Seite.

Entspricht PatternFly `Alert` (inline).

```html
<div class="az-alert az-alert--success" role="status">
  <span class="az-alert__icon"><svg class="az-icon">…check-circle…</svg></span>
  <p class="az-alert__title">Crontab gespeichert</p>
  <p class="az-alert__body">Backup unter <code>/var/backups/…</code></p>
</div>
```

- Oberhalb der Karte, die er betrifft, oder ganz oben auf der Seite.
- Titel sagt, was passiert ist. Bei Fehlern sagt der Text, wie man es behebt; eine Aktion als `az-btn--link` darunter.
- Fehler bekommen `role="alert"`, alles andere `role="status"`.
- Erfolgsmeldungen verschwinden nicht von selbst, wenn sie etwas Wichtiges nennen (z. B. den Backup-Pfad).
- Die Rahmenfarbe trägt Bedeutung; im Thema „Cockpit hell“ ist Gelb dafür zu blass (PatternFly-Wert). Deshalb nie ohne Icon und Titel.

# TextField

Ein beschriftetes Eingabefeld mit Hilfetext oder Fehlermeldung darunter.

Entspricht PatternFly `FormGroup` mit `TextInput` und `HelperText`.

```html
<div class="az-field">
  <label class="az-field__label" for="cmd">Befehl</label>
  <input class="az-input az-mono" id="cmd" aria-invalid="true" aria-describedby="cmd-err">
  <p class="az-field__error" id="cmd-err"><svg class="az-icon">…</svg>Geben Sie einen Befehl ein.</p>
</div>
```

- Beschriftung über dem Feld, nie nur als Platzhalter.
- Der Hilfetext darf live mitlaufen: unter dem Zeitplan steht die Übersetzung in Klartext („Montags um 03:00“).
- Fehler erst nach dem Verlassen des Feldes oder beim Speichern. Text in `danger-text`, mit Icon, als Anweisung formuliert.
- Technische Eingaben (Zeitplan, Befehl, Pfad) in `az-input az-mono`, mit `spellcheck="false"`.

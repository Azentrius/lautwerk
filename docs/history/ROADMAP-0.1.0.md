# OpenAuto AI — Roadmap

## Ziel
Offene, lokale Android-App für sprachgesteuerte KI-Unterhaltung in Verbindung mit Android Auto (USB oder AAWireless). Smartphone-Funktionen bleiben ohne Autodisplay nutzbar.

## Etappe 0 — Forschungsprototyp (Quellcode vorhanden, Android-Build ungetestet)
- [x] Anbieter-Adapter (OpenAI Responses API, Anthropic Messages API)
- [x] Auswahl aus einer statischen Modellliste
- [x] Schlüssel im Android Keystore (lokal verschlüsselt)
- [x] Eigener Chatverlauf (lokal verschlüsselt) mit Wiederöffnen und Löschen
- [x] Android SpeechRecognizer + TTS für erste Sprachversuche
- [ ] Build mit Android Studio; Sprach-/API-Probe auf Pixel 10 Pro / Android 17

## Etappe 1 — Smartphone-MVP
- [ ] Crash- und Netzwerkwiederholungen robust behandeln
- [ ] Bessere Sprachunterbrechung (Barge-in), Audiofokus und Audiowege
- [ ] Modelle serverseitig ermitteln; Kostenhinweise
- [ ] Sprachbefehle zur Modellauswahl; sicherer Anbieterwechsel
- [ ] Chat-Export; Löschen und Datenschutzoptionen
- [ ] Verständliche Konto-/API-Key-Einrichtung

## Etappe 2 — Auto-Integration (experimentell)
- [ ] USB-Android-Auto-Tests auf Kia
- [ ] AAWireless-Tests auf Kia
- [ ] CarAppService/CarAudioRecord-Probeprojekt, sofern als gültige Kategorie testbar
- [ ] Bedienung ohne Lesen/Tippen, keine Ablenkung im Fahrbetrieb
- [ ] Emulatortests sowie auf mehreren Android-/Fahrzeugversionen

## Etappe 3 — Open Source
- [ ] GitHub-Repository, CI, Tests, Issues und Beitragsrichtlinien
- [ ] Sicherheitsreview und dokumentierte API-/Berechtigungsanforderungen
- [ ] Signierte Builds und nachvollziehbare Releases

## Rahmenbedingungen
Eine freie universelle KI-Chat-App ist noch keine allgemein zugelassene Android-Auto-Kategorie. Eine Sideload-Installation der APK garantiert keine Projektion auf das Fahrzeugdisplay. Technische Ausweichwege sollen optional bleiben und keine Root-Pflicht schaffen.

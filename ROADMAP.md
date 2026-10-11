# Roadmap ab 0.1.6

Erledigt: Auf Basis von 0.1.0 ist der minimale DHU-Prototyp 0.1.1 gebaut und vom
Nutzer bestätigt. 0.1.2 ergänzt einen gemeinsamen Anbieter-/Modellkatalog und
lokal gespeicherte Auswahl für Smartphone und Car-Template derselben Test-App.
Das bestehende Assistant-Modul verwendet dieselben Typen und Speicherlogik.
0.1.3 ergänzt getrennte Anbieter-Adapter und einen offline getesteten Transport;
die bestehenden DHU-Regressionstests bestehen weiterhin. 0.1.4 kapselt die vorhandene
Chat-/Schlüsselspeicherung mit kompatiblem Format, gemeinsamen Sperren und
geprüften Fehlerfällen. 0.1.5 ergänzt eine gemeinsame Sprachsteuerung, Android-Audio-
Adapter und Hintergrundzugriff für den Sprachablauf; 72 automatisierte Tests bestehen.
0.1.6 korrigiert die beim manuellen Test entdeckte Kollision mit dem Fokus der
Spracherkennung. 57 JVM-Tests bestehen; praktischer Wiederholungstest steht aus.

1. Bestätigten DHU-Stand erhalten: Auch die Auswahl und Starten sind für 0.1.2
   vom Nutzer im echten DHU bestätigt. Künftige Erweiterungen separat regressionsprüfen.
2. Zulässigen Produktumfang für Android Auto klären; keine allgemeine KI-Kategorie
   behaupten und den Debug-POI-Platzhalter nicht veröffentlichen.
3. Die vorhandenen OpenAI-/Claude-Adapter mit eigenen API-Konten kontrolliert live testen.
   Fehlerfälle und Android-JSON sind offline geprüft; Modellzugriff und Voice-End-to-End
   sind nur nach einem bestätigten praktischen Test belegt; siehe `docs/VOICE.md`.
   Der DHU-Prototyp löst weiterhin keine API-Anfrage aus.
4. Auch die übrige Speicheroberfläche im Hintergrund ausführen und große Verläufe
   begrenzen. Export/Import, Gerätewechsel und explizite Wiederherstellung planen.
   Vorhandene Daten nicht stillschweigend migrieren oder löschen.
5. Die implementierten Voice-Schnittstellen praktisch prüfen: Mikrofonfreigaben,
   Audiofokus, hörbares TTS, Wiederaufnahme und Abbruch mit beiden API-Konten.
   Sofortigen Transportabbruch ergänzen. CarAudioRecord/Auto-Routing erst nach
   geklärter Plattformberechtigung; keine Voice-Funktion im DHU-Prototyp.
6. Modellkatalog gezielt pflegen und mögliche Modellmigrationen explizit anbieten;
   bisherige Auswahl/Chat-Zuordnung nicht unbemerkt verändern.
7. Quellcode in [Azentrius/openauto-ai](https://github.com/Azentrius/openauto-ai) pflegen
   und CI einrichten. Reale Testergebnisse vor weiteren Auto-Funktionen bewerten.

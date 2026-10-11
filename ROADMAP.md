# Roadmap ab 0.1.2

Erledigt: Auf Basis von 0.1.0 ist der minimale DHU-Prototyp 0.1.1 gebaut und vom
Nutzer bestätigt. 0.1.2 ergänzt einen gemeinsamen Anbieter-/Modellkatalog und
lokal gespeicherte Auswahl für Smartphone und Car-Template derselben Test-App.
Das bestehende Assistant-Modul verwendet dieselben Typen und Speicherlogik.

1. Bestätigten DHU-Stand erhalten: Auch die Auswahl und Starten sind für 0.1.2
   vom Nutzer im echten DHU bestätigt. Künftige Erweiterungen separat regressionsprüfen.
2. Zulässigen Produktumfang für Android Auto klären; keine allgemeine KI-Kategorie
   behaupten und den Debug-POI-Platzhalter nicht veröffentlichen.
3. `ChatApi` hinter getrennten OpenAI-/Claude-Adaptern anbinden. Modellzugriff und
   Fehlerbehandlung separat testen; gemeinsame Auswahl allein löst keine API-Anfrage aus.
4. `ConversationStore` und `SecretStore` als Repository/Credential-Komponenten kapseln.
   Bestehende Archivformate und Schlüssel erhalten; keine stillschweigende Übertragung
   von Chatverläufen bei Anbieterwechsel. Fehlerfälle und gleichzeitige Zugriffe prüfen.
5. VoiceService hinter einer Voice-Schnittstelle anbinden; Wiederaufnahme, Abbruch,
   Mikrofonberechtigungen und TTS-/Recognizer-Fehler prüfen. CarAudioRecord/Audio-Routing
   erst nach geklärter Plattformberechtigung; keine Voice-Funktion im DHU-Prototyp.
6. Modellkatalog gezielt pflegen und mögliche Modellmigrationen explizit anbieten;
   bisherige Auswahl/Chat-Zuordnung nicht unbemerkt verändern.
7. Quellcode in [Azentrius/openauto-ai](https://github.com/Azentrius/openauto-ai) pflegen
   und CI einrichten. Reale Testergebnisse vor weiteren Auto-Funktionen bewerten.

# Roadmap ab 0.1.1

1. Sichtbarkeit dokumentieren: APK-Installation, Car-Service-Registrierung,
   Android-Auto-Launcher, echtes Template und Starten-Klick jeweils getrennt bewerten.
2. Zulässigen Produktumfang für Android Auto klären. Keine allgemeine KI-Kategorie
   behaupten und den Debug-POI-Platzhalter nicht in eine Veröffentlichung übernehmen.
3. Die erhaltene Smartphone-App aus 0.1.0 separat testen: API-Modellzugriff,
   Keystore, verschlüsselte Archive, Mikrofonberechtigungen und Service-Lebenszyklus.
4. Vorhandene Provider-/Modelltypen in einen gemeinsamen Kern überführen;
   OpenAI und Claude mit separaten Adaptern, austauschbarem Modellkatalog und
   expliziter Nutzerwahl verbinden. Beim Anbieterwechsel keine stillschweigende
   Übertragung eines bisherigen Chatverlaufs.
5. ConversationStore als Repository und SecretStore als Credential-Komponente
   kapseln. Gleichzeitige Archivzugriffe und Fehlerfälle prüfen; lokale Datenmigration
   planen. Keine Änderungen an vorhandenen Schlüsseln/Archivformaten im Sichtbarkeitstest.
6. VoiceService hinter einer Voice-Schnittstelle anbinden, Wiederaufnahme,
   Abbruch und TTS-/Recognizer-Fehler testen. CarAudioRecord/Audio-Routing erst
   nach geklärter Plattformberechtigung und eigenem Test; kein Mikrofonzugriff im DHU-Prototyp.
7. GitHub-Repository und CI einrichten, anschließend reale Testergebnisse und
   kategoriekonformen Umfang bewerten. Kein automatischer Upload durch diesen Auftrag.

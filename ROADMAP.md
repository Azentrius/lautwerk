# Roadmap ab 0.1.15

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
Spracherkennung. Der Nutzer bestätigt den Sprachablauf für beide Anbieter.
0.1.7 ergänzt gemeinsame Stimmwahl, Tempo und Hörprobe; 75 automatisierte Prüfungen bestehen.
Der Nutzer bestätigt einen deutlich besseren Klang; lokale und Online-Varianten können ähnlich klingen.
0.1.8 ergänzt aktiven Netzwerkabbruch und Stoppen bei Display-/Anruf-/Geräteunterbrechungen;
90 automatisierte Prüfungen bestehen. Stoppen/Neustarten per Schaltfläche und Display-aus
sind für beide Anbieter vom Nutzer bestätigt. Anruf- und Headset-/Bluetooth-Tests sind
auf Nutzerwunsch vom 12.10.2026 zurückgestellt und bleiben ungeprüft.
0.1.9 trennt Gespräch/Einstellungen/Verlauf, hält Starten/Beenden sichtbar und führt
Speicherarbeit der Oberfläche im Hintergrund aus. Lange Verläufe werden bedarfsgerecht
gezeichnet; ältere Nachrichten bleiben erreichbar. 34 Gerätetests bestehen.
0.1.10 ergänzt den ausdrücklich gewählten Textexport einzelner Chats ohne Änderung
des verschlüsselten Archivs. 59 automatisierte Prüfungen bestehen. Der Nutzer
bestätigt am 12.10.2026 den lokalen Export sowie die vollständige, lesbare Datei.
Cloud-Dateidienste und Rotation während der Auswahl bleiben ungeprüft.
0.1.11 ergänzt die lokale Verlaufssuche mit Anbieterfilter und direktem Sprung zur
ersten passenden Nachricht. 78 automatisierte Prüfungen bestehen. Der Nutzer
bestätigt am 12.10.2026 Suche, Filter und Treffer-Navigation.

0.1.12 ergänzt Deutsch/Englisch, „Gespräch“-Bezeichnungen, sprachabhängige
Stimmwahl und zusätzliche Absicherung des Gesprächsstarts. 157 verschiedene
Tests bestanden; Neuerstellung und Sprachwechsel der Oberfläche mit erfundenen
Chats geprüft. GitHub-CI konfiguriert. Der Nutzer bestätigt den Gesprächsablauf
mit beiden Anbietern und den tatsächlichen Gerätesprachwechsel Deutsch/Englisch. Die DHU-Variante steigt
auf 0.1.12-prototype wegen der Übersetzung; Layout und Aktionen bleiben erhalten.

0.1.13 überträgt das freigegebene Azentrius-Design auf die Smartphone-App,
einschließlich gemessener, dreistufiger Reiter für große Schrift. 169 verschiedene
automatisierte Tests bestehen. Die DHU-Variante bleibt unverändert auf 0.1.12.
Der Nutzer bestätigt „Darstellung und Bedienung passen“; Audio- und Speicherabläufe
erhalten ihre bisherige Funktion. Herkunft und Regeln stehen in `docs/DESIGN_SYSTEM.md`.

0.1.14 setzt B1–B5 aus dem Android-Auto-Design `eb827c8` um: ausschließlich
das freigegebene Car-Farbpaar und der Primärfarbwunsch für „Starten“.
Build, Lint und GitHub-CI des Implementierungsstands sind erfolgreich. 0.1.14 ist
installiert; elf Prototype-Gerätetests und 64 DHU-Kombinationen bestehen.
A3/A4 und A7 sind gegen den Ausgangsstand geprüft, Host-API 8 ist erfasst.
Beide Sechs-Start-Tests bestehen; der Host ersetzt den Farbwert durch gut lesbares
Hellblau. Vollständige Texte, Live-Auswahl, Rotary-Fokus und englischer Fallback
bei französischer Gerätesprache sind geprüft. Keine Änderung am Zählerverhalten.
Siehe `docs/ANDROID_AUTO_DESIGN.md` und `docs/BUILD_AND_TEST.md`.

0.1.15 bindet das freigegebene App-Symbol einschließlich Monochrom-Ebene,
nativem Startbild und kleinem Dienstsymbol ein. Assistent und DHU sind auf dem
Pixel aktualisiert; 28 ausgewählte Gerätetests und Touch-/Rotary-Prüfungen
einschließlich sechs Starts ohne Reset bestehen. B2/B3 und Gesprächsverhalten
bleiben erhalten. Details: `docs/APP_IDENTITY.md` und `docs/BUILD_AND_TEST.md`.

1. Bestätigten DHU-Stand erhalten: Auch die Auswahl und Starten sind für 0.1.2
   vom Nutzer im echten DHU bestätigt. Künftige Erweiterungen separat regressionsprüfen.
2. Vorrangig einen reproduzierbaren Verteilungsweg außerhalb von Google Play prüfen:
   unveränderte APK nach freigegebenem Herkunftsversuch im ID.7 nativ drahtlos,
   zunächst im Stand. Launcher-Auswahl auf dem Pixel und DHU-Kontrolle bestehen;
   der Fahrzeugnachweis steht noch aus. Kia/AAWireless
   ist auf Nutzerwunsch zurückgestellt.
   Anleitung und Messstand: [Sideload](docs/SIDELOADING.md) und
   [Fahrzeugtests](docs/SIDELOAD_TEST_REPORT.md). Noch keine Kompatibilitätszusage.
   Keine allgemeine KI-Kategorie behaupten; der POI-Eintrag bleibt ein
   offengelegter Debug-Testplatzhalter, kein Produktrelease.
3. Die vorhandenen OpenAI-/Claude-Adapter mit eigenen API-Konten kontrolliert live testen.
   Fehlerfälle und Android-JSON sind offline geprüft; Modellzugriff und Voice-End-to-End
   sind nur nach einem bestätigten praktischen Test belegt; siehe `docs/VOICE.md`.
   Der DHU-Prototyp löst weiterhin keine API-Anfrage aus.
4. Verlaufssuche 0.1.11 und lokaler Textexport 0.1.10 sind bestätigt; Cloud-Ziele und Rotation während der
   Auswahl später prüfen. Die Smartphone-Bedienung wurde mit realen Gesprächen
   beider Anbieter unter 0.1.12 vom Nutzer bestätigt. Die vollständige
   Archivdatei wird weiterhin im Hintergrund gelesen; echte Seitennachladung und
   Größenregeln für sehr große Archive planen. Verschlüsseltes Backup/Import,
   Gerätewechsel und explizite Wiederherstellung folgen später. Der neue Textexport
   ist kein Wiederherstellungsformat; keine stillschweigende Migration oder Löschung.
5. Die implementierten Voice-Schnittstellen praktisch prüfen: Mikrofonfreigaben,
   Audiofokus, hörbares TTS, Wiederaufnahme und Abbruch mit beiden API-Konten.
   Zurückgestellt auf Nutzerwunsch: Anruf- und Headset-/Bluetooth-Unterbrechungen
   aus 0.1.8 später praktisch prüfen; derzeit kein bestätigtes Testergebnis.
   CarAudioRecord/Auto-Routing erst nach
   geklärter Plattformberechtigung; keine Voice-Funktion im DHU-Prototyp.
6. Modellkatalog gezielt pflegen und mögliche Modellmigrationen explizit anbieten;
   bisherige Auswahl/Chat-Zuordnung nicht unbemerkt verändern.
7. Quellcode in [Azentrius/openauto-ai](https://github.com/Azentrius/openauto-ai) pflegen
   und die eingerichtete CI nutzen. Entwurfs-PRs vor einer geordneten Zusammenführung prüfen. Reale Testergebnisse vor weiteren Auto-Funktionen bewerten.

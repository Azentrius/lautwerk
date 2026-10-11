# OpenAuto AI (early Android prototype)

Ein offenes Experiment für **sprachgesteuerte KI-Gespräche** mit OpenAI und Claude auf Android.

**Status:** Quellcode-Prototyp, **nicht** auf einem echten Gerät gebaut oder getestet. Enthält **noch keine** Android-Auto-Display-App oder `CarAudioRecord`-Integration. Die Sprach-Ein-/Ausgabe nutzt die auf dem Smartphone verfügbaren `SpeechRecognizer`- und `TextToSpeech`-Dienste. Ob das im Kia über AAWireless/USB auf das Fahrzeugmikrofon routet, muss getestet werden.

## Bereits implementiert (Stand 0.1.0-dev)
- Native Android/Jetpack-Compose-Oberfläche (Kotlin)
- API-Key-Verwaltung für OpenAI und Claude; lokale AES-GCM-Verschlüsselung im Android Keystore
- Wählbarer Anbieter und eine kleine feste Modellliste (API-Zugriff je nach Konto)
- OpenAI Responses API und Anthropic Messages API, begrenzter Verlauf pro Anfrage
- Spracherkennung über Android `SpeechRecognizer`, lokale TTS-Sprachausgabe, Vordergrunddienst
- Chatverlauf als lokal **verschlüsseltes** Archiv mit Wiederöffnen/Fortsetzen und Löschen
- Sprachbefehle zum Wechseln des Anbieters; Wechsel erstellt absichtlich einen **neuen** Chat, statt Gespräche ungefragt an einen anderen Anbieter zu übermitteln

## Voraussetzungen
- Android Studio **Quail 4** (2026.1.4) oder kompatible Version mit Android Gradle Plugin 9.4
- JDK 17, Android SDK API 37
- Gradle 9.6.0 (wenn keine Wrapper-Skripte bereitgestellt sind, lokal über Android Studio)
- Android 10+ Smartphone (minSdk 29), getestet werden soll insbesondere Pixel 10 Pro / Android 17
- Eigene API-Schlüssel für die gewünschten Anbieter (Abrechnung getrennt von ChatGPT-/Claude-Abos)

## Starten
1. Diesen Ordner in Android Studio öffnen und SDK/Gradle synchronisieren.
2. Falls Android Studio einen Gradle-Wrapper verlangt: mit installiertem Gradle 9.6.0 `gradle wrapper --gradle-version 9.6.0` ausführen (Wrapper-Binärdateien sind hier nicht enthalten).
3. `app` auf einem Gerät ausführen. Mikrofonberechtigung auf Nachfrage erteilen.
4. API-Schlüssel **nur auf dem Gerät** eingeben und speichern; Modell auswählen; Gespräch starten.
5. In einem Chatverlauf auf einen Eintrag tippen, um ihn nach dem nächsten Start fortzusetzen.

## Bekannte Einschränkungen
- **Kein lauffähiges APK mitgeliefert.** Die Quellen konnten in der aktuellen Umgebung ohne Android SDK und Gradle nicht kompiliert werden.
- Noch keine offizielle Anmeldung per „Mit ChatGPT“/Claude-Konto; nur eigene API-Schlüssel.
- Noch kein echtes OpenAI-Realtime-Streaming, keine kontinuierliche Unterbrechung während TTS (kein Barge-in), keine zuverlässige Wake-Word-Erkennung und keine freie Eingabe auf dem Autodisplay.
- Spracherkennung und TTS hängen vom auf dem Gerät installierten Dienst ab; können Netzwerk verwenden und bei gesperrtem Bildschirm eingeschränkt sein.
- API-Schlüssel verbleiben auf dem Gerät, aber Nachrichten werden zur Verarbeitung an den ausgewählten Provider übermittelt.
- Der lokale Chatverlauf erscheint **nicht** in den offiziellen ChatGPT- oder Claude-Apps.
- Die Modellliste ist derzeit statisch, nicht serverseitig abgefragt. Modellzugang/-bezeichnungen ändern sich mit der Zeit.
- Android Auto erlaubt bestimmte App-Kategorien. Die experimentelle Integration wird erst nach gesonderter Erprobung hinzugefügt; bitte keine sichere Car-Display-Kompatibilität erwarten.
- Kein automatisierter Integrationstest auf echter Hardware.

## Nächste Meilensteine
1. Build und Test auf Pixel 10 Pro, Mikrofon, TTS und API-Verbindung validieren.
2. Audiostabilität, Unterbrechen, Wiederverbinden und Zugriff im Hintergrund verbessern.
3. Car-Audio-Routing über USB/AAWireless testen; anschließend separates experimentelles Car-App-Modul.
4. Testabdeckung, CI, datenschutzfreundlicher Export, öffentliche GitHub-Veröffentlichung und Releases.

## Datenschutz und Sicherheit
Der Archivschlüssel wird im Android Keystore verwaltet und das Archiv mit AES-GCM verschlüsselt gespeichert. Auto-Backup der App ist deaktiviert. Verlust des Keystore-Schlüssels kann die lokalen Daten unlesbar machen. Schlüsselspeicherung direkt auf einem Endgerät eignet sich zum privaten Prototypen, für allgemeine Veröffentlichung müssen Bedrohungsmodell und Authentifizierung weiter ausgebaut werden.

## Lizenz
Apache 2.0; siehe [LICENSE](LICENSE).

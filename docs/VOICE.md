# Smartphone-Sprachfunktion – 0.1.8

OpenAI und Claude verwenden jetzt denselben getesteten Gesprächsablauf:
Android erkennt die Sprache, der gewählte Anbieter verarbeitet den erkannten
Text und Android liest die Antwort vor. Das ist eine verkettete Sprachfunktion
mit den vorhandenen Textmodellen, keine native OpenAI-Realtime- oder Claude-Audio-API.
Das Prinzip getrennter Erkennung, Textverarbeitung und Sprachausgabe beschreibt
auch [OpenAI Voice agents](https://developers.openai.com/api/docs/guides/voice-agents).

Die DHU-Test-App bleibt bei Titel, Auswahl und Starten. Sie bekommt weder
Mikrofonzugriff noch Netzwerkanbindung oder eine behauptete neue Auto-Kategorie.

## Stimme auswählen und vergleichen

1. Auf der Smartphone-Hauptseite **„Stimme und Tempo“** öffnen. Während eines
   laufenden Gesprächs ist diese Schaltfläche deaktiviert.
2. Die Stimmwahl öffnen und eine Variante wählen. Das Pixel meldet aktuell
   **fünf Stimmen auf dem Gerät und vier Online-Stimmen für Deutsch (de-DE)**.
   Die Nummern dienen der Orientierung; gespeichert wird die tatsächliche
   Stimmenkennung zusammen mit dem Sprachdienst, nicht die Nummer.
3. **„Hörprobe abspielen“** liest einen festen Beispielsatz ohne Mikrofon,
   Chatdaten oder KI-Anfrage vor. Mit **„Hörprobe stoppen“** jederzeit beenden.
4. Bei Bedarf „Etwas langsamer“, „Normal“ oder „Etwas schneller“ wählen.
   Stimme und Tempo werden sofort nur für OpenAuto AI gespeichert. Die
   systemweiten Android-Einstellungen bleiben unverändert.
5. **„Zurück zum Gespräch“** wählen und ein Gespräch starten. Beide Anbieter
   verwenden dieselbe Auswahl. Beim Verlassen der Einstellungsseite wird die
   Hörprobe gestoppt und ihr Audiofokus freigegeben.

Online-Stimmen benötigen laut Sprachdienst eine Verbindung; beim Auswählen
erscheint ein Hinweis zur Übertragung des vorzulesenden Textes. Die Auswahl
erfolgt ausdrücklich. Auch „Android-Standard“ folgt dem installierten Dienst
und ist keine Offline-Zusage. Es gibt keine zusätzliche OpenAI- oder Claude-
Sprach-API und keine eigene Speicherung von Audio. Androids Sprachdienst kann
seine eigenen Datenverarbeitungsregeln haben.

Auf dem getesteten Pixel sind **2/6, 3/7, 4/8 und 5/9** jeweils lokale/Online-
Varianten mit derselben Basiskennung (`dea`, `deb`, `deg`, `nfh`). Sie können
sehr ähnlich klingen. Ein weiterer Eintrag heißt `de-DE-language`; auch Android-
Standard kann eine der gelisteten Stimmen verwenden. Die Nummern gelten für den
hier geprüften Katalog und können sich nach einem Sprachdienst-Update ändern.
Diese Varianten sind verschiedene vom Dienst gemeldete Konfigurationen, keine
neun garantierten unterschiedlichen Sprecheridentitäten.

Die App verspricht keine bestimmte Klangqualität. Stimmenangebot und Klang
bestimmt der installierte Dienst. Fehlt eine gespeicherte Stimme oder wurde der
Sprachdienst gewechselt, wird vor der Aufnahme ein verständlicher Fehler gezeigt;
bitte eine neue Stimme oder „Android-Standard“ wählen. Es gibt keinen stillen
Wechsel auf eine andere Online-Stimme.

Technische Grundlage: [Android TextToSpeech](https://developer.android.com/reference/android/speech/tts/TextToSpeech)
und [Voice-Metadaten](https://developer.android.com/reference/android/speech/tts/Voice).

## Auf dem Pixel ausprobieren

1. Die Assistenten-App `dev.openautoai.app` öffnen. Sie ist getrennt von der
   DHU-Test-App `dev.openautoai.prototype.dhu`; beide heißen derzeit OpenAuto AI.
2. Anbieter und Modell auswählen. Den eigenen API-Schlüssel ausschließlich
   auf dem Smartphone eintragen und speichern, nicht in Git oder einen Chat.
   Ein vorhandener Schlüssel bleibt gespeichert, obwohl das Eingabefeld leer ist.
3. „Sprachgespräch starten“ antippen. Mikrofon freigeben; Android fragt auf
   neueren Versionen auch nach Benachrichtigungen. Eine abgelehnte Mikrofonfreigabe
   verhindert die Aufnahme. Ohne Benachrichtigungsfreigabe bleibt Beenden in der App möglich.
4. Auf **„Ich höre zu“** warten und eine kurze Frage stellen, etwa „Sag mir in
   einem Satz, was du tun kannst.“ Sobald die Erkennung fertig ist, wird das
   Mikrofon freigegeben und die Anfrage an den ausgewählten Anbieter geschickt.
5. Während der Antwort hört die App nicht zu. Nach dem Vorlesen beginnt der
   nächste kurze Erkennungsdurchlauf. Die Gerätesprache bestimmt Erkennung und TTS.
6. **„Gespräch beenden“** in der App oder **„Beenden“** in der Benachrichtigung
   funktioniert auch während einer Anfrage oder während des Vorlesens.
7. Den zweiten Anbieter separat testen; dabei ein neues Gespräch verwenden.

Androids installierte Sprachdienste können Audio online verarbeiten. Die App
speichert keine Rohaufnahmen; sie speichert den erkannten Text und die Modellantwort
im verschlüsselten lokalen Verlauf. Der gewählte KI-Anbieter erhält den Textkontext.
Dabei können API-Kosten entstehen. Das Stoppen des lokalen Ablaufs ist keine Zusage,
dass ein bereits beim Anbieter eingetroffener Auftrag nicht mehr berechnet wird.
Siehe auch [SpeechRecognizer](https://developer.android.com/reference/android/speech/SpeechRecognizer).

Für bestehende Chats: Chat öffnen, dann „Gespräch fortsetzen“. Der ursprüngliche
Anbieter und das Modell bleiben zugeordnet. Nicht mehr gelistete Modell-IDs bleiben
lesbar, lassen sich aber nicht unbemerkt unter einem anderen Modell fortsetzen.

## Sprachbefehle

| Während „Ich höre zu“ sagen | Ergebnis |
|---|---|
| „Stopp“, „Stop“, „Beenden“, „Gespräch beenden“ | Sitzung beenden, keine Anbieteranfrage |
| „Wechsle zu Claude“ / „Wechsel zu Claude“ | Neues Claude-Gespräch mit dem zuletzt gewählten Claude-Modell |
| „Wechsle zu OpenAI“ / „Wechsel zu OpenAI“ | Neues OpenAI-Gespräch mit dem zuletzt gewählten OpenAI-Modell |
| „Wechsle zu ChatGPT“ / „Wechsel zu ChatGPT“ | Gleichbedeutend mit OpenAI |

Befehle müssen die vollständige Äußerung sein. Großschreibung, mehrfache Leerzeichen
und abschließende Satzzeichen sind unkritisch. Eine längere Frage mit demselben
Anfang wird als Frage behandelt. Beim Wechsel wird der alte Verlauf nicht kopiert;
ohne Schlüssel für den neuen Anbieter wird kein neuer Chat angelegt. Ein Befehl
zum bereits aktiven Anbieter legt ebenfalls keinen neuen Chat an.

Sprachbefehle unterbrechen kein laufendes Vorlesen, weil das Mikrofon dabei pausiert.
Zum sofortigen lokalen Stoppen die Schaltfläche oder Benachrichtigung verwenden.

## Ablauf und Abbrüche

| Zustand | Verhalten |
|---|---|
| Vorbereiten | Chat und Zugangsdaten prüfen; Vordergrunddienst und TTS vorbereiten; keinen Wiedergabefokus anfordern |
| Zuhören | Genau ein kurzer Android-Erkennungsdurchlauf; keine parallele Anbieteranfrage |
| Anbieter antwortet | Nutzernachricht archivieren, gewählten Adapter einmal aufrufen; Mikrofon aus |
| Vorlesen | Antwort archivieren, Wiedergabefokus anfordern und vorlesen; Fokus danach freigeben; lange Texte aufteilen |
| Beendet | Aufnahme, TTS, Zeitgeber und ausstehende lokale Verarbeitung abbrechen; Audiofokus freigeben |

Jeder Schritt hat eine eigene Kennung. Doppelte oder verspätete Rückmeldungen
aus älteren Schritten oder Sitzungen lösen keine neue Aufnahme oder Ausgabe aus.
Jeder Worker-Auftrag erhält eine eigene Abbruchquelle. Sie wird durch Chat-Anfrage
und Anbieteradapter bis zum konkreten OkHttp-Aufruf weitergereicht. Beenden ruft
`Call.cancel()` auf, unterbricht den Worker und unterdrückt seine spätere Rückmeldung.
Der Abbruch eines alten Auftrags betrifft keine neue Anfrage. Vor einer anschließenden
Archivierung wird erneut geprüft. Ein bereits begonnener atomarer Schreibvorgang
kann noch abschließen; beim Anbieter bereits eingetroffene Verarbeitung und Kosten
lassen sich lokal nicht zurücknehmen. System-DNS kann ebenfalls weiter blockieren.
Netzwerkzeitlimits: Verbindung 15 s, Lesen/Schreiben je 50 s, gesamter Aufruf 60 s.

Es gibt keine automatische Wiederholung kostenpflichtiger KI-Anfragen und keinen
automatischen Anbieterwechsel. Fehler erscheinen als feste Hinweise. Bei fehlendem
TTS, Mikrofon-/Audiofokusproblemen oder API-/Speicherfehlern endet die Sitzung.
Nach zwei erfolglosen Erkennungsdurchläufen mit Stille/leerem Ergebnis endet sie
ebenfalls; für einen hängenden Durchlauf gilt zusätzlich ein Zeitlimit. Es gibt
keinen endlosen Hintergrund-Erkennungszyklus. Eine Sitzung endet spätestens nach
zehn Minuten und kann ausdrücklich erneut gestartet werden.

Zeitlimits: Chat laden/Anbieter wechseln 15 s, TTS vorbereiten 12 s, einzelner
Erkennungsdurchlauf 45 s, Anfrage einschließlich Speicherung 70 s, Vorlesen 120 s.
Beim Entfernen der App aus der Übersicht beendet Android auch den Sprachdienst.
Während des Vorlesens beendet ein verlorener oder verweigerter Audiofokus das Gespräch.
Bei Vorbereitung, Zuhören und API-Wartezeit beansprucht die App keinen eigenen
Wiedergabefokus. Der installierte Erkennungsdienst verwaltet seinen Fokus selbst.

Android verlangt Erkennungsaufrufe auf dem Hauptthread und Freigabe per `destroy()`.
Die Implementierung erstellt für jeden Erkennungsdurchlauf eine eigene Instanz und
gibt sie danach frei. [SpeechRecognizer](https://developer.android.com/reference/android/speech/SpeechRecognizer)
TTS wird erst nach erfolgreicher Initialisierung und Sprachprüfung verwendet;
einzigartige Äußerungskennungen schützen vor veralteten Fertig-Rückmeldungen.
[TextToSpeech](https://developer.android.com/reference/android/speech/tts/TextToSpeech)

## Unterbrechungen in 0.1.8

Eine laufende Sitzung endet in jeder Phase (Vorbereiten, Zuhören, Anfrage, Vorlesen), wenn:

- das Display ausgeschaltet wird oder die App ein gesperrtes Gerät erkennt;
- Android einen Ruf-/Telefonie-/Kommunikationsmodus meldet;
- ein externes Audio-Ausgabegerät angeschlossen oder getrennt wird, etwa ein
  Bluetooth-, USB- oder Kabel-Headset.

Danach erscheinen ein fester Hinweis und „Gespräch beendet“. Es gibt keine automatische
Wiederaufnahme beim Entsperren, Auflegen oder erneuten Verbinden. Zum Fortsetzen
entsperren und das Gespräch ausdrücklich erneut starten. Ein bereits gesperrtes
Gerät oder gemeldeter Anruf blockiert auch den Start.

Die Beobachtung läuft nur während einer Sitzung und wird beim Beenden entfernt.
Android 12+ meldet Audio-Moduswechsel direkt; zusätzlich wird alle 500 ms der Zustand
geprüft, auch für Android 10/11 und Gerätesperre. Initiale Geräteauflistungen und
veraltete Rückmeldungen gelten nicht als neue Unterbrechung. Die App fordert weder
Telefonstatus-/Anrufdaten noch Bluetooth-Steuerrechte an und ändert kein Audio-Routing.
Grundlagen: [AudioManager](https://developer.android.com/reference/android/media/AudioManager),
[Display-aus-Signal](https://developer.android.com/reference/android/content/Intent#ACTION_SCREEN_OFF).

Grenzen: Die App beobachtet angeschlossene Ausgabegeräte, nicht jeden internen
Routingwechsel zwischen bereits verbundenen Geräten. Eine Telefonie-/VoIP-App,
die keinen passenden Audiomodus meldet, ist darüber nicht garantiert erkennbar.
Audiofokusverlust während des Vorlesens und die Fehler der Erkennung bleiben
zusätzliche Stoppsignale. Stoppen/Neustarten per Schaltfläche und Beenden bei
Display-aus hat der Nutzer für beide Anbieter praktisch bestätigt. Physische
Anrufe und Bluetooth-/Headset-Wechsel sind auf Wunsch des Nutzers zurückgestellt
und weiterhin ungeprüft; simulierte Signale allein bestätigen diese Fälle nicht.

### Gezielter manueller Abnahmetest

Für OpenAI und Claude jeweils mit eigenem Schlüssel in der App testen (API-Kosten
möglich; keine Schlüssel in Logs oder Chat kopieren):

1. Eine kurze Frage stellen und während „Anbieter antwortet“ beenden. Es darf
   keine verspätete Antwort vorgelesen werden. Danach neu starten; die neue
   Sitzung muss normal reagieren. Auch Beenden während Zuhören und Vorlesen prüfen.
2. Während einer Sitzung das Display ausschalten, danach entsperren. Das Gespräch
   muss beendet bleiben und darf erst nach erneutem Start weiterlaufen.
3. **Zurückgestellt auf Nutzerwunsch (12.10.2026), für einen späteren Test:**
   Mit einem Headset eine Sitzung beginnen und das Headset trennen/verbinden.
   Es muss beendet bleiben. Einen gewöhnlichen eingehenden Anruf separat prüfen;
   keine Testanrufe oder Anrufannahme durch die App selbst.

## Architektur

- `voice`: plattformunabhängige Gesprächssteuerung, Sprach-/Worker-Schnittstellen,
  Archiv-/Anbieteranbindung, Fokus-Lebensdauer beim Vorlesen und TTS-Textaufteilung.
- `app/voice`: Android SpeechRecognizer, TTS, Audiofokus, Hauptthread-Zeitgeber und
  abbrechbare Hintergrundaufgaben.
- `VoiceService`: Mikrofon-Vordergrunddienst und Beenden-Benachrichtigung. Er wird
  vor asynchroner Initialisierung als Vordergrunddienst angemeldet und nicht
  automatisch nach Prozessende neu gestartet.
- `providers`, `storage`, `settings`: vorhandene Adapter und Speicherbausteine.
  Modellkatalog, API-Protokolle, Ausgabe-Limit und Archiv-/Schlüsselkennungen bleiben gleich.

Der Dienst beginnt nur nach dem sichtbaren Start auf dem Smartphone. Die
Mikrofonberechtigung und Androids Einschränkungen für Vordergrunddienste bleiben
aktiv. Die App fordert kein Umgehen dieser Einschränkungen an.
[Mikrofon-Vordergrunddienste](https://developer.android.com/develop/background-work/services/fgs/service-types#microphone)

Speicherzugriffe des Sprachablaufs laufen im Hintergrund. Die übrige bestehende
Verlaufs-/Schlüsseloberfläche arbeitet noch synchron. Die zwei Worker bleiben
erhalten; Netzwerkabbruch erfolgt jetzt pro Aufruf. Anbieter-seitige Verarbeitung
und Abrechnung werden dadurch nicht garantiert gestoppt.

## Bauen und gezielt öffnen

```powershell
Set-Location -LiteralPath 'C:\Users\stefa\Documents\Projekte\Azentrius\OpenAuto AI'
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat :app:assembleDebug
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
& $adb -s 56161FDCH000H8 install -r '.\app\build\outputs\apk\debug\app-debug.apk'
& $adb -s 56161FDCH000H8 shell am start -n 'dev.openautoai.app/dev.openautoai.app.MainActivity'
```

Für die Download-APK den Pfad auf `OpenAutoAI-0.1.8-assistant-debug.apk` ändern.
Bei anderen Geräten die Seriennummer ersetzen. Bei abweichender Signatur nicht
deinstallieren oder Daten löschen, sondern denselben Debug-Schlüssel verwenden.
Die bestehenden [DHU-Schritte](START_WINDOWS.md) bleiben gültig.

## Korrektur des sofortigen Abbruchs in 0.1.6

Auf dem Pixel forderte `dev.openautoai.app` am 12.10.2026 um 00:58:52.043
Wiedergabefokus an. Um 00:58:52.627 forderte der gestartete Google-Sprachdienst
(`com.google.android.tts`) eigenen exklusiven Fokus an. OpenAuto AI erhielt dabei
`AUDIOFOCUS_LOSS_TRANSIENT` und beendete die gerade gestartete Erkennung.
Dieses Muster wiederholte sich bei weiteren Versuchen. Der Paketname des
Google-Dienstes ist hier kein Nachweis für bereits laufendes Vorlesen; der
Aufnahmeverlauf meldete `VOICE_RECOGNITION`.

Die App fordert deshalb erst unmittelbar zum Vorlesen eigenen Fokus an und gibt
ihn vor dem nächsten Erkennungsdurchlauf frei. Bei Fokusverweigerung wird nichts
abgespielt. Echter Fokusverlust während der Ausgabe stoppt sie weiterhin; es gibt
kein automatisches Wiederanlaufen. Veraltete Fokus-Rückmeldungen wirken nicht auf
einen späteren Durchlauf. Das folgt Androids Empfehlung, Fokus direkt vor der
Wiedergabe anzufordern und danach abzugeben:
[Audiofokus](https://developer.android.com/media/optimize/audio-focus).

## Teststand und Grenzen

**90 automatisierte Prüfungen bestehen:** 69 JVM-Tests, 14 Assistant-Gerätetests
und 7 DHU-/Auswahltests. 0.1.8-dev / Code 9 ist als Update auf dem Pixel installiert.
Die Tests prüfen Socket-Abbruch, Abbruch-/Neustart-Rennen, alle Gesprächsphasen,
Unterbrechungsregeln und die Registrierung/Freigabe echter Android-Beobachter.
Keine hörbare Ausgabe, Mikrofonaufnahme oder echte KI-Anfrage wird automatisiert
gestartet; persönliche Daten und gewählte Stimme werden nicht verändert.

Der Nutzer bestätigte Erkennung, Vorlesen und Beenden für beide Anbieter unter
0.1.6 und einen deutlich besseren Klang mit der Stimmwahl in 0.1.7. Am 12.10.2026
bestätigt er für 0.1.8 Stoppen und Neustarten per Schaltfläche sowie Beenden bei
Display-aus für OpenAI und Claude. Anruf- und Headset-/Bluetooth-Tests sind
auf Wunsch des Nutzers zurückgestellt und bleiben ungeprüft;
die Rückmeldung bestätigt nicht jede mögliche zeitliche Überschneidung separat.
Android-Auto-Audio und reale Fahrzeugnutzung sind nicht bestätigt.
Die DHU-Oberfläche bleibt bei 0.1.2-prototype. Details: [BUILD_AND_TEST.md](BUILD_AND_TEST.md).

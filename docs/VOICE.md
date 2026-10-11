# Smartphone-Sprachfunktion – 0.1.6

OpenAI und Claude verwenden jetzt denselben getesteten Gesprächsablauf:
Android erkennt die Sprache, der gewählte Anbieter verarbeitet den erkannten
Text und Android liest die Antwort vor. Das ist eine verkettete Sprachfunktion
mit den vorhandenen Textmodellen, keine native OpenAI-Realtime- oder Claude-Audio-API.
Das Prinzip getrennter Erkennung, Textverarbeitung und Sprachausgabe beschreibt
auch [OpenAI Voice agents](https://developers.openai.com/api/docs/guides/voice-agents).

Die DHU-Test-App bleibt bei Titel, Auswahl und Starten. Sie bekommt weder
Mikrofonzugriff noch Netzwerkanbindung oder eine behauptete neue Auto-Kategorie.

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
Worker-Aufgaben werden unterbrochen; späte Ergebnisse werden verworfen und vor
einer anschließenden Archivierung erneut auf Abbruch geprüft. Ein bereits
begonnener atomarer Schreibvorgang kann noch abschließen. Ein blockierender
HTTP-Aufruf lässt sich mit dem vorhandenen Transport nicht garantiert sofort
beenden; Verbindungs-/Lese-Timeouts bleiben 15/50 Sekunden.

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
Verlaufs-/Schlüsseloberfläche arbeitet noch synchron. Die beiden Worker verhindern,
dass ein abbrechender alter Auftrag direkt jeden neuen Schritt blockiert; daraus
folgt keine Garantie für sofortigen Abbruch auf Transport- oder Anbieterseite.

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

Für die Download-APK den Pfad auf `OpenAutoAI-0.1.6-assistant-debug.apk` ändern.
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

57 JVM-Tests bestehen, darunter 13 neue Regressionstests für die Fokus-Lebensdauer
und den Gesprächsablauf beider Anbieter. Diese Prüfungen verwenden künstliche
Antworten und Audio-/Fokus-Simulationen, keine echte Mikrofonaufnahme oder TTS-Ausgabe.
Die Assistenten-App **0.1.6-dev / Code 7** wurde als Update auf dem Pixel 10 Pro / Android 17 installiert; die Paketversion und ein erfolgreicher Kaltstart sind geprüft. **14 Gerätetests bestehen erneut** (7 Assistant, 7 DHU/Auswahl), insgesamt mit den JVM-Tests **71 automatisierte Prüfungen**. Bestehende App-Daten wurden nicht gelöscht.

Android-Auto-Audio, Bluetooth-Routing, gesperrtes Gerät und reale Fahrzeugnutzung
sind nicht bestätigt. Die experimentelle Car-App-Oberfläche bleibt ein UI-Test.
Details und Wiederholung: [BUILD_AND_TEST.md](BUILD_AND_TEST.md).

**Manueller Sprachtest:**
0.1.5 scheiterte beim Nutzer wiederholt mit „Ein anderer Audiovorgang hat Vorrang“. Die Ursache ist im Geräteprotokoll nachvollzogen und in 0.1.6 korrigiert. Ein neuer praktischer Sprachtest mit OpenAI und Claude steht noch aus; Erkennung, hörbare Antwort und Beenden sind für 0.1.6 noch nicht vom Nutzer bestätigt.

# OpenAI-/Claude-Adapter – Projektstand 0.1.12

## Verhalten und Grenzen

Die bestehende Smartphone-Assistenten-App `dev.openautoai.app` verwendet jetzt
eine gemeinsame `ChatClient`-Schnittstelle. Der gewählte Anbieter und die vorhandene
Modell-ID bestimmen genau einen API-Adapter. Es gibt keinen automatischen Wechsel
des Anbieters, keinen automatischen Wiederholungsversuch und keine automatische
Fortsetzung mit einem höheren Tokenbudget.

Die fünf Modell-IDs, deren Defaults und die gespeicherte Auswahl aus 0.1.2 bleiben
unverändert. Das bisherige Ausgangslimit von 350 Ausgabetokens bleibt erhalten.
Bei OpenAI zählt dazu auch gegebenenfalls verwendetes Reasoning; das Limit ist
deshalb keine Garantie für eine vollständige sichtbare Antwort. Eine Teilantwort
wird vor dem Vorlesen als gekürzt gekennzeichnet. Ohne Text erscheint ein fester
Fehlerhinweis. Der lokale Hinweis wird nicht als Modellantwort im Archiv abgelegt.
[OpenAI Responses-Referenz](https://developers.openai.com/api/reference/resources/responses/methods/create)

API-Fehler werden als feste deutsche Hinweise ausgegeben: fehlender/ungültiger
Schlüssel, abgelehnte Anmeldung, fehlende Berechtigung, nicht gefundenes Modell,
Rate-Limit, Guthaben/Nutzungslimit, Serverfehler, Zeitüberschreitung und Netzwerkfehler.
Rohe Server-Fehlermeldungen und Ausnahmeursachen werden weder angezeigt noch vorgelesen.
Der neue Sprachablauf beendet die Sitzung bei Fehlern und zeigt den Hinweis auf dem Smartphone.
HTTP-404 wird bewusst als „Modell oder API-Endpunkt nicht gefunden“ formuliert;
die genaue Ursache kann ohne echten Kontotest nicht zugesichert werden.

## Module

| Baustein | Aufgabe |
|---|---|
| `core` | `ChatClient`, `ChatRequest`, `ChatMessage`, `ChatReply`, `Completion`, `CredentialSource`, `ChatFailure` |
| `providers` | `OpenAiAdapter`, `ClaudeAdapter`, `ProviderChatClient`, `HttpTransport`, `OkHttpTransport` |
| `app/ChatApi` | Weiterhin vorhandene Kompatibilitätsbrücke vom bestehenden `Turn`-Format und `CredentialStore` zur gemeinsamen Schnittstelle |
| `voice` / `app/VoiceService` | Gemeinsamer Ablauf; speichert Antworttext, liest Modellantworten vor und zeigt feste Fehlerhinweise |
| `settings`, `prototype-app`, `car-ui` | Keine neue API-Abhängigkeit; DHU bleibt Auswahl-/UI-Test |

`providers` ist ein Kotlin/JVM-Modul. Android liefert `org.json` zur Laufzeit;
die JVM-JSON-Bibliothek ist nur `compileOnly` und eine Testabhängigkeit. Ein zusätzlicher
JSON-JAR wird nicht in die APK eingebaut. Zwei Gerätetests prüfen die tatsächliche
Android-JSON-Implementierung und die Brücke vom alten Turn-Format.

## Protokolle

- OpenAI: `POST https://api.openai.com/v1/responses`, Bearer-Schlüssel,
  `instructions`, `input`, `model`, `max_output_tokens=350`, `store=false`.
  Nur Assistant-Text bzw. explizite Ablehnungstexte werden als Antwort verwendet.
  Reasoning-Inhalte werden nicht vorgelesen. Status `incomplete` wird ausgewertet.
  `store=false` deaktiviert die Abrufspeicherung der Response; daraus folgt keine
  Zusage über sämtliche Aufbewahrungsregeln beim Anbieter.
  [Responses-Referenz](https://developers.openai.com/api/reference/resources/responses/methods/create)
- Claude: `POST https://api.anthropic.com/v1/messages`, `x-api-key`,
  `anthropic-version: 2023-06-01`, `system`, `messages`, `model`, `max_tokens=350`.
  Textblöcke werden ausgewertet; Thinking-Blöcke werden nicht vorgelesen.
  `stop_reason` unterscheidet vollständige, abgeschnittene und abgelehnte Antworten.
  Unangeforderte Tool-Abläufe werden nicht ausgeführt.
  [Messages-Referenz](https://platform.claude.com/docs/en/api/messages/create),
  [Stop-Gründe](https://platform.claude.com/docs/en/build-with-claude/handling-stop-reasons)

Der Transport nutzt OkHttp 5.3.2 und akzeptiert weiterhin nur die beiden festen
HTTPS-Endpunkte. Weiterleitungen und automatische Verbindungswiederholungen sind
deaktiviert. Der Request-Body ist außerdem `isOneShot`, damit z. B. `503` mit
`Retry-After: 0` keine zweite kostenpflichtige Anfrage auslöst. Keine HTTP-Caches.
Verbindungs-/Lese-/Schreib-/Gesamtzeitlimit: 15/50/50/60 Sekunden; Antworten maximal
1 MiB. Der pro Auftrag durchgereichte Abbruch ruft `Call.cancel()` auf und löst
blockierte Socket-Zugriffe; Streams und Abbruchregistrierung werden freigegeben.
Abbruch wird als CANCELLED statt als Verbindungsfehler behandelt. Die Tests nutzen
auch echte lokale Sockets für ausbleibende Header und teilweise gelieferte Antworten.
Bereits begonnene Serververarbeitung oder Abrechnung wird damit nicht zurückgenommen.
System-DNS und andere Betriebssystemoperationen sind nicht garantiert sofort
unterbrechbar. Siehe [VOICE.md](VOICE.md) und den
[Call-Vertrag](https://github.com/square/okhttp/blob/parent-5.3.2/okhttp/src/commonJvmAndroid/kotlin/okhttp3/Call.kt).

Die Fehlerklassifikation nutzt HTTP-Status und bekannte maschinenlesbare Codes,
keine Interpretation beliebiger Servertexte. OpenAI-Guthaben-/Kontolimits werden
von einem temporären 429-Rate-Limit unterschieden. Claude-402 wird als Abrechnungs-
problem behandelt; 400 kann verschiedene Ursachen haben und bleibt ein allgemeiner
Anfragefehler.
[OpenAI-Fehlercodes](https://developers.openai.com/api/docs/guides/error-codes),
[Claude-Fehlercodes](https://platform.claude.com/docs/en/api/errors)

## Daten und Testumfang

Der Client speichert selbst keinen Verlauf. Er liest ausschließlich den Schlüssel
für den ausdrücklich gewählten Anbieter und sendet ausschließlich den übergebenen
Gesprächskontext. Der bestehende Voice-Anbieterwechsel beginnt weiterhin einen
neuen Chat. Archivformat, Keystore-Aliase, Dateinamen und Schlüsselkennungen ändern
sich nicht; es findet keine Datenmigration statt. Die Implementierung liegt seit
0.1.4 im eigenen [Speichermodul](LOCAL_STORAGE.md).

Alle Adaptertests nutzen erfundene Schlüssel und simulierte Antworten. Es wurden
keine echten Zugangsdaten gelesen, keine Anbieter-Endpunkte aufgerufen und keine
Mikrofonaufnahme gestartet. Diese Tests belegen kein tatsächliches Kontoguthaben,
keinen Modellzugriff und keinen erfolgreichen Voice-End-to-End-Ablauf.

## Manueller Test der Assistenten-App

1. `app:debug` installieren. Das Paket heißt `dev.openautoai.app`, während der
   DHU-Test weiterhin `dev.openautoai.prototype.dhu` heißt. Beide tragen derzeit
   den Anzeigenamen OpenAuto AI; im Zweifel per ADB gezielt öffnen (siehe unten).
2. Auf dem Smartphone unter „Einstellungen“ Anbieter und Modell wählen. Einen eigenen API-Schlüssel
   ausschließlich dort eingeben. Beim Start eines Gesprächs werden Mikrofon
   und echte API-Nutzung aktiv; dabei können die normalen API-Kosten entstehen.
3. Eine kurze Frage stellen. Antwort, Verlauf, Stoppen und eventuelle Hinweise prüfen.
4. Gespräch beenden. Den anderen Anbieter separat wählen und testen. Vorhandene
   Chats müssen bei ihrem Anbieter und Modell bleiben.

Der aktuelle Rückmeldungsstand zum echten API-/Sprachtest steht in [VOICE.md](VOICE.md).
Der DHU-Starten-Button löst auch nach diesem Schritt weiterhin keine API-Anfrage aus.

```powershell
Set-Location -LiteralPath 'C:\Users\stefa\Documents\Projekte\Azentrius\OpenAuto AI'
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat :app:assembleDebug
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb -s 56161FDCH000H8 install -r '.\app\build\outputs\apk\debug\app-debug.apk'
& $adb -s 56161FDCH000H8 shell am start -n 'dev.openautoai.app/dev.openautoai.app.MainActivity'
```

Für das Download-APK den Installationspfad durch den vollständigen Pfad zu
`OpenAutoAI-0.1.9-assistant-debug.apk` ersetzen. Andere Geräte: Seriennummer mit
`adb devices -l` ermitteln. Bei Signaturkonflikten nicht automatisch deinstallieren,
da das gespeicherte Daten löschen würde; denselben Debug-Schlüssel verwenden.

Die bestehenden [DHU-Schritte](START_WINDOWS.md) bleiben gültig.

Seit 0.1.12: [Deutsch/Englisch und Wiederaufnahme](LOCALIZATION.md).

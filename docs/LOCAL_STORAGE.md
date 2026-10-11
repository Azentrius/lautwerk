# Lokale Chats und API-Schlüssel – 0.1.4

Die vorhandene Speicherung aus 0.1.0–0.1.3 ist jetzt ein eigener Baustein.
Die Smartphone-Oberfläche, die Anbieter-Brücke und der Sprachdienst verwenden
gemeinsame Schnittstellen. Es gibt keine Datenmigration und keinen neuen Cloud-Speicher.
Die DHU-Oberfläche bleibt beim bestätigten Stand 0.1.2-prototype.

## Aufbau und Kompatibilität

| Baustein | Aufgabe |
|---|---|
| `core/storage` | `Conversation`, `Turn`, `ConversationRepository`, `CredentialStore` und feste Speicherfehler |
| `storage` | Android Keystore, AES/GCM, atomare Archivdatei, verschlüsselte Zugangsdaten und gemeinsame Sperren |
| `app` | Smartphone-/Voice-Anbindung; die bisherigen Namen `ConversationStore` und `SecretStore` bleiben als Typalias erhalten |
| `prototype-app`, `car-ui` | Keine Abhängigkeit von `storage` oder `providers`; weiterhin nur Auswahl-/UI-Test |

Die folgenden Kennungen und Daten bleiben identisch:

| Daten | Bestehendes Format |
|---|---|
| Chatdatei | `files/conversation_archive.dat` |
| Archivschlüssel | Android-Keystore-Alias `openautoai-chat-archive-v1` |
| Zugangsdaten | SharedPreferences `api_credentials`, Einträge `openai` und `anthropic` |
| Zugangsschlüssel | Android-Keystore-Alias `openautoai-key-v1` |
| Verschlüsselter Inhalt | 12 Byte IV, danach AES/GCM-Chiffrat einschließlich 128-Bit-Tag; Zugangsdaten zusätzlich Base64 |
| JSON-Archiv | `id`, `title`, `provider`, `model`, `updated`, `turns`; Nachrichten mit `role` und `content` |

`OPENAI` und `CLAUDE` sowie die Modell-ID jedes Chats bleiben erhalten. Der
Kompatibilitätstest erzeugt Daten mit einer unabhängigen Nachbildung des alten
Codecs, liest und ergänzt sie mit dem neuen Modul und liest das Ergebnis wieder
mit dem alten Codec. Das testet Formatkompatibilität mit künstlichen Daten;
persönliche Chats und Schlüssel des Nutzers wurden dafür nicht ausgelesen.

## Verbessertes Verhalten

- Mehrere Speicherinstanzen verwenden dieselbe Sperre pro Datei bzw. Zugangsdatenablage.
  Gleichzeitiges Lesen, Ändern und Schreiben innerhalb des App-Prozesses verliert
  dadurch keine Änderung einer anderen Instanz.
- Eine beschädigte oder unlesbare Chatdatei wird nicht als leer interpretiert.
  Die Oberfläche zeigt einen Hinweis und „Verlauf erneut laden“; Starten bleibt
  bis zum erfolgreichen Lesen gesperrt. Mutationen überschreiben keine unlesbare Datei.
- Ein alter `.bak`-Stand wird über `AtomicFile.openRead()` wiederhergestellt,
  auch wenn die Hauptdatei fehlt. Abgebrochene Schreibvorgänge werden zurückgerollt.
- Das Lesen erzeugt niemals einen neuen Keystore-Schlüssel. Fehlt der Schlüssel
  zu vorhandenen Daten, erscheint ein Fehler. Ein fehlender API-Schlüssel und
  ein unlesbarer gespeicherter Schlüssel werden unterschiedlich behandelt.
- Zugangsdaten werden mit geprüftem `commit()` gespeichert. Ein Fehlschlag wird
  angezeigt; ein bereits veränderter Eintrag wird nach einem negativen Rückgabewert
  nach Möglichkeit wiederhergestellt. Ein voller oder defekter Datenträger kann
  auch diese Wiederherstellung verhindern; Erfolg wird dann nicht zugesichert.
- Bei alten Modell-IDs außerhalb des heutigen Katalogs bleibt der Chat lesbar.
  Fortsetzen ist gesperrt; es wird nicht unbemerkt ein anderes Modell verwendet.
- Schlägt das Archivieren einer neuen Nutzernachricht fehl, wird sie nicht in
  den laufenden Anfragekontext aufgenommen und nicht an den Anbieter gesendet.

Die Synchronisierung ist erforderlich, weil Androids `AtomicFile` selbst keine
Sperren für gleichzeitige Zugriffe bereitstellt.
[AtomicFile-Dokumentation](https://developer.android.com/reference/android/util/AtomicFile)
Die synchrone Rückmeldung von `commit()` wird bewusst benötigt.
[SharedPreferences.Editor](https://developer.android.com/reference/android/content/SharedPreferences.Editor)
Die Verschlüsselung nutzt weiterhin den Android Keystore.
[KeyGenParameterSpec](https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec)

## Auf dem Pixel prüfen

0.1.4-dev wird als Update von `dev.openautoai.app` installiert. Die DHU-Test-App
ist ein anderes Paket mit eigenem Speicher. Bei einem Signaturkonflikt nicht
deinstallieren oder App-Daten löschen, sondern denselben Debug-Schlüssel verwenden.

```powershell
Set-Location -LiteralPath 'C:\Users\stefa\Documents\Projekte\Azentrius\OpenAuto AI'
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
& $adb -s 56161FDCH000H8 install -r '.\app\build\outputs\apk\debug\app-debug.apk'
& $adb -s 56161FDCH000H8 shell am start -n 'dev.openautoai.app/dev.openautoai.app.MainActivity'
```

Für die Download-APK den Installationspfad durch den vollständigen Pfad zu
`OpenAutoAI-0.1.4-assistant-debug.apk` ersetzen. Auf einem anderen Gerät dessen
Seriennummer aus `adb devices -l` verwenden.

Vorhandene Chats lassen sich ohne API-Aufruf öffnen und lesen. Zum späteren
echten API-/Sprachtest siehe [PROVIDER_ADAPTERS.md](PROVIDER_ADAPTERS.md).
Ein API-Schlüssel gehört ausschließlich in das Eingabefeld der Smartphone-App,
nicht in Git, Diagnoseausgaben oder diesen Chat. Die Eingabe wird nach erfolgreichem
Speichern geleert. Anbieterwechsel beginnen weiterhin einen separaten Chat.

Die automatisierten Speichertests verwenden eine eigene Test-App, zufällige
Testdateien und eigene Keystore-Aliase. Sie löschen ausschließlich ihre eigenen
Testdaten. Die drei neuen Oberflächentests verwenden injizierte Speicherattrappen:
keine echten Schlüssel, Chatdaten, Sprachaufnahme oder Anbieteranfrage.

## Grenzen und nächster Schritt

Die Sperren gelten innerhalb eines Prozesses. Mehrprozesszugriffe werden nicht
unterstützt. Die Schnittstellen arbeiten synchron, und die bestehende Oberfläche
sowie Teile des Sprachdiensts greifen weiterhin auf dem Hauptthread darauf zu.
Das gesamte Archiv wird eingelesen und bei Änderungen neu verschlüsselt. Sehr
große Verläufe können deshalb verzögern; Hintergrundzugriff und eine begrenzte
Verlaufsansicht sind spätere Arbeiten.

Der Verlust eines Keystore-Schlüssels macht die damit verschlüsselten Daten
unlesbar. Es gibt hier noch keinen Export, Import oder Wiederherstellungsassistenten.
Kein automatisches Löschen als Fehlerbehebung. Backup-/Gerätetransfer-Regeln
bleiben ein offener Punkt des Prototyps. Die vorhandene `allowBackup=false`-Angabe
ersetzt keinen geprüften Ablauf für Gerätewechsel.

Ein kompletter Sprachdialog mit echtem API-Konto sowie Mikrofon-/TTS-Lebenszyklus
und sofortiger Netzwerkabbruch sind weiterhin nicht getestet. Die geprüfte lokale
Speicherung ist die Grundlage für die nächste Voice-Überarbeitung.
Alle Ergebnisse: [BUILD_AND_TEST.md](BUILD_AND_TEST.md).

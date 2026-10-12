# Build- und Testbericht 0.1.12

Stand: 12.10.2026. **157 verschiedene automatisierte Prüfungen bestanden:**
22 Core-JVM, 21 Anbieter-JVM, 45 Gesprächs-JVM, 61 Assistant-Gerätetests und
8 DHU-/Auswahltests. Assistant **0.1.12-dev / Code 13** und DHU-Test-App
**0.1.12-prototype / Code 4** als Updates auf Pixel 10 Pro / Android 17 installiert.
Assistant geöffnet, installierte Version und aktive MainActivity geprüft.

## Änderungen

Deutsch/Englisch entsprechend Androids Sprachpräferenzen, Englisch als Rückfall.
„Gespräch starten“ ersetzt „Sprachgespräch starten“. Oberflächen, feste Meldungen,
Hörprobe, Anweisungen an beide Anbieter und Exportbeschriftungen folgen der Sprache.
Stimmwahl wird je Sprache bewahrt; vorhandene Auswahl wird über Android-Metadaten
zugeordnet. Bestehende Chats und Schlüssel bleiben im gleichen verschlüsselten Format.

Startaufträge werden nur in einer fortgesetzten Oberfläche verbraucht. Abbruch und
verspätete Berechtigungsrückmeldung können kein neues Gespräch autorisieren.
Ein Prozessneustart stellt keinen Startauftrag aus einem Bundle wieder her.
Navigation und Nachrichtenposition überstehen die Neuerstellung der Oberfläche.

## Ergebnisse

| Prüfung | Ergebnis |
|---|---|
| Core-JVM | 22 bestanden, einschließlich Sprachreihenfolge, Rückfall, dynamischer Fehlermeldungen und Exportlabels ohne Änderung der Inhalte |
| Anbieter-JVM | 21 bestanden; beide Sprachvarianten in Requests beider Anbieter, gleiches Modell und Antwortbudget, keine echte API-Anfrage |
| Gesprächs-JVM | 45 bestanden; bestehende Abbruch-/Unterbrechungsprüfungen und englischer Anbieterwechsel/Ende ohne Weiterleitung als Benutzerfrage |
| Assistant-Gerätetests | 61 verschiedene Tests bestanden, siehe Wiederholung unten |
| DHU-/Auswahltests | 8 bestanden; bisherige deutsche Prüfungen plus englisches Template mit Titel, Auswahl und funktionierender Start-Aktion |
| Android-Lint | 0 Fehler, 11 Warnungen und 1 KTX-Hinweis; eine zusätzliche Empfehlung zum Preferences-KTX-Stil gegenüber 0.1.11 |
| Visuelle Prüfung | Deutsch und Englisch mit ausschließlich erfundenen Chats; Beschriftungen und feste Startsteuerung sichtbar |

Der erste Assistant-Lauf bestand 52 von 61 Tests. Neun bisherige UI-Tests suchten
noch die alten deutschen internen Seitenkennungen. Nach Umstellung der betroffenen
Testaufrufe bestand die gesamte betroffene Klasse `StorageUiTest` mit 11 Tests.
Die Produktions-App wurde zwischen diesen Läufen nicht verändert. Es handelt sich
um 61 verschiedene bestandene Tests, nicht um 72 verschiedene Prüfungen.

Die neuen vier `LocalizationLifecycleTest`-Prüfungen bestehen: reale Activity-
Neuerstellung mit behaltenem ViewModel, Sprachwechsel mit Suchtext/Filter,
erhaltener älterer Nachricht und Scrollposition, Hintergrundwechsel während eines
langsamen Starts und einmaliger anschließender Start. Ein separater Debug-Testhost
verwendet die echte MainContent/HomeViewModel-Anbindung, ausschließlich Speicherattrappen
und einen Startzähler; er kann keinen Sprachdienst oder Anbieteraufruf starten.
Er ist nicht exportiert und nicht in Release-Builds enthalten.

Die drei Start-Gate-Tests prüfen Abbruch/verspätete Freigaben, Vordergrund-/Mikrofon-
Voraussetzungen, optionale Benachrichtigungsfreigabe sowie einen frischen Prozesszustand.
Der Stimmtest prüft die Übernahme einer alten deutschen Auswahl und eine davon
unabhängige englische Auswahl. Keine persönlichen Chats oder Schlüssel ausgelesen,
keine Aufnahme oder hörbare TTS gestartet. Die Gerätesprache blieb unangetastet.

## Grenzen und Praxistest

Der Nutzer bestätigt am 12.10.2026 für 0.1.12 nach dem Testauftrag mit Starten,
Antwort anhören und Beenden: **„Funktioniert bei beiden einwandfrei.“** Für den
Gerätesprachwechsel Englisch/Deutsch einschließlich Rückkehr zur deutschen
Stimmwahl lautet die Rückmeldung: **„Deutsch und Englisch funktionieren.“**
Ein vollständiges englischsprachiges Gespräch wurde nicht gesondert bestätigt.

Activity-Neuerstellung im Test ersetzt keinen vollständigen Prozessabbruch mit
offenem Systemdialog. Echtes englisches Vorlesen, Cloud-Ziele und Rotation im
externen Export-Dateidialog sind nicht automatisiert abgenommen. Die Sprachauswahl
des Modells ist nur anhand ausgehender Test-Requests geprüft.

**Anruf- und Headset-/Bluetooth-Praxistests bleiben zurückgestellt.** Eine neue
reale DHU-Verbindung und Auto-Audio wurden nicht geprüft. DHU-Layout/Aktionen,
Kategorie-Platzhalter und Hostprüfung bleiben erhalten; keine Zulassungsbehauptung.
Die DHU-Versionsnummer steigt wegen der zusätzlichen Übersetzung, nicht wegen
einer neuen Auto-Funktion. Der Debug-Release-Schutz bleibt aktiv.

GitHub-CI ist konfiguriert; der erste Online-Lauf wird getrennt dokumentiert.
Das Speichermodul erhält nur den lokalisierten Titel neuer leerer Chats; seine
separaten Verschlüsselungs-Gerätetests wurden nicht wiederholt.

## Wiederholen

JDK 17 und SDK gemäß [START_WINDOWS.md](START_WINDOWS.md) setzen.

```powershell
.\gradlew.bat :core:test :providers:test :voice:test :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest
.\gradlew.bat :prototype-app:assembleDhuDebug :prototype-app:assembleDhuDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest :prototype-app:connectedDhuDebugAndroidTest
```

Die bisherigen Gerätefixtures erwarten Deutsch. Neue Englisch-/Rückfalltests
verwenden isolierte Konfigurationskontexte. JVM-Tests setzen die deutsche
Baseline unabhängig von der Sprache des Build-Rechners; neue Sprachtests prüfen
Englisch zusätzlich explizit.

[Sprache und Wiederaufnahme](LOCALIZATION.md) · [Bedienung](PHONE_UI.md)
Vorheriger Bericht: [0.1.11](history/BUILD_AND_TEST-0.1.11.md).

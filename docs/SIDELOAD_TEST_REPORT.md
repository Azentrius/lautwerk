# Sideload und Fahrzeugtests für OpenAuto AI

Stand 12.10.2026. App-Code und APK bleiben auf dem bestätigten Stand 0.1.15,
Quellcode-Commit `5600446fbe08a8bbdf460f31a9e3b9fbbeaedead`. Die
[Testanleitung](SIDELOADING.md) beschreibt den Vergleich. Noch liegt kein
bestätigtes Ergebnis für Kia oder ID.7 vor. Auf Nutzerwunsch wird jetzt der ID.7
mit nativer drahtloser Verbindung geprüft; alle Kia-Versuche sind zurückgestellt.

## Am angeschlossenen Pixel gemessen

| Merkmal | Ergebnis |
| --- | --- |
| Telefon | Pixel 10 Pro |
| Android | 17, API 37 |
| Sicherheitspatch | 2026-09-05 |
| Aktives Android Auto | 17.7.663654-release, Code 177663654 |
| Aktive Google Play-Dienste | 26.39.63 (260400-995553646), Code 263963035 |
| AAWireless-Handy-App | 8.5.2819, Code 8052819; keine Adapter-Firmware |
| Test-App | dev.openautoai.prototype.dhu, 0.1.15-prototype, Code 6 |
| Assistent separat | dev.openautoai.app, 0.1.15-dev, Code 15 |
| Installationsherkunft Test-App | installerPackageName=null; initiatingPackageName=com.android.shell |
| Registrierter Car-Dienst | dev.openautoai.car.OpenAutoCarService, genau ein Treffer |
| Lokale Test-APK SHA-256 | F580E27ACEC2395AE0097E0A8E7ACE4F7B62288B1CEAD6B6698042A6505F5D70 |

Die SHA-256 bezieht sich auf die lokale Build-Ausgabe
`prototype-app/build/outputs/apk/dhu/debug/prototype-app-dhu-debug.apk`.
In dieser Vorbereitung wurde keine APK neu installiert oder aus dem Telefon
extrahiert. Die Paketdaten belegen die installierte Version und den ADB-Installationsweg.

Die von Android ebenfalls gelisteten System-/Stub-Versionen sind nicht als aktive
Android-Auto- oder Play-Dienste-Version übernommen. Der Nutzer bezeichnet den
Adapter als erstes AAWireless-Modell mit Firmware „7.xxx“; genaue Firmware und
Hardwarekennung sind am verbundenen Adapter noch zu bestätigen.

## Ergebnisse je Verbindung

| Lauf | Einstellungen | Launcher / Öffnen / Starten / Wiederverbinden | Car-API | Status |
| --- | --- | --- | --- | --- |
| DHU aus Abnahme 0.1.15 | damalige PC-Konfiguration | bestanden, siehe damaligen Bericht | siehe damaligen Bericht | Historischer Kontrollbefund, kein neuer Fahrzeugtest |
| Kia mit AAWireless, Ausgangszustand | noch zu erfassen | offen / offen / offen / offen | nicht gemessen | Auf Nutzerwunsch zurückgestellt |
| Kia mit Adapter-Entwicklermodus | erst nach Ausgangsmessung | offen / offen / offen / offen | nicht gemessen | Auf Nutzerwunsch zurückgestellt |
| Kia nach Wiederherstellung | ursprüngliche Werte | offen / offen / offen / offen | nicht gemessen | Auf Nutzerwunsch zurückgestellt |
| ID.7 nativ drahtlos | ohne AAWireless, bisherige Einstellungen | offen / offen / offen / offen | nicht gemessen | Nutzer um Ausgangstest gebeten |

Für jeden tatsächlichen Lauf ergänzen: Zeitpunkt mit Zeitzone, Infotainment-Version,
beim Kia genaue Adapter-Firmware und beide Einstellungswerte, Anzahl erfolgreicher
Neu-Verbindungen und gegebenenfalls genauen Fehler. Nutzerbeobachtung und
ausgelesene Logs sind als unterschiedliche Belege zu kennzeichnen.

## Vorbereitung des ID.7 Versuchs

Vor dem Fahrzeugversuch wurden die installierte Test-App 0.1.15-prototype/6
und Android Auto 17.7.663654-release/177663654 erneut per ADB bestätigt.
Kein laufender DHU-Prozess wurde am PC gefunden. Die Geräteuhr lieferte
als Startmarke für die spätere Logauswertung Unix-Zeit `1791839347`.
Das ist eine vorbereitende Zeitgrenze, kein Nachweis einer Fahrzeugverbindung.
Das Pixel wird für diesen Test regulär vom PC getrennt und nativ drahtlos
mit dem stehenden ID.7 verbunden. Noch kein Ergebnis und keine Car-API gemessen.

## Aussagegrenzen

Die Vorbereitung bestätigt, dass der bekannte UI-Prototyp auf dem Pixel installiert
und sein Auto-Service registriert ist. Sie belegt keine Launcher-Freigabe im Fahrzeug.
Die Auto-App besitzt weiterhin nur den UI-Testzähler. API-Anfragen, Sprache im Auto,
Anruf-, Headset- und Bluetooth-Audio-Unterbrechungen wurden nicht getestet.

App-Code, Design, Manifest und Host-Prüfung wurden für diese Vorbereitung nicht
geändert. Ein erneuter App-Build ist für die Dokumentationsänderung nicht erforderlich.
Die [Abnahme 0.1.15](BUILD_AND_TEST.md) bleibt der bisherige App-/DHU-Teststand.

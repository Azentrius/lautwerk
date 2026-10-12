# Sideload und Fahrzeugtests für OpenAuto AI

Stand 13.10.2026. App-Code und APK bleiben auf dem bestätigten Stand 0.1.15,
Quellcode-Commit `5600446fbe08a8bbdf460f31a9e3b9fbbeaedead`. Die
[Testanleitung](SIDELOADING.md) beschreibt den Vergleich. Der Nutzer berichtet,
dass OpenAuto AI am 12.10.2026 gegen 17 Uhr im ID.7 nicht sichtbar war.
Ein funktionierender Fahrzeugweg ist damit weiterhin nicht bestätigt. Der ID.7
bleibt das Testfahrzeug; alle Kia-Versuche sind zurückgestellt.

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

Die SHA-256 wurde zunächst an der lokalen Build-Ausgabe
`prototype-app/build/outputs/apk/dhu/debug/prototype-app-dhu-debug.apk` bestimmt.
Bei der anschließenden Diagnose am 12.10.2026 wurde die installierte APK aus dem
Telefon ausgelesen: **Beide Dateien haben dieselbe SHA-256**. Der ausgelesene
Signierzertifikat-Fingerabdruck (SHA-256) lautet
`687cf415a6b6d4f45a8a92a55a9fcd91c463bc0d0d9d8a77ce95c9afbe63e696`.
Die Paketdaten in der Tabelle beschreiben den Ausgangszustand vor dem weiter
unten beschriebenen, separat freigegebenen Installationsversuch.

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
| ID.7 gegen 17 Uhr am 12.10.2026 | laut Kontext native Funkverbindung; damalige Versionen und Detailkonfiguration nicht ausgelesen | App nicht sichtbar / nicht geprüft / nicht geprüft / nicht geprüft | nicht gemessen | Nutzerbeobachtung, keine zeitgleichen Logs |
| ID.7 mit aktuell vorbereiteter 0.1.15 | ohne AAWireless, bisherige Einstellungen | offen / offen / offen / offen | nicht gemessen | Kein erneuter unveränderter Versuch erforderlich; zunächst konkrete technische Änderung oder Diagnose vorbereiten |

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
Ein neuer Fahrzeuglauf mit dieser Zeitgrenze ist nicht bestätigt. Nach der
Rückmeldung zur Fahrt gegen 17 Uhr wird kein unveränderter Wiederholungstest
mehr angefordert. Die Beobachtung von 17 Uhr liegt vor dieser Startmarke.

Der Git-Vergleich nach 17 Uhr zeigt Design-Referenzdateien, das App-Symbol/
Startbild in 0.1.15 und anschließend reine Dokumentationsänderungen.
Car-Service-Registrierung, Kategorie, HostValidator, Session und Car-Template-
Logik blieben dabei unverändert. Der Installationsweg blieb ADB. Es wurde kein
Sichtbarkeitsproblem im Fahrzeug behoben; aus diesen Änderungen ist kein anderer
Launcher-Befund zu erwarten. Die gegen 17 Uhr tatsächlich installierte App-Version
ist nicht belegt, daher wird der Befund nicht als durchgeführter 0.1.15-Test geführt.

## Aussagegrenzen

Die Vorbereitung bestätigt, dass der bekannte UI-Prototyp auf dem Pixel installiert
und sein Auto-Service registriert ist. Sie belegt keine Launcher-Freigabe im Fahrzeug.
Die Auto-App besitzt weiterhin nur den UI-Testzähler. API-Anfragen, Sprache im Auto,
Anruf-, Headset- und Bluetooth-Audio-Unterbrechungen wurden nicht getestet.

App-Code, Design, Manifest und Host-Prüfung wurden für diese Vorbereitung nicht
geändert. Ein erneuter App-Build ist für die Dokumentationsänderung nicht erforderlich.
Die [Abnahme 0.1.15](BUILD_AND_TEST.md) bleibt der bisherige App-/DHU-Teststand.

## Diagnose auf dem Pixel: 12.10.2026, 23:52 Uhr MESZ

Ohne Fahrzeugverbindung wurde die vollständige Android-Auto-Seite „Launcher
anpassen“ geprüft. OpenAuto AI fehlt auch am Ende der Liste. „Unbekannte Quellen“
ist in den Entwicklereinstellungen eingeschaltet; kabelloses Android Auto ebenso.
Der App-Modus-Dialog zeigte keinen ausgewählten Eintrag. Keine dieser Einstellungen
wurde geändert. Der Head-Unit-Server war im Menü als gestartet erkennbar; auf dem
PC lief kein DHU.

Im aktiven Android-Benutzer ist das Testpaket installiert, aktiviert, nicht
versteckt, nicht angehalten und nicht suspendiert. Die Service-Abfrage mit
CarAppService-Aktion und POI-Kategorie liefert genau unseren Dienst. Die
POI-Deklaration bleibt der bekannte Debug-Platzhalter, keine Produktzulassung.

Nach einem Neustart nur des Android-Auto-Prozesses (keine Datenlöschung) wurden
beim Öffnen der Launcher-Auswahl sechs Ablehnungen unseres Pakets aufgezeichnet.
Auszug aus dem ausgelesenen Logpuffer; Unix-Zeit und Logtext unverändert:

```text
1791841976.418 W CAR.VALIDATOR: isPackageInstalledByPlayCheck service not connected
1791841976.418 W CAR.VALIDATOR: Package DENIED; failed all other checks [dev.openautoai.prototype.dhu]
```

`1791841976.418` entspricht 12.10.2026, 23:52:56.418 MESZ. Prozess-/Threadnummern
wurden im veröffentlichten Auszug weggelassen. Die beiden Zeilen stammen aus
demselben Prüflauf/Thread. Die kurzzeitig auf VERBOSE gesetzten Log-Tags
`CAR.VALIDATOR` und `PlayGearheadService` wurden danach auf ihre vorherigen leeren
Werte zurückgesetzt. Eine zusätzlich gestartete, gefilterte Hintergrundaufnahme
blieb leer und wurde beendet; der Nachweis stammt ausdrücklich aus `logcat -d`,
nicht aus dieser Aufnahme. Private Rohdaten bleiben unter dem ignorierten
Verzeichnis `diagnostics/`.

**Belegt:** Android Auto lehnt unser installiertes Paket bereits bei seiner
App-Auswahl ab. Es fehlen in diesem Prüflauf Aufrufe von `car.createHostValidator`,
`car.onCreateSession` und `car.onCreateScreen`. Ein Fehler in der Darstellung des
Car-Templates erklärt diesen Vorfilter nicht.

**Noch nicht belegt:** Welche einzelne Zulassungsregel im echten ID.7 entscheidend
ist. Die Meldung zur nicht verbundenen Play-Prüfung stammt aus der Smartphone-
Einstellungsseite nach Prozessneustart. Sie beweist weder einen dauerhaften
Play-Dienste-Fehler noch einen fehlenden Play-Besitznachweis im Fahrzeug. Der Befund
von 17 Uhr besitzt weiterhin keine zeitgleichen Logs. Die Car-API des ID.7 ist
weiterhin nicht gemessen.

## Freigegebener Herkunftsversuch

Der Nutzer hat nach Erklärung ausdrücklich einen begrenzten Versuch erlaubt:
dieselbe Test-APK durch den vorhandenen Android-Systeminstaller installieren und
Google Play als Installationsherkunft angeben. Das ist eine vorgetäuschte Herkunft,
keine normale APK-Installation und keine von Google zugesicherte Zulassung. Root,
Änderungen an Android Auto und Änderungen am Car-App-Code gehören nicht dazu.

Der Systeminstaller `com.google.android.packageinstaller` ist auf dem Pixel
vorhanden. Er hat die identische APK als **Update** angenommen. Nach gesonderter
Nutzerfreigabe wurde die APK durch Google Play Protect geprüft. Dessen Dialog
meldete „Diese App ist anscheinend sicher“ und erlaubte die Installation. Das
Update wurde am **13.10.2026 um 00:03:32 MESZ** abgeschlossen. Keine Deinstallation,
keine Datenlöschung und keine Deaktivierung von Play Protect.

| Prüfung | Vorher | Nach dem Versuch |
| --- | --- | --- |
| Version / Code | 0.1.15-prototype / 6 | unverändert |
| APK SHA-256, jeweils vom Pixel ausgelesen | F580E27ACEC2395AE0097E0A8E7ACE4F7B62288B1CEAD6B6698042A6505F5D70 | identisch |
| installerPackageName | null | com.android.vending |
| initiatingPackageName | com.android.shell | com.google.android.packageinstaller |
| originatingPackageName | null | null |
| packageSource | 1 | 3 |
| „Launcher anpassen“, ohne laufenden DHU | OpenAuto AI fehlt in der vollständigen Liste | OpenAuto AI vorhanden; Kontrollkästchen checked=true, enabled=true |
| DHU-Kontrolle nach dem Update | historische Abnahme 0.1.15 | Öffnen und Starten erneut bestanden |
| ID.7 nativ drahtlos nach dem Update | noch nicht getestet | Fahrzeugtest angefordert; Ergebnis offen |

Die Oberfläche wurde nach einem erneuten Start nur des Android-Auto-Prozesses
gelesen. Ein Screenshot/Log eines DHU-Launchers wurde nicht als Nachweis für die
Smartphone-Launcher-Auswahl verwendet. [Sanitierte Messdaten](evidence/sideload/origin-experiment-2026-10-13.json).

**Ergebnis:** Das geänderte Installationsverfahren hat bei unveränderter APK und
unveränderten Android-Auto-Entwicklereinstellungen einen zuvor fehlenden
Launcher-Eintrag hergestellt. Damit ist eine wirksame Abhängigkeit vom
Installationsverfahren belegt. Da Installer, Initiator und Installationsablauf
zusammen geändert wurden, ist damit nicht isoliert, welches einzelne Feld nötig
oder hinreichend ist. Insbesondere ist `adb install -i com.android.vending` nicht
der getestete Weg. Eine konkrete Play-Besitzabfrage im ID.7 bleibt noch zu messen.

Die [reproduzierbaren Installationsschritte](SIDELOAD_INSTALL_EXPERIMENT.md)
benötigen einen PC mit ADB und sind ein freigegebener Entwicklungsversuch. Sie sind
noch keine bestätigte Verteilungsanleitung für normale Nutzer.

### DHU-Kontrolle nach dem Herkunftsversuch

Offizieller DHU 2.0-windows, Build 2022-03-30-438482292, mit vorhandenem
800×480-Touch-Profil, diesmal über seine Headless-/Konsolensteuerung. Öffnen im
Launcher zeigte „OpenAuto AI“, „Bereit für den UI-Test“ und die unveränderte
Auswahl „Claude / Anthropic · Claude Haiku 4.5“. „Starten“ führte sichtbar zu
„Gestartet · Test 1“. Passende App-Logs:

```text
1791842847.712 I OpenAutoAI: car.createHostValidator (signature allowlist enabled)
1791842847.718 I OpenAutoAI: car.onCreateSession
1791842847.728 I OpenAutoAI: car.onCreateScreen api=8
1791842847.749 I OpenAutoAI: car.onGetTemplate count=0
1791842866.972 I OpenAutoAI: car.start count=1
1791842866.974 I OpenAutoAI: car.onGetTemplate count=1
```

Das entspricht 13.10.2026, 00:07:27 bis 00:07:46 MESZ. Android Auto weiterhin
17.7.663654-release / 177663654. **Car-API 8 gilt für diesen DHU-Lauf**, nicht für
den ID.7. Der DHU wurde mit `quit` beendet, die ADB-Portweiterleitung entfernt und
der Head-Unit-Server für den anschließenden Fahrzeugtest regulär gestoppt.

### Aufzeichnung für den Fahrzeuglauf

Die [Diagnosehilfe](../tools/sideload/README.md) wurde auf dem Pixel geprüft:
eindeutig markierte Selbsttestzeile erfasst, Ende nach zehn Sekunden bestätigt,
ungültige Sitzungs-ID, Laufzeit über 30 Minuten und vorhandene Sitzungs-ID jeweils
mit Exitcode 2 abgelehnt. Sie ruft gefilterte Log-Snapshots alle fünf Sekunden ab
und schreibt sie in begrenzte rotierende Dateien. Keine Änderungen an Apps oder
Systemeinstellungen durch das Skript. Selbsttestzeilen sind keine Fahrzeugbelege.

Für den Nutzerlauf startete sie am 13.10.2026 um 00:08:50 MESZ (Unix 1791842930),
mit Ende nach rund 20 Minuten. Diese Zeitgrenze ist nur die gestartete Aufnahme;
eine tatsächliche Fahrzeugverbindung oder ein Erfolg im ID.7 ist damit noch nicht
behauptet. Der Nutzer wurde um zwei Verbindungen im Stand und Rückkehr zum PC
gebeten; eine Verschiebung ist ausdrücklich möglich.

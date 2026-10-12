# App-Symbol und Startbild 0.1.15

OpenAuto AI verwendet die freigegebene Sprechblase mit Punkt aus dem
[Design-System 170dd38](https://github.com/Azentrius/design-system/blob/170dd38be84a9f107440a730eb29361087fc63ea/android.md), O1 aus
`6a7a951`. Die sechs gelieferten Android-Dateien sind im Assistenten bytegleich
übernommen; der Prototyp enthält dieselben fünf Launcher-Dateien, ohne das dort
nicht benötigte Dienstsymbol. Der Generator wurde nicht ausgeführt. Es gibt
keine eigenen Zeichen-, Größen- oder Farbwerte.

## Umsetzung

- Beide Manifeste verwenden `@mipmap/ic_launcher` und `@mipmap/ic_launcher_round`.
  Hintergrund `#0A0A0F`, Vordergrund `#C8A96E`, gleiche Geometrie für die
  Monochrom-Ebene; `evenOdd` und die ausgesparten Balken bleiben erhalten.
- Der native Fensterhintergrund verwendet dieselbe Tintenfarbe. Ab API 31
  setzt das Theme nur die Startbild-Hintergrundfarbe; das Launcher-Symbol wird
  automatisch verwendet. Ab API 33 fordert `windowSplashScreenBehavior`
  mit `icon_preferred` dessen Anzeige auch bei Starts außerhalb des Launchers an.
  `windowSplashScreenAnimatedIcon` bleibt ungesetzt. Kein eigenes Startfenster,
  keine Bibliothek, kein Markenbild, keine Startverzögerung und keine eigene Animation.
  Die Android-Fensteranimation bleibt systemgesteuert.
- Unter API 31 ist der Fensterhintergrund ebenfalls Tinte; ein eigener
  Splash-Dialog oder ein zweites Startfenster wird nicht eingeführt.
- Der Gesprächsdienst verwendet ausschließlich das gelieferte weiße
  `ic_stat_openauto` für `setSmallIcon`. Keine Benachrichtigungsfarbe in Gold.
  Aktionen, Texte, Kanal, Berechtigungen und Dienstablauf bleiben erhalten.
- Android Auto übernimmt das adaptive App-Symbol über das vorhandene
  `Action.APP_ICON` und den Launcher. CarAppTheme/B2/B3, MessageTemplate,
  Texte, Zähler, HostValidator und Kategorie bleiben unverändert.

Technische Grundlage: [Android-Startbild](https://developer.android.com/develop/ui/views/launch/splash-screen)
und [adaptive Symbole](https://developer.android.com/develop/ui/compose/system/icon_design_adaptive).
Die Smartphone-Seiten behalten das bestätigte Design aus 0.1.13. Der minimale
Phone-Test des Prototyps bleibt funktional und strukturell unverändert.

## Bauen und installieren

Im Projektordner mit JDK 17, SDK Platform 37 und Build Tools 36.0.0:

```powershell
.\gradlew.bat :app:assembleDebug :prototype-app:assemblePhoneDebug :prototype-app:assembleDhuDebug
```

Die Pakete sind getrennt und verwenden weiterhin ihre eigenen vorhandenen Daten:

| Variante | Version / Code | Paket |
|---|---|---|
| Assistent | 0.1.15-dev / 15 | `dev.openautoai.app` |
| DHU-Test | 0.1.15-prototype / 6 | `dev.openautoai.prototype.dhu` |
| Phone-Vergleich | 0.1.15-prototype / 5 | `dev.openautoai.prototype.phone` |

Assistent und DHU wurden auf dem Pixel aktualisiert, der Phone-Vergleich nur gebaut.
Installation aus dem Projektordner; beim heruntergeladenen Paket die beiden
APK-Pfade durch `OpenAutoAI-0.1.15-assistant-debug.apk` und
`OpenAutoAI-0.1.15-dhu-debug.apk` ersetzen:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
& $adb -s 56161FDCH000H8 install -r .\app\build\outputs\apk\debug\app-debug.apk
& $adb -s 56161FDCH000H8 install -r .\prototype-app\build\outputs\apk\dhu\debug\prototype-app-dhu-debug.apk
& $adb -s 56161FDCH000H8 shell am start -n dev.openautoai.app/.MainActivity
```

Erwartet: zweimal `Success`; keine Deinstallation und kein Löschen von App-Daten.
Bei einem anderen Gerät dessen Kennung aus `devices -l` verwenden.

## DHU-Prüfung

Die bestehende Einrichtung aus [START_WINDOWS.md](START_WINDOWS.md) weiterverwenden.
Der Android-Auto-Head-Unit-Server muss auf dem Pixel laufen. Im Projektordner:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$dhu = "$env:LOCALAPPDATA\Android\Sdk\extras\google\auto\desktop-head-unit.exe"
& $adb -s 56161FDCH000H8 forward tcp:5277 tcp:5277
& $dhu -c .\tools\dhu\800x480-touch.ini -i touch
```

Im Launcher „OpenAuto AI“ öffnen. Das neue Zeichen erscheint im Launcher und im
Template-Kopf. Titel, aktuelle Auswahl und „Starten“ bleiben sichtbar. Ein Tipp
ergibt „Gestartet · Test 1“. Sechs Starts nacheinander müssen 1 bis 6 ergeben.
`day` und `night` in der DHU-Konsole prüfen beide Modi; `quit` beendet den DHU.
Die Starttaste wird auf diesem Host weiterhin hellblau dargestellt.

Für den Rotary-Vergleich den DHU anschließend mit
`-c .\tools\dhu\1280x720-wide-rotary.ini -i rotary` starten. Mit
`dpad rotate right` den Fokus verschieben, mit `dpad click` auslösen.
Eine Anbieterwahl in der Smartphone-Test-App muss in derselben DHU-Sitzung
erscheinen, ohne den Zähler zurückzusetzen.

Dies bleibt ein lokaler UI-Prototyp ohne KI-Anfrage im Auto. Es wird keine neue
offiziell unterstützte Car-App-Kategorie behauptet und keine Host-Prüfung umgangen.
Die Testergebnisse und Grenzen stehen in [BUILD_AND_TEST.md](BUILD_AND_TEST.md).

Beobachtung am Testhost: Nach Bedienung am Smartphone kann der nutzbare
Rotary-Fokus verloren gehen. Diese Auffälligkeit zeigte sich auch im Vergleich
mit 0.1.14; siehe Testbericht. Drehen allein stellte den Fokus nicht zuverlässig
wieder her. Eine frische DHU-Sitzung ermöglichte wieder einen Start. Die normale
[DHU-Steuerung](https://developer.android.com/training/cars/testing/dhu) bleibt erhalten;
es wird keine App- oder Host-Beschränkung abgeschaltet.

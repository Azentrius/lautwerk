# Begrenzte Android-Auto-Diagnose

`capture-car-logs.sh` zeichnet nur Meldungen mit dem Paketnamen
`dev.openautoai.prototype.dhu`, unsere Car-Lebenszyklusereignisse und die allgemeine
Meldung zur nicht verbundenen Play-Herkunftsprüfung auf. Es verändert weder
Installationsherkunft noch Android-Auto-Einstellungen und benötigt kein Root.
Es ist keine Sideload-Freischaltung.

Die Aufnahme bleibt beim Abziehen des USB-Kabels auf dem Telefon aktiv. Jeder
Logabruf wird beendet und auf die Datei geschrieben; der Selbsttest auf Pixel 10
Pro / Android 17 hat Aufnahme und automatisches Ende bestätigt. Laufzeit maximal
30 Minuten, Standard 20 Minuten. Drei rotierende Logdateien von jeweils ungefähr
128 KiB begrenzen den Speicherverbrauch. Ein Telefonneustart beendet die Aufnahme.
Die Fünf-Sekunden-Abstände können bei sehr hoher Loglast Einträge verpassen.

## Start in PowerShell im Projektordner

Telefon entsperrt am PC, ADB autorisiert; keine gleichzeitige DHU-Sitzung.

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$serial = 'SERIENNUMMER_AUS_ADB_DEVICES'
$session = 'id7-' + [Guid]::NewGuid().ToString('N')
$remoteBase = '/data/local/tmp/openauto-car-' + $session
& $adb -s $serial push tools/sideload/capture-car-logs.sh /data/local/tmp/openauto-capture-car-logs.sh
& $adb -s $serial shell "nohup timeout -k 5s 1230s sh /data/local/tmp/openauto-capture-car-logs.sh $session 1200 >$remoteBase.err 2>&1 </dev/null & echo `$!"
Start-Sleep -Seconds 1
& $adb -s $serial shell cat "$remoteBase.status"
```

Die letzte Zeile muss `running start=… deadline=…` zeigen. Bei einem Fehler nicht
ins Fahrzeug gehen, sondern zuerst die Datei `$remoteBase.err` lesen. Die
Bezeichner enthalten keine Seriennummer. Jede Aufnahme braucht eine neue ID.

Danach Telefon vom PC trennen und im stehenden ID.7 nativ drahtlos verbinden.
Uhrzeit des Verbindens und Trennens notieren. Launcher, Öffnen und Starten getrennt
beurteilen. Es werden keine Gespräche, Mikrofon- oder Unterbrechungstests benötigt.

## Nach Rückkehr sichern

In derselben PowerShell-Sitzung und am wieder verbundenen Telefon:

```powershell
# Nur den zu dieser Aufnahme gehörenden Shell-Prozess beenden.
$expected = '^\s*(\d+)\s+sh /data/local/tmp/openauto-capture-car-logs\.sh ' + [regex]::Escape($session) + ' 1200\s*$'
$rows = & $adb -s $serial shell ps -A -o PID,ARGS
foreach ($row in $rows) {
    if ($row -match $expected) {
        & $adb -s $serial shell kill -TERM $Matches[1]
    }
}
$destination = Join-Path (Get-Location) ('diagnostics\sideload-' + $session)
New-Item -ItemType Directory -Path $destination -Force | Out-Null
& $adb -s $serial pull "$remoteBase.log" $destination
& $adb -s $serial pull "$remoteBase.status" $destination
& $adb -s $serial pull "$remoteBase.err" $destination
# Nur falls Rotation stattgefunden hat; fehlende Dateien sind hier normal.
& $adb -s $serial pull "$remoteBase.log.1" $destination
& $adb -s $serial pull "$remoteBase.log.2" $destination
```

Der laufende `sleep` kann das Reagieren auf das Stoppsignal um bis zu fünf Sekunden
verzögern. Bei Bedarf den Status anschließend erneut lesen. Keine anderen
Logcat-/ADB-Prozesse beenden. Die Aufnahme stoppt auch ohne manuellen Abbruch
von selbst. Rohdateien unter `diagnostics/` sind von Git ausgeschlossen.

Beim Auswerten doppelte Zeilen entfernen: Die Ein-Sekunden-Überlappung zwischen
Abrufen ist beabsichtigt. Nur Meldungen innerhalb des tatsächlichen Fahrzeuglaufs
als Fahrzeugbeleg verwenden. Zeilen von `OpenAutoCaptureSelfTest` oder mit
`SELFTEST_ONLY_` sind Prüfmarkierungen des Diagnosewerkzeugs, keine Car-Ereignisse.

Auch gefilterte Logs vor einer Veröffentlichung prüfen. Gerätekennungen, Konten,
Dateipfade und sonstige private Inhalte nicht auf GitHub hochladen. Ein fehlender
Logeintrag beweist keine bestimmte Ablehnungsursache. `car.onCreateScreen api=…`
belegt die Car-API nur für die zugehörige Sitzung.

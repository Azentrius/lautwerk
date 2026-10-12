# Bounded Android Auto diagnostics

`capture-car-logs.sh` records only messages containing the package name
`dev.openautoai.prototype.dhu`, our car lifecycle events, or the generic warning
about the disconnected Play-origin check. It changes neither install origin nor
Android Auto settings and requires no root. It does not enable sideloaded apps.

The recording continues on the phone after USB is unplugged. Each snapshot exits
and flushes its file; a Pixel 10 Pro / Android 17 self-test confirmed capture and
automatic completion. Maximum duration is 30 minutes, default 20 minutes. Three
rotating log files of roughly 128 KiB each bound storage use. A phone reboot ends
the capture. Five-second polling can miss events under very high system log load.

## Start from PowerShell in the project directory

Unlock the phone, connect it to the PC, and authorize ADB. No concurrent DHU session.

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$serial = 'SERIAL_FROM_ADB_DEVICES'
$session = 'id7-' + [Guid]::NewGuid().ToString('N')
$remoteBase = '/data/local/tmp/openauto-car-' + $session
& $adb -s $serial push tools/sideload/capture-car-logs.sh /data/local/tmp/openauto-capture-car-logs.sh
& $adb -s $serial shell "nohup timeout -k 5s 1230s sh /data/local/tmp/openauto-capture-car-logs.sh $session 1200 >$remoteBase.err 2>&1 </dev/null & echo `$!"
Start-Sleep -Seconds 1
& $adb -s $serial shell cat "$remoteBase.status"
```

The final command must show `running start=… deadline=…`. If startup fails, read
`$remoteBase.err` before going to the car. IDs contain no device serial. Use a new
ID for every recording; the script rejects reuse.

Disconnect from the PC and connect natively and wirelessly to the parked ID.7.
Record connection and disconnection times. Assess launcher visibility, Open and
Start separately. No conversation, microphone or interruption tests are needed.

## Collect after returning

Use the same PowerShell session with the phone reconnected:

```powershell
# Stop only the shell process belonging to this exact recording.
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
# Only present after rotation; missing files are normal here.
& $adb -s $serial pull "$remoteBase.log.1" $destination
& $adb -s $serial pull "$remoteBase.log.2" $destination
```

A running `sleep` may delay the stop signal by up to five seconds. Re-read status
if necessary. Do not stop unrelated logcat or ADB processes. Capture also ends on
its own without manual stopping. Raw files in `diagnostics/` are ignored by Git.

Deduplicate lines when analyzing: the one-second overlap between snapshots is
intentional. Attribute only events within an actual vehicle session to that
vehicle. `OpenAutoCaptureSelfTest` and `SELFTEST_ONLY_` mark diagnostic-tool tests,
not car events.

Review even these filtered logs before publishing. Do not upload device identifiers,
accounts, file paths or other private information to GitHub. A missing log event
does not establish a rejection cause. `car.onCreateScreen api=…` identifies the
Car API only for its associated session.

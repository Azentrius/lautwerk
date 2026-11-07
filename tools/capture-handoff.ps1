# SPDX-License-Identifier: Apache-2.0
[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$Serial,
    [Parameter(Mandatory)][ValidateSet('ChatGPT', 'Claude')][string]$Assistant,
    [Parameter(Mandatory)][string]$OutputPath,
    [ValidateSet('Pixel 10 Pro', 'Pixel 6 Pro')][string]$ExpectedModel = 'Pixel 10 Pro',
    [string]$Adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    [ValidateRange(5, 60)][int]$Seconds = 20
)

$ErrorActionPreference = 'Stop'
function Read-Adb([string[]]$CommandArguments) {
    $lines = & $Adb -s $Serial @CommandArguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw 'ADB query failed. No raw device output was saved.' }
    return ($lines -join "`n").Trim()
}
function Read-Version([string]$Package) {
    $report = Read-Adb @('shell', 'dumpsys', 'package', $Package)
    return [ordered]@{
        name = [regex]::Match($report, '(?m)^\s*versionName=([^\r\n]+)').Groups[1].Value
        code = [regex]::Match($report, '\bversionCode=(\d+)').Groups[1].Value
    }
}
function Read-Visibility([string]$Package) {
    $escaped = [regex]::Escape($Package)
    $activities = Read-Adb @('shell', 'dumpsys', 'activity', 'activities')
    $windows = Read-Adb @('shell', 'dumpsys', 'window', 'displays')
    $resumedDisplays = @()
    $focusedDisplays = @()
    $display = $null
    foreach ($line in ($activities -split "`n")) {
        if ($line -match '^\s*Display #(\d+)') { $display = [int]$Matches[1] }
        if ($null -ne $display -and $line -match "(?:mResumedActivity|topResumedActivity).*\b$escaped/") {
            $resumedDisplays += $display
        }
    }
    $display = $null
    foreach ($line in ($windows -split "`n")) {
        if ($line -match '^\s*Display: mDisplayId=(\d+)') { $display = [int]$Matches[1] }
        if ($null -ne $display -and $line -match "mCurrentFocus=.*\b$escaped/") {
            $focusedDisplays += $display
        }
    }
    # Display 0 is the physical Pixel screen. A virtual-display activity is not phone visibility.
    # Keep booleans and display IDs only, never records, titles, intents or app content.
    return [ordered]@{
        focusFieldPresent = [regex]::IsMatch($windows, 'mCurrentFocus=')
        resumedDisplays = @($resumedDisplays | Sort-Object -Unique)
        focusedDisplays = @($focusedDisplays | Sort-Object -Unique)
        resumed = $resumedDisplays -contains 0
        focused = $focusedDisplays -contains 0
    }
}

$model = Read-Adb @('shell', 'getprop', 'ro.product.model')
$sdk = Read-Adb @('shell', 'getprop', 'ro.build.version.sdk')
if ($model -ne $ExpectedModel -or $sdk -ne '37') {
    throw 'This evidence procedure requires the selected Pixel on Android 17 (API 37). No launch was performed.'
}
$package = if ($Assistant -eq 'ChatGPT') { 'com.openai.chatgpt' } else { 'com.anthropic.claude' }
$before = Read-Visibility $package
if ($before.resumed -or $before.focused) {
    throw 'The destination is already visible. Return to the phone home screen before measuring a new handoff.'
}
$record = [ordered]@{
    schema = 2
    model = $model
    androidRelease = Read-Adb @('shell', 'getprop', 'ro.build.version.release')
    sdk = [int]$sdk
    securityPatch = Read-Adb @('shell', 'getprop', 'ro.build.version.security_patch')
    lautwerk = Read-Version 'com.azentrius.lautwerk.prototype.dhu'
    androidAuto = Read-Version 'com.google.android.projection.gearhead'
    destination = $Assistant
    destinationVersion = Read-Version $package
    trigger = 'Manual DHU action; this script never launches an app'
    before = $before
    resumedObserved = $false
    focusedObserved = $false
    focusFieldObserved = $before.focusFieldPresent
    resumedDisplaysObserved = @()
    focusedDisplaysObserved = @()
    firstObservationMs = $null
    voiceSession = 'unknown'
    mcpApproval = 'unknown'
}
$record.startedAtUtc = [DateTime]::UtcNow.ToString('o')
Write-Output "Observation ready. Click the $Assistant action in the DHU now."
$clock = [Diagnostics.Stopwatch]::StartNew()
while ($clock.Elapsed.TotalSeconds -lt $Seconds) {
    $visible = Read-Visibility $package
    $record.focusFieldObserved = $record.focusFieldObserved -or $visible.focusFieldPresent
    $record.resumedDisplaysObserved = @(@($record.resumedDisplaysObserved) + @($visible.resumedDisplays) | Sort-Object -Unique)
    $record.focusedDisplaysObserved = @(@($record.focusedDisplaysObserved) + @($visible.focusedDisplays) | Sort-Object -Unique)
    if ($visible.resumed -or $visible.focused) {
        if ($null -eq $record.firstObservationMs) { $record.firstObservationMs = $clock.ElapsedMilliseconds }
        $record.resumedObserved = $record.resumedObserved -or $visible.resumed
        $record.focusedObserved = $record.focusedObserved -or $visible.focused
    }
    Start-Sleep -Milliseconds 300
}
$record.observationDurationMs = $clock.ElapsedMilliseconds
$record.endedAtUtc = [DateTime]::UtcNow.ToString('o')
$resolvedOutput = [IO.Path]::GetFullPath($OutputPath)
if (Test-Path -LiteralPath $resolvedOutput) { throw 'The evidence file already exists; choose a new file name.' }
$parent = Split-Path -Parent $resolvedOutput
New-Item -ItemType Directory -Path $parent -Force | Out-Null
$record | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $resolvedOutput -Encoding utf8
Write-Output "Saved sanitized visibility evidence to $resolvedOutput"

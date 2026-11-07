# SPDX-License-Identifier: Apache-2.0
$ErrorActionPreference = 'Stop'

# Load only the pure parsing boundary, without running ADB or the observer's entry point.
$tokens = $null
$parseErrors = $null
$ast = [Management.Automation.Language.Parser]::ParseFile(
    (Join-Path $PSScriptRoot 'capture-handoff.ps1'), [ref]$tokens, [ref]$parseErrors)
if ($parseErrors.Count) { throw 'Observer syntax is invalid' }
$parser = $ast.Find({ param($node)
    $node -is [Management.Automation.Language.FunctionDefinitionAst] -and $node.Name -eq 'Read-Visibility'
}, $true)
if ($null -eq $parser) { throw 'Visibility parser is missing' }
. ([scriptblock]::Create($parser.Extent.Text))

function Read-Adb([string[]]$CommandArguments) {
    switch ($CommandArguments -join ' ') {
        'shell dumpsys activity activities' { return $script:activities }
        'shell dumpsys window displays' { return $script:windows }
        default { throw 'Unexpected device access in parser fixture' }
    }
}
function Assert-Visibility([bool]$Resumed, [bool]$Focused, [bool]$FocusField) {
    $actual = Read-Visibility 'com.openai.chatgpt'
    if ($actual.resumed -ne $Resumed -or $actual.focused -ne $Focused -or
        $actual.focusFieldPresent -ne $FocusField) { throw 'Incorrect phone visibility result' }
    return $actual
}

$script:activities = "Display #797 (activities from top to bottom):`n  mResumedActivity: ActivityRecord{fixture com.openai.chatgpt/.Fixture}`nDisplay #0 (activities from top to bottom):`n  mResumedActivity: ActivityRecord{fixture example.home/.Fixture}"
$script:windows = "Display: mDisplayId=797`n  mCurrentFocus=Window{fixture com.openai.chatgpt/.Fixture}`nDisplay: mDisplayId=0`n  mCurrentFocus=Window{fixture example.home/.Fixture}"
$virtual = Assert-Visibility $false $false $true
if ($virtual.resumedDisplays -notcontains 797 -or $virtual.focusedDisplays -notcontains 797) {
    throw 'Virtual display diagnostics were lost'
}

$script:activities = "Display #0 (activities from top to bottom):`n  topResumedActivity=ActivityRecord{fixture com.openai.chatgpt/.Fixture}`nDisplay #797 (activities from top to bottom):"
$script:windows = "Display: mDisplayId=0`n  mCurrentFocus=Window{fixture com.openai.chatgpt/.Fixture}`nDisplay: mDisplayId=797"
$phone = Assert-Visibility $true $true $true
if ($phone.resumedDisplays.Count -ne 1 -or $phone.resumedDisplays[0] -ne 0) {
    throw 'Physical display was not identified'
}

$script:windows = 'Display: mDisplayId=0'
$null = Assert-Visibility $true $false $false

$script:activities = 'mResumedActivity: ActivityRecord{fixture com.openai.chatgpt/.Fixture}'
$script:windows = 'mCurrentFocus=Window{fixture com.openai.chatgpt/.Fixture}'
$null = Assert-Visibility $false $false $true

Write-Output 'PASS: 4 visibility-parser fixtures; no ADB or app launch.'

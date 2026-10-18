# Android Auto audio and conversation – 0.1.17

## Scope and data flow

Commands below use `com.azentrius.lautwerk.prototype.dhu`, the package since 0.3.0.
Follow the [package replacement steps](SIDELOADING.md#installing-030); old data, permissions and
installer metadata are not carried over. The historical audio results below do not validate this new ID.

The original car Start action only incremented `PrototypeSession`. The independent assistant app
already had working speech, provider adapters and encrypted storage, but it was a different Android
package. Nothing connected that voice flow to the car screen.

The voice flow introduced in 0.1.17 keeps the HostValidator and MessageTemplate,
title, icon, provider/model line and host-controlled colors. Its one action now starts the selected
mode and becomes End while active. Long messages are shown in phone setup; short car summaries
leave room for the model. No transcript is placed on the car screen.

Three explicitly selected modes in **Audio and conversation setup** on the phone:

1. **Output test** (initial default): AudioManager playback focus → installed local TextToSpeech
   voice → fixed sample. No microphone, credentials, archive or AI client.
2. **Microphone and recognition test**: one recognized sentence → local TTS echo → stop. No AI client
   or conversation persistence. In the DHU, speak into the PC microphone; in a real car, the host
   supplies its microphone. Start on phone explicitly uses the phone microphone instead.
3. **Conversation**: recognition → existing ConversationVoiceBackend → ProviderChatClient/ClaudeAdapter
   → encrypted archive → local TTS → listen for the next question. The provider/model is captured when
   the session starts. Spoken provider switches reuse the existing controller and start a separate
   chat, without forwarding the former provider's history.

Car capture uses `CarAudioRecord`, 16 kHz mono PCM16, piped directly to the Android on-device
SpeechRecognizer. Android 13+ and Car API 5+ are required for this path. `EXTRA_SEGMENTED_SESSION`
and `onSegmentResults` are necessary on the tested Pixel; listening only for `onResults` produced
an empty final bundle. The shared adapter accepts the first nonempty final result and ignores partials
and empty intermediate segments. `onEndOfSpeech` is a speech boundary, not a request to close the PCM
stream. The stream stays open until a nonempty result, actual session end, cancellation or the existing
deadline. The ordinary phone recognizer still treats its empty final result as terminal.
Unsupported car input ends with a fixed error; it does not deliberately fall back to the phone mic.
Android recognizer implementations may ignore external input, so the synthetic PCM probe is required
before claiming support on another device/service version.

The phone fallback prefers the on-device recognizer; otherwise the phone's recognition service may
process audio online. TTS in this test app uses an installed local voice. It does not import the
assistant app's selected voice or enable online TTS voices. Raw microphone audio is never saved by
Lautwerk. Conversation mode sends recognized text to the selected provider and incurs API costs.
Chats and keys use the existing encrypted storage in the car test package's separate sandbox.
There is no history browser in this small setup page yet; the assistant's history is separate.

## Setup

1. Install the `prototype-app:dhuDebug` APK as an update, retaining the package and signing key.
   Do not uninstall or clear data to resolve a signing mismatch.
2. Open this test app on the phone. Select Claude / Anthropic and Claude Haiku 4.5 on its main page.
3. Open **Audio and conversation setup**. Run Output test first, then Microphone and recognition test.
   Grant microphone permission on the phone. Notification permission makes the ongoing status and
   End action visible in the notification drawer.
4. For each car test, choose the mode on the phone, open Lautwerk in Android Auto and press Start
   there. **Start on phone** tests the phone path. The old Start button on the selection page remains
   the UI counter; it does not start audio.
5. Only after both audio checks pass, enter an Anthropic API key in the setup page and press Save key.
   The field clears immediately; the key is stored through Android Keystore-backed AES-GCM. Never put
   keys into source, ADB commands, logs or an issue. Keys from `com.azentrius.lautwerk` are not copied.
6. Choose Conversation, start from Android Auto, ask a short question and wait for its spoken answer.
   Ask a follow-up when Listening appears. End stops the session. This test sends text to Anthropic
   and can use paid API credits. Test while parked.

Leaving the active phone/car surface, losing the service connection or audio focus, End, a timeout,
or process death ends the session; there is no automatic restart. Playback and capture hold separate
short focus leases. Negative focus changes stop capture/playback. Android's non-normal audio mode
also ends the session. A session is limited to ten minutes; diagnostics to sixty seconds. Late
callbacks cannot restart a stopped session. Cancellation cannot retract a request already received
by the provider. Call and headset interruption hardware tests are still deferred.

The car host can display its own microphone overlay above the template while capturing. Dismissing
that overlay closes the host stream and is treated as a terminal capture stop, not retryable silence.
The app does not control the overlay's position or styling. The wave is a microphone indicator,
not the lifetime of the whole conversation. After a recognized question the app closes capture,
processes the question and reads the answer through a separate TTS playback path. The overlay can
therefore disappear while the answer is audible. The app should continue to show Speaking and End;
only after TTS completion does it return to Listening. This display was confirmed during the
evening Claude retest below. Disappearance by itself is not a premature stop or an audio error.

Start and End are explicit, idempotent actions; an old End action cannot start a new session. Host recorder start/stop calls are serialized
off the UI thread, including across service recreation. End invalidates callbacks and publishes the
inactive state immediately; completion of host audio teardown can follow separately.

## Build and install on Windows

From the project root with the pinned SDK/toolchain installed:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat :prototype-app:assembleDhuDebug :prototype-app:assembleDhuDebugAndroidTest
$adb = "$env:ANDROID_HOME\platform-tools\adb.exe"
& $adb devices -l
$serial = 'SERIAL_FROM_ADB_DEVICES'
$apk = Join-Path (Get-Location) 'prototype-app\build\outputs\apk\dhu\debug\prototype-app-dhu-debug.apk'
Get-FileHash -Algorithm SHA256 -LiteralPath $apk
```

Use ordinary `adb -s $serial install -r $apk` after the 0.3.0 package replacement steps.
It installs the APK but does not guarantee Android Auto visibility; it can also change recorded
installation origin. Expected version: `0.3.0-prototype`, code `300`.

### Historical installation-origin experiment

The following records a bounded experiment approved for earlier builds and package identities.
It was not performed for 0.3.0. The current package name in the verification commands is not evidence
that the experiment works with it, and earlier approvals do not cover this new package.
The experiment **records Google Play as installer although the APK was sideloaded**. It is not a genuine
Play installation, a supported Google distribution path, category approval or a compatibility
guarantee. No root or modified Android Auto is used; host validation stays enabled.

```powershell
& $adb -s $serial push $apk /sdcard/Download/Lautwerk-dhu-debug.apk
& $adb -s $serial shell am start `
  -a android.intent.action.INSTALL_PACKAGE `
  -n com.google.android.packageinstaller/com.android.packageinstaller.InstallStart `
  -d 'content://com.android.externalstorage.documents/document/primary%3ADownload%2FLautwerk-dhu-debug.apk' `
  -t application/vnd.android.package-archive -f 0x10000001 `
  --es android.intent.extra.INSTALLER_PACKAGE_NAME com.android.vending `
  --ez android.intent.extra.NOT_UNKNOWN_SOURCE true `
  --es android.intent.extra.REFERRER_NAME android-app://com.android.vending `
  --ei android.content.pm.extra.INSTALL_REASON 1
```

Confirm Update in the Android system installer. If Play Protect asks to scan the APK, that submits
the compiled APK to Google; the maintainer approved this for the 0.1.17 builds. A scan/installation
failure is a failure to record, not a reason to disable protection. Then verify:

```powershell
& $adb -s $serial shell dumpsys package com.azentrius.lautwerk.prototype.dhu |
  Select-String 'versionCode=|versionName=|installerPackageName=|initiatingPackageName='
& $adb -s $serial shell cmd package query-services --brief `
  -a androidx.car.app.CarAppService -p com.azentrius.lautwerk.prototype.dhu
```

The historical test used `0.1.17-prototype`, code `117`; that is not the expected version of the
current build. An update of the same package must use the existing signing key. This experiment keeps the debug POI placeholder disclosed and `dhuRelease`
disabled. No general-purpose AI car category is claimed.

## DHU and device checks

Start the head unit server through Android Auto's developer menu, then:

```powershell
& $adb -s $serial forward tcp:5277 tcp:5277
& "$env:ANDROID_HOME\extras\google\auto\desktop-head-unit.exe" -a `
  -c "$PWD\tools\dhu\800x480-touch.ini"
```

Open the Android Auto launcher and Lautwerk. Choose the mode on the phone before starting the DHU
session. In the DHU a PC microphone is required. `mic play` is a DHU assistant command and is not
evidence that an app received its CarAudioRecord input. Close the DHU normally with `quit` before
phone UI/instrumentation tests. Reconnect if the USB transport drops; do not treat an unstarted
test as an audio failure.

Install only the instrumentation APK with ordinary ADB (this does not reinstall the target app):

```powershell
& $adb -s $serial install -r 'prototype-app\build\outputs\apk\androidTest\dhu\debug\prototype-app-dhu-debug-androidTest.apk'
$runner = 'com.azentrius.lautwerk.prototype.dhu.test/androidx.test.runner.AndroidJUnitRunner'
& $adb -s $serial shell am instrument -w $runner
# Opt-in fixed sample, audible; no AI request:
& $adb -s $serial shell am instrument -w -e audioPlayback true `
  -e class com.azentrius.lautwerk.prototype.VoicePlaybackDeviceTest $runner
# Opt-in synthetic PCM fixture, offline TTS -> pipe -> on-device recognition:
& $adb -s $serial shell am instrument -w -e externalPcm true `
  -e class com.azentrius.lautwerk.prototype.ExternalPcmRecognitionTest $runner
```

The ordinary suite skips both hardware probes unless requested. The PCM test generates its own
innocuous fixture; it does not record a human. Its configuration and final-result listener are the
same ones used by production car input. It is separate from the real host microphone echo check.

Safe diagnostic events:

```powershell
& $adb -s $serial logcat -d -s Lautwerk:I '*:S'
```

Events include `car.onCreateScreen api=…`, mode/car flag, phase, focus granted, first frame, byte count,
nonzero-signal boolean, recognition success/error number and TTS completion. There are no keys,
recognized words, generated answers, server bodies or exception causes. Capture only these app
events for reports; full Android Auto logs and dashboard screenshots can contain private data.

## Evidence – 2026-10-13

| Environment | Observed version |
|---|---|
| Phone | Pixel 10 Pro, Android 17 / API 37 |
| Android Auto | 17.8.663874-release, code 178663874 |
| DHU | 2.0-windows, build 2022-03-30-438482292 |
| Host Car API | 8, from `car.onCreateScreen api=8` |
| Car App Library | 1.8.0-rc01 |
| App under test | 0.1.17-prototype, code 117 |

| Check | Result |
|---|---|
| Phone independent output | Focus/TTS start/completion passed; user confirmed audible |
| Synthetic external PCM recognition | Passed on Pixel; regression probe also passed with four seconds of leading silence and the corrected production listener |
| DHU discovery and template | Launcher entry and opening retained; actual host API 8 |
| DHU output | Foreground service and TTS completed from the car action |
| DHU real PC microphone echo | Passed: 116736 PCM bytes, recognition success, TTS success; user confirmed correct audible sentence |
| Initial no-speech mic attempt | Ended with NO_INPUT; not counted as a recognized-speech test |
| One interrupted DHU transport | Reconnected; announced but unstarted test repeated successfully |
| Initial Claude live conversation/follow-up | User confirmed both answers audible. Repeated listening, intermittent error summaries and slow End were then reported; reliability was not accepted |
| Stability APK update | Installed 2026-10-13 at about 14:53 CEST; installed SHA-256 matches the prepared APK; installer remains com.android.vending |
| Stability phone/template/recognition-listener instrumentation suite | Passed: 17 checks, two opt-in audio probes skipped in this run |
| Separate output probe after stability update | Passed, including repeated Start on the real service; no microphone or AI request |
| DHU listening stability | A single capture stayed open for 25 seconds across empty segments; no premature recognition failure or second capture |
| DHU host microphone dismissal after correction | CAPTURE_STOPPED, then inactive/Ended, no retry. Local service stop 7 ms; host recorder stop 1 ms. These are event timings, not a claim about acoustic latency |
| Human echo/stop after correction | Passed, confirmed manually on 2026-10-13 ("Alle Tests erfolgreich"). The corresponding sessions at 15:16–15:17 CEST were in MICROPHONE mode: successful recognition and TTS, host dismissal, then explicit restart and another dismissal |
| Claude retest after correction | Completed later on 2026-10-13 with Claude Haiku 4.5: the tester reported overall operation working and confirmed Speaking/End during audible output. Captured events include two completed answers in one session, return to listening, explicit stop and explicit restart. See the separate evening evidence below; this is not a vehicle or latency benchmark |
| Final DHU template | Title, Claude Haiku 4.5, short status and the single action visible together; host API 8 retained |
| Vehicle voice (ID.7 / Kia) | Not tested in this audio change. The planned parked ID.7 test is deferred; Kia/AAWireless remains deferred. Earlier launcher visibility is not audio evidence |
| Call and headset interruption | Deferred |

The initial full CI task list passed locally and in GitHub Actions on commits `17073c6` and `2f477c3`.
After the stability correction the same full task list passed locally and in GitHub Actions on
`b92d0d0`: 100 JVM tests (core 22,
providers 21, voice 57), lint for all Android modules/variants with zero errors (36 warnings), all
three debug APKs and both instrumentation APKs. Unrelated local Android Studio daemon/JVM settings
remain outside this change. Tests cover blocked host start/cancellation, old-stop/new-start ordering,
start/read failure cleanup, duplicate template actions, empty segments followed by valid text and
host dismissal without retry or a late provider request.
The full task list also passed from a clean Git snapshot under JDK 17. Every APK ZIP entry has the
same content as the installed incremental build; the archive checksum itself differs. The checksum
below identifies the installed and downloadable APK.

Prepared final APK: the 0.1.17 DHU debug build.
SHA-256: `a5da94c143c9f5984fc4c5d001e19edaa5629cb9438a165a5580f60ec2b6cccf`.

Conversation mode was explicitly selected and confirmed on the setup page before the live test.
The earlier attempted switch had not selected the radio option after the phone page scrolled;
the resulting MICROPHONE event was correctly identified as an audio diagnostic, not a Claude call.
The subsequent live event sequence includes successful recognition, provider processing, completed
TTS and return to listening; the Claude answers were audible. No transcript or key was
collected.

That live session also exposed an early empty result followed by a second listening attempt. The
old code closed external PCM on `onEndOfSpeech` and accepted empty segments as terminal. During the
stability check the host microphone overlay exposed another retry source: host dismissal was mapped
to NO_INPUT. Both paths are now distinct from normal silence. The original synchronous recorder
stop could also wait for host IPC on the UI thread, and a stale End template click could toggle an
already stopped session back on. These code paths are corrected. The independent echo, host
dismissal and restart checks have now passed manually. The diagnostic events show host-dismissal
stops of 6 ms and 5 ms in the local service, with no automatic retry; later starts have explicit
`car.voice.action=start` events. These were microphone-mode checks; the separate evening retest
below now records Claude conversation evidence. Vehicle audio and the deferred call/headset
checks are not covered by either acceptance.

## Evening Claude retest – 2026-10-13

The existing **0.1.17-prototype/code 117** car APK was retained. Its installed SHA-256 was checked
against the final artifact above. The separate assistant was on 0.1.18/code 118; that does not make
this a test of a newly installed 0.1.18 car APK. Conversation mode and Claude Haiku 4.5 were selected.

The tester initially reported a remaining problem, then clarified that it was the wave overlay
disappearing while the answer remained audible, with no error text. The tester then reported
that the conversation worked overall and confirmed that Speaking and End remained
visible during the answer. This resolves the reported overlay observation as expected capture/
playback separation; no application change was needed. It does not establish measured acoustic
stop latency or a pass across every host, provider or repeated-start scenario.

[The evening evidence](evidence/0.1.17/dhu-claude-acceptance.md) records the environment, installed
artifact, selected technical events and the boundary between logs and the manual confirmation.
The ID.7 vehicle test is deferred. OpenAI car conversation, vehicle audio,
call/headset interruptions and the full current DHU design matrix remain unvalidated here.

## References

- [Car microphone and audio focus](https://developer.android.com/training/cars/apps/library/car-microphone)
- [RecognizerIntent external audio and segmented sessions](https://developer.android.com/reference/android/speech/RecognizerIntent)
- [DHU setup and commands](https://developer.android.com/training/cars/testing/dhu)

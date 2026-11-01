# Android Auto handoff evidence

Date: 2026-11-01. Source baseline: `d00d493` (merged PR #51). Experimental version:
`0.3.3-prototype`, code 303. This is a measured phone-app opening experiment, not native Voice control.

## Configuration

- Pixel 10 Pro, Android 17, API 37, security patch 2026-10-05.
- Android Auto 17.9.664044-release (179664044).
- DHU 2.0-windows, build 2022-03-30-438482292, protocol 1.7, default configuration.
- Negotiated Car API 8, observed as `car.onCreateScreen api=8`.
- ChatGPT 1.2026.293 (2629319); Claude 1.261029.20 (26102920).
- German UI, font scale 1.3, local Google TTS, existing output-check mode.
- Installed by ordinary `adb install -r`, without origin override, uninstall, data reset or scan.
- The Pixel 6 Pro was considered earlier but was disconnected before installation. No test result
  in this directory is from that phone.

See [versions](versions.json), [registered car service](car-service.txt) and [machine-readable results](result.json).
The APK hashes in the result identify the installed experiment and the ordinary rollback build.
APKs remain outside Git in `diagnostics/handoff/apks/`.

## Results

| Check | Result | Evidence / scope |
| --- | --- | --- |
| JVM tests | PASS, 110 | 22 core, 21 providers, 67 voice; no failures or skipped tests. |
| Full CI task list locally | PASS | All module lint tasks, debug APKs and test APKs; JDK 17, Gradle 9.8.2. |
| Opted-in build and lint | PASS | `-PcarHandoffExperiment=true`; final experimental APK built and installed. |
| Device tests | PASS, 16 | [Instrumentation output](instrumentation.txt); fake launch/audio adapters plus existing car contracts. |
| Launcher and experiment UI | PASS | Real host clicks; [entry](dhu-entry.png), [two app actions](dhu-actions.png). |
| ChatGPT opening | PASS on this device | Actual DHU action, then target resumed; later reopen also focused: [observer record](chatgpt-reopen.json). |
| Claude opening and reopening | PASS on this device | [First observation](claude-open.json), [reopen after stopped output](claude-after-stop.json); both resumed and focused. |
| Honest feedback | PASS | [Requested, not confirmed](dhu-requested.png); no external Listening/Speaking status. |
| Duplicate click after dispatch | PASS | Clicking the disabled Claude action left the final-process dispatch count at one before the subsequent deliberate reopen. |
| Fixed TTS sample completes | PASS technically | TTS start/end and successful audio focus. No provider request. |
| Page exit during fixed TTS | PASS technically | Active stop at 01:56:34.273, completion of stop calls after 18 ms; no normal TTS-end callback for that interrupted sample. |
| Native app after output stop | PASS | Claude resumed/focused after the interrupted output; see observer record above. |
| Physical audible overlap | PENDING | No listening confirmation for this particular test run. Log callbacks alone do not prove acoustic silence. |
| Live microphone handoff | NOT RUN | The DHU package has no RECORD_AUDIO grant. Approval for a temporary ordinary Android permission grant was requested. Permission remains denied. |
| Locked phone, missing app, release failure/timeout, late callback | PASS in fixtures | JVM/device tests; no physical-keyguard or uninstalled-native-app test claimed. |
| Forced cold process | NOT RUN | Official apps were not force-stopped. Their process state before the first attempt was not recorded; later attempts are reopens. |
| Native Voice, accounts and MCP | UNKNOWN | No test question, account change, notification reading or approval confirmation. |
| Calls, headsets, real vehicle | DEFERRED | Not part of this run. |

The observer starts before the manual DHU click. Its `firstObservationMs` is an observation-window
offset, not an app-launch latency. The first preliminary observer queried the wrong window section
for focus; the committed records use Android 17's `window displays` section and explicitly record
that the focus field was present. Raw activity/window dumps and native chat screens were not saved.

## Selected diagnostic observations

These fixed Logcat excerpts were copied from the test command output while the events were available.
The shared device ring buffer had rotated before the final bulk export, so this is not a complete
continuous log. No content-bearing system logs, device identifiers or native-app screens are included.

```text
11-01 01:52:15.726 I/Lautwerk(14427): handoff.phase=RELEASING
11-01 01:52:15.762 I/Lautwerk(14427): handoff.dispatch target=CHATGPT result=REQUESTED stage=DISPATCH failure=NONE
11-01 01:52:15.762 I/Lautwerk(14427): handoff.phase=REQUESTED

11-01 01:54:13.646 I/Lautwerk(14427): handoff.phase=RELEASING
11-01 01:54:13.690 I/Lautwerk(14427): handoff.dispatch target=CLAUDE result=REQUESTED stage=DISPATCH failure=NONE
11-01 01:54:13.690 I/Lautwerk(14427): handoff.phase=REQUESTED

11-01 01:56:29.121 I/Lautwerk(14427): voice.tts.ready local=true
11-01 01:56:29.125 I/Lautwerk(14427): voice.focus capture=false granted=true
11-01 01:56:29.127 I/Lautwerk(14427): voice.tts.start
11-01 01:56:34.273 I/Lautwerk(14427): voice.session.stop.request active=true
11-01 01:56:34.291 I/Lautwerk(14427): voice.session.stop elapsedMs=18
```

The first development build used the car display context and returned a generic BLOCKED result.
Its exception category was not recorded, so it is not evidence of a background-launch-policy denial.
The final build uses the application context, records fixed stage/failure categories without raw
exceptions, and passed the observed opening tests. A regression test ensures the dispatcher uses
the phone application context. Host-truncated explanatory text was shortened and visually rechecked.

## Restoration and decision

All recorded system settings match: [before](settings-before.json), [after](settings-restored.json).
The temporarily selected microphone-test mode was restored to PLAYBACK through the existing phone UI
without starting capture. No permission was granted or revoked. The DHU was closed normally and the
test's port-5277 forwarding removed. The experimental APK remains installed; the main assistant,
provider selection, keys, voices and history were not changed. The screen title and host color behavior
remain as before, with one explicitly opted-in extra entry.

**GO for the narrow technical capability:** real parked DHU actions can open both official apps on
this Pixel 10 Pro / Android 17 / Android Auto combination. This is not a guarantee for other hosts,
versions, locked phones or native app configurations.

**Full audio-handoff acceptance remains open:** obtain the microphone permission decision, verify
host-capture stop before dispatch, and confirm no audible overlap. Forced-cold and physical-keyguard
checks also remain outstanding. Keep the PR as a draft until the result is reviewed.

**NO-GO for claiming a complete personal-assistant integration:** no public contract for starting or
ending native Voice, detecting its state or pending MCP permission was established. Android Auto
category/distribution eligibility and cross-package audio ownership remain separate unresolved issues.

See the [experiment procedure and rollback](../../HANDOFF_EXPERIMENT.md).

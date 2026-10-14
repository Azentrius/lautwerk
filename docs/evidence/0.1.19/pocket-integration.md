# Pocket/Alba integration validation — 2026-10-14

## Build and device

- OpenAuto AI 0.1.19 / code 119; both applications follow the existing shared version rule.
- Base: main `c262a3a`. Phone and DHU debug APKs installed as updates without clearing data.
- Pixel 10 Pro, Android 17 / API 37; system TTS remained `com.google.android.tts`.
- Companion: `org.pockettts.android.engine.openautotest`, 0.5.3-openauto-cancel.1 / 24,
  exact installed German `german-fp32::alba-mackenna-casual` voice. No INTERNET permission.
- Android Auto 17.8.663874-release. DHU executable 2.0-windows,
  build 2022-03-30-438482292.
- The user explicitly authorized updating the separate DHU package while retaining its
  experimental `com.android.vending` installer attribution and any required 0.1.19 scans.
  Both updates succeeded without a new scan dialog. This attribution is not real Play
  distribution or evidence of category eligibility. No host checks were changed.

## Automated results

| Check | Result |
| --- | --- |
| Core, providers and voice JVM suites | 100 passed, zero failures/errors |
| Android lint, every module and both prototype flavors | Zero errors/fatal issues; warnings recorded in build-summary.json |
| Three debug APKs and both instrumentation APKs | Built successfully |
| Version/CHANGELOG check | Passed: 0.1.19 |
| Assistant standard device suite | 84 passed; two opt-in audio tests skipped in the 86-test runner result |
| Assistant explicit Pocket suite | Both opt-in tests passed with Wi-Fi and mobile data disabled |
| Car standard device suite | 19 passed; three opt-in tests skipped in the 22-test runner result |
| Car explicit Pocket service suite | One test passed with Wi-Fi and mobile data disabled |
| Speech-selection preservation | Before/after preference hashes equal in all four runs |
| Device settings restoration | Wi-Fi, mobile data, airplane mode, Bluetooth and system TTS equal before/after |
| Reproducible model pack | CRC, original model files and original Alba audio checked; original bytes retained |

The runner reports assumption-skipped tests in its `OK (N tests)` count. They are
not counted as passes above. The other two opt-in car tests (existing playback and
external-PCM recognition) were not requested in this integration run. Call and
headset tests remain deferred. The initial car-run harness rejected a missing
speech preferences file before instrumentation; handling absence as an empty map
fixed the harness. The successful rerun is the evidence retained here.

The tests used synthetic German sentences only, with no provider requests and no
microphone recognition. Active default network was `none` during the explicit
Pocket suites. Existing keys, chats, provider selection and the user's selected
voice were not changed. The car service test restores its non-secret mode/status
map as well as the voice map. Voice preference hashes in the phone report are raw
file hashes; car reports hash a canonical preference map (empty and absent match).

## What the audio checks establish

- The fixed phone sample uses the explicit Alba engine and spells the brand suffix.
- Preview stop, immediate restart and page close complete without a late completion
  changing the new preview's state.
- The phone conversation-output adapter cancels after 50 ms and during playback,
  starts the replacement immediately and receives no stale completion callbacks.
- The real car service's existing output-only mode stops during preparation and
  playback, then completes a fresh output without an API or microphone request.
- Engine events include cancellation before the first audio block and cancellation
  during playback, followed by successful new synthesis. `SYNTH_AUDIO_REJECTED`
  on a cancelled Android playback sink is followed by `SYNTH_CANCELLED`; the new
  utterance completes. This is expected cancellation handling, not a failed restart.

These are software callback/state checks on the Pixel. They do not measure acoustic
latency or prove car-speaker routing. The earlier standalone engine evaluation has
separate measured latency and user-confirmed audible cancellation; see POCKET_TTS.md.
No new audible acceptance or live provider conversation is claimed for this integration.

## Actual DHU and vehicle scope

The current attempt to connect the DHU could not establish a link: the Head Unit
Server was not reachable through the normal forwarded port. A request to start it
is pending. No new host API level, actual-host screenshot or DHU audio result is
claimed yet. Car template tests passed, but they do not establish launcher visibility.
The MessageTemplate, car screen, host validation and microphone path have no diff.
ID.7, call and headset checks remain deferred.

## Remaining setup limits

Alba is selectable; it has not been silently selected for the user. Each app keeps
its own choice. The corrected companion and model remain separate installations,
not bundled in the OpenAuto AI APK. The new pack's bytes and manifest were checked;
its manual importer UI was not exercised in this run, which used the already
installed equivalent Alba fixture. German and normal speed are the evaluated
configuration. English retains its separate Android voice choice.

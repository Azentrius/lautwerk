# Pocket/Alba integration validation – 2026-10-14

## Build and device

- OpenAuto AI 0.1.19 / code 119; both applications follow the existing shared version rule.
- Base: main `c262a3a`. Phone and DHU debug APKs installed as updates without clearing data.
- Pixel 10 Pro, Android 17 / API 37; system TTS remained `com.google.android.tts`.
- Companion: `org.pockettts.android.engine.openautotest`, 0.5.3-openauto-cancel.1 / 24,
  exact installed German `german-fp32::alba-mackenna-casual` voice. No INTERNET permission.
- Android Auto 17.8.663874-release. DHU executable 2.0-windows,
  build 2022-03-30-438482292.
- The maintainer approved updating the separate DHU package while retaining its
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
Pocket suites. Existing keys, chats, provider selection and the selected
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
separate measured latency and manually confirmed audible cancellation; see POCKET_TTS.md.
The visible DHU output was subsequently confirmed audibly in a manual check, as recorded
below. The later live Claude acceptance is recorded in its own section.

## Actual DHU and vehicle scope

A normal forwarded-port connection was established after the initial unavailable
server/connection attempt. Android Auto 17.8.663874-release, DHU 2.0-windows
(2022-03-30-438482292), profile 1280x720-touch; the new session logged
`car.onCreateScreen api=8`.

The app opened through its existing Android Auto icon. Its title, provider/model,
MessageTemplate and Start/Stop control remained unchanged. The phone setup UI was
used to select output-only mode and Pocket/Alba temporarily. No microphone or
provider request was made. Host checks remained enabled.

The first headless attempt produced successful TTS callbacks and UI changes, but
nothing was audible. It is **not** an audible pass. The DHU was then restarted
with its normal visible window. The identical fixed-sentence stop/restart test
completed, and the tester confirmed audible PC-speaker output, clean
stopping and a complete second sentence without overlap. This establishes output
through this DHU configuration, not an ID.7 or other vehicle result.

In the visible run the app's stop handler took 9 ms in its log; this is software
handler time, not a measurement of acoustic stop latency. One earlier explicit
stop logged `SYNTH_AUDIO_REJECTED`/`SYNTH_FAILED` at the closed playback sink; no app
error appeared and the following output completed. Cancellation can race that
engine diagnostic classification; it is retained in the evidence, not hidden.

The previous car voice (Android local) and mode (Conversation) were restored via
the UI and the checked controls verified. The credential file's modification time
remained on the prior day; its contents were not read. The assistant voice and
system TTS default were unchanged. The DHU and temporary ADB forward were closed.
Evidence includes scoped engine/app events and the manual acceptance result.
Raw host screenshots are not published because their adjacent Maps pane includes
unrelated location information.

ID.7, call and headset checks remain deferred. This fixed-output acceptance does
not replace a later live provider conversation with Alba in the parked vehicle.

## Live Claude conversation acceptance

Later on 2026-10-14, Pocket/Alba was selected in the
car app and left selected. Conversation mode and Claude Haiku 4.5 were verified in
the existing UI. Recognized questions are sent to
Anthropic and can consume API credit. No API key or archived conversation was
read by the test tools. The assistant app and system-wide TTS default were unchanged.

The visible DHU used the same configuration as the successful fixed-output check.
The tester asked a question through the PC microphone, heard the answer, asked a
follow-up and heard the second answer. After ending that session, the tester started
again, asked another question and pressed the car's Stop button during the answer.
The tester reported that the complete scenario worked.

The scoped diagnostic events independently show:

- Two `CONVERSATION car=true` sessions with `local=true pocket=true` output.
- Three successful recognition results and three Thinking-to-Speaking transitions.
- Two complete speech callbacks, with listening re-entered between questions.
- The third answer cancelled by the car Stop action. The app's stop handler logged
  13 ms; this is not acoustic latency. The engine recorded cancellation and no later
  speech completion or automatic restart followed.

The first session ended when the host closed capture after the second completed
answer (`voice.car.capture.hostStopped`), then the app stopped. There is no car
Stop-button event for that first termination, so it is not counted as a separate
button-stop measurement. The button stop during the second session is explicit.
The tester reported no visible or audible problem. This distinction is retained in
the evidence rather than treating every stop as the same event.

Only state/engine diagnostics are included in `dhu-live/`; no recognized words,
provider responses, recorded audio, credentials or screenshots containing Maps
data are exported. The existing app retains its normal encrypted conversation
history. No conversation was deleted by the test tools.

The DHU and temporary ADB forward were closed after acceptance. Alba and
Conversation mode remain selected in the car app. This is a real
Claude/DHU acceptance, not a vehicle, call or headset test; those remain deferred.

## Remaining setup limits

Alba is selectable and was explicitly selected for the car app in the later live
acceptance above. Each app keeps its own choice. The corrected companion and model remain separate installations,
not bundled in the OpenAuto AI APK. The new pack's bytes and manifest were checked;
its manual importer UI was not exercised in this run, which used the already
installed equivalent Alba fixture. German and normal speed are the evaluated
configuration. English retains its separate Android voice choice.

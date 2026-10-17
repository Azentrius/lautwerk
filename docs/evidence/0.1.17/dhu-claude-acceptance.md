# Claude DHU follow-up – 2026-10-13

This report supplements the earlier [car voice evidence](../../CAR_VOICE.md). It records a
manual follow-up, not a new application build or an automated test suite. Times below are CEST
(Europe/Berlin, UTC+02:00). No recognized words, generated answers, keys or raw audio are included.

## Environment and installed artifact

| Item | Evidence |
|---|---|
| Device | Pixel 10 Pro, Android 17 / API 37 |
| Android Auto | 17.8.663874-release, code 178663874, checked for this retest |
| DHU | 2.0-windows, build 2022-03-30-438482292; 800×480 touch profile |
| Host Car API | Earlier run recorded `car.onCreateScreen api=8`; the fresh connection event was not retained in this evening capture. Do not count it as remeasured |
| Installed car package | `dev.openautoai.prototype.dhu`, 0.1.17-prototype / code 117 |
| Installed car APK SHA-256 | `a5da94c143c9f5984fc4c5d001e19edaa5629cb9438a165a5580f60ec2b6cccf`, matched after pulling the installed base APK |
| Separate assistant | `dev.openautoai.app`, 0.1.18 / code 118; not the car test target |
| Selected mode/provider/model | Conversation / Claude (Anthropic) / Claude Haiku 4.5 |

The existing car APK, signing identity and installation were retained. No reinstall, change of
origin, root patch, host-check change or new Play Protect submission took place for this retest.

## Technical observations

Selected events from app-tag-only capture (`OpenAutoAI`) establish the following sequence.
The capture covers part of the evening session; it is not a complete acoustic recording or a
benchmark. Log capture does not establish whether the user heard every completed TTS utterance.

| Time | Observation |
|---|---|
| 23:20:14.286 | Explicit car Start; session mode CONVERSATION, car=true |
| 23:20:25.708 | Recognized input moved to THINKING |
| 23:20:27.178–23:20:41.648 | SPEAKING, TTS start, then `voice.tts.end success=true` |
| 23:20:42.015 | Next LISTENING phase |
| 23:20:48.212–23:20:51.777 | Next question processed; SPEAKING and TTS start |
| 23:21:25.649 | Explicit car End while that speech phase had no completion event in the capture; local service stop recorded 14 ms |
| 23:25:42.182 | Later explicit car Start, followed by a new CONVERSATION session |
| 23:25:47.458–23:26:02.691 | THINKING, SPEAKING, TTS start and successful completion |
| 23:26:03.056 | Next LISTENING phase |
| 23:26:11.789–23:26:33.629 | Next question processed and spoken, with successful TTS completion |
| 23:26:33.994 | Next LISTENING phase |
| 23:26:35.212–23:26:35.228 | Host capture stopped; recognition ended without text, conversation STOPPED, local service stop 4 ms, inactive template |

The last host-stop event alone does not identify the user's gesture or a transport cause. It must
not be relabeled an explicit End-button click. Likewise the 14 ms and 4 ms values measure local
service work, not the time until sound stopped at the PC speakers. No acoustic latency was measured.

## User confirmation and resolved observation

After the requested question/follow-up/End/restart exercise, the user first reported a remaining
problem. On clarification there was no error text: the answer was still audible after the wave
overlay had disappeared. The user then stated that operation worked overall and explicitly
confirmed that **Speaking and End remained visible during the audible answer**.

This observation matches the implementation: `CarVoiceAudio` closes recognition/capture after
recognized input, then the controller processes the question and starts independent TTS playback.
The controller stays in SPEAKING until the TTS completion callback; the car service provides the
Speaking summary and the active session retains End. The host-owned microphone wave therefore
does not represent the lifetime of the whole conversation. Its disappearance alone is not an
audio failure and did not require a code or design change.

The user confirmation establishes the reported functional Claude DHU flow and resolves this
specific overlay concern. It is not a guarantee of zero buffering, all repeated-start timings,
all providers, every voice or every host.

## Still outside this evidence

- The parked ID.7 native wireless audio test is deferred at the user's request; no vehicle output,
  microphone, conversation or reconnect acceptance is claimed. Kia/AAWireless remains deferred.
- Call and headset/Bluetooth interruption tests remain deferred.
- A complete OpenAI car conversation test is not covered by this Claude retest.
- The A1–A11 host design matrix, current day/night contrast and a repeat-cycle template-budget test
  were not rerun. The current 0.1.18 car build was not installed by this retest.
- Automated checks and the earlier successful microphone echo remain separate evidence; this
  documentation update adds no new device run or API request.

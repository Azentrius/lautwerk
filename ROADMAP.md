# Roadmap

As of 0.1.18 (2026-10-13). What each version changed, with its tests and the checks the user
confirmed, is in [CHANGELOG.md](CHANGELOG.md).

## Where things stand

- 0.1.17 connects the existing car package to independent output/microphone checks and the shared
  conversation controller. The Pixel/DHU microphone echo and the later Claude Haiku 4.5 conversation
  retest have user confirmation. The wave overlay ends with microphone capture; Speaking and End
  remain during the spoken answer. Exact evidence and limits: [CAR_VOICE.md](docs/CAR_VOICE.md).
- 0.1.18 adds the approved styled phone title, updates the read-only design reference and spells AI
  in the fixed phone voice sample. The user accepted the sample. The Pixel retains the separately
  installed 0.1.17 car build, so phone and car evidence must keep their installed versions distinct.
- The user reports that the app now opens from the vehicle launcher after the explicitly authorized
  installation-origin experiment. This is no general distribution or Play-category approval.

- The minimal DHU prototype (0.1.1) and the shared provider and model selection for the phone and the
  car template of the same test app (0.1.2) are confirmed by the user in a real DHU.
- 0.1.14 applies rules B1–B5 of the Android Auto design `d5990f3`: only the approved car color pair and
  the primary color request for "Start". 64 DHU combinations and both six-start tests pass; the host
  replaces the color value with a readable light blue.
- The assistant app has separate provider adapters (0.1.3), encapsulated encrypted storage (0.1.4), one
  voice flow for both providers with voice choice and reliable cancellation (0.1.5–0.1.8), the split
  phone UI with background storage work (0.1.9), text export of single chats (0.1.10), history search
  (0.1.11), German and English (0.1.12) and the approved Azentrius design (0.1.13).
- 0.1.15 adds the approved app icon with its monochrome layer, the native splash screen and the small
  notification icon; 28 selected device tests and the touch and rotary checks, including six starts
  without reset, pass. Details: `docs/APP_IDENTITY.md` and `docs/BUILD_AND_TEST.md`.
- Confirmed by the user: the voice flow with both providers, a clearly better sound with the chosen voice
  (local and online variants can sound alike), stop and restart by button and on screen-off, the local
  text export with a complete, readable file, history search with filters and jumps to hits, the real
  switch of the device language between German and English, and "look and handling fit" for the design.
- Deferred at the user's request on 2026-10-12 and untested: call and headset/Bluetooth interruptions.
  Untested: cloud file providers and rotation during the export picker.
- Device tests with the 0.1.16 toolchain have since run: the 0.1.18 design build passed 78 Pixel
  checks; its pronunciation follow-up passed seven targeted Pixel checks. The isolated emulator
  passed 19 prototype/template checks with two opt-in audio probes skipped. A supplementary full
  assistant emulator run passed 74/78; the failures and baseline comparison remain documented,
  not counted as green. See [design evidence](docs/evidence/0.1.16/README.md).

## Next

1. **Keep the confirmed DHU state.** Preserve the confirmed microphone echo and Claude conversation
   flow, including the host microphone overlay/answer distinction. Regression-test every extension
   separately; this acceptance does not replace the full host design matrix or vehicle checks.
   Resolve the [seven upstream design questions](docs/DESIGN_SYSTEM.md#open-points-to-return-to-the-design-system-maintainer)
   with the maintainer before implementing unspecified presentation. Do not change behavior to fit
   the old counter-only design reference.
2. **A distribution path outside Google Play.** The planned parked ID.7 native wireless audio test
   is deferred at the user's request; Kia/AAWireless remains deferred too. When resumed, check output,
   microphone echo, conversation and reconnect separately, then validate a reproducible installation/
   update path without deleting stored keys. No vehicle audio result is recorded yet. Guide and
   current results: [Sideloading](docs/SIDELOADING.md) and [car tests](docs/SIDELOAD_TEST_REPORT.md).
   No compatibility promise yet. Claim no general AI category; the POI entry stays a disclosed debug
   test placeholder, not a product release.
3. **Provider coverage.** Phone conversations with both providers are user-confirmed; the later car
   retest used Claude Haiku 4.5 in the DHU. OpenAI's full car conversation flow still needs its own
   controlled practical test. Offline adapter tests do not establish API account/model access or a
   vehicle audio result. See `docs/VOICE.md` and `docs/CAR_VOICE.md`. The DHU's two diagnostic modes
   send no AI request; explicit Conversation mode uses the selected provider.
4. **History and archive.** History search (0.1.11) and local text export (0.1.10) are confirmed; check
   cloud destinations and rotation during the picker later. The user confirmed phone handling with real
   conversations with both providers under 0.1.12. The whole archive file is still read in the
   background; plan real paging and size rules for very large archives. Encrypted backup and import,
   moving to another device and explicit restore come later. The text export is not a restore format;
   no silent migration or deletion.
5. **Remaining voice checks.** Call and headset/Bluetooth interruptions remain deferred at the user's
   request, as does the parked ID.7 test. The DHU Claude evidence includes completed answers, return
   to listening, an explicit stop and a later explicit restart; it is not a measured acoustic stop
   benchmark. Validate other providers, prolonged/repeated sessions and actual vehicle audio before
   claiming them supported. The car test app still uses its own local TTS choice rather than the
   separate assistant's saved voice setting.
6. **Model catalog.** Maintain it deliberately and offer model migrations explicitly; never change an
   existing selection or a chat's model unnoticed.
7. **Test environment and Android 17.** Isolate the mixed-locale assistant emulator suite and recheck
   its documented failures. Keep successful Pixel tests separate from that unresolved emulator run.
   Raise the target SDK to 37 only after testing the Android 17 behavior changes on the device;
   running on Android 17 with target SDK 36 is not that validation.
8. **Repository.** Maintain the source in [Azentrius/openauto-ai](https://github.com/Azentrius/openauto-ai)
   with its CI and release workflow. Weigh real test results before adding more car functions.

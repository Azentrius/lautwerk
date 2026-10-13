# Roadmap

As of 0.1.17 (2026-10-13). What each version changed, with its tests and the checks the user
confirmed, is in [CHANGELOG.md](CHANGELOG.md).

## Where things stand

- 0.1.17 connects the existing car package to independent output/microphone checks and the shared
  conversation controller. The Pixel/DHU microphone echo is user-confirmed; provider and vehicle
  audio validation are tracked separately in [CAR_VOICE.md](docs/CAR_VOICE.md).
- The user reports that the app now opens from the vehicle launcher after the explicitly authorized
  installation-origin experiment. This is no general distribution or Play-category approval.

- The minimal DHU prototype (0.1.1) and the shared provider and model selection for the phone and the
  car template of the same test app (0.1.2) are confirmed by the user in a real DHU.
- 0.1.14 applies rules B1–B5 of the Android Auto design `7fe60bf`: only the approved car color pair and
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
- 0.1.16 moves the build to AGP 9.4.1, Kotlin 2.4.21 and Compose BOM 2026.09.00; the device tests with
  this toolchain are still open.

## Next

1. **Keep the confirmed DHU state.** Selection and Start are confirmed in a real DHU since 0.1.2.
   Regression-test every extension separately.
2. **A distribution path outside Google Play, first.** Check the unchanged APK in the ID.7 over native
   wireless Android Auto, parked at first. Kia/AAWireless is deferred at the user's request. Guide and
   current results: [Sideloading](docs/SIDELOADING.md) and [car tests](docs/SIDELOAD_TEST_REPORT.md).
   No compatibility promise yet. Claim no general AI category; the POI entry stays a disclosed debug
   test placeholder, not a product release.
3. **Live tests of the provider adapters.** Test the existing OpenAI and Claude adapters with own API
   accounts, under control. Error cases and Android's JSON are tested offline; model access and voice end
   to end count as shown only after a confirmed practical test, see `docs/VOICE.md` and `docs/CAR_VOICE.md`.
   The DHU's two diagnostic modes send no AI request; its explicit Conversation mode uses the provider.
4. **History and archive.** History search (0.1.11) and local text export (0.1.10) are confirmed; check
   cloud destinations and rotation during the picker later. The user confirmed phone handling with real
   conversations with both providers under 0.1.12. The whole archive file is still read in the
   background; plan real paging and size rules for very large archives. Encrypted backup and import,
   moving to another device and explicit restore come later. The text export is not a restore format;
   no silent migration or deletion.
5. **Voice in practice.** Check the implemented voice interfaces: microphone permissions, audio focus,
   audible text-to-speech, resuming and cancelling with both API accounts. Deferred at the user's
   request: the call and headset/Bluetooth interruptions from 0.1.8; there is no confirmed result yet.
   `CarAudioRecord` and local recognition now pass the independent DHU echo check. Validate complete
   conversations, stop/restart and the actual vehicle audio path before claiming them supported.
6. **Model catalog.** Maintain it deliberately and offer model migrations explicitly; never change an
   existing selection or a chat's model unnoticed.
7. **Toolchain on the device.** Run the device tests with the 0.1.16 toolchain
   (`:app:connectedDebugAndroidTest`, `:prototype-app:connectedDhuDebugAndroidTest`) and a DHU session;
   raise the target SDK to 37 only after testing the Android 17 behavior changes on the device.
8. **Repository.** Maintain the source in [Azentrius/openauto-ai](https://github.com/Azentrius/openauto-ai)
   with its CI and release workflow. Weigh real test results before adding more car functions.

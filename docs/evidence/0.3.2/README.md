# Return to phone speech voices, 0.3.2

Date: 2026-10-19. Base: design PR 46 at `8eb3288`, retained unchanged beneath this change.
Version 0.3.2 / 302. No provider requests, calls or headset tests.

## Changes

- Remove the optional companion connection from both speech adapters and the preview, its choice
  and availability checks from both phone pages, and its package visibility entry and error texts.
- Remove companion playback/selection tests, model-pack builder, cancellation patch and now-unused
  attribution assets. The current setup guide becomes a retirement notice linking to the historical
  implementation; historical evidence and design files are unchanged.
- Keep the shared `speech-output` module for Android voice preferences, language handling and rate.
- Retain the system speech service, assistant voice picker, online-voice confirmation, rate controls,
  preview stop/callback protections and the car's existing local-voice selection policy.
- Retire only saved selections of the former companion package. The package identifier survives
  solely as an exact migration match and in its test fixture, never as a discovery/binding target.
  Legacy and localized choices are handled; unrelated settings and other engines are preserved.
- The host template, action labels from PR 46, B2/B3, recognition, provider flow and chat/key storage
  are unchanged. The app update does not uninstall separate packages. The later device cleanup below
  was explicitly requested by the user.

## Validation

| Check | Result |
| --- | --- |
| Version and changelog | `tools/check-version.sh` returned 0.3.2 |
| JVM tests | 100 passed: core 22, providers 21, voice 57 |
| Android lint | All seven CI targets completed, zero errors and 33 warnings (same count as the base) |
| Debug and instrumentation builds | Every task in `.github/workflows/android.yml` succeeded with JDK 17 in 2m 20s |
| Packaged integration | No removed companion class, UI resource, error identifier or attribution asset in any of the three debug APKs |
| Historical files | 341 existing evidence/design files have identical SHA-256 hashes; previous changelog entries are unchanged |
| New migration tests | All four instrumented fixtures passed on the Pixel: localized choices, legacy choice, coexistence with legacy phone voice, preservation of other engines |
| Pixel tests and installation | Assistant and DHU apps installed with ordinary `adb install -r`, both 0.3.2 / 302; 23 phone and 18 car tests passed on the final runs |
| Audio and host acceptance | One fixed-text car service playback test passed on the Pixel; two local phone previews were confirmed audible by the user, including clean stop/restart. No DHU or vehicle session |

`build-result.json` contains suite counts, lint counts, history verification and hashes of the three
debug APKs. The source diff was checked with `git diff --check`.

## Pixel installation, phone voices and removal of the Alba helpers

The user connected and unlocked the Pixel and explicitly requested installing 0.3.2, testing phone
voices without a provider request, and removing the apps used for Alba. The assistant, DHU variant
and their two test APKs were installed with ordinary `adb install -r`. No Lautwerk application data was cleared,
no signature conflict occurred and no installer-origin override or security bypass was used.
The phone-only UI comparison prototype was not installed. Both installed runtime apps report code
302; the DHU package registers exactly one `com.azentrius.lautwerk.car.LautwerkCarService`.

The first phone run had one failure out of 23: the existing real-engine test expected
`TextToSpeech.isSpeaking()` to be false. Its cause is not established. The isolated four-test
speech-settings recheck and the complete 23-test phone rerun both passed without changing app or test
code. All 18 selected car tests also passed. The original failure output is retained, not overwritten.

The real Google phone engine exposes five local German voice entries and four online entries. Fixed
previews used `de-de-x-dea-local` and `de-de-x-deb-local`; no online voice was selected. The second
preview was also stopped and immediately restarted through the visible buttons. The user confirmed
both voices audible and understandable, and the stop/restart clean without overlap. No hardware stop
latency was measured. The assistant's original Android-default choice and speech preferences were
restored to their original values.

The auto app's opt-in `VoicePlaybackDeviceTest` ran separately on the Pixel using fixed text and no
microphone or provider. It passed, including the repeated-start assertion. Logs show local TTS ready,
audio focus granted, TTS start/end with success, and STOPPED without a problem. This is a phone playback
check of the car service, not a DHU/vehicle route or launcher test. Its audibility was not separately
confirmed by the user.

The two installed Alba helpers, `de.azentrius.lautwerk.audiocompare` and
`com.azentrius.lautwerk.albaprobe`, were uninstalled including their app-private data. The former
`org.pockettts.android.engine.openautotest` engine and `dev.openautoai.pocketprobe` test app were already
absent. All four are absent after cleanup; no other installed package was removed. Google TTS and the
Lautwerk apps remain installed. Desktop research files and historical evidence were not removed.

Language, theme, font scale, radios and the default TTS engine match the pre-test snapshot. No API keys
or conversation content were read. No provider, recognition, call, headset, DHU or vehicle test ran.
Evidence and the exact selected suites are in `device/result.json` and the instrumentation transcripts.

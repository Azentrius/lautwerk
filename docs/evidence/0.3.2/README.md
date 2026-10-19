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
  are unchanged. Separately installed engines, model files and diagnostic apps are not uninstalled.

## Validation

| Check | Result |
| --- | --- |
| Version and changelog | `tools/check-version.sh` returned 0.3.2 |
| JVM tests | 100 passed: core 22, providers 21, voice 57 |
| Android lint | All seven CI targets completed, zero errors and 33 warnings (same count as the base) |
| Debug and instrumentation builds | Every task in `.github/workflows/android.yml` succeeded with JDK 17 in 2m 20s |
| Packaged integration | No removed companion class, UI resource, error identifier or attribution asset in any of the three debug APKs |
| Historical files | 341 existing evidence/design files have identical SHA-256 hashes; previous changelog entries are unchanged |
| New migration tests | Four instrumented fixtures compiled: localized choices, legacy choice, coexistence with legacy phone voice, preservation of other engines |
| Pixel tests and installation | Pending. ADB had no connected Pixel; the user was asked to reconnect it |
| Audio and host acceptance | Not run for this change. No claim of audibility or a new DHU/vehicle acceptance |

`build-result.json` contains suite counts, lint counts, history verification and hashes of the three
debug APKs. The source diff was checked with `git diff --check`.

When the Pixel is available, install the assistant and DHU APKs plus their test APKs with ordinary
`adb install -r`. Stop on any signature conflict. Run `am instrument -w`, keeping the apps installed.
The intended device scope is RetiredVoiceSelectionTest, SpeechSettingsTest, SpeechSettingsScreenTest,
AndroidVoiceEnvironmentTest, AndroidVoiceTasksTest, DesignUiTest, CarActionLabelTest, CarTemplateTest,
CarDesignContractTest, PhoneSmokeTest, CarPhoneDesignTest and FinalSpeechListenerTest. Do not enable
audioPlayback or externalPcm for the silent automated run. Any audible follow-up uses fixed local
test text, not a provider conversation. Preserve language, theme, font scale, radios and default TTS.

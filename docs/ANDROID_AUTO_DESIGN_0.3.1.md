# Android Auto design alignment for 0.3.1

Reference: [Azentrius/design-system at 1d1a742](https://github.com/Azentrius/design-system/blob/1d1a742f7e54ddc98d2d6b99ff4cf5ee5ef02ad2/android-auto.md), read on 2026-10-19.
The companion `android.md` is used only for the V3 phone-counter labels referenced by the Auto rules.
The design repository and the existing local reference snapshot are unchanged. No upstream script ran.

Baseline: `origin/main` at `ca721c5`, version 0.3.0. This change applies V3's explicitly specified labels;
it does not redesign the host template or change conversation, provider, permission or storage behavior.
The separate Alba regression experiment does not patch the production speech engine.

## App contract

| Rule | Code location | Result |
| --- | --- | --- |
| B1: MessageTemplate, plain title, APP_ICON, one message and one action | `car-ui/.../LautwerkScreen.kt` | Fulfilled. No action strip, second action, content icon or loading indicator. |
| B2: one gold/bronze pair, no secondary pair or night override | `prototype-app/src/dhu/res/values/car_styles.xml`, DHU manifest | Fulfilled. `#C8A96E` and `#7D5F22` remain unchanged. |
| B3: PRIMARY background for every label, no text tint, icon or FLAG_PRIMARY | `LautwerkScreen.onGetTemplate`, `CarDesignContractTest`, `CarActionLabelTest` | Fulfilled. Includes active states and all three modes. |
| B4: title, state then full provider/model, localized plain text | `LautwerkScreen`, `car-ui/src/main/res/values*/strings.xml` | Fulfilled. No app-side truncation, font span or extra punctuation in the car title. |
| B4/V3: mode-specific start action | `LautwerkScreen`, `VoicePreferences.observeMode` | Adjusted. Conversation uses Start conversation; output and microphone checks use Start audio check. German values follow the specification exactly. |
| V3: live mode changes without reconnecting | `LautwerkScreen` lifecycle observers | Adjusted. Observe only the mode preference while visible. Mode changes during active sessions do not add template refreshes; End remains visible. |
| V3: related instructions and phone counter | `car-ui/.../voice_strings.xml`, `prototype-app/.../strings.xml`, DHU `voice_note.xml` | Adjusted. Existing phone counter is Start test. Setup instructions name the correct car actions. Start on phone is unchanged. |
| B5: host owns type, geometry, background and actual colors | Car template and theme | Fulfilled. No phone fonts, status pills, extra icons or custom surface values are sent to the host. |
| O1: existing speech-bubble app icon | App/prototype manifests and launcher resources | Fulfilled. Existing launcher resources and host mask remain unchanged. |
| State texts, active selection and cached action intent | `LautwerkScreen`, `VoiceConnection`, `CarVoiceService` | Fulfilled for existing behavior. The state machine and Start/End click behavior are retained. Tests use a fake audio port. |
| O2/O3: measured contrast or clipping requires a decision | Host acceptance | Open until a current host-rendered visual check. Do not substitute arbitrary colors or shorter labels. |
| O4: reduce intermediate states only after failing the baseline comparison | Template quota acceptance | Open. No throttling or state suppression is introduced without the specified baseline result. |
| O5: relocalize a previously saved outcome | `VoicePreferences.lastOutcome` | Open. Requires changing stored outcome representation and error behavior; it is kept separate from this display-only change. |

`VoicePreferences` retains its original file name, keys, values and default mode. The new injectable
constructor enables tests to use isolated preferences. The observer ignores stored outcome changes.
Its strong listener reference is retained until the lifecycle subscription closes.

## Host acceptance

Automated template tests cannot prove rendering, contrast, launch visibility, speech-wave overlays,
rotary focus or the host template quota. Earlier 0.1.14 and 0.2.0 results are historical evidence only.

| Criterion | Current scope and limitation |
| --- | --- |
| A1 | Plain Lautwerk title and APP_ICON checked in the template; host visibility requires a DHU screenshot. |
| A2 | Exact start/end labels checked in German, English and unsupported-language fallback for every mode. Host clipping still requires inspection. |
| A3/A4 | Packaged requested colors checked in day/night configurations. Actual host pixels and contrast are not inferred from those values. |
| A5 | Full state/provider/model strings retained. Small-screen clipping remains a host check. |
| A6 | Fake-port Start/End delegate tests preserve intent. No real provider session is started. |
| A7 | Full baseline/repeated-conversation acceptance remains open because this task excludes provider requests. |
| A8 | Host rotary focus is not proved by instrumentation. |
| A9 | New labels checked in German, English and French fallback. Previously stored outcome localization remains the known O5 issue. |
| A10 | Isolated device tests cover live mode and model selection, inactive lifecycle and active End label. |
| A11 | Existing truthful states retained. The host speech-wave behavior is not re-tested here. |

Call, headset and vehicle tests remain deferred. Actual results and any rendered DHU checks are recorded
in `docs/evidence/0.3.1/`; this table does not convert an unrun check into a pass.

## Notes for the design-system maintainer

- The current document still states an eleven-character title; Lautwerk has eight characters.
- Its baseline library/version and static length examples describe older source. This change keeps
  the repository's pinned library and current full model labels rather than downgrading dependencies
  or changing the model catalog to fit those examples.
- O5 is a separately described storage/behavior change. It is not silently included in a visual update.
- The wider prototype phone-page styling and key-field V2 behavior in `android.md` are outside this
  Android Auto surface change. Only its explicitly linked V3 copy is updated here.

## Executed validation

All 100 JVM tests, seven lint targets and all CI debug/test APK builds succeeded. On the Pixel,
13 instrumented tests passed: two action-label/lifecycle tests, seven existing car contract/template
tests and four shared prototype phone UI tests. Device settings were unchanged.

A normal headless DHU connection attempt did not deliver video focus. Two screenshot requests failed;
no negotiated host Car API was observed. The session was closed normally. No host rejection was bypassed.
Therefore the host-dependent acceptance rows above remain open. See `evidence/0.3.1/README.md`.

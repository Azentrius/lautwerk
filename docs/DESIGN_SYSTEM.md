# Azentrius design audit — 0.1.18

Reference: [Azentrius/design-system at edd4c67](https://github.com/Azentrius/design-system/tree/edd4c67da6d7c606296c3bbd13d7aa5da548f452),
specifically `android.md`, `android-auto.md` and `tokens.json` in
`design/azentrius/upstream-edd4c67/`. The complete 134-file tree is unchanged and byte-verified.
Historical copies remain available; [reference metadata](../design/azentrius/README.md) describes them.
The upstream repository was read only. No upstream scripts were run.

Final baseline: application `main` at `f1982ba8eea276be770a370749e9b79aeb721c0c`, version 0.1.17/code 117.
The work began on `c862095` (0.1.16); PRs #27 and #28 merged during the audit. This branch was
rebased onto the new main, preserving the car voice implementation and dependency updates.
The maintainer approved **0.1.18/code 118**, retaining the shared version rule, because 0.1.17 is
used by the now-merged voice PR. Both apps receive 118; the prototype phone UI changes too.
PR #29 subsequently updated upstream commit references only; its documentation and CarAppTheme
comment changes are retained. The device-tested application code baseline remains `5a3520b`.
This PR does not rewrite the merged conversation behavior. After the design review, the
maintainer separately requested a pronunciation correction in the fixed phone voice sample:
only its AI suffix receives a spelling annotation. This is an explicitly approved follow-up,
not behavior inferred from the design system. The sample wording and visible name are unchanged.

Statuses: **fulfilled** = existing implementation meets the applicable rule;
**adjusted** = purely visual correction in this PR; **open** = conflict, missing specification,
or validation that cannot be inferred from source. Absence of an optional feature is stated
explicitly; it is not a reason to add behavior. All paths below are relative to the repository.

This audit records the design change merged in PR #30. The subsequent
[Claude DHU retest](CAR_VOICE.md#evening-claude-retest--2026-10-13) used the retained 0.1.17 car APK,
not the newly built 0.1.18 car variant. It does not close the unrun A1–A11 design checks below.
The seven upstream questions are expanded into an actionable handoff near the end of this report;
none is marked resolved and no read-only reference file is changed.

## Android: units, typography, touch, layout and state

| Rule | Code location | Result |
|---|---|---|
| Device light/dark theme; no dynamic colors | `app/.../design/AzentriusTheme.kt`; `prototype-app/src/main/res/values[-night]/az_theme.xml` | adjusted — assistant already follows the system; native prototype now uses the supplied light/dark page and text palette. |
| dp for dimensions, sp for type and line height | `AzType`, `AzComponents.kt`, `AzScreens.kt`; prototype `styles.xml` | fulfilled — dimensions scale independently from text; no fixed-height text. |
| CSS RGBA to Android ARGB | `app/.../design/AzPalette.kt` | fulfilled — backdrop conversions and all palette entries checked against the reference. |
| 1 dp borders; 2 dp focus ring with 2 dp gap | `AzComponents.kt:focusRing`; `AzScreens.kt`; `PrototypeButtonBackground.kt` | fulfilled — outer stroke center is 3 dp from the element. Input focus uses its specified 2 dp border. |
| Pill based on actual dimensions | `AzComponents.kt:pill`; `PrototypeButtonBackground.kt` | fulfilled — 50% corners / half the measured height, no arbitrary large radius. |
| Six application text styles, weights and line heights | `AzentriusTheme.kt:AzType`; prototype `styles.xml`, `activity_main.xml` | adjusted — assistant already matches; native prototype title, body, labels and button use supplied styles. |
| Zero letter spacing and no added font padding | `AzType.style`; prototype `styles.xml`, `activity_main.xml` | fulfilled — explicit zero spacing and disabled extra padding. |
| 200% font, growing content and stacked actions | `AzActions`, `AzTabs`, `MainContent`; prototype `ScrollView` | fulfilled — title layout and screenshot matrix test normal/200%; intrinsic action measurement stacks controls. |
| No hyphenation, complete tab words, no all-caps labels | `AzType.style`, `AzTabs`; prototype XML | fulfilled — tabs use one line without wrapping/ellipsis; actions retain original casing. |
| General “no italic” versus new italic AI exception | `android.md` Typography / App header; `MainContent.kt` | open — the reference contradicts itself. The maintainer explicitly ordered the more specific AI style; applied only there. Clarify the general sentence upstream. |
| brand-display only on About; no text on splash | `app` and `prototype-app` splash themes | fulfilled — no About screen exists; no new screen or brand heading was added. |
| Minimum 48×48 dp, non-overlapping touch targets and 8 dp gaps | `AzButton`, `AzIconButton`, `AzChip`, `AzActions`, `AzVoiceRow`, `AzTabs` | fulfilled — controls have explicit minimum targets and wrapping; status labels are noninteractive. |
| 16/24 dp responsive screen/card inset and card spacing; dialog 24 dp | `AzentriusTheme.kt:LocalSpace`, `AzCard`, `AzDialog`; prototype `az_dimensions.xml` | adjusted — assistant already matches; prototype page inset now follows available width. |
| Page/card/floating hierarchy; cards flat with 1 dp border | `AzCard`, `AzHistoryRow`, `VoiceOptions`, `AzDialog` | fulfilled — matching tokens, radius 16 dp for cards, no card elevation. |
| Floating shadows only; zero tonal elevation | `AzDialog`; `AzentriusTheme.kt` | fulfilled — dialog shadow 12 dp, exact floating surface, no tonal tint. Assistant has no menus. |
| Edge-to-edge, themed bars and safe system insets | `AzentriusTheme.kt`, `ConversationBar.kt`, `SpeechSettingsActivity.kt`; prototype `MainActivity.kt` | adjusted — assistant keeps existing insets; prototype bars follow the page and theme, API-29 contrast setting guarded for minSdk 28. |
| Material 3 mapping, all listed scheme roles | `AzentriusTheme.kt:scheme` | fulfilled — primary, surfaces, containers, text, outlines, error and scrim map to the required tokens. |
| Pressed color, no ripple; selected primary; keyboard-only focus | `AzComponents.kt`, `AzScreens.kt`; prototype primary drawable | fulfilled — immediate state changes, no ripple/transition added. See native selector gap below. |
| Disabled neutral/text-disabled, no button border; transparent icon/text controls | `AzButton`, `AzChip`, `AzIconButton`, `AzTextField` | fulfilled — field border follows the more specific disabled-field rule. |
| At most 150 ms; remove-animations setting | `AzComponents.kt`, `AzScreens.kt` | fulfilled — no transitions or indefinite animation in the custom components. |

## Android: components

| Rule | Code location | Result |
|---|---|---|
| Primary/secondary/danger/text buttons, five states and dimensions | `AzButton`; prototype `PrototypeButtonBackground.kt`, `activity_main.xml` | adjusted — assistant already matches; native Start uses primary palette, full pill, 48 dp minimum, 24 dp horizontal padding and app-control. |
| Icon button 48 dp circle, 24 dp icon, action description | `AzIconButton`; uses in `MainContent`, `ConversationBar`, `SpeechSettingsActivity` | fulfilled — existing localized action descriptions retained. |
| Only one primary per action group; danger only in delete confirmation | `MainContent.kt`, `ConversationBar.kt`, `VoiceOptions` confirmation | fulfilled — action placement and conditions unchanged. |
| Text fields: label above, radius 6, min 48, h16, 4 dp gaps, text/cursor/focus/disabled | `AzTextField`; settings and history search in `MainContent.kt` | fulfilled — normal/focused/disabled presentation uses supplied values. |
| Inline field error after blur/save, red outline and instruction | `AzTextField`, `HomeViewModel`, `MainContent.kt` | open — existing validation/storage errors are page-level messages, not field-owned error state. Do not invent validation timing, attribution or new instructions. Upstream should describe existing behavior. |
| Chips 32 dp visual / 48 dp touch, h16, check18+gap8, wrapping and states | `AzChip`; settings/search/tempo FlowRows | fulfilled — existing immediate selection and disabled rules retained. |
| Tabs: three measured modes, common mode, padding, icons, marker and scroll-to-selection | `AzTabs`, `DesignUiTest` | fulfilled — measured at current font scale; 48/64 dp minimum; 3 dp rounded marker uses label width in modes 2/3; no animation. |
| Menus: floating, radius16, border1, shadow6, max280, rows48 and selected check | Assistant: no menu component; prototype `MainActivity.adapter` / native Spinner | open — the legacy native prototype selectors have no explicit collapsed-selector mapping in android.md. They keep native control rendering and selection behavior. Specify the complete native selector/menu mapping upstream before a full replacement; the phone assistant has no such control. |
| Dialog: floating/radius24/shadow12/backdrop; max560, outer16, inner24 | `AzDialog` | fulfilled — exact tokens and dimensions, scrollable content. |
| Dialog title20/26, content gap16, action gap24; primary first/left, cancel after16, stack | `AzDialog`, `AzActions` | fulfilled — action callbacks and strings retained. |
| Back closes dialog; retain unsaved input on backdrop | `AzDialog` call sites | fulfilled — existing dialogs contain confirmations/read-only content, no unsaved editable fields. No dismissal behavior changed. |
| Status pill: h8/v4, icon16, gap4, label13/18, no touch target | `AzStatusLabel` | fulfilled — polite merged live region and specified tints. |
| List card/radius16/border1; h16, one-line56, two-line72, inset divider | `AzHistoryRow`, `VoiceOptions`, `AzVoiceRow` | adjusted — voice rows with a description now reserve 72 dp instead of 56 dp and grow for large text. |
| Whole voice row selects; radio20/ring2/dot10/gap16 | `AzVoiceRow` | fulfilled — selection semantics and callbacks unchanged. |
| Switch and slider specifications | No such controls in current assistant | fulfilled (not applicable) — tempo already uses chips; no switch or slider added. |

## Android: conversation app and identity

| Rule | Code location | Result |
|---|---|---|
| App header: existing name, one baseline and one accessible text | `MainContent.kt` top bar; prototype `MainActivity.stylePhoneTitle` | adjusted — AnnotatedString / SpannableString from existing app_name, OpenAuto in DM Sans22/600/text, AI in real DM Serif Display italic22/400/signature. No duplicate content description. |
| Copy italic font and XML family exactly | `app/src/main/res/font/dm_serif_display_italic.ttf`, `dm_serif_display.xml` | adjusted — byte-identical upstream files; prototype uses the identical TTF directly through native TypefaceSpan. |
| No renamed label/notification title or new translatable name | Existing values/values-de, manifests and `VoiceService` | fulfilled — strings and labels unchanged; car title remains plain host text. |
| Conversation / Settings / History order, icons and section typography | `MainContent.kt`, `AzTabs` | fulfilled — existing arrangement, section17/22 with 24 dp above/8 below. |
| Key save stays a primary text-only button below its input | `MainContent.kt` settings | fulfilled — no new save action, no toolbar save icon. |
| Bottom bar surface, divider, padding/insets, state before separate export label | `ConversationBar.kt` | fulfilled — existing conditions and separate labels retained; controls wrap without truncating labels. |
| Start/continue/end based on activity/selected chat; preparation-only cancel | `ConversationBar.kt`, `DesignUiTest` | fulfilled — no change to conversation state machine or start gate. |
| Export preparation cancel / system picker cancel / no cancellation during write | `ConversationBar.kt`, `MainActivity`, `DocumentConversationExporter` | fulfilled — no new operation or cancellation path. |
| User bubble80%, right, tint-neutral, h16/v8, radius16/lower-right6; assistant full width | `AzMessage`, message items in `MainContent` | fulfilled — existing role semantics; 8 dp same-role / 24 dp changed-role gap. |
| Preserve accessibility role prefix and existing wording | `MainContent.kt`, localized `user_role` | fulfilled — existing German address retained. Examples in the specification do not authorize translation changes. |
| Interim speech: outline-only then filled final bubble | No interim transcript in current UI | fulfilled (not applicable) — do not add partial speech recognition behavior. |
| History title/body single line; last-modified/provider/model metadata; trailing angle24 | `AzHistoryRow`, `historyDate`, `MainContent.kt` | fulfilled — ellipsis at the end, date first, locale-aware relative/calendar dates, no invented duration. |
| Search message match third line only, label/subtle, min88; no snippets/highlights | `AzHistoryRow`, `MainContent.kt` | fulfilled — title/provider/model-only matches do not get a third line. |
| Day groups if present; empty icon48/title20/body; delete confirmation | `MainContent.kt` history | fulfilled — empty/delete presentation exists; no day-grouping feature added. |
| Separate Voice and speed page entered from Conversation, same location | `MainContent.kt`, `SpeechSettingsActivity.kt` | fulfilled — no Settings entry added. |
| Voice row title/body and description/label; Online only for known online voice | `VoiceOptions`, `voice/SpeechSettings` | fulfilled — default remains unmarked, existing labels/descriptions retained. |
| Confirm every explicit online choice; no stored consent; cancel keeps selection | `VoiceOptions` confirmation, `SpeechPreview` and screen tests | fulfilled — appearance only, no permission or persistence changes. |
| Preview current voice only; two always-visible secondary buttons, play/stop, gap8/stack | `SpeechSettingsContent`, `AzActions` | fulfilled — same enablement, no per-row preview, no selection change. |
| Tempo uses current control | `SpeechSettingsContent` | fulfilled — three existing rate chips retained. |
| State/color/icon table; listening strong info, thinking/speaking tinted info, others neutral | `ConversationBar.kt`, `AzStatusLabel` | fulfilled — no gold/green conversation phase, no spinner/pulse; error reason in danger label. |
| State exactly once, in lower bar | `MainContent.kt:voice-notice`, `ConversationBar.kt:issue` | open — an existing stopped error can appear both as a page notice and in the bar. Removing an existing error presentation conflicts with the instruction to preserve texts/behavior. Keep it and request an upstream clarification. |
| Adaptive icon108, safe circle66, ink/gold, monochrome/evenOdd, roundIcon | Both app manifests and supplied `ic_launcher*` files | fulfilled — existing icon files verified byte-identical to the reference; no asset regeneration. |
| Splash ink, static icon, no text/animation; pre-12 window background | Both modules' `values`, `values-v31`, `values-v33` styles | fulfilled — startup icon/background unchanged; runtime page colors do not replace startup ink. |
| Notification white24 silhouette, no gold setColor, title unchanged | `VoiceService.kt`, `CarVoiceService.kt`, both `ic_stat_openauto.xml` assets | adjusted — assistant already matches; newly merged car service now uses the same byte-identical white silhouette. Notification titles, channels and actions unchanged. |
| No car-specific launcher icon; Play image only for publication | Manifests / upstream app-icon assets | fulfilled — same adaptive icon; no publication artwork or Play claim added. |
| Font licenses shipped; preserve actual third-party attribution | `NOTICE`, both modules' `assets/licenses`, `LicenseDialog` | adjusted — italic font covered by existing OFL; prototype includes both font licenses. |
| Icon licensing stated as MIT in reference | Upstream LICENSE / android.md versus existing `NOTICE` | open — 29 existing icons are Font Awesome CC BY 4.0, only pending is PatternFly MIT. Keep correct application attribution; report the upstream discrepancy without editing the snapshot. |

## Newly merged car phone setup

| Rule | Code location | Result |
|---|---|---|
| Readable page after ink startup, system light/dark palette | `VoiceSetupActivity.kt`, prototype `az_theme.xml` | adjusted — runtime page explicitly resolves the supplied background color, avoiding light-theme text on the ink launch window. No key/permission/audio callback changes. |
| Existing car audio setup and entry button have no dedicated mapping in the reference | `VoiceSetupActivity.kt`, DHU `VoiceEntryPoint.kt` | open — android.md describes the original assistant's pages, not the new test-mode/permission/key setup. Its native button/radio/input presentation and legacy 28 dp inset need a coherent upstream mapping, including primary-action grouping. Do not invent one or move controls. |

## Android Auto: complete source comparison

`OpenAutoScreen.kt`, `prototype-app/src/dhu/`, all existing localized strings, launcher assets,
providers, selection/storage contracts and audio logic are unchanged from the final baseline.
The only car-ui edits are the supplied notification icon and the phone setup background.
The prototype phone title is styled locally; no styled spans, typefaces or phone surface colors
are passed to the car host. Native phone resources do not replace CarAppTheme.

| Rule | Code location | Result |
|---|---|---|
| Scope: debug experiment, no approved category/release or driving claim | DHU manifest, variant configuration, `OpenAutoCarService` | fulfilled — placeholder, HostValidator and disabled dhuRelease unchanged. |
| B1 MessageTemplate, APP_ICON, message, exactly one action, no strip/icon/loading | `car-ui/.../OpenAutoScreen.kt` | fulfilled — source unchanged. |
| B2 primary C8A96E / primaryDark 7D5F22, no secondary pair | `prototype-app/src/dhu/res/values/car_styles.xml`, DHU manifest metadata | fulfilled — exact existing theme and manifest unchanged. |
| B3 action PRIMARY, no ForegroundCarColorSpan/icon/FLAG_PRIMARY | `OpenAutoScreen.kt` action builder | fulfilled — unchanged. |
| B4 existing title/status/provider/model/action texts and order | `OpenAutoScreen.kt`, `car-ui/src/main/res/values*/strings.xml` | open — current main has real Start/End and voice status, whereas the reference still specifies an offline counter. Preserve current texts/order and behavior, including plain title; upstream must be updated. |
| B5 no phone tokens/fonts/layout passed to host | `OpenAutoScreen.kt`, DHU resources | fulfilled — only B2 pair and existing launcher icon. |
| Host owns background, typography, spacing, focus, pressed feedback and final colors | `OpenAutoScreen.kt` | fulfilled — no attempt to impose phone rendering or assumed black background. |
| Primary pair is not a day/night palette; host replacements permitted | `CarAppTheme`, historical 0.1.14 evidence | fulfilled — requested colors are not claimed as actual colors; cause of historical replacement is unknown. |
| Icon only in header; UI-test action icon gap | `OpenAutoScreen.kt`, launcher resources | fulfilled — no new icon; missing UI-test icon needs no decision while B1/B3 forbid it. |
| Ready/started counter, live provider selection, localization | `PrototypeSession`, `OpenAutoScreen`, `CarTemplateTest` | open — counter remains only on the prototype phone page. Car templates now use active voice state; tests preserve current Start/End intent, localization and live selection. Do not restore the obsolete car counter. |
| Template budget / O4 | `OpenAutoScreen.kt` | open — the old six-counter-start scenario no longer models current voice behavior. No flow change here. Any host regression must be compared with the current main, not inferred from the obsolete counter scenario. |
| O1 decided; O2 only after failed measured contrast; O3 only after clipping | Icon assets, `CarAppTheme`, catalog | fulfilled — O1 already implemented; no unmeasured O2 fallback or content-changing O3 workaround. |

## Android Auto acceptance boundaries (A1–A11)

The historical 0.1.14 report is evidence of that host session, not a rerun on 0.1.18.
Historical environment: Pixel 10 Pro / Android 17; Android Auto 17.7.663654-release
(177663654); DHU 2.0-windows, build 2022-03-30-438482292; negotiated Car API 8 from
`car.onCreateScreen api=8`. Historical 800×480 baseline button contrast was 8.25:1;
after B2/B3, 8.84:1 day and 8.92:1 night, with host blue on a dark-gray template.
These observations are not tokens or a promised result on another host.

| Rule | Code location | Result |
|---|---|---|
| A1 complete icon/title | `OpenAutoScreen`, launcher assets; historical DHU evidence | open — actual host display not rerun for this phone-only change; source and assets unchanged. |
| A2 complete Start caption | `OpenAutoScreen`, localized resources | open — template content covered automatically; real host layout still needs its own session. |
| A3 actual accent in day/night, including host replacement | `CarAppTheme`, historical A3 measurements | open — no new host color measurement; B2/B3 unchanged. |
| A4 measured text/background ≥4.5, unchanged message/title | Historical `docs/evidence/0.1.14/`; B2/B3 | open — phone contrast below is not a replacement for new DHU pixel measurements. |
| A5 full longest/shortest provider-model in each display/language | `ModelCatalog`, `OpenAutoScreen` | open — no new host truncation matrix. Do not shorten labels or assume emulator unit tests prove it. |
| A6 touch/rotary counter and log match | `CarTemplateTest`, `PrototypeSession` | open — counter requirement is obsolete for the car; current Start/End intent and live model selection are tested with an audio fake. Actual host input was not rerun. |
| A7 six starts at800×480, baseline distinction | `CarTemplateTest`, `OpenAutoScreen` | open — no new host run; current voice app has no car counter. Existing baseline failures do not block this design change; only regressions do. Upstream needs a scenario matching current behavior. |
| A8 host rotary focus day/night | Host-owned focus | open — no new physical/host focus test; no application focus styling added. |
| A9 German/English/fallback, no mixed language | Resource folders, localized car tests | fulfilled at resource/template-test level; full host language matrix not rerun. |
| A10 live phone selection without reconnect | `ModelSelectionStore`, `CarTemplateTest` lifecycle test | fulfilled at template-test level; real projection session not rerun. |
| A11 no false conversation/category/approval claim | Text and manifest audit | open — no category/approval claim is added, but legitimate voice-state text now exists. The reference's prohibition on indicating any conversation is obsolete; preserve current behavior. |

## Open points to return to the design-system maintainer

Handoff prepared for the maintainer to send to Claude. The comparison is against the audited
`edd4c67` snapshot and application `main` at `c262a3a` (0.1.18); it does not claim a new audit of a
later upstream commit. **All seven points remain open.** The design describes existing behavior;
any requested behavior change must be identified separately for the app owner to approve. Do not
change application code, wording, provider/storage logic, car template or B2/B3 to settle these gaps.

### 1. App-name italics exception

- Reference: `android.md`, Typography and App header.
- Evidence: the general rule bans italic text, but the name suffix is explicitly italic. The owner
  approved and the app implements that specific exception in `MainContent.kt` and native
  `MainActivity.stylePhoneTitle`. Android Auto still receives a plain text title.
- Request: make the exception explicit in the general rule, without changing the approved title,
  fonts, colors, visible name, accessibility text or other typography.

### 2. Page-level validation messages

- Reference: `android.md`, text-field error state and timing.
- Evidence: `app/src/main/java/dev/openautoai/app/MainContent.kt`, `HomeViewModel.kt` and
  `design/AzComponents.kt` keep existing validation/storage messages at page level. They do not
  have field-owned error state or a new validation-on-blur flow.
- Request: describe the existing message placement and its presentation. Do not require new
  validation triggers, field attribution, instructions or message text as a visual correction.

### 3. Native provider/model selectors

- Reference: `android.md`, menus and the prototype phone surface.
- Evidence: `prototype-app/src/main/java/dev/openautoai/prototype/MainActivity.kt` uses native
  Spinners, including their collapsed presentation and dropdown rows. Selecting an item immediately
  updates the existing selection store; these controls are separate from the assistant's chips.
- Request: specify the complete visual mapping for the collapsed selector and popup together:
  typography, supplied tokens, spacing, touch targets, applicable selected/pressed/focused/disabled
  states, and large-font behavior. Preserve native selection, item order and immediate persistence.
  Do not introduce a replacement flow or controls from an unrelated screen.

### 4. Stopped-error presentation

- Reference: `android.md`, conversation states shown exactly once in the bottom bar.
- Evidence: `MainContent.kt` renders `AppState.notice` as the `voice-notice` page item, while
  `ConversationBar.kt` uses the same notice as its label when the conversation is inactive.
- Request: clarify how the rule accommodates these existing presentations. Removing one notice,
  changing its lifecycle or rewriting its text is not assumed to be a purely visual change; if that
  is desired, identify it as a separate owner decision. Preserve both until resolved.

### 5. Icon attribution

- Reference: `android.md` asset/license list and the snapshot's license statements.
- Evidence: application `NOTICE` and drawable SPDX headers distinguish 29 Font Awesome Free 5
  icons (CC BY 4.0) from the PatternFly pending icon (MIT). Obtaining them through the PatternFly
  package does not make the Font Awesome icons MIT.
- Request: reconcile upstream asset provenance and per-asset attribution with the source licenses.
  Keep the existing correct application notices; do not relabel all icons as MIT or replace artwork
  merely to resolve the documentation discrepancy. The app symbol has separate Azentrius terms.

### 6. Actual car conversation and microphone overlay

- Reference: `android-auto.md`, prototype scope, B4/content/states, A6, A7, A11 and O4.
- Evidence: `car-ui/src/main/java/dev/openautoai/car/OpenAutoScreen.kt` and `voice/CarVoiceService.kt`
  now expose a real selected audio-test/conversation mode, short status, provider/model and one
  Start/End action. The old offline counter remains on the prototype phone page only. The DHU user
  confirmed audible Claude answers and Speaking/End while the host's microphone wave was absent.
- Request: replace obsolete car counter/no-conversation statements with the current behavior,
  keeping the existing MessageTemplate, title/order/strings, host ownership and B2/B3. Describe the
  host wave as capture UI: capture ends before separate TTS playback, Speaking/End stay available,
  and the next listening turn begins after playback. Do not require a persistent/custom wave or
  keep recording merely to retain the overlay.
- Request for acceptance: A6 must describe the existing explicit Start/End actions rather than
  counter increments. A7/O4 need a repeat-cycle scenario matching real voice sessions, with the
  baseline recorded first; an existing baseline failure is known, only a new failure is a regression.
  A11 must prohibit false category/approval claims without banning truthful conversation status.
  These are specification requests, not a new A1–A11 pass or permission to change behavior.
- Keep real host colors/background as measured observations, not guaranteed tokens; record Android
  Auto, DHU and negotiated Car API for future host tests. ID.7, call and headset tests are deferred.

### 7. Car test app's phone audio setup

- Reference: missing dedicated mapping in `android.md` for the newer native setup surface.
- Evidence: `car-ui/src/main/java/dev/openautoai/car/voice/VoiceSetupActivity.kt` and
  `prototype-app/src/dhu/java/dev/openautoai/prototype/VoiceEntryPoint.kt`. The page contains help,
  three mode radio rows, permission action, masked key field with save/delete/status, and a phone
  Start/End action. It retains native controls and its existing 28 dp content inset.
- Request: provide a coherent mapping for that page and its entry button, including layout/insets,
  type, field/status/radio/button states, primary/secondary action grouping and large text. Use
  existing tokens wherever applicable and explicitly decide missing mappings upstream.
- Preserve the current order, texts, immediate mode selection, permission request timing, key
  storage and clearing, separate package stores, phone-versus-car microphone route, secure window
  and start/stop callbacks. Do not add consent persistence, provider calls, key copying or a new
  settings entry point as part of styling.

Return a commit and a point-by-point resolution for these seven requests. Importing that future
reference and implementing its purely visual changes are separate work; this handoff does neither.

Unrun DHU host checks and template-budget behavior are validation/behavior limitations, not new
design choices. No values, new user-facing strings or additional consent state were invented.

## Validation and review

See [evidence and measured contrast](evidence/0.1.16/README.md). The requested evidence directory
keeps its original name; every new screenshot and result there belongs to **0.1.18/code 118**.
The Pixel full suite passes 78/78. A supplementary emulator suite reported four failures; the
evidence records the original run and targeted baseline/final comparison, without treating it as green.
Before merging, inspect the title in light/dark and with 200% device font, both supported languages,
the voice list with description rows, and the separately installed prototype phone page.
TalkBack should read the title once. Automated semantics do not replace an audible TalkBack check.

No live provider requests, audio previews, microphone capture, calls or headset tests are required
for this design change. The existing car voice build on the Pixel is not replaced by the newly rebuilt
car prototype; template tests use an isolated emulator.

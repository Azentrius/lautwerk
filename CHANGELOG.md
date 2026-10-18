# Changelog

## 0.3.0 – 2026-10-18

Move the Android package names to com.azentrius.lautwerk following the move to azentrius.com.

- New application IDs: `com.azentrius.lautwerk`, `com.azentrius.lautwerk.prototype.dhu` and
  `com.azentrius.lautwerk.prototype.phone`. All module namespaces, source packages, fully qualified
  components and internal intent identifiers use `com.azentrius.lautwerk.*`.
- Android treats these as new apps. Uninstall the old packages `de.azentrius.lautwerk`,
  `de.azentrius.lautwerk.prototype.dhu` and `de.azentrius.lautwerk.prototype.phone` before installing
  the new APKs. Uninstalling deletes their local data; export any chats you want to keep first.
- API keys and conversation histories do not move to the new apps. Android Keystore access is bound
  to the app identity; there is no migration. Enter keys and select providers, models and voices again
  in each new app. The existing encrypted formats and Keystore alias strings are unchanged.
- Keep the Lautwerk name, design, provider and voice behavior, car template, category declaration,
  HostValidator and external Pocket engine identity unchanged. Previous Android Auto acceptance and
  installer metadata do not transfer to the new package; validate visibility separately.
- All three apps use versionCode 300; the assistant is 0.3.0 and the prototypes are 0.3.0-prototype.
  Historical evidence, design snapshots and earlier changelog entries are preserved.
- The design reference in `design/azentrius/` is now a single snapshot of design-system commit 94009ab
  (155 files, checksums in `UPSTREAM.sha256`). Earlier snapshots and the original package files are
  removed from the working tree and remain in the Git history.

## 0.2.0 – 2026-10-18

Rename the app from OpenAuto AI to Lautwerk. For Android it is a new app: nothing is carried over
from installations of earlier versions.

- New package IDs: the assistant is `de.azentrius.lautwerk` (previously `dev.openautoai.app`), the test
  app `de.azentrius.lautwerk.prototype.dhu` and `.phone` (previously `dev.openautoai.prototype.dhu` and
  `.phone`). Kotlin packages and namespaces are `de.azentrius.lautwerk.*` (previously `dev.openautoai.*`).
- New Android Keystore aliases `lautwerk-key-v1` and `lautwerk-chat-archive-v1` (previously
  `openautoai-key-v1` and `openautoai-chat-archive-v1`). API keys have to be entered again. Chats of the
  old packages can only be kept as a text export; there is no import.
- Display name, Android Auto title, notification and the text export (`Lautwerk-Chat.txt`, header
  "Lautwerk – Conversation export"; previously `OpenAutoAI-Chat.txt`) use the new name.
- The phone title shows the word mark "Lautwerk." with the dot in the signature color, in the app and on
  the prototype's phone page; screen readers read "Lautwerk". The car title stays plain text.
- The voice sample and the car audio test name Lautwerk. The pronunciation aids that spelled "AI"
  (Android TTS span, "A I" for Pocket) and their tests are removed.
- Classes `LautwerkApplication`, `LautwerkCarService`, `LautwerkScreen` and `LautwerkSession` (previously
  `OpenAutoApplication`, `OpenAutoCarService`, `OpenAutoScreen` and `OpenAutoSession`), theme
  `Theme.Lautwerk` (previously `Theme.OpenAutoAI`), log tag `Lautwerk` (previously `OpenAutoAI`),
  notification icon `ic_stat_lautwerk` (previously `ic_stat_openauto`) and Gradle project name `Lautwerk`.
- Active design reference is snapshot `upstream-f2c9df4`, the design-system commit with the rename. The
  app icon files come from it; only their header comment changed. Its other changes since `edd4c67` are
  not yet audited.
- Release archives are called `lautwerk-<version>.tar.gz` (previously `openauto-ai-<version>.tar.gz`). The
  repository moves to `Azentrius/lautwerk`; GitHub redirects the old address.
- Docs use the new names and IDs in their commands. Test results recorded for earlier versions refer
  to the old packages; `docs/evidence/` is unchanged.
- Unchanged: the Pocket test engine `org.pockettts.android.engine.openautotest`, chat and key formats,
  providers, voice flow and the car template. Both apps use version 0.2.0, versionCode 200.

## 0.1.19 – 2026-10-14

Add the tested Pocket/Alba engine as an explicit local voice.

- Offer Pocket · Alba alongside Android voices on the phone, with fixed-text preview and cancellation.
- Reuse the same explicit local output selection in the car package’s phone setup, keeping the car template unchanged.
- Require the evaluated cancellation-fixed companion engine and offline German Alba voice; report missing setup rather than silently substituting another voice.
- Keep Android voice speed and per-language preferences. Explain that this Pocket build uses its normal speed and supports German only.
- Add isolated selection and opt-in audio regression tests, setup instructions and evaluation attribution.

## 0.1.18 – 2026-10-13

Apply the Azentrius app title and refresh the read-only design reference.

- Render the existing phone title as one accessible text: DM Sans OpenAuto and italic DM Serif
  Display AI in the theme's signature color. The native prototype phone title follows the same rule.
- Spell the AI suffix as separate letters in the fixed voice sample, using an Android TTS
  pronunciation span. Keep the visible name, sample wording, voice choice and conversation flow.
- Give voice rows with a description the specified 72 dp minimum height.
- Bring the prototype phone page's typography, surface, text and primary button into the supplied
  light/dark palette. Keep its controls, texts and selection flow.
- Restore historical design snapshots and import the complete, byte-verified `edd4c67` tree.
  Record the full Android and Android Auto audit, open specification points and evidence.
- Use the supplied small notification icon for the newly merged car voice service and keep its phone
  setup page readable in the selected theme. Its remaining native component mapping is an open point.
- Keep the car template, B2/B3, strings, provider selection, storage and conversation behavior.
  Both apps use the existing shared version rule: 0.1.18, versionCode 118.

## 0.1.17 – 2026-10-13

Connect the experimental Android Auto surface to independently testable audio and the existing conversation flow.

- Keep the car package, signed-host validation, title, provider/model line and single action. Start now
  invokes the selected audio test or conversation; End cancels capture, playback and pending work.
- Add phone-only setup inside the car test package: output test, microphone/recognition echo test and
  conversation mode. Output is the initial mode; both audio checks have no AI backend or key dependency.
- Use AudioManager focus and local TextToSpeech output. Car capture uses CarAudioRecord (Car API 5+),
  PCM 16-bit mono at 16 kHz and the Android on-device recognition input (Android 13+).
  Explicit phone testing uses the phone recognizer; the car path never deliberately switches microphones.
- Reuse VoiceController, ConversationVoiceBackend, ProviderChatClient and encrypted credentials/chats.
  No key transfer from the separate assistant package, no keys or conversation contents in logs.
- Add bounded foreground audio sessions, stop/disconnect cleanup, fixed English/German errors and
  opt-in device probes for TTS playback and external PCM recognition. Call/headset tests remain deferred.
- Fix repeated listening after empty recognition segments: keep external PCM open across speech
  boundaries until a nonempty final segment, actual session end, cancellation or timeout. Host microphone
  dismissal ends the session without a silence retry. Start and End retain their explicit intent even
  when Android Auto delivers a click from an older template. Host microphone start/stop run on a serial
  background executor so End does not wait for host IPC; no-input has its own neutral status.

Device evidence and remaining checks: [car voice report](docs/CAR_VOICE.md).

## 0.1.16 – 2026-10-13

The build moves to the current Android toolchain, and the repository gets one version, a release
workflow and complete license information. The apps behave as in 0.1.15.

- One version for the whole repository in `gradle.properties`. Both apps take their `versionName` from it
  (`0.1.16`, the prototype `0.1.16-prototype`) and derive `versionCode` as major × 10000 + minor × 100 +
  patch, here 116 for every variant. Before, the assistant was 0.1.15-dev (code 15) and the prototype
  variants had their own codes; 116 is higher than all of them, so updates install over the old apps.
- Toolchain: Gradle 9.8.1, AGP 9.4.1 with built-in Kotlin, Kotlin 2.4.21, Compose BOM 2026.09.00,
  compile SDK 37 and Build Tools 36.0.0; target SDK stays 36 until the Android 17 behavior changes are
  tested on a device. The voice settings read their strings through `LocalResources`. The build tooling
  gets Apache HttpClient 4.5.14 instead of a vulnerable 4.5 version; it is not in any APK.
- CI job `test` checks the version against `CHANGELOG.md` and runs Android lint for every module (`app`,
  both `prototype-app` variants, `car-ui`, `settings`, `storage`) next to the JVM tests and debug builds.
- New `release` workflow, run on request from `main`: it checks the version against `gradle.properties` and
  the existing tags, runs the same tests, lint and builds and publishes the source archive
  `openauto-ai-VERSION.tar.gz` with `SHA256SUMS` and a signed build provenance. No APKs.
- License information: an SPDX header in every source file and a `NOTICE` for the fonts, the icons and the
  app icon. 29 of the bundled icons are Font Awesome Free 5 (CC BY 4.0), not MIT as their headers said; the
  app's license list now includes the Font Awesome attribution. The app icon from the Azentrius design
  system is not covered by the Apache License.
- The copy of the Azentrius design system under `design/azentrius/` is removed; the docs link to the
  design-system repository at the referenced commits instead.
- Device tests with the new toolchain (Compose 1.9 → 1.12) are still open.

## 0.1.15 – 2026-10-12

The assistant and the Android Auto prototype get the approved app icon and a native splash screen.

- Both apps use the approved speech bubble with a dot from the Azentrius design system as adaptive and
  round launcher icons with a monochrome layer, gold `#C8A96E` on ink `#0A0A0F`. It replaces the
  prototype's turquoise-blue icon; the assistant had no launcher icon before.
- Native splash screen on ink: from API 31 the theme sets only the splash background color, from API 33
  `windowSplashScreenBehavior` also shows the icon for starts outside the launcher. No splash library,
  custom animation or start delay.
- The conversation service uses the supplied white small icon `ic_stat_openauto` instead of an Android
  system icon; its actions, texts, channel and permissions are unchanged.
- The 11 adopted XML files are byte-identical to the design-system source; no design values were invented.
  Car template, B2/B3 colors, counter, `HostValidator`, providers, storage and voice logic are unchanged.
- Assistant 0.1.15-dev (code 15) and DHU test app 0.1.15-prototype (code 6) were installed as updates on
  the Pixel; the phone comparison variant was only built, as 0.1.15-prototype (code 5).
- 17 selected assistant device tests and all 11 DHU device tests pass; lint shows 0 errors. Splash in
  light and dark, the existing phone pages, the new DHU launcher icon, six touch starts without reset and
  rotary starts in fresh day and night sessions were checked (Android Auto 17.7.663654, host Car API 8).
- Open host observation: after using the phone selection, rotary focus can become unusable; the unchanged
  0.1.14 also missed rotary clicks, so the cause is not isolated.
- `docs/SIDELOADING.md` and `docs/SIDELOAD_TEST_REPORT.md` describe how to check the APK installed
  outside Google Play in a car, starting with the ID.7 on native wireless Android Auto. An earlier
  observation that day, before 0.1.15 and without recorded versions, found the app not visible in the
  ID.7; a working sideload path is not confirmed.

## 0.1.14 – 2026-10-12

The Android Auto prototype requests the approved Azentrius colors for its Start action.

- DHU flavor only: `CarAppTheme`, referenced through the `androidx.car.app.theme` metadata, sets primary
  gold `#C8A96E` and primary dark bronze `#7D5F22`; the Start action in `OpenAutoScreen` uses
  `CarColor.PRIMARY`.
- Implements rules B1–B5 of the approved Android Auto design: the existing `MessageTemplate`, `APP_ICON`,
  full texts, order and exactly one action stay. No secondary color pair, `FLAG_PRIMARY`, custom text
  color, new fonts or icons.
- Category declaration, `HostValidator` and the disabled `dhuRelease` are unchanged; no official AI
  category is claimed.
- New DHU profiles in `tools/dhu/` (800×480, 1280×720, 1920×1080 and a wide 1280×720, each for touch and
  rotary) and `tools/dhu/contrast.py` to measure contrast in DHU screenshots.
- Only the DHU variant moves to 0.1.14-prototype (code 5); the assistant stays at 0.1.13-dev and the phone
  comparison variant at 0.1.12-prototype.
- The tested host (Android Auto 17.7.663654, DHU 2.0-windows, host Car API 8) replaces the requested
  color with light blue; the measured Start contrast is 8.47–8.98:1 across 64 views, 8.25:1 before.
- All 11 prototype device tests pass, including three new `CarDesignContractTest` checks; DHU lint shows
  0 errors. 64 combinations (four formats × German/English × day/night × OpenAI/Claude × touch/rotary)
  were checked, with a reachable, visible rotary focus in all 32 rotary combinations.
- Six starts in one session without reset passed for both 0.1.12 and 0.1.14; live model switching and
  the English fallback for a French device language were checked too. Results apply to this host only.

## 0.1.13 – 2026-10-12

The phone app adopts the approved Azentrius design in light and dark.

- New `design` package (`AzentriusTheme`, `AzPalette`, `AzComponents`, `AzScreens`) with fixed color
  tokens, DM Sans and the supplied PatternFly icons, taken unchanged from the approved package; no
  dynamic colors and no ripple effects.
- The three tabs measure their longest label at the current font size and switch between side by side,
  stacked and horizontally scrollable, so labels stay complete with large fonts.
- User messages appear as bubbles, answers in full width; history entries show the last change time,
  provider and model. "Voice and speed" becomes a separate page with radio buttons and preview buttons.
- Every explicit online-voice choice shows the existing confirmation, without a stored consent state. The
  bottom bar (`ConversationBar`) shows the conversation phase and the export separately.
- The font and icon licenses (OFL, MIT) ship with the app and can be read offline under Settings →
  Licenses.
- No change to provider requests, model catalog, audio control, encrypted storage formats or the car
  template. Assistant 0.1.13-dev (code 14); the DHU variant stays at 0.1.12-prototype.
- 169 distinct automated tests pass: 88 unchanged JVM tests, 73 assistant device tests on a Pixel 10 Pro
  (Android 17) and 8 DHU/selection tests; lint shows 0 errors.
- Light German, dark English, dialogs, the voice list and 200 % font size were checked visually; look and
  handling were confirmed manually on the device. TalkBack/D-pad acceptance and call and headset tests are
  still open.

## 0.1.12 – 2026-10-12

The app follows the device language in German or English, and starting a conversation is more robust.

- Android string resources for German and English plus fixed bilingual status and error texts for the
  JVM modules (`core` `i18n`); the first supported device language wins, otherwise English.
  "Gespräch starten" replaces "Sprachgespräch starten"; the English label is "Start conversation".
- Recognition, text-to-speech, the preview, export labels and the instructions sent to OpenAI and Claude
  follow the language, and English voice commands such as "Stop" and "Switch to Claude" work. Existing
  chat content, model IDs and answer budgets are unchanged.
- The voice choice is stored per language; an earlier choice is carried over using Android's voice
  metadata, and no arbitrary fallback voice is picked when a voice or service is missing.
- `ConversationStartGate` hands a start request over only while the activity is resumed, only once and
  never through the saved bundle; late permission grants after a cancel cannot start a conversation.
  Changing the language ends an active conversation.
- Navigation, search text, filter and the position of an older message survive recreating the UI.
- The DHU test app gets the same two languages as 0.1.12-prototype (code 4); layout and actions stay.
  Assistant 0.1.12-dev (code 13). The CI `test` job now also runs `:voice:test`.
- 157 distinct automated tests pass: 22 core, 21 provider and 45 conversation JVM tests, 61 assistant
  device tests and 8 DHU/selection tests; recreation and backgrounding were checked in a non-exported
  debug test host with synthetic data. Lint shows 0 errors.
- Manually confirmed: start, answer and stop with both providers and switching the device between English
  and German; a full English conversation was not confirmed separately.

## 0.1.11 – 2026-10-12

The chat history can be searched by text and filtered by provider.

- Under History, a literal, case-insensitive search of up to 200 characters covers title, provider, model
  and all messages; the filters All, OpenAI and Claude apply together with the text. `ConversationSearch`
  in `core` does the matching.
- A hit inside a message opens the chat at the first matching message, even outside the last 50 messages
  shown at first. A counter shows the matching chats; reset and a safe retry after errors are available.
- Search runs on the existing background worker with a 200 ms debounce; outdated jobs are cancelled and
  late results dropped after new input or an archive update.
- Search text and filter live only in the ViewModel: no plaintext index and no stored search history.
  Search stays available during a voice conversation; opening another chat stays locked.
- No new permission, API request or storage format change. Assistant 0.1.11-dev (code 12).
- 78 automated tests pass: 18 core JVM, 53 assistant device tests and 7 DHU/selection tests, including
  1,000 synthetic chats and direct jumps to message 4 of 140 and 96 of 100; lint shows 0 errors.
- Manually confirmed on the Pixel: search, filters and opening hits. There is no fuzzy search or accent
  folding, and the archive is still loaded as a whole.

## 0.1.10 – 2026-10-12

Single chats can be exported as UTF-8 text files.

- "Export as text file" below the chat title writes one chat to a destination picked in Android's file
  dialog, after a notice that the copy is unencrypted. The file holds title, provider, model, last change
  (UTC) and all messages, including older ones the UI hides at first.
- `ACTION_CREATE_DOCUMENT` with `text/plain` and `CATEGORY_OPENABLE`; only the returned `content:` URI is
  written. No new permission, no automatic upload and no persisted URI grant.
- `ConversationTextExport` in `core` writes in sections, also for very long messages, and success is
  reported only after flush and close. Stored API keys, the voice service and the providers are not
  involved, and the encrypted archive is not changed.
- Preparation and writing run in the background, one dialog job at a time. Export is locked during a
  voice conversation, and other changes are locked during an export.
- Assistant 0.1.10-dev (code 11). 59 automated tests pass: 11 core JVM, 41 assistant device tests and
  7 DHU/selection tests; lint shows 0 errors. The first device run showed `CATEGORY_OPENABLE` missing;
  the app now sets it explicitly.
- Manually confirmed: exporting a chat to Downloads and opening the file. Cloud file providers and rotation
  during the picker are untested; after errors an empty or partial file may remain. The text export is
  not a backup or import format.

## 0.1.9 – 2026-10-12

The phone UI is split into Conversation, Settings and History and does storage work in the background.

- `MainContent` separates the three areas; status and start/stop stay in a fixed bottom bar while the
  content scrolls.
- `HomeViewModel` runs encrypted storage jobs on a serial background worker, batches updates and drops
  stale read and start results; no double start and no late start after cancelling or leaving the screen.
- Chats render in lazy lists with the last 50 messages first and 50 more on request; stored histories are
  never truncated.
- Deleting a chat asks for a confirmation bound to the original chat ID; missing or reassigned chats are
  never silently replaced by a new one.
- API keys stay out of the ViewModel and the saved activity state. The input is cleared when the save job
  is accepted, and success is reported only after the write completed.
- Assistant 0.1.9-dev (code 10). 34 device tests pass: 27 assistant tests, covering slow storage,
  cancellation, stale results, 200 synthetic chats and 120 messages, and 7 DHU/selection tests; lint shows
  0 errors. One test fails on any storage call on the main thread.
- The archive is still read and rewritten as a whole; a manual voice test of the new UI and a rotation
  test during a write are pending.

## 0.1.8 – 2026-10-12

Stopping a voice conversation cancels the running provider request, and interruptions end the session.

- Every background job gets its own cancellation source (`RequestCancellation` in `core`) down to the
  single OkHttp call; stop calls `Call.cancel()`, interrupts the worker and drops late results.
- OkHttp 5.3.2 (`OkHttpTransport`) replaces `HttpURLConnection`. Redirects, connection retries and
  resending the request body are disabled, and `503 / Retry-After: 0` causes no second request. The
  fixed endpoints, 1 MiB limit and safe error texts stay; timeouts are 15 s connect, 50 s read/write and
  60 s per call.
- Screen off or a locked device, a call or communication mode reported by Android, or connecting or
  disconnecting an external audio output ends every conversation phase without automatic resume; a
  locked device or a reported call also blocks the start.
- No phone-state or Bluetooth permission and no change to audio routing; a 500 ms state check covers
  Android 10/11 and the device lock.
- Assistant 0.1.8-dev (code 9), installed as an update without data loss; the DHU UI stays at
  0.1.2-prototype.
- 90 automated checks pass: 69 JVM tests (5 core, 20 provider, 44 voice) and 21 device tests
  (14 assistant, 7 DHU/selection). Local socket tests cancel while waiting for headers and during a
  partial response.
- Manually confirmed: stop and restart by button and stop on screen-off for both providers. Call and
  headset/Bluetooth tests are deferred; older Android versions were not tested.

## 0.1.7 – 2026-10-12

A new page lets the user choose the voice and speech rate for both providers.

- "Voice and speed" (`SpeechSettingsActivity`) offers the voices of the installed Android text-to-speech
  service, three speech rates and a stoppable preview with a fixed sentence, without microphone, chat
  data or AI request.
- The choice applies to both providers and is stored in separate, non-secret app preferences
  (`SpeechSettingsStore`); system TTS settings, API keys and chats are not changed.
- Online voices need an explicit confirmation before they are saved, and a missing voice is reported
  before the conversation starts. No additional cloud speech API.
- The preview stops when the app goes to the background; audio focus is still only held while reading out.
- Assistant 0.1.7-dev (code 8). 75 automated checks pass: 57 JVM tests and 18 device tests (11 assistant,
  7 DHU/selection), including setting a real local voice on the Pixel's TTS engine; lint shows 0 errors.
- Google's speech service on the Pixel reports five local and four online voices for de-DE; four of them
  are local/online pairs with the same base ID. A listening check confirmed that the voice choice sounds
  clearly better.

## 0.1.6 – 2026-10-12

Fixes voice conversations that ended right after starting because of an audio focus collision.

- On the Pixel, Google's speech recognition took exclusive focus after the app had requested playback
  focus; the app then received `AUDIOFOCUS_LOSS_TRANSIENT` and ended the recognition.
- The app now requests its own focus only right before reading out and releases it before the next
  recognition round; preparing, listening and waiting for the API need no playback focus. Focus denial
  and real focus loss during output still end the session.
- New `PlaybackFocusAudio` layer in `voice`; `prototype-app`, `car-ui`, `core`, `providers`, `storage` and
  `settings` are unchanged.
- Assistant 0.1.6-dev (code 7), installed as an update; the DHU UI stays at 0.1.2-prototype.
- 71 automated checks pass: 57 JVM tests (38 voice including 11 new focus tests, 17 provider, 2 core) and
  14 device tests (7 assistant, 7 DHU/selection); lint shows 0 errors. Storage tests were not rerun.
- Manually confirmed: recognition, read-out and stopping for OpenAI and Claude; the voice sounded poor.

## 0.1.5 – 2026-10-12

The phone app gets one tested voice conversation flow for OpenAI and Claude.

- New `voice` module: a platform-independent conversation controller (`VoiceController`), worker and
  audio interfaces, the archive and provider binding and TTS text splitting; only the assistant uses it.
- `app/voice` adapts Android's `SpeechRecognizer`, text-to-speech, audio focus, main-thread timers and
  cancellable background tasks; storage and provider work of the voice flow runs off the main thread.
- One flow for both providers: listen, send the recognized text to the selected provider, then archive
  and read out the answer. Exact voice commands still end the session or switch providers in a new chat.
- Duplicate and late callbacks from older steps or sessions are ignored; two silent rounds and step time
  limits end a session, which is limited and never restarts automatically.
- Assistant 0.1.5-dev (code 6), installed with a successful cold start; the DHU UI stays at
  0.1.2-prototype without network or microphone permission.
- 72 automated tests pass: 44 JVM tests (25 new voice tests, 17 provider, 2 core) and 28 device tests,
  including 2 new `AndroidVoiceTasksTest` checks of the thread boundary. The tests use fake provider
  clients and start neither the microphone nor TTS.
- The first manual voice test on the Pixel failed repeatedly with an audio focus error; fixed in 0.1.6.

## 0.1.4 – 2026-10-12

Local chat and key storage moves into its own module and handles concurrent access and storage failures.

- New `storage` module (`EncryptedConversationRepository`, `EncryptedCredentialStore`) behind the
  `ConversationRepository` and `CredentialStore` interfaces in `core`. The phone UI, the API bridge and
  the voice service use them; `ConversationStore` and `SecretStore` remain as type aliases in `app`.
- Archive file, JSON fields, AES/GCM payload, Keystore aliases and provider IDs stay compatible; there is
  no data migration.
- Process-wide locks protect related reads and writes across storage instances. Failed writes are
  reported, credentials are saved with a checked `commit()`, and a leftover `.bak` copy is restored
  through `AtomicFile`.
- An unreadable archive is neither treated as empty nor overwritten; the UI offers to reload it and blocks
  starting until it can be read. Reading never creates a new Keystore key, and an unreadable API key is
  no longer treated like a missing one.
- Chats with model IDs no longer in the catalog stay readable but are not continued with another model;
  a user message that could not be archived is not sent to the provider.
- Assistant 0.1.4-dev (code 5); the DHU test app stays at 0.1.2-prototype without new permissions. The
  assistant's device tests move to Espresso 3.7.0.
- 45 tests pass: 19 JVM, 14 isolated storage tests on Android (old format via an independent codec, real
  Keystore encryption, concurrent access, corrupt data, missing keys, write failures), 5 assistant/UI
  tests and 7 selection/car template tests; lint shows 0 errors. Only synthetic test data was used.
- Storage access is still synchronous on the main thread, and the locks apply within one process only.

## 0.1.3 – 2026-10-12

The assistant app talks to OpenAI and Claude through separate provider adapters behind one chat interface.

- New `providers` module with `OpenAiAdapter` (Responses API) and `ClaudeAdapter` (Messages API) behind
  the shared `ChatClient` interface in `core`; only the assistant app uses it.
- API errors, empty replies, refusals and replies cut off at the output limit are handled explicitly and
  shown as fixed messages, also in the conversation view; raw server messages are never shown or read aloud.
- The HTTP transport accepts only the two fixed HTTPS endpoints, follows no redirects, uses 15 s connect
  and 50 s read timeouts and limits responses to 1 MiB.
- Assistant app 0.1.3-dev; the phone and DHU test app stays at 0.1.2-prototype.
- 19 JVM tests and 9 device tests pass; API responses are simulated, no live API or voice end-to-end test yet.

## 0.1.2 – 2026-10-11

Phone and car template of the DHU test app share one provider and model selection.

- `core` gains `Provider`, `ModelOption`, `ModelCatalog` and `ModelSelection`; a selection only accepts
  a model that belongs to its provider.
- New `settings` module: `ModelSelectionStore` keeps the selection in app-private storage, one model per
  provider; invalid stored values fall back to a matching default.
- DHU test app: provider and model picker on the phone; the car template shows the current selection
  below the test status and follows changes while visible.
- The assistant app uses the same types and store; switches are disabled during a voice conversation,
  and the voice command to switch provider uses that provider's last model and still starts a new chat.
- The model catalog keeps the five IDs from 0.1.0.
- Selection and Start confirmed manually in a real DHU.

## 0.1.1 – 2026-10-11

The 0.1.0 source becomes a buildable project with a minimal Android Auto prototype for the DHU.

- Complete Gradle wrapper (jar, shell and `.bat` scripts) with a pinned distribution checksum; Gradle 8.13,
  AGP 8.13.2, Kotlin 2.2.20, JDK 17 and compile/target SDK 36 instead of 37: the build machine had API 36,
  and the code needs no API 37 feature. Compose BOM 2026.09.00 needs SDK 37 and AGP 9.1, so 2025.08.01
  replaces it; Activity Compose stays 1.12.0.
- Assistant app kept as `app` (`dev.openautoai.app`, 0.1.1-dev, code 2, same application ID). Compared by
  hash with the 0.1.0 archive, `AppState`, `ChatApi`, `ConversationStore`, `Provider`, `SecretStore` and
  `VoiceService` are byte-identical. `MainActivity` gets two compile fixes: a broken quotation mark in the
  text with the voice command "Stopp", and `permissions: Array<out String>` becomes `Array<String>` to
  match AndroidX `ComponentActivity`. Keystore aliases, archive format, API endpoints, provider logic and
  voice flow are unchanged; this keeps the code, it does not validate the provider APIs or review the
  old code's security.
- New modules `core`, `car-ui` and `prototype-app` with the flavors `phone` and `dhu`; the car service
  keeps host validation on and shows a `MessageTemplate` with a Start action (Car App Library 1.8.0-rc01).
- The `dhu` flavor declares the POI category only as a debug-only test placeholder; `dhuRelease` is
  disabled.
- Device tests for the phone Start button and the car template's click callback.
- Launcher visibility, title and Start confirmed manually in a real DHU.
- The original 0.1.0 README and roadmap were kept as history; the 0.1.0 section below sums them up.

## 0.1.0

First source prototype for voice conversations with OpenAI and Claude on Android, not yet built.

- Received as `OpenAutoAI_0.1.0_Quellcode.zip` on 2026-10-11, SHA-256
  `01ac3818fe2d174461c8a5303e4b1a5afe15e6f9a7c97bf61737b1deca0010b8`. Source only: not built or tested on
  a device, no APK, no Android Auto display app and no `CarAudioRecord`.
- Jetpack Compose phone app; API keys for OpenAI and Claude encrypted with AES-GCM under an Android
  Keystore key; app auto-backup disabled.
- Selectable provider with a small fixed model list; OpenAI Responses API and Anthropic Messages API with
  a limited history per request.
- Speech input through Android's `SpeechRecognizer`, local text-to-speech, foreground service.
- Locally encrypted chat archive with reopening, continuing and deleting chats.
- Voice commands to switch provider; a switch deliberately starts a new chat.
- Known limits: own API keys only; no realtime streaming, no interrupting the read-out (barge-in), no wake
  word; the model list is static; the local history does not appear in the providers' own apps.
- The roadmap went from a phone MVP to experimental car integration (USB and AAWireless in a Kia, a Car
  App Library prototype if a valid category can be tested) to an open-source repository with CI and
  signed, traceable releases. A free general AI chat app is not an approved Android Auto category, and a
  sideloaded APK does not guarantee projection to the car display.

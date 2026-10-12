# Changelog

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
- Selection and Start confirmed by the user in a real DHU.

## 0.1.1 – 2026-10-11

The 0.1.0 source becomes a buildable project with a minimal Android Auto prototype for the DHU.

- Complete Gradle wrapper with a pinned distribution checksum; Gradle 8.13, AGP 8.13.2, Kotlin 2.2.20,
  JDK 17 and compile/target SDK 36 instead of 37, with Compose BOM 2025.08.01 instead of 2026.09.00.
- Assistant app kept as `app` (`dev.openautoai.app`, 0.1.1-dev) with two compile fixes in `MainActivity`;
  the other 0.1.0 Kotlin files are unchanged.
- New modules `core`, `car-ui` and `prototype-app` with the flavors `phone` and `dhu`; the car service
  keeps host validation on and shows a `MessageTemplate` with a Start action (Car App Library 1.8.0-rc01).
- The `dhu` flavor declares the POI category only as a debug-only test placeholder; `dhuRelease` is
  disabled.
- Device tests for the phone Start button and the car template's click callback.
- Launcher visibility, title and Start confirmed by the user in a real DHU.
- The original 0.1.0 README and roadmap are kept in `docs/history/`.

## 0.1.0

First source prototype for voice conversations with OpenAI and Claude on Android, not yet built.

- Jetpack Compose phone app; API keys for OpenAI and Claude encrypted with AES-GCM under an Android
  Keystore key; app auto-backup disabled.
- Selectable provider with a small fixed model list; OpenAI Responses API and Anthropic Messages API with
  a limited history per request.
- Speech input through Android's `SpeechRecognizer`, local text-to-speech, foreground service.
- Locally encrypted chat archive with reopening, continuing and deleting chats.
- Voice commands to switch provider; a switch deliberately starts a new chat.

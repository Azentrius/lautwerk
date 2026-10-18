# Pocket / Alba local speech output

OpenAuto AI 0.1.19 can explicitly use the tested Pocket/Alba companion engine.
The selected AI provider is unchanged: both OpenAI and Claude can supply the text
that is read locally. This makes **speech output** offline, not the whole conversation.
Existing speech-recognition and provider network behavior remains unchanged.

## Setup

1. Install the separate cancellation-fixed engine described in
   [the pinned patch/build instructions](../third_party/pocket-android/README.md).
   The unmodified upstream 0.5.3 APK has the reproduced cancellation problem and
   is not accepted as this integration's Pocket engine. Root is not needed.
2. Obtain the German FP32 pack from
   [PocketTTS-Android-Engine v0.5.3](https://github.com/The-unknown-Shadowman/PocketTTS-Android-Engine/releases/download/v0.5.3/PocketTTS-german-FP32.zip)
   and the unchanged [Alba reference](https://huggingface.co/kyutai/tts-voices/resolve/323332d33f997de8394f24a193e1a76df720e01a/alba-mackenna/casual.wav).
   Review the model's CC BY 4.0 attribution and upstream use conditions. Alba's
   voice performance is by Alba MacKenna, CC BY 4.0. Other catalog voices can differ.
3. Create a pack containing the exact stable Alba ID, using Python 3.11+:

   ```sh
   python tools/pocket/build_alba_pack.py PocketTTS-german-FP32.zip casual.wav PocketTTS-german-Alba.zip
   ```

   The script performs no download and checks both input hashes. Import the result
   through the corrected Pocket engine's existing model-pack importer. It keeps
   the German model bytes and reference audio unchanged and includes attribution.
   The pack remains `german-fp32`; importing replaces that pack in the test engine.
   Do not import it into an unrelated engine or overwrite personal custom packs.
4. In the assistant, open **Conversation → Voice and speed**, choose **Pocket · Alba**,
   then use the existing **Play sample** and **Stop sample** buttons. Merely opening
   the page or choosing a voice does not play audio or make a provider request.
5. For the separate car app, open its phone-side **Set up audio and conversation**
   page and select **Pocket · Alba** under the voice choices. This setting is
   intentionally separate from the assistant. First use its existing output-test
   mode; return to your previous mode explicitly after testing.

Keep Android's global TTS default as it is. Neither app changes it. Missing engine,
model/voice, unsupported language or incompatible build produces a fixed error.
The app never silently switches a Pocket request to Google or an online voice.

Input hashes:

- German FP32 ZIP: `e45e5d0b70dadc9ab9f86f30ae519d79dbfd8adf013d42826af9205545cfdcb3`
- Alba WAV: `46264e83cb99115c3d210260e029117566d9c64f20266d10daa78107759ede3e`

The pack is approximately 207 MB compressed / 440 MB installed. The OpenAuto AI
APKs do not contain these assets or the native engine. No automatic download,
model replacement or transfer of keys/chat archives is performed by the app.

## Scope and behavior

- German only in this evaluated configuration. English keeps its separate Android
  voice choice. The UI explains why Alba is unavailable for English.
- Pocket currently ignores Android's speaking-rate argument. Its normal speed is
  shown and tempo controls are disabled while selected; the prior Android rate is
  retained and restored when choosing an Android voice.
- The fixed preview names Lautwerk and is spoken as written by every engine;
  it needs no pronunciation span. Provider responses and stored messages are
  never rewritten.
- Output uses the existing focus, timeout, chunking, stop and lifecycle logic.
  The corrected engine prevents cancelled native work blocking the next utterance.
- The car MessageTemplate, host validation, microphone path and B2/B3 are unchanged.
  A phone/service test is not proof of DHU or vehicle audio routing.
- No call or headset tests are part of this change. They remain deferred.
- Android Auto visibility still depends on the host and installation conditions.
  Do not replace a working installation or its installer identity as a test shortcut.

## Verification

The separate engine's seven offline test suites passed on Pixel 10 Pro / Android 17:
54 utterance records, 34 cancelled/replaced requests, no PCM after cancellation.
Immediate restart first PCM: 142–496 ms (baseline 13.4–13.9 s). A process-cold early
stop produced no PCM; replacement first PCM arrived after 828 ms (baseline 47.3 s).
A listening check confirmed clean audible stopping and no old sentence after early stop.
These numbers measure callbacks/PCM, not acoustic latency.

Integration test results belong in [the 0.1.19 report](evidence/0.1.19/pocket-integration.md).
Normal device tests do not speak. Explicit `pocketAudio=true` enables fixed German
output checks without recognition or provider calls. Phone tests use isolated voice
preferences; service tests restore only the non-secret speech-output preference map.

Public Android API contracts:
[TextToSpeech](https://developer.android.com/reference/android/speech/tts/TextToSpeech),
[Voice](https://developer.android.com/reference/android/speech/tts/Voice).
The explicit-engine constructor can fall back during binding; the exact offline
Alba ID is therefore validated before submitting any text.

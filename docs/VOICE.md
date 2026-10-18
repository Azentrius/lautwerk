# Phone voice feature – 0.1.12

OpenAI and Claude now use the same tested conversation flow: Android recognizes the speech, the
selected provider processes the recognized text and Android reads the answer out. This is a chained
voice feature built on the existing text models, not a native OpenAI Realtime or Claude audio API.
The principle of separate recognition, text processing and speech output is also described in
[OpenAI Voice agents](https://developers.openai.com/api/docs/guides/voice-agents).

The DHU test app stays limited to title, selection and start. It gets no microphone access, no
network connection and no claimed new car category.

## Choosing and comparing voices

1. Open **"Conversation" → "Voice and speed"**. This button is disabled while a conversation is
   running.
2. Open the voice picker and choose a variant. The Pixel currently reports **five on-device voices
   and four online voices for German (de-DE)**. The numbers are for orientation only; what is saved
   is the actual voice identifier together with the speech service, not the number.
3. **"Play sample"** reads out a fixed sample sentence without microphone, chat data or AI request.
   **"Stop sample"** ends it at any time.
4. If needed, choose "A little slower", "Normal" or "A little faster". Voice and speech rate are
   saved immediately and only for Lautwerk. The system-wide Android settings stay unchanged.
5. Choose **"Back to conversation"** and start a conversation. Both providers use the same
   selection. Leaving the settings page stops the preview and releases its audio focus.

According to the speech service, online voices need a connection; selecting one shows a notice about
the transfer of the text that will be read out. The selection is explicit. "Android default" also
follows the installed service and is no promise of offline use. There is no additional OpenAI or
Claude speech API, and the app does not store audio itself. Android's speech service may have its
own data processing rules.

On the tested Pixel, **2/6, 3/7, 4/8 and 5/9** are each local/online variants with the same base
identifier (`dea`, `deb`, `deg`, `nfh`). They can sound very similar. Another entry is called
`de-DE-language`; "Android default" can also use one of the listed voices. The numbers apply to the
catalog checked here and may change after a speech service update. These variants are different
configurations reported by the service, not nine guaranteed distinct speaker identities.

The app does not promise any particular sound quality. The installed service determines which voices
are offered and how they sound. If a saved voice is missing or the speech service was changed, a
clear error is shown before recording; please choose a new voice or "Android default". There is no
silent switch to a different online voice.

Technical basis: [Android TextToSpeech](https://developer.android.com/reference/android/speech/tts/TextToSpeech)
and [Voice metadata](https://developer.android.com/reference/android/speech/tts/Voice).

## Trying it on the Pixel

1. Open the assistant app `de.azentrius.lautwerk`. It is separate from the DHU test app
   `de.azentrius.lautwerk.prototype.dhu`; both are called Lautwerk.
2. Under **"Settings"**, choose provider and model. Enter and save your own API key only on the
   phone, never in Git or a chat. An existing key stays saved even though the input field is empty.
3. Tap "Start conversation" at the bottom. The button stays visible while scrolling. Allow the
   microphone; on newer versions Android also asks for notifications. A denied microphone permission
   prevents recording. Without notification permission, ending the conversation in the app is still
   possible.
4. Wait for **"Listening"** and ask a short question, such as "Tell me in one sentence what you can
   do." As soon as recognition is finished, the microphone is released and the request is sent to
   the selected provider.
5. The app does not listen while the answer is being given. After reading out, the next short
   recognition pass begins. The device language determines recognition and TTS.
6. **"End conversation"** in the app or **"End"** in the notification also works during a request or
   while reading out.
7. Test the second provider separately, using a new conversation.

Android's installed speech services may process audio online. The app does not store raw
recordings; it stores the recognized text and the model answer in the encrypted local history. The
selected AI provider receives the text context. This can incur API costs. Stopping the local flow is
no promise that a request that has already reached the provider will not be billed.
See also [SpeechRecognizer](https://developer.android.com/reference/android/speech/SpeechRecognizer).

For existing chats: open the chat, then "Continue conversation". The original provider and model
remain assigned. Model IDs that are no longer listed stay readable, but cannot be silently continued
under a different model.

## Voice commands

| Say while "Listening" is shown | Result |
|---|---|
| "Stopp", "Stop", "Beenden" ("End"), "Gespräch beenden" ("End conversation") | End the session, no provider request |
| "Wechsle zu Claude" / "Wechsel zu Claude" ("Switch to Claude") | New Claude conversation with the most recently selected Claude model |
| "Wechsle zu OpenAI" / "Wechsel zu OpenAI" ("Switch to OpenAI") | New OpenAI conversation with the most recently selected OpenAI model |
| "Wechsle zu ChatGPT" / "Wechsel zu ChatGPT" ("Switch to ChatGPT") | Same as OpenAI |

Commands must be the complete utterance. Capitalization, multiple spaces and trailing punctuation do
not matter. A longer question with the same beginning is treated as a question. Switching does not
copy the old history; without a key for the new provider, no new chat is created. A command for the
provider that is already active does not create a new chat either.

Voice commands do not interrupt reading out in progress, because the microphone is paused during it.
To stop locally right away, use the button or the notification.

## Flow and cancellation

| State | Behavior |
|---|---|
| Preparing | Check chat and credentials; prepare foreground service and TTS; do not request playback focus |
| Listening | Exactly one short Android recognition pass; no parallel provider request |
| Provider responding | Archive the user message, call the selected adapter once; microphone off |
| Speaking | Archive the answer, request playback focus and read out; release focus afterwards; split long texts |
| Ended | Cancel recording, TTS, timers and pending local processing; release audio focus |

Each step has its own identifier. Duplicate or late callbacks from older steps or sessions do not
trigger a new recording or output. Each worker job gets its own cancellation source. It is passed
through the chat request and the provider adapter down to the concrete OkHttp call. Ending calls
`Call.cancel()`, interrupts the worker and suppresses its later callback. Cancelling an old job does
not affect a new request. Another check is made before any subsequent archiving. An atomic write
that has already started may still complete; processing and costs that have already reached the
provider cannot be undone locally. System DNS can also keep blocking.
Network timeouts: connect 15 s, read/write 50 s each, entire call 60 s.

There is no automatic retry of paid AI requests and no automatic provider switch. Errors appear as
fixed messages. The session ends on missing TTS, microphone/audio focus problems or API/storage
errors. It also ends after two unsuccessful recognition passes with silence/an empty result; a
hanging pass is additionally subject to a timeout. There is no endless background recognition loop.
A session ends after ten minutes at the latest and can be started again explicitly.

Timeouts: load chat/switch provider 15 s, prepare TTS 12 s, single recognition pass 45 s, request
including saving 70 s, reading out 120 s. When the app is removed from the recent apps overview,
Android also ends the voice service. While reading out, a lost or denied audio focus ends the
conversation. During preparing, listening and waiting for the API, the app does not claim its own
playback focus. The installed recognition service manages its focus itself.

Android requires recognition calls on the main thread and release via `destroy()`. The
implementation creates a separate instance for each recognition pass and releases it afterwards.
[SpeechRecognizer](https://developer.android.com/reference/android/speech/SpeechRecognizer)
TTS is only used after successful initialization and language check; unique utterance IDs protect
against stale done callbacks.
[TextToSpeech](https://developer.android.com/reference/android/speech/tts/TextToSpeech)

## Interruptions in 0.1.8

A running session ends in every phase (preparing, listening, request, reading out) when:

- the display is turned off or the app detects a locked device;
- Android reports a ringing/telephony/communication mode;
- an external audio output device is connected or disconnected, such as a Bluetooth, USB or wired
  headset.

Afterwards a fixed message and "Conversation ended" appear. There is no automatic resumption on
unlocking, hanging up or reconnecting. To continue, unlock the device and explicitly start the
conversation again. An already locked device or a reported call also blocks the start.

Monitoring only runs during a session and is removed when it ends. Android 12+ reports audio mode
changes directly; in addition, the state is checked every 500 ms, also for Android 10/11 and the
device lock. Initial device listings and stale callbacks do not count as a new interruption. The app
requests neither phone state/call data nor Bluetooth control permissions and does not change audio
routing.
Basis: [AudioManager](https://developer.android.com/reference/android/media/AudioManager),
[screen-off signal](https://developer.android.com/reference/android/content/Intent#ACTION_SCREEN_OFF).

Limits: the app monitors connected output devices, not every internal routing change between
devices that are already connected. A telephony/VoIP app that does not report a matching audio mode
is not guaranteed to be detected this way. Audio focus loss while reading out and recognition errors
remain additional stop signals. Manual tests confirmed stopping/restarting via the
button and ending on screen-off for both providers. Physical calls and Bluetooth/headset changes are
deferred and still untested; simulated signals alone do not confirm these
cases.

### Targeted manual acceptance test

Test OpenAI and Claude, each with its own key in the app (API costs possible; do not copy keys into
logs or chat):

1. Ask a short question and end the conversation while "Provider responding" is shown. No late
   answer may be read out. Then start again; the new session must respond normally. Also check
   ending during listening and reading out.
2. Turn the display off during a session, then unlock. The conversation must stay ended and may only
   continue after a new start.
3. **Deferred since 2026-10-12, for a later test:**
   Start a session with a headset and disconnect/connect the headset. It must stay ended. Check an
   ordinary incoming call separately; no test calls and no call answering by the app itself.

## Architecture

- `voice`: platform-independent conversation control, speech/worker interfaces, archive/provider
  integration, focus lifetime while reading out and TTS text splitting.
- `app/voice`: Android SpeechRecognizer, TTS, audio focus, main-thread timers and cancellable
  background tasks.
- `VoiceService`: microphone foreground service and end notification. It is registered as a
  foreground service before asynchronous initialization and is not restarted automatically after
  the process ends.
- `providers`, `storage`, `settings`: existing adapters and storage components. Model catalog, API
  protocols, output limit and archive/key identifiers stay the same.

The service only starts after the visible start on the phone. The microphone permission and
Android's restrictions for foreground services remain in effect. The app does not request any way
around these restrictions.
[Microphone foreground services](https://developer.android.com/develop/background-work/services/fgs/service-types#microphone)

Storage access of the voice flow and, since 0.1.9, of the phone UI as well runs in the background.
A separate serial UI worker keeps file/Keystore work apart from the UI; the two workers of the voice
service remain. Network cancellation happens per call. This does not guarantee that provider-side
processing and billing stop. See [PHONE_UI.md](PHONE_UI.md).

## Building and opening directly

```powershell
Set-Location -LiteralPath 'C:\Users\stefa\Documents\Projekte\Azentrius\Lautwerk'
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat :app:assembleDebug
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
& $adb -s 56161FDCH000H8 install -r '.\app\build\outputs\apk\debug\app-debug.apk'
& $adb -s 56161FDCH000H8 shell am start -n 'de.azentrius.lautwerk/de.azentrius.lautwerk.app.MainActivity'
```

For a supplied APK, change the path to its full path. For other
devices, replace the serial number. If the signature differs, do not uninstall or clear data; use
the same debug key instead. The existing [DHU steps](START_WINDOWS.md) remain valid.

## Fix for the immediate cancellation in 0.1.6

On the Pixel, the assistant app requested playback focus on 2026-10-12 at 00:58:52.043. At
00:58:52.627 the Google speech service that had been started (`com.google.android.tts`) requested
its own exclusive focus. The app received `AUDIOFOCUS_LOSS_TRANSIENT` and ended the recognition
that had just started. This pattern repeated on further attempts. The package name of the Google
service is not proof here that reading out was already in progress; the recording history reported
`VOICE_RECOGNITION`.

The app therefore only requests its own focus immediately before reading out and releases it before
the next recognition pass. If focus is denied, nothing is played. Real focus loss during output
still stops it; there is no automatic restart. Stale focus callbacks do not affect a later pass.
This follows Android's recommendation to request focus directly before playback and to release it
afterwards:
[Audio focus](https://developer.android.com/media/optimize/audio-focus).

## Test status and limits

**157 distinct automated checks pass for 0.1.12:**
22 core JVM, 21 provider JVM, 45 conversation JVM, 61 assistant device tests and 8 DHU/selection
tests. Assistant 0.1.12-dev / code 13 is installed. German/English, start cancellation and
recreation are checked. On 2026-10-12 a manual test confirmed for 0.1.12: "Works flawlessly with both."
German/English are confirmed as well.

Manual tests confirmed recognition, reading out and ending for both providers under 0.1.6, and a
clearly better sound with the voice selection in 0.1.7. On 2026-10-12 a manual test confirmed for 0.1.8
stopping and restarting via the button as well as ending on screen-off for OpenAI and Claude. Call
and headset/Bluetooth tests are deferred and remain untested;
the feedback does not separately confirm every possible timing overlap.
The current phone UI was confirmed under 0.1.12 with real conversations for both providers. Android
Auto audio and real use in a car are not confirmed.
The DHU UI remains functionally intact and gets English texts in 0.1.12-prototype. Details:
[BUILD_AND_TEST.md](BUILD_AND_TEST.md).

Since 0.1.12: [German/English and resumption](LOCALIZATION.md).

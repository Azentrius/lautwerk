# OpenAI/Claude adapters – project status 0.1.12

## Behavior and limits

The existing phone assistant app `com.azentrius.lautwerk` now uses a shared `ChatClient` interface. The
selected provider and the existing model ID determine exactly one API adapter. There is no automatic
provider switch, no automatic retry and no automatic continuation with a higher token budget.

The five model IDs, their defaults and the saved selection from 0.1.2 stay unchanged. The previous
limit of 350 output tokens is kept. For OpenAI, any reasoning that is used also counts towards it;
the limit is therefore no guarantee of a complete visible answer. A partial answer is marked as
shortened before it is read out. Without text, a fixed error message appears. The local notice is
not stored in the archive as a model answer.
[OpenAI Responses reference](https://developers.openai.com/api/reference/resources/responses/methods/create)

API errors are shown as fixed messages (German and English since 0.1.12): missing/invalid key, rejected authentication,
missing permission, model not found, rate limit, credit/usage limit, server error, timeout and
network error. Raw server error messages and exception causes are neither shown nor read out.
The new voice flow ends the session on errors and shows the message on the phone.
HTTP 404 is deliberately worded as "Model or API endpoint not found"; the exact cause cannot be
guaranteed without a real account test.

## Modules

| Module | Responsibility |
|---|---|
| `core` | `ChatClient`, `ChatRequest`, `ChatMessage`, `ChatReply`, `Completion`, `CredentialSource`, `ChatFailure` |
| `providers` | `OpenAiAdapter`, `ClaudeAdapter`, `ProviderChatClient`, `HttpTransport`, `OkHttpTransport` |
| `app/ChatApi` | Compatibility bridge, still present, from the existing `Turn` format and `CredentialStore` to the shared interface |
| `voice` / `app/VoiceService` | Shared flow; saves the answer text, reads out model answers and shows fixed error messages |
| `settings`, `prototype-app`, `car-ui` | No new API dependency; the DHU app remains a selection/UI test |

`providers` is a Kotlin/JVM module. Android provides `org.json` at runtime; the JVM JSON library is
only `compileOnly` and a test dependency. No additional JSON JAR is built into the APK. Two device
tests check the actual Android JSON implementation and the bridge from the old turn format.

## Protocols

- OpenAI: `POST https://api.openai.com/v1/responses`, bearer key,
  `instructions`, `input`, `model`, `max_output_tokens=350`, `store=false`.
  Only assistant text or explicit refusal texts are used as the answer.
  Reasoning content is not read out. The status `incomplete` is evaluated.
  `store=false` disables storing the response for later retrieval; this implies no promise about
  all of the provider's retention rules.
  [Responses reference](https://developers.openai.com/api/reference/resources/responses/methods/create)
- Claude: `POST https://api.anthropic.com/v1/messages`, `x-api-key`,
  `anthropic-version: 2023-06-01`, `system`, `messages`, `model`, `max_tokens=350`.
  Text blocks are evaluated; thinking blocks are not read out.
  `stop_reason` distinguishes complete, truncated and refused answers.
  Tool flows that were not requested are not executed.
  [Messages reference](https://platform.claude.com/docs/en/api/messages/create),
  [stop reasons](https://platform.claude.com/docs/en/build-with-claude/handling-stop-reasons)

The transport uses OkHttp 5.3.2 and still only accepts the two fixed HTTPS endpoints. Redirects and
automatic connection retries are disabled. The request body is also `isOneShot`, so that, for
example, `503` with `Retry-After: 0` does not trigger a second paid request. No HTTP caches.
Connect/read/write/total timeouts: 15/50/50/60 seconds; responses at most 1 MiB. The cancellation
passed through per job calls `Call.cancel()` and unblocks blocked socket operations; streams and the
cancellation registration are released. Cancellation is treated as CANCELLED instead of a
connection error. The tests also use real local sockets for missing headers and partially delivered
responses. Server processing or billing that has already started is not undone by this.
System DNS and other operating system operations are not guaranteed to be interruptible
immediately. See [VOICE.md](VOICE.md) and the
[Call contract](https://github.com/square/okhttp/blob/parent-5.3.2/okhttp/src/commonJvmAndroid/kotlin/okhttp3/Call.kt).

The error classification uses HTTP status codes and known machine-readable codes, not an
interpretation of arbitrary server texts. OpenAI credit/account limits are distinguished from a
temporary 429 rate limit. Claude 402 is treated as a billing problem; 400 can have various causes
and remains a general request error.
[OpenAI error codes](https://developers.openai.com/api/docs/guides/error-codes),
[Claude error codes](https://platform.claude.com/docs/en/api/errors)

## Data and test scope

The client itself does not store any history. It reads only the key for the explicitly selected
provider and sends only the conversation context passed to it. The existing voice provider switch
still starts a new chat. Archive format, Keystore aliases, file names and key identifiers do not
change; there is no data migration. Since 0.1.4 the implementation lives in a separate
[storage module](LOCAL_STORAGE.md).

All adapter tests use made-up keys and simulated responses. No real credentials were read, no
provider endpoints were called and no microphone recording was started. These tests do not prove
any actual account credit, model access or a successful voice end-to-end flow.

## Manual test of the assistant app

1. Install `app:debug`. The package is called `com.azentrius.lautwerk`, while the DHU test is
   called `com.azentrius.lautwerk.prototype.dhu`. Both have the display name Lautwerk; if in
   doubt, open the app directly via ADB (see below).
2. On the phone, choose provider and model under "Settings". Enter your own API key only there.
   Starting a conversation activates the microphone and real API use; the normal API costs may
   arise.
3. Ask a short question. Check the answer, history, stopping and any notices.
4. End the conversation. Choose and test the other provider separately. Existing chats must stay
   with their provider and model.

The current feedback on the real API/voice test is in [VOICE.md](VOICE.md).
The DHU "Start" button still does not trigger an API request after this step either.

```powershell
# In the root of the Lautwerk checkout:
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat :app:assembleDebug
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb -s 56161FDCH000H8 install -r '.\app\build\outputs\apk\debug\app-debug.apk'
& $adb -s 56161FDCH000H8 shell am start -n 'com.azentrius.lautwerk/com.azentrius.lautwerk.app.MainActivity'
```

For a supplied APK, replace the install path with its full path. Other devices: get the serial number with
`adb devices -l`. In case of signature conflicts, do not uninstall automatically, since that would
delete saved data; use the same debug key.

The existing [DHU steps](START_WINDOWS.md) remain valid.

Since 0.1.12: [German/English and resumption](LOCALIZATION.md).

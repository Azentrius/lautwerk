# Local chats and API keys – project status 0.1.12

The existing storage from 0.1.0–0.1.3 is now a separate module. The phone UI, the provider bridge
and the voice service use shared interfaces. There is no data migration and no new cloud storage.
The DHU UI gets English texts in 0.1.12-prototype; its actions remain unchanged.

## Structure and compatibility

| Module | Responsibility |
|---|---|
| `core/storage` | `Conversation`, `Turn`, `ConversationRepository`, `CredentialStore` and fixed storage errors |
| `storage` | Android Keystore, AES/GCM, atomic archive file, encrypted credentials and shared locks |
| `app` | Phone/voice integration; the previous names `ConversationStore` and `SecretStore` remain as type aliases |
| `prototype-app`, `car-ui` | No dependency on `storage` or `providers`; still only a selection/UI test |

The following identifiers and data stay identical:

| Data | Existing format |
|---|---|
| Chat file | `files/conversation_archive.dat` |
| Archive key | Android Keystore alias `openautoai-chat-archive-v1` |
| Credentials | SharedPreferences `api_credentials`, entries `openai` and `anthropic` |
| Credentials key | Android Keystore alias `openautoai-key-v1` |
| Encrypted content | 12-byte IV, followed by AES/GCM ciphertext including 128-bit tag; credentials additionally Base64 |
| JSON archive | `id`, `title`, `provider`, `model`, `updated`, `turns`; messages with `role` and `content` |

`OPENAI` and `CLAUDE` as well as the model ID of each chat are preserved. The compatibility test
creates data with an independent re-implementation of the old codec, reads and extends it with the
new module and reads the result again with the old codec. This tests format compatibility with
synthetic data; no real chats or keys were read for this.

## Improved behavior

- Multiple storage instances use the same lock per file or credential store. Concurrent reading,
  modifying and writing within the app process therefore does not lose a change made by another
  instance.
- A corrupted or unreadable chat file is not interpreted as empty. The UI shows a notice and
  "Reload history"; starting stays blocked until reading succeeds. Mutations do not overwrite an
  unreadable file.
- An old `.bak` state is restored via `AtomicFile.openRead()`, even if the main file is missing.
  Aborted writes are rolled back.
- Reading never creates a new Keystore key. If the key for existing data is missing, an error
  appears. A missing API key and an unreadable saved key are handled differently.
- Credentials are saved with a checked `commit()`. A failure is shown; an entry that was already
  changed is restored where possible after a negative return value. A full or faulty storage medium
  can also prevent this restoration; success is not guaranteed in that case.
- For old model IDs outside today's catalog, the chat stays readable. Continuing is blocked; a
  different model is never used silently.
- If archiving a new user message fails, the message is not added to the running request context
  and not sent to the provider.

The synchronization is necessary because Android's `AtomicFile` itself does not provide locks for
concurrent access.
[AtomicFile documentation](https://developer.android.com/reference/android/util/AtomicFile)
The synchronous result of `commit()` is needed on purpose.
[SharedPreferences.Editor](https://developer.android.com/reference/android/content/SharedPreferences.Editor)
Encryption still uses the Android Keystore.
[KeyGenParameterSpec](https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec)

## Checking on the Pixel

0.1.12-dev is installed as an update of `dev.openautoai.app`. The DHU test app is a different
package with its own storage. In case of a signature conflict, do not uninstall or clear app data;
use the same debug key instead.

```powershell
Set-Location -LiteralPath 'C:\Users\stefa\Documents\Projekte\Azentrius\OpenAuto AI'
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
& $adb -s 56161FDCH000H8 install -r '.\app\build\outputs\apk\debug\app-debug.apk'
& $adb -s 56161FDCH000H8 shell am start -n 'dev.openautoai.app/dev.openautoai.app.MainActivity'
```

For the download APK, replace the install path with the full path to
`OpenAutoAI-0.1.12-assistant-debug.apk`. On another device, use its serial number from
`adb devices -l`.

Existing chats can be opened and read without an API call. For the later real API/voice test, see
[PROVIDER_ADAPTERS.md](PROVIDER_ADAPTERS.md). An API key belongs only in the input field of the
phone app, not in Git, diagnostic output or a chat. Save it under "Settings"; the input field is
cleared as soon as the save request is accepted. Only the success message confirms that the key was
saved; on an error, enter the key again. Key changes are blocked while a session is running.
Provider switches still start a separate chat.

The automated storage tests use their own test app, random test files and their own Keystore
aliases. They delete only their own test data. In 0.1.9, nine UI tests and seven state/worker tests
additionally check slow access, cancellation, long lists and delete confirmation with storage
fakes. No real keys, chat data, voice recording or provider request.

## Limits and next step

The locks apply within one process. Multi-process access is not supported. The interfaces work
synchronously, but since 0.1.9 they are also called in the background from the phone UI. The voice
service keeps its separate workers; the shared storage locks remain in effect. The UI bundles
refresh requests and discards stale read results. Start preparation can be cancelled; an atomic
save that has already started can complete.

Chats appear under "History", messages under "Conversation". The list only draws the visible area;
initially the last 50 messages are offered. "Show older messages" extends the list by 50 each time.
No message is deleted in the process. Deleting requires a confirmation that is bound to the original
chat ID.

Since 0.1.11, the [history search](HISTORY_SEARCH.md) searches the archive state held in memory on
the background worker. Search text/provider filter and search results remain transient; no
additional plaintext index and no new file format. Changed archive states invalidate running
searches and stale matches.

The entire archive is still read in and re-encrypted on changes. The display is limited, not
the file format or the total storage usage. Very large archives can further delay background jobs.
Later indexed storage/paged loading and explicit size rules remain open work.

Losing a Keystore key makes the data encrypted with it unreadable. Since 0.1.10 there is an
explicitly chosen [text export](CHAT_EXPORT.md) of individual chats. It is unencrypted and not a
backup/import format; it does not read any saved credentials and does not change the archive.
Encrypted backup, import and a recovery wizard are still open work. No automatic deletion as error
recovery. Backup/device transfer rules remain an open point of the prototype. The existing
`allowBackup=false` setting does not replace a tested flow for changing devices.

Manual tests confirmed the basic voice dialog with both providers under 0.1.6, as well as
stopping/restarting and screen-off under 0.1.8. The renewed practical test of the UI since 0.1.9 is
still pending. Call and headset/Bluetooth tests remain deferred. The exact status is described in
[VOICE.md](VOICE.md). All results: [BUILD_AND_TEST.md](BUILD_AND_TEST.md).

Since 0.1.12: [German/English and resumption](LOCALIZATION.md).

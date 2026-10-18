# Phone UI – 0.1.12

The main page has three sections. The conversation button and the current status stay reachable
at the bottom edge, even when the list is scrolled.

| Section | Content |
|---|---|
| Conversation | Current provider/model, voice and speed, opened conversation and messages |
| Settings | Provider/model selection and entry and management of the API key |
| History | Search chats, filter by provider, open a chat or select a new conversation |

Opening a chat only displays it. Only "Continue conversation" starts the voice feature with the
original provider and model. Old models that are no longer listed stay readable, but must not be
continued unnoticed under a different model. "Select new conversation" creates a new chat only
when it is started. Start preparation shows "Cancel start"; a running session always shows "End
conversation". During a session, provider/model changes via Settings, key changes and deletion
are locked; the existing explicit voice commands for switching provider stay available.

For long chats, the last 50 messages are offered first. "Show older messages" extends the view
by 50 each time. All saved messages are kept. Deleting a chat requires confirmation; cancelling
leaves it unchanged. The confirmation applies only to the originally selected chat ID.

## Searching history

Under **History**, enter text in the **Search chats** field and, if needed, select **All**,
**OpenAI** or **Claude**. Title, provider, model and all messages are searched; case does not
matter. Entries with **Match in message …** open directly at that point, also outside the first
50 displayed messages. Search text and filter change neither content nor the active provider
selection. **Reset search** shows all chats again. During a voice session, ending stays reachable;
another chat can only be opened after the session has ended.

Search, filter and matches were confirmed manually on 2026-10-12.
Details and limits: [HISTORY_SEARCH.md](HISTORY_SEARCH.md).

## Exporting a chat as a text file

Below the title of the opened chat, select **Export as text file**. After the notice about the
unencrypted copy, **Choose location** leads to Android's file dialog. Regardless of the search,
the file contains all messages, including older hidden ones, and no saved API credentials.
Personal information typed into the chat text remains included. For a local copy, choose a local
folder; cloud destinations transfer the file to the chosen service. Export is locked during a
voice session.

Only the message **Text file saved** confirms completion. On errors, an incomplete file can be
left behind. The export does not change the chat and is not a backup/import format. Details:
[CHAT_EXPORT.md](CHAT_EXPORT.md).

## Responsiveness with slow storage

Reading the archive, checking/saving/removing keys and creating/deleting chats run outside the
main thread. A loading operation shows its state; duplicate jobs are blocked. A failed access
appears as a persistent notice; on read errors the archive is not treated as empty. Repeated
change notifications are batched; outdated results are not shown as the new state.

The state is kept in a ViewModel across a screen recreation. A voice start that is still being
prepared is cancelled when the visible screen is left. No Activity object is held as a background
callback. An atomic write that has already begun can still complete; for example, an empty new
chat can remain without recording being started.

The key field is a password field. It stores the input neither in the ViewModel nor in the saved
Activity state, and it is cleared once the save request has been accepted. A success notice
confirms saving; on errors, enter the key again. Saved keys are never loaded back into the field
as plain text.

The view uses a lazily drawn list; the existing encrypted archive is still read completely in the
background. This is not a migration to a database and not a guaranteed bounded memory use for
arbitrarily large archives. Format, file names and Keystore aliases are kept.
References: [Lazy Lists](https://developer.android.com/develop/ui/compose/lists),
[Scaffold](https://developer.android.com/develop/ui/compose/components/scaffold),
[ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel).

## Hands-on check

1. Open an existing chat under "History". Check provider/model and old messages without entering
   a new key. Opening alone does not trigger an API call.
2. Switch between the three sections and scroll. Start/End must stay reachable.
3. When convenient, continue the chat, ask a short question and end it with the button.
   Also check starting again. Real provider calls can incur costs.
4. When convenient, repeat for the second provider. Use only disposable test chats to check the
   delete confirmation.

Call and headset/Bluetooth tests remain deferred.
The new UI was checked by automated tests and with a screenshot of fictional data only; the
repeated manual voice test since 0.1.9 and the full test of cloud file services/rotation are
still pending. On 2026-10-12 a manual test confirmed the local text export and the complete, readable
file. The DHU layout and actions are unchanged; 0.1.12-prototype adds English texts.

Build/install: [VOICE.md](VOICE.md). DHU: [START_WINDOWS.md](START_WINDOWS.md).
Results: [BUILD_AND_TEST.md](BUILD_AND_TEST.md).

Since 0.1.12: [German/English and resumption](LOCALIZATION.md).

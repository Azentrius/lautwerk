# Saving individual chats as text files – 0.1.12

The text export saves a selected chat as a readable UTF-8 file. It contains the title, provider,
model, last modification time in UTC and **all messages** in their stored order. Older messages
that are initially hidden in the view are exported as well. Even after opening the chat via the
history search, the entire chat is exported, not only the matching messages. An old model name
does not prevent the export.

## Usage

1. Under **History**, open a chat and end the conversation if necessary.
2. Below the chat title, tap **Export as text file**.
3. Read the notice about the unencrypted copy. **Cancel** leaves everything unchanged.
4. **Choose location** opens Android's save dialog. The suggested name is
   `OpenAutoAI-Chat.txt`, without any part of your chat text in the file name.
5. For a local copy, choose a local folder, e.g. **Downloads**, and save.
   With a cloud service, the file is transferred there by that service.
6. Only **Text file saved** confirms that the write has completed.
   Then open and check the text file in the file manager.

Opening and exporting need no AI request, no API key and no microphone. No message is sent.
Saved API credentials are not read or exported. **Personal information or secrets accidentally
written into a message remain part of the chat text.** There is no automatic redaction. The
chosen file is unencrypted and independent of the encrypted app archive. It remains even after
the chat is deleted.

The export is a text copy, not an encrypted backup and not an import format.
It does not back up keys, settings or voice messages and cannot be restored into OpenAuto AI
automatically. The app archive is not changed.

## Cancellation and errors

- During preparation, **Cancel export** is available. Leaving the screen also cancels a
  preparation that is still running.
- Exit the Android save dialog with Back/Cancel: no chat data is written.
- During preparation, selection and writing, further starts, exports, key changes and deletions
  are locked. The other sections stay reachable.
- Write, access, flush and close errors lead to a persistent error notice, not to a success
  message. An empty or incomplete file can remain at the destination; check or delete it there
  and start the export again.
- If the process ends during selection, no chat is guessed from the current selection. A late
  result writes nothing; an empty file already created by the file service can remain. While
  writing, the output is not atomic, especially with cloud file services. An interrupted export
  must be checked.

An activity recreation keeps the prepared snapshot in the ViewModel.
The dialog request is taken only once. Chat texts and destination addresses are not written to
the saved Activity state, logs or persistent URI grants.
A real rotation test with the system dialog open is still pending.

## Technical scope and testing

The app uses Android's `ACTION_CREATE_DOCUMENT` with `text/plain` and `CATEGORY_OPENABLE`; only
the returned `content:` address is written. Android handles file selection and permission. There
is no new storage permission, no access to arbitrary folders and no automatic upload.
Reference: [Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files).

A separate formatter writes the full chat in chunks, without building a second full output text
in memory. The existing archive file is still read completely; this is not a database migration
or paged loading.

Automated tests cover both providers, old models, Unicode, 121 messages, large single texts,
cancellation, duplicate dialog callbacks, missing chats, storage errors, UTF-8 output to a real
temporary test file and the confirmation in the UI.
The tests use only fictional content. On 2026-10-12, after the test assignment for a
non-sensitive chat and the local destination Downloads, the user confirmed:
**"Export and file work."** This confirms in practice the local export, opening the file and the
complete content of this tested chat. Cloud destinations and rotation during selection are not
confirmed by this.

Call and headset/Bluetooth tests remain deferred. The DHU UI 0.1.12-prototype still offers no
export; layout and actions are unchanged.

[Phone UI](PHONE_UI.md) · [Installation](VOICE.md) · [Test report](BUILD_AND_TEST.md)

Since 0.1.12: [German/English and resumption](LOCALIZATION.md).

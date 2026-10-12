# Searching and filtering chats – 0.1.12

Under **History**, saved chats can be searched by text and narrowed down to one provider. On
2026-10-12, after the test assignment for search in older messages, provider filter, opening a
match and reset, the user confirmed:
**"Search, filter and matches work."**

## Usage

1. Open **History** and enter a word or a text sequence in the **Search chats** field. Title,
   provider name, model ID and all messages are searched.
2. Optionally select **OpenAI** or **Claude**. **All** removes only the provider filter.
   Search text and provider filter apply together; the conversation/model selection does not
   change as a result.
3. The display **x of y conversations** counts matching chats, not individual text positions.
   Each chat appears once, in the existing archive order.
4. Open a match. If the text is found only in messages, the entry shows
   **Match in message …**; the app jumps to the first matching message.
   Older messages are made available in the view automatically for this.
   If the match is already in the title/provider/model, the chat opens at the start of the
   conversation view. No conversation and no AI request is started.
5. Return via **History**: search text and filter are kept for this session.
   **Reset search** clears both and shows all existing chats again.

The search is a literal, case-insensitive substring search.
Leading and trailing spaces in the search text are ignored. The input is limited to
200 characters. There are no regular expressions, no stemming, no fuzzy search and no automatic
conversion of `ä` to `ae`. An empty search text shows all chats of the selected provider. The
archive and message texts stay unchanged.

## Existing features are kept

- Start/End stays reachable in the fixed bottom bar. During a voice session the list may be
  searched; opening another chat stays blocked.
- Chats with models that are no longer listed stay readable and exportable. The search does not
  change their provider or model. Continuing stays blocked for old models.
- The [text export](CHAT_EXPORT.md) still contains **all messages of the opened chat**,
  regardless of search text, filter and the displayed section.
- Deleting still uses the confirmation for the specific chat ID. After an archive update, the
  search runs again on the current state.
- The DHU UI gets English texts in 0.1.12-prototype; it gets no search UI, no new permission and
  no voice feature.

## Responsiveness and data storage

Text search and filter run on the existing background worker. During text input, the app waits
briefly (200 ms) so that it does not start a full search for every keystroke. Outdated jobs are
cancelled and their results discarded. This also applies after the archive has changed or been
reloaded. In the meantime, no stale matches are offered for opening.

On read errors, the archive is not treated as empty. Previous search results disappear; an
existing storage error stays visible. A search-specific error offers **Retry search** and does
not show internal data or search texts as the error message. No automatic deletion or
overwriting.

Search and filter stay exclusively in ViewModel memory. There is no additional plain-text index,
no search history in files/settings and no search text in the saved Activity state. After a
process restart, the search starts empty. The search uses no API key and sends nothing to AI
providers. The keyboard is still subject to the settings of the selected Android keyboard
service.

The existing encrypted archive is still read in full. The new search is not a database index
and does not page in files. Very large archives still need memory and time. Long single texts
are searched in chunks, so that cancellation is checked within them as well.

## Testing

Automated tests cover, among other things, both providers, old models, umlauts/emoji, literal
special characters, 1,000 synthetic chats, search across a text chunk boundary, cancellation in
a long message, late results and archive errors.
The UI tests open matches in message 4 of 140 and message 96 of 100 directly, without manually
expanding or scrolling first. The search view was checked visually with fictional chats. The
user's hands-on test confirms the core operation.

Since 0.1.12, Activity recreation and language changes have been tested with a retained search
text, filter and an older match. A full process kill was not run as an end-to-end device test.
The lifetime remains bound to the ViewModel. Call and headset/Bluetooth tests remain explicitly
deferred at the user's request.

References: [Compose lists and scroll position](https://developer.android.com/develop/ui/compose/lists#controlling-scroll-position)
and [LaunchedEffect](https://developer.android.com/develop/ui/compose/side-effects#launchedeffect).

[Phone UI](PHONE_UI.md) · [Installation](VOICE.md) · [Test report](BUILD_AND_TEST.md)

Since 0.1.12: [German/English and resumption](LOCALIZATION.md).

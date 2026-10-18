# Language and resumption – 0.1.12

The app now calls the process a conversation (German **Gespräch**). The German buttons are
**Gespräch starten**, **Gespräch fortsetzen** and **Gespräch beenden**. In English these buttons
are **Start conversation**, **Continue conversation** and **End conversation**.

## Automatic language

Android selects German or English based on the device's language preferences.
The first supported language in the list is used; if neither is present, English applies.
There is no additional language switch in the app, and Lautwerk does not change the
system-wide device language.

The phone UI, dialogs, fixed status and error messages, the notification, voice settings,
preview and the labels of the text export are translated. The DHU/phone test app gets the same
two languages. Brand and model names stay unchanged. Existing titles and chat content are neither
translated nor rewritten; new empty conversations get a matching title.

Speech recognition, Android text-to-speech and the existing instruction to OpenAI and Claude also
follow the supported language. An actual model answer in that language is not guaranteed by this.
The instructions were checked offline; under 0.1.12 a manual test confirmed the conversation flow for
both providers and the German/English UI. Endpoints, models and answer budget stay the same.

While listening, the existing German commands as well as **Stop**, **End conversation**,
**Switch to Claude**, **Switch to OpenAI** and **Switch to ChatGPT** are recognized. These exact
commands switch locally or end the conversation.

## Voices

The explicit voice choice is saved separately for German and English.
Without the user's own choice, the Android default for the respective language applies. The speech
rate is still shared. A language change does not overwrite a known voice choice.

A previously saved voice is assigned based on its language as reported by the Android speech
service. Old values are not deleted. If the old voice or its service is not available, the
specific error stays visible until a new choice is made. No arbitrary replacement voice is
picked. Online voices still require the existing explicit selection.

Available voices and recognition languages depend on the installed Android speech service.
A missing language is reported as an error; the app does not download language packs without
asking.

## Rotating, switching and reopening

- When the UI is recreated within the same process, the opened chat, search text, provider filter
  and the displayed message position are kept.
- A fully prepared start or file dialog is handed over only to an active, resumed UI and is
  consumed only once.
- If the UI leaves the foreground, a conversation start that is still being prepared is
  cancelled. A late result does not start recording.
- An open permission request is kept in the ViewModel only for a configuration change, never as
  a start request in a saved Bundle. After cancellation, its later callbacks are discarded; they
  do not allow a newly started request. Notification permission stays optional.
- A new process does not start a conversation automatically. Archive and settings stay saved;
  search text/filter are not persisted to storage.
- A language change ends an active conversation. It must be started again explicitly so that
  microphone and voice use the same language.

Call and headset/Bluetooth field tests remain explicitly deferred.
The existing handling of screen off and other interruptions is unchanged.

## Checking on the Pixel

1. In German, check the three sections **Gespräch / Einstellungen / Verlauf** ("Conversation /
   Settings / History") and the **Gespräch starten** ("Start conversation") button.
2. Optionally set the device language to English in Android, reopen the app and check
   **Conversation / Settings / History**. End any conversation first.
3. After switching back, check the German voice choice under **Stimme und Tempo** ("Voice and
   speed").
4. Open a fictional or non-sensitive match in the history and rotate the device. No additional
   request or recording may start.
5. Start one short conversation each with OpenAI and Claude, listen to the answer and end it.
   Real API calls can incur costs; enter API keys only in the app.

The automated language and UI checks use only fictional content. The device language of the test
phone was not changed for them. A full process kill with a real operating-system permission dialog and
audible English reading out were not run as automated tests.

## GitHub checks

The new [workflow](../.github/workflows/android.yml) runs JVM tests, Android Lint and the debug
builds on pull requests and after changes to `main`.
JDK 17, SDK Platform 37 and Build Tools 36.0.0 are pinned. The official actions used are pinned
to full Git revisions. The workflow uses no API keys, runs no live conversations and publishes no
application. Test reports are kept for 14 days. Device tests run separately on the Pixel.

References: [Android localization](https://developer.android.com/guide/topics/resources/localization),
[Activity lifecycle](https://developer.android.com/guide/components/activities/activity-lifecycle),
[OpenAI instructions](https://developers.openai.com/api/docs/guides/prompt-engineering).

[Phone UI](PHONE_UI.md) · [Test report](BUILD_AND_TEST.md) · [Installation](VOICE.md)

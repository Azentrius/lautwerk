# Official native voice widget test

Date: 2026-11-01. Follow-up to the [settings inspection](../native-voice-settings-2026-11-01/README.md)
and [public assistant entry experiment](../../ASSIST_EXPERIMENT.md). The user explicitly requested
testing both official widget actions on the Pixel. Their goal remains starting and ending native
conversations from the car display without handling the phone.

## Result

**PASS for native phone-widget Voice entry and native End in two cycles per app.** Each widget
tap entered a voice-specific screen without an additional start tap. A microphone-mute control
and an End control were visible, and Android reported active microphone access for that app.
Selecting the native End control returned to the ordinary chat screen and ended microphone access.
The second cycle started again from the home widget after the first session had ended.

**Not an Android Auto start/stop result.** All taps were on the actual phone widget or native phone
screen, driven through ADB for this test. No widget intent or internal activity was extracted or
replayed. No tap automation was added to Lautwerk. The earlier negative real DHU `ACTION_ASSIST`
result remains valid.

## Environment

- Pixel 10 Pro, Android 17, API 37; unlocked, German UI.
- ChatGPT 1.2026.293 / 2629319, `com.openai.chatgpt`.
- Claude 1.261030.19 / 26103019, `com.anthropic.claude`.
- Existing widgets, account settings and permissions; no installation or reconfiguration.
- Google was the default assistant before and after the test.
- Repository baseline `0b237f38681207188253e6b8babc6adfe8b1d0c4`, draft PR #52.

## Procedure and observations

1. Confirm both packages have no running `RECORD_AUDIO` operation.
2. Inspect the current home widgets; use the observed voice action's bounds.
3. Tap the official widget action and read only voice controls, not conversation text.
4. Check the selected package's `cmd appops get <package> RECORD_AUDIO` result for `(running)`.
5. Tap its observed native End control. Check that the voice controls are gone, ordinary chat
   controls are back, and `RECORD_AUDIO` no longer reports `(running)`.
6. Repeat once for each app, return to Home and verify both microphones are inactive.

| App | Widget action, translated | Voice-specific controls, translated | First cycle | Second cycle |
| --- | --- | --- | --- | --- |
| ChatGPT | Start voice conversation | Turn off microphone; End | Voice UI and active microphone, then inactive after End | Same result after restart |
| Claude | Voice chat | Mute microphone; Mute Claude's voice; End | Voice UI and active microphone, then inactive after End | Same result after restart |

The exact German labels and local timestamps are in [result.json](result.json). During the first
cycle, Android reported microphone durations of 14.060 seconds for ChatGPT and 13.135 seconds for
Claude after End. These are operation durations, not measured start/stop latency. UI polling takes
time; the samples establish eventual release at observation, not an instantaneous-stop guarantee.

Both cycles were warm, unlocked entries. No app was force-stopped. No spoken test question,
answer playback, acoustic routing, voice-only end command or provider-session acknowledgement was
tested. Voice UI plus microphone access supports native Voice entry, not a completed conversation
or proof that the remote provider session has ended. There was no separate user acoustic or visual
confirmation in this test.

## Privacy and cleanup

Opening native Voice can connect to the provider and transmit microphone audio. This was not an
offline or API-free test. No test prompt was authored or sent by the test driver, and no MCP
permission was approved. Any native account-side history is owned by the original apps; it was
not inspected or deleted.

Only control labels, app versions, timestamps and microphone running/not-running observations
are retained. No recordings, transcripts, native screenshots, notification contents, account
identifiers, tokens or full system logs are stored. Transient UI dump files were removed.

At completion both microphone operations were inactive, the phone showed its home launcher and
Google remained the default assistant. No app settings, permissions, roles, data, APKs or Lautwerk
production functions changed. Calls and headset tests remain deferred.

## What this changes for the architecture

The current official widgets provide a working native Voice entry on the phone, including Claude's
widget despite the older help article describing dictation. This is more than a label-only lead.
It does not turn those controls into commands that Lautwerk can invoke or into a car Stop action.

The reviewed public Android routes have separate requirements:

- [Widget hosting](https://developer.android.com/develop/ui/views/appwidgets/host) uses
  `AppWidgetHost` and `AppWidgetHostView` with widget binding/user authorization. Hosting a view
  on a phone does not by itself expose its semantic voice-start action to another car control.
- [Launcher shortcuts](https://developer.android.com/reference/android/content/pm/LauncherApps#hasShortcutHostPermission())
  are a distinct API. Access is granted to the current launcher or active voice interaction service;
  `startShortcut` requires that access. Neither the existence of a widget nor this test proves that
  either app publishes a suitable Voice shortcut. Lautwerk was not given either role.
- The [Car App Library](https://developer.android.com/training/cars/apps/library) supplies host-rendered
  templates. No documented widget-to-template control path was established in this review.

**Next gate:** identify an actual documented entry available to Lautwerk and a supported native
end mechanism before writing a bridge. A future proposal that requires Lautwerk to become the
default assistant or launcher needs separate review and user approval. Do not extract private
intents or use background widget clicks as the implementation.

The native-phone entry is a positive result. Complete hands-free Android Auto control remains
unproven: external End, locked start, car routing, network recovery and MCP approval handling are
still separate acceptance requirements. No new DHU or vehicle test was run here.

This update changes documentation only. JSON, local links and diff formatting are checked; no
local build or instrumented suite was rerun for unchanged application code.

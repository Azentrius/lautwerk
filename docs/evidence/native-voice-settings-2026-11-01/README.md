# Native voice settings inspection

Read-only follow-up to the [public assistant entry experiment](../../ASSIST_EXPERIMENT.md),
2026-11-01. Goal: find an official automatic voice start option on the installed apps before
proposing another Android Auto route. No app code, APK, permissions or settings were changed.

## Device and scope

- Pixel 10 Pro, Android 17, API 37.
- ChatGPT `com.openai.chatgpt`: 1.2026.293, code 2629319.
- Claude `com.anthropic.claude`: 1.261030.19, code 26103019.
- German UI. Existing account configuration only; other accounts, models and rollouts are untested.
- Public MAIN/LAUNCHER resolution was used to navigate to the apps. These phone launches are not
  DHU results. No private activity was hard-coded and no shortcut intent was extracted.

The inspection read visible settings and the static labels of the already installed home widgets.
Transient UI dumps were removed. No native screenshots, conversation text, recordings, account
identifiers, notification content or MCP permission data are retained. The companion
[result.json](result.json) contains only selected findings and verbatim UI labels.

## Observed results

| App or control | Direct observation | What it establishes |
| --- | --- | --- |
| ChatGPT voice settings | Live selected. Complete visible page contains model, voice, language, dictation history, background conversations and default-assistant setup. A scroll did not reveal further rows. | No auto-start switch was visible in this configuration. Voice/model selections were not changed. |
| ChatGPT settings search | Search for voice found the voice settings category; search for start returned no matching settings. | No indexed start setting was found. This is not proof about other releases or accounts. |
| ChatGPT background conversations | Enabled. Its help text describes keeping an existing conversation running in other apps or with the screen off. | Continuation, not a verified automatic start when opening the app. |
| ChatGPT home widget | An action is labelled **Start voice conversation**. | An official phone-widget entry is present. It was not activated; successful voice start is not claimed. |
| Claude voice settings | The separate voice settings page shows voice, language and pace. A scroll did not reveal further controls. The main settings and Capabilities page were also inspected. | No auto-start switch was visible. Voice/language/pace were not changed. |
| Claude home widget | An action is labelled **Voice chat**, alongside camera, code and photos. | The current widget differs from the older article describing dictation. Its behavior was not tested. |
| Default assistant | Google remained the role holder at the end. | No role change in this inspection. |

The widget labels above are English translations for the report; the exact German labels are
retained in `result.json`. No widget audio action was pressed, no voice session or provider request
was deliberately started, and no MCP request was confirmed. The phone was returned to Home.

## Official documentation and its limits

The official [Claude Voice guide](https://support.claude.com/en/articles/11101966-use-voice-mode)
describes selecting the wave icon after opening the app. It does not establish automatic Voice on
app launch or a supported external start/stop contract. Speech that interrupts a response does not
by itself prove that the conversation has ended.

The [Claude Android widget article](https://support.claude.com/en/articles/10534883-use-the-claude-widget-on-android),
dated 2026-07-09 and checked on 2026-11-01, describes a microphone action for dictation. The installed
widget instead exposes a voice-chat label. Treat the article and the current label as distinct
evidence; neither is proof of the untested action's behavior.

No current Android-specific official documentation was obtained in this review that establishes
ChatGPT **Start with Voice** on the installed version. The earlier architecture spike's general
statement has therefore been replaced with the measured settings result, not treated as a
working setup instruction.

## Decision and next acceptance gate

**No confirmed automatic-start setting for either installed app.** This is a bounded finding, not
a claim that such a feature cannot exist on another version or configuration.

**No new GO for Android Auto start and stop.** The earlier real DHU `ACTION_ASSIST` test did not
start Voice. A home-widget action is not automatically a callable inter-app API or a car template
control. No supported external stop contract has been established. Both ends of the conversation
remain necessary for the user's hands-free goal.

The two official widget actions are worth a separate, coordinated phone test to distinguish full
Voice from dictation. This would prove a native phone entry only. Before implementing any bridge,
establish a documented invocation route available to Lautwerk, with an explicit user action,
working stop/restart, locked-phone behavior and car audio routing. Do not extract private intents,
simulate clicks in another app, or report an app launch as a running voice session.

No build or instrumented test was needed for this documentation-only update. Calls, headsets,
DHU, vehicle, network recovery and live voice tests were not performed in this inspection.

## Later authorized widget test

The [separate widget test](../native-widget-voice-2026-11-01/README.md) subsequently entered native
Voice twice per app and ended microphone access through each app's native End control. That result
does not change the read-only scope of this earlier inspection or establish Android Auto control.

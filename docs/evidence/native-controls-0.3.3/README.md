# Native voice controls from the DHU

Tested on 2026-11-01 and just after midnight on 2026-11-02, Europe/Berlin. Recorded observation times
in [result.json](result.json) are UTC. Repository baseline `572451b`, draft PR #52, experimental
0.3.3-prototype / 303. Implementation and consent model: [technical concept](../../NATIVE_CONTROLS_EXPERIMENT.md).

**GO for further supervised parked tests. No product GO.** Both official apps entered native Voice
from the actual DHU Start button and exited it through the DHU Hang up button. Two consecutive cycles
per app passed; a third cycle per app passed after shortening only the experimental status labels.
There was no phone tap between a DHU Start and its matching DHU Hang up. The test driver did not speak
or submit a question, approve MCP, or inspect native conversation content. Native Voice may transmit
microphone audio to its provider; these were not offline tests.

## Environment and results

Pixel 10 Pro, Android 17 / API 37, unlocked. Android Auto 17.9.664044-release / 179664044.
DHU 2.0-windows, build 2022-03-30-438482292, protocol 1.7, default 800 x 480 display.
The host reported Car API 8 in `car.onCreateScreen api=8`. ChatGPT 1.2026.293 / 2629319 and
Claude 1.261030.19 / 26103019. The registered [car service](car-service.txt) remains
`com.azentrius.lautwerk.car.LautwerkCarService`.

| Check | Result | Evidence |
| --- | --- | --- |
| ChatGPT Start from DHU | Native Voice controls visible on physical display 0; RECORD_AUDIO running, 3/3 cycles | [Observed states and APK hashes](result.json) |
| ChatGPT Hang up from DHU | Native End/microphone controls absent and RECORD_AUDIO inactive, 3/3 cycles | Same records; no claim about server-side session state |
| Claude Start from DHU | Native Voice controls visible on physical display 0; RECORD_AUDIO running, 3/3 cycles | Same records |
| Claude Hang up from DHU | Native End/microphone controls absent and RECORD_AUDIO inactive, 3/3 cycles | Same records |
| Explicit restart | Second cycle successful for each app, no native phone taps | Cycles 1 and 2 |
| Owned audio release | Idle DHU service cleanup acknowledged before each final native start | [Fixed event log](control-events.txt); active recording/playback release tested with fixtures, not live native audio |
| Existing car behavior and new guards | 24 synthetic device tests passed, including release-before-start, late callback, denied access, double End, restart and action matching | [Final instrumentation](instrumentation-final.txt) |
| Stale/ambiguous/replaced/removed leases | Three new JVM tests passed; dispatch consumed once | [Build summary](build-summary.json), `ControlLeaseStoreTest` |
| Ordinary app access boundary | Two tests passed before the grants and two after restoration | [Before](instrumentation-access-before.txt), [restored](instrumentation-access-restored.txt) |
| Default and release isolation | Five debug components disabled by default; no native-control classes in compiled release library | [Default manifest extraction](default-components.json), [build summary](build-summary.json) |
| Builds, lint and JVM tests | Full CI task list passed locally, 113 JVM tests without failures; final opt-in build/lint/voice tests also passed | [Build summary](build-summary.json) |
| Cleanup | All captured settings match; Google restored, prototype listener revoked, both native recordings inactive, DHU closed | [Before](settings-before.json), [restored](settings-restored.json), [result](result.json) |

Lint has zero errors, with existing warnings and three API-level metadata warnings for the
Android 17 assist-content opt-outs. This is not a zero-warning claim. The full CI task list ran
before the final text-only shortening; the affected opt-in build, car/DHU lint and 24 device fixtures
were repeated after it. The default manifest was regenerated and checked without reinstalling it.

## What actually ran

After separate user approval, the normal Android settings selected the prototype's inert voice
interaction service as assistant and granted its notification listener. The home launcher and Google
speech recognizer were not replaced. Public shortcut selection on the phone saved the observed
ChatGPT **Audio** and Claude **Sprachmodus starten** entries without starting them. No private intent,
component, session token or shortcut extras were extracted or replayed.

The actual parked DHU buttons dispatched `LauncherApps.startShortcut` under the app's own identity,
then the current app-published Hang up `PendingIntent` through the authorized listener. These were
not shell shortcut launches or simulated clicks inside the native apps. Android's native launcher
and notification contracts remain app-version-dependent.

The separate diagnostic used Android UI automation only to observe known End/microphone control
descriptions, plus physical-display focus/resume and RECORD_AUDIO state. Native UI XML was processed
in memory and its temporary device file removed. No native screenshots, texts, recordings, notification
bodies, action intents or keys were retained. Widget nodes on the home screen do not prove visibility:
the report requires physical-display resume/focus as well as the known native Voice controls.
The final fixed event log is restricted to owned cleanup, dispatch outcomes and Car API level.

The preceding launcher concern was a test-navigation error: Lautwerk was at the very bottom, after
the customization entry. Both the saved pre-prototype APK and the new APK appeared there and opened
after fresh DHU connections. No host rejection or launcher regression was established. The comparison
and final update used ordinary `adb install -r`; no uninstall, data reset, installer-origin change,
host-validation change or Play Protect bypass was performed.

## UI evidence

The original API screen and host-drawn template remain. The new opt-in pages use the existing host
colors and parked action guard. Their initially truncated status messages were shortened; the final
German messages below are visible in full on this host. A request or available action is deliberately
not presented as a confirmed native conversation state.

- [App selection](dhu-picker.png)
- ChatGPT: [ready](dhu-chatgpt-ready.png), [Hang up available](dhu-chatgpt-end-available.png),
  [Hang up requested](dhu-chatgpt-end-requested.png)
- Claude: [ready](dhu-claude-ready.png), [Hang up available](dhu-claude-end-available.png),
  [Hang up requested](dhu-claude-end-requested.png)

## Remaining limits and next acceptance

- Start deliberately refuses a locked phone. This prototype does not meet unattended locked-phone
  operation; no lockscreen bypass was added. Lock behavior is a product constraint, not a successful test.
- The temporary assistant role replaces Google's assistant gesture. Android notification access is
  broad despite the app-side package/action filter. Both require explicit informed setup and were
  restored/revoked after this trial. Installing the APK alone is insufficient to activate the controls.
- Shortcut labels are a narrow observed compatibility list; app updates require reselection. Unknown
  labels/actions fail closed. No universal language, app version or account guarantee is established.
- Real questions/answers and acoustic routing, End during a spoken answer, network loss/reconnect,
  listener/process recovery, locked End, and a stationary vehicle trial are still open. Stale and
  duplicate controls have synthetic coverage, not every possible native race.
- MCP prompts remain in the original app. Lautwerk neither detects nor approves them. Notification
  disappearance and microphone inactivity do not prove remote session closure.
- Call/headset and driving tests remain deferred. Do not merge or treat this as distribution approval.

Next: review this result, then explicitly schedule a short parked question/answer test per app, including
End during an answer. Evaluate the role/privacy/lockscreen constraints before considering production
changes. The installed opt-in build remains available, but its special access is now revoked.

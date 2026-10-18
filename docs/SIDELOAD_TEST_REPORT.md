# Sideload and car tests

Historical baseline. A later manual test confirmed vehicle launcher visibility and opening after the
approved installation-origin experiment. See [CAR_VOICE.md](CAR_VOICE.md) for the current package,
installation steps and separate audio evidence; the negative observation below remains historical.

As of 2026-10-12. App code and APK stay at the confirmed state 0.1.15, source commit
`5600446fbe08a8bbdf460f31a9e3b9fbbeaedead`. The [test guide](SIDELOADING.md) describes the comparison.
Package and class names of that version differ from today's; [CHANGELOG.md](../CHANGELOG.md) (0.2.0)
lists them.
On 2026-10-12 at about 17:00, the app was not visible in the ID.7. A working
path in the car is therefore still not confirmed. The ID.7 remains the test car; all Kia attempts are
deferred.

## Measured on the connected Pixel

| Property | Result |
| --- | --- |
| Phone | Pixel 10 Pro |
| Android | 17, API 37 |
| Security patch | 2026-09-05 |
| Active Android Auto | 17.7.663654-release, code 177663654 |
| Active Google Play services | 26.39.63 (260400-995553646), code 263963035 |
| AAWireless phone app | 8.5.2819, code 8052819; no adapter firmware |
| Test app | DHU test package, 0.1.15-prototype, code 6 |
| Assistant (separate) | assistant package, 0.1.15-dev, code 15 |
| Installation source of the test app | installerPackageName=null; initiatingPackageName=com.android.shell |
| Registered car service | car service of the test package, exactly one match |
| Local test APK SHA-256 | F580E27ACEC2395AE0097E0A8E7ACE4F7B62288B1CEAD6B6698042A6505F5D70 |

The SHA-256 refers to the local build output
`prototype-app/build/outputs/apk/dhu/debug/prototype-app-dhu-debug.apk`.
No APK was reinstalled or extracted from the phone during this preparation. The package data shows the
installed version and the ADB installation path.

The system and stub versions that Android also lists are not taken as the active Android Auto or Play
services version. The adapter is described as the first AAWireless model with firmware "7.xxx";
the exact firmware and hardware ID still have to be confirmed on the connected adapter.

## Results per connection

| Run | Settings | Launcher / Open / Start / Reconnect | Car API | Status |
| --- | --- | --- | --- | --- |
| DHU from the 0.1.15 acceptance | PC configuration at the time | passed, see the report from that time | see the report from that time | Historical control result, not a new car test |
| Kia with AAWireless, baseline | still to be recorded | open / open / open / open | not measured | Deferred |
| Kia with adapter developer mode | only after the baseline measurement | open / open / open / open | not measured | Deferred |
| Kia after restoring | original values | open / open / open / open | not measured | Deferred |
| ID.7 at about 17:00 on 2026-10-12 | native wireless connection according to context; versions and detailed configuration at the time not read out | app not visible / not checked / not checked / not checked | not measured | User observation, no logs from the same time |
| ID.7 with the currently prepared 0.1.15 | without AAWireless, previous settings | open / open / open / open | not measured | No new unchanged attempt needed; first prepare a concrete technical change or diagnosis |

For each actual run, add: time with time zone, infotainment version, for the Kia the exact adapter
firmware and both setting values, number of successful reconnects and, if any, the exact error. Mark
user observations and logs read from the device as different kinds of evidence.

## Preparing the ID.7 attempt

Before the car attempt, the installed test app 0.1.15-prototype/6 and Android Auto
17.7.663654-release/177663654 were confirmed again via ADB. No running DHU process was found on the
PC. As the start mark for the later log analysis, the device clock gave Unix time `1791839347`. This
is a preparatory time boundary, not proof of a car connection. A new car run after this time boundary
is not confirmed. After the feedback on the drive at about 17:00, no unchanged repeat test is requested
anymore. The 17:00 observation is before this start mark.

The Git comparison after 17:00 shows design reference files, the app icon and splash screen in 0.1.15,
and after that only documentation changes. Car service registration, category, HostValidator, session
and car template logic stayed unchanged. The installation path stayed ADB. No visibility problem in
the car was fixed; these changes are not expected to produce a different launcher result. The app
version actually installed at about 17:00 is not documented, so the finding is not listed as a
completed 0.1.15 test.

## Limits of these results

The preparation confirms that the known UI prototype is installed on the Pixel and its car service is
registered. It does not prove that the launcher in the car allows the app. The car app still only has
the UI test counter. API requests, voice in the car, and call, headset and Bluetooth audio
interruptions were not tested.

App code, design, manifest and host check were not changed for this preparation. The documentation
change does not require a new app build. The [0.1.15 acceptance](BUILD_AND_TEST.md) remains the latest
app and DHU test state.

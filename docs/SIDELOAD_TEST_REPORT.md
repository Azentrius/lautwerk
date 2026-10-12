# Sideload and vehicle test report

As of 2026-10-13. The tested APK is unchanged **0.1.15-prototype / 6**, built from
source commit `5600446fbe08a8bbdf460f31a9e3b9fbbeaedead`. A specifically authorized
installation-origin experiment now makes OpenAuto AI appear in Android Auto's
phone-side launcher settings; the DHU control passes. **The native wireless ID.7
test after that change is still pending.** Kia/AAWireless tests remain deferred.

[Distribution investigation](SIDELOADING.md) · [Exact installation experiment](SIDELOAD_INSTALL_EXPERIMENT.md)

## Measured environment

| Item | Measurement |
| --- | --- |
| Phone | Pixel 10 Pro |
| Android | 17, API 37; security patch 2026-09-05 |
| Active Android Auto | 17.7.663654-release, code 177663654 |
| Active Google Play services | 26.39.63 (260400-995553646), code 263963035 |
| AAWireless phone app | 8.5.2819, code 8052819; not adapter firmware |
| Test app | dev.openautoai.prototype.dhu, 0.1.15-prototype, code 6 |
| Separate assistant | dev.openautoai.app, 0.1.15-dev, code 15 |
| Car service | dev.openautoai.car.OpenAutoCarService; exactly one registration |
| Local and installed APK SHA-256 | F580E27ACEC2395AE0097E0A8E7ACE4F7B62288B1CEAD6B6698042A6505F5D70 |
| APK certificate SHA-256 | 687cf415a6b6d4f45a8a92a55a9fcd91c463bc0d0d9d8a77ce95c9afbe63e696 |

The installed APK was pulled from the phone both before and after the experiment.
Both hashes match the local build at
`prototype-app/build/outputs/apk/dhu/debug/prototype-app-dhu-debug.apk` exactly.
Android Auto and Play services stub/system versions were not mistaken for the
active versions. The user describes the first-generation AAWireless adapter as
running firmware “7.xxx”; its exact firmware and hardware ID remain unverified.

## Results by connection

| Run | Visibility / Open / Start / Reconnect | Car API | Evidence |
| --- | --- | --- | --- |
| Historical DHU acceptance 0.1.15 | Passed; see previous report | 8 in that report | Historical control, not a vehicle test |
| ID.7, approximately 17:00 CEST on 2026-10-12 | Not visible / not tested / not tested / not tested | Not measured | User observation; no contemporaneous logs or verified app version |
| Phone launcher settings before origin experiment | Not offered in the complete list | Not applicable | UI inspection and package rejection logs |
| Phone launcher settings after origin experiment | Present and checked | Not applicable | UI inspection; no simultaneous DHU session |
| DHU after origin experiment, 2026-10-13 | Visible / passed / Test 1 / not repeated | 8 | New screenshot and app-log control |
| ID.7 native wireless after origin experiment | Pending | Not measured | Two parked connections requested |
| Kia/AAWireless baseline, developer-mode and restoration comparisons | Deferred | Not measured | User preference |

For actual vehicle runs record timestamps/timezone, infotainment version, connection
type, number of successful reconnects and exact errors. User observations and
retrieved logs are different evidence. The adapter's firmware and its phone-app
version must also remain separate.

## What was known before this diagnosis

The user reported no icon during an ID.7 drive around 17:00 CEST on October 12.
That observation predates the preparation marker `1791839347` (23:09:07 CEST).
The marker was not a vehicle connection. The installed version at 17:00 is not
proven, so this is not retroactively called a 0.1.15 test.

Changes after 17:00 covered design-reference files, the app icon/splash screen and
documentation. Car registration, category, HostValidator, session and template logic
were unchanged, and installation still used ordinary ADB. Those changes did not
fix discovery. An unchanged blind retest was therefore not requested.

## Phone diagnosis: 2026-10-12, 23:52 CEST

With no car connection, the complete Android Auto Customize launcher page
(“Launcher anpassen”) did not contain OpenAuto AI. Unknown sources and wireless
Android Auto were enabled. The App mode dialog had no selected entry. None of
these settings was changed. The head-unit server was initially running; no DHU
process was running on the PC.

In the active Android user the test package was installed, enabled, not hidden,
not stopped and not suspended. Querying the CarAppService action plus POI category
returned exactly our service. POI remains the disclosed debug placeholder, not
an approved product category.

After restarting only the Android Auto process, without clearing data, opening
Customize launcher produced six denials of our package. Excerpt from the retrieved
log buffer; timestamp and message are unchanged, process/thread IDs omitted:

```text
1791841976.418 W CAR.VALIDATOR: isPackageInstalledByPlayCheck service not connected
1791841976.418 W CAR.VALIDATOR: Package DENIED; failed all other checks [dev.openautoai.prototype.dhu]
```

This is 2026-10-12 23:52:56.418 CEST. The two lines came from the same check/thread.
The temporarily verbose `CAR.VALIDATOR` and `PlayGearheadService` log tags were
restored to their original empty values. An additional continuous background
capture stayed empty and was stopped; the evidence above came from `logcat -d`,
not that file. Private raw data remains in ignored `diagnostics/`.

**Established:** Android Auto rejected the package while selecting apps. That
check did not call `car.createHostValidator`, `car.onCreateSession` or
`car.onCreateScreen`. A rendering failure in our template does not explain this gate.

**Not established by those lines:** the particular eligibility rule decisive in
the ID.7. The disconnected Play-check warning came from phone settings after a
process restart. It proves neither a permanent Play-services fault nor an empty
Play ownership record in the car. No contemporaneous logs remain from 17:00.

## Authorized origin experiment

The user explicitly authorized one bounded exception to the earlier no-spoofing
constraint: update the identical test APK through the existing Android system
installer while claiming Google Play as its origin. No root, host patches or
app-code changes were authorized or used for this experiment.

The system installer accepted the APK as an **update**. With separate user approval,
Google Play Protect scanned the APK. Its result read “Diese App ist anscheinend
sicher” and allowed installation. The update completed at **2026-10-13 00:03:32 CEST**.
There was no uninstall, data deletion, disabling of Play Protect, KingInstaller
installation or Shizuku installation.

| Check | Before | After |
| --- | --- | --- |
| Version / code | 0.1.15-prototype / 6 | Unchanged |
| Installed APK SHA-256 | F580E27ACEC2395AE0097E0A8E7ACE4F7B62288B1CEAD6B6698042A6505F5D70 | Identical |
| installerPackageName | null | com.android.vending |
| initiatingPackageName | com.android.shell | com.google.android.packageinstaller |
| originatingPackageName | null | null |
| packageSource | 1 | 3 |
| Customize launcher, without DHU | Missing from complete list | Present; checked=true, enabled=true |
| DHU control | Historical acceptance | Open and Start pass again |
| ID.7 native wireless after update | Not tested | Pending |

Android Auto alone was restarted again before reading the phone launcher page.
DHU launcher images were not substituted for this phone-side observation.
[Sanitized measurements](evidence/sideload/origin-experiment-2026-10-13.json).

**Result:** A changed installation procedure made a previously missing entry appear,
with byte-identical APK and unchanged Android Auto developer settings. This establishes
an effective dependency on the installation procedure. Installer, initiator and
installation flow changed together, so neither field was isolated as necessary or
sufficient. In particular, `adb install -i com.android.vending` is **not** the tested
procedure. The ID.7's actual eligibility check is still to be measured.

The [reproduction guide](SIDELOAD_INSTALL_EXPERIMENT.md) requires a PC with ADB.
It is an authorized development experiment, not yet a validated user distribution guide.

## DHU control after the origin experiment

Official DHU 2.0-windows, build 2022-03-30-438482292, existing 800x480 touch profile,
using its headless/console controls. Opening from the launcher showed OpenAuto AI,
“Bereit für den UI-Test” and the retained “Claude / Anthropic · Claude Haiku 4.5”
selection. Start visibly changed the status to “Gestartet · Test 1”. Matching logs:

```text
1791842847.712 I OpenAutoAI: car.createHostValidator (signature allowlist enabled)
1791842847.718 I OpenAutoAI: car.onCreateSession
1791842847.728 I OpenAutoAI: car.onCreateScreen api=8
1791842847.749 I OpenAutoAI: car.onGetTemplate count=0
1791842866.972 I OpenAutoAI: car.start count=1
1791842866.974 I OpenAutoAI: car.onGetTemplate count=1
```

These events span 00:07:27–00:07:46 CEST on October 13. Android Auto remained
17.7.663654-release / 177663654. **Car API 8 belongs to this DHU session**, not the
ID.7. The DHU was closed with `quit`, ADB port forwarding removed and the head-unit
server stopped normally for the following vehicle test.

## Vehicle log capture

The [diagnostic helper](../tools/sideload/README.md) was tested on the Pixel:
an explicit self-test marker was captured; automatic completion after ten seconds
was confirmed. Invalid session ID, duration over 30 minutes and a reused session
ID each failed with exit code 2. It takes package-filtered snapshots every five
seconds into bounded rotating files. It changes no app or system settings.
Self-test markers are not vehicle evidence.

The user-run capture started at 2026-10-13 00:08:50 CEST (Unix 1791842930), ending
after approximately 20 minutes. This is a recording start, not proof of an actual
vehicle connection or success. The user was asked for two parked native wireless
connections and a return to the PC, with an explicit option to defer the car test.

## Remaining limits

The car app still contains only the UI test counter. API requests, conversations
in the car, calls, headsets and Bluetooth-audio interruptions were not tested.
No new APK build was necessary for these documentation/diagnostic changes. The
[0.1.15 acceptance](BUILD_AND_TEST.md) remains the broader app test baseline.

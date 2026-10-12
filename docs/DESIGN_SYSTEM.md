# Azentrius in OpenAuto AI 0.1.13

The existing phone app uses the approved Azentrius design: light/dark following the system,
DM Sans, the supplied PatternFly icons, fixed color tokens and components without ripple.
Dynamic Material colors are off.
The DHU UI keeps its existing Android Auto templates.

## Current reference and implementation sources

Current design reference: [170dd38](https://github.com/Azentrius/design-system/tree/170dd38be84a9f107440a730eb29361087fc63ea)
in the private repository Azentrius/design-system.
It contains the DHU refinements and the new app icon from `6a7a951`.
The original import was a pure source update. In 0.1.15 the app icon, splash screen and service
icon were integrated; see [APP_IDENTITY.md](APP_IDENTITY.md).
The confirmed page design is kept.

The following historical sources document the implementation of 0.1.13:

- Original package (ZIP), imported unchanged; the copy has since been removed from this
  repository. ZIP SHA256: `fdf2805c9690584e6ea12b36032972bb021285070758245c666ac42b873d984f`.
- Addition: [Design system 7601a14](https://github.com/Azentrius/design-system/tree/7601a14763258e8d0b7929efd4a3689e10d31cab).
  Its Android rule takes precedence over the Android section of the ZIP.
- Color values and Android assets match both sources. Four TTFs, the XML font family and
  30 vector drawables were taken over unchanged.
  DM Serif Display is included but not used on these pages.
- The OFL and MIT licenses are included in the app and can be read under Settings →
  Licenses without network access.

No new colors, icons or sizes were invented for the design.
The rule for narrow tabs, which was missing at first, was added to the design system by the user
and only implemented afterwards. No further requirements are open.

## Rendering and existing behavior

`design/AzentriusTheme.kt`, `AzPalette.kt`, `AzComponents.kt` and `AzScreens.kt`
encapsulate the rendering. Font sizes stay in sp, spacing in dp.
The three tabs measure their longest label at the current font size: side by side, stacked, or
horizontally scrollable. All tabs switch together; with scrollable tabs, the selected tab is
moved fully into view without animation. Labels stay single-line, unhyphenated and complete.
The general typography disables hyphenation. Actions stack as space requires.

Conversation, Settings, History and the separate Voice and speed page keep their functions.
User texts appear as a right-aligned bubble, answers at full width. History entries show the
last change, provider and model; existing message matches are unchanged. There are no new
snippets, highlights, durations or interim speech recognition results.

The bottom bar shows the conversation phase and the export separately. Its rendering uses typed
states from the existing controller. Start, Continue, End and start/export cancellation keep
their existing preconditions. Export results stay visible until the next preparation or the next
conversation start. There is no additional save button and no cancellation while the file is
being written.

The voice list uses radio buttons. Each explicit online selection opens the existing
confirmation; cancelling does not change the selection. There is no saved consent status. The
Android default stays unmarked. The two preview buttons stay visible and play only the selected
voice. Providers, model catalog, storage, API calls and audio flow are kept.

## Update on this Windows PC

Project: `C:\Users\stefa\Documents\Projekte\Azentrius\OpenAuto AI`.

```powershell
Set-Location -LiteralPath 'C:\Users\stefa\Documents\Projekte\Azentrius\OpenAuto AI'
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
.\gradlew.bat :app:assembleDebug
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
& $adb -s 56161FDCH000H8 install -r '.\app\build\outputs\apk\debug\app-debug.apk'
& $adb -s 56161FDCH000H8 shell am start -n 'dev.openautoai.app/.MainActivity'
```

For the download, use the same install command with the full path to
`OpenAutoAI-0.1.13-assistant-debug.apk`. Expected: `Success`;
version `0.1.13-dev`, code 14. The update uses the same debug signature and
keeps the app data. Do not uninstall if Android reports a different signature.

The DHU/phone variant stays at `0.1.12-prototype`, code 4. Its exact installation
and DHU connection steps are still in [START_WINDOWS.md](START_WINDOWS.md).
This update does not turn the phone assistant into a permitted Android Auto category.
The experimental registration and host validation do not change.

## Visual check by the user

View Conversation, Settings, History and Voice and speed in light and dark mode.
With a large system font, the tabs stay complete and can be scrolled.
An existing chat can be opened; the export and online voice dialogs can be cancelled.
The existing German/English settings are kept.
A pure design check needs no API requests or previews.

Automated and visual results: [BUILD_AND_TEST.md](BUILD_AND_TEST.md).
Call and headset/Bluetooth field tests remain explicitly deferred.

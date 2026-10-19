# Retired Pocket / Alba integration

The optional companion integration was removed in 0.3.2. Lautwerk now uses the phone's configured
Android text-to-speech service. Existing companion selections return to Android default, while
device voice choices and speech rate are preserved. This change does not uninstall other apps,
delete model files or change the system-wide TTS engine.

The setup guide, build helper and cancellation patch are no longer part of the current integration.
For historical investigation only, see the [previous implementation at 8eb3288](https://github.com/Azentrius/lautwerk/blob/8eb3288/docs/POCKET_TTS.md)
and the unchanged [0.1.19 evidence](evidence/0.1.19/pocket-integration.md).

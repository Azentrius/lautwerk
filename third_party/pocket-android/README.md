# Evaluated Pocket Android cancellation patch

This is an optional companion engine, not a native dependency embedded in OpenAuto AI.
The original engine is MIT-licensed; preserve the adjacent `LICENSE` and all upstream
native/model notices. `cancellation.patch` is a complete diff against upstream commit
`37809d23d68ea17fdeb5faad5edc97771022cf9a` (0.5.3). It includes the isolated debug package,
version and changelog, synchronous synthesis, native cancellation/lifetime fixes,
and two Windows build-portability adjustments. No model weights are in this patch.

The tested local source commit was `503048d449352c1187b01560ae76aa8176b5bf82`.
The app explicitly requires `org.pockettts.android.engine.openautotest`, version
`0.5.3-openauto-cancel.1` / 24, with no INTERNET permission. It never selects the
uncorrected `org.pockettts.android.engine` automatically. This is a temporary
evaluation identity, not an official Pocket release or a general engine discovery API.

Build in a separate checkout with JDK 17, Android SDK 37, NDK 27.2.12479018 and
CMake 3.22.1, following the upstream `docs/BUILDING.md`:

```sh
git clone https://github.com/The-unknown-Shadowman/PocketTTS-Android-Engine.git
cd PocketTTS-Android-Engine
git checkout 37809d23d68ea17fdeb5faad5edc97771022cf9a
git apply /path/to/openauto-ai/third_party/pocket-android/cancellation.patch
bash scripts/prepare_android_native_deps.sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

On Windows use the upstream Android Studio/native setup and `gradlew.bat`.
The preparation script is a Bash script; do not run it as PowerShell.
Install `app/build/outputs/apk/debug/app-debug.apk` as a separate companion.
Different developer signing keys cannot update an existing debug installation;
do not uninstall an existing engine silently, because that deletes its models.

The exact APK validated on the Pixel had SHA-256
`8519c903dfcf4ecaa9d2e242c517a7a7f3cdc71496502c9631895a276ac31f72`.
A local build with another debug certificate will have a different APK hash.

See [setup and limitations](../../docs/POCKET_TTS.md). The pinned companion is
arm64-only. This change does not claim support for every Android device or a public
installer distribution. The default Google/Android engine stays available.

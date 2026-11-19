# Local Android environment

Last verified: 19 November 2026

Use this installed environment before downloading or installing anything.

## Toolchain

| Component | Existing location/version |
| --- | --- |
| Host | macOS arm64 |
| JDK | OpenJDK 17.0.21 at `/opt/homebrew/Cellar/openjdk@17/17.0.21.0.3/libexec/openjdk.jdk/Contents/Home` |
| Android SDK | `/Users/sundi/Library/Android/sdk` |
| Platform tools | 37.0.1 |
| Emulator | 37.2.13.0 |
| SDK platforms | Android 34 and 36 |
| Build tools | 34.0.0, 35.0.0, 36.0.0 |
| Cached Gradle distributions | 8.9, 8.11.1, 8.13, 8.14.7 |
| Cached Android Gradle Plugin | 8.5.2, 8.9.2, 8.13.2 |
| Cached Kotlin Gradle plugin | 2.0.21 |

No Android Studio installation was found; the working environment is CLI-first. Use a project Gradle wrapper rather than requiring a global Gradle installation.

Recommended shell environment:

```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.21.0.3/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/Users/sundi/Library/Android/sdk
export ANDROID_SDK_ROOT=/Users/sundi/Library/Android/sdk
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
```

## Existing emulator

- AVD ID: `NoDoomPolicyPixel9Pro`
- Display name: **No Doom Policy Pixel 9 Pro**
- Device profile: Pixel 9 Pro
- Android/API: Android 16 / API 36, Google APIs, ARM64
- Display: 1280 × 2856 at 480 dpi
- RAM/data: 4096 MB / 6 GB
- Play Store image: no; Google APIs are available

Launch it with:

```bash
$ANDROID_HOME/emulator/emulator -avd NoDoomPolicyPixel9Pro
```

Wait for readiness with:

```bash
adb wait-for-device
until [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do sleep 2; done
```

This AVD is the shared disposable test device. It was reset to a clean baseline on 19 November 2026. Do not create another emulator unless a materially different API level, form factor, or Play Store image is required.

## Physical device

A Pixel 9 Pro is available over ADB when the user connects and unlocks it. Always discover it with `adb devices -l`; do not hard-code a serial. Preserve installed app data with `adb install -r` and verify the package/application ID before updating.

## Dependency discipline

1. Inspect installed SDK components and Gradle caches first.
2. Prefer versions already cached when they meet current Android/Play requirements.
3. Verify current official compatibility before selecting AGP, Gradle, Kotlin, Compose, or target SDK versions.
4. Download only a proven missing dependency, and record it here afterward.


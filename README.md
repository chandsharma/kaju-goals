# Kaju Goals

Kaju Goals is an open-source, local-first Android app for making personal goals and progress effortless to track. The current build is an early verified vertical slice, not a Play release.

## What works

- Today dashboard and first-class Goals list
- One-tap quick starts plus Count, Time, Percentage, Money, and Done goals, including INR
- Large metric-aware progress stepper with shortcuts, optional notes, and immediate Undo
- Exact chronological history and confirmed deletion
- Goal edit, pause/resume, complete, archive, filter, and sort
- Offline Room persistence with no required account, analytics, ads, or trackers
- Optional user-owned Google Drive hidden-app-data sync with merge-first restore and no Kaju backend
- Edge-to-edge Compose UI, keyboard-safe forms, Navigation 3 back stacks, and adaptive navigation foundations

## Build and test

The project uses JDK 17 and the checked-in Gradle wrapper.

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

To run device tests, start an Android API 36 emulator and use:

```bash
export ANDROID_SERIAL="$(adb devices | awk '/^emulator-/{print $1; exit}')"
./gradlew :app:connectedDebugAndroidTest
```

Install the development build without clearing existing app data:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Project documentation

- [Project context](docs/PROJECT_CONTEXT.md)
- [Decision log](docs/DECISIONS.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Google Drive sync architecture](docs/SYNC_ARCHITECTURE.md)
- [Testing strategy](docs/TESTING.md)
- [Product proposal](docs/PRODUCT_PROPOSAL.md)
- [Design direction](docs/DESIGN_DIRECTION.md)
- [Tracking usability requirements](docs/UX_REQUIREMENTS.md)
- [Quality standard](docs/QUALITY_STANDARD.md)
- [Open-source policy](docs/OPEN_SOURCE.md)

## Contributing

Product discussion, accessibility feedback, Android expertise, design critique, tests, and implementation contributions are welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md), the [Code of Conduct](CODE_OF_CONDUCT.md), and [Security Policy](SECURITY.md) before participating.

## Release status

Play publication, release signing, store assets, and Play disclosures are intentionally deferred until the owner explicitly approves a release.

## License

Apache License 2.0. See [LICENSE](LICENSE).

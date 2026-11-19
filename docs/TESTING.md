# Testing strategy

Last updated: 19 November 2026

## Current layers

- Local JUnit 4 tests cover domain progress calculations and ViewModel filtering with a fake repository.
- Instrumented Room tests use an in-memory database and the real Android SQLite engine.
- Instrumented Compose behavior tests cover the Goals empty/create action and explicit confirmation before deleting progress.
- Manual AVD journeys cover the complete create/log/undo/history/navigation path and rendered edge-to-edge/IME behavior.

## Commands

Use the environment from `docs/ENVIRONMENT.md`.

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
./gradlew :app:lintDebug
./gradlew :app:assembleDebug :app:assembleRelease :app:bundleRelease
```

Run connected tests against the disposable emulator only:

```bash
export ANDROID_SERIAL="$(adb devices | awk '/^emulator-/{print $1; exit}')"
./gradlew :app:connectedDebugAndroidTest
```

## Device evidence

The validated AVD is `NoDoomPolicyPixel9Pro`, Android 16/API 36, 1280 × 2856 at 480 dpi. Fictional-data screenshots live in `docs/screenshots/development/`.

The verified manual flow is:

1. Open empty Today and create a count goal.
2. Use the keyboard and scroll the form without obscuring target or save controls.
3. Save and reach focused goal detail.
4. Log progress with a shortcut, observe the exact total/history, and receive Undo.
5. Switch among Today, Goals, and Progress and confirm the completed form does not resurface.
6. Create and complete a binary goal.
7. Rotate with state intact.
8. Attempt history deletion and verify the explicit confirmation dialog.

## Deferred coverage

- Preview screenshot regression: stable AGP 9.0 requires the deprecated standalone screenshot plugin. Use AGP test suites after a separately reviewed AGP 9.5+/API 37 toolchain upgrade.
- Add UI tests for back handling, multiple-back-stack restoration, edit, pause/resume, complete/archive, and validation failures.
- Add 400/610/900 dp width and 400/500/1000 dp height coverage, dark theme, and 1.5× font scale.
- Add TalkBack traversal, large dataset, startup/scroll performance, and process-death testing.
- Add migration, export/restore, backup, and recovery tests when those features exist.

Do not update visual references automatically. Review diffs and rendered states before accepting any future screenshot baseline.

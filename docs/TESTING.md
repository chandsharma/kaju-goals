# Testing strategy

Last updated: 20 November 2026

## Current layers

- Local JUnit 4 tests cover domain progress calculations, ViewModel filtering, and deterministic Drive snapshot merging with a fake repository or pure merge inputs.
- Instrumented Room tests use the real Android SQLite engine for aggregation and the explicit v1→v2 migration.
- Instrumented Compose behavior tests cover the Goals empty/create action, one-tap template creation, automatic Percentage targets, and explicit confirmation before deleting progress.
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

On 20 November 2026, all 7 connected tests passed on that AVD after the corrected create/log/list/sync implementation. The final debug APK was installed with `adb install -r`, launched into `MainActivity`, and produced no runtime fatal exception. The physical Pixel was not attached to ADB during this final pass; its existing app data must be preserved when the same APK is installed after reconnecting.

The verified manual flow is:

1. Open empty Today and create a count goal.
2. Use the keyboard and scroll the form without obscuring target or save controls.
3. Save and reach focused goal detail.
4. Log progress with a shortcut, observe the exact total/history, and receive Undo.
5. Switch among Today, Goals, and Progress and confirm the completed form does not resurface.
6. Create and complete a binary goal.
7. Rotate with state intact.
8. Attempt history deletion and verify the explicit confirmation dialog.
9. Create a complete `Read 12 books` goal from the template without typing.
10. Log `+1 book` using the large stepper and verify singular/plural formatting, exact list progress, and Undo.
11. Inspect the disconnected Drive state and verify that local use does not require an account.

## Deferred coverage

- Preview screenshot regression: stable AGP 9.0 requires the deprecated standalone screenshot plugin. Use AGP test suites after a separately reviewed AGP 9.5+/API 37 toolchain upgrade.
- Add UI tests for back handling, multiple-back-stack restoration, edit, pause/resume, complete/archive, and validation failures.
- Add 400/610/900 dp width and 400/500/1000 dp height coverage, dark theme, and 1.5× font scale.
- Add TalkBack traversal, large dataset, startup/scroll performance, and process-death testing.
- Add end-to-end Drive API tests with a dedicated disposable Google account before public release; pure merge and migration safety are covered now.

Do not update visual references automatically. Review diffs and rendered states before accepting any future screenshot baseline.

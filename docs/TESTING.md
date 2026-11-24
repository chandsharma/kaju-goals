# Testing strategy

Last updated: 24 November 2026

## Current layers

- Local JUnit 4 tests cover progress calculations, Indian-number/title inference, deadline pace, recurrence period boundaries, ViewModel filtering, and deterministic Drive snapshot merging/legacy decoding.
- Instrumented Room tests use the real Android SQLite engine for recurring aggregation and explicit v1→v2→v3 migrations.
- Instrumented Compose behavior tests cover the Goals empty/create action, natural-title inference, manual inference override, one-tap templates, automatic Percentage targets, progress correction editing, and explicit confirmation before deleting progress.
- Manual AVD journeys cover creation, logging, correction, weekly review, organization, portable-data controls, edge-to-edge/IME behavior, dark theme, 130% text scaling, and landscape.

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

On 24 November 2026, all 12 connected tests passed on that AVD after the sentence-first/correction/pace/exceptional-feature slice. The final debug APK was installed with `adb install -r`, launched into `MainActivity`, and produced no runtime fatal exception. The same version code 2 APK upgraded the USB-connected physical Pixel from version code 1 with `adb install -r`; the package was not uninstalled or cleared.

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
12. Type `Save ₹1 lakh`, inspect the Money/INR inference, manually change the measurement to Count, edit the title, and verify the manual choice survives.
13. Add, subtract, set total, backdate, edit, and delete progress while checking exact totals and history.
14. Inspect the exact seven-day review and the You screen's JSON backup/restore, CSV export, lock, and Drive controls.
15. Inspect dark mode, 130% text scale, and landscape on the Pixel 9 Pro AVD.

## Deferred coverage

- Preview screenshot regression: stable AGP 9.0 requires the deprecated standalone screenshot plugin. Use AGP test suites after a separately reviewed AGP 9.5+/API 37 toolchain upgrade.
- Add UI tests for back handling, multiple-back-stack restoration, edit, pause/resume, complete/archive, and validation failures.
- Add 400/610/900 dp width and 400/500/1000 dp height coverage and 1.5× font scale; dark theme, 1.3× text, and landscape have manual evidence.
- Add TalkBack traversal, large dataset, startup/scroll performance, and process-death testing.
- Add end-to-end Drive API tests with a dedicated disposable Google account before public release; pure merge and migration safety are covered now.

Do not update visual references automatically. Review diffs and rendered states before accepting any future screenshot baseline.

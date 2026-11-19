# Project context

Last updated: 19 November 2026 (Asia/Kolkata)

## Current state

Kaju Goals now has a working, local-first Android vertical slice. It is intentionally an early development build, not a Play release and not yet production-complete. The core personal-tracking loop works on the Pixel 9 Pro API 36 AVD: create a goal, view it in a real list, log progress in two actions, undo a new log, inspect exact history, edit a goal, filter/sort goals, and complete a binary goal.

The public repository is `https://github.com/chandsharma/kaju-goals`. Play Console publication is explicitly deferred until the user confirms.

## Product identity

| Item | Decision |
| --- | --- |
| Product name | Kaju Goals |
| Application ID | `com.chandsharma.kajugoals` |
| Debug application ID | `com.chandsharma.kajugoals.debug` |
| Version | `1` / `0.1.0-dev` |
| Primary user | The owner as the first real user; broader launch audience remains open |
| Primary promise | Make personal goals and progress effortless to track |
| Primary destinations | Today, Goals, Progress, You |
| Minimum / target Android | API 26 / API 36 |
| Business model | Undecided; no purchase or premium implementation exists |

## Implemented scope

- Today dashboard with one priority goal, active-goal and log counts, empty state, and additional active goals
- Canonical Goals list with Active, Paused, Completed, and All filters; Recent, Name, and Progress sorting; quick log; edit; pause/resume; complete; and archive actions
- Goal creation and editing for count, duration, percentage, and binary measurements
- Goal detail with exact current/target values, accessible progress ring, recent chronological history, editing, and quick logging
- Metric-aware progress sheet with shortcuts, validation, optional notes, and immediate Undo
- Exact Progress history and explicit confirmation before destructive deletion
- Navigation 3 multiple top-level back stacks, with focused create/detail flows hiding top-level navigation
- Edge-to-edge layouts, keyboard-safe form scrolling, portrait/landscape state restoration, light/dark theme foundations, and adaptive navigation components
- Local Room database with exported version 1 schema; Hilt dependency injection; ViewModel/Flow state; no account, backend, analytics, ads, trackers, network permission, or sensitive permission

## Data and privacy

Goals and progress entries are stored locally in Room. Goal values use `Long`; progress is derived deterministically from exact entries. Deleting an entry recalculates the goal total. Android backup/data-extraction rules include the app database, but user-facing export/restore is not implemented yet.

The merged app currently declares no network, notification, location, camera, contacts, storage, or other sensitive permission. The only generated permission is the package-scoped `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` used by AndroidX internals.

## Architecture

The implementation uses Kotlin, Jetpack Compose, Material 3 foundations, Navigation 3, unidirectional state flow, coroutines/Flow, Hilt, and Room. Details and dependency rationale are in `docs/ARCHITECTURE.md`; test coverage and commands are in `docs/TESTING.md`.

## Verification evidence

Verified on 19 November 2026 using the `NoDoomPolicyPixel9Pro` Android 16/API 36 AVD at 1280 × 2856, 480 dpi. The same verified debug APK was subsequently installed as a fresh package on the connected physical Pixel 9 Pro using data-preserving `adb install -r` and launched successfully.

- `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease :app:bundleRelease` — passed
- `./gradlew :app:connectedDebugAndroidTest` — 3/3 tests passed on the AVD
- Debug installation used `adb install -r`; no package data was deliberately cleared or app uninstalled
- Physical Pixel installation verified package `com.chandsharma.kajugoals.debug`, version code 1 / `0.1.0-dev-debug`, with the app activity in focus after launch
- Interactively verified empty Today, create/edit form and IME behavior, populated Today, Goals list, count and binary logging, undo feedback, exact detail/history, Progress history, deletion confirmation, top-level back stacks, and rotation
- Verified navigation regression: a completed create flow no longer reappears as a stale form when returning to its origin tab
- `zipalign -c -P 16 -v 4` passed for the unsigned release APK
- Debug APK signature verifies with one signer and APK Signature Scheme v2
- Release APK is intentionally unsigned; no release signing identity was created or used
- Debug APK SHA-256: `782a3ea4ca029b839df73e8721b56f3c658c456247ff1d5b4383484cb784b6b5`
- Unsigned release APK SHA-256: `fd483f9bce910e5fedb463758f5c4c47e0692c4a7fc6f056f2b7139bdf625016`
- Unpublished release AAB SHA-256: `84f79c2ffcb6d7e2a4765c4b2c4b85ecf8596969bfce2bafc0a0608df255950a`

Rendered development evidence using fictional data is under `docs/screenshots/development/`.

## Known limitations before production readiness

1. User-facing export/restore, backup verification, and migration tests beyond the initial version 1 schema are not implemented.
2. Older progress records can be deleted with confirmation but not edited in place yet.
3. Dates, milestones, reminders, recurrence/rituals, widgets, attachments, and richer progress analysis are not in the first slice.
4. Large datasets, 1.5× font scale, TalkBack, dark theme, tablet/foldable screenshots, performance, and long-duration reliability still need dedicated validation.
5. Automated screenshot regression is deferred because the stable AGP 9.0 toolchain only supports the deprecated standalone preview-screenshot plugin; adopting AGP 9.5+ and API 37 is a separate toolchain decision.
6. Release signing, Play disclosures, store assets, closed testing, and publishing have not begun.

## Open product decisions

1. Broader launch audience and public positioning.
2. Free/open-source versus optional paid sustainability model and what, if anything, may be premium.
3. Exact export format and whether end-to-end encrypted sync is ever needed.
4. Priorities for the next slice: edit historical entries, deadlines/milestones, reminders, recurring habits, or richer review/insights.

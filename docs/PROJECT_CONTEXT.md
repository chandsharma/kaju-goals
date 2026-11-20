# Project context

Last updated: 20 November 2026 (Asia/Kolkata)

## Current state

Kaju Goals now has a corrected, working local-first Android vertical slice with optional user-owned Google Drive sync. It is an early development build, not a Play release and not yet production-complete. The dominant loop works on the Pixel 9 Pro API 36 AVD: create a complete goal from a one-tap template or a minimal icon-led form, scan exact progress in a compact list, log a metric-aware increment in two actions, undo it, inspect history, edit, filter/sort, and complete a goal.

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
- One-tap quick starts plus icon-led goal creation/editing for Count, Time, Percentage, Money, and Done; automatic targets for Percentage and Done; common Count units; INR, USD, EUR, and GBP
- Goal detail with exact current/target values, accessible progress ring, recent chronological history, editing, and quick logging
- Metric-aware large −/+ progress stepper with shortcuts, collapsed optional note, exact confirmation copy, and immediate Undo
- Exact Progress history and explicit confirmation before destructive deletion
- Navigation 3 multiple top-level back stacks, with focused create/detail flows hiding top-level navigation
- Edge-to-edge layouts, keyboard-safe form scrolling, portrait/landscape state restoration, light/dark theme foundations, and adaptive navigation components
- Compact goal rows with metric symbols, exact current/target text, visible progress, one-tap `+`, and overflow actions
- Fast 160/90 ms content transitions replacing Navigation 3's 700 ms default
- Local Room database with explicit v1→v2 migration and exported schemas; Hilt; ViewModel/Flow state
- Optional Google Drive hidden app-data sync using only short-lived `drive.appdata` authorization; WorkManager retries changes when connected; sync can be turned off without deleting either copy; no Kaju account/backend, analytics, ads, or trackers

## Data and privacy

Goals and progress entries are stored locally in Room and remain fully usable offline. Goal values use `Long`; currency uses integer minor units; progress is derived deterministically from exact entries. Deleting an entry creates a sync tombstone and recalculates the visible total.

If the user explicitly connects Google Drive, the app stores one checksummed JSON snapshot in that account's hidden `appDataFolder`. It requests only the non-sensitive `drive.appdata` OAuth scope, cannot browse ordinary Drive files, stores no OAuth access or refresh token, and sends no goal data to a Kaju-controlled service. Every sync downloads and merges before upload; an empty fresh install cannot overwrite a nonempty cloud snapshot. Details are in `docs/SYNC_ARCHITECTURE.md`.

The app now declares Internet and network-state permissions solely for optional Drive sync. WorkManager contributes wake-lock, boot-completed, and foreground-service system permissions for durable scheduled work. The app declares no notification, location, camera, contacts, broad storage, or other sensitive runtime permission.

## Architecture

The implementation uses Kotlin, Jetpack Compose, Material 3 foundations, Navigation 3, unidirectional state flow, coroutines/Flow, Hilt, Room, WorkManager, Google AuthorizationClient, and the Drive REST API. Details and dependency rationale are in `docs/ARCHITECTURE.md`; test coverage and commands are in `docs/TESTING.md`.

## Verification evidence

Originally verified on 19 November 2026 using the `NoDoomPolicyPixel9Pro` Android 16/API 36 AVD at 1280 × 2856, 480 dpi and installed on the connected physical Pixel 9 Pro. The corrected create/log/list/sync slice was re-verified on the same AVD; physical Pixel installation of the final corrected APK is pending the device reconnecting to ADB.

- `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease :app:bundleRelease` — passed
- `./gradlew :app:connectedDebugAndroidTest` — 7/7 corrected-slice tests passed on the AVD, including the v1→v2 migration and quick-start/currency behavior
- Local tests include deterministic sync merge/restore/tombstone coverage and integer INR formatting
- Debug installation used `adb install -r`; no package data was deliberately cleared or app uninstalled
- Physical Pixel installation verified package `com.chandsharma.kajugoals.debug`, version code 1 / `0.1.0-dev-debug`, with the app activity in focus after launch
- Interactively verified empty Today, create/edit form and IME behavior, populated Today, Goals list, count and binary logging, undo feedback, exact detail/history, Progress history, deletion confirmation, top-level back stacks, and rotation
- Corrected slice interactively verified: template-only creation of `Read 12 books`, large quick-log stepper at `+1 book`, singular/plural values, compact Goals row with exact 1/12 state, immediate Undo, and the optional/off Drive privacy state
- Verified navigation regression: a completed create flow no longer reappears as a stale form when returning to its origin tab
- `zipalign -c -P 16 -v 4` passed for the unsigned release APK
- Debug APK signature verifies with one signer and APK Signature Scheme v2
- Release APK is intentionally unsigned; no release signing identity was created or used
- Debug APK SHA-256: `d560fa2f70e37398c95ca9b9ac7dc6117496f2a4a1b2888ebcd31adb761d2f55`
- Unsigned release APK SHA-256: `4a14e8eed916c7a544b0838b949a50fa1238f2d3ab34ea195de44f0052096cea`
- Unsigned/unpublished release AAB SHA-256: `0b6a1a29fee2fde759c73ac67d84b541e4c2d6151db9a4a5c1ea2462afade811`

Rendered development evidence using fictional data is under `docs/screenshots/development/`.

## Known limitations before production readiness

1. Google Drive OAuth and merge logic are implemented, but the first real-account consent/upload/restore journey still needs completion on the connected physical Pixel. User-facing portable file export is not implemented.
2. Older progress records can be deleted with confirmation but not edited in place yet.
3. Dates, milestones, reminders, recurrence/rituals, widgets, attachments, and richer progress analysis are not in the first slice.
4. Large datasets, 1.5× font scale, TalkBack, dark theme, tablet/foldable screenshots, performance, and long-duration reliability still need dedicated validation.
5. Automated screenshot regression is deferred because the stable AGP 9.0 toolchain only supports the deprecated standalone preview-screenshot plugin; adopting AGP 9.5+ and API 37 is a separate toolchain decision.
6. Release signing, Play disclosures, store assets, closed testing, and publishing have not begun. The production OAuth Android client must later use the base application ID and Play App Signing SHA-1.

## Open product decisions

1. Broader launch audience and public positioning.
2. Free/open-source versus optional paid sustainability model and what, if anything, may be premium.
3. Whether a separate portable export and optional end-to-end encryption should complement the owner-controlled Drive snapshot.
4. Priorities for the next slice: edit historical entries, deadlines/milestones, reminders, recurring habits, or richer review/insights.

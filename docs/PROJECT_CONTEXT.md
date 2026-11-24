# Project context

Last updated: 24 November 2026 (Asia/Kolkata)

## Current state

Kaju Goals now has a substantial local-first Android development build with optional user-owned Google Drive sync. It is not a Play release and is not yet production-complete. The dominant loop works on the Pixel 9 Pro API 36 AVD: type an ordinary sentence or use a quick start, review an editable inferred measurement, create the goal, log or correct progress in two actions, backdate entries, inspect exact history and a weekly review, and organize growing lists with search, pinning, categories, duplication, and bulk archive.

The public repository is `https://github.com/chandsharma/kaju-goals`. Play Console publication is explicitly deferred until the user confirms. The current debug build is installed on the connected physical Pixel with existing app data preserved.

## Product identity

| Item | Decision |
| --- | --- |
| Product name | Kaju Goals |
| Application ID | `com.chandsharma.kajugoals` |
| Debug application ID | `com.chandsharma.kajugoals.debug` |
| Version | `2` / `0.2.0-dev` |
| Primary user | The owner as the first real user; broader launch audience remains open |
| Primary promise | Make personal goals and progress effortless to track |
| Primary destinations | Today, Goals, Progress, You |
| Minimum / target Android | API 26 / API 36 |
| Business model | Undecided; no purchase or premium implementation exists |

## Implemented scope

- Today dashboard with one priority goal, active-goal and log counts, empty state, additional active goals, exact progress, and immediate logging
- Canonical Goals list with search; Active, Paused, Completed, Archived, and All filters; sorting; pinning; optional categories; quick log; edit; pause/resume; complete; restore/archive; duplicate; selection; and bulk archive
- Sentence-first creation that locally infers Count, Time, Percentage, Money, or Done plus target, currency, and unit from titles such as `Read 12 books`, `Save ₹1 lakh`, and `Exercise 30 hours`; the visible inference remains editable and manual changes win
- One-tap quick starts; automatic targets for Percentage and Done; common Count units; Indian-number multipliers; INR, USD, EUR, and GBP; progressive disclosure for measurement and optional details
- Optional deadlines with calm pace status and remaining-per-week guidance; daily, weekly, and monthly recurring periods without punitive streaks
- Goal detail with exact current/target values, accessible progress ring, automatic 25/50/75/100 milestones, pace, chronological history, editing, and quick logging
- Metric-aware large −/+ progress stepper with shortcuts, visible primary action, backdating, collapsed optional note, Add/Subtract/Set-total modes, exact confirmation copy, and immediate Undo
- Exact Progress history with in-place entry editing, deletion confirmation, and an exact seven-day weekly review
- Navigation 3 multiple top-level back stacks, with focused create/detail flows hiding top-level navigation
- Edge-to-edge layouts, keyboard-safe form scrolling, portrait/landscape state restoration, light/dark theme foundations, and adaptive navigation components
- Compact goal rows with metric symbols, exact current/target text, visible progress, one-tap `+`, and overflow actions
- Fast 160/90 ms content transitions replacing Navigation 3's 700 ms default
- Local Room database version 3 with explicit v1→v2→v3 migrations and exported schemas; Hilt; ViewModel/Flow state
- Optional deadline reminders through inexact WorkManager jobs; notification permission is requested only after the user chooses a reminder
- JSON full backup/merge restore and CSV export through Android's document picker
- Optional biometric or device-credential app lock, stored only on the device
- Glance home-screen widget showing the next active goal and progress and opening Kaju Goals
- Optional Google Drive hidden app-data sync using only short-lived `drive.appdata` authorization; WorkManager retries changes when connected; sync can be turned off without deleting either copy; no Kaju account/backend, analytics, ads, or trackers

## Data and privacy

Goals and progress entries are stored locally in Room and remain fully usable offline. Goal values use `Long`; currency uses integer minor units; progress is derived deterministically from exact entries. Deleting an entry creates a sync tombstone and recalculates the visible total.

If the user explicitly connects Google Drive, the app stores one checksummed JSON snapshot in that account's hidden `appDataFolder`. It requests only the non-sensitive `drive.appdata` OAuth scope, cannot browse ordinary Drive files, stores no OAuth access or refresh token, and sends no goal data to a Kaju-controlled service. Every sync downloads and merges before upload; an empty fresh install cannot overwrite a nonempty cloud snapshot. Details are in `docs/SYNC_ARCHITECTURE.md`.

The app declares Internet and network-state permissions solely for optional Drive sync. WorkManager contributes wake-lock, boot-completed, and foreground-service system permissions for durable scheduled work. Biometric APIs contribute biometric/fingerprint declarations. Android 13+ notification permission is requested only if the user enables a reminder. The app declares no location, camera, contacts, broad storage, or exact-alarm permission.

## Architecture

The implementation uses Kotlin, Jetpack Compose, Material 3 foundations, Navigation 3, unidirectional state flow, coroutines/Flow, Hilt, Room, WorkManager, Google AuthorizationClient, and the Drive REST API. Details and dependency rationale are in `docs/ARCHITECTURE.md`; test coverage and commands are in `docs/TESTING.md`.

## Verification evidence

Verified on 24 November 2026 using the `NoDoomPolicyPixel9Pro` Android 16/API 36 AVD at 1280 × 2856, 480 dpi and installed on the USB-connected physical Pixel 9 Pro without clearing or uninstalling its existing package data.

- `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease :app:bundleRelease :app:connectedDebugAndroidTest` — passed
- `./gradlew :app:connectedDebugAndroidTest` — 12/12 tests passed on the AVD, including v1→v2→v3 migrations, natural-title inference, manual inference override, correction editing, quick-start/currency behavior, and weekly progress behavior
- Local tests include deterministic sync merge/restore/tombstone coverage, legacy Drive schema decoding, Indian-number inference, pace calculations, recurrence period boundaries, and integer INR formatting
- Debug installation used `adb install -r`; no package data was deliberately cleared or app uninstalled
- Physical Pixel installation upgraded package `com.chandsharma.kajugoals.debug` from version code 1 to version code 2 / `0.2.0-dev-debug`; `MainActivity` was running after launch and `adb install -r` preserved the package rather than uninstalling or clearing it
- Interactively verified empty Today, create/edit form and IME behavior, populated Today, Goals list, count and binary logging, undo feedback, exact detail/history, Progress history, deletion confirmation, top-level back stacks, and rotation
- Interactively verified sentence-first and template creation, editable inference, immediately visible log confirmation, Add/Subtract/Set-total correction modes, backdate affordance, exact 1/12 state, weekly review, progress entry edit/delete controls, search/list organization, portable-data controls, and the optional/off Drive privacy state
- Visually inspected light and dark Today, 130% font scaling, portrait and landscape, focused creation, goal detail, quick log, Goals list, Progress review, and You/data controls on the AVD
- Verified navigation regression: a completed create flow no longer reappears as a stale form when returning to its origin tab
- `zipalign -c -P 16 -v 4` passed for the unsigned release APK
- Debug APK signature verifies with one signer and APK Signature Scheme v2
- Release APK is intentionally unsigned; no release signing identity was created or used
- Debug APK SHA-256: `0ee69a26952ce6ee2f1a0d3be1389c1367e18555d92f952125927a6145d036ef`
- Unsigned release APK SHA-256: `fb3fd4aea60d27083aa53b041ce8114a5f50db89109857a698b66d3e0e7b8cd7`
- Unsigned/unpublished release AAB SHA-256: `58c1dd20f74c0f5a4322525c9f8a785802429607ab973c6a810ed0d402313589`

Rendered development evidence using fictional data is under `docs/screenshots/development/`.

## Known limitations before production readiness

1. Google Drive OAuth and merge logic are implemented, but the first real-account upload/fresh-install restore journey still needs completion by the user on the connected physical Pixel. While the OAuth consent screen remains in Testing, each Google account must be listed as a test user; the app itself does not hard-code an account.
2. The widget is intentionally glanceable and opens the app; a direct widget log action remains future work.
3. Attachments remain deferred until synced-byte storage, deletion, quotas, recovery, and privacy semantics are explicitly designed and tested.
4. Large datasets, 1.5× font scale, TalkBack traversal, tablet/foldable screenshots, performance, and long-duration reminder/sync reliability still need dedicated validation.
5. Automated screenshot regression is deferred because the stable AGP 9.0 toolchain only supports the deprecated standalone preview-screenshot plugin; adopting AGP 9.5+ and API 37 is a separate toolchain decision.
6. Release signing, Play disclosures, store assets, closed testing, and publishing have not begun. The production OAuth Android client must later use the base application ID and Play App Signing SHA-1.

## Open product decisions

1. Broader launch audience and public positioning.
2. Free/open-source versus optional paid sustainability model and what, if anything, may be premium.
3. Whether optional end-to-end encryption should complement the owner-controlled Drive snapshot and portable JSON backup.
4. The exact attachment and direct widget-action design.

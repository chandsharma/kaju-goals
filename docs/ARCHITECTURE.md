# Architecture

Last updated: 20 November 2026

## Shape

Kaju Goals is a single-module, local-first Android app with optional user-owned Drive sync. It has no Kaju backend or account, analytics, ads, or broad platform permissions.

```text
Compose screens
    ↓ actions / StateFlow state
MainViewModel
    ↓ domain repository contract
RoomGoalRepository
    ↓
Room DAO → goals + progress_entries
    ↕ merge-first snapshots
WorkManager → Google AuthorizationClient → Drive appDataFolder
```

Business rules and persistence do not live in composables. Screens receive immutable state and callbacks; `MainViewModel` combines repository flows with list filter/sort state and emits one-off UI events for save, log, undo, and errors.

## Navigation and adaptation

- Navigation 3 uses serializable `NavKey` routes.
- Today, Goals, Progress, and You each own a saveable back stack.
- Selecting the active top-level destination returns it to its root.
- Create/edit and detail routes hide the navigation suite to preserve focus and vertical space, then restore it on return.
- A successfully saved create route is removed from its origin stack before navigating to the new goal. This prevents a completed form from resurfacing later.
- `NavigationSuiteScaffold` provides adaptive top-level navigation; individual screens own their safe-area padding because the adaptive scaffold does not propagate insets.
- `enableEdgeToEdge()` is called before content; `adjustResize` is declared; scrolling content receives safe/system padding through `contentPadding`; form and quick-log inputs remain reachable above the IME.

## Domain model

`Goal` has an ID, title, measurement type, target, exact current value, unit, status, timestamps, and optional last-progress timestamp. Supported measurements are Count, Time in minutes, Percentage, Currency, and Binary/Done. Currency values are authoritative integer minor units in `Long`; display formatting uses the saved ISO currency code.

`ProgressRecord` is the authoritative event: ID, goal ID/title, metric/unit snapshot, integer delta, optional note, occurrence time, and creation time. Goal progress is the sum of its entries. UI percentages are derived and clamped for display; exact stored values remain integer `Long`s.

Goal statuses are Active, Paused, Completed, and Archived. A goal is displayed as complete when its stored status is Completed or its exact progress reaches its target.

## Persistence

Room database version 2 contains:

- `goals`: durable goal definition, status, order, timestamps, deterministic mutation ID, and optional deletion tombstone
- `progress_entries`: exact deltas and notes, indexed by goal and occurrence time, with updated/mutation timestamps and optional deletion tombstone

The exported schemas and explicit v1→v2 migration are committed under `app/schemas/`; migration preservation is tested on Android SQLite. Destructive fallback is not configured.

## Optional Drive sync

Google Drive sync is an adapter around the local source of truth, not a prerequisite for app use. AuthorizationClient requests only `drive.appdata` and returns a short-lived token when the app needs to call the Drive REST API. The app stores no token and does not request account identity.

Every sync reads all matching hidden snapshots, verifies schema/checksum, merges stable IDs by `(updatedAt, mutationId)`, applies the merge locally, then conditionally updates or creates the canonical snapshot. WorkManager schedules network-constrained sync after mutations and on app initialization when connected. HTTP 412 conflicts repeat the full download/merge/write cycle. See `docs/SYNC_ARCHITECTURE.md`.

## Dependency injection

Hilt constructs Room, the DAO/repository, clock/UUID generator, Drive authorization/API/engine/state, and WorkManager scheduler. Tests use an in-memory Room database, migration helper, pure sync merger, or fake repository as appropriate.

## Toolchain choices

| Component | Version | Reason |
| --- | --- | --- |
| Android Gradle Plugin | 9.0.1 | Stable API 36-compatible baseline available in the official Android CLI template |
| Gradle | 9.1.0 | Compatible with AGP 9.0.1 |
| Kotlin / Compose compiler plugin | 2.3.20 | Compatible stable pair for the selected toolchain |
| Compose BOM | 2026.03.01 | Latest verified BOM that builds with installed API 36; 2026.09 requires API 37 / newer AGP |
| Navigation 3 | 1.2.1 | Available stable artifact; 1.2.2 was indexed in release metadata but absent from Google Maven during setup |
| Room | 2.8.5 | Current stable local persistence baseline |
| Hilt | 2.60.1 | Current verified stable dependency-injection baseline |
| WorkManager | 2.12.0 | Durable network-constrained sync scheduling |
| Google Play services auth | 22.0.0 | Current AuthorizationClient for short-lived user-granted Drive access |
| Kotlin serialization | 1.11.0 | Versioned deterministic JSON snapshot encoding |

Version warnings from lint are intentional compatibility pins, not accidental neglect. Upgrade them together only after API 37 and a matching stable AGP/Gradle/Kotlin stack are present and verified.

## Privacy and security boundary

- Internet/network-state access is used only when optional Drive sync is connected
- No analytics, ads, telemetry, trackers, or crash reporting
- No sensitive runtime permission
- No secrets or signing files in source
- Release artifacts remain unsigned until the explicit release workflow
- Progress deletion requires confirmation; newly logged progress provides immediate Undo

## Next architecture work

- Add editable historical progress while preserving auditability.
- Complete a real-account Drive upload/restore/conflict journey and add dedicated disposable-account integration coverage before public release.
- Define a separate portable file export format before public release.
- Add stable screenshot regression when the toolchain can use AGP test suites without a deprecated plugin.
- Consider list-detail Navigation 3 scenes for tablet/foldable layouts after the phone workflow is stable.

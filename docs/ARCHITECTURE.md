# Architecture

Last updated: 19 November 2026

## Shape

Kaju Goals is a single-module, local-first Android app. The first slice deliberately avoids a backend, accounts, analytics, ads, and broad platform permissions.

```text
Compose screens
    ↓ actions / StateFlow state
MainViewModel
    ↓ domain repository contract
RoomGoalRepository
    ↓
Room DAO → goals + progress_entries
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

`Goal` has an ID, title, measurement type, target, exact current value, unit, status, timestamps, and optional last-progress timestamp. Supported measurements are count, duration in minutes, percentage, and binary.

`ProgressRecord` is the authoritative event: ID, goal ID/title, metric/unit snapshot, integer delta, optional note, occurrence time, and creation time. Goal progress is the sum of its entries. UI percentages are derived and clamped for display; exact stored values remain integer `Long`s.

Goal statuses are Active, Paused, Completed, and Archived. A goal is displayed as complete when its stored status is Completed or its exact progress reaches its target.

## Persistence

Room database version 1 contains:

- `goals`: durable goal definition, status, order, and timestamps
- `progress_entries`: exact deltas and notes, indexed by goal and occurrence time, with a cascading foreign key to the owning goal

The exported schema is committed under `app/schemas/`. Future schema changes require explicit migrations and migration tests. Destructive fallback is not configured.

## Dependency injection

Hilt constructs the Room database, DAO, repository, clock, and UUID generator. Tests use an in-memory Room database or a fake repository as appropriate.

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

Version warnings from lint are intentional compatibility pins, not accidental neglect. Upgrade them together only after API 37 and a matching stable AGP/Gradle/Kotlin stack are present and verified.

## Privacy and security boundary

- No network stack or Internet permission
- No analytics, ads, telemetry, trackers, or crash reporting
- No sensitive runtime permission
- No secrets or signing files in source
- Release artifacts remain unsigned until the explicit release workflow
- Progress deletion requires confirmation; newly logged progress provides immediate Undo

## Next architecture work

- Add editable historical progress while preserving auditability.
- Define, implement, and test an export/restore format before public release.
- Add Room migration tests when schema version 2 is introduced.
- Add stable screenshot regression when the toolchain can use AGP test suites without a deprecated plugin.
- Consider list-detail Navigation 3 scenes for tablet/foldable layouts after the phone workflow is stable.

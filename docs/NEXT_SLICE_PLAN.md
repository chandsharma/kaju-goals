# Effortless tracking roadmap

Last updated: 24 November 2026 (Asia/Kolkata)

## Outcome

Kaju Goals should understand an ordinary sentence, create a correctly measured goal with almost no setup, and make every logged value reversible and explainable. The interface should feel calm and premium, but speed, legibility, and data safety take precedence over decoration.

## Interaction contract

### Sentence-first creation

- The first field is an ordinary sentence such as `Read 12 books`, `Save ₹1 lakh`, or `Exercise 30 hours`.
- A local deterministic parser suggests measurement, target, currency, and unit while the user types. No goal text is sent to a server or AI service.
- Suggestions appear as compact editable controls. The app never hides what it inferred.
- A manual change wins. Later title edits must not silently overwrite a measurement, unit, or target that the user deliberately changed.
- A visible `Apply suggestion` action lets the user opt back into a newer interpretation.
- Percentage and Done remain one-tap measurements with automatic finish lines.

### Correctable progress

- New progress may be logged for now or a selected past date.
- A history entry can be edited or deleted with confirmation.
- A correction may add or subtract, and `Set total` records the exact delta needed to reach a chosen total instead of replacing history.
- Every correction retains a stable record ID, update timestamp, and mutation ID so Drive conflict resolution remains deterministic.

### Time and pace

- A goal may have an optional deadline.
- Goal surfaces show days remaining and a plain-language pace: complete, on pace, behind pace, or no deadline.
- When meaningful, the app shows the remaining amount per week. Pace is guidance, not a shame-based streak.

## Delivery sequence

### Slice A — effortless core (implemented in 0.2.0-dev)

1. Smart local title inference with unit and Indian-number support.
2. Compact progressive-disclosure creation with editable suggestions and optional deadline/category.
3. Room v2→v3 migration and backward-compatible Drive fields for deadline, category, and pinning.
4. Edit, backdate, subtract, set-total, and delete progress.
5. Search, pinning, categories, duplicate, and bulk archive foundations.
6. Pace calculations and deadline presentation.
7. Consistent metric symbols, semantic color, tighter hierarchy, faster completion feedback, and haptics where useful.

### Slice B — helpful automation (implemented in 0.2.0-dev)

1. Optional reminders implemented with inexact scheduled work; notification permission is requested only when a reminder is enabled.
2. Automatic 25/50/75/100 milestones and a completion moment. User-defined milestones remain a later refinement.
3. Weekly review summarizing real totals, pace changes, and neglected goals.
4. Recurring goals with flexible periods and no punitive streak model.

### Slice C — portability and glanceability (partially implemented in 0.2.0-dev)

1. Home-screen widget for the next active goal. It currently opens the app; direct widget logging remains future work.
2. JSON backup/restore plus CSV export through Android’s document picker.
3. Optional biometric/device-credential app lock.
4. Optional photo/file attachments remain deferred until an explicit cross-device byte-storage and recovery design exists.

## Safety and release gates

- Existing local data is migrated in place; development updates use `adb install -r`.
- Sync always downloads, verifies, and merges before uploading. New fields have safe defaults so an older Drive snapshot remains readable.
- An empty installation must restore remote data before any cloud write.
- Reminders will not require exact-alarm access.
- Attachments are not implemented or advertised as synced until their bytes, deletion behavior, quotas, and recovery semantics are defined and tested.
- No Play Console publication occurs without explicit user confirmation.

## Verification

- Pure unit tests cover inference, Indian number multipliers, money minor units, and pace calculations.
- Room migration tests cover v1→v2→v3 and preserve existing records.
- Repository tests cover negative corrections, edits, backdating, and set-total math.
- Compose tests cover suggestion application/manual override and progress editing.
- Connected tests and visual inspection cover the Pixel 9 Pro AVD, the physical Pixel, rotation, keyboard/insets, dark theme, large font, and a large-window layout before the slice is called complete.

# Tracking usability requirements

Last updated: 19 November 2026 (Asia/Kolkata)
Status: primary usability contract approved; detailed interaction design pending

## Primary use

The first real user will mainly maintain personal goals and record progress. The application succeeds when opening it, finding the right goal, logging what changed, and confirming the new state feels obvious and fast.

Visual distinctiveness supports this job; it never outranks it.

## Primary navigation

Use four persistent, labeled destinations:

1. **Today** — current priorities and quick logging.
2. **Goals** — the canonical list of goals and their states.
3. **Progress** — chronological history, summaries, charts, and weekly review.
4. **You** — preferences, privacy, export/restore, appearance, and account or plan if one later exists.

Do not hide a primary destination behind an icon-only control, horizontal cover interaction, or undisclosed gesture.

## Goals list

The list is a core working surface, not a fallback behind the visual dashboard.

Each visible row must expose:

- goal name;
- current value and target where applicable;
- explicit status;
- compact progress representation with a textual equivalent;
- next milestone or relevant date when present;
- a labeled or unambiguous quick-log action;
- an overflow menu for edit, pause/resume, archive, and delete.

Required list behavior:

- filters for Active, Paused, Completed, and All;
- explicit sorting with a predictable default;
- stable row positions during ordinary logging;
- useful empty and no-results states;
- support for long names, large text, many goals, and zero/complete/over-target values;
- search when dataset size justifies it, without making search the only way to navigate.

Cards may be used for comfortable mode, but the information structure must still scan like a list. Dense decoration must not reduce the number of useful visible rows excessively.

## Quick progress logging

Once a goal is visible on Today, Goals, or Goal detail, the common logging case must complete in no more than two deliberate actions.

The log surface adapts to the metric:

- binary: mark complete or undo;
- count/amount/currency/duration: numeric entry plus common increment shortcuts;
- percentage: direct value or increment, with bounds and over-target policy made explicit;
- milestone: complete the milestone and show the resulting next state;
- note/photo evidence: optional, never required for the ordinary case.

Logging requirements:

- preserve the entered timestamp while allowing an explicit change;
- display the unit beside the value;
- validate before saving and explain invalid input in place;
- update list, detail, chart, pace, and widget data from the same authoritative record;
- provide an immediate undo action;
- allow explicit edit and delete from chronological history;
- prevent accidental double submission;
- work offline without losing the event.

## Goal detail

Goal detail presents, in this order:

1. name, status, current value, and target;
2. primary Log progress action;
3. next milestone or next move;
4. chronological recent history;
5. progress visualization and pace interpretation;
6. supporting notes, rituals, attachments, and settings.

Charts must never be the only way to retrieve an exact value or understand history.

## Creation and editing

- Provide a minimal create path: name, measurement type, and optional target are sufficient to save a valid draft goal.
- Dates, milestones, reminders, rituals, notes, and appearance are progressive options.
- Explain measurement choices using examples instead of domain terminology.
- Preserve entered data when validation fails or the app is backgrounded.
- Warn before a measurement-type change would reinterpret or invalidate history.

## Interaction and accessibility acceptance criteria

- Primary actions use text labels, not icon recognition alone.
- Touch targets are at least 48dp.
- Normal text meets at least 4.5:1 contrast; meaningful non-text elements meet at least 3:1.
- Every progress graphic has an equivalent accessible label and exact value.
- TalkBack reading and focus order match the visual task order.
- Large text does not hide Log progress, current value, target, or navigation.
- Reduced-motion mode preserves state changes without spatial flourish.
- Loading, empty, populated, offline, validation, permission-denied, error, and recovery states are designed and tested.

## Validation plan

Before treating the interaction design as complete, test these tasks on the documented Pixel 9 Pro AVD and connected physical Pixel when available:

1. Create a simple numeric goal without help.
2. Find one goal among at least 25 realistic goals.
3. Log progress from Today, Goals, and Goal detail.
4. Undo a mistaken log, then edit an older entry.
5. Pause and resume a goal.
6. Inspect exact history without interpreting a chart.
7. Complete and archive a goal, then find it again.
8. Repeat the core tasks with large text and TalkBack.

Usability evidence should include task completion, error points, number of actions, screenshots or recordings, and accessibility observations—not aesthetic judgment alone.

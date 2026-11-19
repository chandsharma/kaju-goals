# Product proposal: Morrow (working title)

Status: **Exploratory; no product decision is approved**
Prepared: 19 November 2026 (Asia/Kolkata)
Active concept candidate: [`docs/concepts/morrow-product-vision-v2.png`](concepts/morrow-product-vision-v2.png)
Superseded exploration: [`docs/concepts/morrow-product-vision-v1.png`](concepts/morrow-product-vision-v1.png)

## Executive recommendation

Build an Android-first goal operating system for people who have meaningful long-term aims but struggle to translate them into a realistic day.

The product promise is:

> Turn what matters into one clear next move today.

The product should not become a task manager with inspirational decoration, a punishing streak tracker, or a chatbot wrapped around a database. Its core loop should connect a measurable outcome to milestones, a single next move, supporting rituals, frequent progress evidence, and a humane weekly review.

`Morrow` is only a working name used to make the concept concrete. It has not been checked for trademark, Play listing, domain, or package-name availability.

## Evidence collected

### Installed Goals app audit

The installed app was inspected on the connected physical Pixel 9 Pro without clearing data, uninstalling it, starting a purchase, or editing user records.

Technical facts:

- Package: `com.mindfulsuite.goals`
- Display name: Goals
- Version: 10.3.13 (`versionCode` 337)
- Runtime: Flutter
- Minimum SDK: 24
- Target/compile SDK: 36
- Main navigation observed: dashboard, coach, learning library, profile/settings, and journey history
- Supporting systems found in packaged resources: Firebase/Firestore/Storage, billing, Android widgets, camera and audio flows, notifications, biometric app lock, multiple languages, app icons, badges, sounds, goal templates, and a REST/API connector surface

Observed experience:

- The opening moment is an immersive horizontal set of full-screen cards: daily greeting, a long-term direction, and a mindset.
- Tapping the cover reveals the functional dashboard with habits, categorized goals, progress cards, and five bottom destinations.
- The coach offers goal/habit prompts and an educational practice library.
- The library contains short learning modules inspired by well-known behavior and productivity methods.
- Journey history presents kept days as a decorative star-field calendar.
- The app uses mostly near-black surfaces, large white type, teal controls, orange habit accents, purple journey screens, gradient goal cards, full-bleed photography, and occasional serif/handwritten display type.

Observed paid boundaries:

- Unlimited goals and habits
- Inspiration and reward tracking
- Custom photo backgrounds and colors
- Home-screen widgets and analytics
- Full journey history
- Additional customization such as goal colors and greeting backgrounds
- AI coaching sold separately in Basic and Premium message tiers; coach plans include the unlocked app
- Annual, monthly, lifetime, and coach-oriented plan language exists in the product resources

What works:

- Emotionally strong opening and rich motivational identity
- Multiple goal measurement types, milestones, supporting habits, reminders, rewards, and progress logging
- Habit anchors, immediate celebrations, skip/rest behavior, photo check-ins, and useful analytics
- Templates that create a goal, habits, milestones, and a supporting mindset together
- Widgets, badges, sound/haptics, archives, app lock, translations, and API access show product depth

What should be improved in an original product:

- The beautiful cover and the functional dashboard feel like two different products.
- Tapping the full-screen cover to reveal the dashboard is not discoverable.
- Five icon-only destinations increase interpretation cost.
- Goal cards, habit rings, the coach, educational content, star calendar, gradients, and photography compete for the visual center.
- The initial paywall is visually dominant before the product demonstrates value.
- The product is feature-rich but does not make a single next action the obvious answer to “what should I do now?”
- Some gray text and icon treatments are visually subdued enough to warrant contrast testing.

This proposal uses the audit as category evidence only. It does not reuse the installed app's name, visual assets, copy, layouts, source, package identity, or private user data.

## Market scan

| Product | Strongest lesson | What not to inherit wholesale |
| --- | --- | --- |
| Todoist | Extremely fast capture, clear hierarchy, daily/weekly productivity goals, and vacation-safe streaks | It is fundamentally a task manager, not a long-horizon goal system |
| TickTick | Tasks, calendar, habits, focus timer, matrix, statistics, and many useful widgets in one mature system | Breadth produces density and can bury the primary job |
| Habitify | Flexible schedules, numeric habits, timers, health data, automation, integrations, and serious analytics | Data-first tracking can feel clinical and subscription-led |
| Daylio | A two-tap daily log, strong correlations, year-in-pixels, exports, app lock, and low-friction reflection | Mood/activity logging does not create a credible plan by itself |
| Finch | Emotional safety, gentle streak repair, visible rewards, delightful character growth, and a generous free core | Pet/game mechanics narrow the audience and can overpower real outcomes |
| Fabulous | Guided multi-week journeys, routines, lessons, coaching, and a coherent behavioral narrative | Long content sequences can feel prescriptive and slow to personalize |
| Structured | One visually legible day, a clear timeline, fast entry, calendar import, and strong cross-device aesthetics | A timeline answers when, but not necessarily why or whether a goal is working |
| Loop | Private, free, open-source habit tracking with reminders and honest charts | Its intentionally narrow scope does not address goal planning or review |

Sources:

- [Todoist productivity goals and streak controls](https://www.todoist.com/help/articles/use-the-productivity-view-in-todoist-6S63uAa9)
- [TickTick beginner guide](https://help.ticktick.com/articles/7054286604315131904) and [widget catalogue](https://help.ticktick.com/articles/7055780404896202752)
- [Habitify habit setup](https://habitify.me/onboarding-instruction/create-new-habit) and [plan comparison](https://habitify.me/pricing)
- [Finch product and free/Plus model](https://finchcare.com/about-finch)
- [Fabulous routines, journeys, challenges, coaching, and circles](https://help.thefabulous.co/en/support/solutions/articles/101000427430-how-does-fabulous-work-)
- [Daylio goals](https://daylio.net/faq/docs/daylio-faq/tutorials/setting-up-goals/) and [mood/activity insight model](https://daylio.net/faq/docs/daylio-faq/about/activity-and-mood-statistics/)
- [Structured visual timeline](https://structured.app/)
- [Loop Habit Tracker](https://loophabits.org/)

## Behavioral product principles

1. **Convert intention into a specific response.** A goal should resolve to an action attached to a situation, time, or context. Implementation-intention research supports explicitly planning when, where, and how action will happen.
2. **Make progress easy to record.** A meta-analysis found that increasing progress-monitoring frequency promotes goal attainment. Logging must take seconds, while notes and photos remain optional.
3. **Use stable cues for rituals.** Real-world habit research supports repeating a behavior in a consistent context rather than relying on motivation alone.
4. **Treat streaks as feedback, not moral judgment.** Streaks can motivate, but broken streaks can become a second failure. The product should support weekly rhythms, planned rest, recovery, and “resume” states.
5. **Show evidence, not vanity data.** Insights must answer an actionable question: Are you on pace? What conditions helped? What should change next week?

Research:

- [Implementation intentions and goal achievement](https://pure.manchester.ac.uk/ws/portalfiles/portal/220271792/FULL_TEXT.PDF)
- [Progress monitoring and goal attainment](https://pubmed.ncbi.nlm.nih.gov/26479070/)
- [Habit formation in the real world](https://storage.googleapis.com/mailerlite-uploads-prod/1500700/t8ntgevUqxD4dN75U5ZhvvpmnKsILFnMqTZX8LDM.pdf)
- [How broken streaks affect later decisions](https://academic.oup.com/jcr/article/49/6/1095/6623414)

## Product brief

### Primary audience

Initial assumption: ambitious but overloaded adults, roughly 22–45, who can name important outcomes but repeatedly lose them under daily work and life demands.

This audience must be narrowed before implementation. Professionals/founders, students, and broad personal-growth users require different defaults, language, templates, and pricing.

### Core jobs

1. Help me turn a meaningful ambition into an outcome I can recognize as complete.
2. Tell me the smallest credible move that advances it today.
3. Help me build repeatable behavior around the goal without guilt when life interrupts.
4. Show whether I am moving, why I am stuck, and what to adjust next.
5. Preserve my history and let me leave with my data.

### Product principles

- One next move is more valuable than ten charts.
- Outcomes, milestones, moves, and rituals are different concepts and should look different.
- The daily screen stays calm even when the underlying plan is deep.
- Recovery is a first-class state, not an error state.
- AI may suggest; the user decides.
- Core tracking works offline and without an account.
- No ads, dark patterns, forced trial, or selling behavior data.

## Information architecture

Use four labeled destinations:

1. **Today** — one meaningful move, due rituals, and a fast progress log
2. **Paths** — active goals, milestones, plan details, archive, and templates
3. **Review** — weekly reset, pace, patterns, and history
4. **You** — preferences, data/export, privacy, widgets, and plan

AI assistance, if approved, should be contextual within goal creation, a stuck-state, or weekly review. It should not occupy a permanent top-level tab.

## Core model

| Entity | Purpose |
| --- | --- |
| Area | Optional life context such as health, craft, or finances |
| Path | A desired outcome with proof, target, dates, and personal meaning |
| Metric | Count, amount, duration, currency minor units, percentage, or binary completion |
| Milestone | A meaningful checkpoint on the way to the outcome |
| Move | A one-time action; at most one is designated as the current next move |
| Ritual | A repeated behavior with a cue, rhythm, reminder, and planned-rest rules |
| Check-in | Immutable evidence of progress, completion, skip, note, or optional photo |
| Review | A weekly snapshot of outcomes, obstacles, decisions, and next commitments |
| Attachment | Optional local media with explicit retention and export behavior |

Money must use integer minor units. Dates, time zones, recurrence changes, archived items, deletion, exports, and migrations require explicit models and tests.

## Critical experience flows

### First-run value

1. Ask what the user wants to make true in the next 90 days.
2. Let them type freely or choose a lightweight example—not a large template catalogue.
3. Define “done” using an outcome and proof.
4. Add 2–5 milestones.
5. Choose the first move and, optionally, one supporting ritual.
6. Land on Today with something immediately actionable.
7. Ask for notification permission only if the user turns on a reminder.

Target: a useful first plan in under three minutes, skippable at every optional step.

### Daily loop

1. Today opens on the one move that matters.
2. `Log progress` accepts a quick amount/completion first.
3. Note and photo are optional secondary actions.
4. Completion produces restrained motion, a short haptic, and the next move—not confetti every time.
5. Rituals are compact and can be marked done, skipped, or moved without breaking identity.

### Weekly review

1. Show what moved and what did not, using neutral language.
2. Ask: What helped? What got in the way? What will change?
3. Recalculate pace and flag impossible targets honestly.
4. Let the user recommit, shrink, pause, or archive a Path.
5. Select next week's moves before leaving the review.

## Scope proposal

### Version 1: focused and release-worthy

- Offline-first goal, milestone, move, ritual, and check-in creation
- Numeric, duration, count, currency, percentage, and binary goal types
- Fast daily logging and editable history
- Flexible weekly rhythms, reminders, planned rest, skip, pause, and resume
- Weekly review and basic pace/progress charts
- Archive/restore and safe deletion
- Dark and light themes
- One useful Today widget and one Path progress widget
- Local JSON and CSV export plus recoverable backup/restore
- App lock using device authentication
- Accessible large text, TalkBack semantics, reduced motion, contrast, and 48dp targets
- Explicit offline, empty, loading, error, permission-denied, and recovery states

### Version 1.1 candidates

- Curated starting plans for a small number of well-researched outcomes
- Calendar/Health Connect integrations only after each permission and data-flow case is justified
- Tablet/foldable list-detail layouts
- Wear OS quick check-ins if demand supports it
- Additional widgets, icon themes, and home-screen customization
- Encrypted cross-device sync after account, deletion, conflict, and recovery design is complete

### Deliberate non-goals for initial release

- Social feed, public leaderboards, or accountability marketplace
- Generic task/project management
- Mandatory cloud account
- Ads, trackers, or behavioral-data resale
- Always-on AI coach
- Therapy, medical, financial, or diagnostic claims
- A giant content library before the core planning loop proves valuable

## Visual direction

Concept v1's warm editorial palette and serif-led hierarchy were rejected after user review. The active candidate is a **cool dimensional utility** system derived from the shared grammar—not the literal layouts—of five user-supplied references.

- Cool fog background, porcelain app surfaces, raised white cards, and graphite text/action surfaces
- One confident grotesk sans-serif family with large numeric and outcome hierarchy
- Oversized squircles, circular control wells, progress rings, compact bento modules, and disciplined negative space
- Desaturated glacier blue, mineral mint, soft clay, and muted periwinkle used only for semantic data/state
- Soft directional lighting adds tactility, while tonal borders keep boundaries visible
- No strict low-contrast neumorphism, generic glassmorphism, saturated purple gradients, neon accents, or decorative rainbows
- Progress animation should communicate cause and effect; decorative motion respects reduced-motion settings
- Haptics confirm logging, milestone completion, and reorder boundaries and must respect system settings
- Photography remains optional user content rather than the default visual identity

The complete visual grammar, accessibility thresholds, and research sources are recorded in `docs/DESIGN_DIRECTION.md`. Concept v2 is directional, not a final specification. All displayed data are fictional.

## Monetization recommendation

Do not show a paywall before the user creates and advances a real Path.

Suggested fair boundary:

| Free | Pro |
| --- | --- |
| Up to 3 active Paths | Unlimited active Paths |
| Unlimited milestones, moves, and core ritual tracking inside those Paths | Advanced pace/pattern insights |
| Weekly review and basic history | Full historical comparisons and exports |
| One Today widget | Full widget set and visual customization |
| Local export/backup | Optional encrypted sync/backup |
| Light and dark defaults | Additional themes and icon styles |

Offer monthly, annual, and lifetime access if the economics work. An optional AI planning/coaching service should be separately disclosed and priced because it has recurring inference cost and a materially different privacy boundary.

This is a proposal, not an approved business decision.

## Android delivery direction

If approved, implementation should use Kotlin, Jetpack Compose, Material 3 foundations, unidirectional state flow, coroutines/Flow, durable Room persistence with migrations, DataStore for preferences, and the smallest set of additional libraries that solves proven needs.

- New Play submissions currently need to target Android 16 / API 36: [Play target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878)
- Compose supports Material 3 Expressive and Android 16 styling: [Material 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- Adaptive navigation should become bottom navigation on compact windows and a rail on expanded windows: [Android adaptive navigation](https://developer.android.com/develop/adaptive-apps/guides/build-adaptive-navigation)
- Haptic feedback should use system-aware APIs and not require a vibration permission for normal touch feedback: [Android haptic feedback](https://developer.android.com/develop/ui/views/haptics/haptic-feedback)

Minimum Android version remains TBD until target-user device needs are chosen. The connected Pixel 9 Pro and the existing API 34/API 36 environment are suitable for primary development and current-platform verification but are not sufficient release coverage by themselves.

## Privacy proposal

- Local-first, usable without registration
- No analytics, telemetry, ads, or crash upload by default
- No private content in logs
- Notification permission only after a reminder is enabled
- System Photo Picker for existing photos; camera permission only when the user chooses in-app capture
- Biometric prompt only when app lock is enabled
- Export at any time and complete account/data deletion if cloud sync is later added
- Clear separate consent before any private goal text is sent to an AI provider
- AI-off mode must remain a complete product

## Decisions needed before implementation

1. Is this a new independent product, or a deliberate successor to the installed Goals app with migration and identity implications?
2. Which first audience is primary: professionals/founders, students, or broad personal growth?
3. Should version 1 be local-only, or must multi-device sync be present at launch?
4. Which business model is preferred: paid/lifetime, fair freemium, or subscription-first?
5. Is AI absent at launch, optional during planning/review, or a central paid feature?
6. Should the product feel mostly calm/editorial as proposed, or more energetic/playful?

No Android code, package identity, store listing, account system, backend, or monetization integration should be created until the first five product decisions are made.

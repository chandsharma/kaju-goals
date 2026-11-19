# Project context

Last updated: 19 November 2026 (Asia/Kolkata)

## Current state

The primary personal-tracking job and concept v2 visual foundation are approved; the final name, package identity, business model, sync boundary, and implementation scope remain open. No Android code has been created. A research-backed proposal is available in `docs/PRODUCT_PROPOSAL.md`, and production usability requirements are recorded in `docs/UX_REQUIREMENTS.md`. This repository remains independent from every sibling app.

## Product identity

| Item | Decision |
| --- | --- |
| Product name | TBD |
| One-sentence promise | Direction approved: make personal goals and progress effortless to track; final public copy TBD |
| Primary users | The owner as the first real user; broader launch audience TBD |
| Primary jobs | List and manage goals, log progress quickly, inspect history, and understand current progress |
| Business model | TBD |
| Application ID | TBD; must be globally unique |
| Minimum Android version | TBD from user needs and current device data |
| Target Android version | Use the current Play-required target verified at implementation time |

## Product scope

- Core workflow: open Today or Goals, find a goal, log progress in no more than two deliberate actions, and see the result immediately
- Required screens: Today, Goals list, Goal detail/history, quick progress log, create/edit goal, Progress, and You/settings
- Offline behavior: TBD
- Account/cloud behavior: TBD
- Import/export/recovery: TBD
- Accessibility requirements: production-grade by default
- Explicit non-goals: TBD

## Data and privacy

- Data model: TBD
- Storage and encryption: TBD
- Permissions: none until justified by a user-activated feature
- Analytics/telemetry/ads: none unless explicitly approved and documented
- Data deletion/export policy: TBD before release

## Design direction

The user approved concept v2 as the visual foundation. It uses cool luminous neutrals, large rounded modular surfaces, crisp black sans-serif typography, circular controls and progress instruments, restrained pastel data colors, and subtle dimensional lighting. Approval is conditional on real usability: first-class list views, obvious actions, quick logging, accessible contrast, and complete state coverage. The concept is a direction, not a pixel specification.

## Architecture

TBD after product requirements are known. Use the smallest maintainable production architecture that satisfies durability, security, testing, and performance requirements.

## Current verification

- Repository is initialized.
- Shared Android development environment and publishing route are documented.
- No application code, package identity, store listing, backend, analytics service, or runtime integration has been created.
- The public contribution-ready repository is live at `https://github.com/chandsharma/kaju-goals`; `main` is the default branch, issues and discussions are enabled, and private vulnerability reporting is available.
- On 19 November 2026, the installed Goals app (`com.mindfulsuite.goals`, version 10.3.13) was audited on the connected physical Pixel 9 Pro for information architecture, visible features, visual system, and paid boundaries. No app data was cleared, no purchase was started, and no user record was intentionally changed.
- Current category products, behavior-change research, Android design guidance, and the 2026 Play target API requirement were reviewed; sources are linked from `docs/PRODUCT_PROPOSAL.md`.
- Concept board generated and visually inspected at 1672 × 941 pixels. It contains fictional sample data and is not a final UI specification.
- Five user-supplied visual references were inspected on 19 November 2026. Their shared design grammar and accessibility constraints are recorded in `docs/DESIGN_DIRECTION.md`; the reference images themselves are not redistributed as project assets.
- Concept v2 was generated from those references and visually inspected at 1672 × 941 pixels. It uses fictional data and is approved as a visual foundation, not as a final UI specification.
- The project is designated open source and contribution-ready under Apache-2.0. Repository and contribution details are maintained in `docs/OPEN_SOURCE.md`.
- The official `android/skills` repository was reviewed as a future implementation aid. Relevant Compose, navigation, performance, security, testing, and Play skills should be selected only when their work begins; no blanket skill installation is needed during product exploration.

## Open decisions

1. Broader launch audience beyond the owner as first user.
2. Name and unique application ID.
3. Core feature boundary and monetization.
4. Local/cloud data model and privacy posture.


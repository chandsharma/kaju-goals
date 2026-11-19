# Android project instructions

This repository is intentionally product-neutral. Do not import product ideas, UI decisions, names, assets, package IDs, code, screenshots, or data from sibling projects unless the user explicitly requests a specific item.

## Start every substantial task here

Read these files before acting:

1. `docs/PROJECT_CONTEXT.md`
2. `docs/DECISIONS.md`
3. `docs/ENVIRONMENT.md`
4. `docs/QUALITY_STANDARD.md`
5. `docs/PLAY_RELEASE.md` when signing or publishing is involved

Keep `PROJECT_CONTEXT.md` and `DECISIONS.md` current as the product becomes defined. Record facts and decisions, not chat transcripts.

For every substantial task, preserve durable learnings, research conclusions, and changed assumptions in an appropriate Markdown file under `docs/`. Update `PROJECT_CONTEXT.md` with current facts and `DECISIONS.md` with every approved or rejected durable choice. Do not rely on chat history as the only record of product context.

## Product and research discipline

- Treat every app as a real production product, not a sample, tutorial, or disposable prototype.
- Before choosing architecture, libraries, platform APIs, Play policy answers, or important UX patterns, verify current information online. Prefer official Android, Google Play, Kotlin, and library documentation for technical claims.
- Research current high-quality mobile interfaces and relevant competitors, but create an original product-specific design. Do not blindly copy another app or impose a design system before the product is understood.
- Aim for a distinctive, premium, modern, intuitive result with excellent typography, spacing, motion, states, accessibility, and visual hierarchy.
- Define the product brief, audience, core jobs, information architecture, data model, application ID, and privacy model before large implementation work.

## Engineering defaults

- Prefer Kotlin, Jetpack Compose, Material 3 foundations, unidirectional state flow, coroutines/Flow, and a layered architecture. Use only the pieces the product actually needs.
- Keep business logic out of composables. Model loading, empty, populated, offline, error, permission-denied, and recovery states explicitly.
- Use integer minor units for money. Never use floating point for authoritative financial values.
- Make local data durable, migration-tested, exportable, and recoverable when the product stores user-created records.
- Request optional or sensitive permissions only when the user activates the relevant feature. Core use should not depend on unrelated permissions.
- No ads, analytics, telemetry, trackers, or cloud backend by default. Add them only after an explicit product decision and update privacy/Play documentation.
- Never log secrets, access tokens, passwords, personal records, or private user content.

## Safe device and data handling

- Reuse the installed SDK, JDK, Gradle caches, and existing Pixel 9 Pro AVD described in `docs/ENVIRONMENT.md`. Prove something is missing before downloading it.
- Use `adb install -r` for updates. Never uninstall an installed app, clear package data, change its application ID, or replace its signing identity when that could erase or strand user data without explicit approval and a verified migration/backup.
- Use fictional data on emulators and in screenshots. Treat the physical phone as persistent user data.

## Verification bar

Before calling a change complete, run the relevant unit tests, lint, debug build, release build, and device/instrumentation tests. Visually inspect real rendered states on the existing Pixel 9 Pro AVD. For releases also verify signing, version codes, AAB contents, 16 KB compatibility, permissions, privacy disclosures, and store assets.

## Secrets and publishing

- Never commit keystores, password files, API secrets, OAuth secrets, recovery passwords, service-account files, or tokens.
- Existing local signing and Play Console details are documented in `docs/PLAY_RELEASE.md`. Use them without copying secrets into source or tool output.
- A new app must receive a new unique `applicationId` and a new Play Console app entry. Do not reuse another app's package identity or listing.
- Publishing is an external action: perform it only when the user requests publishing, then complete the full release workflow and accurately report review/live status.


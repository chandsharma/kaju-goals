# Contributing

Thank you for helping build a production-quality, accessible Android goals app.

## Before starting

1. Read `docs/PROJECT_CONTEXT.md`, `docs/DECISIONS.md`, `docs/QUALITY_STANDARD.md`, and any relevant design or architecture document.
2. Search existing issues and discussions.
3. Open or comment on an issue before undertaking a substantial feature, dependency, schema, privacy, or UX change.
4. Do not treat exploratory concepts as approved specifications.

## Pull requests

- Keep each pull request focused on one problem.
- Explain the user problem, the chosen approach, alternatives considered, and verification performed.
- Add or update tests in proportion to risk.
- Update durable Markdown context when the change alters product behavior, architecture, privacy, dependencies, or visual-system rules.
- Preserve accessibility, offline behavior, migration safety, exportability, and recovery.
- Use fictional data in tests, screenshots, and demos.
- Never commit secrets, signing material, private user records, or copyrighted reference assets.

Once Android code exists, pull requests are expected to pass formatting, unit tests, lint, debug and release builds, and relevant instrumentation tests. Visual changes must include rendered evidence on the documented Pixel 9 Pro AVD and accessibility checks.

## Commit style

Use concise imperative subjects, for example:

```text
Add weekly review empty state
Fix currency migration overflow
Document reminder permission flow
```

## Licensing

By submitting a contribution, you agree that it is licensed under Apache License 2.0 and that you have the right to submit it.

# Open-source project policy

Last updated: 19 November 2026 (Asia/Kolkata)

## Repository

- Canonical repository: `https://github.com/chandsharma/kaju-goals`
- Visibility: public
- License: Apache License 2.0
- Default branch: `main`
- Product name and repository name are independent; the repository may be renamed after product naming is approved.

## Contribution posture

The project welcomes:

- product and UX critique backed by a clear user problem;
- accessibility review and assistive-technology testing;
- Android, Kotlin, Compose, persistence, security, performance, and testing work;
- documentation and localization improvements;
- reproducible bug reports and focused feature proposals.

Large implementation work should begin from an agreed issue so contributors do not build against an unsettled product decision. Pull requests must preserve the documented privacy posture and verification bar.

## Governance

- Durable product decisions belong in `docs/DECISIONS.md`.
- Current facts and verification evidence belong in `docs/PROJECT_CONTEXT.md`.
- Design changes must update `docs/DESIGN_DIRECTION.md` when they change the system rather than one screen.
- Architecture decisions should be added as ADRs under `docs/adr/` once implementation begins.
- Security reports follow `SECURITY.md` and must not be filed publicly before coordination.

## Licensing

Source, documentation, and accepted contributions are licensed under Apache-2.0 unless a file explicitly states otherwise. Contributors retain copyright in their work and license accepted contributions under the repository license.

Third-party assets, fonts, sample data, and libraries require documented provenance and compatible terms. User-supplied design references are research inputs only and are not redistributed or treated as licensed project assets.

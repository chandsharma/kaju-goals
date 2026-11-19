# Production quality standard

## Product

- Solve a clear user problem with a focused primary workflow.
- Prefer depth, reliability, and clarity over a large shallow feature list.
- Define sensible onboarding, defaults, empty states, recovery, and failure handling.

## Experience

- Create an original, product-specific visual system with premium typography, spacing, hierarchy, color, motion, and feedback.
- Validate concepts with rendered screens, not only descriptions or wireframes.
- Support dynamic text, screen readers, touch targets, contrast, reduced motion, keyboard/switch access where relevant, dark mode when appropriate, and adaptive layouts.
- Test realistic populated states; an attractive empty screen is not enough.

## Engineering

- Use explicit domain models, stable persistence, migrations, deterministic calculations, bounded imports, secure exports, and actionable errors.
- Keep secrets out of source. Minimize permissions and third-party dependencies.
- Add tests around money, dates/time zones, migrations, deletion/recovery, concurrency, background work, import/export, and security boundaries as applicable.
- Profile startup, scrolling, memory, battery, and large datasets before release.

## Release evidence

Maintain evidence in `docs/PROJECT_CONTEXT.md`: commands run, tests passed, devices/API levels, inspected UI states, signing fingerprints, artifact hashes, known limitations, and external review status. Do not claim production readiness from compilation alone.


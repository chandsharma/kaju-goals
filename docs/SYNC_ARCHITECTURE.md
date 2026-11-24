# Google Drive sync architecture

Last updated: 24 November 2026 (Asia/Kolkata)

## Product boundary

Sync is optional. Kaju Goals remains fully usable offline without a Google account. When enabled, goal data travels directly between the Android app and a hidden file in the Google account selected by the user. Kaju operates no account service, application backend, analytics pipeline, or copy of that data.

The app requests only:

`https://www.googleapis.com/auth/drive.appdata`

This non-sensitive scope is limited to the app's hidden `appDataFolder`; it does not grant access to ordinary Drive files. AuthorizationClient provides short-lived access tokens. Kaju does not persist access tokens, refresh tokens, passwords, or an asserted account email. UI copy therefore says “the Google account you choose,” not an email address the app cannot reliably know.

Official references:

- [Android user data authorization](https://developer.android.com/identity/authorization)
- [Store application-specific data in Drive](https://developers.google.com/workspace/drive/api/guides/appdata)
- [Drive API scopes](https://developers.google.com/workspace/drive/api/guides/api-specific-auth)
- [WorkManager persistent work](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started)

## Google Cloud development configuration

| Item | Development value |
| --- | --- |
| Project | `Kaju Goals` |
| Project ID / number | `kaju-goals` / `370615343432` |
| API | Google Drive API enabled |
| Auth audience | External, Testing |
| OAuth scope | `drive.appdata` only |
| Test user | `hello@nanti.ai` |
| Android debug package | `com.chandsharma.kajugoals.debug` |
| Debug SHA-1 | `E4:B3:AC:57:8E:F9:70:8D:11:D1:1B:92:9C:57:3E:5D:EB:46:0A:7A` |

No OAuth JSON, client secret, service account, token, or other credential was downloaded or committed. Android OAuth client IDs are public identifiers, but the app does not need one embedded in source for AuthorizationClient's Android package/signature matching.

Before a public release:

1. Create a separate Android OAuth client for `com.chandsharma.kajugoals` with the final Google Play App Signing SHA-1, not the upload/debug certificate.
2. Finish OAuth branding with the final homepage, privacy policy, terms, and production icon.
3. Re-evaluate the exact scope and data disclosures from the final implementation, then move OAuth publishing status out of Testing only when release is approved.
4. Keep Play Console publication deferred until the user explicitly confirms.

## Local source of truth

Room database version 3 stores goals and progress. Goal records now include optional deadline, reminder, category, pinning, and recurrence fields. Every syncable record has:

- a stable UUID;
- an `updatedAt` epoch-millisecond timestamp;
- a random `mutationId` used as a deterministic tie-breaker;
- an optional `deletedAt` tombstone.

Progress totals are still derived from nondeleted progress entries. Deletion never depends on a cloud snapshot omitting an item; the tombstone is synchronized so an older device cannot resurrect it.

## Snapshot format

The hidden file remains named `kaju_goals_snapshot_v1.json` and contains:

- snapshot schema version;
- generation time and installation-scoped random device ID;
- complete goal and progress payload, including tombstones;
- SHA-256 checksum over the deterministic serialized payload.

The current snapshot schema is 2. The decoder retains schema-1 checksum and payload compatibility so an existing Drive snapshot can be upgraded through the normal merge-first path. New goal fields have safe defaults. An unsupported schema, malformed JSON, or checksum mismatch stops sync before any Drive write. The app never logs the snapshot body or token.

## Merge-first algorithm

Each sync performs this order:

1. Read the complete local snapshot, including tombstones.
2. List and download every matching hidden Drive snapshot.
3. Validate every remote schema and checksum.
4. Group records by stable ID and select the maximum `(updatedAt, mutationId)` version.
5. Reject progress whose owning goal is absent from the merged snapshot.
6. Apply merged goals first and progress second in one local Room transaction.
7. If the merged snapshot is empty, stop without creating a Drive file.
8. If Drive already contains the identical canonical payload, stop without writing.
9. Otherwise conditionally update the newest Drive file using its ETag, or create a first file.
10. On HTTP 412, repeat the entire download/merge/write cycle up to three times.

This ordering is the fresh-install guard: zero local records plus existing cloud records first becomes a nonempty merged local database. An empty install can never upload an empty replacement over nonempty Drive data.

Concurrent first uploads can create duplicate hidden files. Subsequent syncs deliberately read and merge every duplicate rather than deleting anything automatically. The newest listed file is the canonical update target; stale duplicates remain recoverable input instead of being destructively cleaned up.

## Scheduling and recovery

- Every local create, edit, status change, progress log, or progress deletion requests unique network-constrained WorkManager work when Drive is connected.
- App initialization also requests a sync for connected installations.
- Snapshot sync coalesces current database state, so it does not depend on an in-memory mutation queue.
- I/O and server errors retry through WorkManager.
- Missing/revoked consent changes the UI to a reconnect state; a worker cannot launch consent UI in the background.
- The user can always use local tracking while offline or disconnected.
- “Turn off sync” clears only the local connected flag. It keeps the Room database and hidden Drive snapshot intact; any already queued worker exits before network access. Reconnecting merges both copies again.

## Verified safety properties

Automated tests currently cover:

- empty local + nonempty cloud restores cloud records;
- newer local edits win without dropping cloud-only progress;
- equal timestamps resolve by mutation ID independent of input order;
- a newer deletion tombstone cannot be resurrected by an older snapshot;
- orphan progress is excluded;
- Room v1→v2 migration preserves goals/progress and initializes sync metadata.
- Room v2→v3 migration preserves goals/progress and initializes deadline, reminder, category, pinning, and recurrence defaults.
- legacy Drive schema-1 snapshots decode and validate before merging into schema 2.

Still required before public release: a disposable real-account upload, fresh-install restore, revoked-consent recovery, offline queue, two-device conflict, corrupt-file refusal, large-snapshot, and process-death test matrix.

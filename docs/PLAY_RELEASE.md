# Google Play release route

Last verified: 19 November 2026

## Publisher

| Item | Value |
| --- | --- |
| Console | `https://play.google.com/console/` |
| Google account | `temstir@gmail.com` |
| Developer ID | `8287137183353797722` |

Create a new Play Console app for this project after its name and globally unique application ID are final. Never reuse another app's package identity, signing registration, OAuth client, listing, or Data safety answers.

## Existing upload signing material

An owner-controlled upload key already exists locally and may be used for a new app's upload certificate. This repository contains ignored symlinks to the existing files; it does not duplicate the private key:

- Keystore: `local-infrastructure/android-upload.jks`
- Password file: `local-infrastructure/android-upload.password`
- SHA-1: `8F:7A:C8:31:D8:D5:BA:BC:89:0D:26:19:C6:F1:A9:F5:4B:D0:74:4C`
- SHA-256: `D4:0F:71:B3:29:F2:0C:43:8B:D8:01:A6:C5:6E:54:BB:93:D0:A2:2E:CF:CF:89:24:D5:33:B3:A6:1B:A8:B5:33`

Do not commit either private file, print its password, or expose it in logs. Discover the existing key alias locally with `keytool -list` when release signing is configured; do not record it in product documentation. Release configuration must read the password locally at build time. Google Play App Signing should remain enabled; the Play signing certificate is different and must be read from the new app's Play Console after its first AAB upload.

## New-app release checklist

1. Finalize product name, application ID, default language, app/game type, and free/paid status.
2. Create the Play Console app under the publisher above.
3. Enable Play App Signing and upload an AAB signed by the local upload key.
4. Record the Play app-signing SHA-1/SHA-256. Register exact package/signing pairs with Google APIs only if the app uses those APIs.
   - Kaju Goals uses optional Google Drive `appDataFolder` authorization. Create the production Android OAuth client for `com.chandsharma.kajugoals` with the Play App Signing SHA-1 after the Play app exists. Do not reuse the debug client or upload-key SHA-1.
   - Finish OAuth branding URLs/icon and move the external OAuth app out of Testing only as part of the approved release workflow. The development project/configuration is recorded in `docs/SYNC_ARCHITECTURE.md`.
5. Complete store listing, icon, feature graphic, real-device screenshots, privacy policy, Data safety, ads, content rating, target audience, app access, permissions/declarations, and country availability from the actual implementation.
6. Run unit, lint, release, device, accessibility, backup/restore, upgrade, and signing checks. Inspect the final AAB and verify the version code is new.
7. Use internal testing when it materially improves release confidence. Submit production only when requested and report whether it is draft, in review, approved, or live.

Never guess Play declarations or copy them from another app. Re-evaluate them from the final manifest, dependencies, network behavior, data flows, and current Play policy.


# SDK migrations

## API 35 → 36 (Android 16)

- Raise compileSdk and targetSdk together; retain minSdk 26.
- Use AGP 8.13.2 and Gradle 8.13, which support API 36.
- Use safe drawing insets on the landing and onboarding screens, including
  horizontal display cutouts when the device rotates. The transfer activity
  uses a floating dialog theme. No edge-to-edge opt-out is present.
- Upgrade Rive to 10.0.4 for 16 KB native page alignment (the old 4.0.0
  runtime predates support). Retain the existing RiveAnimationView API.
- Let the existing Firebase BOM select analytics-ktx instead of latest.release,
  which resolves to an incompatible Kotlin metadata version on a fresh build.
- Back handling uses AndroidX; there are no onBackPressed overrides or raw
  back-key handlers to migrate.
- No orientation, aspect-ratio, or resizability restrictions are declared.
- File transfer uses a foreground service, not JobScheduler/WorkManager.
- No ordered-broadcast priorities, MediaStore version parsing, or nested
  untrusted intent forwarding are used.

Before release, manually check onboarding, the tip sheet, back gestures,
landscape/tablet layouts, sharing one/multiple files, cancellation, and a
background transfer on Android 16. These device checks have not been run.

References:
- https://developer.android.com/about/versions/16/behavior-changes-16
- https://developer.android.com/about/versions/16/behavior-changes-all
- https://developer.android.com/build/releases/agp-8-13-0-release-notes

## API 36 → 37 (Android 17)

- Raise compileSdk and targetSdk together to 37; retain minSdk 26.
- Use AGP 9.1.1 and Gradle 9.3.1 (the minimum supported API 37 toolchain),
  with AGP's built-in Kotlin 2.2.10 and matching Compose/serialization plugins.
- Switch to the supported optimizing ProGuard defaults. Limit log-removal
  rules to static logging methods, avoiding inherited Object methods.
- Declare ACCESS_LOCAL_NETWORK. Gate onboarding discovery and the share
  screen behind a user-initiated runtime request on API 37+. After denial,
  show retry and app-settings actions; refresh grants on activity resume.
  Earlier Android versions proceed without the new request.
- Guard discovery (including the legacy chooser service), resolution retries,
  and foreground transfer startup. Route a direct-share selection through the
  permission UI if its previously granted permission has been revoked.
- Dispose share-screen discovery when the screen/permission gate leaves the
  composition. Keep the existing multi-device picker and LAN socket protocol.
- No widgets, SMS/contacts access, private MessageQueue reflection, static-final
  mutation, or custom background activity launch exemptions exist in app code.
  Rive native libraries are packaged by AGP, not downloaded as executable code.

Before release, manually check Android 17 fresh-install grant, denial, repeated
denial, grant/revoke in Settings, onboarding, direct share, file transfer and
cancellation. Also check Android 26–36 sharing without a local-network prompt.
These device checks have not been run. The versionCode must be increased when
preparing the next Play release; neither migration publishes an app bundle.

References:
- https://developer.android.com/about/versions/17/behavior-changes-17
- https://developer.android.com/about/versions/17/behavior-changes-all
- https://developer.android.com/privacy-and-security/local-network-permission
- https://developer.android.com/build/releases/agp-9-1-0-release-notes
- https://developer.android.com/build/migrate-to-built-in-kotlin

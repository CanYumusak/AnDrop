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

# DailyHub

A personal productivity app — tasks, subtasks, recurring tasks, a calendar,
workout tracking and progress stats in one place. Built as a native Android app
(Kotlin + Jetpack Compose).

> Note: the project started out in Flutter and was later fully migrated to native
> Android. The application id keeps the old name (`com.moltrax.personalnoteapp`).

## Features

- **Tasks** — subtasks, recurrence, categories and reminder notifications
- **Calendar** — browse tasks by date
- **Workouts** — session tracking, post-workout summary and exercise demo videos
- **Profile** — progress / development stats
- **Home-screen widget** — complete, undo and toggle subtasks without opening the
  app (Glance)
- **Localization** — Turkish / English
- **Dark / neon theme**

> Google Drive backup & sync exists in the codebase but is **currently disabled**
> behind `FeatureFlags.DRIVE_SYNC_ENABLED` (the app starts straight on the home
> screen, all data stays local). See [Enabling Google Drive sync](#enabling-google-drive-sync).

## Tech stack

| Area | Used |
|------|------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| DI | Hilt |
| Database | Room |
| Background work | WorkManager |
| Widget | Glance |
| Networking | Retrofit + OkHttp + kotlinx.serialization |
| Media | Coil, Media3 (ExoPlayer) |
| Cloud | Google Drive API, Google Sign-In |

- **minSdk** 26 · **targetSdk** 35 · **compileSdk** 35

## Prerequisites

- Android Studio (latest stable) with the Android SDK, or a standalone
  JDK 17 + Android SDK for command-line builds
- JDK 17 (Android Studio bundles a compatible JBR)

## Setup

1. Clone the repository and open it in Android Studio.
2. Copy `local.properties.example` to `local.properties` and fill in the values
   (this file is git-ignored and **must never be committed**):

   ```properties
   sdk.dir=C:\\Users\\<user>\\AppData\\Local\\Android\\Sdk
   GOOGLE_CLIENT_ID=your_web_client_id.apps.googleusercontent.com
   DRIVE_FOLDER_NAME=DailyHubBackup
   DRIVE_SCOPE=https://www.googleapis.com/auth/drive.appdata
   ```

   With Drive sync disabled (the default) the Google values are optional — only
   `sdk.dir` is required to build.

## Building an APK from source

The commands below use the Gradle wrapper (`./gradlew` on macOS/Linux,
`gradlew.bat` on Windows). If no wrapper is checked out, generate one with
`gradle wrapper --gradle-version 8.9`, or run the tasks from Android Studio
(**Build → Build Bundle(s) / APK(s) → Build APK(s)**).

### Debug APK (quickest, no signing needed)

```bash
./gradlew :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

A debug APK is signed with an auto-generated debug key and installs directly on a
device or emulator. It is meant for testing, not for distribution.

### Release APK (optimized & signed)

A release APK is minified (R8/ProGuard) and must be signed with your own keystore.

1. **Create a keystore** (once — keep it safe, you need the *same* keystore to ship
   future updates):

   ```bash
   keytool -genkeypair -v \
     -keystore release.jks \
     -alias dailyhub \
     -keyalg RSA -keysize 2048 -validity 10000
   ```

   Put `release.jks` in the project root (the `.gitignore` already excludes
   `*.jks`).

2. **Add the signing credentials** to `local.properties`:

   ```properties
   RELEASE_STORE_FILE=release.jks
   RELEASE_STORE_PASSWORD=********
   RELEASE_KEY_ALIAS=dailyhub
   RELEASE_KEY_PASSWORD=********
   ```

3. **Build:**

   ```bash
   ./gradlew :app:assembleRelease
   ```

   Output: `app/build/outputs/apk/release/app-release.apk`

> ⚠️ Back up the keystore file and its passwords. Without the same keystore you
> cannot update an already-published app.

### Install on a device

Enable "install from unknown sources", copy the APK to the phone and open it, or
with USB debugging on:

```bash
adb install app/build/outputs/apk/release/app-release.apk
```

## Enabling Google Drive sync

Drive backup/sync ships disabled because it requires a verified Google OAuth
client. To turn it on:

1. Register your signing certificate's **SHA-1** with the Android OAuth client in
   the [Google Cloud Console](https://console.cloud.google.com/apis/credentials).
   Get the SHA-1 with:

   ```bash
   keytool -list -v -keystore release.jks -alias dailyhub
   ```

2. Add `app/google-services.json` (from Firebase / Google Cloud) — git-ignored.
3. Set `DRIVE_SYNC_ENABLED = true` in
   `app/src/main/java/com/moltrax/personalnoteapp/FeatureFlags.kt` and rebuild.

## Tests

```bash
./gradlew :app:testDebugUnitTest
```

## License

Private project — all rights reserved.

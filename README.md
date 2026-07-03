# DailyHub

A personal productivity app — tasks, subtasks, recurring tasks, a calendar,
workout tracking and progress stats. Native Android (Kotlin + Jetpack Compose).

## First-time setup on a new machine

Only needed once. Create a file named `local.properties` in the project root
(it is git-ignored, so it never gets committed) and fill in the values below.
Each one is explained under the table.

```properties
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
EXERCISEDB_KEY=<your RapidAPI key>
RELEASE_STORE_FILE=release.jks
RELEASE_STORE_PASSWORD=<your password>
RELEASE_KEY_ALIAS=pna
RELEASE_KEY_PASSWORD=<your password>
```

| Value | Required? | Where to get it |
|-------|-----------|-----------------|
| `sdk.dir` | ✅ Yes | Path to your Android SDK |
| `EXERCISEDB_KEY` | Optional | RapidAPI key for exercise videos |
| `RELEASE_STORE_FILE` | For release builds | Your signing keystore |
| `RELEASE_STORE_PASSWORD` | For release builds | Password you set when creating the keystore |
| `RELEASE_KEY_ALIAS` | For release builds | Key alias inside the keystore |
| `RELEASE_KEY_PASSWORD` | For release builds | Password for that key |

### `sdk.dir` — your Android SDK path

The folder where the Android SDK is installed. If you have Android Studio, it's
usually:

```
C:\Users\<you>\AppData\Local\Android\Sdk
```

You can confirm the exact path in Android Studio under
**Settings → Languages & Frameworks → Android SDK** (the "Android SDK Location"
field at the top). Escape the colon and backslashes as shown in the example
above (`C\:\\Users\\...`).

### `EXERCISEDB_KEY` — exercise demo videos (optional)

Powers the exercise demo videos. If you leave it out the app still builds and
runs — you just won't see those videos. To get a key:

1. Sign up at [rapidapi.com](https://rapidapi.com).
2. Subscribe to the **ExerciseDB** API (has a free tier).
3. Copy the `X-RapidAPI-Key` value from the API's endpoint page and paste it in.

### `RELEASE_*` — signing keystore (only for release builds)

A release APK must be signed. You create the keystore once and reuse it forever
(the **same** file is required to update an already-installed app).

Create it with `keytool` (bundled with the JDK), run from the project root:

```cmd
keytool -genkeypair -v -keystore release.jks -alias pna -keyalg RSA -keysize 2048 -validity 10000
```

It will ask you to set a password and some name/org details. Then fill
`local.properties`:

- `RELEASE_STORE_FILE` → `release.jks` (the file you just created, in the project root)
- `RELEASE_STORE_PASSWORD` → the password you typed
- `RELEASE_KEY_ALIAS` → `pna` (the `-alias` from the command above)
- `RELEASE_KEY_PASSWORD` → the key password (same as the store password unless you set a different one)

> ⚠️ Back up `release.jks` and its passwords. Without them you can't update an
> already-installed app.

## Build a release APK (Windows)

Open **cmd** in the project folder and run:

```cmd
gradlew.bat :app:assembleRelease
```

The signed APK lands here:

```
app\build\outputs\apk\release\app-release.apk
```

Copy it to your phone and open it to install (enable "install from unknown
sources" if Android asks).

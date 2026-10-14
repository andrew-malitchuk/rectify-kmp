# aos-application

> Android application entry point — bootstraps Koin and hosts the single Activity.

## Responsibility

Bridges the Android platform lifecycle to the shared KMP UI layer. Owns the two mandatory Android components: the `Application` subclass that wires the Koin graph and the single `Activity` that hosts the Compose UI. No business logic, no ViewModels, no repositories — all of that lives in KMP-shared modules.

## Module Structure

```
src/main/kotlin/dev/rectify/io/
├── core/                          # Internal platform infrastructure
│   └── RectifyApplication.kt  # Application subclass, Koin bootstrap
└── source/                        # Public Android components
    └── activity/
        └── MainActivity.kt        # Single-activity entry point
```

**Convention:**
- `core/` — `internal` classes not visible outside the module (infrastructure, wiring).
- `source/` — public Android framework components, organised into semantic subpackages.

## Dependencies

| Depends on | Purpose |
|---|---|
| `compose-application` | Shared `App()` composable and `initKoin()` |
| `domain-core`, `domain-usecase-api` | Use-case contracts consumed by platform components |
| `androidx.activity.compose` | `ComponentActivity`, `setContent`, `enableEdgeToEdge` |
| `koin-android` | `androidContext()`, `androidLogger()` |

## Public API

| Class | Package | Description |
|---|---|---|
| `MainActivity` | `source.activity` | Single-activity entry point; enables edge-to-edge and delegates to `App()` |

> `RectifyApplication` lives in `core/` with `internal` visibility — it is not part of the public API.

## Component Details

### MainActivity

- Calls `enableEdgeToEdge()` **before** `super.onCreate()` to avoid window-attachment flicker.
- Delegates all UI to the shared `App()` composable — no business logic lives here.

### RectifyApplication

- Registered in `AndroidManifest.xml` via `android:name`.
- Calls `initKoin { androidLogger(); androidContext(this) }` as the very first statement in `onCreate()`, before any component can request injection.

## Manifest Registration

```xml
<application android:name="dev.rectify.io.core.RectifyApplication">

    <activity
        android:name="dev.rectify.io.source.activity.MainActivity"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>

</application>
```

## Build

```bash
# Debug APK
./gradlew :aos-application:assembleDebug

# Release APK (requires configure/secrets/signing.properties)
./gradlew :aos-application:assembleRelease

# Run lint and unit tests
./gradlew :aos-application:check
```

## SDK Targets

| Property | Value |
|---|---|
| `minSdk` | 27 |
| `targetSdk` | 37 |
| `compileSdk` | 37 |
| JVM target | 21 |
| Release minify | enabled (R8 + ProGuard) |
| Dependency metadata in APK | disabled |

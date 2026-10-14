# build-logic/convention

> Gradle convention plugins that standardise KMP module configuration across all project modules.

## Responsibility

Eliminates per-module boilerplate by encoding build setup decisions in four convention plugins.
Each plugin knows which Gradle plugins to apply, which platform targets to register, and which
dependency sets to wire — a module's `build.gradle.kts` only needs to declare the plugin ID
and any module-specific extras.

## Package Layout

```
src/main/kotlin/dev/rectify/io/convention/
├── core/                        # Internal infrastructure (not consumed directly)
│   └── ext/
│       └── Project.kt                # libs catalog + moduleName helpers
└── source/                      # Public convention plugins
    ├── base/
    │   └── BaseConventionPlugin.kt   # Template Method base class
    └── convention/              # Concrete plugins, one subpackage per type
        ├── application/
        │   └── ApplicationConventionPlugin.kt
        ├── di/
        │   └── DiConventionPlugin.kt
        ├── feature/
        │   └── FeatureConventionPlugin.kt
        └── library/
            └── LibraryConventionPlugin.kt
```

**Convention:** `core/` holds `internal` infrastructure; `source/` holds public-facing plugins
organised into semantic subpackages.

## Registered Plugins

| Plugin ID | Class | Purpose |
|---|---|---|
| `dev.rectify.io.convention.application` | `ApplicationConventionPlugin` | Shared KMP app module — Android + iOS + Desktop + Compose |
| `dev.rectify.io.convention.feature` | `FeatureConventionPlugin` | UI feature modules — Android + iOS + Desktop + Compose |
| `dev.rectify.io.convention.library` | `LibraryConventionPlugin` | Non-UI library modules — Android + iOS + Desktop, no Compose |
| `dev.rectify.io.convention.di` | `DiConventionPlugin` | Koin DI dependency aggregator — apply on top of library or feature |

## Public API

| Class | Plugin ID suffix | Key behaviour |
|---|---|---|
| `ApplicationConventionPlugin` | `application` | iOS framework `baseName` hardcoded to `application_compose`; only one app module exists |
| `FeatureConventionPlugin` | `feature` | iOS `baseName` derived from `moduleName`; adds `androidResources` |
| `LibraryConventionPlugin` | `library` | No Compose plugins; no `androidResources`; coroutines + lifecycle only |
| `DiConventionPlugin` | `di` | `koin-core` exposed as `api()`; must be applied alongside `library` or `feature` |

## Usage

```kotlin
// Feature module (UI layer) — build.gradle.kts
plugins {
    alias(libs.plugins.convention.feature)
    alias(libs.plugins.convention.di)
}
```

```kotlin
// Library module (data / domain layer) — build.gradle.kts
plugins {
    alias(libs.plugins.convention.library)
    alias(libs.plugins.convention.di)
}
```

```kotlin
// Shared application module (compose-application) — build.gradle.kts
plugins {
    alias(libs.plugins.convention.application)
}
```

## Plugin Hook Execution Order

Every plugin runs the following hooks in this fixed sequence (no-ops skipped silently):

```
1. configurePlugin()               — apply Gradle plugin IDs
2. configureAndroidPlatform()      — Android KMP library target
3. configureIOsPlatform()          — iosArm64 / iosSimulatorArm64
4. configureDesktopPlatform()      — jvm("desktop")
5. configureCommonDependencies()   — commonMain source set
6. configureAndroidDependencies()  — androidMain source set
7. configureIOsDependencies()      — iosMain source set
8. configureDesktopDependencies()  — desktopMain source set
```

"Desktop" means a plain current-OS `jvm("desktop")` target via Compose Multiplatform — no
macOS/Linux native target (`macosX64`, `linuxX64`, etc.) is registered anywhere in this
project. The actual runnable desktop app lives in the separate, non-KMP `:desktop-application`
module (plain `kotlin("jvm")`), the same way `:aos-application` is a separate module from the
KMP library `:compose-application`.

## Configuration Reference

| Setting | Value |
|---|---|
| Kotlin | 2.4.21 |
| JVM target | 21 |
| Android `minSdk` | 27 |
| Android `targetSdk` / `compileSdk` | 37 |
| iOS frameworks | Static, arm64 + simulatorArm64 |
| Explicit API mode | Enforced in all KMP modules |
| AGP DSL | `com.android.kotlin.multiplatform.library` (AGP 9.4) |

## Known Behaviours and Pitfalls

**`DiConventionPlugin` is not standalone.**
It does not apply any plugins or register targets — it only adds dependencies to existing
source sets. Applying it without `library` or `feature` will fail with
`Extension of type 'KotlinMultiplatformExtension' does not exist`.

**`androidRuntimeClasspath` instead of `debugImplementation`.**
The `com.android.kotlin.multiplatform.library` AGP 9.0 plugin does not expose
`debugImplementation`. Compose UI tooling must be declared on `androidRuntimeClasspath`.

**`moduleName` returns `""` for the root project.**
`Project.path` is `":"` for the root project; after stripping colons the result is an empty
string. Convention plugins must never be applied to the root project — doing so produces an
empty Android namespace and fails the build with an unhelpful error message.

**Signing is configured per-module, not via a convention plugin.**
Signing is configured inline in `aos-application/build.gradle.kts`.
In AGP 9.0 with `com.android.kotlin.multiplatform.library`, `signingConfigs` is not
available on `CommonExtension` — configure it directly in the app module's `android {}` block.

## Build

```bash
# Verify the convention plugins compile
./gradlew :build-logic:convention:assemble

# Run convention plugin tests
./gradlew :build-logic:convention:test
```

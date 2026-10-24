# build-logic/convention

> Gradle convention plugins that standardise JVM module configuration across all project modules.

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
| `dev.rectify.io.convention.application` | `ApplicationConventionPlugin` | Shared app module — Compose + Skiko renderer |
| `dev.rectify.io.convention.feature` | `FeatureConventionPlugin` | UI feature modules — Desktop + Compose |
| `dev.rectify.io.convention.library` | `LibraryConventionPlugin` | Non-UI library modules — Desktop, no Compose |
| `dev.rectify.io.convention.di` | `DiConventionPlugin` | Koin DI dependency aggregator — apply on top of library or feature |

## Public API

| Class | Plugin ID suffix | Key behaviour |
|---|---|---|
| `ApplicationConventionPlugin` | `application` | Adds `compose.desktop.currentOs` + `kotlinx-coroutines-swing`; only one app module exists |
| `FeatureConventionPlugin` | `feature` | Compose plugins + Compose deps; `kotlin-test` on `test` |
| `LibraryConventionPlugin` | `library` | No Compose plugins; coroutines + lifecycle only |
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
1. configurePlugin()        — apply Gradle plugin IDs
2. configureJvm()           — jvmToolchain(21) + explicitApi()
3. configureDependencies()  — main / test dependencies
```

Every module is a plain `kotlin("jvm")` module: classic `src/main/kotlin` and
`src/test/kotlin`, no Kotlin Multiplatform plugin, no platform targets. The runnable desktop
app lives in the separate `:desktop-application` module, which is why `:compose-application`
stays a library.

## Configuration Reference

| Setting | Value |
|---|---|
| Kotlin | 2.4.21 |
| JVM target | 21 |
| Target | JVM 21 (`kotlin("jvm")`) |
| Explicit API mode | Enforced in every module |

## Known Behaviours and Pitfalls

**`DiConventionPlugin` is not standalone.**
It applies no plugins — it only adds dependencies. Applying it without `library` or `feature`
fails, because the `api`/`implementation` configurations it writes to do not exist yet.

**`moduleName` returns `""` for the root project.**
`Project.path` is `":"` for the root project; after stripping colons the result is an empty
string. Convention plugins must never be applied to the root project.

## Build

```bash
# Verify the convention plugins compile
./gradlew :build-logic:convention:assemble

# Run convention plugin tests
./gradlew :build-logic:convention:test
```

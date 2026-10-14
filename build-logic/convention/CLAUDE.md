# build-logic/convention — Claude Instructions

## Module Purpose

Gradle convention plugins for the project. Lives in a composite build (`includedBuild`) so
it compiles separately from the main project and its classpath does not pollute the main build.
Nothing here is shipped in the app — it is pure build tooling.

## Package Layout Convention

```
dev.rectify.io.convention/
├── core/     — internal infrastructure, not imported by module build.gradle.kts files
│   ├── base/ — BaseConventionPlugin (internal open class)
│   └── ext/  — Project.libs, Project.moduleName, configureSigning()
└── source/   — public convention plugins, one subfolder per semantic type
    ├── application/
    ├── di/
    ├── feature/
    └── library/
```

**Rule:** anything `internal` goes in `core/`. Registered Gradle plugin classes go in
`source/<type>/`. Never flatten `source/` — each plugin type gets its own subfolder.

## BaseConventionPlugin — Template Method

`BaseConventionPlugin` defines 8 open hooks: `configurePlugin`, `configureAndroidPlatform`,
`configureIOsPlatform`, `configureDesktopPlatform`, `configureCommonDependencies`,
`configureAndroidDependencies`, `configureIOsDependencies`, `configureDesktopDependencies`.
Subclasses override only the hooks they need; all others default to `Unit`. Hook order in
`apply()` is fixed and must not change: plugins first, then platform targets, then
dependencies. Violating this order causes `Extension does not exist` errors because extensions
are registered by the plugin IDs.

`configureDesktopPlatform()` registers `jvm("desktop")` — a plain current-OS JVM target via
Compose Multiplatform, not a cross-compiled macOS/Linux native target. No module registers
`macosX64`/`macosArm64`/`linuxX64`. If you add one of those, add a *new*, differently-named
hook (e.g. `configureNativeDesktopPlatform`) to `BaseConventionPlugin` first — don't repurpose
the JVM desktop hook.

When adding a new hook, add it to `BaseConventionPlugin` first, then override in the
relevant plugin classes. Do not add hooks that only one plugin needs — put that logic
directly in the plugin class instead.

## Plugin Comparison Cheat-Sheet

| | `application` | `feature` | `library` | `di` |
|---|---|---|---|---|
| KMP plugin | ✓ | ✓ | ✓ | — |
| AGP KMP library | ✓ | ✓ | ✓ | — |
| Compose plugins | ✓ | ✓ | — | — |
| `androidResources` | ✓ | ✓ | — | — |
| Compose deps (commonMain) | ✓ | ✓ | — | — |
| `jvm("desktop")` target | ✓ | ✓ | ✓ | — |
| `compose.desktop.currentOs` (desktopMain) | ✓ | — | — | — |
| Koin deps | — | — | — | ✓ |
| iOS framework baseName | hardcoded | from `moduleName` | from `moduleName` | — |

`compose.desktop.currentOs` lives only on `ApplicationConventionPlugin` (`compose-application`)
— it's the Skiko native renderer, needed only by the module whose `desktopMain` actually opens
a window. `feature`/`library` modules just need the target registered so their `commonMain`
Compose deps resolve a desktop variant; the real runnable entry point is `:desktop-application`,
a plain-JVM module that applies `kotlin("jvm")` directly (no convention plugin — see its own
`CLAUDE.md`).

## Adding a New Convention Plugin

1. Create `source/<type>/<Name>ConventionPlugin.kt` — package `dev.rectify.io.convention.source.<type>`.
2. Extend `BaseConventionPlugin`, override only the hooks you need.
3. Register in `build.gradle.kts` under `gradlePlugin { plugins { ... } }` with a fully-qualified
   `implementationClass`.
4. Add an entry to the `libs.versions.toml` `[plugins]` section if consumers use `alias()`.
5. Update `README.md` (Registered Plugins table + Plugin Comparison cheat-sheet).

## Modifying an Existing Plugin

- `configurePlugin()` — add/remove plugin IDs here only. Never call `apply()` elsewhere.
- `configureAndroidPlatform()` — SDK versions come from `libs.findVersion()`, never hardcode.
- `configureIOsPlatform()` — registers `iosArm64()`/`iosSimulatorArm64()` only. No `iosX64()`
  (Intel simulator, dropped — unsupported by current Compose Multiplatform/AndroidX KMP
  artifacts) and no macOS/Linux native targets.
- `configureDesktopPlatform()` — registers `jvm("desktop")` only. No `macosX64()`/`macosArm64()`/
  `linuxX64()`.
- `configureAndroidDependencies()` — use `androidRuntimeClasspath` for Compose tooling, not
  `debugImplementation` (not available in the KMP Android library DSL).

## DiConventionPlugin Rules

- Always apply alongside `library` or `feature`. Never standalone.
- `koin-core` is `api()` — intentional. Consumers need it at compile time.
- Do not call `explicitApi()` here — the parent plugin already does it.

## moduleName Edge Cases

`Project.moduleName` strips `:` and converts `-` to `.`. Two edge cases to know:
- Root project (`path == ":"`) → returns `""` → empty Android namespace → build fails.
  Guard: convention plugins must never be applied to the root project.
- Modules with numeric segments (e.g. `:data-ble-v2`) → `data.ble.v2` — valid namespace.

## Signing

Signing is configured inline in `aos-application/build.gradle.kts` and must stay there.
In AGP 9.0 with `com.android.kotlin.multiplatform.library`, `signingConfigs` is not
accessible via `CommonExtension` — do not attempt to centralise it in a convention plugin.

## Build Commands

```bash
./gradlew :build-logic:convention:assemble   # compile plugins
./gradlew :build-logic:convention:test       # run plugin tests
```

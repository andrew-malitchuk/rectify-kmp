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
│   └── ext/  — Project.libs, Project.moduleName
└── source/   — public convention plugins, one subfolder per semantic type
    ├── application/
    ├── di/
    ├── feature/
    └── library/
```

**Rule:** anything `internal` goes in `core/`. Registered Gradle plugin classes go in
`source/<type>/`. Never flatten `source/` — each plugin type gets its own subfolder.

## BaseConventionPlugin — Template Method

`BaseConventionPlugin` defines 4 open hooks: `configurePlugin`, `configureDesktopPlatform`,
`configureCommonDependencies`, `configureDesktopDependencies`.
Subclasses override only the hooks they need; all others default to `Unit`. Hook order in
`apply()` is fixed and must not change: plugins first, then platform targets, then
dependencies. Violating this order causes `Extension does not exist` errors because extensions
are registered by the plugin IDs.

`configureJvm()` pins `jvmToolchain(21)` and turns on `explicitApi()`. Every module is a
plain `kotlin("jvm")` module — there is no Kotlin Multiplatform plugin and no platform target
to register. Adding a platform means migrating back to KMP, not adding a hook.

When adding a new hook, add it to `BaseConventionPlugin` first, then override in the
relevant plugin classes. Do not add hooks that only one plugin needs — put that logic
directly in the plugin class instead.

## Plugin Comparison Cheat-Sheet

| | `application` | `feature` | `library` | `di` |
|---|---|---|---|---|
| `kotlin("jvm")` + `java-library` | ✓ | ✓ | ✓ | — |
| Compose plugins | ✓ | ✓ | — | — |
| Compose deps | ✓ | ✓ | — | — |
| `jvmToolchain(21)` + `explicitApi()` | ✓ | ✓ | ✓ | — |
| `compose.desktop.currentOs` | ✓ | — | — | — |
| `kotlin-test` on `test` | — | ✓ | ✓ | — |
| Koin deps | — | — | — | ✓ |

`compose.desktop.currentOs` lives only on `ApplicationConventionPlugin` (`compose-application`)
— it's the Skiko native renderer, needed only by the module that actually opens a window.
`feature`/`library` modules just compile against the Compose APIs. The runnable entry point is
`:desktop-application`, which applies `kotlin("jvm")` directly (no convention plugin — see its
own `CLAUDE.md`).

## Adding a New Convention Plugin

1. Create `source/<type>/<Name>ConventionPlugin.kt` — package `dev.rectify.io.convention.source.<type>`.
2. Extend `BaseConventionPlugin`, override only the hooks you need.
3. Register in `build.gradle.kts` under `gradlePlugin { plugins { ... } }` with a fully-qualified
   `implementationClass`.
4. Add an entry to the `libs.versions.toml` `[plugins]` section if consumers use `alias()`.
5. Update `README.md` (Registered Plugins table + Plugin Comparison cheat-sheet).

## Modifying an Existing Plugin

- `configurePlugin()` — add/remove plugin IDs here only. Never call `apply()` elsewhere.
- `configureJvm()` — `jvmToolchain(21)` and `explicitApi()`; nothing else belongs here.
- `configureDependencies()` — uses the `implementation`/`api`/`testImplementation` helpers in
  `core/ext/Project.kt`, never a `sourceSets { }` block.

## DiConventionPlugin Rules

- Always apply alongside `library` or `feature`. Never standalone.
- `koin-core` is `api()` — intentional. Consumers need it at compile time.
- Do not call `explicitApi()` here — the parent plugin already does it.

## moduleName Edge Cases

`Project.moduleName` strips `:` and converts `-` to `.`. Two edge cases to know:
- Root project (`path == ":"`) → returns `""`. Guard: convention plugins must never be
  applied to the root project.
- Modules with numeric segments (e.g. `:data-ble-v2`) → `data.ble.v2` — valid namespace.

## Build Commands

```bash
./gradlew :build-logic:convention:assemble   # compile plugins
./gradlew :build-logic:convention:test       # run plugin tests
```

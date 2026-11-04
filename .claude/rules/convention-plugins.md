---
paths:
  - "build-logic/**"
  - "*/build.gradle.kts"
---

# Convention plugins (build-logic)

Four plugins under `dev.rectify.io.convention`, all extending `BaseConventionPlugin`
(Template Method — 3 hooks, fixed order: `configurePlugin` → `configureJvm` →
`configureDependencies`; no-op hooks are skipped silently). Every module is a plain JVM
module — see `.claude/rules/platform-coverage.md`.

| | `application` | `feature` | `library` | `di` |
|---|---|---|---|---|
| `kotlin("jvm")` + `java-library` | ✓ | ✓ | ✓ | — |
| Compose plugins | ✓ | ✓ | — | — |
| `jvmToolchain(21)` + `explicitApi()` | ✓ | ✓ | ✓ | — |
| Compose deps | ✓ | ✓ | — | — |
| `compose.desktop.currentOs` + `kotlinx-coroutines-swing` | ✓ | — | — | — |
| `kotlin-test` on `test` | — | ✓ | ✓ | — |
| Koin deps | — | — | — | ✓ |

- `DiConventionPlugin` only adds dependencies (`koin-core` as `api()`, `koin-compose`/
  `koin-viewmodel` as `implementation()`) — it applies no plugin IDs. It **must** be applied
  alongside `library` or `feature`; applied alone the `api`/`implementation` configurations
  it writes to do not exist.
- `java-library` is applied explicitly because `kotlin("jvm")` alone does not guarantee the
  `api` configuration, and several modules use `api(projects.x)`.
- Module build files declare dependencies in a plain top-level `dependencies { }` block —
  there is no `kotlin { sourceSets { } }` indirection any more.
- `Project.moduleName` strips `:` and converts `-` to `.` from the Gradle project path. It
  returns `""` for the root project — convention plugins must never be applied there.

**Adding a new convention plugin:** create `source/<type>/<Name>ConventionPlugin.kt`
extending `BaseConventionPlugin`, register it in `build-logic/convention/build.gradle.kts`
under `gradlePlugin { plugins { ... } }` with a fully-qualified `implementationClass`, then
update this file and `build-logic/convention/README.md`.

**Adding a new hook to `BaseConventionPlugin`:** add it there first, as a no-op default, then
override it only in the plugin classes that need it — don't add a hook that only one plugin
will ever use; put that logic directly in the plugin class instead.

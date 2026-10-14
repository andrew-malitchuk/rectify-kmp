---
paths:
  - "build-logic/**"
  - "*/build.gradle.kts"
---

# Convention plugins (build-logic)

Four plugins under `dev.rectify.io.convention`, all extending `BaseConventionPlugin`
(Template Method — 8 hooks, fixed order: `configurePlugin` → `configureAndroidPlatform` →
`configureIOsPlatform` → `configureDesktopPlatform` → `configureCommonDependencies` →
`configureAndroidDependencies` → `configureIOsDependencies` → `configureDesktopDependencies`;
no-op hooks are skipped silently). Android + iOS + Desktop (JVM) targets exist — see
`.claude/rules/platform-coverage.md` for what "Desktop" does and does not mean here.

| | `application` | `feature` | `library` | `di` |
|---|---|---|---|---|
| KMP + AGP KMP library plugin | ✓ | ✓ | ✓ | — |
| Compose plugins | ✓ | ✓ | — | — |
| `androidResources` | ✓ | ✓ | — | — |
| `jvm("desktop")` target | ✓ | ✓ | ✓ | — |
| `compose.desktop.currentOs` + `kotlinx-coroutines-swing` (desktopMain) | ✓ | — | — | — |
| iOS framework `baseName` | hardcoded `application_compose` | from `moduleName` | from `moduleName` | — |
| Koin deps | — | — | — | ✓ |

- `DiConventionPlugin` only adds dependencies (`koin-core` as `api()`, `koin-compose`/
  `koin-viewmodel` as `implementation()`, plus `koin-android`/`koin-android-compose` on
  `androidMain`) — it applies no plugin IDs and registers no targets. It **must** be applied
  alongside `library` or `feature`; applied alone it fails with `Extension of type
  'KotlinMultiplatformExtension' does not exist`.
- `configureAndroidDependencies()` in `feature`/`application` adds Compose UI tooling to the
  `androidRuntimeClasspath` configuration, not `debugImplementation` — that configuration name
  doesn't exist on the `com.android.kotlin.multiplatform.library` AGP 9.0 DSL.
- `Project.moduleName` strips `:` and converts `-` to `.` from the Gradle project path. It
  returns `""` for the root project (`path == ":"`) — convention plugins must never be applied
  to the root project, or the build fails with an empty Android namespace.
- SDK versions (`minSdk`/`targetSdk`/`compileSdk`) always come from
  `libs.findVersion("minSdk"/"targetSdk")` inside the plugin — never hardcode a version number
  in a module's own `build.gradle.kts`.
- Signing is configured inline in `aos-application/build.gradle.kts`, not centralised in a
  convention plugin — AGP 9.0's `com.android.kotlin.multiplatform.library` doesn't expose
  `signingConfigs` on `CommonExtension`, so there's nowhere else to put it.

**Adding a new convention plugin:** create
`source/<type>/<Name>ConventionPlugin.kt` extending `BaseConventionPlugin`, register it in
`build-logic/convention/build.gradle.kts` under `gradlePlugin { plugins { ... } }` with a
fully-qualified `implementationClass`, add a `[plugins]` entry to `libs.versions.toml` if
consumers use `alias()`, then update this file and
`build-logic/convention/README.md` (plugin comparison table).

**Adding a new hook to `BaseConventionPlugin`:** add it there first, as a no-op default, then
override it only in the plugin classes that need it — don't add a hook that only one plugin
will ever use; put that logic directly in the plugin class instead.

# Platform coverage: Android + iOS + Desktop (JVM)

This project ships **Android, iOS, and Desktop (JVM)**. Convention plugins (`build-logic/convention`)
register `iosArm64()`, `iosSimulatorArm64()`, and `jvm("desktop")` targets, alongside the
Android target AGP adds automatically, in every module's `configureIOsPlatform()` /
`configureDesktopPlatform()` hooks. There is no `macosX64`/`macosArm64`/`linuxX64` target and no
`macosMain` source set anywhere in the repo — "Desktop" here means the current-OS JVM target
only (via Compose Multiplatform's `compose.desktop.currentOs`), not a cross-compiled native
target.

- **Library/feature modules** (`LibraryConventionPlugin`, `FeatureConventionPlugin`) register
  `jvm("desktop")` with no extra dependencies — the `commonMain` dependencies declared via
  `configureCommonDependencies()` resolve for the desktop variant automatically through Gradle's
  KMP dependency substitution.
- **`ApplicationConventionPlugin`** (`compose-application`) additionally wires
  `compose.desktop.currentOs` (Skiko native renderer for the host OS) and
  `kotlinx-coroutines-swing` onto `desktopMain` via `configureDesktopDependencies()` — required
  to actually run a window, not just compile against Compose Desktop APIs.
- **There is no Android-application-equivalent packaged module baked into the KMP target
  itself.** Just like Android needs a separate `com.android.application` module
  (`:aos-application`) on top of the KMP library `:compose-application`, Desktop needs its own
  plain-JVM runnable module: `:desktop-application` (applies `kotlin("jvm")` +
  `composeMultiplatform` + `composeCompiler` directly, no convention plugin — it is not a KMP
  module). It hosts `main()`, which calls `initKoin()` (there is no `Application.onCreate()` on
  desktop to do this implicitly) then `dev.rectify.io.source.window.runDesktopApp()`
  (`compose-application`'s `desktopMain`).
- `expect`/`actual` platform code (`DatabaseBuilder`, `SettingsProvider`) has a `desktopMain`
  actual for every `commonMain` expect — see `.claude/rules/database.md` and
  `.claude/rules/preferences.md`. Room's KSP code generation needs an explicit `kspDesktop`
  configuration entry (alongside `kspAndroid`/`kspIosArm64`/`kspIosSimulatorArm64`) in any
  module using `@Database`/`@ConstructedBy`.

Some KDoc/README prose written earlier in this project's history claims "Android, iOS, macOS,
and Desktop JVM targets" — that was already stale before this template existed. It is no longer
stale for Desktop specifically (Desktop is real now), but **macOS/Linux native targets still do
not exist** — don't trust an unverified doc comment over the actual `BaseConventionPlugin` hook
list or a `find <module>/src -type d` listing.

If you add a macOS or Linux **native** target on top of this:
1. Add `configureNativeDesktopPlatform()` (name it to avoid clashing with the existing
   `configureDesktopPlatform` JVM hook) to `BaseConventionPlugin` first — new hooks are added
   there, not improvised per-plugin.
2. Override it in whichever convention plugins need it (likely `library`, `feature`, and the
   `application` plugin feeding `compose-application`).
3. Also check `ios-application`'s Xcode scheme is untouched — this only affects the Gradle/KMP
   side.

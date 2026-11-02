# Platform coverage: Desktop (JVM) only

This project is a **plain JVM project**. Every module applies `kotlin("jvm")` — there is no
Kotlin Multiplatform plugin, no `commonMain`/`desktopMain` split, no `expect`/`actual`, and
no Android, iOS, macOS or Linux target anywhere. Sources live in the classic layout:

```
<module>/src/main/kotlin/   — production code
<module>/src/test/kotlin/   — tests
```

- **Library/feature modules** (`LibraryConventionPlugin`, `FeatureConventionPlugin`) apply
  `kotlin("jvm")` + `java-library`, pin `jvmToolchain(21)` and enforce `explicitApi()`.
  Feature modules additionally apply Compose Multiplatform and its Compose dependency set.
- **`ApplicationConventionPlugin`** (`compose-application`) additionally wires
  `compose.desktop.currentOs` (the Skiko renderer for the host OS) and
  `kotlinx-coroutines-swing` — required to actually run a window, not just compile against
  Compose Desktop APIs.
- **The runnable module is separate from the UI module.** `:compose-application` is a
  library; the plain-JVM `:desktop-application` (applies `kotlin("jvm")` +
  `composeMultiplatform` + `composeCompiler` directly, no convention plugin) hosts `main()`,
  which calls `initKoin()` then `dev.rectify.io.source.window.runDesktopApp()`.
  There is no second runnable module: the MCP endpoint is served by the app itself.
- Compose Multiplatform runs perfectly well on a JVM-only project — it is used here for the
  renderer and the component library, not for multiplatform reach.

**"Android" and "iOS" still appear throughout the codebase as DOMAIN concepts** — Rectify is
a desktop tool that mirrors Android devices over `adb` and iOS simulators over
`xcrun simctl`/`idb` (`data-device-api`, `data-device-impl`, `presentation-feature-mirror`).
`AdbHostSource`, `IosHostSource`, `ANDROID_SOURCE`/`IOS_SOURCE` Koin qualifiers,
`SimctlDevicesParser` and `IdbTargetsParser` are **the product**, not build targets. Don't
confuse the two: the app runs only on desktop, and the devices it talks to are Android phones
and iOS simulators.

The root project is still named `rectify-kmp`, which is why Compose-resources accessors are
generated under `rectify_kmp.presentation_core_localisation.generated.resources`. That is a
name, not a target.

If you ever need a second platform, that is a migration back to Kotlin Multiplatform, not a
flag: re-add the KMP plugin in the convention plugins, split `src/main` back into
`commonMain` + a platform source set, and add a runnable module for the new platform.

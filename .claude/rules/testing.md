# Testing

`kotlin.test` is wired **once**, in `LibraryConventionPlugin` and `FeatureConventionPlugin`
(`build-logic/convention`), via each plugin's existing `configureCommonDependencies()` —
no dedicated `BaseConventionPlugin` hook. Every KMP library/feature module therefore gets a
working `commonTest` source set for free. **Never add a test dependency module-by-module.**

`:compose-application` applies the *application* plugin and deliberately gets no test wiring —
that is why anything needing a unit test (e.g. window constants) lives in `presentation-core-ui`
rather than in `desktopMain`.

The `junit` catalog entry is still declared-but-unused and is a candidate for deletion
(see `version-catalog-hygiene.md`); `kotlin-test` reuses the existing `kotlin` version ref and
resolves its JVM/desktop variant automatically, so plain `kotlin.test` assertions in `commonTest`
are all that is needed.

## Running tests

The desktop target is the one that runs:

```shell
./gradlew :presentation-core-styling:desktopTest :presentation-core-ui:desktopTest \
          :data-preference-impl:desktopTest :data-repository-impl:desktopTest
```

Prefer this over `./gradlew check` or `./gradlew build`, which additionally build every module's
Android and iOS targets. **But do run a cross-target build before declaring work done** — a
`commonTest` file compiles for *every* target, and Kotlin/Native is stricter than the JVM. A
backtick test name containing a comma, an apostrophe or `:` compiles fine on desktop and fails
`compileTestKotlinIosArm64` with `Name contains illegal characters`. A green desktop suite does
not prove the tests compile everywhere:

```shell
./gradlew :presentation-core-ui:build :presentation-core-styling:build
```

## What is worth testing

- **Pure, side-effect-free logic first** — this is where the existing suite lives:
  `clampPanelWidth`, `shortcutToTab`, `ShellTab`/`ShellWindowDefaults` constants, `ShellState`
  transitions, `ModelResourceMapper` implementations (`toModel`/`toResource` are plain functions),
  and preference round-trips against `multiplatform-settings-test`'s `MapSettings`.
- **Design-token contrast** — `presentation-core-styling`'s `PaletteContrastTest` sweeps both
  shipped palettes. It must live in that module's own `commonTest` because both palettes are
  `internal`, and it must composite the alpha tokens (`line`, `line2`, `hover`, `seg`, `codeBg`)
  over their base surface before measuring, or it reports nonsense. Composited hairlines at 8–14%
  alpha cannot reach WCAG 3:1 by construction; they are recorded as frozen baselines with
  `// WAIVED:` comments rather than asserted into a permanently red suite.
- **Don't add a test per `*UseCaseImpl` by rote** — they're thin `resultLauncher` wrappers around a
  repository call; testing `resultLauncher` itself once covers the pattern. Test a use case
  directly only when it has its own branching logic beyond "call the repository."
- **No Compose UI-test harness exists** (no `runComposeUiTest`, no `ui-test-junit4`). Rendering is
  covered by `@Preview` pairs in light and dark. Introducing a UI-test harness is a real
  infrastructure decision, not a casual addition.

Still unverified by anything and good next targets: `resultLauncher`'s `Failure` passthrough vs.
`errorMapper` mapping behaviour, and `AppNavigatorImpl.navigate`'s `ClearTask` ordering
(add-before-clear).

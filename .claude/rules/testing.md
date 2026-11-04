# Testing

`kotlin.test` is wired **once**, in `LibraryConventionPlugin` and `FeatureConventionPlugin`
(`build-logic/convention`), via each plugin's existing `configureCommonDependencies()` —
no dedicated `BaseConventionPlugin` hook. Every library/feature module therefore gets a
working `src/test` source set for free. **Never add `kotlin-test` itself module-by-module.**

A module may still declare a test dependency of its own when the need is genuinely local —
`data-preference-impl` adds `multiplatform-settings-test` and `kotlinx-coroutines-test` because
only it fakes a settings backend and only it tests suspending sources. The rule is about the
*shared* harness, not about banning local fixtures.

`:compose-application` applies the *application* plugin and deliberately gets no test wiring —
that is why anything needing a unit test (e.g. window constants) lives in `presentation-core-ui`
rather than in `:compose-application`.

The `junit` catalog entry is still declared-but-unused and is a candidate for deletion
(see `version-catalog-hygiene.md`); `kotlin-test` reuses the existing `kotlin` version ref and
resolves its JVM variant automatically, so plain `kotlin.test` assertions in `src/test` are all
that is needed.

## Running tests

This is a plain JVM project with one target, so there is no cross-target compile step and no
Kotlin/Native strictness on backtick test names. `./gradlew build` compiles everything and
RUNS every suite — it is the honest "everything passes" command:

```shell
./gradlew build          # everything
./gradlew :presentation-core-ui:test   # one module
```

Earlier versions of this rule warned that a comma or a colon in a backtick test name compiles
on the JVM and fails `compileTestKotlinIosArm64`. That constraint died with the iOS target;
names are bound only by what the JVM accepts.

## What is worth testing

- **Pure, side-effect-free logic first** — this is where the existing suite lives:
  `clampPanelWidth`, `shortcutToTab`, `ShellTab`/`ShellWindowDefaults` constants, `ShellState`
  transitions, `ModelResourceMapper` implementations (`toModel`/`toResource` are plain functions),
  and preference round-trips against `multiplatform-settings-test`'s `MapSettings`.
- **Design-token contrast** — `presentation-core-styling`'s `PaletteContrastTest` sweeps both
  shipped palettes. It must live in that module's own `src/test` because both palettes are
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
`errorMapper` mapping behaviour.

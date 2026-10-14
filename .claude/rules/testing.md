# Testing

**No test exists anywhere in this repo today** (zero files under any `*Test*` source set, and
the convention plugins wire no `commonTest`/`kotlin.test` dependency) — there's no existing
pattern to extend. The `junit` catalog entry is unused for the same reason the `ktor-*` ones
are: declared, never wired to a module. Treat the following as a starting point to establish,
not a convention already in force:

- Wire `kotlin.test` into `commonTest` once, in `LibraryConventionPlugin`/`FeatureConventionPlugin`
  (`build-logic/convention`), so every module gets it for free — don't add test dependencies
  module-by-module.
- The easiest, highest-value first targets are the pure, side-effect-free pieces already shaped
  for it: `ModelResourceMapper` implementations (`toModel`/`toResource` are plain functions),
  `resultLauncher`'s `Failure` passthrough vs. `errorMapper` mapping behavior, and
  `AppNavigatorImpl.navigate`'s `ClearTask` ordering (add-before-clear) — all are deterministic,
  Android/iOS-independent, and currently unverified by anything.
- Don't add a test per `*UseCaseImpl` by rote — they're thin `resultLauncher` wrappers around a
  repository call; testing `resultLauncher` itself once covers the pattern. Test a use case
  directly only when it has its own branching logic beyond "call the repository."

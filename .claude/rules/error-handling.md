---
paths:
  - "domain-*/**"
  - "presentation-feature-*/**"
---

# Error handling (end-to-end)

The error path crosses three layers; don't short-circuit it by catching and swallowing an
exception partway through:

1. **domain-core** — every error is (or is mapped to) a `Failure` subtype:
   `Failure.Technical.{Database,Platform,Network,Preference}(cause)` for infrastructure
   failures, `Failure.Logic.{NotFound,Business(message)}` for business-rule failures.
2. **domain-usecase-impl** — every `*UseCaseImpl` body runs inside
   `resultLauncher(errorMapper = Failure.Technical::<Kind>) { ... }`. A thrown `Failure`
   propagates unchanged; any other `Throwable` is mapped through `errorMapper` first. The
   use case's `invoke()` always returns `Result<T>` — it never throws past this boundary.
3. **presentation-feature-x** — the `ViewModel` calls `.onFailure { postSideEffect(XSideEffect.ShowError(messageId)) }`
   (or equivalent) on the `Result`; `XSideEffect.ShowError(val messageId: Int)` is a `data class`
   carried in every feature's `XSideEffect` sealed class. The `Screen` renders it in
   `collectSideEffect` as `snackbarHostState.showSnackbar(title = "Error", duration = StackedSnackbarDuration.Short)`.

**Known gap, not a pattern to copy:** every current `ShowError` handler ignores `messageId` and
shows a hardcoded, unlocalized `"Error"` title (see `AboutScreen`, `OnboardingScreen`,
`HomeScreen`, `SettingsScreen`). If you wire up real error messages, resolve `messageId` through
`stringResource()` (see `.claude/rules/localisation.md`) instead of adding another hardcoded
string — don't treat the current shortcut as the convention to extend.

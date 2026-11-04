---
paths:
  - "presentation-feature-*/**"
---

# Orbit MVI screen architecture

`presentation-feature-mirror` is the only feature module left, and it is the reference
shape: four files under `source/<feature>/`. Don't invent a variant — match it even for a
trivial screen.

NOTE: the template screens that used to illustrate this rule (About, Home, Onboarding,
Settings, Splash) were deleted as dead code — nothing rendered them. Mirror is the live
example now, and it has no navigator: the desktop shell (`RectifyWindow`) hosts it directly.

- **`XContract.kt`** — `public data class XState(...)` (UI snapshot, defaults for every field),
  `public sealed class XSideEffect` (one-shot events: navigation, error toasts), `public sealed
  class XIntent` (every user action, as `data object`/`data class`).
- **`XViewModel.kt`** — `class XViewModel(...) : ContainerHost<XState, XSideEffect>, ViewModel()`.
  `override val container: Container<XState, XSideEffect> = container(XState()) { ... }` — the
  trailing lambda is for `init`-time work only. `fun handleIntent(intent: XIntent) = when (intent) { ... }` dispatches to
  private handlers; each handler is `= intent { reduce { state.copy(...) }; postSideEffect(...) }`.
- **`XScreen.kt`** — `@Composable fun XScreen(viewModel: XViewModel = koinViewModel())`. Reads
  `viewModel.collectAsState()`, wires `viewModel.collectSideEffect { effect -> when (effect) {...} }`
  to the snackbar, and forwards intents via `onIntent = viewModel::handleIntent`.
- **`XContent.kt`** — stateless; hosts `XSuccessContent` (+ a loading placeholder if the screen
  has one) and the snackbar host. No Koin here — only `XScreen` resolves dependencies.

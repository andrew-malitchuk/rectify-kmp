---
paths:
  - "presentation-feature-*/**"
---

# Orbit MVI screen architecture

Every `presentation-feature-*` screen (`About`, `Home`, `Onboarding`, `Settings`, `Splash`)
follows the identical four-file shape under `source/<feature>/`. Don't invent a variant —
match it even for a trivial screen:

- **`XContract.kt`** — `public data class XState(...)` (UI snapshot, defaults for every field),
  `public sealed class XSideEffect` (one-shot events: navigation, error toasts), `public sealed
  class XIntent` (every user action, as `data object`/`data class`).
- **`XViewModel.kt`** — `class XViewModel(...) : ContainerHost<XState, XSideEffect>, ViewModel()`.
  `override val container: Container<XState, XSideEffect> = container(XState()) { ... }` — the
  trailing lambda is for `init`-time work only (e.g. `SplashViewModel` resolving the first
  destination). `fun handleIntent(intent: XIntent) = when (intent) { ... }` dispatches to
  private handlers; each handler is `= intent { reduce { state.copy(...) }; postSideEffect(...) }`.
- **`XScreen.kt`** — `@Composable fun XScreen(viewModel: XViewModel = koinViewModel())`. Reads
  `viewModel.collectAsState()`, wires `viewModel.collectSideEffect { effect -> when (effect) {...} }`
  to `LocalAppNavigator.current`/snackbar, and forwards intents via
  `onIntent = viewModel::handleIntent`.
- **`XContent.kt`** — stateless; hosts `XSuccessContent` (+ a loading placeholder if the screen
  has one) and the snackbar host. No Koin, no navigation here — only `XScreen` touches
  `LocalAppNavigator`.

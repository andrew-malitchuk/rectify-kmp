---
paths:
  - "presentation-core-styling/**"
  - "presentation-core-ui/**"
  - "presentation-feature-*/**"
---

# Design tokens (Theme)

- Every token lives behind the `Theme` singleton (`presentation.core.styling.core.Theme`),
  readable only from `@Composable` scope: `Theme.color`, `Theme.spacing`, `Theme.typography`,
  `Theme.cornerToken`, `Theme.stroke`, `Theme.motion`, etc. **Never** write a literal
  `Color(0x...)`, a raw `.dp` value, or a hardcoded `TextStyle` at a call site — if the token
  you need doesn't exist yet, add it to the relevant `Theme*` data class instead of inlining
  a value.
- `AppTheme(mode, reducedMotion) { content() }` is the single provider — it resolves the
  light/dark palette and supplies every `Local*` composition local in one
  `CompositionLocalProvider` call. Don't provide a `Local*Theme` token manually anywhere else.
- `presentation-core-ui` enforces strict **atom → molecule → organism** layering: atoms import
  nothing from `molecule/` or `organism/`; molecules import atoms only; organisms import
  molecules and atoms. A new component that breaks this (e.g. an atom reading another atom's
  molecule-level composite) belongs one layer higher.
- `reducedMotion` (see `ThemeMotion`) is resolved once, centrally — components read
  `Theme.motion.durations.*`/`Theme.motion.reducedMotion`, they never branch on a
  passed-in boolean parameter of their own.

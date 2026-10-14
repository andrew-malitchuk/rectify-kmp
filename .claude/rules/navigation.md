---
paths:
  - "presentation-core-navigation-api/**"
  - "presentation-core-navigation-impl/**"
  - "presentation-feature-*/**"
---

# Navigation

- `presentation-core-navigation-api` is the contract, never imported for its implementation:
  `Destination` is a `sealed class : NavKey`, every case `@Serializable`; `AppNavigator`
  declares `navigate(destination, options)` / `popBackStack()` / `backAction()` plus the
  `NavOptions` enum (`Default`, `SingleTop`, `ClearTask`); `LocalAppNavigator` /
  `LocalBackAction` are composition locals that throw immediately if read outside a
  `NavigationHost` — a missing navigator is a programming error, not a state to handle gracefully.
- `presentation-core-navigation-impl` holds the only implementation and must never be imported
  by a `presentation-feature-*` module. `AppNavigatorImpl` backs onto a `NavBackStack<NavKey>`;
  `ClearTask` **adds the new destination before clearing the old ones** — `NavDisplay` observes
  the back stack via snapshots and throws if it is ever transiently empty, so the order is not
  cosmetic.
- Adding a destination is a five-step checklist, every step required: add the case to
  `Destination`; register it in `navSavedStateConfiguration` (`NavSavedStateConfig.kt`) — a
  missing registration throws `SerializationException` on process-death restore, not at compile
  time; add an `entry<Destination.YourNew> { ... }` branch in `NavigationHost`; add the feature
  module's Gradle dependency if the screen is new; update the destination tables in both
  `presentation-core-navigation-api/README.md` and `presentation-core-navigation-impl/README.md`.

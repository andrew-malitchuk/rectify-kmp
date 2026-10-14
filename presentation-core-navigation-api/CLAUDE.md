# presentation-core-navigation-api — Claude Instructions

## Module Purpose

Navigation contract module. Defines the sealed `Destination` hierarchy, the `AppNavigator`
interface, and the composition locals that give any composable in the tree access to navigation
without importing the navigation runtime. Implementation lives in `presentation-core-navigation-impl`.

## Package Layout Convention

```
presentation.core.navigation.api/
├── core/
│   └── composition/
│       └── NavCompositionLocal.kt   — internal composition locals (public vals)
└── source/
    └── destination/
        ├── AppNavigator.kt
        └── Destination.kt
```

`core/` hosts the composition infrastructure (locals) that is not a primary public API surface
but is public because composables in feature modules need it.

## Destination Hierarchy

`Destination` is a `sealed class : NavKey`. Every subclass is `@Serializable` for
Navigation 3 saved-state process-death recovery.

**Adding a new destination — checklist:**
1. Add the `data object` or `data class` inside `Destination`.
2. Register it in `navSavedStateConfiguration` in `presentation-core-navigation-impl`.
3. Add an `entry<Destination.YourNew>` branch in `NavigationHost`.
4. Update the `Destination Routing` table in `presentation-core-navigation-impl/README.md`.
5. Update the destination table in this module's `README.md`.

## NavOptions

| Option | Behaviour |
|--------|-----------|
| `Default` | Push destination onto back stack |
| `SingleTop` | Push only if the top of the stack differs |
| `ClearTask` | Add destination, then clear all previous entries |

`ClearTask` adds the new destination **before** clearing the old entries to prevent the
NavDisplay from ever observing an empty back stack (which throws at runtime).

## Composition Locals

`LocalAppNavigator` and `LocalBackAction` both use `compositionLocalOf` with a hard error
as the default — accessing them outside a `NavigationHost` throws immediately. This is
intentional: a missing navigator is a programming error, not a recoverable state.

## What Belongs Here vs presentation-core-navigation-impl

| Belongs here | Belongs in navigation-impl |
|---|---|
| `Destination` sealed hierarchy | `AppNavigatorImpl` |
| `AppNavigator` interface + `NavOptions` | `NavigationHost` composable |
| `LocalAppNavigator`, `LocalBackAction` | `navSavedStateConfiguration` |
| — | Feature screen wiring |

## Build

```shell
./gradlew :presentation-core-navigation-api:build
```

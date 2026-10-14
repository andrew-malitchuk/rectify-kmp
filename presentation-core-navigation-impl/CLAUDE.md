# presentation-core-navigation-impl — Claude Instructions

## Module Purpose

Navigation implementation module. Wires the `AppNavigator` interface, the `NavigationHost` composable, and saved-state serialization. All feature screens are registered here. The contract (destinations, interfaces, composition locals) lives in `presentation-core-navigation-api`; this module must never be imported by feature modules.

## Package Layout

```
presentation.core.navigation.impl/
└── source/
    ├── host/
    │   └── NavigationHost.kt          — root composable; maps Destination → screen
    ├── navigator/
    │   └── AppNavigatorImpl.kt        — Navigation 3 AppNavigator implementation
    └── serialization/
        └── NavSavedStateConfig.kt     — polymorphic subclass registration for saved state
```

All declarations live under `source/`. There is no `core/` sub-package in this module.

## Key Files

| File | Role |
|------|------|
| `NavigationHost.kt` | Creates `rememberNavBackStack`, provides `LocalAppNavigator` + `LocalBackAction`, renders `NavDisplay` |
| `AppNavigatorImpl.kt` | Implements `navigate()`, `popBackStack()`, `backAction()` with `NavOptions` logic |
| `NavSavedStateConfig.kt` | `navSavedStateConfiguration` — registers every `Destination` subclass for process-death recovery |

## Adding a New Destination — Checklist

1. Add the `data object` / `data class` in `Destination` (`presentation-core-navigation-api`).
2. Register it in `navSavedStateConfiguration` (this file: `NavSavedStateConfig.kt`).
3. Add an `entry<Destination.YourNew>` branch in `NavigationHost.kt`.
4. Add the feature module dependency in `build.gradle.kts` if the screen comes from a new module.
5. Update the Destination Routing table in `README.md`.
6. Update the destination table in `presentation-core-navigation-api/README.md`.

## NavigationHost Layout

`NavigationHost` uses three nested `Box` containers:

| Box | Purpose |
|-----|---------|
| Outer `Box` with `fillMaxSize + background(canvas)` | Paints the canvas colour behind system bars on edge-to-edge displays |
| Middle `Box` with `statusBarsPadding` | Pushes content below the status bar; navigation bar is **not** padded (screens go edge-to-edge to the bottom) |
| Inner `Box` with `widthIn(max = 600.dp)` | Constrains layout width on tablets and desktop |

Transitions are disabled (`EnterTransition.None togetherWith ExitTransition.None`) — custom animated transitions should be added per-entry.

## ClearTask Safety

`AppNavigatorImpl` implements `ClearTask` by:
1. Adding the new destination to the back stack first.
2. Then removing all previous entries.

This order is critical: `NavDisplay` throws if it ever observes an empty back stack. Reversing the order would momentarily leave the stack empty between the two operations.

## Serialization Requirement

Every `Destination` subclass is `@Serializable` and must be registered in `navSavedStateConfiguration`. Omitting a registration causes a `SerializationException` when the system restores the back stack after process death.

## What Belongs Here vs presentation-core-navigation-api

| Belongs here | Belongs in navigation-api |
|---|---|
| `AppNavigatorImpl` | `AppNavigator` interface + `NavOptions` |
| `NavigationHost` composable | `Destination` sealed hierarchy |
| `navSavedStateConfiguration` | `LocalAppNavigator`, `LocalBackAction` |
| Feature screen wiring | — |

## Build

```shell
./gradlew :presentation-core-navigation-impl:build
```

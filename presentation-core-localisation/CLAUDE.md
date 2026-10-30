# presentation-core-localisation — Claude Instructions

## Module Purpose

Multiplatform locale management. Provides the `AppLocaleProvider` composable that overrides the Compose Resources `ResourceEnvironment` for the entire composition tree, and hosts all shared UI strings. Language switching is instant — no app restart needed.

## Package Layout

```
presentation.core.localisation/
└── source/
    └── provider/
        └── AppLocaleProvider.kt   — composable wrapper + LocalLocalization
```

All declarations live under `source/`. There is no `core/` sub-package.

## Key Files

| File | Role |
|------|------|
| `AppLocaleProvider.kt` | `AppLocaleProvider` composable + `LocalLocalization` composition local |

## Important Implementation Detail

`AppLocaleProvider.kt` carries two file-level annotations:

```kotlin
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@file:OptIn(InternalResourceApi::class)
```

These are **required** because the Compose Resources public API provides no locale-override mechanism. The implementation constructs a `ResourceEnvironment` directly from `LanguageQualifier`, `ScriptQualifier`, `RegionQualifier`, `ThemeQualifier`, and `DensityQualifier` — all internal API. Do not remove these annotations.

## Design Decision: Why Not `key()`?

A `key(languageCode) { content() }` approach would change the locale by destroying and recreating the entire composition, which also destroys the Navigation 3 back stack. `AppLocaleProvider` avoids this by only swapping the `ResourceEnvironment` via `CompositionLocalProvider`, preserving the composition tree across language changes.

## Composition Locals

| Local | Type | Default |
|-------|------|---------|
| `LocalLocalization` | `staticCompositionLocalOf<String>` | `"en"` |

`LocalLocalization` is `staticCompositionLocalOf` — changing the language code causes a full recomposition of all consumers, which is intentional.

## String Resources

All shared strings live in `src/main/composeResources/`:

| Directory | Language |
|-----------|----------|
| `values/strings.xml` | English (default, only language shipped so far) |

Every additional language file must contain identical keys. Missing keys fall back to the default (`values/`) file at runtime.

## Adding a New Language

1. Create `composeResources/values-<code>/strings.xml`.
2. Copy all keys from `values/strings.xml` and translate values.
3. Update the supported-languages table in `README.md`.

NOTE: there is no in-app language picker any more — the settings screen was deleted as dead
code. `ObserveApplicationLanguageUseCase` still drives `AppLocaleProvider`, but nothing
writes the preference.

## Build

```shell
./gradlew :presentation-core-localisation:build
```

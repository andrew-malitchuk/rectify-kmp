# presentation-core-localisation

Multiplatform locale management and shared string resources for Compose Multiplatform.

## Overview

Provides the `AppLocaleProvider` composable that overrides the Compose Resources locale for
the entire composition tree, and hosts all shared UI strings for every supported language.
Language switching takes effect immediately without an app restart.

## Public API

| Symbol | Description |
|--------|-------------|
| `AppLocaleProvider(languageCode, content)` | Composable wrapper; configures `ResourceEnvironment` with the active language code |
| `LocalLocalization` | `staticCompositionLocalOf<String>` — current language code; default `"en"` |

## Supported Languages

| Code | Language | File |
|------|----------|------|
| `en` | English | `composeResources/values/strings.xml` |

Add more languages by following the steps below.

## Usage

```kotlin
// Wrap at the app root (e.g. inside AppTheme)
AppLocaleProvider(languageCode = currentLanguage) {
    // All stringResource() calls resolve to the active locale
    Text(text = stringResource(Res.string.app_name))
}

// Read the current locale code from anywhere in the tree
val lang = LocalLocalization.current
```

## Implementation Note

`AppLocaleProvider` uses `@OptIn(InternalResourceApi::class)` to construct a custom
`ResourceEnvironment`. This is necessary because the Compose Resources public API does not
expose a locale-override mechanism. The `@file:Suppress("INVISIBLE_MEMBER")` suppression is
also required. This approach preserves the composition tree (and therefore the navigation
back-stack) across language changes — a `key()`-based approach would destroy and recreate it.

## Adding a New Language

1. Create `composeResources/values-<code>/strings.xml`.
2. Copy all keys from `values/strings.xml` and translate values.
3. Add the language code to the supported-languages table above.
4. Surface it in the `SettingsViewModel` language list.

## Dependencies

No internal project dependencies. Uses `compose.resources` from the feature convention plugin.

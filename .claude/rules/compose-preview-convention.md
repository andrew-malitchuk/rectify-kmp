---
paths:
  - "presentation-feature-*/**"
  - "presentation-core-ui/**"
---

# Compose `@Preview` convention

Every screen-level content composable (`*SuccessContent`, `*Content`, `*PlaceholderContent` —
the files you'd actually want to see rendered in Android Studio) ends with a private preview
function:

```kotlin
@Preview
@Composable
private fun XSuccessContentPreview() {
    AppTheme {
        XSuccessContent(
            state = XState(),
            onIntent = {},
        )
    }
}
```

- `private`, named `<ComposableName>Preview`, always wraps in `AppTheme { }` so tokens resolve —
  a preview outside `AppTheme` crashes on the first `Theme.*` access.
- Pass a default-constructed `State` (and `onIntent = {}`/a no-op lambda for every callback) —
  previews don't wire a real `ViewModel` or Koin.
- Internal sub-components that aren't a screen's root content (e.g. a single page inside a
  pager, like `OnboardingPageContent`) aren't required to carry their own `@Preview` — the
  convention is for the composable you'd navigate to, not every composable that composes it.
- This is purely a development-time aid: `@Preview` composables are never part of the shipped
  app and are never referenced by `NavigationHost` or any `Destination`.

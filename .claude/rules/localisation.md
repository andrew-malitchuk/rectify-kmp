---
paths:
  - "presentation-core-localisation/**"
  - "presentation-feature-*/**"
---

# Localisation

- `AppLocaleProvider(languageCode) { content() }` (`presentation.core.localisation.source.provider`)
  overrides the Compose Resources locale for the composition subtree it wraps, so every
  `stringResource()` call inside resolves against `languageCode` without destroying and
  recreating the composition (a `key()`-based approach would also blow away the Navigation 3
  back stack — that's why this exists instead).
- It does this by constructing a `ResourceEnvironment` directly from internal Compose Resources
  API (`LanguageQualifier`, `ScriptQualifier`, `RegionQualifier`, `ThemeQualifier`,
  `DensityQualifier`), gated behind `@file:OptIn(InternalResourceApi::class)` and
  `@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")`. These annotations are required,
  not incidental — the public Compose Resources API has no locale-override mechanism. Don't
  remove them, and if a Compose Multiplatform upgrade changes `ResourceEnvironment`'s
  constructor shape, update the call site rather than working around the compiler error.
- Only `values/strings.xml` (English) exists today — `composeResources/values-<code>/` for any
  other language is not shipped yet. Adding one: create `composeResources/values-<code>/strings.xml`
  with every key from the default file translated, then add the code to
  `SettingsViewModel`'s language list (`languageOptions`/`languageCodes` in `SettingsState`).
  Missing keys in a non-default file fall back to `values/` at runtime — but don't rely on that
  as a substitute for translating a key.
- String key convention: `<feature>_<element>` (`onboarding_welcome_title`,
  `settings_erase_confirm_body`, `about_version_label`). Keep a key only as long as something
  references it — this codebase has previously accumulated unused keys (`onboarding_title`,
  `settings_copyright`, etc.) that nothing read; grep for a key's usage before assuming it's live.

---
paths:
  - "data-preference-api/**"
  - "data-preference-impl/**"
---

# Preferences

- `PreferenceSource<T>` (`data.preference.api.source.datasource.base`) is the generic contract:
  `suspend fun getData(): T`, `suspend fun setData(data: T)`, `fun observeData(): Flow<T>`.
  One typed interface per preference (`ThemePreferenceSource : PreferenceSource<ThemePreference>`)
  in `data-preference-api`, one `*SourceImpl` in `data-preference-impl`.
- Implementations hold a `MutableStateFlow` seeded from the backing store on construction;
  `setData()` writes through and updates the flow, `observeData()` returns the flow,
  `getData()` re-reads fresh from storage so the first call is always correct even before
  anything has subscribed.
- Storage key convention: `pref_<name>` (`pref_theme`, `pref_language`,
  `pref_onboarding_completed`, `pref_unit_system`), declared as a `const val KEY_* `in the impl.
  **Never change an existing key** — it silently orphans the stored value on every existing
  install; add a new key and migrate instead.
- Backend is hidden behind `multiplatform-settings`: Desktop uses `PreferencesSettings` backed
  by `java.util.prefs.Preferences` (node `dev.rectify.io`), registered by
  `Module.provideSettings()` — see `.claude/rules/platform-coverage.md`.
- Adding a new preference: contract in `data-preference-api` first (interface + model
  implementing `Resource`), then the `*SourceImpl` in `data-preference-impl`, then register it
  in `DataPreferenceImplModule` — the data source alone isn't wired into the domain layer until
  a `ModelResourceMapper` and a `ConfigureRepository` method expose it (see
  `.claude/rules/mappers.md` and `.claude/rules/domain-layer.md`).

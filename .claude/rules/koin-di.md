---
paths:
  - "**/di/**"
  - "compose-application/**"
---

# Koin DI

- `initKoin()` (`compose-application/source/di/InitKoin.kt`) registers every module in one
  fixed, load-order-significant list: **data → domain → presentation**. Registering a
  presentation module before the domain use case it depends on throws
  `No definition found for type '...'` at the first injection site — this is a real runtime
  failure, not a lint warning. When you add a new feature module's DI, append it to the end of
  its layer group, don't reorder existing entries.
- One Koin module per Gradle module, in `di/<ModuleName>Module.kt`, exposing exactly one
  `public val <camelCaseName>Module: Module = module { ... }` (e.g.
  `presentationFeatureAboutModule`, `dataPreferenceImplModule`).
- Binding style depends on whether the constructor needs raw `get<T>()` calls:
  - No qualifiers needed → `singleOf(::Impl) bind Interface::class` (data/domain layer) or
    `viewModelOf(::ViewModel)` (presentation layer) — see
    `presentationFeatureAboutModule` for the minimal case.
  - Constructor needs explicit typed `get<T>()` resolution (overlapping types, multiple
    use-case params) → `viewModel { XViewModel(param = get<ParamUseCase>(), ...) }`, see
    `presentationFeatureSettingsModule`.
- Feature modules never construct a `ViewModel` directly or call `get()` outside a Koin
  `module { }` block — injection is always resolved through `koinViewModel()` at the `Screen`.

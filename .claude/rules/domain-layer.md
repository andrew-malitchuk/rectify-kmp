---
paths:
  - "domain-core/**"
  - "domain-repository-api/**"
  - "domain-usecase-api/**"
  - "domain-usecase-impl/**"
---

# Domain layer

- `domain-core` — zero dependencies. Holds the `Model` marker interface, the sealed `Failure`
  hierarchy (`Technical.{Database,Platform,Network,Preference}`, `Logic.{NotFound,Business}`),
  and cross-cutting enums (`ThemeModel`, the `device`/`history` model groups). Nothing here may
  depend on a repository, a use case, or a platform type.
- `domain-repository-api` — interfaces only (`ConfigureRepository`). Methods return domain
  models or `Flow<Model?>`, never data-layer resource types.
- `domain-usecase-api` — one interface per use case, exactly one
  `public suspend operator fun invoke(...)`. Suspend, one-shot operations return `Result<T>`
  (or `Optional = Result<Unit>` from `domain.usecase.api.source.monad.Optional`); reactive
  operations return a bare `Flow<T>` — never `Flow<Result<T>>`.
- `domain-usecase-impl` — one `*UseCaseImpl` per interface. Every suspend body goes through
  `resultLauncher(errorMapper = Failure.Technical::<Kind>) { ... }`
  (`domain.usecase.impl.core.resultLauncher`) instead of raw `runCatching`:
  it re-throws a `Failure` unchanged and maps any other `Throwable` through `errorMapper`, so
  infrastructure exceptions always arrive at the caller as the right `Failure` subtype.
- Dependency direction is one-way and enforced by module boundaries:
  `presentation-feature-* → domain-usecase-api ← domain-usecase-impl → domain-repository-api ← data-repository-impl`.
  Presentation and `domain-usecase-impl` must never import a `data-*-impl` type directly.

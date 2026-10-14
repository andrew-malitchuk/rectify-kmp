---
paths:
  - "common-core/**"
  - "data-repository-impl/**"
---

# Mappers / mapping

- `common-core.Mapper<I, O>` (`common.core.source.mapper.Mapper`) — the one mapping contract
  used everywhere: a single-method `fun interface`. Use a lambda for one-liners, a named
  `object` when the mapping has a fallback rule worth naming (see `ThemePreferenceMapper`).
- `data-repository-impl.ModelResourceMapper<MODEL, RESOURCE>`
  (`data.repository.impl.core.mapper.base`) — pairs `toModel: Mapper<RESOURCE, MODEL>` with
  `toResource: Mapper<MODEL, RESOURCE>` on one `internal object` per preference/resource type,
  living in `data-repository-impl/core/mapper/`. The repository impl calls these; the mapper
  never lives inside the `Resource` class or the domain `Model` itself.
- Unknown/invalid data-layer values are handled in `toModel`, falling back to a safe default
  (e.g. `ThemePreferenceMapper.toModel` defaults to `ThemeModel.Light` for an unrecognized
  stored string) — a mapper never throws.
- No type parameter bounds on `MODEL`/`RESOURCE` — plain types like `String` are valid as
  `MODEL` without needing to implement a marker interface (see `LanguagePreferenceMapper`).

---
paths:
  - "data-*/**"
  - "gradle/libs.versions.toml"
---

# Network

**No network layer exists in this codebase yet.** `ktor-client-*` entries are declared in
`gradle/libs.versions.toml` but no module's `build.gradle.kts` consumes them and no file
imports `io.ktor.*` — this is unused scaffolding, not an active pattern. Do not point to it as
a reference, and don't assume a network client exists anywhere in the dependency graph.

When you add one, follow the existing data-layer shape instead of inventing a new one:

- `data-network-api` — client-agnostic interfaces only (no Ktor types leak out), resource
  models implementing `data.core.source.resource.Resource`, mirroring `data-database-api`'s
  api/impl split (see `.claude/rules/database.md`).
- `data-network-impl` — the actual `HttpClient` wiring, with the JVM engine
  (`ktor-client-cio`) registered in Koin the same way `data-database-impl` registers its
  `DatabaseBuilder`.
- Failures funnel through `Failure.Technical.Network(cause)` (already defined in
  `domain-core`, currently unused — this is its intended consumer).
- Convert `data-network-api` resources to domain models via a `ModelResourceMapper` in
  `data-repository-impl/core/mapper/`, same as every other data source — never let a network
  DTO cross into `domain-usecase-impl` directly (see `.claude/rules/mappers.md`).

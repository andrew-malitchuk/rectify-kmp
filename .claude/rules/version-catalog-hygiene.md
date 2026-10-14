---
paths:
  - "gradle/libs.versions.toml"
---

# Version catalog hygiene

**A `gradle/libs.versions.toml` entry existing does not mean anything consumes it.** This
catalog has repeatedly accumulated dead entries — version pins and library aliases declared but
never referenced by any module's `build.gradle.kts` or imported in any `.kt` file:

- `ktor-client-*` / `ktor-serialization-*` — no network layer exists (see
  `.claude/rules/network.md`).
- `couchbase-lite` / `couchbase-lite-ktx` (`dev.kotbase:couchbase-lite`) — unused.
- `play-services-home` / `play-services-home-types` — unused, and the latter's real Maven
  coordinates couldn't even be verified against public indices.
- `junit` — no test source set exists yet (see `.claude/rules/testing.md`).

Before treating a catalog entry as "this is part of the architecture" or extending a pattern
because the dependency is pinned, verify it's actually wired in:

```shell
grep -rln "libs\.<alias>" --include="*.gradle.kts" .      # is it declared as a dependency anywhere?
grep -rln "import <package>" --include="*.kt" .            # is it ever imported?
```

If you add a dependency and don't wire it into a module in the same change, you're creating the
next one of these — either wire it immediately or don't add the catalog entry yet.

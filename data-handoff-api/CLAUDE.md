# data-handoff-api — Claude Instructions

## Module Purpose

The cross-process handoff between Rectify (desktop UI, writer) and `:mcp-application` (the MCP
server Claude Code spawns, reader). Defines the wire envelope (`AnnotationEnvelope` and friends)
and the `AnnotationHandoff` contract, and ships the one real implementation,
`FileSlotAnnotationHandoff` — a single atomic file drop under `~/.rectify/handoff`.

Deliberately Koin-free: no `di` convention plugin is applied. Applying it would expose
`koin-core` as `api()`, landing Koin on `:mcp-application`'s classpath for a server that has no
other use for a DI container and cares about spawn-time startup cost. The Koin binding for
`AnnotationHandoff` lives in `presentation-feature-mirror`, which already has Koin wired.

## Package Layout Convention

```
data.handoff.api/
├── core/
│   └── slot/
│       └── FileSlotAnnotationHandoff.kt   — internal, desktopMain only
└── source/
    ├── envelope/
    │   └── AnnotationEnvelope.kt          — AnnotationEnvelope, EnvelopeDevice,
    │                                         EnvelopeAnnotation, HandoffImage
    └── handoff/
        ├── AnnotationHandoff.kt           — the one public interface, commonMain
        └── FileSlotFactory.kt             — public fileSlotAnnotationHandoff(), desktopMain
```

`core/` exists only because `FileSlotAnnotationHandoff` is `internal` — the slot implementation
is a desktop-only, untrusted-filesystem detail that nothing outside this module should construct
directly. Everything else is `source/`.

## On-disk layout

```
~/.rectify/handoff/                 — root, owner-only POSIX permissions where supported
~/.rectify/handoff/latest.json      — THE slot; written as latest.json.tmp then
                                       Files.move(ATOMIC_MOVE, REPLACE_EXISTING)
~/.rectify/handoff/<sentAtMs>/frame.png
~/.rectify/handoff/<sentAtMs>/crop-1.png … crop-N.png
```

Images are written BEFORE `latest.json` moves into place, so a reader that observes `latest.json`
always sees a complete image set. Every sibling numeric directory `latest.json` does not name is
pruned after each successful publish — exactly one send's images are retained at a time. No
history, no ring buffer, no expiry: F-9's territory, out of scope here.

## Why one interface, not two

Preparation's wording suggested a separate publish-only and read-only interface. This module
ships one `AnnotationHandoff` instead (a declared, gate-approved deviation): the writer and the
reader never share a process, so a second interface would add a type with no caller that could
ever confuse the two roles. The KDoc on each member says which side calls it.

## Build

```shell
./gradlew :data-handoff-api:build
./gradlew :data-handoff-api:desktopTest
```

## Platform coverage

`AnnotationHandoff` (the interface) is `commonMain`; `FileSlotAnnotationHandoff` and
`fileSlotAnnotationHandoff()` are `desktopMain`-only — this module has no `androidMain` or
`iosMain` source set at all. The Android/iOS implementation
(`UnsupportedAnnotationHandoff`, a no-op that answers `latest() = null` and throws on
`publish`/`resolve`) deliberately lives in `presentation-feature-mirror`, not here, so this
module never needs a Koin-bearing `expect`/`actual` pair of its own — see that module's
`core/unsupported/` and `CLAUDE.md`.

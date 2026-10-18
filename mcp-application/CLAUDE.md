# mcp-application — Claude Instructions

## Module Purpose

The MCP (Model Context Protocol) server Claude Code spawns over stdio to read back the most
recent annotation batch Rectify published. Plain-JVM (not KMP), a second OS process entirely
separate from `:desktop-application` — it is started and killed by Claude Code, not by the
user. One tool: `get_latest_annotation`. No business logic beyond JSON-RPC framing and rendering
an `AnnotationEnvelope` into the tool's result document — reading the slot itself is
`:data-handoff-api`'s job.

## Bespoke Gradle — NFR-15 declared deviation

No convention plugin in `build-logic/convention` models a plain-JVM module — `application`,
`feature`, `library` and `di` are all built around `KotlinMultiplatformExtension`.
`:desktop-application` set and documented this precedent first (plain `kotlin("jvm")` +
`composeMultiplatform` + `composeCompiler`, no convention plugin); `:mcp-application` extends it
by one, applying `kotlin("jvm")` + `kotlin.serialization` + the stock `application` plugin
directly. Declared, not discovered — gate-approved under decision 6. Do not "fix" this by
inventing a fifth convention plugin for a module count of one.

The classpath is deliberately pinned to `:data-handoff-api` + `kotlinx-serialization-json`
only — no Compose, no Koin, no Skiko. Claude Code pays this module's JVM startup cost on every
tool call it spawns the server for, so the dependency graph stays as small as the one real job
(reading the handoff slot and answering JSON-RPC) requires. This is also why `main()` is declared
`suspend fun main()` (a stdlib-supported JVM entry point since Kotlin 1.3) rather than wrapped in
`kotlinx.coroutines.runBlocking`: `:data-handoff-api` declares `kotlinx-coroutines-core` as
`implementation`, so it is on this module's runtime classpath transitively but never its compile
classpath, and `runBlocking` would not resolve here. A `suspend main` needs nothing beyond what
`AnnotationHandoff`'s own suspend functions already require.

## Key Files

| File | Role |
|---|---|
| `source/entry/Main.kt` | `suspend fun main()` — stdin read loop, the sole `writeFrame` (stdout) call site, `logStderr` for every diagnostic |
| `core/rpc/McpDispatcher.kt` | Pure `String -> String?` JSON-RPC dispatcher: `initialize`, `tools/list`, `tools/call` → `get_latest_annotation`; `MCP_PROTOCOL_VERSION` and `SUPPORTED_ENVELOPE_VERSION` constants |
| `build.gradle.kts` | `application { mainClass, applicationName = "rectify-mcp" }` — `installDist` → `build/install/rectify-mcp/bin/rectify-mcp` |

## Wiring this into Claude Code

1. Build the launcher: `./gradlew :mcp-application:installDist`. This must run before Claude
   Code can spawn anything — there is no packaged distribution and no generated config entry
   (see the repo-root `.mcp.json.example` for why F-7 documents this step instead of automating
   it).
2. Copy `.mcp.json.example` (repo root) to `.mcp.json` and add it to your Claude Code MCP
   config. The `command` is the relative `installDist` launcher path; make it absolute if Claude
   Code is launched from a working directory other than the repo root.
3. Restart/reconnect Claude Code so it re-reads the MCP config and respawns the server.

**Handshake failure** (Claude Code reports it can't talk to the `rectify` server, or rejects its
`initialize` reply): check `MCP_PROTOCOL_VERSION` in `core/rpc/McpDispatcher.kt` first. It is
echoed verbatim in the `initialize` result's `protocolVersion` field; a version the client
doesn't recognize is the most common cause of a handshake rejection, ahead of anything about
the handoff slot or the environment.

**`get_latest_annotation` says "No annotation has been sent yet." right after a Resolve & Send
you watched succeed**: check the resolved handoff root logged once to stderr at startup
(`Main.kt`'s `logStderr("rectify-mcp: reading the handoff slot under $root")`) before anything
else. The server resolves `~/.rectify/handoff` from `System.getProperty("user.home")` at the
moment it starts, independently of wherever Rectify's desktop process resolved the same path —
a mismatched `$HOME` (sandboxed shells, a different user running Claude Code, etc.) means the
server is watching a slot Rectify never wrote to, and the symptom is indistinguishable from
"nothing was sent" without that log line.

## Build / Test Commands

```shell
./gradlew :mcp-application:installDist   # produces build/install/rectify-mcp/bin/rectify-mcp
./gradlew :mcp-application:test          # kotlin.test, declared locally — no convention plugin wires it here
```

`kotlin.test` is normally wired once by the `library`/`feature` convention plugins
(`.claude/rules/testing.md`); this module has neither, so `build.gradle.kts` declares
`testImplementation(kotlin("test"))` itself — a sanctioned exception, not a pattern to copy
into a module that does have a convention plugin available.

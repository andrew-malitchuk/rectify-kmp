# data-mcp-impl — Claude Instructions

## Module Purpose

The MCP (Model Context Protocol) endpoint Rectify serves **itself**, on the loopback interface,
for as long as the app window is open. One tool: `get_latest_annotation`. No business logic
beyond JSON-RPC framing, HTTP plumbing and rendering an `AnnotationEnvelope` into the tool's
result document — reading the slot itself is `:data-handoff-api`'s job.

This module used to be `:mcp-application`, a second OS process that Claude Code spawned over
stdio. That shape cost the user a separate download, a JDK of their own and an absolute path in
a config file, to buy a property almost nobody used: answering while Rectify was closed. The
dispatcher survived the move unchanged — it was always a pure `String -> String?` function, and
that is exactly why swapping its transport cost nothing.

## Bespoke Gradle

No convention plugin: `application`, `feature`, `library` and `di` all wire a Compose/lifecycle
dependency set this module has no use for. `:desktop-application` set and documented this
precedent first. Do not "fix" this by inventing a fifth convention plugin.

The classpath is `:data-handoff-api` + `kotlinx-serialization-json` + `kotlinx-coroutines-core`,
and the HTTP server is the JDK's own `com.sun.net.httpserver` — no Ktor, no embedded servlet
container. A loopback endpoint answering one POST at a time does not need one, and every
dependency added here lands in the packaged app.

**`jdk.httpserver` must stay in `nativeDistributions { modules(...) }`** in
`desktop-application/build.gradle.kts`. jpackage builds the bundled runtime from the modules it
can see, and dropping that line produces an app that opens its window and then cannot serve the
protocol at all — a failure `./gradlew run` will never reproduce, because it runs on the full
system JDK.

## Key Files

| File | Role |
|---|---|
| `core/rpc/McpDispatcher.kt` | Pure `String -> String?` JSON-RPC dispatcher: `initialize`, `tools/list`, `tools/call` → `get_latest_annotation`; `MCP_PROTOCOL_VERSION` and `SUPPORTED_ENVELOPE_VERSION` constants |
| `core/http/McpHttpHandler.kt` | The Streamable HTTP surface: POST → one JSON response, 202 for notifications, 403 for a cross-origin `Origin`, 405 for GET |
| `source/server/McpEndpoint.kt` | `startMcpEndpoint()` — binds loopback, scans past a taken port, exposes `url` and `registerCommand` |

## Transport Notes

- **Only the POST half of Streamable HTTP is implemented.** The optional server-to-client `GET`
  stream is refused with 405 rather than left open: Rectify never initiates, so a client that
  hangs waiting for events is a client waiting forever.
- **Notifications answer 202 with no body**, not an empty 200. A client that gets a 200 expects a
  response object to parse.
- **The `Origin` check is load-bearing security**, not ceremony — see the KDoc on
  `McpHttpHandler`. Its test lives in `McpEndpointHttpTest` and uses `java.net.http` precisely
  because `HttpURLConnection` silently drops the `Origin` header, which would make the test pass
  against a server with no guard at all.
- **Binding is loopback-only.** Never change `InetAddress.getLoopbackAddress()` to a wildcard
  bind to "make it reachable" — that publishes the user's screenshots to the network.

## The status-bar card

`McpStatus`/`McpConnectCard` in `:compose-application`'s `RectifyWindow.kt` render the chip and
the card it opens. The card SHOWS the `claude mcp add` line and copies it on a button, rather
than copying silently on click: the user can read what they are about to paste into a shell, and
the port in it is the one this instance bound, which is not always 7391.

`AboveAnchorPositionProvider` places the card above the chip. The anchor sits at the very bottom
of the window, so the default below-the-anchor placement would put it off-screen; a position
provider gets the measured popup size up front, which a negative-offset hack does not.

## Lifecycle

`:compose-application`'s `runDesktopApp()` owns it: start before `application { }`, stop in the
`finally`. A failure to bind is logged and the app opens anyway with `MCP off` in the status bar
— a degraded app, not a dead one.

package dev.rectify.io.mcp.source.entry

import data.handoff.api.source.handoff.fileSlotAnnotationHandoff
import dev.rectify.io.mcp.core.rpc.McpDispatcher
import java.nio.file.Paths

/**
 * JVM entry point for the MCP server Claude Code spawns over stdio.
 *
 * Declared `suspend` — a stdlib-supported JVM entry point since Kotlin 1.3 — rather than wrapped
 * in `kotlinx.coroutines.runBlocking`: `:mcp-application`'s classpath is pinned to
 * `:data-handoff-api` + `kotlinx-serialization-json` only (the server's spawn-time budget), and
 * `:data-handoff-api` declares `kotlinx-coroutines-core` as `implementation`, so it sits on this
 * module's RUNTIME classpath transitively but never its COMPILE classpath — `runBlocking` would
 * not resolve here. A `suspend main` needs nothing beyond what `AnnotationHandoff`'s own suspend
 * functions already require.
 *
 * Reads lines from stdin until EOF, feeds each to [McpDispatcher.handle], and writes every
 * non-null reply via [writeFrame] — the ONLY function in this module that touches stdout. Every
 * diagnostic, including the resolved handoff root logged once at startup, goes to stderr via
 * [logStderr] instead.
 */
suspend fun main() {
    // Passed explicitly rather than taken from the factory's default so the path logged below
    // is provably the path actually read.
    val root = Paths.get(System.getProperty("user.home"), ".rectify", "handoff")
    logStderr("rectify-mcp: reading the handoff slot under $root")

    val dispatcher = McpDispatcher(fileSlotAnnotationHandoff(root))
    generateSequence(::readlnOrNull).forEach { line ->
        dispatcher.handle(line)?.let(::writeFrame)
    }
    // stdin EOF: the client went away. Fall off main — exit 0, quietly.
}

/** The sole stdout write site in the module — one JSON-RPC response per line (TAC-9). */
internal fun writeFrame(line: String) {
    println(line)
    System.out.flush()
}

/** Every diagnostic the server emits goes here, never to stdout — stdout is the wire. */
internal fun logStderr(message: String) {
    System.err.println(message)
}

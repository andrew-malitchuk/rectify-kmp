package dev.rectify.io.mcp.core.rpc

import data.handoff.api.source.envelope.AnnotationEnvelope
import data.handoff.api.source.handoff.AnnotationHandoff
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.coroutines.cancellation.CancellationException

/**
 * The MCP (Model Context Protocol) wire version this server speaks, echoed verbatim in every
 * `initialize` reply (TAC-1).
 */
internal const val MCP_PROTOCOL_VERSION: String = "2024-11-05"

/**
 * The highest [data.handoff.api.source.envelope.AnnotationEnvelope.version] this server knows how
 * to read. An envelope with a newer value is reported as a tool error naming both numbers
 * (TAC-7) rather than guessed at.
 */
internal const val SUPPORTED_ENVELOPE_VERSION: Int = 1

/** The one tool this server exposes (TAC-3). */
internal const val GET_LATEST_ANNOTATION_TOOL: String = "get_latest_annotation"

/** Shown to the agent when the slot has never been written. */
private const val EMPTY_SLOT_TEXT: String = "No annotation has been sent yet."

/** How the user re-installs this server after an envelope-format bump (TAC-7). */
private const val REBUILD_COMMAND: String = "./gradlew :mcp-application:installDist"

/** The wire: one compact line per response, never pretty — see [McpDispatcher.handle]. */
private val wire: Json = Json

/**
 * The tool result's inner document is pretty-printed for the agent's benefit; it rides the wire
 * as an escaped STRING value, so its newlines never reach stdout as real line breaks (TAC-9).
 */
private val document: Json = Json { prettyPrint = true }

/**
 * A pure `String -> String?` JSON-RPC dispatcher over [handoff] — the entire testability
 * strategy for this module (see `McpDispatcher*Test`). `null` means "this line was a
 * notification; write nothing" (TAC-2).
 *
 * Dispatches `initialize`, `notifications/initialized`, `tools/list`, and `tools/call` ->
 * `get_latest_annotation`. An unknown method or an unparseable line never throws — both become a
 * JSON-RPC error response so the caller's read loop can keep reading (TAC-4), and a handoff that
 * throws while answering a tool call is caught the same way rather than escaping (TAC-8).
 *
 * [nowMs] is injected (defaulting to [System.currentTimeMillis]) purely so the tool result's
 * `ageSeconds` is deterministic in tests — the wall clock cannot be moved by virtual time, and
 * `sentAtMs` must stay comparable across the two separate OS processes either side of the
 * handoff slot.
 */
internal class McpDispatcher(
    private val handoff: AnnotationHandoff,
    private val nowMs: () -> Long = System::currentTimeMillis,
) {

    /**
     * Answers one JSON-RPC line. NOTHING thrown below here reaches the caller's read loop —
     * not a malformed shape, not a handoff fault, not a path added later: every throwable
     * becomes a response (TAC-4/TAC-8). Only cancellation propagates, because a cancelled
     * server is not a server that owes an answer.
     */
    suspend fun handle(line: String): String? {
        val parsed = runCatching { wire.parseToJsonElement(line) }.getOrElse {
            return errorResponse(JsonNull, code = -32700, message = "Parse error: ${it.message}")
        }
        // Well-formed JSON of the wrong SHAPE is -32600, not -32700: it parsed fine.
        val request = parsed as? JsonObject
            ?: return invalidRequest(JsonNull, "a request must be a JSON object")
        val id = request["id"] ?: JsonNull
        return try {
            dispatch(request, id)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Throwable) {
            errorResponse(id, code = -32603, message = "Internal error: ${failure.message}")
        }
    }

    /**
     * The method table. Every field is read with a NON-throwing accessor ([stringOrNull]) —
     * `jsonPrimitive`/`jsonObject` are casts, and a cast on untrusted input is how a single bad
     * frame used to kill the session.
     */
    private suspend fun dispatch(request: JsonObject, id: JsonElement): String? {
        val method = request["method"].stringOrNull()
            ?: return invalidRequest(id, "\"method\" must be a string")

        // Notifications carry no id and expect no reply at all, per the JSON-RPC spec.
        if (method.startsWith("notifications/")) return null

        return when (method) {
            "initialize" -> result(id) {
                put("protocolVersion", MCP_PROTOCOL_VERSION)
                putJsonObject("capabilities") { putJsonObject("tools") {} }
                putJsonObject("serverInfo") {
                    put("name", "rectify")
                    put("version", "0.4")
                }
            }

            "tools/list" -> result(id) { putJsonArray("tools") { add(toolDescriptor()) } }

            "tools/call" -> {
                val params = request["params"] as? JsonObject
                    ?: return invalidRequest(id, "\"params\" must be an object")
                when (val tool = params["name"].stringOrNull()) {
                    GET_LATEST_ANNOTATION_TOOL -> wrap(id, getLatestAnnotation())
                    null -> invalidRequest(id, "\"params.name\" must be a string")
                    else -> errorResponse(id, code = -32601, message = "Tool not found: $tool")
                }
            }

            else -> errorResponse(id, code = -32601, message = "Method not found: $method")
        }
    }

    /**
     * The tool's own result object. Every failure mode is a TOOL error (`isError: true`), not a
     * JSON-RPC error: the call itself succeeded, the payload is what is unusable — and the agent
     * sees the reason as text rather than a protocol fault.
     */
    private suspend fun getLatestAnnotation(): JsonObject {
        val envelope = runCatching { handoff.latest() }.getOrElse {
            return toolResult("Could not read the handoff slot: ${it.message}", isError = true)
        }
        return when {
            envelope == null -> toolResult(EMPTY_SLOT_TEXT, isError = false)

            envelope.version > SUPPORTED_ENVELOPE_VERSION -> toolResult(
                "Envelope format version ${envelope.version} is newer than this server supports " +
                    "($SUPPORTED_ENVELOPE_VERSION). Rebuild :mcp-application ($REBUILD_COMMAND).",
                isError = true,
            )

            // render() resolves image names through the handoff, which REJECTS a name that is
            // not bare (path traversal, NFR-4) by throwing — that rejection is a tool error
            // like any other, never an exception out of the caller's read loop (TAC-8).
            else -> runCatching {
                toolResult(document.encodeToString(JsonObject.serializer(), render(envelope)), isError = false)
            }.getOrElse {
                toolResult("Could not read the handoff slot: ${it.message}", isError = true)
            }
        }
    }

    /** The documented success document — metadata, prompt and hierarchy inline, images by ref. */
    private fun render(envelope: AnnotationEnvelope): JsonObject = buildJsonObject {
        put("version", envelope.version)
        put("sessionId", envelope.sessionId)
        put("sentAtMs", envelope.sentAtMs)
        put("ageSeconds", (nowMs() - envelope.sentAtMs) / 1_000L)
        putJsonObject("device") {
            put("id", envelope.device.id)
            put("label", envelope.device.label)
            put("widthPx", envelope.device.widthPx)
            put("heightPx", envelope.device.heightPx)
        }
        put("annotationCount", envelope.annotations.size)
        put("prompt", envelope.prompt)
        putJsonObject("hierarchy") {
            put("available", envelope.hierarchyAvailable)
            put("xml", envelope.hierarchyXml)
        }
        putJsonObject("frame") {
            put("ref", handoff.resolve(envelope, envelope.framePath))
            put("widthPx", envelope.device.widthPx)
            put("heightPx", envelope.device.heightPx)
        }
        putJsonArray("annotations") {
            envelope.annotations.forEach { annotation ->
                add(
                    buildJsonObject {
                        put("index", annotation.index)
                        putJsonObject("rect") {
                            put("left", annotation.left)
                            put("top", annotation.top)
                            put("right", annotation.right)
                            put("bottom", annotation.bottom)
                        }
                        put("comment", annotation.comment)
                        // A missing crop is a degraded payload, not a field the agent must
                        // branch on — the key is omitted rather than emitted as null.
                        annotation.cropPath?.let { put("cropRef", handoff.resolve(envelope, it)) }
                    },
                )
            }
        }
    }
}

/** The single `tools/list` entry (TAC-3) — no arguments, hence an empty-object input schema. */
private fun toolDescriptor(): JsonObject = buildJsonObject {
    put("name", GET_LATEST_ANNOTATION_TOOL)
    put(
        "description",
        "Returns the most recent annotation batch sent from Rectify: the generated prompt, the " +
            "device's view-hierarchy XML, and file:// references to the full frame screenshot " +
            "and each per-annotation crop.",
    )
    putJsonObject("inputSchema") {
        put("type", "object")
        putJsonObject("properties") {}
        put("additionalProperties", false)
    }
}

/** An MCP tool result: one text block plus the error flag the agent branches on. */
private fun toolResult(text: String, isError: Boolean): JsonObject = buildJsonObject {
    putJsonArray("content") {
        add(
            buildJsonObject {
                put("type", "text")
                put("text", text)
            },
        )
    }
    put("isError", isError)
}

/** `{"jsonrpc":"2.0","id":…,"result":{…}}`, serialized compactly onto one line (TAC-9). */
private fun result(id: JsonElement, body: JsonObjectBuilder.() -> Unit): String = wire.encodeToString(
    JsonObject.serializer(),
    buildJsonObject {
        put("jsonrpc", "2.0")
        put("id", id)
        putJsonObject("result") { body() }
    },
)

private fun wrap(id: JsonElement, result: JsonObject): String = wire.encodeToString(
    JsonObject.serializer(),
    buildJsonObject {
        put("jsonrpc", "2.0")
        put("id", id)
        put("result", result)
    },
)

/** A shape the server understood as JSON but cannot act on — well-formed, wrong structure. */
private fun invalidRequest(id: JsonElement, detail: String): String =
    errorResponse(id, code = -32600, message = "Invalid request: $detail")

/** The string behind [this], or `null` for a missing key, a non-string primitive, or any
 * composite — the non-throwing counterpart of `jsonPrimitive.content`. */
private fun JsonElement?.stringOrNull(): String? = (this as? JsonPrimitive)?.takeIf { it.isString }?.content

/** `{"jsonrpc":"2.0","id":…,"error":{"code":…,"message":…}}` — the loop keeps reading (TAC-4). */
private fun errorResponse(id: JsonElement, code: Int, message: String): String = wire.encodeToString(
    JsonObject.serializer(),
    buildJsonObject {
        put("jsonrpc", "2.0")
        put("id", id)
        putJsonObject("error") {
            put("code", code)
            put("message", message)
        }
    },
)

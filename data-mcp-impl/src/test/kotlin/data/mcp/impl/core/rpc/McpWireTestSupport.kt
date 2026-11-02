package data.mcp.impl.core.rpc

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/** A JSON-RPC request line for a method that needs no params. */
internal fun request(id: Int, method: String): String = buildJsonObject {
    put("jsonrpc", "2.0")
    put("id", id)
    put("method", method)
}.toString()

/** A JSON-RPC notification line — no `id` key, per spec. */
internal fun notification(method: String): String = buildJsonObject {
    put("jsonrpc", "2.0")
    put("method", method)
}.toString()

/** A `tools/call` request line invoking [name] with empty arguments. */
internal fun toolCallRequest(id: Int, name: String = GET_LATEST_ANNOTATION_TOOL): String = buildJsonObject {
    put("jsonrpc", "2.0")
    put("id", id)
    put("method", "tools/call")
    putJsonObject("params") {
        put("name", name)
        putJsonObject("arguments") {}
    }
}.toString()

/** Parses [response] and returns its `result` object, failing loudly if either is missing. */
internal fun resultOf(response: String?): JsonObject =
    Json.parseToJsonElement(requireNotNull(response) { "expected a non-null response" })
        .jsonObject["result"]
        ?.jsonObject
        ?: error("response had no \"result\" field: $response")

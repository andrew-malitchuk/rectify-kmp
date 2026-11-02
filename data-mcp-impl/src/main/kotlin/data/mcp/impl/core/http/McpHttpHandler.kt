package data.mcp.impl.core.http

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import data.mcp.impl.core.rpc.McpDispatcher
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.nio.charset.StandardCharsets

private const val STATUS_OK: Int = 200
private const val STATUS_ACCEPTED: Int = 202
private const val STATUS_FORBIDDEN: Int = 403
private const val STATUS_METHOD_NOT_ALLOWED: Int = 405
private const val STATUS_PAYLOAD_TOO_LARGE: Int = 413

/**
 * A single JSON-RPC frame is the whole request body, and the largest one this server can
 * legitimately receive is a `tools/call` with no arguments. Anything past this is not a client
 * of ours, so it is refused before it is read into memory.
 */
private const val MAX_REQUEST_BYTES: Long = 1L * 1024 * 1024

/**
 * The MCP Streamable HTTP endpoint, serving [dispatcher] over one path.
 *
 * Only the subset this server actually needs is implemented: `POST` carries one JSON-RPC
 * request and gets one JSON response back. The optional server-to-client `GET` stream is
 * deliberately absent — Rectify never initiates; the agent asks and Rectify answers — so `GET`
 * is refused rather than left hanging, which is what tells a client to stop waiting for events
 * that will never come.
 *
 * **Why an [Origin] check on a loopback socket.** A page in the user's browser can POST to
 * `127.0.0.1` from any origin. Without this guard, any website the user visits while Rectify is
 * open could read their latest annotation batch — screenshots of whatever they were debugging —
 * straight out of this endpoint. A browser always sends `Origin` on a cross-site request, so
 * refusing a non-local one closes that door; a missing `Origin` is a non-browser client such as
 * Claude Code itself.
 */
internal class McpHttpHandler(private val dispatcher: McpDispatcher) : HttpHandler {

    override fun handle(exchange: HttpExchange) {
        exchange.use {
            if (!originIsLocal(exchange)) {
                respond(exchange, STATUS_FORBIDDEN, "cross-origin request refused")
                return
            }
            when (exchange.requestMethod.uppercase()) {
                // DELETE ends a session in the Streamable HTTP spec. This server keeps no
                // session state, so acknowledging is the whole implementation.
                "DELETE" -> respond(exchange, STATUS_OK, "")
                "POST" -> handlePost(exchange)
                else -> respond(exchange, STATUS_METHOD_NOT_ALLOWED, "only POST is supported")
            }
        }
    }

    private fun handlePost(exchange: HttpExchange) {
        val declared = exchange.requestHeaders.getFirst("Content-Length")?.toLongOrNull()
        if (declared != null && declared > MAX_REQUEST_BYTES) {
            respond(exchange, STATUS_PAYLOAD_TOO_LARGE, "request body too large")
            return
        }
        val body = exchange.requestBody.readNBytes(MAX_REQUEST_BYTES.toInt() + 1)
        if (body.size > MAX_REQUEST_BYTES) {
            respond(exchange, STATUS_PAYLOAD_TOO_LARGE, "request body too large")
            return
        }
        val request = String(body, StandardCharsets.UTF_8)
        // The dispatcher answers everything, including malformed input, as a JSON-RPC error —
        // it is the same pure function the stdio server used, and it still never throws.
        val response = runBlocking { dispatcher.handle(request) }
        if (response == null) {
            // A notification. The spec wants 202 with no body, not an empty 200.
            exchange.sendResponseHeaders(STATUS_ACCEPTED, -1L)
        } else {
            writeJson(exchange, response)
        }
    }

    private fun writeJson(exchange: HttpExchange, json: String) {
        val bytes = json.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(STATUS_OK, bytes.size.toLong())
        exchange.responseBody.write(bytes)
    }

    private fun respond(exchange: HttpExchange, status: Int, message: String) {
        val bytes = message.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "text/plain; charset=utf-8")
        exchange.sendResponseHeaders(status, if (bytes.isEmpty()) -1L else bytes.size.toLong())
        if (bytes.isNotEmpty()) exchange.responseBody.write(bytes)
    }
}

/** True when the request carries no `Origin` at all, or one that resolves to the loopback host. */
internal fun originIsLocal(exchange: HttpExchange): Boolean {
    val origin = exchange.requestHeaders.getFirst("Origin") ?: return true
    val host = runCatching { URI(origin).host }.getOrNull() ?: return false
    return host == "127.0.0.1" || host == "::1" || host == "localhost"
}

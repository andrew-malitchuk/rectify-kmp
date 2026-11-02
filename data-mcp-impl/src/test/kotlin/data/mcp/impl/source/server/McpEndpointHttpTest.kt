package data.mcp.impl.source.server

import data.mcp.impl.core.rpc.FakeAnnotationHandoff
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The transport, end to end over a real socket — the half the dispatcher tests cannot see.
 *
 * Binds a real [McpEndpoint] on a port well clear of [DEFAULT_MCP_PORT] so a developer running
 * Rectify while the suite runs does not collide with it (and so the suite never silently tests
 * *their* running app instead of its own server).
 */
class McpEndpointHttpTest {

    private lateinit var endpoint: McpEndpoint

    @BeforeTest
    fun start() {
        endpoint = startMcpEndpoint(FakeAnnotationHandoff { null }, preferredPort = 17391)
    }

    @AfterTest
    fun stop() {
        endpoint.stop()
    }

    @Test
    fun initializeOverHttpAnswersWithTheProtocolVersion() {
        val (status, body) = post(REQUEST_INITIALIZE)
        assertEquals(200, status)
        assertTrue(body.contains("\"protocolVersion\""), "initialize must echo a protocol version: $body")
        assertTrue(body.contains("\"rectify\""), "initialize must name the server: $body")
    }

    @Test
    fun toolsListOverHttpAdvertisesTheOneTool() {
        val (status, body) = post(REQUEST_TOOLS_LIST)
        assertEquals(200, status)
        assertTrue(body.contains("get_latest_annotation"), "tools/list must advertise the tool: $body")
    }

    @Test
    fun aNotificationIsAcceptedWithNoBody() {
        // A JSON-RPC notification has no id and earns no reply — over HTTP that is 202, not an
        // empty 200, and getting it wrong leaves the client waiting for a response object.
        val (status, body) = post(REQUEST_NOTIFICATION)
        assertEquals(202, status)
        assertEquals("", body)
    }

    @Test
    fun malformedJsonIsAProtocolErrorNotATransportFailure() {
        val (status, body) = post("{ this is not json")
        assertEquals(200, status)
        assertTrue(body.contains("-32700"), "a parse error must come back as JSON-RPC -32700: $body")
    }

    @Test
    fun aCrossOriginRequestIsRefused() {
        // The guard that stops any web page the user happens to have open from reading their
        // latest annotation batch off the loopback port.
        val (status, _) = post(REQUEST_INITIALIZE, origin = "https://evil.example")
        assertEquals(403, status)
    }

    @Test
    fun aLoopbackOriginIsAllowed() {
        val (status, _) = post(REQUEST_INITIALIZE, origin = "http://127.0.0.1:1234")
        assertEquals(200, status)
    }

    @Test
    fun getIsRefusedRatherThanLeftHanging() {
        val response = send(HttpRequest.newBuilder(URI(endpoint.url)).GET())
        assertEquals(405, response.first)
    }

    /**
     * Deliberately `java.net.http`, not `HttpURLConnection`: the latter silently DROPS an
     * `Origin` header (it is on its restricted list), so the cross-origin test would pass
     * against a server with no guard at all.
     */
    private fun post(body: String, origin: String? = null): Pair<Int, String> {
        val builder = HttpRequest.newBuilder(URI(endpoint.url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
        origin?.let { builder.header("Origin", it) }
        return send(builder)
    }

    private fun send(builder: HttpRequest.Builder): Pair<Int, String> {
        val response = HttpClient.newHttpClient()
            .send(builder.build(), HttpResponse.BodyHandlers.ofString())
        return response.statusCode() to response.body()
    }
}

private const val REQUEST_INITIALIZE: String =
    """{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05",""" +
        """"capabilities":{},"clientInfo":{"name":"test","version":"1"}}}"""

private const val REQUEST_TOOLS_LIST: String = """{"jsonrpc":"2.0","id":2,"method":"tools/list"}"""

private const val REQUEST_NOTIFICATION: String =
    """{"jsonrpc":"2.0","method":"notifications/initialized"}"""

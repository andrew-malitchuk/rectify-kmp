package dev.rectify.io.mcp.core.rpc

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * TAC-1, TAC-2, TAC-3, TAC-4 — the three non-tool JSON-RPC methods plus the two failure shapes.
 * None of these touch the handoff, so a [FakeAnnotationHandoff] that always reports an empty slot
 * is enough; a single shared [dispatcher] instance across calls within a test also stands in for
 * the stdin read loop continuing across lines.
 */
class McpDispatcherProtocolTest {

    private val dispatcher = McpDispatcher(FakeAnnotationHandoff { null })

    @Test
    fun initializeRepliesWithTheNamedProtocolVersionConstant() {
        val response = runSuspendBlocking { dispatcher.handle(request(id = 1, method = "initialize")) }

        val result = Json.parseToJsonElement(requireNotNull(response)).jsonObject["result"]!!.jsonObject
        assertEquals(MCP_PROTOCOL_VERSION, result["protocolVersion"]!!.jsonPrimitive.content)
    }

    @Test
    fun notificationsInitializedReturnsNullAndWritesNothing() {
        val response = runSuspendBlocking { dispatcher.handle(notification("notifications/initialized")) }

        assertNull(response)
    }

    @Test
    fun toolsListContainsExactlyOneToolWithAnEmptyObjectInputSchema() {
        val response = runSuspendBlocking { dispatcher.handle(request(id = 2, method = "tools/list")) }

        val result = Json.parseToJsonElement(requireNotNull(response)).jsonObject["result"]!!.jsonObject
        val tools = result["tools"]!!.jsonArray
        assertEquals(1, tools.size, "expected exactly one tool, got: $tools")

        val tool = tools.single().jsonObject
        assertEquals(GET_LATEST_ANNOTATION_TOOL, tool["name"]!!.jsonPrimitive.content)
        val inputSchema = tool["inputSchema"]!!.jsonObject
        assertEquals("object", inputSchema["type"]!!.jsonPrimitive.content)
        assertTrue(inputSchema["properties"]!!.jsonObject.isEmpty(), "expected an empty properties object")
    }

    @Test
    fun unknownMethodYieldsMinus32601AndTheLoopKeepsReading() {
        val response = runSuspendBlocking { dispatcher.handle(request(id = 5, method = "bogus/method")) }

        val error = Json.parseToJsonElement(requireNotNull(response)).jsonObject["error"]!!.jsonObject
        assertEquals(-32601, error["code"]!!.jsonPrimitive.int)
        assertEquals("Method not found: bogus/method", error["message"]!!.jsonPrimitive.content)

        // the loop keeps reading: a subsequent, valid request still gets answered
        val next = runSuspendBlocking { dispatcher.handle(request(id = 6, method = "tools/list")) }
        assertTrue(requireNotNull(next).contains(GET_LATEST_ANNOTATION_TOOL))
    }

    @Test
    fun unparseableInputYieldsMinus32700WithIdNullAndTheLoopKeepsReading() {
        val response = runSuspendBlocking { dispatcher.handle("not json at all {") }

        val parsed = Json.parseToJsonElement(requireNotNull(response)).jsonObject
        assertEquals(JsonNull, parsed["id"], "expected id to be the JSON null literal")
        val error = parsed["error"]!!.jsonObject
        assertEquals(-32700, error["code"]!!.jsonPrimitive.int)

        val next = runSuspendBlocking { dispatcher.handle(request(id = 7, method = "tools/list")) }
        assertTrue(requireNotNull(next).contains(GET_LATEST_ANNOTATION_TOOL))
    }
}

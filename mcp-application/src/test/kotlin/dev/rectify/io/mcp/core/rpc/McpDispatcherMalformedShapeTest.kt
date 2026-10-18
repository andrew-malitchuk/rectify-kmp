package dev.rectify.io.mcp.core.rpc

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * TAC-4 regression (Stage 6 F-1) — well-formed JSON of an UNEXPECTED SHAPE must answer with a
 * JSON-RPC error and leave the session alive.
 *
 * `jsonPrimitive` and `jsonObject` are throwing CASTS, and the dispatcher used to read `method`
 * and `params.name` through them: `{"jsonrpc":"2.0","id":1,"method":{"x":1}}` escaped into
 * `Main.kt`'s bare read loop and killed the process with exit 1, so every later line on that
 * stdin went unanswered. Each case here asserts both halves — the error response AND that the
 * SAME dispatcher instance still answers a following `tools/list`, which is what the read loop
 * does next.
 */
class McpDispatcherMalformedShapeTest {

    private val dispatcher = McpDispatcher(FakeAnnotationHandoff { null })

    private fun assertInvalidRequestAndTheLoopKeepsReading(line: String, label: String) {
        val response = requireNotNull(runSuspendBlocking { dispatcher.handle(line) }) { "$label: no response" }
        assertFalse(response.contains('\n'), "$label: response contained an embedded newline: $response")

        val error = Json.parseToJsonElement(response).jsonObject["error"]
        requireNotNull(error) { "$label: expected an error response, got: $response" }
        assertEquals(-32600, error.jsonObject["code"]!!.jsonPrimitive.int, "$label: wrong error code")

        val next = runSuspendBlocking { dispatcher.handle(request(id = 99, method = "tools/list")) }
        assertTrue(
            requireNotNull(next).contains(GET_LATEST_ANNOTATION_TOOL),
            "$label: the dispatcher stopped answering after a malformed line",
        )
    }

    @Test
    fun aMethodThatIsAnObjectYieldsMinus32600RatherThanThrowing() =
        assertInvalidRequestAndTheLoopKeepsReading(
            """{"jsonrpc":"2.0","id":1,"method":{"x":1}}""",
            "object method",
        )

    @Test
    fun aMethodThatIsANumberYieldsMinus32600RatherThanThrowing() =
        assertInvalidRequestAndTheLoopKeepsReading(
            """{"jsonrpc":"2.0","id":2,"method":7}""",
            "numeric method",
        )

    @Test
    fun aMissingMethodYieldsMinus32600RatherThanThrowing() =
        assertInvalidRequestAndTheLoopKeepsReading("""{"jsonrpc":"2.0","id":3}""", "absent method")

    @Test
    fun paramsAsAnArrayOnAToolCallYieldsMinus32600RatherThanThrowing() =
        assertInvalidRequestAndTheLoopKeepsReading(
            """{"jsonrpc":"2.0","id":4,"method":"tools/call","params":[1,2]}""",
            "array params",
        )

    @Test
    fun aToolNameThatIsANumberYieldsMinus32600RatherThanThrowing() =
        assertInvalidRequestAndTheLoopKeepsReading(
            """{"jsonrpc":"2.0","id":5,"method":"tools/call","params":{"name":42}}""",
            "numeric tool name",
        )

    @Test
    fun aTopLevelJsonArrayYieldsMinus32600RatherThanThrowing() =
        assertInvalidRequestAndTheLoopKeepsReading("""[{"jsonrpc":"2.0"}]""", "top-level array")

    /** The id is echoed back untouched even from the malformed-shape path, so the client can pair it. */
    @Test
    fun theRequestIdIsEchoedBackOnAMalformedShapeError() {
        val response = runSuspendBlocking { dispatcher.handle("""{"jsonrpc":"2.0","id":11,"method":{"x":1}}""") }

        val parsed = Json.parseToJsonElement(requireNotNull(response)).jsonObject
        assertEquals(11, parsed["id"]!!.jsonPrimitive.int)
    }
}

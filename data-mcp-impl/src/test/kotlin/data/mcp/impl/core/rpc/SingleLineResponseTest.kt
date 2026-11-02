package data.mcp.impl.core.rpc

import kotlin.test.Test
import kotlin.test.assertFalse

/**
 * TAC-9 — the stdout-corruption guard: every line [McpDispatcher.handle] returns must be exactly
 * one line, with no embedded newline or carriage return. A populated-slot tool result embeds a
 * "pretty-printed" JSON document *as a string value* (per plan.json's success-result contract),
 * and `sampleEnvelope`'s prompt itself contains a real `\n` — serializing the OUTER envelope
 * compactly must turn both into escaped `\n` sequences rather than literal line breaks. Checked
 * across every response shape the dispatcher produces, not just one.
 */
class SingleLineResponseTest {

    private fun assertSingleLine(response: String?, label: String) {
        val line = requireNotNull(response) { "$label: expected a non-null response" }
        assertFalse(line.contains('\n'), "$label: response contained an embedded newline: $line")
        assertFalse(line.contains('\r'), "$label: response contained an embedded carriage return: $line")
    }

    @Test
    fun initializeResponseIsOneLine() {
        val dispatcher = McpDispatcher(FakeAnnotationHandoff { null })
        assertSingleLine(runSuspendBlocking { dispatcher.handle(request(id = 1, method = "initialize")) }, "initialize")
    }

    @Test
    fun toolsListResponseIsOneLine() {
        val dispatcher = McpDispatcher(FakeAnnotationHandoff { null })
        assertSingleLine(runSuspendBlocking { dispatcher.handle(request(id = 2, method = "tools/list")) }, "tools/list")
    }

    @Test
    fun unknownMethodErrorResponseIsOneLine() {
        val dispatcher = McpDispatcher(FakeAnnotationHandoff { null })
        assertSingleLine(runSuspendBlocking { dispatcher.handle(request(id = 3, method = "bogus")) }, "unknown method")
    }

    @Test
    fun unparseableInputErrorResponseIsOneLine() {
        val dispatcher = McpDispatcher(FakeAnnotationHandoff { null })
        assertSingleLine(runSuspendBlocking { dispatcher.handle("not json {") }, "unparseable input")
    }

    @Test
    fun emptySlotToolResponseIsOneLine() {
        val dispatcher = McpDispatcher(FakeAnnotationHandoff { null })
        assertSingleLine(runSuspendBlocking { dispatcher.handle(toolCallRequest(id = 4)) }, "empty slot")
    }

    @Test
    fun populatedSlotToolResponseIsOneLineDespiteAMultilinePromptAndPrettyPrintedInnerJson() {
        val dispatcher = McpDispatcher(FakeAnnotationHandoff { sampleEnvelope() })
        assertSingleLine(runSuspendBlocking { dispatcher.handle(toolCallRequest(id = 5)) }, "populated slot")
    }

    @Test
    fun newerVersionToolResponseIsOneLine() {
        val dispatcher = McpDispatcher(FakeAnnotationHandoff { sampleEnvelope(version = SUPPORTED_ENVELOPE_VERSION + 1) })
        assertSingleLine(runSuspendBlocking { dispatcher.handle(toolCallRequest(id = 6)) }, "newer version")
    }

    @Test
    fun throwingHandoffToolResponseIsOneLine() {
        val dispatcher = McpDispatcher(FakeAnnotationHandoff { error("corrupt") })
        assertSingleLine(runSuspendBlocking { dispatcher.handle(toolCallRequest(id = 7)) }, "throwing handoff")
    }
}

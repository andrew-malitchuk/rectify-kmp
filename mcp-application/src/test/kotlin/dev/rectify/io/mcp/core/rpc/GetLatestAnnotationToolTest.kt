package dev.rectify.io.mcp.core.rpc

import data.handoff.api.source.envelope.HandoffImage
import data.handoff.api.source.handoff.fileSlotAnnotationHandoff
import java.nio.file.Files
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * TAC-5, TAC-6, TAC-7, TAC-8 — `tools/call` -> `get_latest_annotation` over an empty, populated,
 * version-mismatched and corrupt slot. The populated-slot shape is asserted against the exact
 * contract in plan.json's "MCP wire — get_latest_annotation success result".
 */
class GetLatestAnnotationToolTest {

    @Test
    fun emptySlotYieldsTheExplicitNoAnnotationMessageWithIsErrorFalse() {
        val dispatcher = McpDispatcher(FakeAnnotationHandoff { null })

        val response = runSuspendBlocking { dispatcher.handle(toolCallRequest(id = 1)) }

        val result = resultOf(response)
        assertEquals(false, result["isError"]!!.jsonPrimitive.boolean)
        val text = result["content"]!!.jsonArray.single().jsonObject["text"]!!.jsonPrimitive.content
        assertEquals("No annotation has been sent yet.", text)
    }

    @Test
    fun populatedSlotYieldsTheDocumentedJsonWithPromptHierarchyAndFileRefs() {
        val envelope = sampleEnvelope()
        val fixedNowMs = envelope.sentAtMs + 42_000L
        val dispatcher = McpDispatcher(FakeAnnotationHandoff { envelope }, nowMs = { fixedNowMs })

        val response = runSuspendBlocking { dispatcher.handle(toolCallRequest(id = 2)) }

        val result = resultOf(response)
        assertEquals(false, result["isError"]!!.jsonPrimitive.boolean)
        val text = result["content"]!!.jsonArray.single().jsonObject["text"]!!.jsonPrimitive.content
        val document = Json.parseToJsonElement(text).jsonObject

        assertEquals(envelope.version, document["version"]!!.jsonPrimitive.int)
        assertEquals(envelope.sessionId, document["sessionId"]!!.jsonPrimitive.content)
        assertEquals(envelope.sentAtMs, document["sentAtMs"]!!.jsonPrimitive.long)
        assertEquals(42, document["ageSeconds"]!!.jsonPrimitive.int)
        assertEquals(1, document["annotationCount"]!!.jsonPrimitive.int)
        assertEquals(envelope.prompt, document["prompt"]!!.jsonPrimitive.content)

        val device = document["device"]!!.jsonObject
        assertEquals(envelope.device.id, device["id"]!!.jsonPrimitive.content)
        assertEquals(envelope.device.label, device["label"]!!.jsonPrimitive.content)
        assertEquals(envelope.device.widthPx, device["widthPx"]!!.jsonPrimitive.int)
        assertEquals(envelope.device.heightPx, device["heightPx"]!!.jsonPrimitive.int)

        val hierarchy = document["hierarchy"]!!.jsonObject
        assertEquals(true, hierarchy["available"]!!.jsonPrimitive.boolean)
        assertEquals(envelope.hierarchyXml, hierarchy["xml"]!!.jsonPrimitive.content)

        val frame = document["frame"]!!.jsonObject
        assertEquals("file:///fake-handoff/${envelope.sentAtMs}/frame.png", frame["ref"]!!.jsonPrimitive.content)
        assertEquals(envelope.device.widthPx, frame["widthPx"]!!.jsonPrimitive.int)
        assertEquals(envelope.device.heightPx, frame["heightPx"]!!.jsonPrimitive.int)

        val annotation = document["annotations"]!!.jsonArray.single().jsonObject
        assertEquals(1, annotation["index"]!!.jsonPrimitive.int)
        val rect = annotation["rect"]!!.jsonObject
        assertEquals(120, rect["left"]!!.jsonPrimitive.int)
        assertEquals(880, rect["top"]!!.jsonPrimitive.int)
        assertEquals(960, rect["right"]!!.jsonPrimitive.int)
        assertEquals(1040, rect["bottom"]!!.jsonPrimitive.int)
        assertEquals("Button is 8dp too low", annotation["comment"]!!.jsonPrimitive.content)
        assertEquals(
            "file:///fake-handoff/${envelope.sentAtMs}/crop-1.png",
            annotation["cropRef"]!!.jsonPrimitive.content,
        )
    }

    @Test
    fun aNullCropPathOmitsTheCropRefKeyEntirely() {
        val base = sampleEnvelope()
        val envelope = base.copy(annotations = base.annotations.map { it.copy(cropPath = null) })
        val dispatcher = McpDispatcher(FakeAnnotationHandoff { envelope })

        val response = runSuspendBlocking { dispatcher.handle(toolCallRequest(id = 3)) }

        val text = resultOf(response)["content"]!!.jsonArray.single().jsonObject["text"]!!.jsonPrimitive.content
        val annotation = Json.parseToJsonElement(text).jsonObject["annotations"]!!.jsonArray.single().jsonObject
        assertFalse(annotation.containsKey("cropRef"), "a null cropPath must not emit a cropRef key at all")
    }

    @Test
    fun aNewerEnvelopeVersionYieldsIsErrorNamingBothVersions() {
        val newerVersion = SUPPORTED_ENVELOPE_VERSION + 1
        val dispatcher = McpDispatcher(FakeAnnotationHandoff { sampleEnvelope(version = newerVersion) })

        val response = runSuspendBlocking { dispatcher.handle(toolCallRequest(id = 4)) }

        val result = resultOf(response)
        assertEquals(true, result["isError"]!!.jsonPrimitive.boolean)
        val text = result["content"]!!.jsonArray.single().jsonObject["text"]!!.jsonPrimitive.content
        assertTrue(text.contains(newerVersion.toString()), "expected the envelope's version $newerVersion in: $text")
        assertTrue(
            text.contains(SUPPORTED_ENVELOPE_VERSION.toString()),
            "expected the server's supported version $SUPPORTED_ENVELOPE_VERSION in: $text",
        )
        assertTrue(
            text.contains("./gradlew :mcp-application:installDist"),
            "expected the rebuild command in: $text",
        )
    }

    @Test
    fun aThrowingHandoffYieldsAToolErrorAndTheDispatcherKeepsRunning() {
        val dispatcher = McpDispatcher(FakeAnnotationHandoff { error("latest.json is corrupt") })

        val response = runSuspendBlocking { dispatcher.handle(toolCallRequest(id = 5)) }

        val result = resultOf(response)
        assertEquals(true, result["isError"]!!.jsonPrimitive.boolean)

        // never throws out of the loop: a subsequent call on the same dispatcher still answers
        val next = runSuspendBlocking { dispatcher.handle(request(id = 6, method = "tools/list")) }
        assertTrue(requireNotNull(next).contains(GET_LATEST_ANNOTATION_TOOL))
    }

    @Test
    fun aTraversalShapedImagePathYieldsAOneLineToolErrorRatherThanThrowingOutOfTheLoop() {
        val envelope = sampleEnvelope().copy(framePath = "../escape.png")
        val dispatcher = McpDispatcher(FakeAnnotationHandoff { envelope })

        val response = runSuspendBlocking { dispatcher.handle(toolCallRequest(id = 8)) }

        val line = requireNotNull(response)
        assertFalse(line.contains('\n') || line.contains('\r'), "response must stay on one line: $line")
        val result = resultOf(line)
        assertEquals(true, result["isError"]!!.jsonPrimitive.boolean)
        val text = result["content"]!!.jsonArray.single().jsonObject["text"]!!.jsonPrimitive.content
        assertTrue(text.contains("../escape.png"), "expected the rejected path named in: $text")

        // the read loop survives: the next request on the same dispatcher still answers
        val next = runSuspendBlocking { dispatcher.handle(request(id = 9, method = "tools/list")) }
        assertTrue(requireNotNull(next).contains(GET_LATEST_ANNOTATION_TOOL))
    }

    @Test
    fun aSecondCallWithNoPublishInBetweenYieldsAByteIdenticalResult() {
        val tempRoot = Files.createTempDirectory("mcp-application-integration-test")
        try {
            val handoff = fileSlotAnnotationHandoff(tempRoot)
            val envelope = sampleEnvelope()
            runSuspendBlocking {
                handoff.publish(
                    envelope,
                    images = listOf(
                        HandoffImage("frame.png", ByteArray(4)),
                        HandoffImage("crop-1.png", ByteArray(4)),
                    ),
                )
            }
            val dispatcher = McpDispatcher(handoff, nowMs = { envelope.sentAtMs + 1_000L })

            val first = runSuspendBlocking { dispatcher.handle(toolCallRequest(id = 7)) }
            val second = runSuspendBlocking { dispatcher.handle(toolCallRequest(id = 7)) }

            assertEquals(first, second)
        } finally {
            tempRoot.toFile().deleteRecursively()
        }
    }
}

package data.handoff.api.slot

import data.handoff.api.source.handoff.fileSlotAnnotationHandoff
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * [data.handoff.api.core.slot.FileSlotAnnotationHandoff] against a real temp directory — never
 * `~/.rectify`. Covers TAC-1, TAC-2, TAC-4, TAC-6 and TAC-8.
 */
class FileSlotAnnotationHandoffTest {

    private val root: File = createTempDirectory("data-handoff-api-test").toFile()

    @AfterTest
    fun cleanUp() {
        root.deleteRecursively()
    }

    /** TAC-1 — publish then latest() returns an equal envelope by content, not identity. */
    @Test
    fun publishThenLatestReturnsAnEqualEnvelope() = runBlocking {
        val handoff = fileSlotAnnotationHandoff(root.toPath())
        val envelope = sampleEnvelope()

        handoff.publish(envelope, images = listOf(sampleImage("frame.png")))
        val result = handoff.latest()

        assertEquals(envelope, result)
    }

    /** TAC-2 — latest() re-reads from storage; calling it twice with no publish in between still
     * yields equal-content envelopes (AC-81 at the storage layer). */
    @Test
    fun latestCalledTwiceWithNoPublishInBetweenIsIdempotent() = runBlocking {
        val handoff = fileSlotAnnotationHandoff(root.toPath())
        handoff.publish(sampleEnvelope(), images = listOf(sampleImage("frame.png")))

        val first = handoff.latest()
        val second = handoff.latest()

        assertEquals(first, second)
        assertTrue(first != null, "the first read must not itself be null")
    }

    /** TAC-4 — an absent slot returns null, never an exception. */
    @Test
    fun anAbsentSlotReturnsNullNeverThrows() = runBlocking {
        val handoff = fileSlotAnnotationHandoff(root.toPath())

        val result = handoff.latest()

        assertNull(result)
    }

    /** TAC-6 — image bytes round-trip byte-identically at <root>/<sentAtMs>/<name>. */
    @Test
    fun imageBytesRoundTripByteIdenticallyUnderTheSendDirectory() = runBlocking {
        val handoff = fileSlotAnnotationHandoff(root.toPath())
        val envelope = sampleEnvelope(sentAtMs = 1_234_567_890_123L, framePath = "frame.png")
        val frame = sampleImage("frame.png", seed = 7)
        val crop = sampleImage("crop-1.png", seed = 99)

        handoff.publish(envelope, images = listOf(frame, crop))

        val writtenFrame = File(root, "1234567890123/frame.png").readBytes()
        val writtenCrop = File(root, "1234567890123/crop-1.png").readBytes()
        assertContentEquals(frame.bytes, writtenFrame)
        assertContentEquals(crop.bytes, writtenCrop)
    }

    /** TAC-8 (part 1) — latest.json.tmp does not survive a successful publish. */
    @Test
    fun theTmpFileDoesNotSurviveASuccessfulPublish() = runBlocking {
        val handoff = fileSlotAnnotationHandoff(root.toPath())

        handoff.publish(sampleEnvelope(), images = listOf(sampleImage("frame.png")))

        assertTrue(!File(root, "latest.json.tmp").exists())
    }

    /** TAC-8 (part 2) — any latest.json present on disk always parses. */
    @Test
    fun theLatestJsonLeftOnDiskAfterPublishAlwaysParses() = runBlocking {
        val handoff = fileSlotAnnotationHandoff(root.toPath())

        handoff.publish(sampleEnvelope(), images = listOf(sampleImage("frame.png")))

        val raw = Files.readString(File(root, "latest.json").toPath())
        assertTrue(raw.isNotBlank())
        // A real parse, exercised through the production reader itself, not a hand-rolled check.
        assertEquals(sampleEnvelope(), handoff.latest())
    }
}

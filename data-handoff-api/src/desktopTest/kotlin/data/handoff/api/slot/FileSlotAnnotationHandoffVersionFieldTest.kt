package data.handoff.api.slot

import data.handoff.api.source.handoff.fileSlotAnnotationHandoff
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * TAC-5 / AC-32 — `version` survives the round trip AND is the FIRST key in the serialized JSON.
 *
 * Asserted against the actual bytes [FileSlotAnnotationHandoff][data.handoff.api.core.slot.FileSlotAnnotationHandoff]
 * writes to disk, not just the deserialized object — a reader on the other side of the slot sniffs
 * the version by reading the start of the document, so field order is part of the contract.
 */
class FileSlotAnnotationHandoffVersionFieldTest {

    private val root: File = createTempDirectory("data-handoff-api-version-test").toFile()

    @AfterTest
    fun cleanUp() {
        root.deleteRecursively()
    }

    @Test
    fun versionIsTheFirstKeyInTheSerializedLatestJson() = runBlocking {
        val handoff = fileSlotAnnotationHandoff(root.toPath())
        val envelope = sampleEnvelope()

        handoff.publish(envelope, images = listOf(sampleImage("frame.png")))

        val raw = Files.readString(File(root, "latest.json").toPath()).trim()
        assertTrue(
            raw.startsWith("{\"version\""),
            "expected \"version\" to be the first key of the serialized envelope, got: ${raw.take(80)}",
        )
    }

    @Test
    fun versionSurvivesTheRoundTrip() = runBlocking {
        val handoff = fileSlotAnnotationHandoff(root.toPath())
        val envelope = sampleEnvelope()

        handoff.publish(envelope, images = listOf(sampleImage("frame.png")))
        val result = handoff.latest()

        assertEquals(envelope.version, result?.version)
    }
}

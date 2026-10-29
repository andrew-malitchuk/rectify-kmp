package data.handoff.api.slot

import data.handoff.api.source.handoff.fileSlotAnnotationHandoff
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** TAC-3 — a second publish replaces the slot and prunes the old per-send directory. */
class FileSlotAnnotationHandoffReplaceTest {

    private val root: File = createTempDirectory("data-handoff-api-replace-test").toFile()

    @AfterTest
    fun cleanUp() {
        root.deleteRecursively()
    }

    @Test
    fun aSecondPublishReplacesTheSlotAndPrunesTheOldSendDirectory() = runBlocking {
        val handoff = fileSlotAnnotationHandoff(root.toPath())
        val first = sampleEnvelope(sentAtMs = 1_000L, framePath = "frame.png")
        val second = sampleEnvelope(sentAtMs = 2_000L, framePath = "frame.png")

        handoff.publish(first, images = listOf(sampleImage("frame.png")))
        assertTrue(File(root, "1000").isDirectory, "the first send directory must exist after the first publish")

        handoff.publish(second, images = listOf(sampleImage("frame.png")))

        assertEquals(second, handoff.latest())
        assertFalse(File(root, "1000").exists(), "the old per-send directory must be pruned")
        assertTrue(File(root, "2000").isDirectory, "the new send directory must exist")
    }
}

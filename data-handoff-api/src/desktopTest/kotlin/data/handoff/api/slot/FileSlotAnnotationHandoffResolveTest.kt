package data.handoff.api.slot

import data.handoff.api.source.handoff.fileSlotAnnotationHandoff
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * TAC-7 — [data.handoff.api.source.handoff.AnnotationHandoff.resolve] rejects any name containing
 * `/` or `..` before composing a `file://` URI. The slot is untrusted input even though Rectify
 * wrote it.
 */
class FileSlotAnnotationHandoffResolveTest {

    private val root: File = createTempDirectory("data-handoff-api-resolve-test").toFile()

    @AfterTest
    fun cleanUp() {
        root.deleteRecursively()
    }

    @Test
    fun resolveRejectsANameContainingAForwardSlash() {
        val handoff = fileSlotAnnotationHandoff(root.toPath())
        val envelope = sampleEnvelope()

        assertFailsWith<IllegalArgumentException> {
            handoff.resolve(envelope, "sub/frame.png")
        }
    }

    @Test
    fun resolveRejectsANameContainingDotDot() {
        val handoff = fileSlotAnnotationHandoff(root.toPath())
        val envelope = sampleEnvelope()

        assertFailsWith<IllegalArgumentException> {
            handoff.resolve(envelope, "../../etc/passwd")
        }
    }

    @Test
    fun resolveAcceptsAPlainBareFileName() {
        val handoff = fileSlotAnnotationHandoff(root.toPath())
        val envelope = sampleEnvelope(sentAtMs = 4_321L)

        val uri = handoff.resolve(envelope, "frame.png")

        assertTrue(uri.startsWith("file://"), "expected a file:// URI, got $uri")
        assertTrue(uri.endsWith("4321/frame.png"), "expected the send directory in the URI, got $uri")
    }
}

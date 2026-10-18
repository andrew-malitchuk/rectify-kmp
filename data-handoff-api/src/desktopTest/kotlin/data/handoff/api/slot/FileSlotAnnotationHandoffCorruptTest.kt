package data.handoff.api.slot

import data.handoff.api.source.handoff.fileSlotAnnotationHandoff
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNull

/**
 * TAC-9 — a garbage `latest.json` must never throw out of [data.handoff.api.source.handoff.AnnotationHandoff.latest].
 *
 * The interface returns `AnnotationEnvelope?`, with no secondary error channel, so "null-or-error"
 * resolves to `null` here: there is nowhere else for an error to go without widening the contract.
 */
class FileSlotAnnotationHandoffCorruptTest {

    private val root: File = createTempDirectory("data-handoff-api-corrupt-test").toFile()

    @AfterTest
    fun cleanUp() {
        root.deleteRecursively()
    }

    @Test
    fun aGarbageLatestJsonYieldsNullNeverAnEscapingException() = runBlocking {
        File(root, "latest.json").writeText("{ this is not valid json at all")
        val handoff = fileSlotAnnotationHandoff(root.toPath())

        val result = handoff.latest()

        assertNull(result)
    }

    @Test
    fun aTruncatedLatestJsonYieldsNullNeverAnEscapingException() = runBlocking {
        File(root, "latest.json").writeText("{\"version\":1,\"sessionId\":\"x")
        val handoff = fileSlotAnnotationHandoff(root.toPath())

        val result = handoff.latest()

        assertNull(result)
    }
}

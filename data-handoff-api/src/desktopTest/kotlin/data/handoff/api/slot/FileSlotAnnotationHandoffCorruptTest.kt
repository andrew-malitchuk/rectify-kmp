package data.handoff.api.slot

import data.handoff.api.source.handoff.fileSlotAnnotationHandoff
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * TAC-9 — a garbage `latest.json` is a FAULT, not an empty slot.
 *
 * Rewritten at Stage 6 (F-3). This test used to assert `null` for a corrupt slot, matching T-3's
 * abbreviated task wording; the plan's errorState is authoritative and requires a tool error
 * carrying the parse message, and answering "No annotation has been sent yet." right after a send
 * the user watched succeed is actively misleading. ABSENT still means `null` — that case is
 * covered by `FileSlotAnnotationHandoffTest.anAbsentSlotReturnsNullNeverThrows`, untouched — and
 * the thrown message is what `McpDispatcher` renders into `isError: true`.
 */
class FileSlotAnnotationHandoffCorruptTest {

    private val root: File = createTempDirectory("data-handoff-api-corrupt-test").toFile()

    @AfterTest
    fun cleanUp() {
        root.deleteRecursively()
    }

    @Test
    fun aGarbageLatestJsonFailsWithAParseMessageRatherThanReadingAsEmpty() = runBlocking {
        File(root, "latest.json").writeText("{ this is not valid json at all")
        val handoff = fileSlotAnnotationHandoff(root.toPath())

        val failure = assertFails { handoff.latest() }

        assertTrue(
            !failure.message.isNullOrBlank(),
            "the failure must carry a parse message for the tool error to show",
        )
    }

    @Test
    fun aTruncatedLatestJsonFailsWithAParseMessageRatherThanReadingAsEmpty() = runBlocking {
        File(root, "latest.json").writeText("{\"version\":1,\"sessionId\":\"x")
        val handoff = fileSlotAnnotationHandoff(root.toPath())

        val failure = assertFails { handoff.latest() }

        assertTrue(
            !failure.message.isNullOrBlank(),
            "the failure must carry a parse message for the tool error to show",
        )
    }

    /** The other half of the distinction — an absent slot is still the quiet, empty case. */
    @Test
    fun anAbsentLatestJsonIsStillNullRatherThanAFailure() = runBlocking {
        val handoff = fileSlotAnnotationHandoff(root.toPath())

        assertNull(handoff.latest())
    }
}

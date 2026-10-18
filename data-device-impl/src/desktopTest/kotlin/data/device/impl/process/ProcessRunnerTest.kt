package data.device.impl.process

import data.device.api.source.error.DeviceCommandException
import data.device.impl.core.process.ProcessRunner
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.TimeSource

/** Budget every test here runs under; generous enough to survive a loaded CI box. */
private const val TIMEOUT_MS = 500L

/**
 * [ProcessRunner] against real children, using `/bin/sh` instead of hardware.
 *
 * The case worth the process spawn is the one the mirror loop actually hits: a child that
 * exits while something it spawned still holds the write end of the pipe.
 */
class ProcessRunnerTest {

    @Test
    fun stdoutOfASuccessfulCommandIsReturned() = runBlocking {
        val out = ProcessRunner.run(listOf("/bin/sh", "-c", "echo hello"), TIMEOUT_MS)
        assertEquals("hello\n", out.decodeToString())
    }

    @Test
    fun aNonZeroExitCarriesItsStderr() = runBlocking {
        val failure = assertFailsWith<DeviceCommandException> {
            ProcessRunner.run(listOf("/bin/sh", "-c", "echo boom >&2; exit 3"), TIMEOUT_MS)
        }
        assertEquals(3, failure.exitCode)
        assertEquals("boom", failure.stderrTail)
    }

    @Test
    fun aChildThatNeverExitsIsKilledAtTheBudget() = runBlocking {
        val failure = assertFailsWith<DeviceCommandException> {
            ProcessRunner.run(listOf("/bin/sh", "-c", "sleep 30"), TIMEOUT_MS)
        }
        assertEquals(null, failure.exitCode)
    }

    /**
     * T17 — the producer end of the shared timeout classifier.
     *
     * [ProcessRunner] is the only producer of a timeout reason and the escalation ladder is its
     * only consumer, matching via [DeviceCommandException.isTimeout]. This asserts the reason a
     * real watchdog expiry carries is *built from*
     * [DeviceCommandException.TIMEOUT_REASON_PREFIX] and therefore classifies — so retyping it
     * as a hand-written literal (`"timed-out after "`, a reworded log line) goes red here
     * instead of silently disarming the ladder with the suite still green.
     */
    @Test
    fun timeoutPrefixIsSharedBetweenProducerAndConsumer() = runBlocking {
        val failure = assertFailsWith<DeviceCommandException> {
            ProcessRunner.run(listOf("/bin/sh", "-c", "sleep 30"), TIMEOUT_MS)
        }
        assertTrue(
            failure.stderrTail.startsWith(DeviceCommandException.TIMEOUT_REASON_PREFIX),
            "the watchdog reason was not built from the shared constant - got ${failure.stderrTail}",
        )
        assertTrue(failure.isTimeout(), "the watchdog reason did not classify as a timeout")
    }

    /**
     * The regression this file exists for.
     *
     * The shell exits 0 within milliseconds, so `waitFor` is never the slow part - the
     * backgrounded subshell inherits stdout and holds the pipe for ten seconds. While the
     * drains were awaited outside the budget this call took the full ten and the cleanup
     * `finally` was unreachable for all of it.
     */
    @Test
    fun theBudgetCoversADrainHeldOpenByAGrandchild() = runBlocking {
        val start = TimeSource.Monotonic.markNow()
        assertFailsWith<DeviceCommandException> {
            ProcessRunner.run(listOf("/bin/sh", "-c", "( sleep 10; echo late ) & exit 0"), TIMEOUT_MS)
        }
        val elapsedMs = start.elapsedNow().inWholeMilliseconds
        assertTrue(elapsedMs < 5_000, "the held-open pipe outlived the budget by ${elapsedMs}ms")
    }
}

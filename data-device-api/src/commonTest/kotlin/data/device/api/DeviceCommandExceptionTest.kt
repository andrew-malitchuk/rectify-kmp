package data.device.api

import data.device.api.source.error.DeviceCommandException
import data.device.api.source.error.DeviceCommandException.Companion.TIMEOUT_REASON_PREFIX
import kotlin.test.Test
import kotlin.test.assertEquals

private val ARGV = listOf("/usr/bin/adb", "devices", "-l")

/**
 * [DeviceCommandException.isTimeout] — the one classifier the discovery escalation ladder
 * branches on.
 *
 * The trap this guards is that `exitCode == null` is NOT a timeout test: a binary that could
 * not be started at all also has no exit code. Only the reason shape tells them apart.
 */
class DeviceCommandExceptionTest {

    /**
     * T1b.
     *
     * MUTATION: change `isTimeout()`'s body to `exitCode == null` — the non-zero-exit and
     * could-not-start assertions must fail.
     *
     * All three cases are classified before anything is asserted, deliberately: a chain of
     * `assertTrue`/`assertFalse` would short-circuit on the first wrong answer, and the whole
     * point here is to see every case's verdict in one run.
     *
     * One correction to the mutation clause as written, recorded because it was run rather than
     * assumed: `exitCode == null` kills only the could-not-start assertion. It does NOT kill the
     * non-zero-exit one — `exitCode` is `1` there, so a bare null check already answers `false`.
     * That assertion is still load-bearing, just against a different mutation; a body of `true`
     * flips both (verified: `nonZeroExit=true, couldNotStart=true`). Neither assertion is
     * decoration, and no single mutation covers both.
     */
    @Test
    fun timeoutFailureIsDistinguishedFromAParseFailure() {
        val timedOut = DeviceCommandException(ARGV, null, "${TIMEOUT_REASON_PREFIX}3000ms")
        val nonZeroExit = DeviceCommandException(ARGV, 1, "error: device unauthorized")
        val couldNotStart = DeviceCommandException(
            ARGV,
            null,
            "Cannot run program \"/usr/bin/adb\" error=2 No such file or directory",
        )

        assertEquals(
            listOf("timedOut=true", "nonZeroExit=false", "couldNotStart=false"),
            listOf(
                "timedOut=${timedOut.isTimeout()}",
                "nonZeroExit=${nonZeroExit.isTimeout()}",
                "couldNotStart=${couldNotStart.isTimeout()}",
            ),
        )
    }
}

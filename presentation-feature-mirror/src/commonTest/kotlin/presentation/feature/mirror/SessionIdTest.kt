package presentation.feature.mirror

import presentation.feature.mirror.core.send.UNKNOWN_DEVICE_ID
import presentation.feature.mirror.core.send.mintSessionId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * TAC-5 (FR-58): `mintSessionId` — the identifier the completion toast's "Open session" action
 * copies to the clipboard.
 */
class SessionIdTest {

    @Test
    fun producesTheExactDocumentedFormat() {
        assertEquals(
            "RF8M90ABCDE-f3-1760000000000",
            mintSessionId(deviceId = "RF8M90ABCDE", frameId = 3, sentAtMs = 1_760_000_000_000L),
        )
    }

    @Test
    fun degradesToUnknownForANullDeviceId() {
        assertEquals(
            "unknown-f3-1760000000000",
            mintSessionId(deviceId = null, frameId = 3, sentAtMs = 1_760_000_000_000L),
        )
    }

    @Test
    fun degradesToUnknownForABlankDeviceId() {
        assertEquals(
            "unknown-f3-1760000000000",
            mintSessionId(deviceId = "   ", frameId = 3, sentAtMs = 1_760_000_000_000L),
        )
    }

    @Test
    fun degradesToUnknownForAnEmptyDeviceId() {
        assertEquals(
            "unknown-f3-1760000000000",
            mintSessionId(deviceId = "", frameId = 3, sentAtMs = 1_760_000_000_000L),
        )
    }

    @Test
    fun theUnknownDegradationConstantIsExactlyUnknown() {
        assertEquals("unknown", UNKNOWN_DEVICE_ID)
    }

    @Test
    fun differsAcrossTwoSentAtMsValuesForTheSameDeviceAndFrame() {
        val first = mintSessionId(deviceId = "RF8M90ABCDE", frameId = 1, sentAtMs = 1_760_000_000_000L)
        val second = mintSessionId(deviceId = "RF8M90ABCDE", frameId = 1, sentAtMs = 1_760_000_000_001L)

        assertNotEquals(first, second)
    }

    @Test
    fun differsAcrossTwoFrameIdsForTheSameDeviceAndTimestamp() {
        val first = mintSessionId(deviceId = "RF8M90ABCDE", frameId = 1, sentAtMs = 1_760_000_000_000L)
        val second = mintSessionId(deviceId = "RF8M90ABCDE", frameId = 2, sentAtMs = 1_760_000_000_000L)

        assertNotEquals(first, second)
    }
}

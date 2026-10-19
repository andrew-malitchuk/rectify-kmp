package data.device.api

import data.device.api.source.parse.parseIdbTargets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Fixture tests over THIS host's real `idb list-targets` output.
 *
 * The capture was taken with NO `idb_companion` running and the command still exited 0 and
 * still listed every target — see [IDB_LIST_TARGETS].
 */
class IdbTargetsParserTest {

    private val targets = parseIdbTargets(IDB_LIST_TARGETS)

    @Test
    fun `every pipe delimited row of the real capture is parsed`() {
        assertEquals(54, targets.size)
    }

    @Test
    fun `the columns land in the right fields`() {
        assertEquals(
            listOf("iPad (10th generation)", "0CE8BC7C-8E6B-4FA8-A9D1-C5C61AACC946", "Shutdown", "simulator"),
            targets.first().let { listOf(it.name, it.udid, it.state, it.targetType) },
        )
    }

    /** A simulator is always drivable; trust is a physical-device question only. */
    @Test
    fun `every simulator in the capture is trusted`() {
        assertTrue(targets.all { it.trusted })
    }

    /**
     * AS-2's classification, exercised on synthetic physical rows: no trusted or untrusted
     * handset was available on the capture host, which is exactly why the token set has a
     * fallback instead of trusting the list to be complete.
     */
    @Test
    fun `a physical target is trusted only when it reports a usable state`() {
        val output = """
            Andrew iPhone | 00008130-001A2B3C4D5E6F00 | Booted | device | iOS 18.5 | arm64 | No Companion Connected
            Locked iPhone | 00008130-001A2B3C4D5E6F01 | unauthorized | device | iOS 18.5 | arm64 | No Companion Connected
            Odd iPhone | 00008130-001A2B3C4D5E6F02 | Something New | device | iOS 18.5 | arm64 | No Companion Connected
        """.trimIndent()

        assertEquals(
            listOf(true, false, false),
            parseIdbTargets(output).map { it.trusted },
        )
    }

    @Test
    fun `a row without the required columns is skipped rather than thrown on`() {
        val output = """
            not a target row at all
            iPhone 16 | 1122A235-8B04-4139-B652-88BDCF4727C4 | Shutdown | simulator
            | | |
        """.trimIndent()

        assertEquals(listOf("iPhone 16"), parseIdbTargets(output).map { it.name })
    }

    @Test
    fun `empty input yields an empty list`() {
        assertTrue(parseIdbTargets("").isEmpty())
    }
}

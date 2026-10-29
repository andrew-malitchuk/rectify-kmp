package data.device.api

import data.device.api.source.parse.parseSimctlDevices
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Fixture tests over THIS host's real `xcrun simctl list devices available` output.
 *
 * Every claim below was read off the captured payload rather than imagined, which is the point
 * of [SIMCTL_LIST_DEVICES_AVAILABLE] — see its KDoc for the three quirks that a hand-written
 * fixture would not have contained.
 */
class SimctlDevicesParserTest {

    private val simulators = parseSimctlDevices(SIMCTL_LIST_DEVICES_AVAILABLE)

    @Test
    fun `every device row of the real capture is parsed`() {
        assertEquals(54, simulators.size)
    }

    @Test
    fun `the runtime header is attributed to the rows under it`() {
        assertEquals(
            listOf("iOS 17.5", "iOS 18.1", "iOS 18.5", "iOS 26.3"),
            simulators.map { it.runtime }.distinct(),
        )
    }

    /** `(Shutdown) ` has a trailing space in every captured row; it must not reach the state. */
    @Test
    fun `the trailing space after the state is trimmed off`() {
        assertEquals(listOf("Shutdown"), simulators.map { it.state }.distinct())
    }

    /**
     * The quirk that breaks a naive parser: the name itself is parenthesised, so the udid is
     * the SECOND-to-last group and never the first.
     */
    @Test
    fun `a name containing parentheses keeps its parentheses`() {
        val se = simulators.filter { it.name == "iPhone SE (3rd generation)" }

        assertEquals(
            listOf(
                "27E8D85C-C23A-4590-8AB7-2D9507A2F35C",
                "FFE14503-792C-4500-96B2-623DF571FF09",
                "3DA2880B-CEF0-4FF2-912A-9D29F5F5667D",
            ),
            se.map { it.udid },
        )
    }

    /**
     * The strongest argument for udid addressing, and it is measured rather than assumed: the
     * SAME name appears twice under ONE runtime. Name is not a key, and neither is
     * name + runtime.
     */
    @Test
    fun `the same name appears twice under one runtime with different udids`() {
        val duplicates = simulators.filter { it.name == "iPhone 15 Pro" && it.runtime == "iOS 17.5" }

        assertEquals(2, duplicates.size)
        assertEquals(2, duplicates.map { it.udid }.distinct().size)
    }

    /** And the cross-runtime case, which the capture has for `iPhone 16` over 18.1 and 18.5. */
    @Test
    fun `the same name appears under more than one runtime`() {
        assertEquals(
            listOf("iOS 18.1", "iOS 18.5"),
            simulators.filter { it.name == "iPhone 16" }.map { it.runtime },
        )
    }

    @Test
    fun `udids are unique across the whole listing`() {
        assertEquals(simulators.size, simulators.map { it.udid }.distinct().size)
    }

    /**
     * Never throws, per mappers.md. No `(unavailable, …)` row exists in the real capture — the
     * `available` filter removes them — so the defensive drop is exercised with a synthetic
     * section rather than a fabricated fixture pretending to be measured output.
     */
    @Test
    fun `unparseable and unavailable rows are skipped rather than thrown on`() {
        val output = """
            == Devices ==
            -- iOS 18.5 --
                iPhone 16 (1122A235-8B04-4139-B652-88BDCF4727C4) (Shutdown)
                this line is not a device row
                iPhone 15 (5CFB518E-2D13-4A76-ADA6-8234DF234EF7) (unavailable, runtime profile not found)
            -- Unavailable: com.apple.CoreSimulator.SimRuntime.iOS-16-0 --
                iPhone 14 (0C7E7E7E-0000-0000-0000-000000000000) (Shutdown)
        """.trimIndent()

        assertEquals(listOf("iPhone 16"), parseSimctlDevices(output).map { it.name })
    }

    @Test
    fun `empty input yields an empty list`() {
        assertTrue(parseSimctlDevices("").isEmpty())
    }
}

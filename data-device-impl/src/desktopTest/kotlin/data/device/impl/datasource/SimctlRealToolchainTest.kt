package data.device.impl.datasource

import data.device.impl.core.tool.ToolResolver
import data.device.impl.source.datasource.IosHostSourceImpl
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The one test in this spec that talks to the REAL iOS toolchain.
 *
 * Everything else about simctl is proved against a captured fixture, which can only show that
 * the parser still reads what the tool printed on 2026-10-19. This one spawns the installed
 * `xcrun`, hands its stdout to [parseSimctlDevices][data.device.api.source.parse.parseSimctlDevices]
 * through [IosHostSourceImpl], and so is the only evidence that the pipeline works against
 * today's Xcode rather than against a string literal.
 *
 * **It SKIPS, it does not fail, where the toolchain is absent** — a Linux CI box or a Mac
 * without Xcode has nothing to prove here. The skip prints a line into the test report rather
 * than being an `@Ignore`, because an ignored test is one that nobody notices never ran.
 *
 * `runBlocking`, never `runTest`: `runTest`'s virtual clock advances past
 * `ProcessRunner`'s `withTimeoutOrNull` while the real child is still starting, so every
 * spawn inside it times out. That hazard is documented on `ToolResolver` and is the reason
 * its predicates are injectable at all.
 *
 * Asserted on SHAPE, not on inventory: which simulators exist differs per machine, and so
 * does whether one happens to be booted. On the capture host (Xcode 26.3, four runtimes:
 * iOS 17.5 / 18.1 / 18.5 / 26.3) nothing was booted; the booted count is PRINTED rather than
 * asserted so that a developer who opens Simulator.app does not get a red suite.
 */
class SimctlRealToolchainTest {

    @Test
    fun theInstalledSimctlListsParsableSimulators() = runBlocking {
        val hosts = IosHostSourceImpl(ToolResolver())
        val probe = hosts.probeSimctl()
        if (!probe.found) {
            println("SKIPPED SimctlRealToolchainTest: no usable xcrun/simctl here, tried ${probe.probedPaths}")
            return@runBlocking
        }

        val simulators = hosts.listSimulators()
        val runtimes = simulators.map { it.runtime }.distinct()
        println(
            "simctl ${probe.version} at ${probe.path}: ${simulators.size} simulators, " +
                "${runtimes.size} runtimes $runtimes, " +
                "${simulators.count { it.state.equals("Booted", ignoreCase = true) }} booted"
        )

        assertTrue(simulators.isNotEmpty(), "the real simctl listed no parsable simulator row")
        assertTrue(runtimes.isNotEmpty(), "no runtime header was attributed to any row")
        assertTrue(
            simulators.all { it.name.isNotBlank() && it.udid.isNotBlank() && it.state.isNotBlank() },
            "a parsed row came back with a blank field",
        )
        // The udid is the identity simctl is addressed by, and names are NOT unique — this
        // host lists two `iPhone 15 Pro` under iOS 17.5 alone.
        assertTrue(
            simulators.distinctBy { it.udid }.size == simulators.size,
            "the same udid was parsed twice, so the parser is mis-splitting a row",
        )
    }
}

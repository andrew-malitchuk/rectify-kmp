package data.device.impl.source.datasource

import data.device.api.source.datasource.IosHostSource
import data.device.api.source.error.DeviceCommandException
import data.device.api.source.model.IosTargetResource
import data.device.api.source.model.SimulatorResource
import data.device.api.source.model.ToolKind
import data.device.api.source.model.ToolProbe
import data.device.api.source.parse.HostCommand
import data.device.api.source.parse.buildHostArgv
import data.device.api.source.parse.parseIdbTargets
import data.device.api.source.parse.parseSimctlDevices
import data.device.api.source.tuning.MirrorTuning
import data.device.impl.core.process.ProcessRunner
import data.device.impl.core.tool.ToolResolver

/**
 * `xcrun simctl`'s own words when the shim is installed but the simulator CLI is not.
 *
 * The CLT-only Mac: Command Line Tools ship `/usr/bin/xcrun`, full Xcode ships the `simctl`
 * utility behind it. `ToolResolver` finds the shim either way, so without this token the app
 * would report simctl as present and only fail at first use.
 */
private const val SIMCTL_UTILITY_MISSING = "unable to find utility \"simctl\""

/** Runtime section header, e.g. `-- iOS 18.5 --`; its absence is what flags parser drift. */
private const val SECTION_MARKER = "--"

/** Banner `simctl list devices` always prints, header rows or not. */
private const val DEVICES_BANNER = "== Devices =="

/** Enough of the unrecognised output to diagnose the drift from the error card. */
private const val DRIFT_REASON_HEAD = 200

/**
 * The deviceless iOS calls, run as child processes.
 *
 * Desktop only, like every other spawn site in this repo (`platform-coverage.md`): the phone
 * targets bind `UnsupportedIosHostSource` instead.
 *
 * @property tools Locates the `xcrun` and `idb` binaries.
 */
internal class IosHostSourceImpl(
    private val tools: ToolResolver,
) : IosHostSource {

    /**
     * Whether the `simctl` utility has been seen to resolve.
     *
     * SUCCESS ONLY, exactly like [ToolResolver]'s own probe cache and for the same reason: an
     * installed utility cannot vanish while the app runs, but a missing one can arrive — so a
     * negative is re-checked on every poll and a positive is not re-spawned every two seconds.
     */
    private var simctlVerified = false

    /**
     * Resolves `xcrun` AND proves the `simctl` subcommand behind it resolves.
     *
     * Two facts, one probe, because only the pair is actionable: `found = true` here means
     * "simulators can be listed", not "a binary called xcrun exists". The verification is the
     * ordinary device list — measured at 0.28 s on this host, well inside the host budget —
     * rather than a `simctl help` spelling that [HostCommand] would have to be widened for.
     *
     * Any OTHER failure (a timeout, a busy host) leaves the probe `found`: the install hint is
     * for a missing utility, and offering `xcode-select --install` to someone whose Xcode is
     * merely slow would be worse than saying nothing.
     */
    override suspend fun probeSimctl(): ToolProbe {
        val probe = tools.resolve(ToolKind.Simctl)
        val path = probe.path ?: return probe
        return if (simctlUtilityResolves(path)) probe else probe.copy(found = false, path = null, version = null)
    }

    override suspend fun probeIdb(): ToolProbe = tools.resolve(ToolKind.Idb)

    /**
     * A missing `xcrun` lists nothing; reporting that is [probeSimctl]'s job.
     *
     * PARSER DRIFT is the one case this does NOT turn into an empty bench. `simctl`'s
     * plain-text listing is not a stable contract, so the day a release reshapes it
     * [parseSimctlDevices] returns zero rows from perfectly good output and the user would be
     * told "no simulators" forever. Output the parser understood NONE of — no runtime header
     * and nothing but the `== Devices ==` banner stripped away — is therefore raised as a
     * [DeviceCommandException], which the repository's poll loop renders as
     * `PollFailed(tool = "xcrun", …)` and retries on the next tick.
     *
     * The header test is what keeps it honest: a Mac with Xcode and no installed runtimes
     * prints the banner alone and is a legitimately empty bench, not drift.
     */
    override suspend fun listSimulators(): List<SimulatorResource> {
        val path = tools.resolve(ToolKind.Simctl).path ?: return emptyList()
        val output = simctlList(path)
        val simulators = parseSimctlDevices(output)
        if (simulators.isEmpty() && output.looksUnparsed()) {
            throw DeviceCommandException(
                argv = buildHostArgv(path, HostCommand.ListSimulators),
                exitCode = null,
                stderrTail = output.trim().take(DRIFT_REASON_HEAD),
            )
        }
        return simulators
    }

    /**
     * Lists physical and simulated targets through `idb`; a missing `idb` lists nothing.
     *
     * [MirrorTuning.DaemonStartTimeoutMs], not `HostCommandTimeoutMs`, and for the same reason
     * `adb start-server` carries it — MEASURED on this host, three consecutive runs of
     * `idb list-targets` took 5.26 / 5.27 / 5.28 s, as reproducible as adb's 3.03 s cold fork
     * and dominated by idb's own companion handshake rather than by the number of targets. The
     * 3 s host budget would therefore miss on EVERY call and `destroyForcibly` would kill idb
     * mid-handshake, so the physical list would be permanently, silently empty. No new tuning
     * constant: this is the same watchdog shape the existing one already documents.
     */
    override suspend fun listDevices(): List<IosTargetResource> {
        val path = tools.resolve(ToolKind.Idb).path ?: return emptyList()
        val output = ProcessRunner.run(
            argv = buildHostArgv(path, HostCommand.ListTargets),
            timeoutMs = MirrorTuning.DaemonStartTimeoutMs,
        )
        return parseIdbTargets(output.decodeToString())
    }

    /** Only a MISSING utility unsets the probe; any other failure is a slow or busy Xcode. */
    private suspend fun simctlUtilityResolves(path: String): Boolean {
        if (simctlVerified) return true
        return try {
            simctlList(path)
            simctlVerified = true
            true
        } catch (failure: DeviceCommandException) {
            // A timeout or a busy Xcode is reported as found but NOT cached — only a call
            // that actually came back proves the utility is there.
            !failure.stderrTail.contains(SIMCTL_UTILITY_MISSING)
        }
    }

    /** Output holding no runtime header and nothing but the banner: the parser lost the format. */
    private fun String.looksUnparsed(): Boolean = lineSequence()
        .map(String::trim)
        .filter { it.isNotEmpty() && it != DEVICES_BANNER }
        .any { !it.startsWith(SECTION_MARKER) }

    /** The one `simctl list` spawn site, shared by the probe and the listing. */
    private suspend fun simctlList(path: String): String = ProcessRunner.run(
        argv = buildHostArgv(path, HostCommand.ListSimulators),
        timeoutMs = MirrorTuning.HostCommandTimeoutMs,
    ).decodeToString()
}

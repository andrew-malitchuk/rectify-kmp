package data.device.impl.datasource

import data.device.api.source.datasource.IosHostSource
import data.device.api.source.error.DeviceCommandException
import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId
import data.device.api.source.model.IosTargetResource
import data.device.api.source.model.SimulatorResource
import data.device.api.source.model.ToolKind
import data.device.api.source.model.ToolProbe
import data.device.impl.core.tool.ToolResolver
import data.device.impl.source.datasource.IosDeviceCommandExecutor
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * [IosDeviceCommandExecutor] against shell scripts standing in for `xcrun` and `idb`.
 *
 * Same shape as [AdbDeviceCommandExecutorTest] and for the same reason: `ProcessRunner`
 * spawns a real child, and a real child inside `runTest`'s virtual clock always times out
 * (the scheduler runs past `withTimeoutOrNull` before the process starts). So `runBlocking`,
 * a fake binary on a fake `PATH`, and `ToolResolver`'s injected `isExecutable` pinned to it.
 *
 * Three behaviours that were inspection-only before this file: the already-booted swallow,
 * the idb temp-file fallback, and simulator-vs-handset tool classification with its cache.
 */
class IosDeviceCommandExecutorTest {

    /**
     * `simctl boot` on an already-booted simulator is a SUCCESS.
     *
     * Both cases below exit with the SAME code and differ only in stderr, which is the whole
     * point: the executor keys on the token simctl prints, never on the number. The measured
     * code (149) is used by the fake so the fixture stays honest, and asserting on it is
     * exactly the mistake this pairing makes impossible — an implementation that swallowed
     * `exitCode == 149` would pass the first case and fail the second.
     */
    @Test
    fun bootSwallowsTheAlreadyBootedTokenAndStillPropagatesAnUnrelatedFailure() = runBlocking {
        val toolchain = fakeToolchain()
        val executor = toolchain.executor(simulators = listOf(BOOTED_UDID, COLD_UDID))

        val swallowed = executor.run(deviceId(BOOTED_UDID), DeviceCommand.SimulatorBoot)
        val propagated = assertFailsWith<DeviceCommandException> {
            executor.run(deviceId(COLD_UDID), DeviceCommand.SimulatorBoot)
        }

        assertEquals(0, swallowed.size, "an already-booted simulator must come back as empty success")
        assertEquals(SAME_EXIT_CODE, propagated.exitCode, "the fake must fail both cases identically")
        assertTrue(
            propagated.stderrTail.contains(UNRELATED_BOOT_FAILURE),
            "an unrelated boot failure must reach the caller, got ${propagated.stderrTail}",
        )
    }

    /** Booting addresses the udid, never the display name — simulator names are not unique. */
    @Test
    fun bootAddressesTheUdidAndNotTheDeviceName() = runBlocking {
        val toolchain = fakeToolchain()
        val executor = toolchain.executor(simulators = listOf(COLD_UDID))

        val failure = assertFailsWith<DeviceCommandException> {
            executor.run(deviceId(COLD_UDID), DeviceCommand.SimulatorBoot)
        }

        assertEquals(listOf("simctl", "boot", COLD_UDID), failure.argv.drop(1))
    }

    /**
     * PLAN-AS-4: `idb screenshot -` is UNMEASURED, so a rejection of the stdout sink re-runs
     * the SAME argv with the sink swapped for a temp file.
     *
     * The fake idb rejects `-` and writes its payload to whatever other path it is handed,
     * which is only reachable through the substitution — the hardware is not under test, the
     * substitution is. Receiving the payload also proves the temp file is read back before
     * the `finally` deletes it.
     */
    @Test
    fun idbScreencapFallsBackToATempFileWhenTheStdoutSinkIsRejected() = runBlocking {
        val toolchain = fakeToolchain()
        // Not in the simulator list, so it classifies as a physical handset and routes to idb.
        val executor = toolchain.executor(simulators = emptyList())

        val bytes = executor.run(deviceId(HANDSET_UDID), DeviceCommand.Screencap)

        assertEquals(IDB_FILE_PAYLOAD, bytes.decodeToString())
    }

    /**
     * A udid in the simulator list is `simctl`'s, anything else is `idb`'s — and the answer is
     * cached, because the mirror loop asks once per frame at 10 fps.
     *
     * Which binary ran is read off the stdout each fake prints. Three captures against the
     * same simulator must consult the host source exactly once; the handset is classified
     * separately, so it is a second call rather than a cache hit.
     */
    @Test
    fun toolClassificationFollowsTheSimulatorListAndIsCachedPerUdid() = runBlocking {
        val toolchain = fakeToolchain()
        val hosts = RecordingIosHostSource(listOf(COLD_UDID))
        val executor = toolchain.executor(hosts)

        val fromSimulator = (1..3).map { executor.run(deviceId(COLD_UDID), DeviceCommand.Screencap) }
        val fromHandset = executor.run(deviceId(HANDSET_UDID), DeviceCommand.Screencap)

        assertEquals(
            List(3) { SIMCTL_STDOUT_PAYLOAD },
            fromSimulator.map { it.decodeToString() },
            "a listed udid must be driven by simctl",
        )
        assertEquals(IDB_FILE_PAYLOAD, fromHandset.decodeToString(), "an unlisted udid must be driven by idb")
        assertEquals(
            2,
            hosts.listCalls,
            "the classification must be cached per udid - one list per distinct device, not per frame",
        )
    }
}

/** The measured already-booted stderr, quoted from `simctl`. Keyed on by the executor. */
private const val ALREADY_BOOTED = "Unable to boot device in current state: Booted"

/** Any other boot failure, carrying the SAME exit code as the swallowed one. */
private const val UNRELATED_BOOT_FAILURE = "Invalid device state"

/** The exit code `simctl boot` returned on the capture host - shared by both fake failures. */
private const val SAME_EXIT_CODE = 149

/** What the fake `xcrun` writes to stdout for a screenshot. */
private const val SIMCTL_STDOUT_PAYLOAD = "simctl-png-bytes"

/** What the fake `idb` writes into the file it is handed - unreachable without the fallback. */
private const val IDB_FILE_PAYLOAD = "idb-png-bytes-from-file"

private const val BOOTED_UDID = "11111111-AAAA-4444-8888-BOOTED0000"
private const val COLD_UDID = "22222222-BBBB-4444-8888-COLD000000"
private const val HANDSET_UDID = "33333333-CCCC-4444-8888-HANDSET000"

private fun deviceId(raw: String): DeviceId = requireNotNull(DeviceId.of(raw))

/** A directory holding a fake `xcrun` and a fake `idb`, reachable only through `PATH`. */
private class FakeToolchain(private val binDir: File) {

    fun executor(simulators: List<String>): IosDeviceCommandExecutor =
        executor(RecordingIosHostSource(simulators))

    fun executor(hosts: IosHostSource): IosDeviceCommandExecutor = IosDeviceCommandExecutor(
        tools = ToolResolver(
            env = mapOf("PATH" to binDir.absolutePath),
            homeDir = "",
            // Pinned to the fakes: with the real predicate `/usr/bin/xcrun` wins and the
            // test would drive this machine's own Xcode.
            isExecutable = { it.startsWith(binDir.absolutePath) },
        ),
        hosts = hosts,
    )
}

/**
 * `xcrun` and `idb` as shell scripts in one throwaway directory.
 *
 * `xcrun` answers `--version` (ToolResolver reads it during resolution), fails `simctl boot`
 * two different ways, and prints a payload for `simctl io <udid> screenshot`. `idb` rejects
 * the `-` stdout sink and writes its payload to any other sink path.
 */
private fun fakeToolchain(): FakeToolchain {
    val binDir = Files.createTempDirectory("rectify-fake-ios-bin").toFile()
    script(
        binDir,
        "xcrun",
        """
        |case "${'$'}1" in
        |--version) echo 'xcrun version 70.' ;;
        |simctl)
        |  case "${'$'}2" in
        |  boot)
        |    case "${'$'}3" in
        |    $BOOTED_UDID) echo '$ALREADY_BOOTED' >&2 ;;
        |    *) echo '$UNRELATED_BOOT_FAILURE' >&2 ;;
        |    esac
        |    exit $SAME_EXIT_CODE ;;
        |  io) printf '%s' '$SIMCTL_STDOUT_PAYLOAD' ;;
        |  *) echo "unknown simctl subcommand ${'$'}2" >&2 ; exit 1 ;;
        |  esac ;;
        |*) echo "unknown xcrun invocation ${'$'}*" >&2 ; exit 1 ;;
        |esac
        """.trimMargin(),
    )
    script(
        binDir,
        "idb",
        """
        |case "${'$'}2" in
        |-) echo 'idb: unrecognized destination -' >&2 ; exit 1 ;;
        |*) printf '%s' '$IDB_FILE_PAYLOAD' > "${'$'}2" ;;
        |esac
        """.trimMargin(),
    )
    binDir.deleteOnExit()
    return FakeToolchain(binDir)
}

private fun script(dir: File, name: String, body: String): File = File(dir, name).apply {
    writeText("#!/bin/sh\n$body\n")
    setExecutable(true)
    deleteOnExit()
}

/** An [IosHostSource] that answers from a fixed udid list and counts how often it is asked. */
private class RecordingIosHostSource(private val simulators: List<String>) : IosHostSource {

    var listCalls: Int = 0
        private set

    override suspend fun probeSimctl(): ToolProbe = ToolProbe(ToolKind.Simctl, found = true)

    override suspend fun probeIdb(): ToolProbe = ToolProbe(ToolKind.Idb, found = true)

    override suspend fun listSimulators(): List<SimulatorResource> {
        listCalls++
        return simulators.map { SimulatorResource(name = "iPhone 16", udid = it, runtime = "iOS 26.3", state = "Shutdown") }
    }

    override suspend fun listDevices(): List<IosTargetResource> = emptyList()
}

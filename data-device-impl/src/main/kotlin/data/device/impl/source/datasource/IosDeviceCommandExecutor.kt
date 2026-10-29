package data.device.impl.source.datasource

import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.IosHostSource
import data.device.api.source.error.DeviceCommandException
import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId
import data.device.api.source.model.ToolKind
import data.device.api.source.parse.buildDeviceArgv
import data.device.api.source.tuning.MirrorTuning
import data.device.impl.core.process.ProcessRunner
import data.device.impl.core.tool.ToolResolver
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

/**
 * THE already-booted token, quoted from `simctl`'s own stderr.
 *
 * Matched on the text and never on the exit code. The code HAS since been measured on this
 * host — it is **149** (CoreSimulator error 405), recorded in the spec's demo measurements —
 * and that is what vindicates the decision rather than reversing it: 149 is unguessable, so a
 * version of this guard written against a number would have been a guess dressed as a fact.
 * Booting a simulator that is already up is what a user clicking "Boot simulator" twice does —
 * it is a success, not an error.
 */
private const val ALREADY_BOOTED = "Unable to boot device in current state: Booted"

/** `idb screenshot`'s stdout sink, as [buildDeviceArgv] spells it. */
private const val IDB_STDOUT_SINK = "-"

/**
 * The iOS device-scoped execution path on the desktop host.
 *
 * Two binaries behind one [DeviceCommandExecutor]: a simulator is driven by `xcrun simctl`, a
 * physical handset by `idb`. The udid alone does not say which, so the choice comes from the
 * simulator list the host source produces — never from the device NAME, which is not a key
 * (the capture on this host lists two `iPhone 15 Pro` under the single iOS 17.5 runtime).
 *
 * @property tools Locates the `xcrun` and `idb` binaries.
 * @property hosts Supplies the simulator list that classifies a udid.
 */
internal class IosDeviceCommandExecutor(
    private val tools: ToolResolver,
    private val hosts: IosHostSource,
) : DeviceCommandExecutor {

    private val mutex = Mutex()

    /**
     * Which tool each udid seen so far belongs to.
     *
     * Cached because the mirror loop asks once per FRAME: re-listing simulators at 10 fps
     * would spawn `xcrun` ten times a second to re-learn an answer that cannot change for a
     * udid that already exists. A simulator created mid-session is still classified correctly
     * — discovery has to list it before the user can select it, and the first run against it
     * is a miss that re-lists.
     *
     * ponytail: unbounded, which is fine for a bench of simulators; it would need eviction
     * only if udids were being generated in a loop.
     */
    private val toolByDevice = mutableMapOf<String, ToolKind>()

    override suspend fun run(device: DeviceId, command: DeviceCommand): ByteArray {
        val tool = toolFor(device)
        // Throws for an Android-only command, or for SimulatorBoot against a handset — the
        // documented unsupported-on-this-tool failure, raised by the one argv builder.
        val argv = buildDeviceArgv(tool, tools.requirePath(tool), device, command)
        return when (command) {
            DeviceCommand.SimulatorBoot -> boot(argv)
            DeviceCommand.Screencap -> screencap(tool, argv)
            else -> ProcessRunner.run(argv, MirrorTuning.HostCommandTimeoutMs)
        }
    }

    /**
     * `simctl boot <udid>`, through [ProcessRunner.run] and NOT `spawnDetached`.
     *
     * Unlike an emulator — which the detached spawn leaves running for the session — `boot`
     * returns as soon as the simulator is initiated, and its outcome is the whole point of
     * calling it. [MirrorTuning.DaemonStartTimeoutMs] for the same reason `adb start-server`
     * carries it: a cold boot is a daemon start, not a shell read.
     */
    private suspend fun boot(argv: List<String>): ByteArray = try {
        ProcessRunner.run(argv, MirrorTuning.DaemonStartTimeoutMs)
    } catch (failure: DeviceCommandException) {
        if (!failure.stderrTail.contains(ALREADY_BOOTED)) throw failure
        ByteArray(0)
    }

    /**
     * A PNG on stdout, which `simctl io <udid> screenshot --type=png -` writes cleanly.
     *
     * `idb screenshot -` is UNMEASURED: no trusted physical device existed on the capture
     * host, and idb's CLI has historically wanted a destination path rather than `-`. So the
     * `-` spelling is tried first and, if idb rejects it, the same argv is re-run with the
     * sink swapped for a temp file that is read back and deleted. The transport above never
     * learns which path ran — it is handed bytes either way, which is why
     * `ScreencapMirrorTransport` is reused verbatim for iOS.
     */
    private suspend fun screencap(tool: ToolKind, argv: List<String>): ByteArray = try {
        ProcessRunner.run(argv, MirrorTuning.CaptureTimeoutMs)
    } catch (failure: DeviceCommandException) {
        if (tool != ToolKind.Idb) throw failure
        screencapViaTempFile(argv)
    }

    private suspend fun screencapViaTempFile(argv: List<String>): ByteArray {
        // 0600 AT CREATION, not chmod afterwards: this file is a full screenshot of the
        // user's device and the shared temp dir is world-readable at the default umask. F-8
        // shipped exactly this bug once already (a world-readable history archive beside a
        // 0700 sibling) - the attribute closes the window a later chmod would leave open.
        val sink = Files.createTempFile(
            "rectify-idb-",
            ".png",
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")),
        ).toFile()
        return try {
            // Substituted, not hand-built: buildDeviceArgv stays the single construction point
            // and its selector invariant is untouched — a temp path is not a device id.
            ProcessRunner.run(
                argv.map { if (it == IDB_STDOUT_SINK) sink.absolutePath else it },
                MirrorTuning.CaptureTimeoutMs,
            )
            sink.readBytes()
        } finally {
            sink.delete()
        }
    }

    private suspend fun toolFor(device: DeviceId): ToolKind = mutex.withLock {
        toolByDevice.getOrPut(device.value) {
            if (hosts.listSimulators().any { it.udid == device.value }) ToolKind.Simctl else ToolKind.Idb
        }
    }
}

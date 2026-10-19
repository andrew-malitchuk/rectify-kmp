package data.repository.impl.source

import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.EmulatorSource
import data.device.api.source.datasource.IosHostSource
import data.device.api.source.datasource.MirrorTransport
import data.device.api.source.error.DeviceCommandException
import data.device.api.source.model.AvdName
import data.device.api.source.model.AvdResource
import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId
import data.device.api.source.model.DeviceResource
import data.device.api.source.model.DeviceStateResource
import data.device.api.source.model.ToolKind
import data.device.api.source.tuning.MirrorTuning
import data.preference.api.source.datasource.AdbPathPreferenceSource
import data.preference.api.source.model.AdbPathPreference
import data.repository.impl.core.mapper.DeviceResourceMapper
import data.repository.impl.core.mapper.EmulatorResourceMapper
import data.repository.impl.core.mapper.HierarchyDumpStdout
import data.repository.impl.core.mapper.HierarchySnapshotMapper
import data.repository.impl.core.mapper.IosTargetResourceMapper
import data.repository.impl.core.mapper.MirrorFrameMapper
import data.repository.impl.core.mapper.SimulatorResourceMapper
import data.repository.impl.core.mapper.ToolProbeMapper
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.EmulatorModel
import domain.core.source.model.device.HierarchySnapshotModel
import domain.core.source.model.device.MirrorFrameModel
import domain.core.source.model.device.MirrorStatsModel
import domain.core.source.model.device.PlatformModel
import domain.core.source.model.device.ToolModel
import domain.repository.api.source.repository.DeviceRepository
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

/**
 * Discovery and mirroring on top of the device datasources.
 *
 * @property adbHostSource Deviceless `adb` calls.
 * @property iosHostSource Deviceless `xcrun simctl` and `idb` calls.
 * @property emulatorSource Deviceless `emulator` calls.
 * @property androidExecutor The Android device-scoped command path, used to ask a running
 *   emulator for its AVD name and to capture a frame.
 * @property iosExecutor The iOS device-scoped command path — simulator boot and capture.
 * @property androidTransport Frame source for an active `adb` session.
 * @property iosTransport Frame source for an active simulator session.
 * @property adbPathPreferenceSource Where a user-chosen `adb` path is persisted. Written here,
 *   read by `ToolResolver` at probe time — the resolver validates it, this does not.
 * @property nowMs Wall-clock reader. A parameter so the poll metadata is assertable under
 *   virtual time instead of needing a real two-second wait.
 *
 * The executor and the transport arrive as a PAIR each rather than one already-resolved
 * instance: a repository call carries its own `platform`, so the choice is made PER CALL - by
 * [transport]'s exhaustive `when` for a session, and by the `when (platform)` in
 * [observeDiscovery] and [bootEmulator] for everything device-scoped. Neither has an `else`,
 * so a third platform cannot silently inherit adb's.
 */
private val log = Logger.withTag("DeviceRepository")

internal class DeviceRepositoryImpl(
    private val adbHostSource: AdbHostSource,
    private val iosHostSource: IosHostSource,
    private val emulatorSource: EmulatorSource,
    private val androidExecutor: DeviceCommandExecutor,
    private val iosExecutor: DeviceCommandExecutor,
    private val androidTransport: MirrorTransport,
    private val iosTransport: MirrorTransport,
    private val adbPathPreferenceSource: AdbPathPreferenceSource,
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : DeviceRepository {

    /** Last version the polling platform's tool reported, for an active session's status bar. */
    private val toolVersion = MutableStateFlow("")

    /**
     * The frame source for this platform.
     *
     * No `else`: a third [PlatformModel] has to be given a transport of its own rather than
     * silently streaming the Android one.
     */
    private fun transport(platform: PlatformModel): MirrorTransport = when (platform) {
        PlatformModel.Android -> androidTransport
        PlatformModel.Ios -> iosTransport
    }

    /** The binary whose name belongs on a failure this platform produced. */
    private fun toolNameOf(platform: PlatformModel): String = when (platform) {
        PlatformModel.Android -> ToolKind.Adb.binaryName
        PlatformModel.Ios -> ToolKind.Simctl.binaryName
    }

    /**
     * @see DeviceRepository.refreshDiscovery
     *
     * `trySend`, not `send`: this must never suspend its caller. A refresh is a best-effort
     * nudge, and the conflated channel means a dropped one only ever costs the user the
     * remainder of an interval they were already waiting out.
     */
    override suspend fun refreshDiscovery() {
        refreshRequests.trySend(Unit)
    }

    /**
     * Requests to cut the poll interval short.
     *
     * CONFLATED on purpose: a refresh that arrives mid-poll is kept rather than dropped, so
     * the next interval returns immediately instead of the request vanishing — and two rapid
     * requests collapse into one, because "poll now" is not a queue.
     */
    private val refreshRequests = Channel<Unit>(Channel.CONFLATED)

    /**
     * Serialises everything that talks to the adb daemon's lifecycle.
     *
     * COR-3. Without it, AC-27's "Restart adb" could make the state it repairs worse: the
     * button runs on the Orbit intent scope while the poll loop runs on its own, so a
     * `kill-server` could land while the loop was inside `adb devices -l`. That call then
     * talks to a daemon that was just killed, pays the measured 3.03 s re-fork against a 3 s
     * budget, times out, and enters the ladder — issuing its OWN `start-server` concurrently
     * with the button's. Two racing starts, and a `PollFailed` card for a daemon the restart
     * had already fixed. The interleaving was nondeterministic, so a successful restart could
     * observably read as "adb stopped responding".
     *
     * Guards the daemon's lifecycle only, never a plain poll: holding it across every
     * `listDevices` would serialise the whole loop behind any user action for no benefit.
     */
    private val daemonLifecycle = Mutex()

    /**
     * Probes both platforms (AC-1).
     *
     * The iOS answer comes from `xcrun simctl`, not `idb`: simulators are the mirrorable half,
     * so the dot has to report the binary the session would actually use. `probeSimctl()` also
     * verifies the SUBCOMMAND resolves, so a CLT-only Mac reads as missing rather than found.
     */
    override suspend fun detectTools(): List<ToolModel> {
        val adb = adbHostSource.probe()
        toolVersion.value = adb.version.orEmpty()
        return listOf(adb, iosHostSource.probeSimctl()).map(ToolProbeMapper.toModel::map)
    }

    override fun observeDiscovery(platform: PlatformModel): Flow<DiscoveryModel> = flow {
        emit(DiscoveryModel.Detecting)
        // Collector-local, so two collectors cannot race on it. A serial's AVD name cannot
        // change while it stays attached, which is what makes caching it safe - and worth
        // it, since the alternative is an extra child process per emulator per poll.
        val avdNames = mutableMapOf<String, AvdName>()
        var emptyPolls = 0

        while (true) {
            // The guard belongs HERE and not in a `Flow.catch` further up: `catch` is
            // terminal, so a use-case or collector guard could render the failure exactly
            // once and discovery would still be over for good. Only the loop itself can
            // turn a failed poll into "try again on the next tick" - and `adb` fails
            // transiently all the time, most reliably on the cold start where the very
            // first `adb devices -l` has to boot the server inside HostCommandTimeoutMs.
            val emission = try {
                when (platform) {
                    PlatformModel.Android -> pollAndroid(avdNames, emptyPolls)
                    PlatformModel.Ios -> pollIos(emptyPolls)
                }.also { emptyPolls = it.emptyPolls }
            } catch (failure: DeviceCommandException) {
                // emptyPolls is left exactly as it was: a poll that never answered neither
                // confirms an empty bench nor clears a streak of them.
                PollOutcome(
                    model = DiscoveryModel.PollFailed(
                        // The POLLING platform's own binary. It was hardcoded to adb, which
                        // reported an adb failure for an xcrun one the moment iOS could poll.
                        tool = toolNameOf(platform),
                        reason = failure.stderrTail.ifBlank { failure.message.orEmpty() },
                    ),
                    emptyPolls = emptyPolls,
                )
            }
            // Emitted outside the try: catching around `emit` would also swallow a
            // downstream failure, which flow exception transparency forbids.
            emit(emission.model)
            // Waits the poll interval, OR returns at once if someone asked for a refresh.
            // This is what makes a Retry button do work NOW rather than up to two seconds
            // later. It lives here, beside the delay it shortens, because this loop is
            // covered by virtual-time tests - an earlier attempt to do it in the presentation
            // layer had to be withdrawn when it proved timing-dependent and unprovable
            // (see execution/GAP-fr37-immediate-repoll.md).
            withTimeoutOrNull(MirrorTuning.DiscoveryPollMs) { refreshRequests.receive() }
        }
    }

    /** One poll's emission plus the empty-poll streak it leaves behind. */
    private data class PollOutcome(val model: DiscoveryModel, val emptyPolls: Int)

    /**
     * One Android discovery round trip. Throws [DeviceCommandException] like any other
     * data-layer call - the caller decides what a failed round trip means for the stream.
     */
    private suspend fun pollAndroid(avdNames: MutableMap<String, AvdName>, emptyPolls: Int): PollOutcome {
        val probe = adbHostSource.probe()
        if (!probe.found) {
            // Not an error: a missing tool is a rendered state, so the counter does not
            // advance - we learned nothing about whether a device is attached.
            return PollOutcome(
                model = DiscoveryModel.ToolMissing(
                    tool = ToolKind.Adb.binaryName,
                    probedPaths = probe.probedPaths,
                    installCommand = probe.installCommand,
                    envHint = probe.envHint,
                ),
                emptyPolls = 0,
            )
        }
        toolVersion.value = probe.version.orEmpty()
        val devices = listDevicesEscalating()
        avdNames.keys.retainAll(devices.mapTo(mutableSetOf()) { it.id.value })
        val usable = devices.count { it.state == DeviceStateResource.Device }
        val streak = if (usable == 0) emptyPolls + 1 else 0

        val model = if (streak >= MirrorTuning.EmptyPollEscalation) {
            DiscoveryModel.NoDeviceFound(
                toolVersion = probe.version.orEmpty(),
                lastPollEpochMs = nowMs(),
                attempts = streak,
                attachedCount = devices.count { it.state != DeviceStateResource.Device },
            )
        } else {
            DiscoveryModel.Found(
                physical = devices.filterNot(DeviceResource::isEmulator)
                    .map(DeviceResourceMapper.toModel::map),
                emulators = emulators(devices, avdNames),
            )
        }
        return PollOutcome(model, streak)
    }

    /**
     * One iOS discovery round trip: `xcrun simctl` for the simulators, `idb` for the handsets.
     *
     * Two deliberate asymmetries with [pollAndroid]:
     *
     * - **`idb` failing is not a failed poll.** `listDevices()` is wrapped, so a missing
     *   `idb_companion` degrades the PHYSICAL list to empty instead of hiding the answer
     *   `simctl` already gave. The simulator half is the mirrorable one; losing it to a
     *   handset bridge nobody installed would be the worse outcome.
     * - **No empty-poll escalation before [DiscoveryModel.NoDeviceFound].** `simctl` answers
     *   from a local plist in a measured 0.28 s and does not flap the way a cold adb daemon
     *   does, so there is nothing to wait out. The streak is still counted, so `attempts`
     *   reports the truth.
     *
     * `listSimulators()` throwing IS a failed poll and propagates — that is the parser-drift
     * path, which [IosHostSource]'s implementation raises rather than reporting a silent
     * empty bench, and the caller renders it as `PollFailed(tool = "xcrun")`.
     */
    private suspend fun pollIos(emptyPolls: Int): PollOutcome {
        val probe = iosHostSource.probeSimctl()
        if (!probe.found) {
            return PollOutcome(
                model = DiscoveryModel.ToolMissing(
                    tool = ToolKind.Simctl.binaryName,
                    probedPaths = probe.probedPaths,
                    installCommand = probe.installCommand,
                    envHint = probe.envHint,
                ),
                emptyPolls = 0,
            )
        }
        toolVersion.value = probe.version.orEmpty()
        val simulators = iosHostSource.listSimulators()
        val handsets = try {
            iosHostSource.listDevices()
        } catch (failure: DeviceCommandException) {
            log.d { "idb would not list targets, reporting simulators only: ${failure.message}" }
            emptyList()
        }
        val usable = simulators.count(SimulatorResourceMapper::isBooted) + handsets.count { it.trusted }
        val streak = if (usable == 0) emptyPolls + 1 else 0

        val untrusted = handsets.filterNot { it.trusted }
        val model = when {
            usable > 0 -> DiscoveryModel.Found(
                // Untrusted handsets ride along as unselectable rows, exactly as an
                // `unauthorized` Android serial does - a device the user is holding has to be
                // visible even while it cannot be driven.
                physical = handsets.map(IosTargetResourceMapper.toModel::map),
                emulators = simulators.map(SimulatorResourceMapper.toModel::map),
            )

            // Only when it is the ONLY candidate: a bootable simulator means the card would
            // be covering a list the user could act on.
            untrusted.size == 1 && simulators.isEmpty() -> untrusted.first().let { handset ->
                DiscoveryModel.DeviceNotTrusted(
                    deviceId = handset.udid,
                    deviceName = handset.name,
                    simulatorsAvailable = false,
                )
            }

            simulators.isNotEmpty() -> DiscoveryModel.SimulatorNotBooted(
                simulators = simulators.map(SimulatorResourceMapper.toModel::map),
                toolVersion = probe.version.orEmpty(),
                lastPollEpochMs = nowMs(),
            )

            else -> DiscoveryModel.NoDeviceFound(
                toolVersion = probe.version.orEmpty(),
                lastPollEpochMs = nowMs(),
                attempts = streak,
                attachedCount = untrusted.size,
            )
        }
        return PollOutcome(model, streak)
    }

    /**
     * `adb devices -l`, with the FR-36 escalation ladder behind it.
     *
     * 1. Ask at the normal [MirrorTuning.HostCommandTimeoutMs] budget. Answered — done.
     * 2. A TIMEOUT-classified failure, and only that, means the daemon may be cold: heal it
     *    ([healDaemon]) and ask exactly once more. A parse error or a non-zero exit is a real
     *    answer about something else and is rethrown untouched, so it reaches the caller as
     *    `PollFailed` without a single extra process being spawned. Firing a 15 s watchdog at
     *    output that merely failed to parse would be a bug of its own.
     * 3. Anything still failing is rethrown, so the loop turns it into `PollFailed` with the
     *    real reason and tries again on the next tick. One escalation per poll — never a loop.
     *
     * There is deliberately NO pre-emptive `startServer()` ahead of the first poll. It was
     * proposed on the measurement that a warm `start-server` costs 0.00 s, then WITHDRAWN on
     * the later measurement that it HANGS against a wedged daemon: unconditional, it would
     * burn the full [MirrorTuning.DaemonStartTimeoutMs] on every poll — a second livelock,
     * five times longer than the 3 s one this fixes. Keying on a timeout also means there is
     * no "daemon is believed up" flag to keep, a flag whose belief outlived any daemon the
     * user killed from a shell.
     */
    private suspend fun listDevicesEscalating(): List<DeviceResource> = try {
        adbHostSource.listDevices()
    } catch (failure: DeviceCommandException) {
        // Step 2. Only a timeout is a candidate for healing; everything else is a real answer
        // about something other than the daemon and goes straight back out untouched.
        if (!failure.isTimeout()) throw failure
        healDaemon()
        // Step 3. Exactly one more attempt. If this throws, the caller turns it into
        // PollFailed with the real reason and the next tick starts over - one escalation per
        // poll, never a loop, which is what stops the cure from becoming a second livelock.
        adbHostSource.listDevices()
    }

    /**
     * Brings the adb daemon back, cold case first.
     *
     * A cold daemon is merely absent and `start-server` forks it in a measured, fixed 3.03 s.
     * A WEDGED daemon — alive but not serving — is a second, distinct failure mode that
     * `start-server` cannot repair: it hangs there, because one already exists. Only
     * `kill-server` clears it, so a timed-out [AdbHostSource.startServer] escalates into the
     * very same kill-then-start sequence AC-27's "Restart adb" button runs.
     *
     * A non-timeout `start-server` failure is not a wedge and is rethrown: nothing to repair.
     */
    private suspend fun healDaemon() = daemonLifecycle.withLock {
        try {
            // Logged on the way IN, not on success. ProcessRunner only logs when it kills a
            // process, so a repair that works leaves no trace at all — which made the fix
            // indistinguishable from the bug during hardware verification: one timeout, no
            // start-server line, and a device that appeared anyway. An unobservable repair is
            // one nobody can support in the field.
            log.d { "poll timed out; starting the adb daemon" }
            adbHostSource.startServer()
        } catch (failure: DeviceCommandException) {
            if (!failure.isTimeout()) throw failure
            log.d { "daemon start timed out; it is wedged rather than absent, restarting it" }
            killThenStart()
        }
    }

    /**
     * @see DeviceRepository.restartAdbServer
     *
     * Each half carries its own budget inside [AdbHostSource] — `kill-server` closes a local
     * socket at the ordinary host-command budget, `start-server` forks a daemon under the far
     * longer [MirrorTuning.DaemonStartTimeoutMs] watchdog. Either half failing throws, so the
     * use case reports a `Failure` rather than a button that silently did nothing.
     */
    override suspend fun restartAdbServer() = daemonLifecycle.withLock { killThenStart() }

    /**
     * The kill-then-start sequence itself, WITHOUT taking [daemonLifecycle].
     *
     * Split out because Kotlin's `Mutex` is not reentrant: [healDaemon] already holds the lock
     * when it escalates, so calling the public [restartAdbServer] from inside it deadlocked —
     * caught immediately by two ladder tests, which is the whole reason the wedge path has
     * them.
     */
    private suspend fun killThenStart() {
        adbHostSource.killServer()
        adbHostSource.startServer()
    }

    /**
     * @see DeviceRepository.bootEmulator
     *
     * The iOS half does NOT go through [AvdName]: the identifier is a simulator udid, booting
     * addresses exactly one of them, and that makes it a device-scoped
     * [DeviceCommand.SimulatorBoot] rather than a deviceless `emulator` call.
     */
    override suspend fun bootEmulator(platform: PlatformModel, avdName: String) = when (platform) {
        PlatformModel.Android -> {
            val avd = AvdName.of(avdName)
                ?: throw DeviceCommandException(listOf(ToolKind.Emulator.binaryName), null, "invalid AVD name $avdName")
            emulatorSource.boot(avd)
        }

        PlatformModel.Ios -> {
            val udid = DeviceId.of(avdName)
                ?: throw DeviceCommandException(listOf(ToolKind.Simctl.binaryName), null, "invalid udid $avdName")
            iosExecutor.run(udid, DeviceCommand.SimulatorBoot)
            Unit
        }
    }

    override fun mirror(platform: PlatformModel, deviceId: String): Flow<MirrorFrameModel> {
        val device = DeviceId.of(deviceId)
            ?: return flow {
                throw DeviceCommandException(listOf(toolNameOf(platform)), null, "invalid device id $deviceId")
            }
        return transport(platform).frames(device).map(MirrorFrameMapper.toModel::map)
    }

    override fun observeMirrorStats(platform: PlatformModel): Flow<MirrorStatsModel> =
        combine(transport(platform).stats, toolVersion) { stats, version ->
            MirrorStatsModel(
                toolVersion = version,
                widthPx = stats.widthPx,
                heightPx = stats.heightPx,
                fps = stats.fps,
                latencyMs = stats.latencyMs,
                droppedFrames = stats.droppedFrames,
            )
        }

    /**
     * @see DeviceRepository.dumpHierarchy
     *
     * TWO executor calls, one repository call. `UiAutomatorDump` and `WmDensity` are separate
     * sealed commands with separate argvs, so one `run` cannot carry both — and it does not
     * need to: density is a static device property and the frame size comes from the dump's
     * own root, so nothing can drift between two calls a fraction of a second apart. What
     * matters is that the snapshot is assembled from both halves or from neither, which
     * [HierarchyDumpStdout] enforces at the type level. See the wave-4 addendum in
     * `execution/GATE-step2-go-no-go.md`.
     */
    override suspend fun dumpHierarchy(
        platform: PlatformModel,
        deviceId: String,
    ): HierarchySnapshotModel {
        // AC-50: there is no `uiautomator` on iOS and no simulator equivalent this release, so
        // the executor is never REACHED - an empty snapshot is the honest answer and measure
        // mode degrades to pixels rather than the whole freeze failing.
        if (platform == PlatformModel.Ios) return HierarchySnapshotModel.Empty
        val device = DeviceId.of(deviceId)
            ?: throw DeviceCommandException(listOf(ToolKind.Adb.binaryName), null, "invalid device id $deviceId")
        val dump = androidExecutor.run(device, DeviceCommand.UiAutomatorDump).decodeToString()
        // A dump that worked is not thrown away because `wm density` did not: an unknown
        // density degrades to the 0 sentinel, which makes the caller report px instead of a
        // dp figure derived from a guess. A failed DUMP still throws - that is the payload.
        val density = try {
            androidExecutor.run(device, DeviceCommand.WmDensity).decodeToString()
        } catch (failure: DeviceCommandException) {
            log.d { "wm density failed, reporting an unknown density: ${failure.message}" }
            ""
        }
        val snapshot =
            HierarchySnapshotMapper.toModel.map(HierarchyDumpStdout(dump = dump, density = density))
        // A dump that yielded neither a node nor a density is "nothing at all", and
        // HierarchySnapshotModel.Empty is that sentinel - it must stay reachable through this
        // repository even though the mapper now threads the raw stdout verbatim. Keeping the
        // raw text of a dump nothing could be parsed from would only hand a caller an error
        // message to forward as a hierarchy.
        return if (snapshot.nodes.isEmpty() && snapshot.densityDpi == 0) {
            HierarchySnapshotModel.Empty
        } else {
            snapshot
        }
    }

    /**
     * Configured AVDs merged with the running instances.
     *
     * An AVD that is both listed by `emulator -list-avds` and currently running appears
     * exactly once, marked running: the match is consumed out of [running] as it is made,
     * and only the leftovers (a running instance of an unlisted or unnamed AVD) are appended.
     */
    private suspend fun emulators(
        devices: List<DeviceResource>,
        avdNames: MutableMap<String, AvdName>,
    ): List<EmulatorModel> {
        val running = mutableMapOf<String, DeviceId>()
        val unnamed = mutableListOf<DeviceId>()
        devices.filter { it.isEmulator && it.state == DeviceStateResource.Device }
            .forEach { device ->
                when (val name = avdNameOf(device.id, avdNames)) {
                    null -> unnamed += device.id
                    else -> running[name.value] = device.id
                }
            }

        val listed = emulatorSource.listAvds()
            .map { avd -> AvdResource(name = avd.name, runningAs = running.remove(avd.name.value)) }
            .map(EmulatorResourceMapper.toModel::map)
        val unlisted = running.map { (name, id) ->
            EmulatorModel(avdName = name, isRunning = true, runningDeviceId = id.value)
        }
        val anonymous = unnamed.map { id ->
            EmulatorModel(avdName = id.value, isRunning = true, runningDeviceId = id.value)
        }
        return listed + unlisted + anonymous
    }

    /**
     * AVD name of a running emulator, asked once per attached serial.
     *
     * @return The name, or `null` when the instance will not answer - it is still listed,
     *   just under its serial, rather than disappearing from the picker.
     */
    private suspend fun avdNameOf(device: DeviceId, avdNames: MutableMap<String, AvdName>): AvdName? {
        avdNames[device.value]?.let { return it }
        val output = try {
            androidExecutor.run(device, DeviceCommand.EmuAvdName).decodeToString()
        } catch (failure: DeviceCommandException) {
            return null
        }
        val name = output.lineSequence()
            .map(String::trim)
            .filter { it.isNotEmpty() && it != "OK" }
            .firstNotNullOfOrNull(AvdName::of)
            ?: return null
        avdNames[device.value] = name
        return name
    }

    /**
     * @see DeviceRepository.setAdbPath
     *
     * No validation and no probe here on purpose. The path is checked against the filesystem
     * by `ToolResolver`'s existing `isExecutable` predicate, the same one every other candidate
     * passes through, so a bad path falls through to the ordered candidate list rather than
     * becoming an error the user has to clear.
     */
    override suspend fun setAdbPath(path: String) {
        adbPathPreferenceSource.setData(AdbPathPreference(path))
    }
}

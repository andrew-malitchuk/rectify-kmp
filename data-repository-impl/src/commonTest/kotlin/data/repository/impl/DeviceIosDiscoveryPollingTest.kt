package data.repository.impl

import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.EmulatorSource
import data.device.api.source.datasource.MirrorTransport
import data.device.api.source.error.DeviceCommandException
import data.device.api.source.model.AvdName
import data.device.api.source.model.AvdResource
import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId
import data.device.api.source.model.DeviceResource
import data.device.api.source.model.IosTargetResource
import data.device.api.source.model.MirrorFrame
import data.device.api.source.model.SimulatorResource
import data.device.api.source.model.ToolKind
import data.device.api.source.model.ToolProbe
import data.device.api.source.model.TransportStats
import data.repository.impl.source.DeviceRepositoryImpl
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.HierarchySnapshotModel
import domain.core.source.model.device.PlatformModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `pollIos`'s five classifications, its two degradation paths, and AC-50.
 *
 * Everything here runs against fakes under virtual time: the iOS poll spawns `xcrun` and
 * `idb` in production and a real spawn can never complete inside `runTest`'s clock.
 *
 * The emissions are compared as one summary STRING per poll rather than variant by variant,
 * the same shape `DeviceDiscoveryPollingTest` uses: a chain of `assertTrue`s short-circuits,
 * so a mutation that only breaks the later links looks fully killed by the first.
 */
class DeviceIosDiscoveryPollingTest {

    @Test
    fun `a booted simulator is Found`() = runTest {
        val hosts = FakeIosHostSource(
            found = true,
            simulators = listOf(simulator("iPhone 16", BOOTED_UDID, "Booted"), simulator("iPad Pro", COLD_UDID)),
        )

        val first = repository(hosts).observeIos().drop(1).first()

        // Both rows survive: the picker lists the cold one as a bootable row beside the
        // running one, exactly as a cold AVD sits beside a running emulator on Android.
        assertEquals("Found(sims=iPhone 16 running|iPad Pro cold)", first.summary())
    }

    @Test
    fun `only shut down simulators report SimulatorNotBooted`() = runTest {
        val hosts = FakeIosHostSource(found = true, simulators = listOf(simulator("iPhone 16", COLD_UDID)))

        val first = repository(hosts).observeIos().drop(1).first()

        // NOT NoDeviceFound: the user has something to boot, and offering "boot one" instead
        // of "plug something in" is the whole reason the two are separate states (AC-31).
        assertEquals("SimulatorNotBooted(1 xcrun=16.2)", first.summary())
    }

    @Test
    fun `a lone untrusted handset reports DeviceNotTrusted`() = runTest {
        val hosts = FakeIosHostSource(found = true, devices = listOf(handset("Andrews iPhone", trusted = false)))

        val first = repository(hosts).observeIos().drop(1).first()

        assertEquals("DeviceNotTrusted($HANDSET_UDID sims=false)", first.summary())
    }

    @Test
    fun `an untrusted handset beside a simulator never hides the simulator`() = runTest {
        val hosts = FakeIosHostSource(
            found = true,
            simulators = listOf(simulator("iPhone 16", COLD_UDID)),
            devices = listOf(handset("Andrews iPhone", trusted = false)),
        )

        val first = repository(hosts).observeIos().drop(1).first()

        // The card would cover a list the user can act on, so the bootable simulator wins.
        assertEquals("SimulatorNotBooted(1 xcrun=16.2)", first.summary())
    }

    @Test
    fun `an absent xcrun reports ToolMissing naming xcrun`() = runTest {
        val first = repository(FakeIosHostSource(found = false)).observeIos().drop(1).first()

        assertEquals("ToolMissing(xcrun)", first.summary())
    }

    @Test
    fun `an empty bench reports NoDeviceFound`() = runTest {
        val first = repository(FakeIosHostSource(found = true)).observeIos().drop(1).first()

        assertEquals("NoDeviceFound(attempts=1)", first.summary())
    }

    @Test
    fun `an idb failure degrades the physical list without erasing the simulators`() = runTest {
        val hosts = FakeIosHostSource(
            found = true,
            simulators = listOf(simulator("iPhone 16", BOOTED_UDID, "Booted")),
            // No idb_companion on the host - the measured failure of a Mac that never
            // installed the handset bridge.
            deviceFailure = DeviceCommandException(listOf("idb", "list-targets"), 1, "no idb_companion"),
        )

        val first = repository(hosts).observeIos().drop(1).first()

        // simctl answered; losing idb must not cost the user the mirrorable half.
        assertEquals("Found(sims=iPhone 16 running)", first.summary())
    }

    @Test
    fun `parser drift surfaces as PollFailed naming xcrun rather than adb`() = runTest {
        val hosts = FakeIosHostSource(
            found = true,
            simulatorFailure = DeviceCommandException(listOf("xcrun", "simctl"), null, "{\"devices\":{}}"),
        )

        val first = repository(hosts).observeIos().drop(1).first()

        // The line-146 bug: the catch hardcoded adb, so an xcrun failure was reported as an
        // adb one - on the platform whose card offers no adb action at all.
        assertEquals("PollFailed(xcrun {\"devices\":{}})", first.summary())
    }

    @Test
    fun `dumpHierarchy on iOS answers Empty without reaching the executor`() = runTest {
        // THROWS if it is ever invoked: a quiet fake would pass even if the iOS path started
        // reaching the executor, which is the tautology this assertion exists to avoid.
        val repository = repository(FakeIosHostSource(found = true), executor = ExplodingExecutor)

        val snapshot = repository.dumpHierarchy(PlatformModel.Ios, BOOTED_UDID)

        assertEquals(HierarchySnapshotModel.Empty, snapshot)
    }

    @Test
    fun `booting a simulator addresses its udid through the executor`() = runTest {
        val executor = RecordingExecutor()
        val repository = repository(FakeIosHostSource(found = true), executor = executor)

        repository.bootEmulator(PlatformModel.Ios, BOOTED_UDID)

        // By udid and as a DEVICE command, never through AvdName: two simulators can share a
        // name under one runtime, so a name does not address one of them.
        assertEquals(listOf<Pair<String, DeviceCommand>>(BOOTED_UDID to DeviceCommand.SimulatorBoot), executor.calls)
    }

    @Test
    fun `booting a simulator whose udid is unusable never spawns anything`() = runTest {
        val executor = RecordingExecutor()
        val repository = repository(FakeIosHostSource(found = true), executor = executor)

        var failed = false
        try {
            repository.bootEmulator(PlatformModel.Ios, "not a udid")
        } catch (failure: DeviceCommandException) {
            failed = true
        }

        assertTrue(failed)
        assertEquals(emptyList<Pair<String, DeviceCommand>>(), executor.calls)
    }

    private fun repository(
        hosts: FakeIosHostSource,
        executor: DeviceCommandExecutor = RecordingExecutor(),
    ): DeviceRepositoryImpl = DeviceRepositoryImpl(
        adbHostSource = AbsentAdbHostSource,
        iosHostSource = hosts,
        emulatorSource = AbsentEmulatorSource,
        androidExecutor = ExplodingExecutor,
        iosExecutor = executor,
        androidTransport = SilentTransport,
        iosTransport = SilentTransport,
        adbPathPreferenceSource = FakeAdbPathPreferenceSource(),
        nowMs = { 0L },
    )

    /** The first POLL, with [DiscoveryModel.Detecting] - which every stream opens with - dropped. */
    private fun DeviceRepositoryImpl.observeIos(): Flow<DiscoveryModel> =
        observeDiscovery(PlatformModel.Ios)

    private fun simulator(name: String, udid: String, state: String = "Shutdown"): SimulatorResource =
        SimulatorResource(name = name, udid = udid, runtime = "iOS 18.5", state = state)

    private fun handset(name: String, trusted: Boolean): IosTargetResource = IosTargetResource(
        name = name,
        udid = HANDSET_UDID,
        state = "connected",
        targetType = "device",
        trusted = trusted,
    )
}

private const val BOOTED_UDID = "11111111-2222-3333-4444-555555555555"
private const val COLD_UDID = "66666666-7777-8888-9999-000000000000"
private const val HANDSET_UDID = "00008110-001E2D3C1E88801E"

/** One line per emission, so a whole classification is one [assertEquals]. */
private fun DiscoveryModel.summary(): String = when (this) {
    is DiscoveryModel.Detecting -> "Detecting"
    is DiscoveryModel.ToolMissing -> "ToolMissing($tool)"
    is DiscoveryModel.NoDeviceFound -> "NoDeviceFound(attempts=$attempts)"
    is DiscoveryModel.PollFailed -> "PollFailed($tool $reason)"
    is DiscoveryModel.Found ->
        "Found(sims=" + emulators.joinToString("|") { "${it.avdName} ${if (it.isRunning) "running" else "cold"}" } + ")"
    is DiscoveryModel.SimulatorNotBooted -> "SimulatorNotBooted(${simulators.size} xcrun=$toolVersion)"
    is DiscoveryModel.DeviceNotTrusted -> "DeviceNotTrusted($deviceId sims=$simulatorsAvailable)"
}

/** An executor no iOS poll may reach; invoking it fails the test that let it happen. */
private object ExplodingExecutor : DeviceCommandExecutor {
    override suspend fun run(device: DeviceId, command: DeviceCommand): ByteArray =
        throw AssertionError("the executor was reached: $command on ${device.value}")
}

/** Records what was addressed, so a boot's udid is assertable without a process. */
private class RecordingExecutor : DeviceCommandExecutor {

    val calls: MutableList<Pair<String, DeviceCommand>> = mutableListOf()

    override suspend fun run(device: DeviceId, command: DeviceCommand): ByteArray {
        calls += device.value to command
        return ByteArray(0)
    }
}

/** No `adb` on this host - the iOS poll must never consult it anyway. */
private object AbsentAdbHostSource : AdbHostSource {
    override suspend fun probe(): ToolProbe = ToolProbe(kind = ToolKind.Adb, found = false)
    override suspend fun listDevices(): List<DeviceResource> = emptyList()
    override suspend fun startServer() = Unit
    override suspend fun killServer() = Unit
}

private object AbsentEmulatorSource : EmulatorSource {
    override suspend fun probe(): ToolProbe = ToolProbe(kind = ToolKind.Emulator, found = false)
    override suspend fun listAvds(): List<AvdResource> = emptyList()
    override suspend fun boot(avd: AvdName) = Unit
}

private object SilentTransport : MirrorTransport {
    override fun frames(device: DeviceId): Flow<MirrorFrame> = emptyFlow()
    override val stats: StateFlow<TransportStats> = MutableStateFlow(TransportStats())
}

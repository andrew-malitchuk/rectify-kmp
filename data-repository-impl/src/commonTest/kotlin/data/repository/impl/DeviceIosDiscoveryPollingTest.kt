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
import data.device.api.source.parse.parseIdbTargets
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
    fun `simulator rows from a real idb listing are not handsets`() = runTest {
        val hosts = FakeIosHostSource(
            found = true,
            simulators = listOf(simulator("iPhone 16", COLD_UDID)),
            // The captured payload, parsed by the production parser. `idb list-targets` lists
            // SIMULATORS too and on this host it lists nothing else, so a poll that read every
            // row as a connected handset reported 54 phones - all of them shut down - and made
            // SimulatorNotBooted unreachable on any Mac where idb resolves.
            devices = parseIdbTargets(IDB_LIST_TARGETS),
        )

        val first = repository(hosts).observeIos().drop(1).first()

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

/**
 * Raw stdout of `idb list-targets`, copied BYTE-FOR-BYTE from the canonical capture in
 * `data-device-api/src/commonTest/.../IosDiscoveryFixtures.kt`.
 *
 * Duplicated rather than imported because a `commonTest` source set is not published to its
 * consumers, and the point of this fixture is that the payload `pollIos` sees is the one the
 * tool really printed. Every row here is a `simulator` -- there is NO `| device |` row on this
 * host -- which is exactly what makes it the regression fixture for COR-1.
 */
private const val IDB_LIST_TARGETS: String = """iPad (10th generation) | 0CE8BC7C-8E6B-4FA8-A9D1-C5C61AACC946 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad (10th generation) | 209E0106-A82E-4C84-9058-59DEBD20B037 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad (10th generation) | F58C10ED-FCD1-4D11-A26C-8A338CBEEA60 | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPad (A16) | 0764116C-6504-429C-A2D1-E290024CA358 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPad (A16) | D36E9299-AC34-424A-9A19-6A89D2F28295 | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPad Air 11-inch (M2) | 6F1A7E2D-2CDE-4FBE-88E7-34C9328F20C4 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad Air 11-inch (M2) | AAE4BCC3-4783-4438-9CA7-71667BADBBE5 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad Air 11-inch (M2) | 9F4C90A5-8A71-4EE5-9E0C-4C917E1F46B5 | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPad Air 11-inch (M3) | 07346815-3946-493C-8CF9-B1F8D7D72621 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPad Air 11-inch (M3) | 99936AC8-57C9-41D7-A46C-8E3E11B56C84 | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPad Air 13-inch (M2) | CAC20292-3055-4B3B-92EE-BECDDADB7EC1 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad Air 13-inch (M2) | FF94CD04-F9D9-435A-9D3B-9F08D336D2B5 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad Air 13-inch (M2) | D32A6923-6194-4A6A-951E-33EAC1E84F37 | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPad Air 13-inch (M3) | 6E0BF003-763E-499B-9ABC-D2E9BBB3BBA3 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPad Air 13-inch (M3) | 4CC578CA-A262-4BEF-AF51-DD5141BB4B80 | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPad Pro 11-inch (M4) | 865DB6BF-801B-4EFC-A11F-721C9770DB66 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad Pro 11-inch (M4) | ADBAE3FE-92BA-4C1D-91DE-EFA5AEB787C1 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad Pro 11-inch (M4) | FA8351AE-8033-495C-BA87-9B072B9141EA | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPad Pro 11-inch (M4) | 41A2E426-A7DD-4380-9E91-FA46A9BE8124 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPad Pro 11-inch (M5) | 7C36DF04-3C33-4DDA-B298-8413702EAC3D | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPad Pro 13-inch (M4) | 9567B6A9-047A-4587-A10F-525D561601E9 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad Pro 13-inch (M4) | B0B5D60B-8C4E-47D3-96CC-CA7CF0308753 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad Pro 13-inch (M4) | 3227FEE7-9089-41CA-83E7-28223362C155 | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPad Pro 13-inch (M4) | D7D53914-E44B-434A-8E8A-989C5F0AA1D6 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPad Pro 13-inch (M5) | 73706957-A27C-41CB-8DAC-7B45FC7FE1D7 | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPad mini (6th generation) | 6F20E331-1692-4938-B491-92C6C2B8DADD | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad mini (A17 Pro) | 6DC67602-F428-4731-AED2-5B2936EA5715 | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPad mini (A17 Pro) | A37CFB7E-39CC-490C-8AAB-7B38A206C8D7 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPad mini (A17 Pro) | AD07D465-28C9-41BC-BDC0-B23486C41131 | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPhone 15 | 5CFB518E-2D13-4A76-ADA6-8234DF234EF7 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone 15 | 68D2823C-F382-4C4E-877D-0A29F56FD47A | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone 15 Plus | 98EAC1B6-882F-48CE-9E33-F6C5E9BB0FE0 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone 15 Plus | C4BD6AC6-9F71-402D-BC15-E7377253B053 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone 15 Pro | 4210645D-322E-43DB-B6FB-7E4F388D1DF5 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone 15 Pro | 77C17238-5B51-4206-9A58-CA601AF51E7E | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone 15 Pro Max | 98DBDA97-34DE-4652-933B-E3A348D40687 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone 15 Pro Max | D89E58B3-0B21-41CC-A9DF-F6E38DA22DCA | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone 16 | 5F20FFD2-3B90-42DA-9227-26725B78EA58 | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPhone 16 | 1122A235-8B04-4139-B652-88BDCF4727C4 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPhone 16 Plus | 6D100525-3C2F-4989-8EF4-E1BCCF346DBB | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPhone 16 Plus | 446A4CE5-DE51-4008-BA78-1060ED89E541 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPhone 16 Pro | 08F37FE6-AC58-4976-AE2B-4B7A980F8D7F | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPhone 16 Pro | 0CF85EBA-0C01-4E30-89BA-8DF4D2293AE9 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPhone 16 Pro Max | 6B5F435E-AF11-4C42-B2E3-8FFD24D1AF64 | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPhone 16 Pro Max | 79F75075-4405-4712-BACA-1118701F7D32 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPhone 16e | 73B83F89-9B46-473B-85F3-71CC61E4688E | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPhone 16e | E386D0BB-4A15-46BC-B933-00F7B485359B | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPhone 17 | B041CD5C-D279-4F6C-B7F1-69B6C991412D | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPhone 17 Pro | CE5DE0A3-61D7-4186-BF0D-7EC5360C3573 | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPhone 17 Pro Max | 495B63CB-89F5-49E1-8626-E801E9F0E234 | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPhone Air | 554ED76A-6C36-4046-83BB-E316CD387DA1 | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPhone SE (3rd generation) | 27E8D85C-C23A-4590-8AB7-2D9507A2F35C | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone SE (3rd generation) | FFE14503-792C-4500-96B2-623DF571FF09 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone SE (3rd generation) | 3DA2880B-CEF0-4FF2-912A-9D29F5F5667D | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
"""

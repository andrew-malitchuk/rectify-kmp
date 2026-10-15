package data.repository.impl

import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.EmulatorSource
import data.device.api.source.datasource.IdbHostSource
import data.device.api.source.datasource.MirrorTransport
import data.device.api.source.error.DeviceCommandException
import data.device.api.source.model.AvdName
import data.device.api.source.model.AvdResource
import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId
import data.device.api.source.model.DeviceResource
import data.device.api.source.model.DeviceStateResource
import data.device.api.source.model.MirrorFrame
import data.device.api.source.model.ToolKind
import data.device.api.source.model.ToolProbe
import data.device.api.source.model.TransportStats
import data.device.api.source.tuning.MirrorTuning
import data.repository.impl.source.DeviceRepositoryImpl
import domain.core.source.model.device.DiscoveryModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The discovery state machine under virtual time, with no device, no process and no real
 * two-second wait anywhere.
 */
class DeviceDiscoveryPollingTest {

    @Test
    fun `poll emissions land two seconds apart`() = runTest {
        val adb = FakeAdbHostSource()
        val received = collectDiscovery(adb)

        advanceTimeBy(4 * MirrorTuning.DiscoveryPollMs + 1)

        // Detecting is immediate, then one poll per DiscoveryPollMs.
        assertEquals(listOf(0L, 0L, 2_000L, 4_000L, 6_000L, 8_000L), received.map { it.first })
    }

    @Test
    fun `an empty poll stays passive until the escalation threshold`() = runTest {
        val adb = FakeAdbHostSource()
        val received = collectDiscovery(adb)

        // One poll short of the threshold.
        advanceTimeBy((MirrorTuning.EmptyPollEscalation - 2) * MirrorTuning.DiscoveryPollMs + 1)

        assertEquals(MirrorTuning.EmptyPollEscalation - 1, received.count { it.second is DiscoveryModel.Found })
        assertTrue(received.none { it.second is DiscoveryModel.NoDeviceFound })
    }

    @Test
    fun `the escalation poll reports attempts and the last poll time`() = runTest {
        val adb = FakeAdbHostSource(version = "Android Debug Bridge version 1.0.41")
        adb.devices = listOf(device("R5CT10ABCDE", DeviceStateResource.Unauthorized))
        val received = collectDiscovery(adb)

        advanceTimeBy((MirrorTuning.EmptyPollEscalation - 1) * MirrorTuning.DiscoveryPollMs + 1)

        val escalated = assertIs<DiscoveryModel.NoDeviceFound>(received.last().second)
        assertEquals(MirrorTuning.EmptyPollEscalation, escalated.attempts)
        assertEquals("Android Debug Bridge version 1.0.41", escalated.toolVersion)
        // An unauthorized phone is attached but unusable, and the user must not be told
        // there is no device while holding one.
        assertEquals(1, escalated.attachedCount)
        assertEquals(received.last().first, escalated.lastPollEpochMs)
        assertEquals(MirrorTuning.EmptyPollEscalation - 1, received.count { it.second is DiscoveryModel.Found })
    }

    @Test
    fun `a device attached after escalation produces Found again`() = runTest {
        val adb = FakeAdbHostSource()
        val received = collectDiscovery(adb)

        advanceTimeBy(MirrorTuning.EmptyPollEscalation * MirrorTuning.DiscoveryPollMs + 1)
        assertIs<DiscoveryModel.NoDeviceFound>(received.last().second)

        adb.devices = listOf(device("R5CT10ABCDE", DeviceStateResource.Device))
        advanceTimeBy(MirrorTuning.DiscoveryPollMs)

        val found = assertIs<DiscoveryModel.Found>(received.last().second)
        assertEquals(listOf("R5CT10ABCDE"), found.physical.map { it.id })
        assertTrue(found.physical.single().isSelectable)
    }

    @Test
    fun `a missing adb arrives as a ToolMissing value`() = runTest {
        val adb = FakeAdbHostSource(found = false)
        val received = collectDiscovery(adb)

        advanceTimeBy(1)

        val missing = assertIs<DiscoveryModel.ToolMissing>(received.last().second)
        assertEquals("adb", missing.tool)
        assertEquals(listOf("/opt/homebrew/bin/adb"), missing.probedPaths)
        assertEquals("brew install", missing.installCommand)
    }

    @Test
    fun `a missing adb never escalates to NoDeviceFound`() = runTest {
        val adb = FakeAdbHostSource(found = false)
        val received = collectDiscovery(adb)

        advanceTimeBy(5 * MirrorTuning.DiscoveryPollMs + 1)

        assertTrue(received.drop(1).all { it.second is DiscoveryModel.ToolMissing })
    }

    @Test
    fun `an avd both listed and running appears once as running`() = runTest {
        val adb = FakeAdbHostSource()
        adb.devices = listOf(device("emulator-5554", DeviceStateResource.Device, isEmulator = true))
        val emulators = FakeEmulatorSource(listOf("Pixel_7_API_34", "Tablet_API_33"))
        val executor = FakeDeviceCommandExecutor(avdNameOf = mapOf("emulator-5554" to "Pixel_7_API_34"))
        val received = collectDiscovery(adb, emulators, executor)

        advanceTimeBy(1)

        val found = assertIs<DiscoveryModel.Found>(received.last().second)
        assertTrue(found.physical.isEmpty())
        assertEquals(listOf("Pixel_7_API_34", "Tablet_API_33"), found.emulators.map { it.avdName })
        val running = found.emulators.single { it.isRunning }
        assertEquals("Pixel_7_API_34", running.avdName)
        assertEquals("emulator-5554", running.runningDeviceId)
    }

    @Test
    fun `a running emulator that is not listed still appears once`() = runTest {
        val adb = FakeAdbHostSource()
        adb.devices = listOf(device("emulator-5554", DeviceStateResource.Device, isEmulator = true))
        val emulators = FakeEmulatorSource(emptyList())
        val executor = FakeDeviceCommandExecutor(avdNameOf = mapOf("emulator-5554" to "Ghost_API_34"))
        val received = collectDiscovery(adb, emulators, executor)

        advanceTimeBy(1)

        val found = assertIs<DiscoveryModel.Found>(received.last().second)
        assertEquals(1, found.emulators.size)
        assertEquals("Ghost_API_34", found.emulators.single().avdName)
    }

    @Test
    fun `an emulator that will not name itself is listed under its serial`() = runTest {
        val adb = FakeAdbHostSource()
        adb.devices = listOf(device("emulator-5554", DeviceStateResource.Device, isEmulator = true))
        val received = collectDiscovery(adb, FakeEmulatorSource(emptyList()), FakeDeviceCommandExecutor())

        advanceTimeBy(1)

        val found = assertIs<DiscoveryModel.Found>(received.last().second)
        assertEquals("emulator-5554", found.emulators.single().avdName)
        assertTrue(found.emulators.single().isRunning)
    }

    @Test
    fun `the avd name of an attached serial is asked for once`() = runTest {
        val adb = FakeAdbHostSource()
        adb.devices = listOf(device("emulator-5554", DeviceStateResource.Device, isEmulator = true))
        val executor = FakeDeviceCommandExecutor(avdNameOf = mapOf("emulator-5554" to "Pixel_7_API_34"))
        collectDiscovery(adb, FakeEmulatorSource(listOf("Pixel_7_API_34")), executor)

        advanceTimeBy(3 * MirrorTuning.DiscoveryPollMs + 1)

        assertEquals(1, executor.calls)
    }

    @Test
    fun `a failed poll is reported and the next tick polls again`() = runTest {
        val adb = FakeAdbHostSource()
        adb.devices = listOf(device("R5CT10ABCDE", DeviceStateResource.Device))
        adb.failuresLeft = 1
        val received = collectDiscovery(adb)

        advanceTimeBy(1)
        val failed = assertIs<DiscoveryModel.PollFailed>(received.last().second)
        assertEquals("adb", failed.tool)
        assertEquals("timed out after 3000ms", failed.reason)

        // The stream is still alive - a terminal operator would have ended it here.
        advanceTimeBy(MirrorTuning.DiscoveryPollMs)
        val found = assertIs<DiscoveryModel.Found>(received.last().second)
        assertEquals(listOf("R5CT10ABCDE"), found.physical.map { it.id })
    }

    @Test
    fun `a stream of failed polls keeps polling forever`() = runTest {
        val adb = FakeAdbHostSource()
        adb.failuresLeft = Int.MAX_VALUE
        val received = collectDiscovery(adb)

        advanceTimeBy(5 * MirrorTuning.DiscoveryPollMs + 1)

        assertEquals(6, received.count { it.second is DiscoveryModel.PollFailed })
    }

    @Test
    fun `a failed poll does not advance the empty poll escalation`() = runTest {
        val adb = FakeAdbHostSource()
        val received = collectDiscovery(adb)

        // Two empty polls, then one that never answered, then the third empty one. If the
        // failure counted as empty the escalation would already have fired by now.
        advanceTimeBy(MirrorTuning.DiscoveryPollMs + 1)
        adb.failuresLeft = 1
        advanceTimeBy(MirrorTuning.DiscoveryPollMs)
        assertIs<DiscoveryModel.PollFailed>(received.last().second)
        assertTrue(received.none { it.second is DiscoveryModel.NoDeviceFound })

        advanceTimeBy(MirrorTuning.DiscoveryPollMs)
        val escalated = assertIs<DiscoveryModel.NoDeviceFound>(received.last().second)
        assertEquals(MirrorTuning.EmptyPollEscalation, escalated.attempts)
    }

    /**
     * Starts collecting the discovery flow on the background scope and returns the list it
     * fills, each emission stamped with the virtual time it arrived at.
     */
    private fun TestScope.collectDiscovery(
        adb: AdbHostSource,
        emulators: EmulatorSource = FakeEmulatorSource(emptyList()),
        executor: DeviceCommandExecutor = FakeDeviceCommandExecutor(),
    ): List<Pair<Long, DiscoveryModel>> {
        val repository = DeviceRepositoryImpl(
            adbHostSource = adb,
            idbHostSource = FakeIdbHostSource(),
            emulatorSource = emulators,
            deviceCommandExecutor = executor,
            mirrorTransport = FakeMirrorTransport,
            nowMs = { testScheduler.currentTime },
        )
        val received = mutableListOf<Pair<Long, DiscoveryModel>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeDiscovery().collect { received += testScheduler.currentTime to it }
        }
        return received
    }

    private fun device(
        serial: String,
        state: DeviceStateResource,
        isEmulator: Boolean = false,
    ): DeviceResource = DeviceResource(
        id = requireNotNull(DeviceId.of(serial)),
        state = state,
        isEmulator = isEmulator,
    )
}

/** Scriptable [AdbHostSource]; every call returns instantly, so only the poll delay moves time. */
private class FakeAdbHostSource(
    private val found: Boolean = true,
    private val version: String = "1.0.41",
) : AdbHostSource {

    var devices: List<DeviceResource> = emptyList()

    /** Set to make the next [listDevices] calls fail, the way a cold adb server does. */
    var failuresLeft: Int = 0

    override suspend fun probe(): ToolProbe = ToolProbe(
        kind = ToolKind.Adb,
        found = found,
        path = "/opt/homebrew/bin/adb".takeIf { found },
        version = version.takeIf { found },
        probedPaths = listOf("/opt/homebrew/bin/adb"),
        installCommand = "brew install",
        envHint = "set ANDROID_HOME",
    )

    override suspend fun listDevices(): List<DeviceResource> {
        if (failuresLeft > 0) {
            failuresLeft--
            throw DeviceCommandException(listOf("adb", "devices", "-l"), null, "timed out after 3000ms")
        }
        return devices
    }
}

/** An `idb` probe that answers without touching a process. */
internal class FakeIdbHostSource(
    private val found: Boolean = false,
) : IdbHostSource {

    override suspend fun probe(): ToolProbe = ToolProbe(
        kind = ToolKind.Idb,
        found = found,
        path = "/opt/homebrew/bin/idb".takeIf { found },
        probedPaths = listOf("/opt/homebrew/bin/idb"),
        installCommand = "brew tap facebook/fb && brew install idb-companion",
    )
}

private class FakeEmulatorSource(avds: List<String>) : EmulatorSource {

    private val avds: List<AvdResource> = avds.map { AvdResource(requireNotNull(AvdName.of(it))) }

    override suspend fun probe(): ToolProbe = ToolProbe(kind = ToolKind.Emulator, found = true)

    override suspend fun listAvds(): List<AvdResource> = avds

    override suspend fun boot(avd: AvdName) = Unit
}

/** Answers `emu avd name`; an unmapped serial answers with an error line, as a cold one does. */
private class FakeDeviceCommandExecutor(
    private val avdNameOf: Map<String, String> = emptyMap(),
) : DeviceCommandExecutor {

    var calls: Int = 0
        private set

    override suspend fun run(device: DeviceId, command: DeviceCommand): ByteArray {
        calls++
        val name = avdNameOf[device.value] ?: return "KO: unknown command\n".encodeToByteArray()
        return "$name\nOK\n".encodeToByteArray()
    }
}

private object FakeMirrorTransport : MirrorTransport {
    override fun frames(device: DeviceId): Flow<MirrorFrame> = emptyFlow()
    override val stats: StateFlow<TransportStats> = MutableStateFlow(TransportStats())
}

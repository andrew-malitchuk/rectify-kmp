package data.repository.impl

import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.EmulatorSource
import data.device.api.source.datasource.MirrorTransport
import data.device.api.source.model.AvdName
import data.device.api.source.model.AvdResource
import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId
import data.device.api.source.model.DeviceResource
import data.device.api.source.model.MirrorFrame
import data.device.api.source.model.ToolKind
import data.device.api.source.model.ToolProbe
import data.device.api.source.model.TransportStats
import data.repository.impl.source.DeviceRepositoryImpl
import domain.core.source.model.device.PlatformModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** AC-1 and AC-45 at the repository seam: both tools are probed, latency is never invented. */
class DeviceToolDetectionTest {

    @Test
    fun `both platforms report a probed tool`() = runTest {
        val repository = repository(ios = FakeIosHostSource(found = true))

        val tools = repository.detectTools()

        assertEquals(listOf(PlatformModel.Android, PlatformModel.Ios), tools.map { it.platform })
        assertTrue(tools.single { it.platform == PlatformModel.Ios }.isInstalled)
        // `xcrun`, not `idb`: the dot reports the binary a session would actually use, and
        // probeSimctl additionally proves the simctl SUBCOMMAND resolves behind the shim.
        assertEquals("/usr/bin/xcrun", tools.single { it.platform == PlatformModel.Ios }.path)
    }

    @Test
    fun `an absent xcrun is reported as missing with the paths that were tried`() = runTest {
        val repository = repository(ios = FakeIosHostSource(found = false))

        val ios = repository.detectTools().single { it.platform == PlatformModel.Ios }

        // Not installed, but still a PROBE: the candidate paths prove a probe ran, which a
        // hardcoded constant could not produce.
        assertTrue(!ios.isInstalled)
        assertEquals(listOf("/usr/bin/xcrun"), ios.probedPaths)
        assertEquals("xcode-select --install", ios.installCommand)
    }

    @Test
    fun `a capture round trip never reaches the status bar as latency`() = runTest {
        // AC-45: the transport measures its own round-trip and leaves latencyMs null; the
        // repository must pass that null through so the UI renders its placeholder.
        val transport = StatsTransport(TransportStats(fps = 4.2f, captureRoundTripMs = 260L, widthPx = 1080))

        val stats = repository(transport = transport).observeMirrorStats(PlatformModel.Android).first()

        assertNull(stats.latencyMs)
        assertEquals(4.2f, stats.fps)
    }

    private fun repository(
        ios: FakeIosHostSource = FakeIosHostSource(),
        transport: MirrorTransport = StatsTransport(TransportStats()),
    ) = DeviceRepositoryImpl(
        adbHostSource = StubAdbHostSource,
        iosHostSource = ios,
        emulatorSource = StubEmulatorSource,
        androidExecutor = StubDeviceCommandExecutor,
        iosExecutor = StubDeviceCommandExecutor,
        androidTransport = transport,
        iosTransport = transport,
        adbPathPreferenceSource = FakeAdbPathPreferenceSource(),
    )
}

private object StubAdbHostSource : AdbHostSource {
    override suspend fun probe(): ToolProbe =
        ToolProbe(kind = ToolKind.Adb, found = true, path = "/opt/homebrew/bin/adb", version = "35.0.1")

    override suspend fun listDevices(): List<DeviceResource> = emptyList()

    override suspend fun startServer() = Unit

    override suspend fun killServer() = Unit
}

private object StubEmulatorSource : EmulatorSource {
    override suspend fun probe(): ToolProbe = ToolProbe(kind = ToolKind.Emulator, found = true)
    override suspend fun listAvds(): List<AvdResource> = emptyList()
    override suspend fun boot(avd: AvdName) = Unit
}

private object StubDeviceCommandExecutor : DeviceCommandExecutor {
    override suspend fun run(device: DeviceId, command: DeviceCommand): ByteArray = ByteArray(0)
}

private class StatsTransport(stats: TransportStats) : MirrorTransport {
    override fun frames(device: DeviceId): Flow<MirrorFrame> = emptyFlow()
    override val stats: StateFlow<TransportStats> = MutableStateFlow(stats)
}

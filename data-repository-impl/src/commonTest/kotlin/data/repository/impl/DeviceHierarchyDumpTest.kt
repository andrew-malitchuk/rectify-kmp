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
import domain.core.source.model.device.HierarchySnapshotModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * [DeviceRepositoryImpl.dumpHierarchy] against a fake [DeviceCommandExecutor] - no phone, no
 * process.
 *
 * TAC-9 / AC-69 / AC-72, REWORDED post-T-3: `DeviceCommand.UiAutomatorDump` and
 * `DeviceCommand.WmDensity` are two separate sealed cases with two separate argvs (T-3's
 * frozen `DeviceArgvTest`), so one executor call cannot carry both. Density is a static
 * device property and the frame size comes from the dump's own root bounds, so there is no
 * drift mechanism between two calls a fraction of a second apart - the invariant worth
 * pinning is that [DeviceRepositoryImpl.dumpHierarchy] always pairs the two inside ONE
 * repository call, never building a snapshot from one without the other.
 */
class DeviceHierarchyDumpTest {

    @Test
    fun `dumpHierarchy pairs the dump with wm density inside a single repository call`() = runTest {
        val executor = FakeHierarchyDumpExecutor(dumpStdout = PARSEABLE_DUMP, densityStdout = DENSITY_LINE)
        val repository = repository(executor)

        repository.dumpHierarchy("R5CT10ABCDE")

        // Fails an implementation that issues the dump and forgets the density (or vice
        // versa), and one that issues either command more than once.
        assertEquals(setOf(DeviceCommand.UiAutomatorDump, DeviceCommand.WmDensity), executor.commands.toSet())
        assertEquals(2, executor.commands.size)
    }

    @Test
    fun `a parseable dump maps to the snapshot with the parsed densityDpi`() = runTest {
        val executor = FakeHierarchyDumpExecutor(dumpStdout = PARSEABLE_DUMP, densityStdout = DENSITY_LINE)
        val repository = repository(executor)

        val snapshot = repository.dumpHierarchy("R5CT10ABCDE")

        assertEquals(420, snapshot.densityDpi)
        assertEquals(2, snapshot.nodes.size)
        val button = snapshot.nodes.single { it.id == "android:id/button1" }
        assertEquals("android.widget.Button", button.className)
        assertEquals(100, button.left)
        assertEquals(200, button.top)
        assertEquals(300, button.right)
        assertEquals(260, button.bottom)
        assertTrue(button.isTappable)
        assertEquals(1, button.depth)
    }

    @Test
    fun `garbage stdout maps to an Empty-shaped snapshot`() = runTest {
        val executor = FakeHierarchyDumpExecutor(dumpStdout = "not xml at all, just garbage", densityStdout = "")
        val repository = repository(executor)

        assertEquals(HierarchySnapshotModel.Empty, repository.dumpHierarchy("R5CT10ABCDE"))
    }

    @Test
    fun `empty stdout maps to an Empty-shaped snapshot`() = runTest {
        val executor = FakeHierarchyDumpExecutor(dumpStdout = "", densityStdout = "")
        val repository = repository(executor)

        assertEquals(HierarchySnapshotModel.Empty, repository.dumpHierarchy("R5CT10ABCDE"))
    }

    @Test
    fun `an absent density maps to densityDpi 0`() = runTest {
        val executor = FakeHierarchyDumpExecutor(dumpStdout = PARSEABLE_DUMP, densityStdout = "")
        val repository = repository(executor)

        val snapshot = repository.dumpHierarchy("R5CT10ABCDE")

        assertEquals(0, snapshot.densityDpi)
        // Not the Empty case: nodes are still present, only the density is unknown. Per the
        // plan's ruling, 0 is the sentinel that makes PixelUnits fall back to MeasureUnit.Px
        // rather than showing a wrong dp figure.
        assertEquals(2, snapshot.nodes.size)
    }

    private fun repository(executor: DeviceCommandExecutor): DeviceRepositoryImpl = DeviceRepositoryImpl(
        adbHostSource = NoOpAdbHostSource,
        idbHostSource = FakeIdbHostSource(),
        emulatorSource = NoOpEmulatorSource,
        deviceCommandExecutor = executor,
        mirrorTransport = NoOpMirrorTransport,
        adbPathPreferenceSource = FakeAdbPathPreferenceSource(),
    )
}

/**
 * Root `android.widget.FrameLayout` (depth 0, not tappable) with one tappable
 * `android:id/button1` child (depth 1), followed by the AOSP "hierchary" trailer -
 * [DeviceCommand.UiAutomatorDump]'s own stdout shape, carrying no density of its own.
 */
private const val PARSEABLE_DUMP = """<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><hierarchy rotation="0"><node index="0" text="" resource-id="" class="android.widget.FrameLayout" package="dev.rectify.io" content-desc="" clickable="false" bounds="[0,0][1080,2400]"><node index="0" text="OK" resource-id="android:id/button1" class="android.widget.Button" package="dev.rectify.io" content-desc="" clickable="true" bounds="[100,200][300,260]" /></node></hierarchy>UI hierchary dumped to: /dev/tty
"""

/** [DeviceCommand.WmDensity]'s own stdout shape. */
private const val DENSITY_LINE = "Physical density: 420\n"

/**
 * Dispatches per [DeviceCommand] - [dumpStdout] for [DeviceCommand.UiAutomatorDump],
 * [densityStdout] for [DeviceCommand.WmDensity] - and records the sequence received, so the
 * pairing invariant is assertable regardless of which order the implementation issues them in.
 */
private class FakeHierarchyDumpExecutor(
    private val dumpStdout: String,
    private val densityStdout: String,
) : DeviceCommandExecutor {

    val commands: MutableList<DeviceCommand> = mutableListOf()

    override suspend fun run(device: DeviceId, command: DeviceCommand): ByteArray {
        commands += command
        val stdout = when (command) {
            DeviceCommand.UiAutomatorDump -> dumpStdout
            DeviceCommand.WmDensity -> densityStdout
            else -> error("unexpected command $command")
        }
        return stdout.encodeToByteArray()
    }
}

/** Never expected to be called by [DeviceRepositoryImpl.dumpHierarchy]. */
private object NoOpAdbHostSource : AdbHostSource {
    override suspend fun probe(): ToolProbe = ToolProbe(kind = ToolKind.Adb, found = true)
    override suspend fun listDevices(): List<DeviceResource> = emptyList()
    override suspend fun startServer() = Unit
    override suspend fun killServer() = Unit
}

/** Never expected to be called by [DeviceRepositoryImpl.dumpHierarchy]. */
private object NoOpEmulatorSource : EmulatorSource {
    override suspend fun probe(): ToolProbe = ToolProbe(kind = ToolKind.Emulator, found = true)
    override suspend fun listAvds(): List<AvdResource> = emptyList()
    override suspend fun boot(avd: AvdName) = Unit
}

/** Never expected to be called by [DeviceRepositoryImpl.dumpHierarchy]. */
private object NoOpMirrorTransport : MirrorTransport {
    override fun frames(device: DeviceId): Flow<MirrorFrame> = emptyFlow()
    override val stats: StateFlow<TransportStats> = MutableStateFlow(TransportStats())
}

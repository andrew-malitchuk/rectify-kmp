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
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

/**
 * The two degradation rulings of [DeviceRepositoryImpl.dumpHierarchy], which were KDoc-only
 * until wave 4's T-5 follow-up.
 *
 * Separate from the frozen `DeviceHierarchyDumpTest` so that file stays untouched; these need
 * an executor that can FAIL per command, which the frozen fake cannot do.
 */
class DeviceHierarchyDumpFallbackTest {

    @Test
    fun `a thrown wm density degrades to the 0 sentinel while a thrown dump propagates`() = runTest {
        // Half one. The dump is the ~2.7s payload; losing it because a trivial density probe
        // failed is the wrong trade, and densityDpi 0 makes the UI label px rather than a
        // confidently wrong dp.
        val densityFailed = repository(
            FailingHierarchyDumpExecutor(
                dumpStdout = PARSEABLE_DUMP,
                densityStdout = DENSITY_LINE,
                failing = DeviceCommand.WmDensity,
            )
        )

        val snapshot = densityFailed.dumpHierarchy("R5CT10ABCDE")

        assertEquals(0, snapshot.densityDpi)
        assertEquals(2, snapshot.nodes.size)
        assertEquals(1080, snapshot.frameWidthPx)

        // Half two, asserted in the same test because the ASYMMETRY is the decision - two
        // separate tests would let a future change collapse one onto the other with both
        // still green.
        val dumpFailed = repository(
            FailingHierarchyDumpExecutor(
                dumpStdout = PARSEABLE_DUMP,
                densityStdout = DENSITY_LINE,
                failing = DeviceCommand.UiAutomatorDump,
            )
        )

        assertFailsWith<DeviceCommandException> { dumpFailed.dumpHierarchy("R5CT10ABCDE") }
    }

    @Test
    fun `a readable dump with no nodes keeps the parsed density instead of collapsing to Empty`() = runTest {
        val executor = FailingHierarchyDumpExecutor(
            dumpStdout = NODELESS_DUMP,
            densityStdout = DENSITY_LINE,
            failing = null,
        )

        val snapshot = repository(executor).dumpHierarchy("R5CT10ABCDE")

        // The density came from a separate command and is a real measurement - measure mode's
        // "View (no match)" fallback still needs it to label its numbers in dp.
        assertEquals(420, snapshot.densityDpi)
        assertEquals(emptyList(), snapshot.nodes)
        assertNotEquals(HierarchySnapshotModel.Empty, snapshot)
        // No nodes means no root, and the frame IS the root's bounds - so 0 here is the
        // honest answer, not a discarded measurement.
        assertEquals(0, snapshot.frameWidthPx)
        assertEquals(0, snapshot.frameHeightPx)
    }

    private fun repository(executor: DeviceCommandExecutor): DeviceRepositoryImpl = DeviceRepositoryImpl(
        adbHostSource = FallbackNoOpAdbHostSource,
        idbHostSource = FakeIdbHostSource(),
        emulatorSource = FallbackNoOpEmulatorSource,
        deviceCommandExecutor = executor,
        mirrorTransport = FallbackNoOpMirrorTransport,
        adbPathPreferenceSource = FakeAdbPathPreferenceSource(),
    )
}

/** Same shape as the frozen fixture's dump - root FrameLayout plus one tappable child. */
private const val PARSEABLE_DUMP = """<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><hierarchy rotation="0"><node index="0" text="" resource-id="" class="android.widget.FrameLayout" package="dev.rectify.io" content-desc="" clickable="false" bounds="[0,0][1080,2400]"><node index="0" text="OK" resource-id="android:id/button1" class="android.widget.Button" package="dev.rectify.io" content-desc="" clickable="true" bounds="[100,200][300,260]" /></node></hierarchy>UI hierchary dumped to: /dev/tty
"""

/**
 * READABLE stdout that simply contains no nodes - the distinct case from garbage stdout.
 * `uiautomator` answers this way against a window whose root it cannot serialise.
 */
private const val NODELESS_DUMP = """<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><hierarchy rotation="0"></hierarchy>UI hierchary dumped to: /dev/tty
"""

private const val DENSITY_LINE = "Physical density: 420\n"

/** Answers both commands, except [failing], which throws as a real tool failure does. */
private class FailingHierarchyDumpExecutor(
    private val dumpStdout: String,
    private val densityStdout: String,
    private val failing: DeviceCommand?,
) : DeviceCommandExecutor {

    override suspend fun run(device: DeviceId, command: DeviceCommand): ByteArray {
        if (command == failing) {
            throw DeviceCommandException(listOf("adb", "shell"), 1, "device offline")
        }
        val stdout = when (command) {
            DeviceCommand.UiAutomatorDump -> dumpStdout
            DeviceCommand.WmDensity -> densityStdout
            else -> error("unexpected command $command")
        }
        return stdout.encodeToByteArray()
    }
}

/** Never expected to be called by [DeviceRepositoryImpl.dumpHierarchy]. */
private object FallbackNoOpAdbHostSource : AdbHostSource {
    override suspend fun probe(): ToolProbe = ToolProbe(kind = ToolKind.Adb, found = true)
    override suspend fun listDevices(): List<DeviceResource> = emptyList()
    override suspend fun startServer() = Unit
    override suspend fun killServer() = Unit
}

/** Never expected to be called by [DeviceRepositoryImpl.dumpHierarchy]. */
private object FallbackNoOpEmulatorSource : EmulatorSource {
    override suspend fun probe(): ToolProbe = ToolProbe(kind = ToolKind.Emulator, found = true)
    override suspend fun listAvds(): List<AvdResource> = emptyList()
    override suspend fun boot(avd: AvdName) = Unit
}

/** Never expected to be called by [DeviceRepositoryImpl.dumpHierarchy]. */
private object FallbackNoOpMirrorTransport : MirrorTransport {
    override fun frames(device: DeviceId): Flow<MirrorFrame> = emptyFlow()
    override val stats: StateFlow<TransportStats> = MutableStateFlow(TransportStats())
}

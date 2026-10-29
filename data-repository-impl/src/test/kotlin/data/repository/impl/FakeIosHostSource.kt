package data.repository.impl

import data.device.api.source.datasource.IosHostSource
import data.device.api.source.error.DeviceCommandException
import data.device.api.source.model.IosTargetResource
import data.device.api.source.model.SimulatorResource
import data.device.api.source.model.ToolKind
import data.device.api.source.model.ToolProbe

/**
 * Scriptable [IosHostSource]; every call answers instantly, so only the poll delay moves time.
 *
 * Shared by every repository test that has to construct a [data.repository.impl.source
 * .DeviceRepositoryImpl], which is all of them: the iOS host source is a constructor
 * parameter whether or not the test under way polls iOS.
 *
 * @property found Whether `xcrun` AND the `simctl` utility behind it resolve.
 * @property version Xcode version string the probe reports.
 * @property simulators Rows `xcrun simctl list devices available` returns.
 * @property devices Rows `idb list-targets` returns.
 * @property simulatorFailure Raised by [listSimulators] instead of answering — the parser
 *   drift path, which must reach the user as `PollFailed`, not as an empty bench.
 * @property deviceFailure Raised by [listDevices] instead of answering — a missing
 *   `idb_companion`, which must degrade the physical list without erasing the simulators.
 */
internal class FakeIosHostSource(
    private val found: Boolean = false,
    private val version: String = "16.2",
    var simulators: List<SimulatorResource> = emptyList(),
    var devices: List<IosTargetResource> = emptyList(),
    var simulatorFailure: DeviceCommandException? = null,
    var deviceFailure: DeviceCommandException? = null,
) : IosHostSource {

    override suspend fun probeSimctl(): ToolProbe = ToolProbe(
        kind = ToolKind.Simctl,
        found = found,
        path = "/usr/bin/xcrun".takeIf { found },
        version = version.takeIf { found },
        probedPaths = listOf("/usr/bin/xcrun"),
        installCommand = "xcode-select --install",
    )

    override suspend fun probeIdb(): ToolProbe = ToolProbe(
        kind = ToolKind.Idb,
        found = found,
        path = "/opt/homebrew/bin/idb".takeIf { found },
        probedPaths = listOf("/opt/homebrew/bin/idb"),
        installCommand = "brew tap facebook/fb && brew install idb-companion",
    )

    override suspend fun listSimulators(): List<SimulatorResource> =
        simulatorFailure?.let { throw it } ?: simulators

    override suspend fun listDevices(): List<IosTargetResource> =
        deviceFailure?.let { throw it } ?: devices
}

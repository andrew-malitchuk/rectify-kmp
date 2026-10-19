package data.device.api.source.datasource

import data.device.api.source.model.IosTargetResource
import data.device.api.source.model.SimulatorResource
import data.device.api.source.model.ToolProbe

/**
 * The deviceless half of the iOS toolchain — `xcrun simctl` and `idb` behind one contract.
 *
 * ONE interface for two binaries, because one discovery poll needs both answers together: the
 * simulator list comes from `simctl` and the physical-handset list only from `idb`, and a
 * split pair of probe-only interfaces (the shape of the deleted `IdbHostSource`) would
 * duplicate the registration and the fake without buying a seam anyone uses.
 *
 * There is deliberately no `bootSimulator` here: booting addresses ONE udid, so it is a
 * [data.device.api.source.model.DeviceCommand] run through [DeviceCommandExecutor], not a
 * deviceless host call.
 *
 * Every member returns a SUCCESS VALUE. A missing binary is `ToolProbe(found = false, …)` and
 * an empty bench is an empty list — neither is an exception, per `.claude/rules/error-handling`.
 */
public interface IosHostSource {

    /**
     * Locates `xcrun` and reads the Xcode shim's version.
     *
     * The version string is Xcode's own: `simctl` has no `--version` of its own, so this is
     * the only version the iOS half can report.
     */
    public suspend fun probeSimctl(): ToolProbe

    /**
     * Locates `idb`.
     *
     * [ToolProbe.version] is `null` even when found — `idb` answers `--version` with its usage
     * block rather than a version, as the capture on this host recorded.
     */
    public suspend fun probeIdb(): ToolProbe

    /** Lists available simulators via `xcrun simctl list devices available`; may be empty. */
    public suspend fun listSimulators(): List<SimulatorResource>

    /**
     * Lists physical iOS targets via `idb list-targets`; may be empty.
     *
     * Returns the untrusted ones too — [IosTargetResource.trusted] classifies them — because a
     * handset the user is holding has to be visible even when it cannot be driven yet.
     */
    public suspend fun listDevices(): List<IosTargetResource>
}

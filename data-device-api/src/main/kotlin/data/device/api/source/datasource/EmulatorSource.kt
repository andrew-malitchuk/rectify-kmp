package data.device.api.source.datasource

import data.device.api.source.model.AvdName
import data.device.api.source.model.AvdResource
import data.device.api.source.model.ToolProbe

/** The deviceless half of the `emulator` tool. */
public interface EmulatorSource {

    /**
     * Locates the `emulator` binary and reads its version.
     *
     * A missing binary yields `ToolProbe(found = false, …)`, never an exception.
     */
    public suspend fun probe(): ToolProbe

    /** Lists configured AVDs. */
    public suspend fun listAvds(): List<AvdResource>

    /**
     * Boots [avd], spawning the emulator detached and untimed.
     *
     * A lock-file non-zero exit for an already-running AVD is a no-op success.
     */
    public suspend fun boot(avd: AvdName)
}

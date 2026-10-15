package data.device.api.source.datasource

import data.device.api.source.model.DeviceResource
import data.device.api.source.model.ToolProbe

/**
 * The deviceless half of `adb`, enumerated explicitly so that "this command has no
 * device" stays an auditable exception rather than a nullable parameter on
 * [DeviceCommandExecutor].
 */
public interface AdbHostSource {

    /**
     * Locates `adb` and reads its version.
     *
     * A missing binary yields `ToolProbe(found = false, …)`, never an exception.
     */
    public suspend fun probe(): ToolProbe

    /** Lists attached devices, including the ones that are not usable. */
    public suspend fun listDevices(): List<DeviceResource>
}

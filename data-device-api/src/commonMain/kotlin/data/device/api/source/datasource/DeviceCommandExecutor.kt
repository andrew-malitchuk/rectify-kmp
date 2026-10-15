package data.device.api.source.datasource

import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId

/**
 * The sole device-scoped execution path.
 *
 * One method, one overload, and [device] is neither nullable nor defaultable — a command
 * cannot be run without a resolved device.
 */
public interface DeviceCommandExecutor {

    /**
     * Runs [command] against [device] and returns its raw stdout.
     *
     * @throws data.device.api.source.error.DeviceCommandException on a non-zero exit,
     *   a timeout, or a missing binary.
     */
    public suspend fun run(device: DeviceId, command: DeviceCommand): ByteArray
}

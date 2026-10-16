package domain.repository.api.source.repository

import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.MirrorFrameModel
import domain.core.source.model.device.MirrorStatsModel
import domain.core.source.model.device.ToolModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository for discovering devices and mirroring one of them.
 *
 * Domain models only — no data-layer resource type crosses this boundary, which is also why
 * the identifiers here are plain [String]s: validating them back into a device id is the
 * data layer's job.
 */
public interface DeviceRepository {

    /**
     * Probes for the external platform tools.
     *
     * A missing tool is reported as `isInstalled = false`, never as an exception.
     */
    public suspend fun detectTools(): List<ToolModel>

    /**
     * Polls for attached devices and configured emulators.
     *
     * Emits [DiscoveryModel.Detecting] immediately, then one value per poll, forever, until
     * the collector stops. Every emission is a success value, a missing tool included.
     */
    public fun observeDiscovery(): Flow<DiscoveryModel>

    /**
     * Boots the emulator image named [avdName].
     *
     * Returns as soon as the emulator is launched; booting completes in the background and
     * the next discovery poll reports it as running.
     */
    public suspend fun bootEmulator(avdName: String)

    /**
     * Streams frames from the device identified by [deviceId].
     *
     * Cancelling the collector stops the capture loop and kills any in-flight child process.
     */
    public fun mirror(deviceId: String): Flow<MirrorFrameModel>

    /** Live statistics of the active mirroring session. */
    public fun observeMirrorStats(): Flow<MirrorStatsModel>

    /**
     * Restarts the `adb` daemon: kills it, then starts it again.
     *
     * Both halves are needed and in that order. `start-server` alone is a "make sure one
     * exists" command, not a repair command, so it cannot clear a daemon that is alive but
     * not serving — measured against a wedged daemon, it hangs there. Only killing it first
     * does, which is why the recovery action is a restart and not a start.
     *
     * Fails rather than reporting a success it did not achieve, so a dead `adb` is visible
     * instead of a button that appears to have worked.
     */
    public suspend fun restartAdbServer()

    /**
     * Records the path to the `adb` binary the user pointed Rectify at, or clears it when
     * [path] is empty.
     *
     * The path is deliberately NOT validated here. It arrives from a file dialog, so it is
     * outside the program's control, and the only honest place to check it is against the
     * filesystem at probe time — by the same `isExecutable` predicate every other candidate
     * goes through. Validating it twice, in two places, is how the two checks drift apart.
     *
     * A stored path that stops working is therefore not an error state: the resolver simply
     * falls through to the ordered candidate list, which is what it would have done had the
     * user never chosen anything.
     */
    public suspend fun setAdbPath(path: String)
}

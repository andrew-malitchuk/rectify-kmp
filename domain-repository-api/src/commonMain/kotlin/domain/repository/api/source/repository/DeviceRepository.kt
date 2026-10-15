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
}

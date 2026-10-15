package data.device.api.source.datasource

import data.device.api.source.model.DeviceId
import data.device.api.source.model.MirrorFrame
import data.device.api.source.model.TransportStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * The frame source seam: swapping screen-capture for another transport replaces the
 * implementation bound to this interface and nothing else.
 */
public interface MirrorTransport {

    /**
     * Streams frames from [device].
     *
     * Cancelling the collector stops the loop and destroys any in-flight child process.
     */
    public fun frames(device: DeviceId): Flow<MirrorFrame>

    /** Live statistics of the active session. */
    public val stats: StateFlow<TransportStats>
}

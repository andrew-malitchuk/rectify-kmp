package domain.usecase.api.source.usecase.device

import domain.core.source.model.device.MirrorFrameModel
import kotlinx.coroutines.flow.Flow

/**
 * Use case for streaming frames from a selected device.
 */
public interface StartMirroringUseCase {
    /**
     * @param deviceId Identifier of the device to mirror.
     * @return A [Flow] of frames. Cancelling the collector ends the session and kills any
     *   in-flight child process.
     */
    public operator fun invoke(deviceId: String): Flow<MirrorFrameModel>
}

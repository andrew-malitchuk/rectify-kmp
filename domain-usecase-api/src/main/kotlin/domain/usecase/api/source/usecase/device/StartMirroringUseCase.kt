package domain.usecase.api.source.usecase.device

import domain.core.source.model.device.MirrorFrameModel
import domain.core.source.model.device.PlatformModel
import kotlinx.coroutines.flow.Flow

/**
 * Use case for streaming frames from a selected device.
 */
public interface StartMirroringUseCase {
    /**
     * @param platform Which device family [deviceId] belongs to.
     * @param deviceId Identifier of the device to mirror.
     * @return A [Flow] of frames. Cancelling the collector ends the session and kills any
     *   in-flight child process.
     */
    public operator fun invoke(platform: PlatformModel, deviceId: String): Flow<MirrorFrameModel>
}

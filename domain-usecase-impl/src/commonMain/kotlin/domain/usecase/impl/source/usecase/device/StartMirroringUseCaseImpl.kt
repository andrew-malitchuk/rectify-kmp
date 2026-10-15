package domain.usecase.impl.source.usecase.device

import domain.core.source.model.device.MirrorFrameModel
import domain.repository.api.source.repository.DeviceRepository
import domain.usecase.api.source.usecase.device.StartMirroringUseCase
import kotlinx.coroutines.flow.Flow

/**
 * Default implementation of [StartMirroringUseCase].
 *
 * A session has no deadline — it runs until the collector stops or the device goes away, at
 * which point the flow fails and the caller ends the session.
 *
 * @property deviceRepository Repository used for streaming frames.
 */
internal class StartMirroringUseCaseImpl(
    private val deviceRepository: DeviceRepository,
) : StartMirroringUseCase {

    /**
     * @param deviceId Identifier of the device to mirror.
     * @return A [Flow] of [MirrorFrameModel].
     * @see StartMirroringUseCase.invoke
     */
    override fun invoke(deviceId: String): Flow<MirrorFrameModel> = deviceRepository.mirror(deviceId)
}

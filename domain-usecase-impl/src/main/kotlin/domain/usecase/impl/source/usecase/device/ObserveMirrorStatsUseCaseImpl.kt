package domain.usecase.impl.source.usecase.device

import domain.core.source.model.device.MirrorStatsModel
import domain.core.source.model.device.PlatformModel
import domain.repository.api.source.repository.DeviceRepository
import domain.usecase.api.source.usecase.device.ObserveMirrorStatsUseCase
import kotlinx.coroutines.flow.Flow

/**
 * Default implementation of [ObserveMirrorStatsUseCase].
 *
 * @property deviceRepository Repository used for observing transport statistics.
 */
internal class ObserveMirrorStatsUseCaseImpl(
    private val deviceRepository: DeviceRepository,
) : ObserveMirrorStatsUseCase {

    /**
     * @param platform Which device family the session streams from.
     * @return A [Flow] of [MirrorStatsModel].
     * @see ObserveMirrorStatsUseCase.invoke
     */
    override fun invoke(platform: PlatformModel): Flow<MirrorStatsModel> =
        deviceRepository.observeMirrorStats(platform)
}

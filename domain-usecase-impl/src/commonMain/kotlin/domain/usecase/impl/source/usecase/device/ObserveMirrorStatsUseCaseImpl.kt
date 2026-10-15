package domain.usecase.impl.source.usecase.device

import domain.core.source.model.device.MirrorStatsModel
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
     * @return A [Flow] of [MirrorStatsModel].
     * @see ObserveMirrorStatsUseCase.invoke
     */
    override fun invoke(): Flow<MirrorStatsModel> = deviceRepository.observeMirrorStats()
}

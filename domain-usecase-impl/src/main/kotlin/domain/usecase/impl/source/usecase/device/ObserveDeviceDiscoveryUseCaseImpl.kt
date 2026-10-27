package domain.usecase.impl.source.usecase.device

import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.PlatformModel
import domain.repository.api.source.repository.DeviceRepository
import domain.usecase.api.source.usecase.device.ObserveDeviceDiscoveryUseCase
import kotlinx.coroutines.flow.Flow

/**
 * Default implementation of [ObserveDeviceDiscoveryUseCase].
 *
 * Passes the repository flow through untouched: every discovery outcome is already a value,
 * so there is nothing to wrap and nothing to map.
 *
 * @property deviceRepository Repository used for observing discovery.
 */
internal class ObserveDeviceDiscoveryUseCaseImpl(
    private val deviceRepository: DeviceRepository,
) : ObserveDeviceDiscoveryUseCase {

    /**
     * @param platform Which device family to poll.
     * @return A [Flow] of [DiscoveryModel].
     * @see ObserveDeviceDiscoveryUseCase.invoke
     */
    override fun invoke(platform: PlatformModel): Flow<DiscoveryModel> =
        deviceRepository.observeDiscovery(platform)
}

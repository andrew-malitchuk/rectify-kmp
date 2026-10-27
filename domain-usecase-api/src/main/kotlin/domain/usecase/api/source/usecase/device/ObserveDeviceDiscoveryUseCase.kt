package domain.usecase.api.source.usecase.device

import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.PlatformModel
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing device discovery.
 */
public interface ObserveDeviceDiscoveryUseCase {
    /**
     * @param platform Which device family to poll.
     * @return A [Flow] emitting the current [DiscoveryModel] on every poll. Bare, not
     *   `Flow<Result<T>>`: every discovery outcome, a missing tool included, is a value.
     */
    public operator fun invoke(platform: PlatformModel): Flow<DiscoveryModel>
}

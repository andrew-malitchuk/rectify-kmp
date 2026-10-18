package domain.usecase.impl.source.usecase.device

import domain.core.source.monad.Failure
import domain.repository.api.source.repository.DeviceRepository
import domain.usecase.api.source.monad.Optional
import domain.usecase.api.source.usecase.device.RefreshDiscoveryUseCase
import domain.usecase.impl.core.resultLauncher

/**
 * Default implementation of [RefreshDiscoveryUseCase].
 *
 * @property deviceRepository Repository whose poll loop is being nudged.
 */
internal class RefreshDiscoveryUseCaseImpl(
    private val deviceRepository: DeviceRepository,
) : RefreshDiscoveryUseCase {

    /**
     * @return An [Optional] indicating success or failure.
     * @see RefreshDiscoveryUseCase.invoke
     */
    override suspend fun invoke(): Optional = resultLauncher(
        errorMapper = Failure.Technical::Process,
    ) {
        deviceRepository.refreshDiscovery()
    }
}

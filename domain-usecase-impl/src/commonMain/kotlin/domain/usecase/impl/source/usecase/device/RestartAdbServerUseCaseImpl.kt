package domain.usecase.impl.source.usecase.device

import domain.core.source.monad.Failure
import domain.repository.api.source.repository.DeviceRepository
import domain.usecase.api.source.monad.Optional
import domain.usecase.api.source.usecase.device.RestartAdbServerUseCase
import domain.usecase.impl.core.resultLauncher

/**
 * Default implementation of [RestartAdbServerUseCase].
 *
 * @property deviceRepository Repository used for restarting the daemon.
 */
internal class RestartAdbServerUseCaseImpl(
    private val deviceRepository: DeviceRepository,
) : RestartAdbServerUseCase {

    /**
     * @return An [Optional] indicating success or failure.
     * @see RestartAdbServerUseCase.invoke
     */
    override suspend fun invoke(): Optional = resultLauncher(
        errorMapper = Failure.Technical::Process,
    ) {
        deviceRepository.restartAdbServer()
    }
}

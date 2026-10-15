package domain.usecase.impl.source.usecase.device

import domain.core.source.monad.Failure
import domain.repository.api.source.repository.DeviceRepository
import domain.usecase.api.source.monad.Optional
import domain.usecase.api.source.usecase.device.SetAdbPathUseCase
import domain.usecase.impl.core.resultLauncher

/**
 * Default implementation of [SetAdbPathUseCase].
 *
 * The failure kind is [Failure.Technical.Preference] and not `Process`: this writes a
 * preference, it does not run `adb`.
 *
 * @property deviceRepository Repository used for persisting the chosen path.
 */
internal class SetAdbPathUseCaseImpl(
    private val deviceRepository: DeviceRepository,
) : SetAdbPathUseCase {

    /**
     * @param path Absolute path to an `adb` executable, or an empty string to unset it.
     * @return An [Optional] indicating success or failure.
     * @see SetAdbPathUseCase.invoke
     */
    override suspend fun invoke(path: String): Optional = resultLauncher(
        errorMapper = Failure.Technical::Preference,
    ) {
        deviceRepository.setAdbPath(path)
    }
}

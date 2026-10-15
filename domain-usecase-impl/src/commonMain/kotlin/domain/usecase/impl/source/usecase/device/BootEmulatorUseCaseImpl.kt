package domain.usecase.impl.source.usecase.device

import domain.core.source.monad.Failure
import domain.repository.api.source.repository.DeviceRepository
import domain.usecase.api.source.monad.Optional
import domain.usecase.api.source.usecase.device.BootEmulatorUseCase
import domain.usecase.impl.core.resultLauncher

/**
 * Default implementation of [BootEmulatorUseCase].
 *
 * The emulator is launched detached and is not waited on, so there is nothing here to time
 * out: an image takes tens of seconds to boot and must outlive this call. Booting an AVD that
 * is already running is a no-op success, not an error.
 *
 * @property deviceRepository Repository used for launching emulator images.
 */
internal class BootEmulatorUseCaseImpl(
    private val deviceRepository: DeviceRepository,
) : BootEmulatorUseCase {

    /**
     * @param avdName Name of the AVD to boot.
     * @return An [Optional] indicating success or failure.
     * @see BootEmulatorUseCase.invoke
     */
    override suspend fun invoke(avdName: String): Optional = resultLauncher(
        errorMapper = Failure.Technical::Process,
    ) {
        deviceRepository.bootEmulator(avdName)
    }
}

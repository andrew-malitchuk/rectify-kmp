package domain.usecase.api.source.usecase.device

import domain.usecase.api.source.monad.Optional

/**
 * Use case for booting an emulator image.
 */
public interface BootEmulatorUseCase {
    /**
     * @param avdName Name of the AVD to boot.
     * @return An [Optional] that completes once the emulator has been launched; the image
     *   finishes booting in the background.
     */
    public suspend operator fun invoke(avdName: String): Optional
}

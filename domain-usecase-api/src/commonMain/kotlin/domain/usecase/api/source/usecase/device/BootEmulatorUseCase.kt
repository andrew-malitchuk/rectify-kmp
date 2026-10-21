package domain.usecase.api.source.usecase.device

import domain.core.source.model.device.PlatformModel
import domain.usecase.api.source.monad.Optional

/**
 * Use case for booting an emulator image.
 */
public interface BootEmulatorUseCase {
    /**
     * @param platform Which device family [avdName] names an image of.
     * @param avdName Name of the AVD to boot.
     * @return An [Optional] that completes once the emulator has been launched; the image
     *   finishes booting in the background.
     */
    public suspend operator fun invoke(platform: PlatformModel, avdName: String): Optional
}

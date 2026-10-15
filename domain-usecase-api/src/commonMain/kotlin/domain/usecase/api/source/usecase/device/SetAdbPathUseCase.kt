package domain.usecase.api.source.usecase.device

import domain.usecase.api.source.monad.Optional

/**
 * Use case for recording the path to the `adb` binary the user pointed Rectify at.
 */
public interface SetAdbPathUseCase {
    /**
     * @param path Absolute path to an `adb` executable, or an empty string to go back to
     *   probing the usual locations. The path is NOT validated here - it is validated at
     *   probe time, against the filesystem, by the same predicate every other candidate
     *   goes through.
     * @return An [Optional] that completes once the path is persisted.
     */
    public suspend operator fun invoke(path: String): Optional
}

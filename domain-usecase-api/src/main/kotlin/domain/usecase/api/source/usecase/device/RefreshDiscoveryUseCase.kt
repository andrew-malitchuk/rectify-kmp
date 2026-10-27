package domain.usecase.api.source.usecase.device

import domain.usecase.api.source.monad.Optional

/**
 * Use case for cutting the discovery poll interval short.
 */
public interface RefreshDiscoveryUseCase {
    /**
     * @return An [Optional] that completes once the nudge is delivered. Best-effort: it asks a
     *   running poll loop to skip the rest of its wait, it does not guarantee a poll.
     */
    public suspend operator fun invoke(): Optional
}

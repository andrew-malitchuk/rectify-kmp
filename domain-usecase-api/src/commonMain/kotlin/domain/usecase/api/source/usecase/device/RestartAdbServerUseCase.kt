package domain.usecase.api.source.usecase.device

import domain.usecase.api.source.monad.Optional

/**
 * Use case for restarting the `adb` daemon.
 */
public interface RestartAdbServerUseCase {
    /**
     * @return An [Optional] that completes once the daemon has been killed and started
     *   again. A real restart, not a re-poll: `start-server` on its own cannot clear a
     *   daemon that is alive but not serving, so the kill is the half that does the work.
     */
    public suspend operator fun invoke(): Optional
}

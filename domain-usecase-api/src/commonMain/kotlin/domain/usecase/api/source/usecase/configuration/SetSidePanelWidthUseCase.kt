package domain.usecase.api.source.usecase.configuration

import domain.usecase.api.source.monad.Optional

/**
 * Use case for persisting the shell side panel width.
 */
public interface SetSidePanelWidthUseCase {
    /**
     * @param value The new side panel width in dp.
     * @return An [Optional] result.
     */
    public suspend operator fun invoke(value: Int): Optional
}

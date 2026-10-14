package domain.usecase.impl.source.usecase.configuration

import domain.core.source.monad.Failure
import domain.repository.api.source.repository.ConfigureRepository
import domain.usecase.api.source.monad.Optional
import domain.usecase.api.source.usecase.configuration.SetSidePanelWidthUseCase
import domain.usecase.impl.core.resultLauncher

/**
 * Default implementation of [SetSidePanelWidthUseCase].
 *
 * Persists the provided width via [ConfigureRepository].
 *
 * @property configureRepository Repository used for writing the side panel width.
 */
internal class SetSidePanelWidthUseCaseImpl(
    private val configureRepository: ConfigureRepository,
) : SetSidePanelWidthUseCase {

    /**
     * @param value The new side panel width in dp.
     * @return An [Optional] indicating success or failure.
     * @see SetSidePanelWidthUseCase.invoke
     */
    override suspend fun invoke(value: Int): Optional = resultLauncher(
        errorMapper = Failure.Technical::Preference,
    ) {
        configureRepository.setSidePanelWidth(value)
    }
}

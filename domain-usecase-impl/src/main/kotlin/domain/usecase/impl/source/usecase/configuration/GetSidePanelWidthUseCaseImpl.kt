package domain.usecase.impl.source.usecase.configuration

import domain.core.source.monad.Failure
import domain.repository.api.source.repository.ConfigureRepository
import domain.usecase.api.source.usecase.configuration.GetSidePanelWidthUseCase
import domain.usecase.impl.core.resultLauncher

/**
 * Default implementation of [GetSidePanelWidthUseCase].
 *
 * Reads the persisted side panel width from [ConfigureRepository]; the repository
 * already substitutes the default when nothing has been stored yet.
 *
 * @property configureRepository Repository used for reading the side panel width.
 */
internal class GetSidePanelWidthUseCaseImpl(
    private val configureRepository: ConfigureRepository,
) : GetSidePanelWidthUseCase {

    /**
     * @return A [Result] containing the stored width in dp.
     * @see GetSidePanelWidthUseCase.invoke
     */
    override suspend fun invoke(): Result<Int> = resultLauncher(
        errorMapper = Failure.Technical::Preference,
    ) {
        configureRepository.getSidePanelWidth()
    }
}

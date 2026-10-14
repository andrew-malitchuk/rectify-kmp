package domain.usecase.impl.source.usecase.configuration

import domain.core.source.monad.Failure
import domain.core.source.model.UnitSystemModel
import domain.repository.api.source.repository.ConfigureRepository
import domain.usecase.api.source.monad.Optional
import domain.usecase.api.source.usecase.configuration.SetUnitSystemUseCase
import domain.usecase.impl.core.resultLauncher

internal class SetUnitSystemUseCaseImpl(
    private val configureRepository: ConfigureRepository,
) : SetUnitSystemUseCase {

    override suspend fun invoke(unitSystem: UnitSystemModel): Optional = resultLauncher(
        errorMapper = Failure.Technical::Preference,
    ) {
        configureRepository.setUnitSystem(unitSystem)
    }
}

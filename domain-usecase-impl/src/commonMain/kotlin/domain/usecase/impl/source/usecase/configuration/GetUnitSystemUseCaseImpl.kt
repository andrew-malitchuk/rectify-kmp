package domain.usecase.impl.source.usecase.configuration

import domain.core.source.monad.Failure
import domain.core.source.model.UnitSystemModel
import domain.repository.api.source.repository.ConfigureRepository
import domain.usecase.api.source.usecase.configuration.GetUnitSystemUseCase
import domain.usecase.impl.core.resultLauncher

internal class GetUnitSystemUseCaseImpl(
    private val configureRepository: ConfigureRepository,
) : GetUnitSystemUseCase {

    override suspend fun invoke(): Result<UnitSystemModel> = resultLauncher(
        errorMapper = Failure.Technical::Preference,
    ) {
        configureRepository.getUnitSystem() ?: UnitSystemModel.Metric
    }
}

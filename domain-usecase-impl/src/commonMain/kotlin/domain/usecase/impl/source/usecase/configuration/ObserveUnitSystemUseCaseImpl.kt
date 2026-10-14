package domain.usecase.impl.source.usecase.configuration

import domain.core.source.model.UnitSystemModel
import domain.repository.api.source.repository.ConfigureRepository
import domain.usecase.api.source.usecase.configuration.ObserveUnitSystemUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ObserveUnitSystemUseCaseImpl(
    private val configureRepository: ConfigureRepository,
) : ObserveUnitSystemUseCase {

    override fun invoke(): Flow<Result<UnitSystemModel>> =
        configureRepository.observeUnitSystem().map { Result.success(it ?: UnitSystemModel.Metric) }
}

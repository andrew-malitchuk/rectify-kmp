package domain.usecase.api.source.usecase.configuration

import domain.core.source.model.UnitSystemModel

/**
 * Use case for retrieving the current measurement unit system.
 */
public interface GetUnitSystemUseCase {
    /**
     * @return A [Result] containing the [UnitSystemModel].
     */
    public suspend operator fun invoke(): Result<UnitSystemModel>
}

package domain.usecase.api.source.usecase.configuration

import domain.core.source.model.UnitSystemModel
import domain.usecase.api.source.monad.Optional

/**
 * Use case for persisting the selected measurement unit system.
 */
public interface SetUnitSystemUseCase {
    /**
     * @param unitSystem The [UnitSystemModel] to persist.
     * @return An [Optional] indicating success or failure.
     */
    public suspend operator fun invoke(unitSystem: UnitSystemModel): Optional
}

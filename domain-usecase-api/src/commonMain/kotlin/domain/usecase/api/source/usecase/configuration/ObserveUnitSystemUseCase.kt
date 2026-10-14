package domain.usecase.api.source.usecase.configuration

import domain.core.source.model.UnitSystemModel
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing changes to the measurement unit system preference.
 */
public interface ObserveUnitSystemUseCase {
    /**
     * @return A hot [Flow] emitting the current [UnitSystemModel] on every change.
     */
    public operator fun invoke(): Flow<Result<UnitSystemModel>>
}

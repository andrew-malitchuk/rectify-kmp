package domain.usecase.api.source.usecase.history

import domain.core.source.model.history.HistoryEntryModel
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing every recorded batch, newest first.
 */
public interface ObserveHistoryUseCase {
    /**
     * @return A bare [Flow] of the recorded batches. History has no error surface of its
     *   own — an unservable read emits an empty list.
     */
    public operator fun invoke(): Flow<List<HistoryEntryModel>>
}

package domain.usecase.impl.source.usecase.history

import domain.core.source.model.history.HistoryEntryModel
import domain.repository.api.source.repository.HistoryRepository
import domain.usecase.api.source.usecase.history.ObserveHistoryUseCase
import kotlinx.coroutines.flow.Flow

/**
 * Default implementation of [ObserveHistoryUseCase].
 *
 * @property historyRepository Repository used for observing recorded batches.
 */
internal class ObserveHistoryUseCaseImpl(
    private val historyRepository: HistoryRepository,
) : ObserveHistoryUseCase {

    /**
     * @return A bare [Flow] of the recorded batches, newest first.
     * @see ObserveHistoryUseCase.invoke
     */
    override fun invoke(): Flow<List<HistoryEntryModel>> = historyRepository.observe()
}

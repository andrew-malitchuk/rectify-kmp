package domain.usecase.impl.source.usecase.history

import domain.core.source.model.history.SentBatchModel
import domain.core.source.monad.Failure
import domain.repository.api.source.repository.HistoryRepository
import domain.usecase.api.source.monad.Optional
import domain.usecase.api.source.usecase.history.RecordSentBatchUseCase
import domain.usecase.impl.core.resultLauncher

/**
 * Default implementation of [RecordSentBatchUseCase].
 *
 * [HistoryRepository.record] throws on failure by contract; this is the seam that turns that
 * throw into a [Failure.Technical.Database] so the caller can ignore it without losing a send.
 *
 * @property historyRepository Repository used for persisting sent batches.
 */
internal class RecordSentBatchUseCaseImpl(
    private val historyRepository: HistoryRepository,
) : RecordSentBatchUseCase {

    /**
     * @param batch The batch that was just published.
     * @return An [Optional] indicating success or failure.
     * @see RecordSentBatchUseCase.invoke
     */
    override suspend fun invoke(batch: SentBatchModel): Optional = resultLauncher(
        errorMapper = Failure.Technical::Database,
    ) {
        historyRepository.record(batch)
    }
}

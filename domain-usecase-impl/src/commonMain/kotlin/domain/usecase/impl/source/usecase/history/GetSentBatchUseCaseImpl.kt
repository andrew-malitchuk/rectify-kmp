package domain.usecase.impl.source.usecase.history

import domain.core.source.model.history.SentBatchModel
import domain.core.source.monad.Failure
import domain.repository.api.source.repository.HistoryRepository
import domain.usecase.api.source.usecase.history.GetSentBatchUseCase
import domain.usecase.impl.core.resultLauncher

/**
 * Default implementation of [GetSentBatchUseCase].
 *
 * The only decision in here is the one the repository deliberately does not make: it answers
 * `null` for both "no such batch" and "the frame is gone from disk", and this is where that
 * becomes [Failure.Logic.NotFound]. Throwing it inside the [resultLauncher] block — rather
 * than building a `Result.failure` by hand — keeps the single exit path the rule asks for:
 * `resultLauncher` re-throws a [Failure] unchanged and maps anything else through
 * [Failure.Technical.Database].
 *
 * @property historyRepository Repository used for rehydrating recorded batches.
 */
internal class GetSentBatchUseCaseImpl(
    private val historyRepository: HistoryRepository,
) : GetSentBatchUseCase {

    /**
     * @param batchId Batch to rehydrate.
     * @return The batch, or a [Failure].
     * @see GetSentBatchUseCase.invoke
     */
    override suspend fun invoke(batchId: String): Result<SentBatchModel> = resultLauncher(
        errorMapper = Failure.Technical::Database,
    ) {
        historyRepository.get(batchId) ?: throw Failure.Logic.NotFound
    }
}

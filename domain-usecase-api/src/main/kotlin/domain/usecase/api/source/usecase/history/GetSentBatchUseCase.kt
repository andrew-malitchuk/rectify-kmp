package domain.usecase.api.source.usecase.history

import domain.core.source.model.history.SentBatchModel

/**
 * Use case for rehydrating one recorded batch — the row, its annotations and the frame bytes
 * that were put on disk — so a verify run has a BEFORE side to diff against.
 */
public interface GetSentBatchUseCase {
    /**
     * @param batchId [domain.core.source.model.history.HistoryEntryModel.id] of the batch.
     * @return The batch, or `Failure.Logic.NotFound` when there is nothing to verify against.
     *   The repository collapses "no such row" and "the frame is no longer readable" into a
     *   single `null`, and both are equally unverifiable, so the absence is an error value
     *   here rather than a `Result.success(null)` the caller would have to re-branch on. A
     *   read that fails at the database itself arrives as `Failure.Technical.Database`.
     */
    public suspend operator fun invoke(batchId: String): Result<SentBatchModel>
}

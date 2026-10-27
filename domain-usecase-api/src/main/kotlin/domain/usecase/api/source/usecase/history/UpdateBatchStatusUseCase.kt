package domain.usecase.api.source.usecase.history

import domain.core.source.model.history.BatchStatusModel
import domain.usecase.api.source.monad.Optional

/**
 * Use case for moving a recorded batch to a new status.
 */
public interface UpdateBatchStatusUseCase {
    /**
     * @param batchId Batch to move. An unknown id is a no-op, not an error.
     * @param status The status to write. The verify-and-rediff loop writes only
     *   [BatchStatusModel.Verified] and [BatchStatusModel.FollowUpSent]; nothing in the app
     *   sets [BatchStatusModel.InProgress] or [BatchStatusModel.Fixed].
     * @return An [Optional] result; a storage failure arrives as `Failure.Technical.Database`
     *   instead of escaping as an exception, so a bookkeeping write can never fail a run.
     */
    public suspend operator fun invoke(batchId: String, status: BatchStatusModel): Optional
}

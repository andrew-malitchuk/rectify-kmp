package domain.usecase.impl.source.usecase.history

import domain.core.source.model.history.BatchStatusModel
import domain.core.source.monad.Failure
import domain.repository.api.source.repository.HistoryRepository
import domain.usecase.api.source.monad.Optional
import domain.usecase.api.source.usecase.history.UpdateBatchStatusUseCase
import domain.usecase.impl.core.resultLauncher

/**
 * Default implementation of [UpdateBatchStatusUseCase].
 *
 * @property historyRepository Repository used for moving a batch to a new status.
 */
internal class UpdateBatchStatusUseCaseImpl(
    private val historyRepository: HistoryRepository,
) : UpdateBatchStatusUseCase {

    /**
     * @param batchId Batch to move.
     * @param status The status to write.
     * @return An [Optional] indicating success or failure.
     * @see UpdateBatchStatusUseCase.invoke
     */
    override suspend fun invoke(batchId: String, status: BatchStatusModel): Optional = resultLauncher(
        errorMapper = Failure.Technical::Database,
    ) {
        historyRepository.updateStatus(batchId, status)
    }
}

package domain.repository.api.source.repository

import domain.core.source.model.history.BatchStatusModel
import domain.core.source.model.history.HistoryEntryModel
import domain.core.source.model.history.SentBatchModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository for the batches of annotations that were sent to the coding agent.
 *
 * Domain models only — no entity, relation or any other data-layer type crosses this boundary.
 */
public interface HistoryRepository {

    /**
     * Observes every recorded batch, newest first.
     *
     * A bare [Flow], never `Flow<Result<T>>`: a read that cannot be served emits an empty list
     * rather than an error value, because History has no error surface of its own.
     */
    public fun observe(): Flow<List<HistoryEntryModel>>

    /**
     * Makes a just-sent [batch] durable — bulk bytes to disk, the row and its annotations to
     * the database.
     *
     * Throws on failure; the caller's `resultLauncher` turns that into
     * `Failure.Technical.Database`. A partial write is never left behind: the row and its
     * annotations land in one transaction.
     */
    public suspend fun record(batch: SentBatchModel)

    /**
     * Moves the batch identified by [batchId] to [status].
     *
     * Unknown [batchId] is a no-op, not an error. Nothing in F-8 calls this — it is the hook
     * the verify-and-rediff loop (F-9) advances a batch with.
     */
    public suspend fun updateStatus(batchId: String, status: BatchStatusModel)
}

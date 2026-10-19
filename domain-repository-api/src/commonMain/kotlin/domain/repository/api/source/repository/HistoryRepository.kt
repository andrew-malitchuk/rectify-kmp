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
     * Rehydrates one recorded batch — the row, its annotations and the bytes that were put on
     * disk — or `null` when there is nothing worth verifying against.
     *
     * `null` means **either** no such batch **or** a batch whose full frame is no longer
     * readable (the user deleted `~/.rectify/history/<id>/`): a stored path is a hint, never a
     * guarantee, and without the BEFORE frame there is no diff to run, so both cases collapse
     * into the single "not on disk" error the caller raises. A batch whose *hierarchy* is gone
     * still returns, with `hierarchyXml` and `hierarchy` both `null` — the verify run degrades
     * that to per-annotation "no match" rather than aborting.
     *
     * The dump comes back BOTH raw and parsed: `hierarchy` is the implementation's own parse of
     * `hierarchyXml`, because the parser is a data-layer type presentation cannot import. A
     * dump that is missing, unreadable or unparseable leaves it `null` — the parse never
     * throws and never turns a readable batch into a `null` one.
     *
     * `cropBytes` is always `null` on every annotation: nothing on the read path consumes a
     * crop, and the full-res frame plus the stored rects strictly dominates it.
     *
     * **Known data gap — density.** The stored `hierarchy.xml` is `uiautomator dump` stdout
     * only; `wm density` was never persisted, so the parsed `hierarchy` carries
     * `densityDpi = 0` and the BEFORE side cannot label itself in dp on its own. The verify run covers this by applying
     * the freshly dumped AFTER density to both sides. The upgrade path, deliberately **not**
     * built (it cannot retroactively supply density for rows already on disk, so the fallback
     * would be needed anyway): a `densityDpi` column on `history_entry` with a `@Database`
     * version bump and a hand-written migration.
     *
     * Reads that fail at the database itself throw, exactly like [record] — the caller's
     * `resultLauncher` maps that to a `Failure`. Only the *files* degrade silently.
     */
    public suspend fun get(batchId: String): SentBatchModel?

    /**
     * Moves the batch identified by [batchId] to [status].
     *
     * Unknown [batchId] is a no-op, not an error. The verify-and-rediff loop (F-9) is its
     * caller: a run whose annotations all come back fixed advances the batch to
     * [BatchStatusModel.Verified].
     */
    public suspend fun updateStatus(batchId: String, status: BatchStatusModel)
}

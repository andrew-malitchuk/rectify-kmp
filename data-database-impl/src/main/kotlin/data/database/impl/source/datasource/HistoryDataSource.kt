package data.database.impl.source.datasource

import data.database.api.source.datasource.base.AppendableDataSource
import data.database.api.source.datasource.base.ObservableDataSource
import data.database.impl.source.entity.HistoryEntryWithAnnotations

/**
 * The sent-batch table, as the repository layer sees it.
 *
 * Mixes only the two bases it actually serves. `UpsertableDataSource` is deliberately left out:
 * a batch is written once and never re-written, and a status change is a targeted column
 * UPDATE rather than an insert-or-replace of the whole row — mixing in an `upsert` with no
 * caller would be scaffolding.
 *
 * Declared here rather than in `data-database-api` because that module holds only the generic
 * composable bases, never an entity-specific interface (`.claude/rules/database.md`).
 */
public interface HistoryDataSource :
    ObservableDataSource<HistoryEntryWithAnnotations>,
    AppendableDataSource<HistoryEntryWithAnnotations> {

    /**
     * One batch with its annotations, or `null` when [id] names no row.
     *
     * Bespoke rather than `ReadableDataSource.getById`: that base is `Int`-keyed and a batch id
     * is the `mintSessionId` string, so the generic read would not type-check, let alone work.
     */
    public suspend fun getBatch(id: String): HistoryEntryWithAnnotations?

    /**
     * Moves the batch [id] to [status], the stored name of a `BatchStatusModel`.
     *
     * A string and not the enum: the domain type belongs on the other side of the repository.
     * An unknown [id] is a no-op.
     */
    public suspend fun updateStatus(id: String, status: String)
}

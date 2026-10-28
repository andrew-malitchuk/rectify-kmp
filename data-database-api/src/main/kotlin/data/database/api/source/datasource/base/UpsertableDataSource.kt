package data.database.api.source.datasource.base

/** Marks a data source that supports insert-or-update semantics. */
public interface UpsertableDataSource<T> {

    /**
     * Inserts [resource] if it has no existing primary key, or updates it otherwise.
     *
     * @return the row id of the inserted/updated record.
     */
    public suspend fun upsert(resource: T): Int
}

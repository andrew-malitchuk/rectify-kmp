package data.database.api.source.datasource.base

/** Marks a data source that only supports appending new records (no update). */
public interface AppendableDataSource<T> {

    /**
     * Inserts a new [resource] record.
     *
     * @return the auto-generated row id of the inserted record.
     */
    public suspend fun insert(resource: T): Long
}

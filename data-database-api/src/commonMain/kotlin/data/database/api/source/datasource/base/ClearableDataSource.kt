package data.database.api.source.datasource.base

/** Marks a data source that supports bulk clearing of all records. */
public interface ClearableDataSource {

    /** Removes all records from the backing store. */
    public suspend fun clearAll()
}

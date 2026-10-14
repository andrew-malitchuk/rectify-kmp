package data.database.api.source.datasource.base

/** Marks a data source that supports targeted and bulk deletion. */
public interface DeletableDataSource {

    /** Deletes a single record by its [id]. */
    public suspend fun deleteById(id: Int)

    /** Deletes all records from the backing store. */
    public suspend fun deleteAll()
}

package data.database.api.source.datasource.base

/** Marks a data source that supports one-shot read operations. */
public interface ReadableDataSource<T> {

    /** Returns all records in a single shot. */
    public suspend fun getAll(): List<T>

    /** Returns a single record by its [id], or null if not found. */
    public suspend fun getById(id: Int): T?
}

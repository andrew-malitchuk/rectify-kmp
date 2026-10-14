package data.database.api.source.datasource.base

import kotlinx.coroutines.flow.Flow

/** Marks a data source that exposes all records as a hot [Flow]. */
public interface ObservableDataSource<T> {

    /** Returns all records as a hot [Flow] that re-emits on every database change. */
    public fun observeAll(): Flow<List<T>>
}

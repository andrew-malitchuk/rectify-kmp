package data.database.impl.core.datasource

import data.database.impl.core.dao.HistoryEntryDao
import data.database.impl.source.datasource.HistoryDataSource
import data.database.impl.source.entity.HistoryEntryWithAnnotations
import kotlinx.coroutines.flow.Flow

/** DAO-backed [HistoryDataSource] — no logic of its own beyond unpacking the relation. */
internal class HistoryDataSourceImpl(
    private val dao: HistoryEntryDao,
) : HistoryDataSource {

    override fun observeAll(): Flow<List<HistoryEntryWithAnnotations>> = dao.observeAll()

    override suspend fun getBatch(id: String): HistoryEntryWithAnnotations? = dao.findById(id)

    override suspend fun insert(resource: HistoryEntryWithAnnotations): Long =
        dao.insert(resource.entry, resource.annotations)

    override suspend fun updateStatus(id: String, status: String): Unit = dao.updateStatus(id, status)
}

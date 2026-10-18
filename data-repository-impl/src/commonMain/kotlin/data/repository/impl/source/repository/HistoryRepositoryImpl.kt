package data.repository.impl.source.repository

import co.touchlab.kermit.Logger
import data.database.impl.source.datasource.HistoryDataSource
import data.database.impl.source.entity.HistoryEntryWithAnnotations
import data.repository.impl.core.mapper.HistoryEntryMapper
import data.repository.impl.core.mapper.HistoryEntryMapper.toEntities
import data.repository.impl.source.store.BatchFileStore
import domain.core.source.model.history.BatchStatusModel
import domain.core.source.model.history.HistoryEntryModel
import domain.core.source.model.history.SentBatchModel
import domain.repository.api.source.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val log = Logger.withTag("HistoryRepository")

/**
 * Sent-batch history on top of [BatchFileStore] (bulk bytes) and [HistoryDataSource] (rows).
 *
 * @property store Where frame/crop/hierarchy bytes go. Returns `null` instead of throwing, so
 *   a failed disk write costs the row its paths and nothing else.
 * @property dataSource The `history_entry` / `history_annotation` pair.
 */
internal class HistoryRepositoryImpl(
    private val store: BatchFileStore,
    private val dataSource: HistoryDataSource,
) : HistoryRepository {

    override fun observe(): Flow<List<HistoryEntryModel>> =
        dataSource.observeAll()
            .map { rows -> rows.map(HistoryEntryMapper.toModel::map) }
            // History has no error surface of its own — an unreadable table degrades to the
            // empty state rather than taking the mirror surface down with it.
            .catch { cause ->
                log.w(cause) { "history could not be read; showing an empty list" }
                emit(emptyList())
            }

    override suspend fun record(batch: SentBatchModel) {
        val files = batch.bulkFiles()
        val dirPath = if (files.isEmpty()) null else store.write(batch.id, files)
        val (entry, annotations) = batch.toEntities(dirPath)
        dataSource.insert(HistoryEntryWithAnnotations(entry, annotations))
    }

    override suspend fun updateStatus(batchId: String, status: BatchStatusModel): Unit =
        dataSource.updateStatus(batchId, status.name)

    /** Everything too big for a column, named exactly as [HistoryEntryMapper] expects it. */
    private fun SentBatchModel.bulkFiles(): List<Pair<String, ByteArray>> = buildList {
        frameBytes?.let { add(HistoryEntryMapper.FRAME_FILE to it) }
        hierarchyXml?.let { add(HistoryEntryMapper.HIERARCHY_FILE to it.encodeToByteArray()) }
        annotations.forEachIndexed { index, annotation ->
            annotation.cropBytes?.let { add(HistoryEntryMapper.cropFile(index) to it) }
        }
    }
}

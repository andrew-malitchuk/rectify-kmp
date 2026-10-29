package data.repository.impl.source.repository

import co.touchlab.kermit.Logger
import data.database.impl.source.datasource.HistoryDataSource
import data.database.impl.source.entity.HistoryEntryWithAnnotations
import data.repository.impl.core.mapper.HierarchyDumpStdout
import data.repository.impl.core.mapper.HierarchySnapshotMapper
import data.repository.impl.core.mapper.HistoryEntryMapper
import data.repository.impl.core.mapper.HistoryEntryMapper.toEntities
import data.repository.impl.source.store.BatchFileReader
import data.repository.impl.source.store.BatchFileStore
import domain.core.source.model.history.BatchStatusModel
import domain.core.source.model.history.HistoryEntryModel
import domain.core.source.model.history.SentAnnotationModel
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
 * @property reader Reads those bytes back. Same never-throws contract in the other direction.
 * @property dataSource The `history_entry` / `history_annotation` pair.
 */
internal class HistoryRepositoryImpl(
    private val store: BatchFileStore,
    private val reader: BatchFileReader,
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

    /**
     * See [HistoryRepository.get] for the contract, the `null` semantics and the density gap.
     *
     * The frame is read first and aborts the whole assembly, because a batch without its
     * BEFORE pixels has nothing to diff; the hierarchy is read second and is allowed to stay
     * `null`. Annotations are re-sorted on their stored index — the `@Relation` makes no
     * ordering promise, and the index is what the verdict rows are numbered by.
     *
     * The dump is also PARSED here, which is the only place it can be: the parser lives in
     * `data-device-api` and presentation may not import it. Neither half can throw — the
     * parser degrades unreadable input to no nodes, and no nodes becomes a `null` snapshot, so
     * an unparseable dump costs exactly what a missing file costs. Density is `0`, the
     * "unknown" sentinel: `wm density` was never persisted, and a verify run fills it in from
     * the fresh AFTER dump rather than guessing one here.
     */
    override suspend fun get(batchId: String): SentBatchModel? {
        val (entry, annotations) = dataSource.getBatch(batchId) ?: return null
        val frameBytes = entry.framePath?.let { reader.read(it) } ?: return null
        val hierarchyXml = entry.hierarchyPath?.let { reader.read(it) }?.decodeToString()
        return SentBatchModel(
            id = entry.id,
            sentAtMs = entry.sentAtMs,
            deviceLabel = entry.deviceLabel,
            prompt = entry.prompt,
            frameBytes = frameBytes,
            thumbPng = entry.thumbPng,
            hierarchyXml = hierarchyXml,
            hierarchy = hierarchyXml
                ?.let { HierarchySnapshotMapper.toModel.map(HierarchyDumpStdout(it, density = "")) }
                ?.takeIf { it.nodes.isNotEmpty() },
            annotations = annotations.sortedBy { it.index }.map { annotation ->
                SentAnnotationModel(
                    left = annotation.left,
                    top = annotation.top,
                    right = annotation.right,
                    bottom = annotation.bottom,
                    comment = annotation.comment,
                    // Never read: the full frame plus this rect dominates a crop, and leaving
                    // crops out keeps history's 0-based index away from the envelope's 1-based
                    // crop file names entirely.
                    cropBytes = null,
                )
            },
        )
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

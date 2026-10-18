package data.repository.impl

import data.database.impl.source.datasource.HistoryDataSource
import data.database.impl.source.entity.HistoryEntryEntity
import data.database.impl.source.entity.HistoryEntryWithAnnotations
import data.repository.impl.source.repository.HistoryRepositoryImpl
import data.repository.impl.source.store.BatchFileReader
import data.repository.impl.source.store.BatchFileStore
import domain.core.source.model.history.BatchStatusModel
import domain.core.source.model.history.SentAnnotationModel
import domain.core.source.model.history.SentBatchModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Records every call so the write path can be asserted without a database or a disk. */
private class FakeHistoryDataSource(
    private val rows: Flow<List<HistoryEntryWithAnnotations>> = flowOf(emptyList()),
) : HistoryDataSource {

    val inserted: MutableList<HistoryEntryWithAnnotations> = mutableListOf()
    val statusUpdates: MutableList<Pair<String, String>> = mutableListOf()

    override fun observeAll(): Flow<List<HistoryEntryWithAnnotations>> = rows

    override suspend fun getBatch(id: String): HistoryEntryWithAnnotations? = null

    override suspend fun insert(resource: HistoryEntryWithAnnotations): Long {
        inserted += resource
        return inserted.size.toLong()
    }

    override suspend fun updateStatus(id: String, status: String) {
        statusUpdates += id to status
    }
}

/** The write path never reads a file back; `get` has its own test. */
private val noFiles = BatchFileReader { null }

/**
 * TAC-4 / TAC-5 — the three branches [HistoryRepositoryImpl] actually owns: a store that
 * failed, a store that succeeded, and a read that blew up.
 */
class HistoryRepositoryImplTest {

    @Test
    fun recordStillInsertsTheRowWithNullPathsWhenTheStoreCouldNotWriteAnything() = runTest {
        val dataSource = FakeHistoryDataSource()
        val neverWrites = BatchFileStore { _, _ -> null }

        HistoryRepositoryImpl(neverWrites, noFiles, dataSource).record(batch())

        assertEquals(1, dataSource.inserted.size, "a failed disk write must not cost us the row")
        val (entry, annotations) = dataSource.inserted.single()
        assertNull(entry.dirPath)
        assertNull(entry.framePath)
        assertNull(entry.hierarchyPath)
        assertEquals(listOf(null, null), annotations.map { it.cropPath })
        assertNotNull(entry.thumbPng, "the inline thumbnail is what keeps the row renderable (SC-18)")
    }

    @Test
    fun recordInsertsPathsThatAreChildrenOfTheDirectoryTheStoreReturned() = runTest {
        val dataSource = FakeHistoryDataSource()
        var written: List<Pair<String, ByteArray>> = emptyList()
        val store = BatchFileStore { _, files ->
            written = files
            "/x/history/batch-1"
        }

        HistoryRepositoryImpl(store, noFiles, dataSource).record(batch())

        val (entry, annotations) = dataSource.inserted.single()
        assertEquals("/x/history/batch-1", entry.dirPath)
        assertEquals("/x/history/batch-1/frame.png", entry.framePath)
        assertEquals("/x/history/batch-1/hierarchy.xml", entry.hierarchyPath)
        assertEquals(
            listOf("/x/history/batch-1/crop-0.png", "/x/history/batch-1/crop-1.png"),
            annotations.map { it.cropPath },
        )
        assertEquals(
            listOf("frame.png", "hierarchy.xml", "crop-0.png", "crop-1.png"),
            written.map { it.first },
            "every stored path must name a file the repository actually handed to the store",
        )
        assertContentEquals("<hierarchy/>".encodeToByteArray(), written[1].second)
    }

    @Test
    fun recordSkipsTheStoreEntirelyWhenTheBatchHasNoBulkBytes() = runTest {
        val dataSource = FakeHistoryDataSource()
        var called = false
        val store = BatchFileStore { _, _ ->
            called = true
            "/x"
        }

        val thumbnailOnly = batch().copy(frameBytes = null, hierarchyXml = null, annotations = emptyList())
        HistoryRepositoryImpl(store, noFiles, dataSource).record(thumbnailOnly)

        assertTrue(!called, "no bytes means no empty directory on disk")
        assertNull(dataSource.inserted.single().entry.dirPath)
    }

    @Test
    fun recordIsBlockedByNothingTheStoreDoesBecauseTheStoreCannotFail() = runTest {
        // The contract is null-on-failure, so the only failure a store can express is already
        // covered above. What this pins down is that record never inspects the store's answer
        // beyond passing it to the mapper - no existence check, no throw.
        val dataSource = FakeHistoryDataSource()
        val nonsensePath = BatchFileStore { _, _ -> "/this/directory/does/not/exist" }

        HistoryRepositoryImpl(nonsensePath, noFiles, dataSource).record(batch())

        assertEquals("/this/directory/does/not/exist/frame.png", dataSource.inserted.single().entry.framePath)
    }

    @Test
    fun observeMapsStoredRowsNewestFirstAsTheDataSourceOrderedThem() = runTest {
        val dataSource = FakeHistoryDataSource(flowOf(listOf(row("b", 2L), row("a", 1L))))

        val repository = HistoryRepositoryImpl(BatchFileStore { _, _ -> null }, noFiles, dataSource)
        val models = repository.observe().first()

        assertEquals(listOf("b", "a"), models.map { it.id })
        assertEquals(listOf(BatchStatusModel.Sent, BatchStatusModel.Sent), models.map { it.status })
    }

    @Test
    fun observeLogsAndEmitsAnEmptyListWhenTheDatabaseReadBlowsUp() = runTest {
        val exploding = object : HistoryDataSource {
            override fun observeAll(): Flow<List<HistoryEntryWithAnnotations>> =
                flow { throw IllegalStateException("table history_entry is gone") }

            override suspend fun getBatch(id: String): HistoryEntryWithAnnotations? = null
            override suspend fun insert(resource: HistoryEntryWithAnnotations): Long = 0
            override suspend fun updateStatus(id: String, status: String) = Unit
        }

        val repository = HistoryRepositoryImpl(BatchFileStore { _, _ -> null }, noFiles, exploding)
        val models = repository.observe().first()

        assertEquals(emptyList(), models, "History has no error surface - it degrades to empty")
    }

    @Test
    fun updateStatusPassesTheEnumNameDownAsAString() = runTest {
        val dataSource = FakeHistoryDataSource()

        HistoryRepositoryImpl(BatchFileStore { _, _ -> null }, noFiles, dataSource)
            .updateStatus("batch-1", BatchStatusModel.Fixed)

        assertEquals(listOf("batch-1" to "Fixed"), dataSource.statusUpdates)
    }

    private fun row(id: String, sentAtMs: Long) = HistoryEntryWithAnnotations(
        entry = HistoryEntryEntity(
            id = id,
            sentAtMs = sentAtMs,
            status = BatchStatusModel.Sent.name,
            title = "t",
            annotationCount = 0,
            deviceLabel = "Pixel 7",
            prompt = "p",
        ),
        annotations = emptyList(),
    )

    private fun batch() = SentBatchModel(
        id = "batch-1",
        sentAtMs = 1_700_000_000_000,
        deviceLabel = "Pixel 7",
        prompt = "fix these",
        frameBytes = byteArrayOf(7),
        thumbPng = byteArrayOf(9),
        hierarchyXml = "<hierarchy/>",
        annotations = listOf(
            SentAnnotationModel(1, 2, 3, 4, "first", cropBytes = byteArrayOf(1)),
            SentAnnotationModel(5, 6, 7, 8, "second", cropBytes = byteArrayOf(2)),
        ),
    )
}

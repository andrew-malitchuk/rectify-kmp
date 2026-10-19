package data.database.impl.core.dao

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import data.database.impl.core.database.AppDatabase
import data.database.impl.core.datasource.HistoryDataSourceImpl
import data.database.impl.source.entity.HistoryAnnotationEntity
import data.database.impl.source.entity.HistoryEntryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * AC-23 / AC-45 — reading one sent batch back **with its annotations**, which is what the verify
 * loop re-measures against. The `@Relation` always carried them; only the query was missing.
 *
 * Separate from `HistoryEntryDaoTest` because that suite is the write/observe surface; this one
 * is the single-row read, including the `HistoryDataSource` method that delegates to it.
 */
internal class HistoryEntryDaoFindByIdTest {

    private val directory: File = createTempDirectory(prefix = "rectify-db-findbyid").toFile()

    private val database: AppDatabase = Room
        .databaseBuilder<AppDatabase>(name = File(directory, "rectify.db").absolutePath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

    @AfterTest
    fun tearDown() {
        database.close()
        directory.deleteRecursively()
    }

    @Test
    fun `a present batch comes back with its annotations in index order`() = runTest {
        val dao = database.historyEntryDao()
        dao.insert(
            entry(id = "batch-1"),
            listOf(annotation("batch-1", index = 0), annotation("batch-1", index = 1)),
        )
        dao.insert(entry(id = "other"), listOf(annotation("other", index = 0)))

        val found = dao.findById("batch-1")

        assertEquals("batch-1", found?.entry?.id)
        assertContentEquals(listOf(0, 1), found?.annotations?.map { it.index })
        assertContentEquals(listOf("note 0", "note 1"), found?.annotations?.map { it.comment })
    }

    @Test
    fun `an absent id reads null rather than an empty row`() = runTest {
        database.historyEntryDao().insert(entry(id = "batch-1"), annotations = emptyList())

        assertNull(database.historyEntryDao().findById("never-sent"))
    }

    @Test
    fun `a batch with no annotations reads back with an empty list`() = runTest {
        val dao = database.historyEntryDao()
        dao.insert(entry(id = "bare"), annotations = emptyList())

        val found = dao.findById("bare")

        assertEquals("bare", found?.entry?.id)
        assertTrue(found?.annotations?.isEmpty() == true)
    }

    @Test
    fun `the data source delegates to the same single-row read`() = runTest {
        val source = HistoryDataSourceImpl(database.historyEntryDao())
        database.historyEntryDao().insert(entry(id = "batch-1"), listOf(annotation("batch-1", 0)))

        assertEquals("batch-1", source.getBatch("batch-1")?.entry?.id)
        assertEquals(1, source.getBatch("batch-1")?.annotations?.size)
        assertNull(source.getBatch("missing"))
    }

    private fun entry(id: String): HistoryEntryEntity = HistoryEntryEntity(
        id = id,
        sentAtMs = 1_000L,
        status = "Sent",
        title = "Price sits 6px below the title",
        annotationCount = 2,
        deviceLabel = "Pixel 7",
        prompt = "fix the spacing",
        thumbPng = byteArrayOf(1, 2, 3),
    )

    private fun annotation(batchId: String, index: Int): HistoryAnnotationEntity =
        HistoryAnnotationEntity(
            batchId = batchId,
            index = index,
            left = index,
            top = index,
            right = index + 10,
            bottom = index + 10,
            comment = "note $index",
        )
}

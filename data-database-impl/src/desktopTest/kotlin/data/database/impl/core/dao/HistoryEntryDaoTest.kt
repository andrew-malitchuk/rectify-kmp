package data.database.impl.core.dao

import androidx.room.Room
import androidx.room.execSQL
import androidx.room.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import data.database.impl.core.database.AppDatabase
import data.database.impl.core.database.AppDatabaseConstructor
import data.database.impl.source.entity.HistoryAnnotationEntity
import data.database.impl.source.entity.HistoryEntryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * TAC-3 — the real `history_entry` / `history_annotation` behaviour, and with it the proof that
 * the Room KMP toolchain works on desktop at all: KSP output, the generated
 * [AppDatabaseConstructor] actual, [BundledSQLiteDriver] linkage and invalidation-driven `Flow`
 * queries.
 *
 * Supersedes the step-0 spike's `AppDatabaseRoundTripTest`, whose insert / observe / ordering /
 * status cases are folded in below so there is one history DAO suite, not two overlapping ones.
 *
 * Everything runs against a temp-directory file, never the user's `~/.rectify/rectify.db`.
 */
internal class HistoryEntryDaoTest {

    private val directory: File = createTempDirectory(prefix = "rectify-db-test").toFile()

    private val database: AppDatabase = open()

    private fun open(): AppDatabase = Room
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
    fun `batch and its annotations are readable back`() = runTest {
        val dao = database.historyEntryDao()
        dao.insert(entry(id = "batch-1"), listOf(annotation("batch-1", index = 0), annotation("batch-1", index = 1)))

        val stored = dao.observeAll().first()

        assertEquals(1, stored.size)
        assertEquals("batch-1", stored.single().entry.id)
        assertContentEquals(listOf(0, 1), stored.single().annotations.map { it.index })
    }

    @Test
    fun `observe emits the batch inserted after collection starts`() = runTest {
        val dao = database.historyEntryDao()
        val emission = async(Dispatchers.IO) { dao.observeAll().first { it.isNotEmpty() } }

        dao.insert(entry(id = "observed"), annotations = emptyList())

        assertEquals("observed", emission.await().single().entry.id)
    }

    @Test
    fun `observe orders newest first`() = runTest {
        val dao = database.historyEntryDao()
        dao.insert(entry(id = "older", sentAtMs = 1_000L), annotations = emptyList())
        dao.insert(entry(id = "newer", sentAtMs = 2_000L), annotations = emptyList())

        assertContentEquals(listOf("newer", "older"), dao.observeAll().first().map { it.entry.id })
    }

    @Test
    fun `status update replaces the stored value`() = runTest {
        val dao = database.historyEntryDao()
        dao.insert(entry(id = "batch-1"), annotations = emptyList())

        dao.updateStatus(id = "batch-1", status = "Fixed")

        assertEquals("Fixed", dao.observeAll().first().single().entry.status)
    }

    @Test
    fun `status update of an unknown batch changes nothing`() = runTest {
        val dao = database.historyEntryDao()
        dao.insert(entry(id = "batch-1"), annotations = emptyList())

        dao.updateStatus(id = "never-sent", status = "Fixed")

        assertEquals("Sent", dao.observeAll().first().single().entry.status)
    }

    @Test
    fun `deleting a batch cascades to its annotations`() = runTest {
        val dao = database.historyEntryDao()
        dao.insert(entry(id = "doomed"), listOf(annotation("doomed", index = 0), annotation("doomed", index = 1)))
        dao.insert(entry(id = "kept", sentAtMs = 2_000L), listOf(annotation("kept", index = 0)))

        database.useWriterConnection { it.execSQL("DELETE FROM history_entry WHERE id = 'doomed'") }

        assertContentEquals(listOf("kept"), dao.observeAll().first().map { it.entry.id })
        assertEquals(0, countOrphanAnnotations(), "annotations must not outlive their batch")
    }

    @Test
    fun `rows survive closing and re-opening the same file`() = runTest {
        database.historyEntryDao()
            .insert(entry(id = "batch-1"), listOf(annotation("batch-1", index = 0)))
        database.close()

        val reopened = open()
        try {
            val stored = reopened.historyEntryDao().observeAll().first()

            assertEquals("batch-1", stored.single().entry.id)
            assertEquals(1, stored.single().annotations.size)
            assertContentEquals(byteArrayOf(1, 2, 3), stored.single().entry.thumbPng)
        } finally {
            reopened.close()
        }
    }

    /** Annotations whose parent batch is gone — must always be zero. */
    private suspend fun countOrphanAnnotations(): Int = database.useWriterConnection { connection ->
        connection.usePrepared(
            "SELECT COUNT(*) FROM history_annotation WHERE batchId NOT IN (SELECT id FROM history_entry)",
        ) { statement ->
            assertTrue(statement.step(), "COUNT always returns a row")
            statement.getInt(0)
        }
    }

    private fun entry(id: String, sentAtMs: Long = 1_000L): HistoryEntryEntity = HistoryEntryEntity(
        id = id,
        sentAtMs = sentAtMs,
        status = "Sent",
        title = "Price sits 6px below the title",
        annotationCount = 2,
        deviceLabel = "Pixel 7",
        prompt = "fix the spacing",
        thumbPng = byteArrayOf(1, 2, 3),
    )

    private fun annotation(batchId: String, index: Int): HistoryAnnotationEntity = HistoryAnnotationEntity(
        batchId = batchId,
        index = index,
        left = index,
        top = index,
        right = index + 10,
        bottom = index + 10,
        comment = "note $index",
    )
}

package data.database.impl.core.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
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

/**
 * Proves the Room KMP toolchain actually works on desktop — KSP output, the generated
 * [AppDatabaseConstructor] actual, [BundledSQLiteDriver] linkage and invalidation-driven
 * `Flow` queries — against a temp-directory file, never the user's `~/.rectify/rectify.db`.
 */
internal class AppDatabaseRoundTripTest {

    private val directory: File = createTempDirectory(prefix = "rectify-db-test").toFile()

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

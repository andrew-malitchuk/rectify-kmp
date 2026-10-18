package data.repository.impl

import data.database.impl.source.datasource.HistoryDataSource
import data.database.impl.source.entity.HistoryAnnotationEntity
import data.database.impl.source.entity.HistoryEntryEntity
import data.database.impl.source.entity.HistoryEntryWithAnnotations
import data.repository.impl.core.mapper.HierarchyDumpStdout
import data.repository.impl.core.mapper.HierarchySnapshotMapper
import data.repository.impl.source.repository.HistoryRepositoryImpl
import data.repository.impl.source.store.BatchFileReader
import data.repository.impl.source.store.BatchFileStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * T-3 — the read path's four branches: a complete batch, no such row, no frame on disk, and a
 * hierarchy that is gone while the frame is not.
 *
 * No database and no disk: the data source is a fake and [BatchFileReader] is a lambda, which
 * is the whole reason both seams exist (AS-29).
 */
class HistoryRepositoryGetTest {

    @Test
    fun presentRowWithReadableFilesYieldsAFullyPopulatedBatch() = runTest {
        val model = assertNotNull(repository(row()).get(BATCH_ID))

        assertEquals(BATCH_ID, model.id)
        assertEquals(1700, model.sentAtMs)
        assertEquals("Pixel 8", model.deviceLabel)
        assertEquals("fix the padding", model.prompt)
        assertContentEquals(FRAME_BYTES, model.frameBytes)
        assertContentEquals(THUMB_BYTES, model.thumbPng)
        assertEquals(DUMP, model.hierarchyXml)
        // Stored index order, whatever order the @Relation handed them back in.
        assertEquals(listOf("first", "second"), model.annotations.map { it.comment })
        assertEquals(listOf(10, 30), model.annotations.map { it.left })
        // Nothing on this path consumes a crop; the full frame plus the rect dominates it.
        assertEquals(listOf(null, null), model.annotations.map { it.cropBytes })
    }

    @Test
    fun storedHierarchyReparsesWithTheDocumentedZeroDensity() = runTest {
        val model = assertNotNull(repository(row()).get(BATCH_ID))

        val snapshot = HierarchySnapshotMapper.toModel.map(
            // The second half was never persisted — that IS the gap.
            HierarchyDumpStdout(dump = assertNotNull(model.hierarchyXml), density = ""),
        )

        assertEquals(2, snapshot.nodes.size, "the stored dump must still parse")
        assertEquals(1080, snapshot.frameWidthPx)
        assertEquals(
            0,
            snapshot.densityDpi,
            "density was never stored - 0 is the sentinel the AFTER dump fills in, never a guess",
        )
    }

    @Test
    fun absentRowYieldsNull() = runTest {
        assertNull(repository(null).get(BATCH_ID))
    }

    @Test
    fun rowWithoutAFramePathYieldsNull() = runTest {
        assertNull(repository(row(framePath = null)).get(BATCH_ID))
    }

    @Test
    fun rowWhoseFrameFileTheUserDeletedYieldsNullRatherThanThrowing() = runTest {
        val gone = BatchFileReader { null }

        assertNull(repository(row(), reader = gone).get(BATCH_ID))
    }

    @Test
    fun unreadableHierarchyStillYieldsTheBatchWithoutIt() = runTest {
        val frameOnly = BatchFileReader { path -> FRAME_BYTES.takeIf { path == FRAME_PATH } }

        val model = assertNotNull(repository(row(), reader = frameOnly).get(BATCH_ID))

        assertContentEquals(FRAME_BYTES, model.frameBytes)
        assertNull(
            model.hierarchyXml,
            "a lost hierarchy degrades to per-annotation no-match, it does not lose the batch",
        )
    }

    private fun repository(
        stored: HistoryEntryWithAnnotations?,
        reader: BatchFileReader = FakeReader,
    ) = HistoryRepositoryImpl(
        store = BatchFileStore { _, _ -> null },
        reader = reader,
        dataSource = FakeHistoryReadSource(stored),
    )
}

private const val BATCH_ID = "s-42"
private const val DIR = "/x/history/$BATCH_ID"
private const val FRAME_PATH = "$DIR/frame.png"
private const val HIERARCHY_PATH = "$DIR/hierarchy.xml"
private val FRAME_BYTES = byteArrayOf(8, 9, 10)
private val THUMB_BYTES = byteArrayOf(1, 2)

private const val DUMP =
    """<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><hierarchy rotation="0">""" +
        """<node index="0" text="" resource-id="" class="android.widget.FrameLayout" """ +
        """package="dev.rectify.io" content-desc="" clickable="false" bounds="[0,0][1080,2400]">""" +
        """<node index="0" text="OK" resource-id="android:id/button1" """ +
        """class="android.widget.Button" package="dev.rectify.io" content-desc="" """ +
        """clickable="true" bounds="[100,200][300,260]" /></node></hierarchy>"""

/** Reads back exactly what the write path would have stored, by path. */
private val FakeReader = BatchFileReader { path ->
    when (path) {
        FRAME_PATH -> FRAME_BYTES
        HIERARCHY_PATH -> DUMP.encodeToByteArray()
        else -> null
    }
}

private fun row(framePath: String? = FRAME_PATH) = HistoryEntryWithAnnotations(
    entry = HistoryEntryEntity(
        id = BATCH_ID,
        sentAtMs = 1700,
        status = "Fixed",
        title = "first",
        annotationCount = 2,
        deviceLabel = "Pixel 8",
        prompt = "fix the padding",
        dirPath = DIR,
        framePath = framePath,
        hierarchyPath = HIERARCHY_PATH,
        thumbPng = THUMB_BYTES,
    ),
    // Deliberately out of order: Room's @Relation promises no ordering.
    annotations = listOf(
        annotation(index = 1, left = 30, comment = "second"),
        annotation(index = 0, left = 10, comment = "first"),
    ),
)

private fun annotation(index: Int, left: Int, comment: String) = HistoryAnnotationEntity(
    batchId = BATCH_ID,
    index = index,
    left = left,
    top = 20,
    right = left + 40,
    bottom = 60,
    comment = comment,
    cropPath = "$DIR/crop-$index.png",
)

/** Serves one stored batch to [HistoryRepositoryImpl.get]; nothing else is exercised here. */
private class FakeHistoryReadSource(
    private val stored: HistoryEntryWithAnnotations?,
) : HistoryDataSource {

    override fun observeAll(): Flow<List<HistoryEntryWithAnnotations>> = flowOf(emptyList())

    override suspend fun getBatch(id: String): HistoryEntryWithAnnotations? =
        stored?.takeIf { it.entry.id == id }

    override suspend fun insert(resource: HistoryEntryWithAnnotations): Long = 1

    override suspend fun updateStatus(id: String, status: String): Unit = Unit
}

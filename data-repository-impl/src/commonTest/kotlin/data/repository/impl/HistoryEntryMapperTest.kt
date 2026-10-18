package data.repository.impl

import data.database.impl.source.entity.HistoryEntryEntity
import data.database.impl.source.entity.HistoryEntryWithAnnotations
import data.repository.impl.core.mapper.HistoryEntryMapper
import data.repository.impl.core.mapper.HistoryEntryMapper.toEntities
import domain.core.source.model.history.BatchStatusModel
import domain.core.source.model.history.SentAnnotationModel
import domain.core.source.model.history.SentBatchModel
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * TAC-6 — the two branches of [HistoryEntryMapper] that carry real logic: the unknown-status
 * fallback on the way out of the database, and the `dirPath`-dependent path assignment on the
 * way in.
 */
class HistoryEntryMapperTest {

    private fun storedRow(
        status: String = BatchStatusModel.Sent.name,
        framePath: String? = null,
        thumbPng: ByteArray? = null,
    ) = HistoryEntryWithAnnotations(
        entry = HistoryEntryEntity(
            id = "batch-1",
            sentAtMs = 1_700_000_000_000,
            status = status,
            title = "button is misaligned",
            annotationCount = 2,
            deviceLabel = "Pixel 7",
            prompt = "fix these",
            framePath = framePath,
            thumbPng = thumbPng,
        ),
        annotations = emptyList(),
    )

    @Test
    fun toModelFallsBackToSentForAStatusTheEnumNoLongerKnows() {
        val model = HistoryEntryMapper.toModel.map(storedRow(status = "RolledBackInV3"))

        assertEquals(BatchStatusModel.Sent, model.status, "an unrecognised status must degrade, never throw")
    }

    @Test
    fun toModelFallsBackToSentForAnEmptyStatus() {
        assertEquals(BatchStatusModel.Sent, HistoryEntryMapper.toModel.map(storedRow(status = "")).status)
    }

    @Test
    fun toModelReadsBackEveryStatusTheEnumDoesKnow() {
        BatchStatusModel.entries.forEach { status ->
            assertEquals(status, HistoryEntryMapper.toModel.map(storedRow(status = status.name)).status)
        }
    }

    @Test
    fun toModelCarriesAStoredPathThroughWithoutCheckingThatTheFileStillExists() {
        // The user deleted ~/.rectify/history by hand; the row must still map to a full model.
        val deleted = "/nowhere/.rectify/history/batch-1/frame.png"

        val model = HistoryEntryMapper.toModel.map(storedRow(framePath = deleted, thumbPng = byteArrayOf(1, 2)))

        assertEquals(deleted, model.framePath, "a stored path is a hint, carried through verbatim")
        assertEquals("batch-1", model.id)
        assertEquals(1_700_000_000_000, model.sentAtMs)
        assertEquals("button is misaligned", model.title)
        assertEquals(2, model.annotationCount)
        assertContentEquals(byteArrayOf(1, 2), model.thumbPng, "the inline thumbnail is what keeps the row renderable")
    }

    @Test
    fun toEntitiesAssignsEveryPathUnderTheDirectoryTheStoreReturned() {
        val (entry, annotations) = batch().toEntities(dirPath = "/x/history/batch-1")

        assertEquals("/x/history/batch-1", entry.dirPath)
        assertEquals("/x/history/batch-1/frame.png", entry.framePath)
        assertEquals("/x/history/batch-1/hierarchy.xml", entry.hierarchyPath)
        assertEquals(
            listOf("/x/history/batch-1/crop-0.png", "/x/history/batch-1/crop-1.png"),
            annotations.map { it.cropPath },
        )
    }

    @Test
    fun toEntitiesLeavesEveryPathNullWhenTheStoreFailed() {
        val (entry, annotations) = batch().toEntities(dirPath = null)

        assertNull(entry.dirPath)
        assertNull(entry.framePath)
        assertNull(entry.hierarchyPath)
        assertEquals(listOf(null, null), annotations.map { it.cropPath })
        assertContentEquals(byteArrayOf(9), entry.thumbPng, "the thumbnail survives a failed disk write")
    }

    @Test
    fun toEntitiesLeavesAPathNullWhenThereWereNoBytesToWriteThere() {
        val noBulkBytes = batch().copy(frameBytes = null, hierarchyXml = null)

        val (entry, _) = noBulkBytes.toEntities(dirPath = "/x/history/batch-1")

        assertNull(entry.framePath, "a stored path must always refer to a file that was written")
        assertNull(entry.hierarchyPath)
    }

    @Test
    fun toEntitiesTakesTheRowTitleFromTheFirstAnnotationComment() {
        assertEquals("first", batch().toEntities(dirPath = null).first.title)
        assertEquals("", batch().copy(annotations = emptyList()).toEntities(dirPath = null).first.title)
    }

    @Test
    fun toEntitiesNumbersAnnotationsInDrawOrderAndKeepsTheirRects() {
        val (_, annotations) = batch().toEntities(dirPath = null)

        assertEquals(listOf(0, 1), annotations.map { it.index })
        assertEquals(listOf("batch-1", "batch-1"), annotations.map { it.batchId })
        assertEquals(listOf(1, 2, 3, 4), annotations[0].let { listOf(it.left, it.top, it.right, it.bottom) })
    }

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

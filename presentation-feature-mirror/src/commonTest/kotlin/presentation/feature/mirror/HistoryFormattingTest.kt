package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import domain.core.source.model.history.BatchStatusModel
import domain.core.source.model.history.HistoryEntryModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import presentation.core.ui.source.kit.atom.pill.StatusPillTone
import presentation.feature.mirror.core.history.HistoryBucket
import presentation.feature.mirror.core.history.groupHistory
import presentation.feature.mirror.core.history.historyBadgeCount
import presentation.feature.mirror.core.history.inProgressCount
import presentation.feature.mirror.core.history.metaTime
import presentation.feature.mirror.core.history.noteCountKey
import presentation.feature.mirror.core.history.shortBatchId
import presentation.feature.mirror.core.history.statusTone
import presentation.feature.mirror.core.history.thumbnailRect
import presentation.feature.mirror.core.history.unionRect
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.StagedAnnotation
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_history_meta_note_one
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_history_meta_note_other
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * TAC-1 to TAC-6 — the History tab's pure decisions.
 *
 * A fixed zone (UTC) and an injected `nowMs`, so a day boundary is a literal and the suite
 * reads the same in every timezone the CI box happens to be in.
 */
class HistoryFormattingTest {

    private val zone = TimeZone.UTC

    /** 2026-03-10 at [hour]:[minute] UTC, in epoch milliseconds. */
    private fun at(day: Int, hour: Int, minute: Int = 0): Long =
        LocalDateTime(2026, 3, day, hour, minute).toInstant(zone).toEpochMilliseconds()

    private fun entry(
        id: String,
        sentAtMs: Long,
        status: BatchStatusModel = BatchStatusModel.Sent,
    ): HistoryEntryModel = HistoryEntryModel(
        id = id,
        sentAtMs = sentAtMs,
        status = status,
        title = "Price sits 6px below the title",
        annotationCount = 1,
    )

    @Test
    fun groupHistoryBucketsByLocalCalendarDayNotByElapsedHours() {
        val now = at(day = 10, hour = 0, minute = 1)
        // One minute earlier, yet already the previous calendar day.
        val groups = groupHistory(
            entries = listOf(
                entry("today", at(day = 10, hour = 0, minute = 0)),
                entry("yesterday", at(day = 9, hour = 23, minute = 59)),
                entry("older", at(day = 8, hour = 12)),
            ),
            nowMs = now,
            zone = zone,
        )

        assertContentEquals(
            listOf(HistoryBucket.Today, HistoryBucket.Yesterday, HistoryBucket.Older),
            groups.map { it.bucket },
        )
        assertContentEquals(listOf("today", "yesterday", "older"), groups.map { it.entries.single().id })
    }

    @Test
    fun groupHistoryOrdersNewestFirstWithinAGroupAndDropsEmptyBuckets() {
        val groups = groupHistory(
            entries = listOf(
                entry("morning", at(day = 10, hour = 9)),
                entry("evening", at(day = 10, hour = 20)),
                entry("noon", at(day = 10, hour = 12)),
            ),
            nowMs = at(day = 10, hour = 23),
            zone = zone,
        )

        assertEquals(1, groups.size, "Yesterday and Older have no rows and must not be emitted")
        assertContentEquals(listOf("evening", "noon", "morning"), groups.single().entries.map { it.id })
    }

    @Test
    fun groupHistoryOfNothingIsNoGroupsAtAll() {
        assertEquals(emptyList(), groupHistory(emptyList(), nowMs = at(day = 10, hour = 9), zone = zone))
    }

    @Test
    fun metaTimeIsZeroPaddedTwentyFourHourLocalTime() {
        assertEquals("14:02", metaTime(at(day = 10, hour = 14, minute = 2), zone))
        assertEquals("09:05", metaTime(at(day = 10, hour = 9, minute = 5), zone))
        assertEquals("00:00", metaTime(at(day = 10, hour = 0, minute = 0), zone))
    }

    @Test
    fun shortBatchIdIsFourHexCharactersAndDeterministic() {
        val id = "emulator-5554-f3-1773141720000"
        val expected = (id.hashCode() and 0xFFFF).toString(16).padStart(4, '0')

        assertEquals(expected, shortBatchId(id))
        assertEquals(shortBatchId(id), shortBatchId(id))
        assertEquals(4, shortBatchId(id).length)
    }

    @Test
    fun shortBatchIdPadsAHashWithLeadingZeroesToFourCharacters() {
        // "" hashes to 0 -- the one input that would otherwise render as a bare "0".
        assertEquals("0000", shortBatchId(""))
    }

    @Test
    fun unionRectEnvelopesEveryScatteredAnnotation() {
        val union = unionRect(
            listOf(
                StagedAnnotation(index = 1, rect = DeviceRect(left = 900, top = 120, right = 1000, bottom = 200)),
                StagedAnnotation(index = 2, rect = DeviceRect(left = 60, top = 1800, right = 160, bottom = 1900)),
                StagedAnnotation(index = 3, rect = DeviceRect(left = 400, top = 900, right = 500, bottom = 1000)),
            ),
        )

        assertEquals(DeviceRect(left = 60, top = 120, right = 1000, bottom = 1900), union)
    }

    @Test
    fun unionRectOfNoAnnotationsIsNull() {
        assertNull(unionRect(emptyList()))
    }

    @Test
    fun thumbnailRectExpandsByTheDesignScaledPad() {
        // pad = 18 * 1080 / 296 = 65.675..., so left 300 -> 234.32 -> 234, top 400 -> 334.32 -> 334.
        val padded = thumbnailRect(
            union = DeviceRect(left = 300, top = 400, right = 500, bottom = 700),
            frameSize = IntSize(1080, 2400),
        )

        assertEquals(DeviceRect(left = 234, top = 334, right = 566, bottom = 766), padded)
    }

    @Test
    fun thumbnailRectClampsToTheFrameOnAllFourSides() {
        val clamped = thumbnailRect(
            union = DeviceRect(left = 0, top = 0, right = 1080, bottom = 2400),
            frameSize = IntSize(1080, 2400),
        )

        assertEquals(DeviceRect(left = 0, top = 0, right = 1080, bottom = 2400), clamped)
    }

    @Test
    fun statusToneCoversEveryBatchStatus() {
        assertEquals(StatusPillTone.Neutral, statusTone(BatchStatusModel.Sent))
        assertEquals(StatusPillTone.Busy, statusTone(BatchStatusModel.InProgress))
        assertEquals(StatusPillTone.Done, statusTone(BatchStatusModel.Fixed))
        assertEquals(StatusPillTone.Neutral, statusTone(BatchStatusModel.Verified))
        assertEquals(StatusPillTone.Alert, statusTone(BatchStatusModel.FollowUpSent))
    }

    @Test
    fun inProgressCountCountsOnlyInProgressBatches() {
        val entries = listOf(
            entry("a", at(day = 10, hour = 9), BatchStatusModel.InProgress),
            entry("b", at(day = 10, hour = 10), BatchStatusModel.Fixed),
            entry("c", at(day = 10, hour = 11), BatchStatusModel.InProgress),
        )

        assertEquals(2, inProgressCount(entries))
        assertEquals(0, inProgressCount(entries.filter { it.status != BatchStatusModel.InProgress }))
        assertEquals(0, inProgressCount(emptyList()))
    }

    @Test
    fun historyBadgeCountIsNullAtZeroAndTheCountOtherwise() {
        val entries = listOf(
            entry("a", at(day = 10, hour = 9), BatchStatusModel.InProgress),
            entry("b", at(day = 10, hour = 10), BatchStatusModel.Fixed),
            entry("c", at(day = 10, hour = 11), BatchStatusModel.InProgress),
        )

        assertEquals(2, historyBadgeCount(entries))
        assertNull(historyBadgeCount(entries.filter { it.status != BatchStatusModel.InProgress }))
        assertNull(historyBadgeCount(emptyList()))
    }

    @Test
    fun noteCountKeyIsSingularOnlyForExactlyOne() {
        assertEquals(Res.string.mirror_history_meta_note_one, noteCountKey(1))
        assertEquals(Res.string.mirror_history_meta_note_other, noteCountKey(0))
        assertEquals(Res.string.mirror_history_meta_note_other, noteCountKey(3))
    }
}

package presentation.feature.mirror.core.history

import androidx.compose.ui.unit.IntSize
import domain.core.source.model.history.BatchStatusModel
import domain.core.source.model.history.HistoryEntryModel
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import presentation.core.ui.source.kit.atom.pill.StatusPillTone
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.StagedAnnotation
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_history_meta_note_one
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_history_meta_note_other
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.Instant

/** Every text, time, id, tone and geometry decision the History tab makes, and nothing else. */

/** The time period a row is filed under. Declaration order is render order. */
internal enum class HistoryBucket {
    /** Sent on the local calendar day it is now. */
    Today,

    /** Sent on the local calendar day before today. */
    Yesterday,

    /** Anything else, including anything dated into the future. */
    Older,
}

/**
 * One rendered period of the list.
 *
 * @property bucket Which header the group prints.
 * @property entries Its rows, newest first.
 */
internal data class HistoryGroup(
    val bucket: HistoryBucket,
    val entries: List<HistoryEntryModel>,
)

/**
 * Buckets [entries] by **local calendar day**, not by elapsed hours: a batch sent at 23:59 is
 * "Yesterday" one minute later, which is what the reader means by the word.
 *
 * Empty buckets are dropped, so the caller renders a header only for a group that has rows.
 *
 * @param entries Rows to group, in any order.
 * @param nowMs Wall-clock now, in epoch milliseconds — passed in so the function stays pure.
 * @param zone Zone the calendar days are measured in.
 */
internal fun groupHistory(
    entries: List<HistoryEntryModel>,
    nowMs: Long,
    zone: TimeZone = TimeZone.currentSystemDefault(),
): List<HistoryGroup> {
    val today = localDate(nowMs, zone)
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    val byBucket = entries
        .sortedByDescending { it.sentAtMs }
        .groupBy {
            when (localDate(it.sentAtMs, zone)) {
                today -> HistoryBucket.Today
                yesterday -> HistoryBucket.Yesterday
                else -> HistoryBucket.Older
            }
        }
    return HistoryBucket.entries.mapNotNull { bucket ->
        byBucket[bucket]?.let { HistoryGroup(bucket, it) }
    }
}

/** The meta line's clock, zero-padded 24-hour local time — `14:02`, never `2:2`. */
internal fun metaTime(sentAtMs: Long, zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val time = Instant.fromEpochMilliseconds(sentAtMs).toLocalDateTime(zone).time
    return "${time.hour.pad()}:${time.minute.pad()}"
}

/**
 * The meta line's `#a41f` tag.
 *
 * FR-30 asks for "the last 4 hex characters of the batch id", which is unsatisfiable as
 * written: `mintSessionId` produces `<deviceId>-f<frameId>-<sentAtMs>`, whose tail is decimal
 * milliseconds — four digits that change once a second and read as a number, not an id. A
 * 16-bit hash of the whole id keeps the design's four-hex-character shape and is stable for
 * the life of the batch, which is what the tag is for.
 */
internal fun shortBatchId(id: String): String = (id.hashCode() and HASH_MASK)
    .toString(HEX_RADIX)
    .padStart(SHORT_ID_LENGTH, '0')

/**
 * The bounding box of every annotation in the batch, or `null` when there is nothing to frame.
 *
 * The thumbnail shows the whole batch at once, so this is a union and never the first rect.
 */
internal fun unionRect(annotations: List<StagedAnnotation>): DeviceRect? {
    if (annotations.isEmpty()) return null
    val rects = annotations.map { it.rect }
    return DeviceRect(
        left = rects.minOf { it.left },
        top = rects.minOf { it.top },
        right = rects.maxOf { it.right },
        bottom = rects.maxOf { it.bottom },
    )
}

/**
 * Pads [union] out for breathing room and clamps it to the frame on all four sides.
 *
 * Same design-space reasoning as `core/payload/CropWindow.kt`: [designPad] is quoted against a
 * [designWidth]-wide mock, so it scales with the frame — a literal 18 device pixels would be
 * invisible on a 1080-wide capture. Deliberately **not** `cropWindow()`: that one is quoted at
 * a different pad against a different box and returns a render window, not a source rect.
 */
internal fun thumbnailRect(
    union: DeviceRect,
    frameSize: IntSize,
    designPad: Float = THUMBNAIL_DESIGN_PAD,
    designWidth: Float = THUMBNAIL_DESIGN_WIDTH,
): DeviceRect {
    val pad = designPad * frameSize.width / designWidth
    val left = max(0f, union.left - pad)
    val top = max(0f, union.top - pad)
    val width = min(frameSize.width - left, union.width + 2f * pad)
    val height = min(frameSize.height - top, union.height + 2f * pad)
    return DeviceRect(
        left = left.roundToInt(),
        top = top.roundToInt(),
        right = (left + width).roundToInt(),
        bottom = (top + height).roundToInt(),
    )
}

/**
 * Status chip tone, written out one branch per value so adding a sixth status is a compile
 * error rather than a silent default.
 */
internal fun statusTone(status: BatchStatusModel): StatusPillTone = when (status) {
    BatchStatusModel.Sent -> StatusPillTone.Neutral
    BatchStatusModel.InProgress -> StatusPillTone.Busy
    BatchStatusModel.Fixed -> StatusPillTone.Done
    BatchStatusModel.Verified -> StatusPillTone.Neutral
    BatchStatusModel.FollowUpSent -> StatusPillTone.Alert
}

/** How many batches the agent is still working on. */
internal fun inProgressCount(entries: List<HistoryEntryModel>): Int =
    entries.count { it.status == BatchStatusModel.InProgress }

/**
 * The History tab's badge, or `null` when there is no badge to paint (AC-34).
 *
 * `SidebarTabs` renders any non-null count, so a present zero paints a literal `"0"` in the
 * strip. Omitting at zero is therefore a decision, not a formatting detail, and it lives here
 * — testable — rather than as an `if` in the shell window, which has no test wiring.
 */
internal fun historyBadgeCount(entries: List<HistoryEntryModel>): Int? =
    inProgressCount(entries).takeIf { it > 0 }

/**
 * Whether a row has a BEFORE frame to diff against, i.e. whether its Verify button is live.
 *
 * **Not** gated on [BatchStatusModel] (FR-21 / AC-23). `Fixed` is written by nothing in the
 * app — only the agent would ever set it, and it does not — so a status gate made the button
 * unreachable on every row that exists. The real precondition is the frame: a batch whose disk
 * write failed carries no `framePath` and has nothing to compare. The button stays visible and
 * disabled rather than vanishing, so the control is discoverable; a path that pointed at a
 * file the user has since deleted still falls through to `VerifyError.NotOnDisk` at run time,
 * because a stored path is a hint, never a guarantee.
 */
internal fun canVerify(entry: HistoryEntryModel): Boolean = entry.framePath != null

/**
 * Singular/plural key for the meta line's note count.
 *
 * Two keys rather than a `<plurals>` resource: this repo has zero plural resources, and
 * English needs exactly two forms here.
 */
internal fun noteCountKey(count: Int): StringResource =
    if (count == 1) Res.string.mirror_history_meta_note_one else Res.string.mirror_history_meta_note_other

private fun localDate(epochMs: Long, zone: TimeZone): LocalDate =
    Instant.fromEpochMilliseconds(epochMs).toLocalDateTime(zone).date

private fun Int.pad(): String = toString().padStart(2, '0')

/** Low 16 bits of the id's hash — four hex characters, as the design's `#a41f` tag. */
private const val HASH_MASK = 0xFFFF
private const val HEX_RADIX = 16
private const val SHORT_ID_LENGTH = 4

/** Breathing room around the union box, quoted against the design's 296px-wide mock. */
private const val THUMBNAIL_DESIGN_PAD = 18f
private const val THUMBNAIL_DESIGN_WIDTH = 296f

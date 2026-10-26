package domain.core.source.model.history

import domain.core.source.model.base.Model
import domain.core.source.model.device.HierarchySnapshotModel

/**
 * Everything a just-sent batch needs to become durable: the row's own fields plus the bulk
 * bytes that belong on disk rather than in a column.
 *
 * Like [HistoryEntryModel], the `ByteArray` members make equality identity-based.
 *
 * @property id The sent envelope's session id; becomes the row's primary key.
 * @property sentAtMs Wall-clock send time in epoch milliseconds.
 * @property deviceLabel Human-readable device the frame came from.
 * @property prompt The prompt as assembled into the envelope — never the UI's draft override.
 * @property frameBytes Full frame PNG, written to disk by the file store.
 * @property thumbPng Pre-scaled thumbnail PNG, stored inline on the row; `null` is tolerated.
 * @property hierarchyXml View-hierarchy dump, written to disk; may be hundreds of KB. This is
 *   the WRITE side: it is the only form the send path has, since the envelope carries raw XML
 *   and nothing in presentation may parse it.
 * @property hierarchy The same dump already parsed, populated on the READ side only, by the
 *   repository — the parser lives in the data layer and presentation must not import it.
 *   `null` when no dump was stored, when it could not be read back, or when it parsed to no
 *   nodes; a consumer degrades to per-annotation no-match exactly as it does for a missing
 *   file. Kept ALONGSIDE [hierarchyXml] rather than replacing it because the two directions
 *   genuinely differ: `record` can only be given the string, and rebuilding the string from a
 *   snapshot would ship a document the device never emitted.
 * @property annotations The batch's annotations, in the order they were drawn.
 */
public data class SentBatchModel(
    val id: String,
    val sentAtMs: Long,
    val deviceLabel: String,
    val prompt: String,
    val frameBytes: ByteArray? = null,
    val thumbPng: ByteArray? = null,
    val hierarchyXml: String? = null,
    val hierarchy: HierarchySnapshotModel? = null,
    val annotations: List<SentAnnotationModel> = emptyList(),
) : Model

/**
 * One annotation of a [SentBatchModel], in frame pixel coordinates.
 *
 * @property left Left edge of the annotated rect.
 * @property top Top edge of the annotated rect.
 * @property right Right edge of the annotated rect.
 * @property bottom Bottom edge of the annotated rect.
 * @property comment What the tester wrote; may be empty.
 * @property cropBytes PNG of this annotation's crop, written to disk; `null` when unavailable.
 */
public data class SentAnnotationModel(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val comment: String,
    val cropBytes: ByteArray? = null,
) : Model

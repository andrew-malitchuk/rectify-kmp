package domain.core.source.model.history

import domain.core.source.model.base.Model

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
 * @property hierarchyXml View-hierarchy dump, written to disk; may be hundreds of KB.
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

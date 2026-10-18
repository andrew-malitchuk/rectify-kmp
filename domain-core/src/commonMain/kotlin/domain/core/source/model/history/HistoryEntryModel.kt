package domain.core.source.model.history

import domain.core.source.model.base.Model

/**
 * One persisted batch, as the History list reads it.
 *
 * [thumbPng] makes equality **identity-based**: `ByteArray` compares by reference, so two
 * models holding equal bytes are never `==`. Compare by [id] rather than by instance, and key
 * list items by [id] so a re-emission of the same row does not force a recomposition.
 *
 * @property id The batch id, identical to the sent envelope's session id.
 * @property sentAtMs Wall-clock send time in epoch milliseconds.
 * @property status Where the batch sits in the agent loop.
 * @property title First annotation's comment; may be empty.
 * @property annotationCount Number of annotations in the batch.
 * @property thumbPng Small PNG of the annotated region, or `null` — render a bare plate, never
 *   a placeholder image.
 * @property framePath Absolute path of the full frame on disk, or `null` when the write failed.
 *   The file may have been deleted since: treat a stored path as a hint, never as a guarantee.
 */
public data class HistoryEntryModel(
    val id: String,
    val sentAtMs: Long,
    val status: BatchStatusModel,
    val title: String,
    val annotationCount: Int,
    val thumbPng: ByteArray? = null,
    val framePath: String? = null,
) : Model

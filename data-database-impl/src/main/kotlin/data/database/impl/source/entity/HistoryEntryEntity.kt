package data.database.impl.source.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A batch of annotations that was sent to the coding agent.
 *
 * Public, unlike the rest of `core/`, because it is the wire type of the public
 * `HistoryDataSource` and `explicitApi()` forbids a public declaration naming an internal type
 * (see `.claude/rules/database.md`).
 *
 * Bulk bytes live on disk and are referenced by path; only the few-KB [thumbPng] is inline.
 * A path whose file the user later deleted must still render — never assume it resolves.
 *
 * @property id The sent envelope's session id.
 * @property sentAtMs Wall-clock send time in epoch milliseconds; the list orders on this.
 * @property status [domain.core.source.model.history.BatchStatusModel] name as stored; an
 *   unrecognised value is mapped back to `Sent` rather than throwing.
 * @property title First annotation's comment; may be empty.
 * @property annotationCount Number of annotations in the batch.
 * @property deviceLabel Human-readable device the frame came from.
 * @property prompt The prompt as assembled into the envelope.
 * @property dirPath Batch directory under `~/.rectify/history/`, or `null` when the write failed.
 * @property framePath Full-frame PNG path, or `null`.
 * @property hierarchyPath View-hierarchy XML path, or `null`.
 * @property thumbPng Small PNG of the annotated region, or `null` for a bare plate.
 */
@Entity(tableName = "history_entry")
public data class HistoryEntryEntity(
    @PrimaryKey
    val id: String,
    val sentAtMs: Long,
    val status: String,
    val title: String,
    val annotationCount: Int,
    val deviceLabel: String,
    val prompt: String,
    val dirPath: String? = null,
    val framePath: String? = null,
    val hierarchyPath: String? = null,
    val thumbPng: ByteArray? = null,
)

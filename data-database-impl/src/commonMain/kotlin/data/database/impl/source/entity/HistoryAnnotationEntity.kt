package data.database.impl.source.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * One annotation of a [HistoryEntryEntity], normalised into its own row.
 *
 * Deleting the parent batch cascades to its annotations.
 *
 * @property batchId Owning [HistoryEntryEntity.id].
 * @property index Position within the batch; composite primary key with [batchId].
 * @property left Left edge of the annotated rect, in frame pixels.
 * @property top Top edge of the annotated rect, in frame pixels.
 * @property right Right edge of the annotated rect, in frame pixels.
 * @property bottom Bottom edge of the annotated rect, in frame pixels.
 * @property comment What the tester wrote; may be empty.
 * @property cropPath This annotation's crop PNG on disk, or `null` when the write failed.
 */
@Entity(
    tableName = "history_annotation",
    primaryKeys = ["batchId", "index"],
    foreignKeys = [
        ForeignKey(
            entity = HistoryEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("batchId")],
)
public data class HistoryAnnotationEntity(
    val batchId: String,
    val index: Int,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val comment: String,
    val cropPath: String? = null,
)

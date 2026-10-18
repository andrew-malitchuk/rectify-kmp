package data.database.impl.source.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * A batch together with its annotations — the shape every history read returns.
 *
 * @property entry The batch row.
 * @property annotations Its annotations; empty when the batch had none.
 */
public data class HistoryEntryWithAnnotations(
    @Embedded
    val entry: HistoryEntryEntity,
    @Relation(parentColumn = "id", entityColumn = "batchId")
    val annotations: List<HistoryAnnotationEntity>,
)

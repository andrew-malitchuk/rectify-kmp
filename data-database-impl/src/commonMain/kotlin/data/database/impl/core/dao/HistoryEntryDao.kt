package data.database.impl.core.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import data.database.impl.source.entity.HistoryAnnotationEntity
import data.database.impl.source.entity.HistoryEntryEntity
import data.database.impl.source.entity.HistoryEntryWithAnnotations
import kotlinx.coroutines.flow.Flow

/** Reads and writes the `history_entry` / `history_annotation` pair. */
@Dao
internal interface HistoryEntryDao {

    /**
     * Writes a batch and its annotations as one unit — never a half-written batch.
     *
     * @return the parent row's implicit SQLite rowid.
     */
    @Transaction
    suspend fun insert(entry: HistoryEntryEntity, annotations: List<HistoryAnnotationEntity>): Long {
        val rowId = insertEntry(entry)
        insertAnnotations(annotations)
        return rowId
    }

    /** Newest batch first. */
    @Transaction
    @Query("SELECT * FROM history_entry ORDER BY sentAtMs DESC")
    fun observeAll(): Flow<List<HistoryEntryWithAnnotations>>

    /** No-op when [id] is unknown. */
    @Query("UPDATE history_entry SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: HistoryEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotations(annotations: List<HistoryAnnotationEntity>)
}

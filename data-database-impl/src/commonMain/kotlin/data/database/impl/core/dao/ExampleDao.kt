package data.database.impl.core.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import data.database.impl.core.entity.ExampleEntity
import kotlinx.coroutines.flow.Flow

/** Placeholder DAO demonstrating the Room setup. Delete this with [ExampleEntity]. */
@Dao
internal interface ExampleDao {
    @Insert
    suspend fun insert(entity: ExampleEntity): Long

    @Query("SELECT * FROM example")
    suspend fun getAll(): List<ExampleEntity>

    @Query("SELECT * FROM example")
    fun observeAll(): Flow<List<ExampleEntity>>
}

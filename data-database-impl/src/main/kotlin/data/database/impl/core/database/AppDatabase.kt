package data.database.impl.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import data.database.impl.core.dao.HistoryEntryDao
import data.database.impl.source.entity.HistoryAnnotationEntity
import data.database.impl.source.entity.HistoryEntryEntity

@Database(
    entities = [HistoryEntryEntity::class, HistoryAnnotationEntity::class],
    version = 1,
    exportSchema = true,
)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun historyEntryDao(): HistoryEntryDao
}

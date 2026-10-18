package data.database.impl.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import data.database.impl.core.dao.HistoryEntryDao
import data.database.impl.source.entity.HistoryAnnotationEntity
import data.database.impl.source.entity.HistoryEntryEntity

@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

@Database(
    entities = [HistoryEntryEntity::class, HistoryAnnotationEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun historyEntryDao(): HistoryEntryDao
}

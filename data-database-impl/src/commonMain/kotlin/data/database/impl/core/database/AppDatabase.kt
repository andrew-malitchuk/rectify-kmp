package data.database.impl.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import data.database.impl.core.dao.ExampleDao
import data.database.impl.core.entity.ExampleEntity

@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

// Skeleton — ExampleEntity/ExampleDao demonstrate the pattern. Replace with your own
// @Entity classes and DAO accessors.
@Database(
    entities = [ExampleEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun exampleDao(): ExampleDao
}

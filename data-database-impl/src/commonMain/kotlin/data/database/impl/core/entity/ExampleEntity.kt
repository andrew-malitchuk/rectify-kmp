package data.database.impl.core.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Placeholder entity demonstrating the Room setup. Delete this and add your own
 * `@Entity` classes, then update [data.database.impl.core.database.AppDatabase.entities].
 */
@Entity(tableName = "example")
internal data class ExampleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val value: String,
)

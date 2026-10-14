package data.database.impl.core

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import data.database.impl.core.database.AppDatabase
import org.koin.core.module.Module
import java.io.File

/**
 * Desktop platform implementation of [provideDatabaseBuilder].
 *
 * Registers an [AppDatabase] singleton stored under the current user's home directory.
 *
 * @see data.database.impl.core.provideDatabaseBuilder
 */
internal actual fun Module.provideDatabaseBuilder() {
    single<AppDatabase> {
        val dbPath = File(System.getProperty("user.home"), "rectify.db").absolutePath
        Room.databaseBuilder<AppDatabase>(name = dbPath)
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}

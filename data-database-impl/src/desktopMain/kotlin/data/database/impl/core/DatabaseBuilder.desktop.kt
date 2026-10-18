package data.database.impl.core

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import data.database.impl.core.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import java.io.File

/**
 * Desktop platform implementation of [provideDatabaseBuilder].
 *
 * Registers an [AppDatabase] singleton stored at `~/.rectify/rectify.db`, alongside the
 * handoff slot at `~/.rectify/handoff`. The parent directory is created on demand.
 *
 * @see data.database.impl.core.provideDatabaseBuilder
 */
internal actual fun Module.provideDatabaseBuilder() {
    single<AppDatabase> {
        val directory = File(System.getProperty("user.home"), ".rectify").apply { mkdirs() }
        Room.databaseBuilder<AppDatabase>(name = File(directory, "rectify.db").absolutePath)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}

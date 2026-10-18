package data.database.impl.core

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import data.database.impl.core.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.PosixFilePermissions

/** `rwx------` — `~/.rectify` holds the history archive and the screenshots it references. */
private const val OWNER_ONLY = "rwx------"

/**
 * Desktop platform implementation of [provideDatabaseBuilder].
 *
 * Registers an [AppDatabase] singleton stored at `~/.rectify/rectify.db`, alongside the
 * handoff slot at `~/.rectify/handoff`. The parent directory is created on demand, owner-only
 * where the filesystem supports POSIX permissions — `~` is world-readable on a typical Linux
 * box. `0700` on the directory also covers `rectify.db` and its `-wal`/`-shm` siblings, which
 * SQLite creates itself: no other user can traverse into the directory to reach them.
 *
 * @see data.database.impl.core.provideDatabaseBuilder
 */
internal actual fun Module.provideDatabaseBuilder() {
    single<AppDatabase> {
        val directory = File(System.getProperty("user.home"), ".rectify")
        Files.createDirectories(directory.toPath(), *ownerOnlyAttributes())
        Room.databaseBuilder<AppDatabase>(name = File(directory, "rectify.db").absolutePath)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}

/**
 * A second copy of `data-handoff-api`'s slot-root permission helper (see the note on
 * `FileSystemBatchFileStore`): this module cannot see that one, and four lines do not justify
 * a new dependency or a new public API.
 */
private fun ownerOnlyAttributes(): Array<FileAttribute<*>> =
    if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
        arrayOf(PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(OWNER_ONLY)))
    } else {
        emptyArray()
    }

package data.repository.impl.core.store

import co.touchlab.kermit.Logger
import data.repository.impl.source.store.BatchFileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.PosixFilePermissions

private val log = Logger.withTag("BatchFileStore")

/** A batch id reaches us from `mintSessionId`, which embeds a device id — not filesystem-safe. */
private val UNSAFE_IN_A_DIRECTORY_NAME = Regex("[^A-Za-z0-9._-]")

/** Sanitising keeps `.`, so these three would still name the history root itself or its parent. */
private val NOT_A_BATCH_DIRECTORY_NAME = setOf("", ".", "..")

/** `rwx------` — a batch directory holds screenshots of whatever the user had on screen. */
private const val OWNER_ONLY_DIRECTORY = "rwx------"

/** `rw-------` — and so do the files inside it. */
private const val OWNER_ONLY_FILE = "rw-------"

/**
 * Real [BatchFileStore]: one directory per batch under `~/.rectify/history/`, written with
 * `java.io` off the main thread.
 *
 * Unlike the handoff slot this archive is permanent and unpruned, so it is created owner-only
 * where the filesystem supports POSIX permissions — `~` is world-readable on a typical Linux
 * box, and every frame, crop, hierarchy dump and comment the user ever sent ends up here.
 *
 * @property root History root; a parameter purely so tests can point it at a scratch directory.
 */
internal class FileSystemBatchFileStore(
    private val root: File = File(System.getProperty("user.home"), ".rectify/history"),
) : BatchFileStore {

    override suspend fun write(batchId: String, files: List<Pair<String, ByteArray>>): String? =
        withContext(Dispatchers.IO) {
            // Sanitised, so a device id can never walk out of the history root (PLAN-AS-6).
            // Sanitising alone is not enough: it keeps `.`, so "..", "." and "" survive it and
            // resolve back onto the root or its parent. Those are rejected, not rewritten.
            val name = batchId.replace(UNSAFE_IN_A_DIRECTORY_NAME, "_")
            if (name in NOT_A_BATCH_DIRECTORY_NAME) {
                log.w { "refusing batch id $batchId: it does not name a directory inside $root" }
                return@withContext null
            }
            val directory = File(root, name)
            runCatching {
                Files.createDirectories(directory.toPath(), *ownerOnly(OWNER_ONLY_DIRECTORY))
                files.forEach { (fileName, bytes) ->
                    writeOwnerOnly(File(directory, fileName).toPath(), bytes)
                }
                directory.absolutePath
            }
                .onFailure { log.w(it) { "could not write batch $batchId to $directory" } }
                .getOrNull()
        }
}

private val supportsPosix: Boolean =
    FileSystems.getDefault().supportedFileAttributeViews().contains("posix")

/**
 * Deliberately a second copy of `data-handoff-api`'s slot-root permission helper rather than a
 * shared one: this module does not depend on `data-handoff-api` and has no desktop-only source
 * set in common with it, so sharing eight lines would mean a new dependency (or a new module)
 * plus making a filesystem detail public API. Keep the two in step by hand.
 */
private fun ownerOnly(mode: String): Array<FileAttribute<*>> =
    if (supportsPosix) {
        arrayOf(PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(mode)))
    } else {
        emptyArray()
    }

/**
 * Permissions are applied after the write rather than at creation: the enclosing directory is
 * already `rwx------`, so nothing can reach the file during the window in between.
 */
private fun writeOwnerOnly(file: Path, bytes: ByteArray) {
    Files.write(file, bytes)
    if (supportsPosix) {
        Files.setPosixFilePermissions(file, PosixFilePermissions.fromString(OWNER_ONLY_FILE))
    }
}

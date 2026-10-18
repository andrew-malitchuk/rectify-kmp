package data.repository.impl.core.store

import co.touchlab.kermit.Logger
import data.repository.impl.source.store.BatchFileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private val log = Logger.withTag("BatchFileStore")

/** A batch id reaches us from `mintSessionId`, which embeds a device id — not filesystem-safe. */
private val UNSAFE_IN_A_DIRECTORY_NAME = Regex("[^A-Za-z0-9._-]")

/**
 * Real [BatchFileStore]: one directory per batch under `~/.rectify/history/`, written with
 * `java.io` off the main thread.
 *
 * @property root History root; a parameter purely so tests can point it at a scratch directory.
 */
internal class FileSystemBatchFileStore(
    private val root: File = File(System.getProperty("user.home"), ".rectify/history"),
) : BatchFileStore {

    override suspend fun write(batchId: String, files: List<Pair<String, ByteArray>>): String? =
        withContext(Dispatchers.IO) {
            // Sanitised, so a device id can never walk out of the history root (PLAN-AS-6).
            val directory = File(root, batchId.replace(UNSAFE_IN_A_DIRECTORY_NAME, "_"))
            runCatching {
                directory.mkdirs()
                files.forEach { (name, bytes) -> File(directory, name).writeBytes(bytes) }
                directory.absolutePath
            }
                .onFailure { log.w(it) { "could not write batch $batchId to $directory" } }
                .getOrNull()
        }
}

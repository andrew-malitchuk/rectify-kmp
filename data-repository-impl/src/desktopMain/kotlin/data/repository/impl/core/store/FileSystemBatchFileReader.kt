package data.repository.impl.core.store

import co.touchlab.kermit.Logger
import data.repository.impl.source.store.BatchFileReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private val log = Logger.withTag("BatchFileReader")

/**
 * Real [BatchFileReader]: plain `java.io`, off the main thread, for the absolute paths
 * [FileSystemBatchFileStore] wrote under `~/.rectify/history/`.
 *
 * The `isFile` check is what turns "this path is a directory" into `null` instead of the
 * `IOException` `readBytes` would throw; everything else that can go wrong is caught.
 */
internal class FileSystemBatchFileReader : BatchFileReader {

    override suspend fun read(path: String): ByteArray? = withContext(Dispatchers.IO) {
        runCatching { File(path).takeIf { it.isFile }?.readBytes() }
            .onFailure { log.w(it) { "could not read $path" } }
            .getOrNull()
    }
}

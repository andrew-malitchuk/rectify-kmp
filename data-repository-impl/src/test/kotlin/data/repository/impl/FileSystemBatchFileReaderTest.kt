package data.repository.impl

import data.repository.impl.core.store.FileSystemBatchFileReader
import data.repository.impl.core.store.FileSystemBatchFileStore
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * AC-45 — the read half of the on-disk archive: bytes come back exactly as written, and every
 * way a stored path can be wrong reads `null` instead of throwing. A stored path is documented
 * as a hint, never a guarantee, so the reader must survive a user who deleted the directory.
 *
 * Desktop-only because `java.io` is.
 */
class FileSystemBatchFileReaderTest {

    private val root = File(
        System.getProperty("java.io.tmpdir"),
        "rectify-reader-test-${System.nanoTime()}",
    )

    private val reader = FileSystemBatchFileReader()

    @AfterTest
    fun cleanUp() {
        root.deleteRecursively()
    }

    @Test
    fun bytesWrittenByTheStoreReadBackIdentical() = runBlocking {
        val frame = ByteArray(512) { (it % 251).toByte() }
        val directory = FileSystemBatchFileStore(root).write(
            batchId = "batch-1",
            files = listOf("frame.png" to frame, "hierarchy.xml" to "<node/>".encodeToByteArray()),
        )
        assertNotNull(directory)

        assertContentEquals(frame, reader.read(File(directory, "frame.png").absolutePath))
        assertContentEquals(
            "<node/>".encodeToByteArray(),
            reader.read(File(directory, "hierarchy.xml").absolutePath),
        )
    }

    @Test
    fun anEmptyFileReadsBackAsAnEmptyArrayNotNull() = runBlocking {
        val directory = FileSystemBatchFileStore(root)
            .write(batchId = "batch-1", files = listOf("frame.png" to ByteArray(0)))
        assertNotNull(directory)

        assertContentEquals(ByteArray(0), reader.read(File(directory, "frame.png").absolutePath))
    }

    @Test
    fun aMissingFileReadsNull() = runBlocking {
        val directory = FileSystemBatchFileStore(root)
            .write(batchId = "batch-1", files = listOf("frame.png" to byteArrayOf(1)))
        assertNotNull(directory)

        assertNull(reader.read(File(directory, "deleted-by-the-user.png").absolutePath))
    }

    @Test
    fun aDirectoryPathReadsNull() = runBlocking {
        val directory = FileSystemBatchFileStore(root)
            .write(batchId = "batch-1", files = listOf("frame.png" to byteArrayOf(1)))
        assertNotNull(directory)

        assertNull(reader.read(directory))
    }

    @Test
    fun aPathWhoseParentIsAFileReadsNull() = runBlocking {
        val directory = FileSystemBatchFileStore(root)
            .write(batchId = "batch-1", files = listOf("frame.png" to byteArrayOf(1)))
        assertNotNull(directory)

        assertNull(reader.read(File(directory, "frame.png/nested.png").absolutePath))
    }

    @Test
    fun anEmptyPathReadsNull() = runBlocking {
        assertNull(reader.read(""))
    }
}

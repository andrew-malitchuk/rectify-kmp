package data.repository.impl

import data.repository.impl.core.store.FileSystemBatchFileStore
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * TAC-1 / TAC-2 — the only part of the store with logic of its own: the directory name it
 * derives, and its promise to answer `null` rather than throw.
 *
 * Desktop-only because `java.io` is.
 */
class FileSystemBatchFileStoreTest {

    private val root = File(System.getProperty("java.io.tmpdir"), "rectify-store-test-${System.nanoTime()}")

    @AfterTest
    fun cleanUp() {
        root.deleteRecursively()
    }

    @Test
    fun writePutsEveryFileInOneDirectoryNamedAfterTheBatch() = runBlocking {
        val dirPath = FileSystemBatchFileStore(root).write(
            batchId = "batch-1",
            files = listOf("frame.png" to byteArrayOf(1, 2), "crop-0.png" to byteArrayOf(3)),
        )

        assertEquals(File(root, "batch-1").absolutePath, dirPath)
        assertContentEquals(byteArrayOf(1, 2), File(dirPath, "frame.png").readBytes())
        assertContentEquals(byteArrayOf(3), File(dirPath, "crop-0.png").readBytes())
    }

    @Test
    fun writeSanitisesABatchIdSoItCannotEscapeTheHistoryRoot() = runBlocking {
        // mintSessionId embeds a device id, which is not guaranteed filesystem-safe.
        val dirPath = FileSystemBatchFileStore(root).write("../../etc/pwn ed", listOf("f" to byteArrayOf(1)))

        assertEquals(File(root, ".._.._etc_pwn_ed").absolutePath, dirPath)
        assertTrue(File(dirPath).canonicalPath.startsWith(root.canonicalPath), "the batch directory stayed inside the root")
    }

    @Test
    fun writeReturnsNullRatherThanThrowingWhenTheRootCannotBeADirectory() = runBlocking {
        // A regular file where the history root should be: mkdirs fails, writeBytes throws.
        root.parentFile.mkdirs()
        root.writeText("not a directory")

        assertNull(FileSystemBatchFileStore(root).write("batch-1", listOf("frame.png" to byteArrayOf(1))))
    }
}

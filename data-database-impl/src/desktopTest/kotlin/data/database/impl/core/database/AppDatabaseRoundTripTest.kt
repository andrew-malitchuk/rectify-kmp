package data.database.impl.core.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import data.database.impl.core.entity.ExampleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Proves the Room KMP toolchain actually works on desktop — KSP output, the generated
 * [AppDatabaseConstructor] actual, [BundledSQLiteDriver] linkage and invalidation-driven
 * `Flow` queries — against a temp-directory file, never the user's `~/.rectify/rectify.db`.
 */
internal class AppDatabaseRoundTripTest {

    private val directory: File = createTempDirectory(prefix = "rectify-db-test").toFile()

    private val database: AppDatabase = Room
        .databaseBuilder<AppDatabase>(name = File(directory, "rectify.db").absolutePath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

    @AfterTest
    fun tearDown() {
        database.close()
        directory.deleteRecursively()
    }

    @Test
    fun `insert is readable back`() = runTest {
        val id = database.exampleDao().insert(ExampleEntity(value = "round-trip"))

        assertTrue(id > 0, "insert should return a generated row id")
        assertEquals(listOf(ExampleEntity(id = id, value = "round-trip")), database.exampleDao().getAll())
    }

    @Test
    fun `observe emits the row inserted after collection starts`() = runTest {
        val dao = database.exampleDao()
        val emission = async(Dispatchers.IO) { dao.observeAll().first { it.isNotEmpty() } }

        val id = dao.insert(ExampleEntity(value = "observed"))

        assertEquals(listOf(ExampleEntity(id = id, value = "observed")), emission.await())
    }
}

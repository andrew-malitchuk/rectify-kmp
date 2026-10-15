package data.device.impl.tool

import data.device.api.source.model.ToolKind
import data.device.impl.core.tool.ToolResolver
import data.device.impl.core.tool.isExecutableFile
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The user-chosen `adb` path seam.
 *
 * The path arrives from a file dialog, so it is outside the program's control: these tests
 * exercise it with the REAL [isExecutableFile] predicate against real files, because a fake
 * predicate would prove nothing about the trust boundary. The predicate is only sandboxed to
 * the temp root, so `/opt/homebrew/bin/adb` on the developer's own machine cannot win the
 * probe and turn these into hardware tests.
 */
class ToolResolverTest {

    private val root: File = createTempDirectory("tool-resolver").toFile()

    /** `adb` on the fake `PATH` - what every rejected override must fall through to. */
    private val fallbackAdb: String = executable("path-bin/adb")

    /** The file the user would pick in the dialog. */
    private val chosenAdb: String = executable("Downloads/platform-tools/adb")

    @AfterTest
    fun cleanUp() {
        root.deleteRecursively()
    }

    @Test
    fun aValidUserPathWinsOverEveryOrderedCandidateAndIsRecorded() = runTest {
        val probe = resolver { chosenAdb }.resolve(ToolKind.Adb)

        assertEquals(
            listOf(chosenAdb, chosenAdb, true, true),
            listOf(probe.path, probe.probedPaths.first(), probe.found, fallbackAdb in probe.probedPaths),
        )
    }

    /**
     * Every way a dialog-chosen path can be unusable, asserted in one shot.
     *
     * One `assertEquals` over the whole classification on purpose: a chain of `assertTrue`
     * short-circuits, so a mutation that only breaks the third case would look fully proven.
     */
    @Test
    fun anUnusableUserPathFallsThroughToTheOrderedCandidates() = runTest {
        val unusable = listOf(
            "doesNotExist" to "$root/nowhere/adb",
            "isADirectory" to directory("Applications/adb"),
            "isNotExecutable" to plainFile("Downloads/adb.txt"),
        )

        val outcomes = unusable.map { (label, path) ->
            val probe = resolver { path }.resolve(ToolKind.Adb)
            Triple(label, probe.path, probe.probedPaths.first())
        }

        assertEquals(
            unusable.map { (label, path) -> Triple(label, fallbackAdb, path) },
            outcomes,
        )
    }

    /**
     * The resolver caches successes, and a user path is the one candidate that can change
     * under it: picking a different file, or deleting the one that was picked, must be seen on
     * the next probe rather than served from the cache.
     */
    @Test
    fun aUserPathThatStopsWorkingIsNotServedFromTheCache() = runTest {
        val resolver = resolver { chosenAdb }
        val beforeDeletion = resolver.resolve(ToolKind.Adb).path
        File(chosenAdb).delete()
        val afterDeletion = resolver.resolve(ToolKind.Adb).path

        assertEquals(listOf(chosenAdb, fallbackAdb), listOf(beforeDeletion, afterDeletion))
    }

    /**
     * The mirror image of the test above, and the one the cache can get wrong in silence: an
     * override the user removes must not come back from the map a later probe reads.
     */
    @Test
    fun clearingTheUserPathDoesNotResurrectItFromTheCache() = runTest {
        var override: String? = chosenAdb
        val resolver = resolver { override }
        val whileSet = resolver.resolve(ToolKind.Adb).path
        override = null
        val afterClearing = resolver.resolve(ToolKind.Adb).path

        assertEquals(listOf(chosenAdb, fallbackAdb), listOf(whileSet, afterClearing))
    }

    @Test
    fun onlyAdbConsultsTheUserPath() = runTest {
        val emulatorOverride = executable("Downloads/platform-tools/emulator")
        val probe = resolver { emulatorOverride }.resolve(ToolKind.Emulator)

        assertEquals(
            listOf(false, false),
            listOf(emulatorOverride in probe.probedPaths, probe.found),
        )
    }

    /**
     * A resolver that can only see the temp root.
     *
     * `homeDir` is blank so `~/Library/Android/sdk` contributes nothing, and the predicate is
     * the production one narrowed to [root] - the real `isFile && canExecute` still decides.
     */
    private fun resolver(userPath: suspend () -> String?) = ToolResolver(
        env = mapOf("PATH" to "$root/path-bin"),
        homeDir = "",
        isExecutable = { path -> path.startsWith("$root/") && isExecutableFile(path) },
        userPath = userPath,
    )

    private fun executable(relative: String): String = plainFile(relative).also { path ->
        File(path).writeText("#!/bin/sh\necho 'Android Debug Bridge version 1.0.41'\n")
        File(path).setExecutable(true)
    }

    private fun plainFile(relative: String): String = File(root, relative)
        .also { it.parentFile.mkdirs(); it.writeText("not a binary") }
        .absolutePath

    private fun directory(relative: String): String = File(root, relative)
        .also { it.mkdirs() }
        .absolutePath
}

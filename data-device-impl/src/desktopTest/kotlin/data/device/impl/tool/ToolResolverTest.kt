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
private const val ADB_BANNER = "Android Debug Bridge"

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
    /**
     * [identifyAdb] is faked for the same reason [ToolResolver.isExecutable] is — and for one
     * more. The real check SPAWNS `<path> --version`, and spawning inside `runTest`'s virtual
     * clock cannot work: the scheduler advances past `withTimeoutOrNull` instantly while the
     * real child is still starting, so the call always times out and every override would be
     * rejected here regardless of what it is.
     *
     * The fake answers the way the fixtures are built: `executable()` writes a script that
     * echoes adb's banner, `plainFile()` writes `not a binary`. So "identifies as adb" maps
     * onto "was built by `executable()`", which is what these tests mean by a usable path.
     */

    /**
     * The trust boundary's real question: an override must IDENTIFY itself, not merely run.
     *
     * `isExecutable` answers "can this be run", which is not "is this adb" — and the gap was
     * not theoretical. The path is persisted, so it is re-armed every launch; the cache is
     * bypassed while an override is set, so it is re-executed twice per 2 s poll indefinitely;
     * and a binary that exits 0 was indistinguishable from adb in the UI. One mis-click on a
     * downloaded shell script became code Rectify re-ran every two seconds, every session,
     * silently.
     *
     * The ordered candidates are deliberately NOT banner-checked, and that asymmetry is
     * asserted here too: they come from the SDK layout and the user's PATH, which this program
     * does not get to second-guess. Only a value that arrived from a file dialog has to prove
     * what it is.
     *
     * MUTATION: drop `|| identifiesAsAdb(candidate)` from the predicate in `resolve` — the
     * impostorRejected clause fails and the override wins again.
     */
    @Test
    fun anExecutableThatIsNotAdbIsRejectedAsAnOverrideButNotAsACandidate() = runTest {
        val impostor = plainFile("Downloads/rectify-adb-fix.sh").also { path ->
            File(path).writeText("#!/bin/sh\necho 'totally adb, trust me'\n")
            File(path).setExecutable(true)
        }

        assertEquals(
            expected = listOf(
                "impostorRejected=true",
                "fellBackTo=$fallbackAdb",
                "impostorStillListed=true",
            ),
            actual = resolver { impostor }.resolve(ToolKind.Adb).let { probe ->
                listOf(
                    "impostorRejected=${probe.path != impostor}",
                    "fellBackTo=${probe.path}",
                    // Still in probedPaths, so the diagnostic footer can answer "why not mine".
                    "impostorStillListed=${impostor in probe.probedPaths}",
                )
            },
        )
    }

    private fun resolver(userPath: suspend () -> String?) = ToolResolver(
        env = mapOf("PATH" to "$root/path-bin"),
        homeDir = "",
        isExecutable = { path -> path.startsWith("$root/") && isExecutableFile(path) },
        identifyAdb = { path -> File(path).let { it.isFile && it.readText().contains(ADB_BANNER) } },
        userPath = userPath,
    )

    private fun executable(relative: String): String = plainFile(relative).also { path ->
        File(path).writeText("#!/bin/sh\necho '$ADB_BANNER version 1.0.41'\n")
        File(path).setExecutable(true)
    }

    private fun plainFile(relative: String): String = File(root, relative)
        .also { it.parentFile.mkdirs(); it.writeText("not a binary") }
        .absolutePath

    private fun directory(relative: String): String = File(root, relative)
        .also { it.mkdirs() }
        .absolutePath
}

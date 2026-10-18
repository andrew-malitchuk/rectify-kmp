package data.device.impl.datasource

import data.device.api.source.error.DeviceCommandException
import data.device.impl.core.tool.ToolResolver
import data.device.impl.source.datasource.AdbHostSourceImpl
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * How long the fake daemon fork takes.
 *
 * Chosen to sit between the two budgets, which is the whole point of the defect: longer than
 * `MirrorTuning.HostCommandTimeoutMs` (3000 ms), far shorter than
 * `MirrorTuning.DaemonStartTimeoutMs` (15000 ms). A real cold fork measured 3.03 s on every
 * run, so this is the same side of both boundaries as the real cost — and because neither
 * boundary is close to the other, a loaded box can only make the slow call slower, which is
 * the direction both assertions already expect.
 */
private const val SLOW_FORK_SECONDS = "3.2"

/**
 * [AdbHostSourceImpl]'s two daemon calls, against a shell script standing in for `adb`.
 *
 * No handset and no real `adb`: what is under test is the argv each call runs and the budget
 * it runs under, both of which a fake binary observes exactly as well as hardware would.
 */
class AdbHostSourceImplTest {

    @Test
    fun bothDaemonCallsRunTheirOwnHostArgv() = runBlocking {
        val (adb, log) = fakeAdb(body = "exit 0")
        val source = adbHostSource(adb)

        source.startServer()
        source.killServer()

        // One spawn each, plus the one-off version probe ToolResolver does when it first
        // resolves the binary. No -s, no extra invocation.
        assertEquals(listOf("--version", "start-server", "kill-server"), log.readLines())
    }

    /**
     * The budget split, which is the fix.
     *
     * `start-server` pays the fork cost and must survive it; `kill-server` closes a local
     * socket and keeps the ordinary host budget. Asserted as one comparison over both
     * outcomes rather than a pair of chained assertions — a chain short-circuits, so a
     * mutation that only breaks the second call would never be reported.
     */
    @Test
    fun startServerOutlivesTheHostBudgetAndKillServerDoesNot() = runBlocking {
        val (adb, _) = fakeAdb(body = "sleep $SLOW_FORK_SECONDS")
        val source = adbHostSource(adb)

        val outcomes = listOf(
            "startServer" to outcomeOf { source.startServer() },
            "killServer" to outcomeOf { source.killServer() },
        )

        assertEquals(
            listOf("startServer" to "ok", "killServer" to "timeout"),
            outcomes,
            "a ${SLOW_FORK_SECONDS}s fork must fit startServer's watchdog and miss killServer's budget",
        )
    }
}

/** Runs [call] and reduces it to the one word each budget assertion cares about. */
private suspend fun outcomeOf(call: suspend () -> Unit): String = try {
    call()
    "ok"
} catch (failure: DeviceCommandException) {
    if (failure.isTimeout()) "timeout" else "failed ${failure.stderrTail}"
}

/**
 * A fake `adb` in a fake SDK layout.
 *
 * @param body Shell run for any subcommand other than `--version`.
 * @return The executable and the file it appends each invocation's arguments to.
 */
private fun fakeAdb(body: String): Pair<File, File> {
    val sdk = Files.createTempDirectory("rectify-fake-sdk").toFile()
    val log = File(sdk, "argv.log")
    val adb = File(sdk, "platform-tools").apply { mkdirs() }.let { File(it, "adb") }
    adb.writeText(
        """
        |#!/bin/sh
        |printf '%s\n' "${'$'}*" >> '${log.absolutePath}'
        |case "${'$'}1" in
        |--version) echo 'Android Debug Bridge version 1.0.41' ;;
        |*) $body ;;
        |esac
        """.trimMargin()
    )
    adb.setExecutable(true)
    listOf(adb, adb.parentFile, log, sdk).forEach(File::deleteOnExit)
    return adb to log
}

/** A source resolving `adb` to [adb] and nothing else. */
private fun adbHostSource(adb: File) = AdbHostSourceImpl(
    ToolResolver(
        env = mapOf("ANDROID_HOME" to adb.parentFile.parentFile.absolutePath),
        homeDir = "",
        // Pinned to the fake. With the real predicate the next candidate is
        // /opt/homebrew/bin/adb, so on a developer machine this test would start the host's
        // own daemon instead of the script it just wrote.
        isExecutable = { it == adb.absolutePath },
    )
)

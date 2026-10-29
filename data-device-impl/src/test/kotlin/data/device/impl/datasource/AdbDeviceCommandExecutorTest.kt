package data.device.impl.datasource

import data.device.api.source.error.DeviceCommandException
import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId
import data.device.api.source.tuning.MirrorTuning
import data.device.impl.core.tool.ToolResolver
import data.device.impl.source.datasource.AdbDeviceCommandExecutor
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [AdbDeviceCommandExecutor.timeoutMs], against a shell script standing in for `adb`.
 *
 * `UiAutomatorDump` moves a multi-megabyte XML tree, which is why it gets its own budget
 * ([MirrorTuning.DumpTimeoutMs], 10 s) rather than falling into `timeoutMs()`'s `else` branch
 * ([MirrorTuning.HostCommandTimeoutMs], 3 s). That `else` is the risk here: a missing `when`
 * branch does not fail to compile, it is silently swallowed. The budget boundaries below are
 * read from the constants themselves, never a literal, so a branch wired to the wrong constant
 * fails this test even if someone renumbers the constants later.
 */
class AdbDeviceCommandExecutorTest {

    /**
     * One test, both sides of the boundary - mirrors
     * `AdbHostSourceImplTest.startServerOutlivesTheHostBudgetAndKillServerDoesNot`. Both
     * sleep durations are derived from [MirrorTuning.DumpTimeoutMs] itself, never a literal:
     * under-budget must survive every OTHER budget `timeoutMs()` could have fallen back to
     * (`HostCommandTimeoutMs` 3s, `CaptureTimeoutMs` 5s), and over-budget must still miss.
     */
    @Test
    fun uiAutomatorDumpIsBudgetedByDumpTimeoutMsNotTheHostCommandBudget() = runBlocking {
        val outcomes = listOf(
            "underBudget" to outcomeOf(fakeAdbSleeping((MirrorTuning.DumpTimeoutMs - 2_000L) / 1_000.0)),
            "overBudget" to outcomeOf(fakeAdbSleeping((MirrorTuning.DumpTimeoutMs + 2_000L) / 1_000.0)),
        )

        assertEquals(listOf("underBudget" to "ok", "overBudget" to "timeout"), outcomes)
    }
}

/** Runs [DeviceCommand.UiAutomatorDump] against [adb] and reduces the result to one word. */
private suspend fun outcomeOf(adb: File): String {
    val executor = AdbDeviceCommandExecutor(
        ToolResolver(
            env = mapOf("ANDROID_HOME" to adb.parentFile.parentFile.absolutePath),
            homeDir = "",
            // Pinned to the fake: with the real predicate the next candidate is
            // /opt/homebrew/bin/adb, which would run the host's own daemon instead.
            isExecutable = { it == adb.absolutePath },
        )
    )
    val device = requireNotNull(DeviceId.of("emulator-5554"))
    return try {
        executor.run(device, DeviceCommand.UiAutomatorDump)
        "ok"
    } catch (failure: DeviceCommandException) {
        if (failure.isTimeout()) "timeout" else "failed ${failure.stderrTail}"
    }
}

/**
 * A fake `adb` at `<sdk>/platform-tools/adb` that answers `--version` and sleeps [sleepSeconds]
 * for every other subcommand - the layout `ANDROID_HOME`-based resolution expects.
 */
private fun fakeAdbSleeping(sleepSeconds: Double): File {
    val sdk = Files.createTempDirectory("rectify-fake-sdk").toFile()
    val adb = File(sdk, "platform-tools").apply { mkdirs() }.let { File(it, "adb") }
    adb.writeText(
        """
        |#!/bin/sh
        |case "${'$'}1" in
        |--version) echo 'Android Debug Bridge version 1.0.41' ;;
        |*) sleep $sleepSeconds ;;
        |esac
        """.trimMargin()
    )
    adb.setExecutable(true)
    listOf(adb, adb.parentFile, sdk).forEach(File::deleteOnExit)
    return adb
}

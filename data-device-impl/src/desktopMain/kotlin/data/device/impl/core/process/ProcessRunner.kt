package data.device.impl.core.process

import co.touchlab.kermit.Logger
import data.device.api.source.error.DeviceCommandException
import data.device.api.source.tuning.MirrorTuning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

private val log = Logger.withTag("ProcessRunner")

/**
 * The only place in this repository that starts an external process.
 *
 * Everything JVM-only about device tooling lives here: `commonMain` of this module names no
 * `java.*` type, so the Android and iOS compilations never see a `ProcessBuilder`.
 */
internal object ProcessRunner {

    /**
     * Every child that has been started and not yet reaped.
     *
     * The shutdown hook below walks it, so closing the window cannot leave an orphaned `adb`
     * holding the USB interface.
     */
    private val liveChildren = ConcurrentHashMap.newKeySet<Process>()

    init {
        Runtime.getRuntime().addShutdownHook(
            Thread {
                liveChildren.forEach(Process::destroyForcibly)
            }
        )
    }

    /**
     * Runs [argv] to completion and returns its stdout.
     *
     * @param timeoutMs Wall-clock budget for the whole invocation.
     * @return Raw stdout bytes.
     * @throws DeviceCommandException when the binary cannot be started, the budget is
     *   exhausted, or the child exits non-zero.
     */
    suspend fun run(argv: List<String>, timeoutMs: Long): ByteArray = coroutineScope {
        val process = try {
            ProcessBuilder(argv).start()
        } catch (io: IOException) {
            throw DeviceCommandException(argv, null, io.message.orEmpty())
        }
        liveChildren += process
        try {
            // Both pipes are drained concurrently. Reading them in sequence deadlocks as soon
            // as both fill at once - a multi-megabyte screencap on stdout plus a long adb
            // error on stderr is exactly that case.
            val stdout = async(Dispatchers.IO) { process.inputStream.drain() }
            val stderr = async(Dispatchers.IO) { process.errorStream.drain() }
            // withTimeoutOrNull, never withTimeout: a TimeoutCancellationException thrown here
            // IS a CancellationException, and `resultLauncher` up in domain-usecase-impl now
            // rethrows those rather than mapping them, so it would unwind the caller as if
            // someone had cancelled the job instead of surfacing a named timeout Failure.
            //
            // Both drains are awaited INSIDE the budget, not after it. A child can exit
            // cleanly while a grandchild it spawned still holds the write end of the pipe -
            // `adb` auto-starting its own server is the textbook case - and then the read
            // never reaches EOF. With the awaits outside, `waitFor` returned in milliseconds
            // and the call still parked forever on a pipe, so the `finally` below never ran:
            // a child that was never force-killed plus a permanent `liveChildren` leak.
            val completed = withTimeoutOrNull(timeoutMs) {
                val exitCode = runInterruptible(Dispatchers.IO) { process.waitFor() }
                Completion(exitCode, stdout.await(), stderr.await().tail())
            }
            if (completed == null) {
                log.d { "${argv.joinToString(" ")} exceeded ${timeoutMs}ms, killing it" }
                // Built from the shared constant, never retyped: DeviceCommandException.isTimeout()
                // matches on it down in the escalation ladder, and an inlined literal here would
                // let a reword disarm that ladder with the whole suite still green.
                throw DeviceCommandException(
                    argv,
                    null,
                    "${DeviceCommandException.TIMEOUT_REASON_PREFIX}${timeoutMs}ms",
                )
            }
            if (completed.exitCode != 0) throw DeviceCommandException(argv, completed.exitCode, completed.stderrTail)
            completed.stdout
        } finally {
            // Unconditional, and reached on every path out now that nothing is awaited past
            // the budget: timeout, non-zero exit, or collector cancellation. On the happy
            // path the child is already dead and this is a no-op. `destroyForcibly` also
            // closes the child's pipes, which is what unblocks a drain still parked on a
            // grandchild's write end so this scope can join it instead of hanging here.
            process.destroyForcibly()
            liveChildren -= process
        }
    }

    /**
     * Starts [argv] and returns immediately, without a timeout and without reaping it.
     *
     * Used for booting an emulator: the child outlives the call by design, and a non-zero exit
     * from an AVD lock file (the image is already running) is therefore a no-op success.
     *
     * The child is deliberately NOT added to [liveChildren] - an emulator the user booted is
     * an application with its own lifetime, and killing it when this window closes would throw
     * away their session.
     *
     * @throws DeviceCommandException when the binary cannot be started at all.
     */
    fun spawnDetached(argv: List<String>) {
        try {
            ProcessBuilder(argv)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
        } catch (io: IOException) {
            throw DeviceCommandException(argv, null, io.message.orEmpty())
        }
    }

    /** What a finished invocation produced, so the budget can cover all three at once. */
    private class Completion(val exitCode: Int, val stdout: ByteArray, val stderrTail: String)

    /**
     * Reads a pipe to EOF.
     *
     * Returns empty rather than throwing when the stream is closed underneath it: that is
     * exactly what [run]'s `finally` does to unblock this read, and letting the resulting
     * `IOException` out of an `async` would cancel the enclosing scope and mask the real
     * `DeviceCommandException` the caller needs to see.
     */
    private suspend fun InputStream.drain(): ByteArray =
        runInterruptible { runCatching { readBytes() }.getOrDefault(ByteArray(0)) }

    /** Last [MirrorTuning.StderrTailBytes] bytes, decoded and trimmed. */
    private fun ByteArray.tail(): String =
        copyOfRange(maxOf(0, size - MirrorTuning.StderrTailBytes), size).decodeToString().trim()
}

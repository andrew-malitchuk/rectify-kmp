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
            val stdout = async(Dispatchers.IO) { runInterruptible { process.inputStream.readBytes() } }
            val stderr = async(Dispatchers.IO) { runInterruptible { process.errorStream.readBytes() } }
            // withTimeoutOrNull, never withTimeout: `resultLauncher` up in domain-usecase-impl
            // catches Throwable, CancellationException included, so a
            // TimeoutCancellationException thrown here would be swallowed into a generic
            // failure AND would look like real cancellation to every coroutine in between.
            val exitCode = withTimeoutOrNull(timeoutMs) {
                runInterruptible(Dispatchers.IO) { process.waitFor() }
            }
            if (exitCode == null) {
                log.d { "${argv.joinToString(" ")} exceeded ${timeoutMs}ms, killing it" }
                throw DeviceCommandException(argv, null, "timed out after ${timeoutMs}ms")
            }
            val out = stdout.await()
            val err = stderr.await().tail()
            if (exitCode != 0) throw DeviceCommandException(argv, exitCode, err)
            out
        } finally {
            // Unconditional: on the happy path the child is already dead and this is a no-op,
            // on timeout or collector cancellation it is what releases the device. It also
            // unblocks the two drains above, which this scope is about to join.
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

    /** Last [MirrorTuning.StderrTailBytes] bytes, decoded and trimmed. */
    private fun ByteArray.tail(): String =
        copyOfRange(maxOf(0, size - MirrorTuning.StderrTailBytes), size).decodeToString().trim()
}

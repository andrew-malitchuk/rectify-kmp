package data.device.impl.core.tool

import data.device.api.source.error.DeviceCommandException
import data.device.api.source.model.ToolKind
import data.device.api.source.model.ToolProbe
import data.device.api.source.parse.HostCommand
import data.device.api.source.parse.buildHostArgv
import data.device.api.source.tool.probeCandidates
import data.device.api.source.tuning.MirrorTuning
import data.device.impl.core.process.ProcessRunner
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

/**
 * Locates the external tools on this host.
 *
 * The ordering lives in [probeCandidates] (pure, in `commonMain`, unit-tested with a fake
 * environment); this class only applies the executable predicate and reads the version.
 *
 * @param env Environment to probe with. A packaged `.app` inherits a minimal `PATH`, which is
 *   why the SDK locations are tried first and why this is a parameter.
 * @param homeDir User home directory, for the default macOS SDK location.
 * @param isExecutable Predicate deciding whether a candidate path is a runnable file.
 * @param userPath Path the user explicitly pointed at `adb` with the file dialog, or `null` /
 *   blank when they have not. Tried FIRST, and validated by [isExecutable] like every other
 *   candidate: it comes from outside the program, so a path that does not exist, is a
 *   directory, or is not executable falls through to the ordered list instead of being
 *   trusted. One predicate, one place - two would drift apart.
 */
internal class ToolResolver(
    private val env: Map<String, String> = System.getenv(),
    private val homeDir: String = System.getProperty("user.home").orEmpty(),
    private val isExecutable: (String) -> Boolean = ::isExecutableFile,
    private val userPath: suspend () -> String? = { null },
) {

    private val mutex = Mutex()

    /**
     * Successful probes only.
     *
     * A found tool cannot move while the app runs, so re-spawning `adb --version` on every
     * 2s poll buys nothing; a MISSING tool is re-probed every time, so installing `adb` with
     * the app open is picked up on the next poll. A user-chosen path is the one thing that
     * CAN move, so [resolve] does not read or write this map while one is set.
     */
    private val resolved = mutableMapOf<ToolKind, ToolProbe>()

    /**
     * Probes for [kind].
     *
     * @return A probe that is never an error: a missing binary is reported as
     *   `found = false` with every candidate path that was tried.
     */
    suspend fun resolve(kind: ToolKind): ToolProbe {
        // Only `adb` is user-overridable; nothing is persisted for the other two tools.
        val overrides = if (kind == ToolKind.Adb) {
            listOfNotNull(userPath()?.takeIf(String::isNotBlank))
        } else {
            emptyList()
        }

        // The cache is bypassed entirely while an override is set. Unlike an SDK location, the
        // override can change while the app runs - the user can pick a different file, or
        // delete the one they picked - so neither a stale "good" nor a stale fallback may
        // survive. ponytail: costs one extra probe per poll, paid only by the user who opted in.
        if (overrides.isEmpty()) mutex.withLock { resolved[kind] }?.let { return it }

        val candidates = (overrides + probeCandidates(kind, env, homeDir)).distinct()
        val path = candidates.firstOrNull(isExecutable)
            ?: return ToolProbe(
                kind = kind,
                found = false,
                probedPaths = candidates,
                installCommand = kind.installCommand(),
                envHint = kind.envHint(),
            )
        val probe = ToolProbe(
            kind = kind,
            found = true,
            path = path,
            version = readVersion(kind, path),
            probedPaths = candidates,
            installCommand = kind.installCommand(),
            envHint = kind.envHint(),
        )
        if (overrides.isEmpty()) mutex.withLock { resolved[kind] = probe }
        return probe
    }

    /**
     * Path of [kind]'s executable.
     *
     * @throws DeviceCommandException when the tool is not installed - unlike [resolve], a
     *   caller asking for the path is about to run something, and that cannot degrade to a
     *   value.
     */
    suspend fun requirePath(kind: ToolKind): String = resolve(kind).path
        ?: throw DeviceCommandException(listOf(kind.binaryName), null, "${kind.binaryName} not found")

    /** First non-blank line of the tool's version output, or `null` when it will not say. */
    private suspend fun readVersion(kind: ToolKind, path: String): String? {
        val command = when (kind) {
            ToolKind.Adb -> HostCommand.AdbVersion
            ToolKind.Emulator -> HostCommand.EmulatorVersion
            // No HostCommand member exists for idb, and the closed set is not widened for a
            // version string: the probe still reports the binary as found, without a version.
            ToolKind.Idb -> return null
        }
        return try {
            ProcessRunner.run(buildHostArgv(path, command), MirrorTuning.HostCommandTimeoutMs)
                .decodeToString()
                .lineSequence()
                .map(String::trim)
                .firstOrNull(String::isNotEmpty)
        } catch (failure: DeviceCommandException) {
            // An executable that will not report its version is still an executable.
            null
        }
    }
}

/**
 * THE executable predicate: a path is usable only when it is a file that can be executed.
 *
 * Top-level and named so the user-path trust boundary and the ordered candidates share one
 * implementation, and so a test can sandbox the real check instead of restating it.
 */
internal fun isExecutableFile(path: String): Boolean = File(path).let { it.isFile && it.canExecute() }

/** Exact shell line that installs the tool, rendered verbatim by the UI. */
private fun ToolKind.installCommand(): String = when (this) {
    ToolKind.Adb -> "brew install --cask android-platform-tools"
    ToolKind.Emulator -> "brew install --cask android-commandlinetools"
    ToolKind.Idb -> "brew tap facebook/fb && brew install idb-companion"
}

/** Environment hint shown next to the install command. */
private fun ToolKind.envHint(): String = when (this) {
    ToolKind.Adb, ToolKind.Emulator -> "Or set ANDROID_HOME to your Android SDK directory"
    ToolKind.Idb -> ""
}

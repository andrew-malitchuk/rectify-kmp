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
    private val identifyAdb: suspend (String) -> Boolean = ::adbAnswersItsOwnBanner,
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
        // An override must also IDENTIFY itself. `isExecutable` answers "can this be run",
        // which is not the same question as "is this adb" — and the gap was not theoretical:
        // the path is persisted, so it is re-armed every launch; the cache is bypassed while an
        // override is set, so it is re-executed twice per 2 s poll indefinitely; and
        // readVersion swallows failures, so a non-adb binary that exits 0 and prints one
        // plausible line was indistinguishable from adb in the UI. One mis-click on a
        // downloaded shell script became code Rectify re-ran every two seconds, every session,
        // silently.
        //
        // Ordered candidates are NOT banner-checked: they come from the SDK layout and the
        // user's PATH, which this program does not get to second-guess. Only the value that
        // arrived from a file dialog has to prove what it is.
        //
        // The override is still re-identified on every poll rather than cached. Caching it was
        // tried and withdrawn: keyed on the path alone it served a stale probe after the file
        // was deleted, which is exactly the bug the deliberate cache bypass above exists to
        // prevent and which `aUserPathThatStopsWorkingIsNotServedFromTheCache` already guards.
        // ponytail: one extra `--version` per poll for the user who opted in; revisit with a
        // filesystem-stamp key if it ever shows up in a profile.
        val path = candidates.firstOrNull { candidate ->
            isExecutable(candidate) && (candidate !in overrides || identifiesAsAdb(candidate))
        }
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

    /**
     * Whether [path] answers `--version` the way `adb` does.
     *
     * Deliberately a prefix match on adb's own banner — `Android Debug Bridge version 1.0.41`
     * — and not a version-number parse: this is an identity check, not a compatibility check.
     * Rejecting a real adb for being too new would be worse than the problem it solves.
     *
     * A binary that cannot be run, times out, or says something else is not adb. Note
     * [readVersion] deliberately tolerates a silent executable; this does not, and the
     * asymmetry is the point — tolerance is right for a path the SDK layout produced and wrong
     * for one a file dialog did.
     */
    private suspend fun identifiesAsAdb(path: String): Boolean = identifyAdb(path)

    /** First non-blank line of the tool's version output, or `null` when it will not say. */
    private suspend fun readVersion(kind: ToolKind, path: String): String? {
        val command = when (kind) {
            ToolKind.Adb -> HostCommand.AdbVersion
            ToolKind.Emulator -> HostCommand.EmulatorVersion
            // No HostCommand member exists for idb, and the closed set is not widened for a
            // version string: the probe still reports the binary as found, without a version.
            ToolKind.Idb -> return null
            // `simctl` has no version of its own; the Xcode shim's is the one on offer.
            ToolKind.Simctl -> HostCommand.XcrunVersion
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
/** `adb --version`'s own first line starts with this. */
private const val ADB_BANNER_PREFIX = "Android Debug Bridge"

/**
 * THE adb identity check: does running `<path> --version` produce adb's own banner?
 *
 * A seam with a real default, exactly like `isExecutable` — and for the same reason, plus one
 * more. `isExecutable` is injected so a test can sandbox the predicate; this must be injected
 * because it SPAWNS, and spawning inside `runTest`'s virtual clock cannot work: the scheduler
 * advances past `withTimeoutOrNull` instantly while the real child is still starting, so the
 * call always times out. That is not hypothetical — `readVersion` has silently returned `null`
 * in every `ToolResolverTest` since the module was written, which nobody noticed because no
 * test asserted a version.
 *
 * Prefix match on the banner, not a version parse: this answers "is this adb", not "is this an
 * adb I like". Rejecting a real adb for being too new would be worse than the problem it
 * solves.
 */
private suspend fun adbAnswersItsOwnBanner(path: String): Boolean = try {
    ProcessRunner.run(buildHostArgv(path, HostCommand.AdbVersion), MirrorTuning.HostCommandTimeoutMs)
        .decodeToString()
        .lineSequence()
        .map(String::trim)
        .firstOrNull(String::isNotEmpty)
        ?.startsWith(ADB_BANNER_PREFIX) == true
} catch (failure: DeviceCommandException) {
    // Cannot be run, timed out, or said nothing: not adb.
    false
}

internal fun isExecutableFile(path: String): Boolean = File(path).let { it.isFile && it.canExecute() }

/** Exact shell line that installs the tool, rendered verbatim by the UI. */
private fun ToolKind.installCommand(): String = when (this) {
    ToolKind.Adb -> "brew install --cask android-platform-tools"
    ToolKind.Emulator -> "brew install --cask android-commandlinetools"
    ToolKind.Idb -> "brew tap facebook/fb && brew install idb-companion"
    ToolKind.Simctl -> "xcode-select --install"
}

/** Environment hint shown next to the install command. */
private fun ToolKind.envHint(): String = when (this) {
    ToolKind.Adb, ToolKind.Emulator -> "Or set ANDROID_HOME to your Android SDK directory"
    ToolKind.Idb, ToolKind.Simctl -> ""
}

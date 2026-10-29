package data.device.api.source.tool

import data.device.api.source.model.ToolKind

/**
 * Ordered list of paths to try when locating [tool], most specific first.
 *
 * Pure, so the packaged-`.app` case (a GUI process inherits a minimal `PATH`) is testable
 * with a fake environment and no filesystem. The caller applies the executable predicate;
 * every path returned here ends up in `ToolProbe.probedPaths` for the UI footer.
 *
 * Order: `ANDROID_HOME`, `ANDROID_SDK_ROOT`, the macOS default SDK location, the two
 * Homebrew prefixes, then each `PATH` entry. Duplicates are removed, first occurrence wins.
 *
 * @param env Environment variables; an absent or blank entry contributes nothing.
 * @param homeDir User home directory, used for the default SDK location.
 */
public fun probeCandidates(tool: ToolKind, env: Map<String, String>, homeDir: String): List<String> {
    val binary = tool.binaryName
    val candidates = mutableListOf<String>()
    tool.sdkSubdirectory?.let { subdirectory ->
        listOfNotNull(
            env["ANDROID_HOME"]?.takeIf { it.isNotBlank() },
            env["ANDROID_SDK_ROOT"]?.takeIf { it.isNotBlank() },
            homeDir.takeIf { it.isNotBlank() }?.let { "$it/Library/Android/sdk" },
        ).forEach { sdkRoot -> candidates += "${sdkRoot.trimEnd('/')}/$subdirectory/$binary" }
    }
    // Xcode's shim is a system binary, not a Homebrew one: on a machine with Xcode installed
    // `/usr/bin/xcrun` always exists, and it is the ONLY location that is always right. Gated
    // on the tool rather than added for everything so the Android probe order is untouched.
    if (tool == ToolKind.Simctl) candidates += "/usr/bin/$binary"
    candidates += "/opt/homebrew/bin/$binary"
    candidates += "/usr/local/bin/$binary"
    env["PATH"].orEmpty()
        .split(':')
        .filter { it.isNotBlank() }
        .forEach { entry -> candidates += "${entry.trimEnd('/')}/$binary" }
    return candidates.distinct()
}

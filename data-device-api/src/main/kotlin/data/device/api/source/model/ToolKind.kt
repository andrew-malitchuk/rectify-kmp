package data.device.api.source.model

/**
 * An external platform tool this app shells out to.
 *
 * @property binaryName Executable file name.
 * @property sdkSubdirectory Directory inside an Android SDK root that holds the binary,
 *   or `null` for a tool that does not ship in the SDK.
 */
public enum class ToolKind(
    public val binaryName: String,
    public val sdkSubdirectory: String?,
) {
    /** Android Debug Bridge, `<sdk>/platform-tools/adb`. */
    Adb("adb", "platform-tools"),

    /** Android emulator, `<sdk>/emulator/emulator` — NOT platform-tools. */
    Emulator("emulator", "emulator"),

    /** iOS device bridge; installed through Homebrew only. */
    Idb("idb", null),

    /**
     * iOS simulator control, reached through Xcode's `xcrun` shim — widened for F-10 (FR-49).
     *
     * The binary is `xcrun`, not `simctl`: `simctl` is a SUBCOMMAND that
     * [data.device.api.source.parse.buildDeviceArgv] inserts, and there is no standalone
     * `simctl` on `PATH` to probe for. Ships with the Xcode command-line tools at
     * `/usr/bin/xcrun`, so it has no SDK subdirectory.
     */
    Simctl("xcrun", null),
}

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
}

package data.device.api.source.parse

import data.device.api.source.error.DeviceCommandException
import data.device.api.source.model.AvdName
import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId
import data.device.api.source.model.ToolKind

/**
 * The deviceless commands this app may run.
 *
 * Closed for the same reason [DeviceCommand] is: no `connect`, no `-H`, no `-P`.
 */
public sealed interface HostCommand {

    /** `adb --version`. */
    public data object AdbVersion : HostCommand

    /** `adb devices -l`. */
    public data object ListDevices : HostCommand

    /**
     * `adb start-server`.
     *
     * Forks the daemon ahead of a discovery poll. A cold fork takes ~3.03 s on real hardware —
     * longer than `HostCommandTimeoutMs` — so a poll that triggers the fork implicitly kills it
     * mid-start and the next poll re-enters the same cold state. Starting the daemon explicitly
     * is what breaks that livelock.
     */
    public data object StartServer : HostCommand

    /**
     * `adb kill-server`.
     *
     * Against a *wedged* daemon (alive but not serving) `start-server` itself hangs, so
     * [KillServer] then [StartServer] is the only sequence that recovers it.
     */
    public data object KillServer : HostCommand

    /** `emulator -version`. */
    public data object EmulatorVersion : HostCommand

    /** `emulator -list-avds`. */
    public data object ListAvds : HostCommand

    /**
     * `emulator -avd <name>`.
     *
     * @property avd Image to boot.
     */
    public data class BootAvd(public val avd: AvdName) : HostCommand

    /**
     * `xcrun --version` — widened for F-10 (FR-49).
     *
     * The Xcode shim's own version, which is the only version string the iOS half can report:
     * `simctl` has no `--version` of its own. There is deliberately no `IdbVersion` member —
     * nothing renders an idb version, and this set is not widened for an unread string.
     */
    public data object XcrunVersion : HostCommand

    /**
     * `xcrun simctl list devices available` — widened for F-10 (FR-49).
     *
     * `available` filters out runtimes that are installed but unusable, so the parser never
     * has to tell a usable simulator from a dead one.
     */
    public data object ListSimulators : HostCommand

    /** `idb list-targets` — widened for F-10 (FR-49). Physical iOS devices plus simulators. */
    public data object ListTargets : HostCommand
}

/** `adb`'s device selector flag. */
private const val ADB_SELECTOR = "-s"

/** `idb`'s device selector flag. */
private const val IDB_SELECTOR = "--udid"

/** The `xcrun` subcommand that turns the Xcode shim into the simulator CLI. */
private const val SIMCTL = "simctl"

/**
 * Builds the argument vector for a device-scoped command.
 *
 * The single construction point for all three device tools, so no call site can target the
 * wrong device or omit the selector. Each tool spells the selector its own way and puts it in
 * its own place — `adb` takes `-s <serial>` as a PREFIX, `simctl` takes the udid POSITIONALLY
 * right after its subcommand (it has no selector flag at all), `idb` takes `--udid <udid>` as
 * a SUFFIX — so the shape is per-tool while the guarantee is not.
 *
 * **NFR-5 is checked on the SELECTOR SEGMENT, never on the whole argv.** The property that
 * matters is "exactly one device is addressed", and only the selector addresses a device.
 * Counting the whole vector looks stricter and is in fact a defect: [DeviceCommand.GetProp]
 * carries unvalidated free text, so `GetProp(name = <udid>)` would make a pure function throw
 * [IllegalStateException] for a perfectly legal call.
 *
 * @param tool Which tool will run this — decides the selector's spelling and position.
 * @param toolPath Absolute path of the executable.
 * @throws DeviceCommandException when [command] has no spelling on [tool], e.g. an
 *   `adb shell`-shaped Android command handed to `simctl`.
 * @throws IllegalStateException when the selector segment does not name [device] exactly
 *   once — a programming error in this function, not a runtime condition.
 */
public fun buildDeviceArgv(
    tool: ToolKind,
    toolPath: String,
    device: DeviceId,
    command: DeviceCommand,
): List<String> {
    val payload = command.arguments(tool)
    return when (tool) {
        // `adb -s <serial> <command…>`. Emulator never reaches here: arguments() already threw.
        ToolKind.Adb, ToolKind.Emulator ->
            listOf(toolPath) + device.selector(listOf(ADB_SELECTOR, device.value)) + payload

        // `xcrun simctl <subcommand> <udid> <rest…>`.
        ToolKind.Simctl ->
            listOf(toolPath, SIMCTL, payload.first()) +
                device.selector(listOf(device.value)) +
                payload.drop(1)

        // `idb <command…> --udid <udid>`.
        ToolKind.Idb ->
            listOf(toolPath) + payload + device.selector(listOf(IDB_SELECTOR, device.value))
    }
}

/**
 * Builds the argument vector for a deviceless command.
 *
 * Never emits `-s`: a host command that needs a device is a [DeviceCommand] instead.
 *
 * @param toolPath Absolute path of the `adb` or `emulator` executable.
 */
public fun buildHostArgv(toolPath: String, command: HostCommand): List<String> = listOf(toolPath) + when (command) {
    HostCommand.AdbVersion -> listOf("--version")
    HostCommand.ListDevices -> listOf("devices", "-l")
    HostCommand.StartServer -> listOf("start-server")
    HostCommand.KillServer -> listOf("kill-server")
    HostCommand.EmulatorVersion -> listOf("-version")
    HostCommand.ListAvds -> listOf("-list-avds")
    is HostCommand.BootAvd -> listOf("-avd", command.avd.value)
    HostCommand.XcrunVersion -> listOf("--version")
    HostCommand.ListSimulators -> listOf(SIMCTL, "list", "devices", "available")
    HostCommand.ListTargets -> listOf("list-targets")
}

/**
 * NFR-5, scoped to one selector segment.
 *
 * Named and shared by all three branches so the guarantee cannot drift tool by tool: a branch
 * that forgets to call it does not compile into an argv at all.
 */
private fun DeviceId.selector(segment: List<String>): List<String> {
    check(segment.count { it == value } == 1) { "selector must name the device exactly once" }
    return segment
}

/** No spelling on this tool — a programming error at the call site, reported like any other. */
private fun DeviceCommand.unsupportedOn(tool: ToolKind): Nothing =
    throw DeviceCommandException(listOf(tool.binaryName), null, "$this is not supported by ${tool.binaryName}")

private fun DeviceCommand.arguments(tool: ToolKind): List<String> = when (tool) {
    ToolKind.Adb -> adbArguments()
    ToolKind.Simctl -> simctlArguments()
    ToolKind.Idb -> idbArguments()
    // The emulator binary boots images; it is never a device-command target.
    ToolKind.Emulator -> unsupportedOn(tool)
}

private fun DeviceCommand.adbArguments(): List<String> = when (this) {
    DeviceCommand.Screencap -> listOf("exec-out", "screencap", "-p")
    DeviceCommand.WmSize -> listOf("shell", "wm", "size")
    DeviceCommand.WmDensity -> listOf("shell", "wm", "density")
    is DeviceCommand.GetProp -> listOf("shell", "getprop", name)
    DeviceCommand.EmuAvdName -> listOf("emu", "avd", "name")
    DeviceCommand.UiAutomatorDump -> listOf("exec-out", "uiautomator", "dump", "/dev/tty")
    DeviceCommand.SimulatorBoot -> unsupportedOn(ToolKind.Adb)
}

/**
 * The subcommand comes FIRST in every list here — [buildDeviceArgv] splits it off and puts the
 * udid behind it, which is the only place `simctl` accepts a device.
 */
private fun DeviceCommand.simctlArguments(): List<String> = when (this) {
    DeviceCommand.Screencap -> listOf("io", "screenshot", "--type=png", "-")
    DeviceCommand.SimulatorBoot -> listOf("boot")
    // Android-only, and listed one by one rather than behind an `else` so widening
    // DeviceCommand stays the visible API change this file's KDoc promises.
    DeviceCommand.WmSize,
    DeviceCommand.WmDensity,
    is DeviceCommand.GetProp,
    DeviceCommand.EmuAvdName,
    DeviceCommand.UiAutomatorDump,
    -> unsupportedOn(ToolKind.Simctl)
}

private fun DeviceCommand.idbArguments(): List<String> = when (this) {
    // `-` is idb's stdout sink, matching the simctl and adb spellings above. UNMEASURED: no
    // trusted physical device was available on the capture host, so this one line is the only
    // argv in this file that fixtures do not back.
    DeviceCommand.Screencap -> listOf("screenshot", "-")
    // A physical handset cannot be booted, and simulators go through simctl.
    DeviceCommand.SimulatorBoot,
    DeviceCommand.WmSize,
    DeviceCommand.WmDensity,
    is DeviceCommand.GetProp,
    DeviceCommand.EmuAvdName,
    DeviceCommand.UiAutomatorDump,
    -> unsupportedOn(ToolKind.Idb)
}

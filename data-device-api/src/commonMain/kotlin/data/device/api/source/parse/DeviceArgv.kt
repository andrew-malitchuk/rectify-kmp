package data.device.api.source.parse

import data.device.api.source.model.AvdName
import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId

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
}

/**
 * Builds the argument vector for a device-scoped command.
 *
 * The result always starts `[toolPath, "-s", device.value]` — this is the single
 * construction point, so no call site can target the wrong device or omit the selector.
 *
 * @param toolPath Absolute path of the `adb` executable.
 */
public fun buildDeviceArgv(toolPath: String, device: DeviceId, command: DeviceCommand): List<String> =
    listOf(toolPath, "-s", device.value) + command.arguments()

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
}

private fun DeviceCommand.arguments(): List<String> = when (this) {
    DeviceCommand.Screencap -> listOf("exec-out", "screencap", "-p")
    DeviceCommand.WmSize -> listOf("shell", "wm", "size")
    DeviceCommand.WmDensity -> listOf("shell", "wm", "density")
    is DeviceCommand.GetProp -> listOf("shell", "getprop", name)
    DeviceCommand.EmuAvdName -> listOf("emu", "avd", "name")
    DeviceCommand.UiAutomatorDump -> listOf("exec-out", "uiautomator", "dump", "/dev/tty")
}

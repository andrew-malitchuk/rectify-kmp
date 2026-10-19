package data.device.api

import data.device.api.source.error.DeviceCommandException
import data.device.api.source.model.AvdName
import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId
import data.device.api.source.model.ToolKind
import data.device.api.source.parse.HostCommand
import data.device.api.source.parse.buildDeviceArgv
import data.device.api.source.parse.buildHostArgv
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The AC-44 test: every device-scoped argv is addressed to a resolved device, and no
 * deviceless argv carries a device selector.
 */
class DeviceArgvTest {

    private val adb = "/usr/local/bin/adb"
    private val device = requireNotNull(DeviceId.of("emulator-5554"))

    private val xcrun = "/usr/bin/xcrun"
    private val idb = "/opt/homebrew/bin/idb"
    private val simulator = requireNotNull(DeviceId.of("5F20FFD2-3B90-42DA-9227-26725B78EA58"))

    private val everyDeviceCommand = listOf(
        DeviceCommand.Screencap,
        DeviceCommand.WmSize,
        DeviceCommand.WmDensity,
        DeviceCommand.GetProp("ro.product.model"),
        DeviceCommand.EmuAvdName,
        DeviceCommand.UiAutomatorDump,
        DeviceCommand.SimulatorBoot,
    )

    /** Every device command adb actually speaks - the sweep above minus the iOS-only one. */
    private val everyAdbCommand = everyDeviceCommand - DeviceCommand.SimulatorBoot

    private val everyHostCommand = listOf(
        HostCommand.AdbVersion,
        HostCommand.ListDevices,
        HostCommand.StartServer,
        HostCommand.KillServer,
        HostCommand.EmulatorVersion,
        HostCommand.ListAvds,
        HostCommand.BootAvd(requireNotNull(AvdName.of("Pixel_7_API_34"))),
        HostCommand.XcrunVersion,
        HostCommand.ListSimulators,
        HostCommand.ListTargets,
    )

    @Test
    fun `buildDeviceArgv targets the device for every command variant`() {
        everyAdbCommand.forEach { command ->
            val argv = buildDeviceArgv(ToolKind.Adb, adb, device, command)
            assertEquals(adb, argv[0])
            assertEquals("-s", argv[1])
            assertEquals(device.value, argv[2])
            assertTrue(argv.size > 3)
        }
    }

    @Test
    fun `buildDeviceArgv names the selector exactly once`() {
        everyAdbCommand.forEach { command ->
            assertEquals(1, buildDeviceArgv(ToolKind.Adb, adb, device, command).count { it == "-s" })
        }
    }

    /**
     * The exhaustive ToolKind x DeviceCommand sweep: every pair either renders an argv that
     * names its device exactly once in the selector, or refuses with a DeviceCommandException.
     * No pair may throw anything else - and in particular no pair may throw IllegalStateException,
     * which is what a whole-argv NFR-5 count would do.
     */
    @Test
    fun `every tool and command pair either renders or refuses`() {
        val outcomes = ToolKind.entries.flatMap { tool ->
            everyDeviceCommand.map { command ->
                val target = if (tool == ToolKind.Adb || tool == ToolKind.Emulator) device else simulator
                runCatching { buildDeviceArgv(tool, "/tool", target, command) }
                    .fold(
                        onSuccess = { argv -> argv.count { it == target.value } == 1 && argv[0] == "/tool" },
                        onFailure = { failure -> failure is DeviceCommandException },
                    )
            }
        }

        assertEquals(List(ToolKind.entries.size * everyDeviceCommand.size) { true }, outcomes)
    }

    /**
     * NFR-5 is scoped to the SELECTOR SEGMENT, never to the whole argv.
     *
     * [DeviceCommand.GetProp] carries unvalidated free text, so a whole-argv count turns a
     * legal call into an IllegalStateException thrown out of a pure function the moment the
     * property name happens to equal the serial. This is the must-NOT-throw case: the payload
     * repeats the serial, the selector still names it once, and the call succeeds.
     *
     * MUTATION: change the check in buildDeviceArgv to count over the whole argv - this fails.
     */
    @Test
    fun `a payload that repeats the serial is not a selector violation`() {
        val argv = buildDeviceArgv(ToolKind.Adb, adb, device, DeviceCommand.GetProp(device.value))

        assertEquals(listOf(adb, "-s", device.value, "shell", "getprop", device.value), argv)
        assertEquals(2, argv.count { it == device.value })
    }

    /** simctl has no selector flag: the udid is positional, straight after the subcommand. */
    @Test
    fun `simctl places the udid after its subcommand`() {
        assertEquals(
            listOf(xcrun, "simctl", "io", simulator.value, "screenshot", "--type=png", "-"),
            buildDeviceArgv(ToolKind.Simctl, xcrun, simulator, DeviceCommand.Screencap),
        )
        assertEquals(
            listOf(xcrun, "simctl", "boot", simulator.value),
            buildDeviceArgv(ToolKind.Simctl, xcrun, simulator, DeviceCommand.SimulatorBoot),
        )
    }

    /** idb takes --udid as a suffix, after the command payload. */
    @Test
    fun `idb appends the udid selector`() {
        assertEquals(
            listOf(idb, "screenshot", "-", "--udid", simulator.value),
            buildDeviceArgv(ToolKind.Idb, idb, simulator, DeviceCommand.Screencap),
        )
    }

    /** An Android-only command on an iOS tool is refused, not silently mis-rendered. */
    @Test
    fun `an android only command on an ios tool is refused`() {
        assertFailsWith<DeviceCommandException> {
            buildDeviceArgv(ToolKind.Simctl, xcrun, simulator, DeviceCommand.UiAutomatorDump)
        }
        assertFailsWith<DeviceCommandException> {
            buildDeviceArgv(ToolKind.Idb, idb, simulator, DeviceCommand.WmSize)
        }
        assertFailsWith<DeviceCommandException> {
            buildDeviceArgv(ToolKind.Adb, adb, device, DeviceCommand.SimulatorBoot)
        }
    }

    @Test
    fun `buildDeviceArgv renders each command variant`() {
        assertEquals(
            listOf(adb, "-s", device.value, "exec-out", "screencap", "-p"),
            buildDeviceArgv(ToolKind.Adb, adb, device, DeviceCommand.Screencap),
        )
        assertEquals(
            listOf(adb, "-s", device.value, "shell", "wm", "size"),
            buildDeviceArgv(ToolKind.Adb, adb, device, DeviceCommand.WmSize),
        )
        assertEquals(
            listOf(adb, "-s", device.value, "shell", "wm", "density"),
            buildDeviceArgv(ToolKind.Adb, adb, device, DeviceCommand.WmDensity),
        )
        assertEquals(
            listOf(adb, "-s", device.value, "shell", "getprop", "ro.build.version.sdk"),
            buildDeviceArgv(ToolKind.Adb, adb, device, DeviceCommand.GetProp("ro.build.version.sdk")),
        )
        assertEquals(
            listOf(adb, "-s", device.value, "emu", "avd", "name"),
            buildDeviceArgv(ToolKind.Adb, adb, device, DeviceCommand.EmuAvdName),
        )
        assertEquals(
            listOf(adb, "-s", device.value, "exec-out", "uiautomator", "dump", "/dev/tty"),
            buildDeviceArgv(ToolKind.Adb, adb, device, DeviceCommand.UiAutomatorDump),
        )
    }

    @Test
    fun `buildHostArgv never emits a device selector`() {
        everyHostCommand.forEach { command ->
            val argv = buildHostArgv(adb, command)
            assertFalse(argv.contains("-s"))
            assertEquals(adb, argv[0])
        }
    }

    @Test
    fun `buildHostArgv renders each host variant`() {
        assertEquals(listOf(adb, "--version"), buildHostArgv(adb, HostCommand.AdbVersion))
        assertEquals(listOf(xcrun, "--version"), buildHostArgv(xcrun, HostCommand.XcrunVersion))
        assertEquals(
            listOf(xcrun, "simctl", "list", "devices", "available"),
            buildHostArgv(xcrun, HostCommand.ListSimulators),
        )
        assertEquals(listOf(idb, "list-targets"), buildHostArgv(idb, HostCommand.ListTargets))
        assertEquals(listOf(adb, "devices", "-l"), buildHostArgv(adb, HostCommand.ListDevices))
        assertEquals(listOf(adb, "start-server"), buildHostArgv(adb, HostCommand.StartServer))
        assertEquals(listOf(adb, "kill-server"), buildHostArgv(adb, HostCommand.KillServer))
        assertEquals(listOf("emu", "-version"), buildHostArgv("emu", HostCommand.EmulatorVersion))
        assertEquals(listOf("emu", "-list-avds"), buildHostArgv("emu", HostCommand.ListAvds))
        assertEquals(
            listOf("emu", "-avd", "Pixel_7_API_34"),
            buildHostArgv("emu", HostCommand.BootAvd(requireNotNull(AvdName.of("Pixel_7_API_34")))),
        )
    }

    @Test
    fun `buildHostArgv appends nothing beyond the daemon subcommand`() {
        assertEquals(2, buildHostArgv(adb, HostCommand.StartServer).size)
        assertEquals(2, buildHostArgv(adb, HostCommand.KillServer).size)
    }
}

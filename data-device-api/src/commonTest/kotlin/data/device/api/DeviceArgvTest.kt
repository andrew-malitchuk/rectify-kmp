package data.device.api

import data.device.api.source.model.AvdName
import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId
import data.device.api.source.parse.HostCommand
import data.device.api.source.parse.buildDeviceArgv
import data.device.api.source.parse.buildHostArgv
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The AC-44 test: every device-scoped argv is addressed to a resolved device, and no
 * deviceless argv carries a device selector.
 */
class DeviceArgvTest {

    private val adb = "/usr/local/bin/adb"
    private val device = requireNotNull(DeviceId.of("emulator-5554"))

    private val everyDeviceCommand = listOf(
        DeviceCommand.Screencap,
        DeviceCommand.WmSize,
        DeviceCommand.WmDensity,
        DeviceCommand.GetProp("ro.product.model"),
        DeviceCommand.EmuAvdName,
    )

    private val everyHostCommand = listOf(
        HostCommand.AdbVersion,
        HostCommand.ListDevices,
        HostCommand.EmulatorVersion,
        HostCommand.ListAvds,
        HostCommand.BootAvd(requireNotNull(AvdName.of("Pixel_7_API_34"))),
    )

    @Test
    fun `buildDeviceArgv targets the device for every command variant`() {
        everyDeviceCommand.forEach { command ->
            val argv = buildDeviceArgv(adb, device, command)
            assertEquals(adb, argv[0])
            assertEquals("-s", argv[1])
            assertEquals(device.value, argv[2])
            assertTrue(argv.size > 3)
        }
    }

    @Test
    fun `buildDeviceArgv names the selector exactly once`() {
        everyDeviceCommand.forEach { command ->
            assertEquals(1, buildDeviceArgv(adb, device, command).count { it == "-s" })
        }
    }

    @Test
    fun `buildDeviceArgv renders each command variant`() {
        assertEquals(
            listOf(adb, "-s", device.value, "exec-out", "screencap", "-p"),
            buildDeviceArgv(adb, device, DeviceCommand.Screencap),
        )
        assertEquals(
            listOf(adb, "-s", device.value, "shell", "wm", "size"),
            buildDeviceArgv(adb, device, DeviceCommand.WmSize),
        )
        assertEquals(
            listOf(adb, "-s", device.value, "shell", "wm", "density"),
            buildDeviceArgv(adb, device, DeviceCommand.WmDensity),
        )
        assertEquals(
            listOf(adb, "-s", device.value, "shell", "getprop", "ro.build.version.sdk"),
            buildDeviceArgv(adb, device, DeviceCommand.GetProp("ro.build.version.sdk")),
        )
        assertEquals(
            listOf(adb, "-s", device.value, "emu", "avd", "name"),
            buildDeviceArgv(adb, device, DeviceCommand.EmuAvdName),
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
        assertEquals(listOf(adb, "devices", "-l"), buildHostArgv(adb, HostCommand.ListDevices))
        assertEquals(listOf("emu", "-version"), buildHostArgv("emu", HostCommand.EmulatorVersion))
        assertEquals(listOf("emu", "-list-avds"), buildHostArgv("emu", HostCommand.ListAvds))
        assertEquals(
            listOf("emu", "-avd", "Pixel_7_API_34"),
            buildHostArgv("emu", HostCommand.BootAvd(requireNotNull(AvdName.of("Pixel_7_API_34")))),
        )
    }
}

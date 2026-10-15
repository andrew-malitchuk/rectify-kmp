package data.device.api

import data.device.api.source.model.DeviceStateResource
import data.device.api.source.parse.parseAdbDevices
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pins the `adb devices -l` contract, including the rows the proof of concept dropped:
 * an attached but unusable device must still reach the picker.
 */
class AdbDevicesParserTest {

    private val output = """
        * daemon not running; starting now at tcp:5037
        List of devices attached
        R5CT30ABCDE            device usb:338690048X product:a52qnaxx model:SM_A525F device:a52q transport_id:1
        emulator-5554          device product:sdk_gphone64_arm64 model:sdk_gphone64_arm64 device:emu64a transport_id:2
        2A1B3C4D               unauthorized usb:336592896X transport_id:3
        192.168.1.44:5555      offline transport_id:4
        9876543210             no permissions; see [http://developer.android.com/tools/device.html]

    """.trimIndent()

    @Test
    fun `parseAdbDevices drops the header and the daemon chatter`() {
        assertEquals(5, parseAdbDevices(output).size)
        assertTrue(parseAdbDevices("List of devices attached\n\n").isEmpty())
        assertTrue(parseAdbDevices("").isEmpty())
    }

    @Test
    fun `parseAdbDevices reads the serial and the tags of a physical device`() {
        val device = parseAdbDevices(output).first()
        assertEquals("R5CT30ABCDE", device.id.value)
        assertEquals(DeviceStateResource.Device, device.state)
        assertEquals("SM_A525F", device.model)
        assertEquals("a52qnaxx", device.product)
        assertEquals("1", device.transport)
        assertEquals(false, device.isEmulator)
    }

    @Test
    fun `parseAdbDevices flags an emulator serial`() {
        val emulator = parseAdbDevices(output)[1]
        assertEquals("emulator-5554", emulator.id.value)
        assertTrue(emulator.isEmulator)
    }

    @Test
    fun `parseAdbDevices keeps the unusable rows instead of dropping them`() {
        val states = parseAdbDevices(output).map { it.state }
        assertEquals(
            listOf(
                DeviceStateResource.Device,
                DeviceStateResource.Device,
                DeviceStateResource.Unauthorized,
                DeviceStateResource.Offline,
                DeviceStateResource.NoPermissions,
            ),
            states,
        )
    }

    @Test
    fun `parseAdbDevices does not mistake the no permissions hint for a tag`() {
        val row = parseAdbDevices(output).last()
        assertEquals(DeviceStateResource.NoPermissions, row.state)
        assertNull(row.model)
        assertNull(row.product)
        assertNull(row.transport)
    }

    @Test
    fun `parseAdbDevices maps an unmodelled state to Unknown`() {
        val rows = parseAdbDevices("List of devices attached\nR5CT30ABCDE    bootloader\n")
        assertEquals(DeviceStateResource.Unknown, rows.single().state)
    }

    @Test
    fun `parseAdbDevices skips a row with no state column`() {
        assertTrue(parseAdbDevices("List of devices attached\nR5CT30ABCDE\n").isEmpty())
    }
}

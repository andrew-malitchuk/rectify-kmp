package data.device.api

import data.device.api.source.model.AvdName
import data.device.api.source.model.DeviceId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** The other half of AC-44: an unvalidated serial can never become a [DeviceId]. */
class DeviceIdTest {

    @Test
    fun `of accepts a physical serial and an emulator serial`() {
        assertEquals("R5CT30ABCDE", assertNotNull(DeviceId.of("R5CT30ABCDE")).value)
        assertEquals("emulator-5554", assertNotNull(DeviceId.of("emulator-5554")).value)
        assertEquals("192.168.1.44:5555", assertNotNull(DeviceId.of("192.168.1.44:5555")).value)
    }

    @Test
    fun `of rejects blank input`() {
        assertNull(DeviceId.of(""))
        assertNull(DeviceId.of("   "))
    }

    @Test
    fun `of rejects a serial holding whitespace`() {
        assertNull(DeviceId.of("emulator 5554"))
        assertNull(DeviceId.of("emulator-5554\n"))
        assertNull(DeviceId.of(" emulator-5554"))
    }

    @Test
    fun `of rejects a leading dash that would read as a flag`() {
        assertNull(DeviceId.of("-s"))
        assertNull(DeviceId.of("--version"))
    }

    @Test
    fun `of rejects characters outside the serial charset`() {
        assertNull(DeviceId.of("device;rm -rf /"))
        assertNull(DeviceId.of("device\$(whoami)"))
        assertNull(DeviceId.of("device/../other"))
    }

    @Test
    fun `AvdName applies the same rules`() {
        assertEquals("Pixel_7_API_34", assertNotNull(AvdName.of("Pixel_7_API_34")).value)
        assertNull(AvdName.of(""))
        assertNull(AvdName.of("Pixel 7"))
        assertNull(AvdName.of("-avd"))
        assertNull(AvdName.of("Pixel;7"))
    }
}

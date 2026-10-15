package data.repository.impl

import data.device.api.source.model.AvdName
import data.device.api.source.model.AvdResource
import data.device.api.source.model.DeviceId
import data.repository.impl.core.mapper.EmulatorResourceMapper
import domain.core.source.model.device.EmulatorModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pins the AVD to picker entry contract, in particular that running is derived from the
 * presence of a serial rather than stored twice and allowed to disagree.
 */
class EmulatorResourceMapperTest {

    private fun avd(raw: String): AvdName = requireNotNull(AvdName.of(raw))

    @Test
    fun `toModel reports a configured avd as not running`() {
        val model = EmulatorResourceMapper.toModel.map(AvdResource(name = avd("Pixel_7_API_34")))
        assertEquals("Pixel_7_API_34", model.avdName)
        assertFalse(model.isRunning)
        assertNull(model.runningDeviceId)
    }

    @Test
    fun `toModel reports an avd with a serial as running`() {
        val model = EmulatorResourceMapper.toModel.map(
            AvdResource(name = avd("Pixel_7_API_34"), runningAs = DeviceId.of("emulator-5554"))
        )
        assertTrue(model.isRunning)
        assertEquals("emulator-5554", model.runningDeviceId)
    }

    @Test
    fun `toResource round trips a running instance`() {
        val resource = AvdResource(name = avd("Pixel_7_API_34"), runningAs = DeviceId.of("emulator-5556"))
        assertEquals(resource, EmulatorResourceMapper.toResource.map(EmulatorResourceMapper.toModel.map(resource)))
    }

    @Test
    fun `toResource substitutes a placeholder name instead of throwing`() {
        val resource = EmulatorResourceMapper.toResource.map(
            EmulatorModel(avdName = "not a valid name", isRunning = false)
        )
        assertEquals("unknown", resource.name.value)
        assertNull(resource.runningAs)
    }

    @Test
    fun `toResource drops a running serial no tool would report`() {
        val resource = EmulatorResourceMapper.toResource.map(
            EmulatorModel(avdName = "Pixel_7_API_34", isRunning = true, runningDeviceId = "-rf")
        )
        assertNull(resource.runningAs)
    }
}

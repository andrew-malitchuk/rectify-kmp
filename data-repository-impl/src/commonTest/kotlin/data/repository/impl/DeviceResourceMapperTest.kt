package data.repository.impl

import data.device.api.source.model.DeviceId
import data.device.api.source.model.DeviceResource
import data.device.api.source.model.DeviceStateResource
import data.repository.impl.core.mapper.DeviceResourceMapper
import domain.core.source.model.device.DeviceModel
import domain.core.source.model.device.DeviceStateModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pins the device row to picker row contract, in particular which states are selectable and
 * what a row is labelled when the tool reported no model.
 */
class DeviceResourceMapperTest {

    private fun serial(raw: String): DeviceId = requireNotNull(DeviceId.of(raw))

    @Test
    fun `toModel carries the tool model as the display name`() {
        val model = DeviceResourceMapper.toModel.map(
            DeviceResource(
                id = serial("R5CT10ABCDE"),
                state = DeviceStateResource.Device,
                model = "Pixel_7",
                product = "panther",
                transport = "3",
            )
        )
        assertEquals("Pixel_7", model.displayName)
        assertEquals("R5CT10ABCDE", model.id)
        assertEquals("R5CT10ABCDE", model.serial)
        assertEquals("3", model.transportLabel)
        assertFalse(model.isEmulator)
    }

    @Test
    fun `toModel falls back to the product then the serial for a nameless row`() {
        val withProduct = DeviceResourceMapper.toModel.map(
            DeviceResource(id = serial("abc123"), state = DeviceStateResource.Device, product = "panther")
        )
        assertEquals("panther", withProduct.displayName)

        val bare = DeviceResourceMapper.toModel.map(
            DeviceResource(id = serial("abc123"), state = DeviceStateResource.Device)
        )
        assertEquals("abc123", bare.displayName)
    }

    @Test
    fun `toModel marks only an online device as selectable`() {
        val states = mapOf(
            DeviceStateResource.Device to DeviceStateModel.Device,
            DeviceStateResource.Unauthorized to DeviceStateModel.Unauthorized,
            DeviceStateResource.Offline to DeviceStateModel.Offline,
            DeviceStateResource.NoPermissions to DeviceStateModel.NoPermissions,
            DeviceStateResource.Recovery to DeviceStateModel.Recovery,
            DeviceStateResource.Sideload to DeviceStateModel.Sideload,
            DeviceStateResource.Unknown to DeviceStateModel.Unknown,
        )
        states.forEach { (resource, expected) ->
            val model = DeviceResourceMapper.toModel.map(
                DeviceResource(id = serial("abc123"), state = resource)
            )
            assertEquals(expected, model.state)
            assertEquals(resource == DeviceStateResource.Device, model.isSelectable)
        }
    }

    @Test
    fun `toModel keeps the emulator flag of an emulator serial`() {
        val model = DeviceResourceMapper.toModel.map(
            DeviceResource(id = serial("emulator-5554"), state = DeviceStateResource.Device, isEmulator = true)
        )
        assertTrue(model.isEmulator)
    }

    @Test
    fun `toResource round trips a parsed row`() {
        val resource = DeviceResource(
            id = serial("emulator-5554"),
            state = DeviceStateResource.Unauthorized,
            model = "sdk_gphone64_arm64",
            transport = "7",
            isEmulator = true,
        )
        val back = DeviceResourceMapper.toResource.map(DeviceResourceMapper.toModel.map(resource))
        assertEquals(resource.id, back.id)
        assertEquals(resource.state, back.state)
        assertEquals(resource.model, back.model)
        assertEquals(resource.transport, back.transport)
        assertEquals(resource.isEmulator, back.isEmulator)
    }

    @Test
    fun `toResource substitutes a placeholder serial instead of throwing`() {
        val resource = DeviceResourceMapper.toResource.map(
            DeviceModel(
                id = "",
                displayName = "ghost",
                serial = "not a serial",
                state = DeviceStateModel.Unknown,
                isEmulator = false,
            )
        )
        assertEquals("unknown", resource.id.value)
    }
}

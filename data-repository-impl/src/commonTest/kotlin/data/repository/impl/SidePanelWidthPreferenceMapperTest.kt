package data.repository.impl

import data.preference.api.source.model.SidePanelWidthPreference
import data.repository.impl.core.mapper.SidePanelWidthPreferenceMapper
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins the plain-Int round trip of the side panel width mapper across the
 * documented bounds and the default.
 */
class SidePanelWidthPreferenceMapperTest {

    @Test
    fun `round trips the lower bound, the default and the upper bound`() {
        listOf(260, 320, 480).forEach { widthDp ->
            val resource = SidePanelWidthPreferenceMapper.toResource.map(widthDp)

            assertEquals(widthDp, resource.widthDp)
            assertEquals(widthDp, SidePanelWidthPreferenceMapper.toModel.map(resource))
        }
    }

    @Test
    fun `toModel unwraps the default resource`() {
        assertEquals(320, SidePanelWidthPreferenceMapper.toModel.map(SidePanelWidthPreference()))
    }
}

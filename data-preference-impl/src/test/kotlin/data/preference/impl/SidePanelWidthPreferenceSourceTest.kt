package data.preference.impl

import com.russhwolf.settings.MapSettings
import data.preference.api.source.model.SidePanelWidthPreference
import data.preference.impl.source.datasource.SidePanelWidthPreferenceSourceImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Covers the persistence contract of `pref_side_panel_width` against an in-memory
 * [MapSettings], including the relaunch proxy: a fresh source over the same backing
 * store must observe the previously written width.
 */
class SidePanelWidthPreferenceSourceTest {

    @Test
    fun `unset width reads the default`() = runTest {
        val source = SidePanelWidthPreferenceSourceImpl(MapSettings())

        assertEquals(SidePanelWidthPreference(320), source.getData())
        assertEquals(320, SidePanelWidthPreference.DEFAULT_WIDTH_DP)
    }

    @Test
    fun `setData persists and getData reads it back`() = runTest {
        val source = SidePanelWidthPreferenceSourceImpl(MapSettings())

        source.setData(SidePanelWidthPreference(480))

        assertEquals(SidePanelWidthPreference(480), source.getData())
    }

    @Test
    fun `observeData emits the new value`() = runTest {
        val source = SidePanelWidthPreferenceSourceImpl(MapSettings())

        assertEquals(SidePanelWidthPreference(320), source.observeData().first())

        source.setData(SidePanelWidthPreference(260))

        assertEquals(SidePanelWidthPreference(260), source.observeData().first())
    }

    @Test
    fun `a fresh source over the same settings sees the stored width`() = runTest {
        val settings = MapSettings()
        SidePanelWidthPreferenceSourceImpl(settings).setData(SidePanelWidthPreference(480))

        val relaunched = SidePanelWidthPreferenceSourceImpl(settings)

        assertEquals(SidePanelWidthPreference(480), relaunched.getData())
        assertEquals(SidePanelWidthPreference(480), relaunched.observeData().first())
    }

    @Test
    fun `the storage key is pref_side_panel_width`() = runTest {
        val settings = MapSettings()

        SidePanelWidthPreferenceSourceImpl(settings).setData(SidePanelWidthPreference(400))

        assertEquals(400, settings.getIntOrNull("pref_side_panel_width"))
    }
}

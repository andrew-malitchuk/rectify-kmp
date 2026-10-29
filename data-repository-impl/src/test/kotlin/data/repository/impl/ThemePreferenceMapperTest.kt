package data.repository.impl

import data.preference.api.source.model.ThemePreference
import data.repository.impl.core.mapper.ThemePreferenceMapper
import domain.core.source.model.ThemeModel
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins the stored-string <-> [ThemeModel] contract, including the System fallback
 * that makes [ThemePreference.DEFAULT_THEME] resolve to the platform appearance.
 */
class ThemePreferenceMapperTest {

    @Test
    fun `toModel maps light to Light`() {
        assertEquals(ThemeModel.Light, ThemePreferenceMapper.toModel.map(ThemePreference("light")))
    }

    @Test
    fun `toModel maps dark to Dark`() {
        assertEquals(ThemeModel.Dark, ThemePreferenceMapper.toModel.map(ThemePreference("dark")))
    }

    @Test
    fun `toModel maps the legacy materialu value to System`() {
        assertEquals(ThemeModel.System, ThemePreferenceMapper.toModel.map(ThemePreference("materialu")))
    }

    @Test
    fun `toModel maps the default system value to System`() {
        assertEquals(ThemePreference.DEFAULT_THEME, "system")
        assertEquals(ThemeModel.System, ThemePreferenceMapper.toModel.map(ThemePreference()))
        assertEquals(ThemeModel.System, ThemePreferenceMapper.toModel.map(ThemePreference("system")))
    }

    @Test
    fun `toModel falls back to System for an unrecognised value`() {
        assertEquals(ThemeModel.System, ThemePreferenceMapper.toModel.map(ThemePreference("not-a-theme")))
        assertEquals(ThemeModel.System, ThemePreferenceMapper.toModel.map(ThemePreference("")))
    }

    @Test
    fun `toResource keeps the stored materialu value for System`() {
        assertEquals("materialu", ThemePreferenceMapper.toResource.map(ThemeModel.System).theme)
        assertEquals("light", ThemePreferenceMapper.toResource.map(ThemeModel.Light).theme)
        assertEquals("dark", ThemePreferenceMapper.toResource.map(ThemeModel.Dark).theme)
    }
}

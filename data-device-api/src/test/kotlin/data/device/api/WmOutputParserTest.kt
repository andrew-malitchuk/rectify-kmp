package data.device.api

import data.device.api.source.parse.parseWmDensity
import data.device.api.source.parse.parseWmSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Pins the `wm size` and `wm density` contract.
 *
 * The override preference is deliberate: when a display override is set it is what
 * screencap actually produces, so reporting the physical size would disagree with the
 * pixels on screen. Do not invert this.
 */
class WmOutputParserTest {

    @Test
    fun `parseWmSize reads the physical size when it is the only line`() {
        assertEquals(1080 to 2400, parseWmSize("Physical size: 1080x2400\n"))
    }

    @Test
    fun `parseWmSize prefers the override size over the physical size`() {
        val output = "Physical size: 1080x2400\nOverride size: 720x1600\n"
        assertEquals(720 to 1600, parseWmSize(output))
    }

    @Test
    fun `parseWmSize returns null when no size line is present`() {
        assertNull(parseWmSize(""))
        assertNull(parseWmSize("adb server version mismatch\n"))
    }

    @Test
    fun `parseWmDensity reads the physical density when it is the only line`() {
        assertEquals(420, parseWmDensity("Physical density: 420\n"))
    }

    @Test
    fun `parseWmDensity prefers the override density over the physical density`() {
        assertEquals(560, parseWmDensity("Physical density: 420\nOverride density: 560\n"))
    }

    @Test
    fun `parseWmDensity returns null when no density line is present`() {
        assertNull(parseWmDensity(""))
    }
}

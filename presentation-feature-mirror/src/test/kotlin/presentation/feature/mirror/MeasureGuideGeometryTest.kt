package presentation.feature.mirror

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.core.measure.MeasureNode
import presentation.feature.mirror.core.measure.measure
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.MeasureGuide
import presentation.feature.mirror.source.mirror.MeasureSide
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * FR-38 / FR-39 -- `MeasureReadout.guides`, the lines the overlay actually draws.
 *
 * Nothing else asserts guide CONTENTS: `GuideSuppressionTest` pins only `guideNumeral`'s
 * suppression and `MeasureEndToEndTest` pins the panel's value rows, the neighbour rects and
 * the rail. Endpoint geometry was therefore implemented from the design and unverified, and
 * wrong endpoints would put every line in the wrong place with no test failing.
 *
 * The expectations below come from `inputs/design-measure-source.md`'s own `H`/`V` helpers run
 * over its own `ELS` fixture and `item_price` annotation -- re-executed in a throwaway script,
 * exactly as `MeasureEndToEndTest` did for the value rows, not read back off this
 * implementation:
 *
 * ```
 * H(166, 228, 206.5, 62)   H(276, 296, 206.5, 20)
 * V(183, 198, 252.0, 15)   V(215, 264, 252.0, 49)
 * ```
 *
 * The COUNT is asserted alongside the endpoints because `H`/`V` are `if (v < 1) return` --
 * a silently missing guide would otherwise pass.
 */
class MeasureGuideGeometryTest {

    private fun node(
        id: String,
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        tappable: Boolean = false,
    ): MeasureNode =
        MeasureNode(id = id, rect = DeviceRect(x, y, x + w, y + h), isTappable = tappable)

    // The design's own ELS -- all 19 nodes, verbatim from inputs/design-measure-source.md.
    private val els = listOf(
        node("cart_title", 20, 50, 58, 31),
        node("item_image_1", 20, 110, 52, 52),
        node("item_title_1", 84, 110, 104, 17),
        node("item_meta_1", 84, 128, 52, 15),
        node("qty_1", 84, 146, 84, 24, tappable = true),
        node("item_price_1", 228, 110, 48, 17),
        node("divider_1", 20, 182, 256, 1),
        node("item_image", 20, 192, 52, 52),
        node("item_title", 84, 192, 82, 17),
        node("item_meta", 84, 208, 32, 15),
        node("qty", 84, 226, 84, 24, tappable = true),
        node("item_price", 228, 198, 48, 17),
        node("divider", 20, 264, 256, 1),
        node("promo", 20, 278, 256, 40, tappable = true),
        node("subtotal", 20, 340, 256, 16),
        node("shipping", 20, 364, 256, 16),
        node("total", 20, 404, 256, 19),
        node("returns_note", 20, 440, 160, 15),
        node("checkout", 36, 566, 240, 50, tappable = true),
    )

    /** The design's own frame, not an invented one -- the design's `R ? R.x : 296` / `B ? B.y : 640` fallbacks. */
    private val frame = IntSize(296, 640)

    private fun guide(side: MeasureSide, label: String, from: IntOffset, to: IntOffset) =
        MeasureGuide(side = side, label = label, from = from, to = to)

    @Test
    fun allFourGuidesMatchTheDesignsOwnHAndVGeometryOnTheOracleFixture() {
        val readout = measure(
            annotation = els.first { it.id == "item_price" }.rect,
            index = 1,
            nodes = els,
            frameSize = frame,
            densityDpi = 160, // px == dp, so these are the design's own unitless numbers
        )

        // Mid-y is the design's 206.5 truncated to the integer pixel grid the overlay draws on;
        // mid-x is the design's 252.0 exactly. Order is the design's own call order.
        assertEquals(
            listOf(
                // H(lx = item_title's right edge, t.x, my, g.left = 62)
                guide(MeasureSide.Left, "62dp", IntOffset(166, 206), IntOffset(228, 206)),
                // H(t.x + t.w, rx = the frame's right edge, my, g.right = 20)
                guide(MeasureSide.Right, "20dp", IntOffset(276, 206), IntOffset(296, 206)),
                // V(ty = divider_1's bottom edge, t.y, mx, g.top = 15)
                guide(MeasureSide.Top, "15dp", IntOffset(252, 183), IntOffset(252, 198)),
                // V(t.y + t.h, by = divider's top edge, mx, g.bottom = 49)
                guide(MeasureSide.Bottom, "49dp", IntOffset(252, 215), IntOffset(252, 264)),
            ),
            readout.guides,
        )
    }

    @Test
    fun aGuideWhoseGapIsUnderOneDisplayUnitIsAbsentFromTheListEntirely() {
        // Same shape as the design's `if (v < 1) return`, but at a real density: a 2px left gap
        // is 0.76dp at 420 dpi, so the LEFT guide drops out while the other three keep their own
        // geometry. Asserting the whole list is what makes the count part of the assertion --
        // comparing endpoints side by side would pass with a guide silently missing.
        val target = node("item_price", 100, 100, 100, 40) // rect 100,100 -> 200,140
        val leftNeighbour = node("item_title", 0, 100, 98, 40) // right edge 98 -> a 2px gap
        val readout = measure(
            annotation = target.rect,
            index = 1,
            nodes = listOf(target, leftNeighbour),
            frameSize = frame,
            densityDpi = 420,
        )

        // mid-x 150, mid-y 120; gaps 96px right (37dp), 100px to the top frame edge (38dp),
        // 500px to the bottom frame edge (190dp).
        assertEquals(
            listOf(
                guide(MeasureSide.Right, "37dp", IntOffset(200, 120), IntOffset(296, 120)),
                guide(MeasureSide.Top, "38dp", IntOffset(150, 0), IntOffset(150, 100)),
                guide(MeasureSide.Bottom, "190dp", IntOffset(150, 140), IntOffset(150, 640)),
            ),
            readout.guides,
        )
    }
}

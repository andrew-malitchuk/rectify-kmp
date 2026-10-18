package presentation.feature.mirror

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.core.measure.MeasureNode
import presentation.feature.mirror.core.measure.measure
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.MeasureSide
import presentation.feature.mirror.source.mirror.MeasureUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.TimeSource

/**
 * AC-10 / AC-65 / AC-68 -- measure(...) against the design's own ELS fixture and its own
 * measure(a, u), recovered verbatim in design-measure-source.md. That file outranks the prose
 * design-measure-mode.md and every paraphrase of it (including an earlier draft of this file),
 * per the Stage 4 orchestrator's correction. Density 160 is used so px == dp and every number
 * below is exact, matching the design's own unitless arithmetic (the design does no density
 * conversion at all -- `u` is only a label suffix there).
 *
 * The exact numbers asserted here were cross-checked by literally re-implementing
 * design-measure-source.md's `measure(a, u)` in a throwaway script and running it over this
 * same ELS array -- not derived by hand. See the Top-row note below for why that mattered.
 */
class MeasureEndToEndTest {

    private fun node(
        id: String,
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        tappable: Boolean = false,
    ): MeasureNode = MeasureNode(id = id, rect = DeviceRect(x, y, x + w, y + h), isTappable = tappable)

    // The design's own ELS -- all 19 nodes, verbatim from design-measure-source.md.
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

    // design-measure-source.md detail 1: the design's own frame is 296x640, not an invented one.
    private val frame = IntSize(296, 640)
    private val densityDpi = 160

    @Test
    fun itemPriceReproducesTheDesignsOracleExactly() {
        val itemPrice = els.first { it.id == "item_price" }
        val readout = measure(
            annotation = itemPrice.rect,
            index = 1,
            nodes = els,
            frameSize = frame,
            densityDpi = densityDpi,
        )

        assertEquals("item_price", readout.nodeId)
        assertEquals(itemPrice.rect, readout.targetRect)
        assertEquals(MeasureUnit.Dp, readout.unit)

        // Right/Bottom/Left (20/49/62) match MeasureCard.kt's own shipped @Preview exactly, and
        // so does Size and the finding -- strong confirmation this is the right fixture and the
        // right annotation. Top does NOT match the preview's hand-typed "9dp": re-running the
        // design's own algorithm gives 15 (nearest node above is `divider_1`, bottom edge 183,
        // not the "9" the preview card shows). The preview's finding uses the LEFT neighbour
        // (item_title, delta 6) -- a completely different node from the TOP-gap neighbour
        // (divider_1) -- so the two numbers were never coupled, and the preview's "9dp" looks
        // like a hand-picked illustrative value rather than this algorithm's real output. Pinned
        // to the computed 15, per design-measure-source.md outranking the preview.
        assertEquals(
            listOf(
                MeasureSide.Size to "48×17dp",
                MeasureSide.Top to "15dp",
                MeasureSide.Right to "20dp",
                MeasureSide.Bottom to "49dp",
                MeasureSide.Left to "62dp",
            ),
            readout.values.map { it.side to it.value },
        )
        assertEquals("top Δ6dp vs item_title · expected 0", readout.finding)
        assertEquals(
            "⌥ item_price 48×17dp · top 15dp · right 20dp · bottom 49dp · left 62dp" +
                " · top Δ6dp vs item_title · expected 0",
            readout.noteLine,
        )

        // Neighbour rects: [L, R, T, B].filter(present) -- R is null here (nothing to the
        // right), so only three. None of this fixture's dividers are 0px tall, so the design's
        // `Math.max(e.h, 1)` floor is not exercised by this oracle; NOT asserted here.
        assertEquals(
            listOf(
                DeviceRect(84, 192, 166, 209), // item_title
                DeviceRect(20, 182, 276, 183), // divider_1
                DeviceRect(20, 264, 276, 265), // divider
            ),
            readout.neighbourRects,
        )

        // The alignment rail: from the left neighbour's own left edge, at the left neighbour's
        // top, across to the target's right edge -- design-measure-source.md detail 8.
        val rail = readout.rail
        checkNotNull(rail)
        assertEquals(MeasureSide.Top, rail.side)
        assertEquals("Δ6dp", rail.label)
        assertEquals(IntOffset(84, 192), rail.from) // L.x, L.y
        assertEquals(IntOffset(276, 192), rail.to) // t.x + t.w, L.y
    }

    @Test
    fun aRegionContainingNoFullyContainedNodeFallsBackToViewNoMatchOnTheSameFixture() {
        // A small box in the gap between the price row and the next divider: wide enough to
        // overlap several nodes, but small enough (and unaligned enough) that nothing is fully
        // contained within 4px slack. Re-verified against the design's own algorithm.
        val annotation = DeviceRect(150, 220, 160, 230)
        val readout = measure(
            annotation = annotation,
            index = 2,
            nodes = els,
            frameSize = frame,
            densityDpi = densityDpi,
        )

        assertEquals("View (no match)", readout.nodeId)
        assertEquals(annotation, readout.targetRect)
        assertEquals(
            listOf(
                MeasureSide.Size to "10×10dp",
                MeasureSide.Top to "11dp",
                MeasureSide.Right to "136dp",
                MeasureSide.Bottom to "34dp",
                MeasureSide.Left to "34dp",
            ),
            readout.values.map { it.side to it.value },
        )
        assertNull(readout.finding)
        assertNull(readout.rail)
    }

    @Test
    fun ac68TheOptionDownToPanelVisibleComputationStaysWellUnderTheGenerousCeiling() {
        // A generous ceiling over many iterations and a bigger-than-realistic node list, so
        // this catches an accidental O(n^2) rather than benchmarking the host --
        // iosSimulatorArm64 under `:build` is explicitly the slowest target this must tolerate
        // (testing.md).
        val manyNodes = (0 until 300).map { i ->
            node("node_$i", i, i, 10, 10)
        } + els

        val start = TimeSource.Monotonic.markNow()
        repeat(200) {
            measure(
                annotation = els.first { it.id == "item_price" }.rect,
                index = 1,
                nodes = manyNodes,
                frameSize = frame,
                densityDpi = densityDpi,
            )
        }
        val elapsed = start.elapsedNow()
        // 200 calls over 300+ nodes in under 5 seconds is enormously generous for an
        // allocation-light set of linear scans with a ~100ms-per-call budget (NFR-2/NFR-13).
        assertTrue(elapsed.inWholeMilliseconds < 5_000)
    }
}

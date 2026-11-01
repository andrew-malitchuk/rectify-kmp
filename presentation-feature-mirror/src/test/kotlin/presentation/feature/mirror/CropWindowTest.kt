package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.core.payload.CropWindow
import presentation.feature.mirror.core.payload.cropWindow
import presentation.feature.mirror.source.mirror.DeviceRect
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Hand-computed pins for `cropWindow`, the design-space crop transform behind the Payload tab's
 * crop grid (AC-76, AC-14). Every expected literal below was derived independently of the
 * production formula — see the per-test comments for the arithmetic — not re-derived from it.
 *
 * Tolerance is generous (0.1px) because the hand math below is rounded to 2-4 decimals by hand;
 * it is still far tighter than the gap between "scaled" and "unscaled" pad (37px at 1080-wide),
 * so it cannot paper over a real formula mistake.
 */
private const val TOLERANCE = 0.1f

private fun assertNear(expected: Float, actual: Float, what: String) {
    assertTrue(abs(expected - actual) <= TOLERANCE, "$what expected ~$expected but was $actual")
}

/**
 * TAC-4's second half, checked against every window built below rather than once: the outline
 * must never spill past the container it is drawn into.
 */
private fun assertOutlineInsideDst(window: CropWindow, what: String) {
    val outline = window.outlineRect
    val dst = window.dstSize
    assertTrue(outline.left >= -TOLERANCE, "$what outline left ${outline.left} is negative")
    assertTrue(outline.top >= -TOLERANCE, "$what outline top ${outline.top} is negative")
    assertTrue(
        outline.right <= dst.width + TOLERANCE,
        "$what outline right ${outline.right} exceeds dstSize.width ${dst.width}",
    )
    assertTrue(
        outline.bottom <= dst.height + TOLERANCE,
        "$what outline bottom ${outline.bottom} exceeds dstSize.height ${dst.height}",
    )
}

class CropWindowTest {

    // TAC-1: a centred rect on a 1080-wide frame, every field hand-computed independently.
    @Test
    fun centredRectOnFrameWidthTenEightyMatchesHandComputedWindow() {
        // rect centred in 1080x2400: centre (540, 1200) == frame centre.
        val rect = DeviceRect(left = 440, top = 1100, right = 640, bottom = 1300)

        val window = cropWindow(rect, IntSize(1080, 2400))

        // pad = 14 * 1080/296 = 1890/37 = 51.0811 (device px — NOT the literal 14).
        // cx = 440 - 51.0811 = 388.9189, cy = 1100 - 51.0811 = 1048.9189.
        // cw = ch = 200 + 2*51.0811 = 302.1622 (neither clamp fires: 1080-cx=691.08 > cw and
        // 2400-cy=1351.08 > ch).
        // s = min(84/302.1622, 72/302.1622) = 72/302.1622 = 0.238283 (height-bound, since the
        // crop window is square but the box is not).
        // dstSize = cw*s = ch*s = 72 exactly, because cw == ch and s == 72/ch by construction.
        assertNear(388.9189f, window.srcOffset.x, "srcOffset.x")
        assertNear(1048.9189f, window.srcOffset.y, "srcOffset.y")
        assertNear(302.1622f, window.srcSize.width, "srcSize.width")
        assertNear(302.1622f, window.srcSize.height, "srcSize.height")
        assertNear(0.238283f, window.scale, "scale")
        assertNear(72f, window.dstSize.width, "dstSize.width")
        assertNear(72f, window.dstSize.height, "dstSize.height")
        // outline = (rect edge - crop origin) * scale: left/top is pad*s, width/height is
        // rect size*s. pad*s = 51.0811 * 0.238283 = 12.17. 200 * 0.238283 = 47.66.
        assertNear(12.17f, window.outlineRect.left, "outlineRect.left")
        assertNear(12.17f, window.outlineRect.top, "outlineRect.top")
        assertNear(47.66f, window.outlineRect.width, "outlineRect.width")
        assertNear(47.66f, window.outlineRect.height, "outlineRect.height")
        assertOutlineInsideDst(window, "centred")
    }

    // TAC-2, clamp 1 of 4: x - pad < 0 at the frame's left edge.
    @Test
    fun leftEdgeClampZeroesSrcOffsetWhenXMinusPadIsNegative() {
        // frameSize == design size, so pad == the literal 14 (isolates the clamp from scaling).
        // rect left=5: 5 - 14 = -9 < 0, so cx clamps to 0 instead of going negative.
        val rect = DeviceRect(left = 5, top = 100, right = 55, bottom = 150)

        val window = cropWindow(rect, IntSize(296, 640))

        // cx = max(0, 5-14) = 0. cy = max(0, 100-14) = 86.
        // cw = min(296-0, 50+28) = min(296, 78) = 78 (pad branch, not clamped itself).
        // ch = min(640-86, 78) = min(554, 78) = 78.
        // s = min(84/78, 72/78) = 72/78 = 12/13 = 0.923077. dst = 78*12/13 = 72 exactly (both).
        assertNear(0f, window.srcOffset.x, "srcOffset.x")
        assertNear(86f, window.srcOffset.y, "srcOffset.y")
        assertNear(78f, window.srcSize.width, "srcSize.width")
        assertNear(78f, window.srcSize.height, "srcSize.height")
        assertNear(0.923077f, window.scale, "scale")
        assertNear(72f, window.dstSize.width, "dstSize.width")
        assertNear(72f, window.dstSize.height, "dstSize.height")
        // outline left = (5-0)*s = 60/13 = 4.6154. top = (100-86)*s = 168/13 = 12.9231.
        assertNear(4.6154f, window.outlineRect.left, "outlineRect.left")
        assertNear(12.9231f, window.outlineRect.top, "outlineRect.top")
        assertNear(46.1538f, window.outlineRect.width, "outlineRect.width") // 50*12/13
        assertNear(46.1538f, window.outlineRect.height, "outlineRect.height")
        assertOutlineInsideDst(window, "left-clamp")
    }

    // TAC-2, clamp 2 of 4: y - pad < 0 at the frame's top edge (mirror of the left-edge case).
    @Test
    fun topEdgeClampZeroesSrcOffsetWhenYMinusPadIsNegative() {
        val rect = DeviceRect(left = 100, top = 5, right = 150, bottom = 55)

        val window = cropWindow(rect, IntSize(296, 640))

        // cy = max(0, 5-14) = 0. cx = max(0, 100-14) = 86.
        // cw = min(296-86, 78) = min(210, 78) = 78. ch = min(640-0, 78) = 78.
        // s = 72/78 = 12/13 = 0.923077, dst = (72, 72) exactly, same as the left-clamp case.
        assertNear(86f, window.srcOffset.x, "srcOffset.x")
        assertNear(0f, window.srcOffset.y, "srcOffset.y")
        assertNear(78f, window.srcSize.width, "srcSize.width")
        assertNear(78f, window.srcSize.height, "srcSize.height")
        assertNear(0.923077f, window.scale, "scale")
        assertNear(72f, window.dstSize.width, "dstSize.width")
        assertNear(72f, window.dstSize.height, "dstSize.height")
        // outline left = (100-86)*s = 168/13 = 12.9231. top = (5-0)*s = 60/13 = 4.6154.
        assertNear(12.9231f, window.outlineRect.left, "outlineRect.left")
        assertNear(4.6154f, window.outlineRect.top, "outlineRect.top")
        assertNear(46.1538f, window.outlineRect.width, "outlineRect.width")
        assertNear(46.1538f, window.outlineRect.height, "outlineRect.height")
        assertOutlineInsideDst(window, "top-clamp")
    }

    // TAC-2, clamp 3 of 4: the pad-expanded window overflows the frame's right edge.
    @Test
    fun rightEdgeClampShrinksWindowAtFrameRightEdge() {
        // frame 296 wide; rect sits close enough to x=296 that cx + (w + pad*2) > 296.
        val rect = DeviceRect(left = 250, top = 300, right = 290, bottom = 340)

        val window = cropWindow(rect, IntSize(296, 640))

        // cx = 250-14 = 236. cw unclamped = 40+28 = 68, but 296-236 = 60 < 68 -> cw clamps to 60.
        // cy = 300-14 = 286. ch unclamped = 68, 640-286 = 354 > 68 -> ch stays 68 (unclamped).
        // s = min(84/60, 72/68) = min(1.4, 18/17=1.0588) = 18/17 (height-bound this time).
        // dst = (60*18/17 = 1080/17 = 63.5294, 68*18/17 = 72 exactly).
        assertNear(236f, window.srcOffset.x, "srcOffset.x")
        assertNear(286f, window.srcOffset.y, "srcOffset.y")
        assertNear(60f, window.srcSize.width, "srcSize.width") // clamped, not 40+28=68
        assertNear(68f, window.srcSize.height, "srcSize.height")
        assertNear(1.0588f, window.scale, "scale")
        assertNear(63.5294f, window.dstSize.width, "dstSize.width")
        assertNear(72f, window.dstSize.height, "dstSize.height")
        // outline left = top = (250-236)*s = (300-286)*s = 14*18/17 = 252/17 = 14.8235.
        assertNear(14.8235f, window.outlineRect.left, "outlineRect.left")
        assertNear(14.8235f, window.outlineRect.top, "outlineRect.top")
        assertNear(42.3529f, window.outlineRect.width, "outlineRect.width") // 40*18/17
        assertNear(42.3529f, window.outlineRect.height, "outlineRect.height")
        assertOutlineInsideDst(window, "right-clamp")
    }

    // TAC-2, clamp 4 of 4: the pad-expanded window overflows the frame's bottom edge.
    @Test
    fun bottomEdgeClampShrinksWindowAtFrameBottomEdge() {
        // frame 296x640; rect's bottom edge sits exactly on the frame's bottom edge (y=640).
        val rect = DeviceRect(left = 100, top = 600, right = 140, bottom = 640)

        val window = cropWindow(rect, IntSize(296, 640))

        // cx = 100-14 = 86. cw unclamped = 68, 296-86 = 210 > 68 -> cw stays 68 (unclamped).
        // cy = 600-14 = 586. ch unclamped = 68, but 640-586 = 54 < 68 -> ch clamps to 54.
        // s = min(84/68, 72/54) = min(21/17=1.2353, 4/3=1.3333) = 21/17 (width-bound this time).
        // dst = (68*21/17 = 84 exactly, 54*21/17 = 1134/17 = 66.7059).
        assertNear(86f, window.srcOffset.x, "srcOffset.x")
        assertNear(586f, window.srcOffset.y, "srcOffset.y")
        assertNear(68f, window.srcSize.width, "srcSize.width")
        assertNear(54f, window.srcSize.height, "srcSize.height") // clamped, not 40+28=68
        assertNear(1.2353f, window.scale, "scale")
        assertNear(84f, window.dstSize.width, "dstSize.width")
        assertNear(66.7059f, window.dstSize.height, "dstSize.height")
        // outline left = top = (100-86)*s = (600-586)*s = 14*21/17 = 294/17 = 17.2941.
        assertNear(17.2941f, window.outlineRect.left, "outlineRect.left")
        assertNear(17.2941f, window.outlineRect.top, "outlineRect.top")
        assertNear(49.4118f, window.outlineRect.width, "outlineRect.width") // 40*21/17
        assertNear(49.4118f, window.outlineRect.height, "outlineRect.height")
        // The rect's bottom edge touches the frame's bottom edge, so the outline's bottom edge
        // lands exactly on dstSize.height (66.7059) rather than strictly inside it — the
        // sharpest case the "stays inside" invariant can be asked to hold.
        assertOutlineInsideDst(window, "bottom-clamp")
    }

    // TAC-3, frame width 1 of 2: at frameSize.width == designWidth (296), pad is the literal 14
    // with no scaling applied (scale factor 296/296 == 1). This alone can't tell a correctly
    // scaled implementation apart from a bug that always uses the literal 14 — see the next test.
    @Test
    fun padAtDesignWidthIsTheLiteralFourteenPixels() {
        val rect = DeviceRect(left = 100, top = 100, right = 140, bottom = 140)

        val window = cropWindow(rect, IntSize(296, 640))

        // Neither clamp fires: cx=86, cy=86, cw=ch=min(296-86=210, 68)=min(640-86=554,68)=68.
        val recoveredPadX = rect.left - window.srcOffset.x
        val recoveredPadY = rect.top - window.srcOffset.y
        assertNear(14f, recoveredPadX, "pad recovered from srcOffset.x")
        assertNear(14f, recoveredPadY, "pad recovered from srcOffset.y")
        assertOutlineInsideDst(window, "pad-296")
    }

    // TAC-3, frame width 2 of 2: at frameSize.width == 1080, pad must scale to
    // 14 * 1080/296 = 1890/37 = 51.0811 (truncates/rounds to "~51" per the task, asserted here
    // to 2dp since this is a continuous value, not an integer pixel count). A bug that forgets
    // to scale pad would still report 14 here, 37px off — far outside this test's tolerance.
    @Test
    fun padScalesToAboutFiftyOnePixelsOnFrameWidthTenEighty() {
        val rect = DeviceRect(left = 500, top = 500, right = 540, bottom = 540)

        val window = cropWindow(rect, IntSize(1080, 2400))

        // Neither clamp fires: cx=cy=448.9189, cw=ch=142.1622, both well inside the frame.
        val recoveredPadX = rect.left - window.srcOffset.x
        val recoveredPadY = rect.top - window.srcOffset.y
        assertNear(51.0811f, recoveredPadX, "pad recovered from srcOffset.x")
        assertNear(51.0811f, recoveredPadY, "pad recovered from srcOffset.y")
        assertOutlineInsideDst(window, "pad-1080")
    }

    // TAC-4: a rect larger than the box (84x72) scales DOWN only — scale must stay below 1,
    // never flip to an upscale the way a small annotation's crop legitimately does (see the
    // left/top-clamp cases above, where scale is ~0.92 but could exceed 1 for a smaller rect).
    @Test
    fun rectLargerThanBoxScalesDownNeverUp() {
        // 600x600 rect, centred-ish, well clear of every edge on a 1080x2400 frame.
        val rect = DeviceRect(left = 300, top = 900, right = 900, bottom = 1500)

        val window = cropWindow(rect, IntSize(1080, 2400))

        // pad = 51.0811 (as above). cx=cy=248.9189, cw=ch=600+2*51.0811=702.1622 (no clamp:
        // 1080-cx=831.08 > cw, 2400-cy=1551.08 > ch).
        // s = min(84/702.1622, 72/702.1622) = 72/702.1622 = 0.10254.
        assertTrue(window.scale < 1f, "scale ${window.scale} must be < 1 for an oversized rect")
        assertNear(0.10254f, window.scale, "scale")
        assertNear(72f, window.dstSize.width, "dstSize.width") // cw == ch, s == 72/ch
        assertNear(72f, window.dstSize.height, "dstSize.height")
        assertOutlineInsideDst(window, "oversized")
    }
}

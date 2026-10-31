package presentation.core.ui.source.kit.molecule.annotation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.annotation.AnnotationMark
import presentation.core.ui.source.kit.atom.annotation.AnnotationState

/**
 * Opacity of the dim applied outside the marks when the frame is frozen.
 *
 * 42% is the style guide's figure for "frozen · outside dims 42%" — dark enough to push the
 * live-looking parts of the screenshot back, light enough that the user can still read what
 * they did *not* select and decide to move the mark.
 */
private const val SCRIM_ALPHA = 0.42f

/** Preview-only stand-in frame. */
private val PreviewFrameWidth: Dp = 260.dp
private val PreviewFrameHeight: Dp = 180.dp

/**
 * One mark to paint, positioned in **dp relative to the overlay's own top-left corner**.
 *
 * Dp, never pixels, and deliberately so: the host has exactly one component that knows about
 * pixels and density (`FrameGeometry`), and if this type took pixels the kit would need a
 * second density conversion that could silently disagree with it.
 *
 * @property bounds Mark rectangle, in dp within the overlay.
 * @property state Lifecycle state passed straight through to [AnnotationMark].
 * @property badge Corner badge glyph, or `null`.
 * @property sizeLabel Measurement on the bottom edge, e.g. `545 × 254`, or `null`.
 * @property showHandles Whether to draw the four resize handles.
 */
public data class AnnotationOverlayMark(
    val bounds: DpRect,
    val state: AnnotationState = AnnotationState.Pinned,
    val badge: String? = null,
    val sizeLabel: String? = null,
    val showHandles: Boolean = false,
)

/**
 * The annotation layer that sits on top of a mirrored frame: an optional scrim with the marks
 * punched out of it, and one [AnnotationMark] per rectangle.
 *
 * Fills whatever box it is given, which must be **exactly the frame box** — every [bounds] is
 * relative to this composable's own top-left corner, so composing it anywhere but over the
 * painted frame puts the marks in the wrong place.
 *
 * The scrim is one `clipPath(..., ClipOp.Difference)` over a single `drawRect`. The frame's
 * `ImageBitmap` is never read, copied, re-drawn or composited into an offscreen layer: dimming
 * ten frames a second by compositing would cost a full-frame Skia surface per frame, and
 * punching a hole in a flat rectangle costs nothing (NFR-1).
 *
 * `AnnotationState.Frozen` is deliberately **not** used by this component even when [dimmed] is
 * true: that state paints an opaque plate over the mark, which would hide the exact pixels the
 * annotation points at. The dim lives out here, outside the marks, where it belongs.
 *
 * The scrim's colour is `Theme.color.overlayScrim` — the same fixed near-black `rgb(12,12,14)` in
 * both themes, for the same reason
 * [CommentPopover][presentation.core.ui.source.kit.organism.annotation.CommentPopover] is always
 * dark: it is painted over mirrored device UI whose brightness is unknown, so a theme-following
 * surface washes out against a light screenshot. A theme-following token would also be weaker in
 * dark than the design asks for, since dark surfaces sit lighter than `rgb(12,12,14)`.
 *
 * @param marks Marks to paint, in overlay-local dp.
 * @param modifier Modifier applied to the root composable.
 * @param dimmed Whether to dim everything outside the marks — true while the frame is frozen.
 */
@Composable
public fun AnnotationOverlay(
    marks: List<AnnotationOverlayMark>,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
) {
    val scrim = Theme.color.overlayScrim.copy(alpha = SCRIM_ALPHA)
    val density = LocalDensity.current
    // Square corners rather than the mark's 3 dp radius: the sliver of scrim that would show in
    // a corner is under a pixel, and copying the atom's private radius here is the kind of
    // duplicate constant that drifts the first time the atom is restyled.
    val cutouts: Path = remember(marks, density) {
        Path().apply {
            marks.forEach { mark ->
                with(density) {
                    addRect(
                        Rect(
                            left = mark.bounds.left.toPx(),
                            top = mark.bounds.top.toPx(),
                            right = mark.bounds.right.toPx(),
                            bottom = mark.bounds.bottom.toPx(),
                        ),
                    )
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (dimmed) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (marks.isEmpty()) {
                    drawRect(color = scrim)
                } else {
                    clipPath(path = cutouts, clipOp = ClipOp.Difference) { drawRect(color = scrim) }
                }
            }
        }
        marks.forEach { mark ->
            AnnotationMark(
                modifier = Modifier
                    .offset(x = mark.bounds.left, y = mark.bounds.top)
                    .size(width = mark.bounds.width, height = mark.bounds.height),
                state = mark.state,
                badge = mark.badge,
                sizeLabel = mark.sizeLabel,
                showHandles = mark.showHandles,
            )
        }
    }
}

/** Preview-only marks: one pinned with a size label, one selected with handles. */
private val PreviewMarks = listOf(
    AnnotationOverlayMark(
        bounds = DpRect(left = 24.dp, top = 48.dp, right = 150.dp, bottom = 96.dp),
        badge = "1",
        sizeLabel = "545 × 254",
    ),
    AnnotationOverlayMark(
        bounds = DpRect(left = 120.dp, top = 112.dp, right = 236.dp, bottom = 156.dp),
        state = AnnotationState.Selected,
        badge = "2",
        showHandles = true,
    ),
)

/** Stands in for a mirrored frame so the previews show the overlay in its only valid context. */
@Composable
private fun OverlayPreview(mode: ThemeMode) {
    AppTheme(mode = mode) {
        Box(modifier = Modifier.size(width = PreviewFrameWidth, height = PreviewFrameHeight).background(Theme.color.surface)) {
            AnnotationOverlay(marks = PreviewMarks, dimmed = true)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnnotationOverlayLightPreview() {
    OverlayPreview(mode = ThemeMode.Light)
}

@Preview(showBackground = true)
@Composable
private fun AnnotationOverlayDarkPreview() {
    OverlayPreview(mode = ThemeMode.Dark)
}

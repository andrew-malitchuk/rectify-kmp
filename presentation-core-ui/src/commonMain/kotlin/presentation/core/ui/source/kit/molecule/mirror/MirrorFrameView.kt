package presentation.core.ui.source.kit.molecule.mirror

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/**
 * The mirrored device frame — one decoded screenshot, painted aspect-fit on the stage.
 *
 * Deliberately dumb and stateless: it is handed a finished [ImageBitmap] and draws it. It does
 * not decode, poll, scale, cache or know a frame rate, so the capture pipeline can change
 * entirely without touching this file, and a preview can show a frame with no device attached.
 *
 * Aspect-fit via [Modifier.aspectRatio] on the frame's own box rather than a `fillMaxSize`
 * image with [ContentScale.Fit]: the opaque backing must cover exactly where the frame lands
 * and nowhere else, so the stage's dot grid stays visible in the letterbox and is fully hidden
 * under the phone — a stage dot showing through a dark UI screenshot reads as a dead pixel.
 *
 * [FilterQuality.None] on purpose: this is a UI screenshot being inspected pixel by pixel, and
 * smoothing it would blur the 1 px hairlines the user is here to look at.
 *
 * @param frame The decoded device frame.
 * @param deviceName Device the frame came from; named in the accessibility description so the
 *   stage announces *which* device is mirrored, not just that something is.
 * @param modifier Modifier applied to the root composable.
 * @param overlay Content drawn on top of the frame — annotation marks and their scrim. Composed
 *   *inside* the aspect-fit box rather than over the whole stage, so the overlay's coordinate
 *   space and the painted pixels are literally the same rectangle and a mark cannot drift into
 *   the letterbox. Empty by default: a frame with nothing on it is the normal live case.
 */
@Composable
public fun MirrorFrameView(
    frame: ImageBitmap,
    deviceName: String,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val frameAspectRatio = frame.width.toFloat() / frame.height.toFloat()
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(ratio = frameAspectRatio, matchHeightConstraintsFirst = true)
                .background(Theme.color.ink)
                .semantics { contentDescription = "Live mirror of $deviceName" },
        ) {
            Image(
                painter = BitmapPainter(frame, filterQuality = FilterQuality.None),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            overlay()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MirrorFrameViewPreview() {
    AppTheme {
        Box(modifier = Modifier.background(Theme.color.stage).padding(Theme.spacing.spacingL)) {
            MirrorFrameView(
                frame = ImageBitmap(width = 1080, height = 2400),
                deviceName = "Pixel 8 Pro",
            )
        }
    }
}

package presentation.feature.mirror.core.crop

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import presentation.feature.mirror.source.mirror.CropEncoder
import presentation.feature.mirror.source.mirror.DeviceRect
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * Real [CropEncoder]: clamps [DeviceRect] to the frame's bounds first, then `ImageIO.write`s a
 * PNG off the main thread (TAC-1), returning `null` rather than throwing on failure.
 *
 */
internal class AwtCropEncoder : CropEncoder {

    override suspend fun encode(frame: ImageBitmap, rect: DeviceRect): ByteArray? =
        withContext(Dispatchers.IO) {
            val left = rect.left.coerceIn(0, frame.width)
            val top = rect.top.coerceIn(0, frame.height)
            val right = rect.right.coerceIn(left, frame.width)
            val bottom = rect.bottom.coerceIn(top, frame.height)
            // A rect entirely off one edge clamps to zero width or height; getSubimage would
            // throw on that, and the contract is null-on-failure.
            if (right <= left || bottom <= top) return@withContext null
            runCatching {
                val crop = frame.toAwtImage().getSubimage(left, top, right - left, bottom - top)
                ByteArrayOutputStream().use { sink ->
                    if (!ImageIO.write(crop, "png", sink)) return@runCatching null
                    sink.toByteArray()
                }
            }.getOrNull()
        }
}

package presentation.feature.mirror.source.mirror

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Encodes a cropped region of a captured frame to PNG bytes.
 *
 * Mirrors the `decodeFrame` seam on [MirrorViewModel]: an injectable function type (here a `fun
 * interface` instead of a lambda purely so the DI binding has a nameable type) so `commonTest`
 * never needs a real image backend, with exactly one production implementation — desktop's
 * `AwtCropEncoder` — and a null-returning stand-in on Android/iOS (AS-29).
 */
public fun interface CropEncoder {

    /**
     * Encodes the region of [frame] inside [rect] as PNG bytes, or `null` on failure.
     *
     * Never throws (T-5 / TAC-1): [rect] is clamped to [frame]'s bounds before encoding, and any
     * encode failure is reported as `null`, the same contract `decodeFrame` already uses for a
     * bad PNG.
     */
    public suspend fun encode(frame: ImageBitmap, rect: DeviceRect): ByteArray?
}

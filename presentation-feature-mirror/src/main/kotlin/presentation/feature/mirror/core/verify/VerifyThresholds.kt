package presentation.feature.mirror.core.verify

/**
 * Per-channel difference, out of 255, a pixel must EXCEED before it counts as changed.
 *
 * Antialiasing, subpixel text positioning and gamma wobble move a channel by a few counts
 * between two captures of the same unchanged screen; below this floor a "difference" says
 * nothing about whether the agent edited anything.
 *
 * **Ceiling:** a genuine but very low-contrast edit (a hairline divider at 4% opacity moving by
 * one pixel) stays invisible to the diff and reads as "no pixel evidence". Taken knowingly —
 * the hierarchy half of the verdict still sees it if it moved anything measurable.
 *
 * **Tuning (NFR-17 / MET-3):** raise it if unchanged regions report changed, lower it if real
 * edits go unnoticed. Measured by hand against real before/after captures; never pinned by a
 * test, which would turn a tuning knob into a breaking change.
 */
internal const val CHANNEL_NOISE_FLOOR: Int = 10

/**
 * Fraction of a region's pixels that must change before the pixels count as evidence of an edit.
 *
 * A 2×30 px text caret blinking inside a 200×60 px region is 0.5% — noise. A real layout fix
 * (padding, size, position, colour) moves far more than 2% of the region it is drawn in.
 *
 * **Ceiling:** an edit that only moves a region by a subpixel amount, or recolours a tiny part
 * of it, falls under this bar and contributes no pixel evidence.
 *
 * **Tuning (NFR-17 / MET-3):** same method as [CHANNEL_NOISE_FLOOR] — adjust against real
 * captures, never assert its literal value.
 */
internal const val REGION_CHANGE_RATIO: Float = 0.02f

/**
 * Region area, in pixels, below which the pixel ratio carries no information at all.
 *
 * In a region under 64 px (an 8×8 icon) a single antialiased pixel is already over 1.5%, so
 * [REGION_CHANGE_RATIO] would flip the verdict on a subpixel wobble in either direction.
 * Below this the pixel conjunct is short-circuited and the hierarchy half decides alone — see
 * [verdictOf], which is the only place this constant is read.
 *
 * **Ceiling:** a sub-64 px region whose finding vanished because a NEIGHBOUR moved reads Fixed
 * with zero changed pixels.
 *
 * **Tuning (NFR-17 / MET-3):** as above.
 */
internal const val MIN_REGION_PIXELS: Int = 64

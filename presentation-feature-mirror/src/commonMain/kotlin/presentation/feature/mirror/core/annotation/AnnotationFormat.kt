package presentation.feature.mirror.core.annotation

import presentation.feature.mirror.source.mirror.DeviceRect

/**
 * Multiplication sign used by every measurement in this design language.
 *
 * `×`, not the letter `x`: the shipped kit already writes `218×77 @ 1035,862` and `545 × 254`
 * in `AnnotationRow`, `CommentPopover` and `AnnotationMark`, and a stray ASCII `x` would read
 * as a different unit next to them.
 *
 * `internal` rather than `private` so `core/measure`'s own size label reuses the one glyph
 * constant instead of declaring a second `×` that could drift from this one.
 */
internal const val TIMES: Char = '×'

/**
 * The machine-readable position line, e.g. `218×77 @ 1035,862`.
 *
 * Exactly the shape `CommentPopover.meta` and `AnnotationRow.meta` document, so the sidebar row
 * and the popover over the frame cannot drift apart. Always device pixels (FR-8).
 *
 * @param rect Annotation bounds in device pixels.
 * @return The formatted line.
 */
internal fun metaLine(rect: DeviceRect): String =
    "${rect.width}$TIMES${rect.height} @ ${rect.left},${rect.top}"

/**
 * The size label painted on a mark's bottom edge, e.g. `545 × 254`.
 *
 * Spaced around the `×`, unlike [metaLine]: `AnnotationMark.sizeLabel` documents it that way
 * because it sits alone in a tag rather than ahead of a position (FR-6).
 *
 * @param rect Annotation bounds in device pixels.
 * @return The formatted label.
 */
internal fun sizeLabel(rect: DeviceRect): String = "${rect.width} $TIMES ${rect.height}"

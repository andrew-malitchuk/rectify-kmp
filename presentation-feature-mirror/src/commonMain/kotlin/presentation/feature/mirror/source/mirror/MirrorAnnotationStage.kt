package presentation.feature.mirror.source.mirror

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.unit.width
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.annotation.AnnotationState
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonSize
import presentation.core.ui.source.kit.atom.button.AppButtonVariant
import presentation.core.ui.source.kit.molecule.annotation.AnnotationOverlay
import presentation.core.ui.source.kit.molecule.annotation.AnnotationOverlayMark
import presentation.core.ui.source.kit.molecule.annotation.AnnotationPopover
import presentation.core.ui.source.kit.molecule.annotation.Ruler
import presentation.core.ui.source.kit.molecule.mirror.MirrorFrameView
import presentation.core.ui.source.kit.organism.annotation.CommentPopover
import presentation.feature.mirror.core.annotation.NOMINAL_POPOVER_HEIGHT_DP
import presentation.feature.mirror.core.annotation.NOMINAL_POPOVER_WIDTH_DP
import presentation.feature.mirror.core.annotation.anyOverlap
import presentation.feature.mirror.core.annotation.closesPopover
import presentation.feature.mirror.core.annotation.metaLine
import presentation.feature.mirror.core.annotation.sizeLabel
import presentation.feature.mirror.core.geometry.FrameGeometry
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_action_resume_live
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_annotation_delete
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_annotation_empty_comment
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_annotation_placeholder
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_annotation_save_hint

/**
 * Rotation applied to the horizontal [Ruler] to produce the vertical guide.
 *
 * 270 degrees about the top-left corner, then translated down by the strip's own length, which
 * puts device-pixel 0 at the top of the frame and the last pixel at the bottom. `Ruler`'s own
 * KDoc rules out authoring a second component for this: "the vertical ruler in the reference is
 * the same strip rotated by the host".
 */
private const val VERTICAL_RULER_ROTATION_DEGREES = 270f

/**
 * Popover footprint used for **clamping** and for the crowding test, from the shared nominal
 * figures in `core/annotation/PopoverCrowding.kt`.
 *
 * Nominal rather than measured for the reason stated there: measuring a popover in order to
 * decide where to put it is a circular layout dependency, and being a few dp out only moves a
 * popover that was already going to be clamped against an edge.
 */
private val PopoverNominalWidth: Dp = NOMINAL_POPOVER_WIDTH_DP.dp
private val PopoverNominalHeight: Dp = NOMINAL_POPOVER_HEIGHT_DP.dp

/**
 * Gap between a mark's bottom edge and the popover's tail.
 *
 * `CommentPopover` draws its own tail but does not reserve space above it, so without this the
 * tail would sit on the mark's border rather than pointing at it.
 */
private val PopoverTailGap: Dp = 6.dp

/**
 * Height one gutter row is laid out at.
 *
 * The **two-line** figure, not [PopoverNominalHeight]'s one-line 36 dp: `AnnotationPopover`
 * renders up to two lines of 19 dp body plus 8 dp above and below, so a stack pitched at the
 * one-line height would let a wrapped comment run into the row under it. The leader line still
 * aims at the one-line centre, which is exact for a single line and comfortably inside the
 * plate for two.
 */
private val GutterRowHeight: Dp = 54.dp

/** Preview-only stand-in frame; a real device screenshot is never available in a preview. */
private val PreviewDeviceSize = IntSize(width = 1080, height = 2400)

/**
 * The live mirror with the drag layer on it (AC-5).
 *
 * The drag in progress lives in `remember`, never in Orbit: a pointer stream produces an event
 * per frame of the gesture, and routing those through the reducer would mean a full state copy
 * each — the same discipline that keeps decoded frames out of [MirrorUiState]. Exactly one
 * reducer pass happens per gesture, at release, and only when the drag was meaningful.
 *
 * @param frame Live frame to draw on.
 * @param deviceName Device the frame came from.
 * @param onIntent Intent sink.
 * @param modifier Modifier applied to the root composable.
 */
@Composable
internal fun MirrorLiveStage(
    frame: ImageBitmap,
    deviceName: String,
    onIntent: (MirrorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    MirrorAnnotationStage(
        frame = frame,
        deviceName = deviceName,
        frozen = null,
        onIntent = onIntent,
        modifier = modifier,
    )
}

/**
 * The pinned frame with its scrim, marks and comment popover (AC-7).
 *
 * Shares the live stage's body, which is what makes AC-52 fall out for free: the drag layer is
 * still live here, so a second drag stages a second annotation against the same pinned bitmap
 * with no stage transition at all.
 *
 * @param frame The pinned frame — `MirrorViewModel.frozenFrame`, resolved by `MirrorHost`.
 * @param deviceName Device the frame came from.
 * @param frozen Annotations and selection for this frame.
 * @param onIntent Intent sink.
 * @param modifier Modifier applied to the root composable.
 */
@Composable
internal fun MirrorFrozenStage(
    frame: ImageBitmap,
    deviceName: String,
    frozen: FrozenFrame,
    onIntent: (MirrorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    MirrorAnnotationStage(
        frame = frame,
        deviceName = deviceName,
        frozen = frozen,
        onIntent = onIntent,
        modifier = modifier,
    )
}

/**
 * One stage, two appearances.
 *
 * Every coordinate conversion goes through [FrameGeometry] — there is no second one anywhere in
 * this file, which is the whole point of that type existing. The overlay box is sized with
 * `matchParentSize()` inside [MirrorFrameView]'s aspect-fit box, so the pixels `onSizeChanged`
 * reports, the pixels `pointerInput` reports and the painted frame are literally one rectangle.
 *
 * The drag detector sits on a **sibling** of the popover rather than on their common parent.
 * `detectDragGestures` starts on `awaitFirstDown(requireUnconsumed = false)`, so an ancestor of
 * the comment field would still draw a mark while the user drags to select text in it —
 * consumption cannot stop it, only hit-testing can. Compose stops hit-testing siblings once one
 * of them is hit, so the popover (which claims its whole plate through
 * [popoverPointerShield]) and the Resume Live button simply take the pointer, and everywhere
 * else the drag layer underneath gets it.
 */
@Composable
private fun MirrorAnnotationStage(
    frame: ImageBitmap,
    deviceName: String,
    frozen: FrozenFrame?,
    onIntent: (MirrorIntent) -> Unit,
    modifier: Modifier,
) {
    val deviceSize = remember(frame) { IntSize(width = frame.width, height = frame.height) }
    var framePx by remember { mutableStateOf(Size.Zero) }
    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragCurrent by remember { mutableStateOf<Offset?>(null) }

    val geometry = FrameGeometry(
        framePx = framePx,
        deviceSize = deviceSize,
        density = LocalDensity.current.density,
    )
    // The gesture block is keyed on Unit so it survives every frame; these two keep what it
    // reads current without restarting it mid-drag.
    val currentGeometry by rememberUpdatedState(geometry)
    val currentOnIntent by rememberUpdatedState(onIntent)

    val dragRect = dragStart?.let { start ->
        dragCurrent?.let { current -> geometry.toDevice(start, current) }
    }
    val frameBounds: DpRect = geometry.toDp(Rect(Offset.Zero, framePx))

    MirrorFrameView(frame = frame, deviceName = deviceName, modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .onSizeChanged { size -> framePx = size.toSize() },
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { start ->
                                dragStart = start
                                dragCurrent = start
                            },
                            onDrag = { change, _ -> dragCurrent = change.position },
                            onDragEnd = {
                                val start = dragStart
                                val end = dragCurrent
                                // The one and only reducer pass of the whole gesture.
                                if (start != null && end != null &&
                                    currentGeometry.isMeaningfulDrag(start, end)
                                ) {
                                    currentOnIntent(
                                        MirrorIntent.StageAnnotation(
                                            currentGeometry.toDevice(start, end),
                                        ),
                                    )
                                }
                                dragStart = null
                                dragCurrent = null
                            },
                            onDragCancel = {
                                dragStart = null
                                dragCurrent = null
                            },
                        )
                    },
            ) {
                AnnotationOverlay(
                    marks = overlayMarks(geometry = geometry, frozen = frozen, dragRect = dragRect),
                    dimmed = frozen != null,
                )

                if (dragRect != null) {
                    DragGuides(
                        deviceSize = deviceSize,
                        frameBounds = frameBounds,
                        dragRect = dragRect,
                    )
                }
            }

            frozen?.let { pinned ->
                if (anyOverlap(nominalPopoverRects(geometry = geometry, frozen = pinned))) {
                    AnnotationGutter(
                        geometry = geometry,
                        frameBounds = frameBounds,
                        frozen = pinned,
                    )
                }
                SelectedComment(
                    geometry = geometry,
                    frameBounds = frameBounds,
                    frozen = pinned,
                    onIntent = onIntent,
                )
                AppButton(
                    text = stringResource(Res.string.mirror_action_resume_live),
                    onClick = { onIntent(MirrorIntent.ResumeLive) },
                    variant = AppButtonVariant.Secondary,
                    size = AppButtonSize.Toolbar,
                    modifier = Modifier.align(Alignment.TopEnd).padding(Theme.spacing.spacingS),
                )
            }
        }
    }
}

/**
 * The marks to paint: every staged annotation, plus the one being drawn right now.
 *
 * The in-progress mark goes through the same overlay as the committed ones rather than being
 * drawn separately, which also means it punches its own hole in the frozen scrim — the user can
 * see the pixels they are selecting while they select them.
 *
 * [AnnotationState.Frozen] is deliberately never produced here: it paints an opaque plate, which
 * would hide the exact pixels an annotation points at. The dim is `AnnotationOverlay`'s scrim,
 * outside the marks.
 */
private fun overlayMarks(
    geometry: FrameGeometry,
    frozen: FrozenFrame?,
    dragRect: DeviceRect?,
): List<AnnotationOverlayMark> = buildList {
    frozen?.annotations?.forEach { annotation ->
        val isSelected = annotation.index == frozen.selectedIndex
        add(
            AnnotationOverlayMark(
                bounds = geometry.toDp(geometry.toFrame(annotation.rect)),
                state = if (isSelected) AnnotationState.Selected else AnnotationState.Pinned,
                badge = annotation.index.toString(),
                showHandles = isSelected,
            ),
        )
    }
    if (dragRect != null) {
        add(
            AnnotationOverlayMark(
                bounds = geometry.toDp(geometry.toFrame(dragRect)),
                state = AnnotationState.Drawing,
                sizeLabel = sizeLabel(dragRect),
            ),
        )
    }
}

/**
 * Where every comment popover *would* sit if it stayed on its mark — the input to the crowding
 * test (FR-9 / AC-9).
 *
 * Plain dp floats in the overlay's own space, matching what [SelectedComment] does with the one
 * popover it actually places. Nominal sizes, never measured: see [PopoverNominalWidth].
 */
private fun nominalPopoverRects(geometry: FrameGeometry, frozen: FrozenFrame): List<Rect> =
    frozen.annotations.map { annotation ->
        val mark = geometry.toDp(geometry.toFrame(annotation.rect))
        val top = (mark.bottom + PopoverTailGap).value
        Rect(
            left = mark.left.value,
            top = top,
            right = mark.left.value + NOMINAL_POPOVER_WIDTH_DP,
            bottom = top + NOMINAL_POPOVER_HEIGHT_DP,
        )
    }

/**
 * The right-edge gutter: comments that no longer fit on their marks, each on a leader line
 * (FR-9 / AC-9).
 *
 * Everything here is in the overlay's own coordinate space — the marks, the column and the
 * [Canvas] are all children of one box the size of the painted frame, so a leader line is a
 * straight line between two points in the same space with no cross-composable translation and
 * nothing to go wrong when the window is resized. The gutter therefore sits at the right edge
 * of the **frame**, not of the whole stage: moving it out into the letterbox would mean exactly
 * the coordinate translation this avoids.
 *
 * The annotation whose popover is open keeps its editable `CommentPopover` on the mark — it is
 * being typed into, and a read-only copy of it in the gutter would just be the same text twice.
 *
 * No 8 px elbow-stagger: explicitly deferred (the style guide's polish, not demo fidelity).
 */
@Composable
private fun BoxScope.AnnotationGutter(
    geometry: FrameGeometry,
    frameBounds: DpRect,
    frozen: FrozenFrame,
) {
    val relocated = frozen.annotations.filter { it.index != frozen.selectedIndex }
    if (relocated.isEmpty()) return

    val inset = Theme.spacing.spacingS
    val emptyLabel = stringResource(Res.string.mirror_annotation_empty_comment)
    val leaderColor = Theme.color.annotate
    val leaderWidth = Theme.stroke.thin
    val gutterLeft = (frameBounds.width - inset - PopoverNominalWidth).coerceAtLeast(Dp.Hairline)
    val lastTop = (frameBounds.height - GutterRowHeight).coerceAtLeast(Dp.Hairline)
    val slots = relocated.mapIndexed { position, annotation ->
        annotation to (inset + (GutterRowHeight + inset) * position).coerceAtMost(lastTop)
    }

    Canvas(modifier = Modifier.matchParentSize()) {
        slots.forEach { (annotation, top) ->
            val mark = geometry.toDp(geometry.toFrame(annotation.rect))
            drawLine(
                color = leaderColor,
                start = Offset(x = mark.right.toPx(), y = ((mark.top + mark.bottom) / 2f).toPx()),
                end = Offset(x = gutterLeft.toPx(), y = (top + PopoverNominalHeight / 2f).toPx()),
                strokeWidth = leaderWidth.toPx(),
            )
        }
    }

    slots.forEach { (annotation, top) ->
        AnnotationPopover(
            text = annotation.comment.ifEmpty { emptyLabel },
            index = annotation.index,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = gutterLeft, y = top)
                .width(PopoverNominalWidth),
        )
    }
}

/**
 * The two ruler guides, shown only while a drag is in flight.
 *
 * Both are the kit's horizontal [Ruler]; the vertical one is the same strip rotated. They are
 * fed `totalPx`/`extentPx` in **device** pixels, which is the unit `Ruler` already documents and
 * the unit the annotation is filed in, so no conversion happens here at all.
 *
 * The two strips overlap in the top-left corner square. That is cosmetic and deliberate:
 * insetting either one would mean its scale no longer matched the frame it measures, which is a
 * worse error than a shared corner.
 */
@Composable
private fun BoxScope.DragGuides(
    deviceSize: IntSize,
    frameBounds: DpRect,
    dragRect: DeviceRect,
) {
    val verticalLength = frameBounds.height

    Ruler(
        totalPx = deviceSize.height,
        extentPx = dragRect.top..dragRect.bottom,
        modifier = Modifier
            .align(Alignment.TopStart)
            .requiredWidth(verticalLength)
            .graphicsLayer {
                transformOrigin = TransformOrigin(pivotFractionX = 0f, pivotFractionY = 0f)
                rotationZ = VERTICAL_RULER_ROTATION_DEGREES
                // Rotating about the top-left corner sweeps the strip above the frame;
                // `size.width` is its own length, so this drops it back down the leading edge.
                translationY = size.width
            },
    )

    Ruler(
        totalPx = deviceSize.width,
        extentPx = dragRect.left..dragRect.right,
        modifier = Modifier.align(Alignment.TopStart),
    )
}

/**
 * The comment popover for the selected mark, if any (AC-7 / AC-8 / A11Y-2).
 *
 * `onClose` deletes rather than merely dismissing: FR-8 makes the popover's close control the
 * delete control. Dismissing without deleting is `↵`/`esc`, both of which land on
 * [MirrorIntent.SelectAnnotation]`(null)` and keep the annotation and the staged count — see
 * [closesPopover] for why they coincide and why no new dispatch layer is needed.
 *
 * The field is focused the moment the popover appears, and again whenever the selection moves
 * to another annotation, which is what makes the whole flow reachable without a pointer.
 *
 * Only `x`/`y` are clamped into the stage. `CommentPopover`'s tail inset is fixed, so on a mark
 * hard against the right edge the tail no longer points exactly at it: an accepted
 * demo-fidelity limitation rather than a reason to re-author the organism.
 */
@Composable
private fun BoxScope.SelectedComment(
    geometry: FrameGeometry,
    frameBounds: DpRect,
    frozen: FrozenFrame,
    onIntent: (MirrorIntent) -> Unit,
) {
    val selected = frozen.selected ?: return
    val markBounds = geometry.toDp(geometry.toFrame(selected.rect))
    val maxX = (frameBounds.width - PopoverNominalWidth).coerceAtLeast(Dp.Hairline)
    val maxY = (frameBounds.height - PopoverNominalHeight).coerceAtLeast(Dp.Hairline)
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(selected.index) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(
                x = markBounds.left.coerceIn(Dp.Hairline, maxX),
                y = (markBounds.bottom + PopoverTailGap).coerceIn(Dp.Hairline, maxY),
            )
            .onPreviewKeyEvent { event ->
                when {
                    event.type != KeyEventType.KeyDown -> false
                    !closesPopover(isMeta = event.isMetaPressed, key = event.key) -> false
                    else -> {
                        onIntent(MirrorIntent.SelectAnnotation(null))
                        true
                    }
                }
            }
            .popoverPointerShield(),
    ) {
        CommentPopover(
            value = selected.comment,
            onValueChange = { text ->
                onIntent(MirrorIntent.CommentAnnotation(index = selected.index, comment = text))
            },
            onClose = { onIntent(MirrorIntent.DeleteAnnotation(selected.index)) },
            index = selected.index,
            placeholder = stringResource(Res.string.mirror_annotation_placeholder),
            meta = metaLine(selected.rect),
            saveHint = stringResource(Res.string.mirror_annotation_save_hint),
            // Close IS delete here (FR-8), and the screen reader has to say which.
            closeLabel = stringResource(Res.string.mirror_annotation_delete),
            focusRequester = focusRequester,
        )
    }
}

/**
 * Claims the popover's whole plate for hit-testing so the drag layer underneath never sees a
 * pointer that landed on it.
 *
 * Consumes nothing — the comment field's own click, caret drag and text selection are
 * untouched. It exists purely so the parts of the plate that have no interactive child (the
 * padding, the meta line) still count as a hit, because Compose stops hit-testing siblings
 * beneath the first one that reports a hit, and that is the only mechanism that can stop
 * `detectDragGestures`' `requireUnconsumed = false` down.
 */
private fun Modifier.popoverPointerShield(): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
    }
}

/** A frame stand-in for the previews — no device is attached when one renders. */
private fun previewFrame(): ImageBitmap =
    ImageBitmap(width = PreviewDeviceSize.width, height = PreviewDeviceSize.height)

private fun previewFrozen(): FrozenFrame = FrozenFrame(
    deviceSize = PreviewDeviceSize,
    annotations = listOf(
        StagedAnnotation(
            index = 1,
            rect = DeviceRect(left = 120, top = 420, right = 960, bottom = 674),
            comment = "Row title clips at this width",
        ),
        StagedAnnotation(index = 2, rect = DeviceRect(left = 80, top = 1500, right = 298, bottom = 1577)),
    ),
    selectedIndex = 2,
)

/** Three marks stacked close enough that their popovers overlap — the FR-9 case. */
private fun previewCrowded(): FrozenFrame = FrozenFrame(
    deviceSize = PreviewDeviceSize,
    annotations = listOf(
        StagedAnnotation(
            index = 1,
            rect = DeviceRect(left = 120, top = 420, right = 520, bottom = 520),
            comment = "Row title clips at this width",
        ),
        StagedAnnotation(
            index = 2,
            rect = DeviceRect(left = 140, top = 560, right = 540, bottom = 660),
            comment = "Price sits lower than the title",
        ),
        StagedAnnotation(
            index = 3,
            rect = DeviceRect(left = 160, top = 700, right = 560, bottom = 800),
        ),
    ),
)

@Preview
@Composable
private fun MirrorLiveStagePreview() {
    AppTheme {
        Box(modifier = Modifier.background(Theme.color.stage)) {
            MirrorLiveStage(frame = previewFrame(), deviceName = "Pixel 8 Pro", onIntent = {})
        }
    }
}

@Preview
@Composable
private fun MirrorFrozenStagePreview() {
    AppTheme {
        Box(modifier = Modifier.background(Theme.color.stage)) {
            MirrorFrozenStage(
                frame = previewFrame(),
                deviceName = "Pixel 8 Pro",
                frozen = previewFrozen(),
                onIntent = {},
            )
        }
    }
}

@Preview
@Composable
private fun MirrorFrozenStageCrowdedPreview() {
    AppTheme {
        Box(modifier = Modifier.background(Theme.color.stage)) {
            MirrorFrozenStage(
                frame = previewFrame(),
                deviceName = "Pixel 8 Pro",
                frozen = previewCrowded(),
                onIntent = {},
            )
        }
    }
}

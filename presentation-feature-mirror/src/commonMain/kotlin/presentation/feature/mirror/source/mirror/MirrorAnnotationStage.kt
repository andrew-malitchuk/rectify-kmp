package presentation.feature.mirror.source.mirror

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed as isAltKeyDown
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.isAltPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.unit.width
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.annotation.AnnotationState
import presentation.core.ui.source.kit.atom.annotation.MeasureMark
import presentation.core.ui.source.kit.atom.annotation.MeasureMarkRole
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonSize
import presentation.core.ui.source.kit.atom.button.AppButtonVariant
import presentation.core.ui.source.kit.atom.tag.MeasureTag
import presentation.core.ui.source.kit.molecule.status.FeedbackStatus
import presentation.core.ui.source.kit.molecule.status.FeedbackBar
import presentation.core.ui.source.kit.molecule.annotation.AnnotationOverlay
import presentation.core.ui.source.kit.molecule.annotation.AnnotationOverlayMark
import presentation.core.ui.source.kit.molecule.annotation.AnnotationPopover
import presentation.core.ui.source.kit.molecule.annotation.Ruler
import presentation.core.ui.source.kit.molecule.mirror.MirrorFrameView
import presentation.core.ui.source.kit.organism.annotation.CommentPopover
import presentation.core.ui.source.kit.organism.annotation.MeasureCard
import presentation.core.ui.source.kit.organism.annotation.MeasureSpec
import presentation.feature.mirror.core.annotation.NOMINAL_POPOVER_HEIGHT_DP
import presentation.feature.mirror.core.annotation.NOMINAL_POPOVER_WIDTH_DP
import presentation.feature.mirror.core.annotation.POPOVER_TAIL_GAP_DP
import presentation.feature.mirror.core.annotation.closesPopover
import presentation.feature.mirror.core.annotation.metaLine
import presentation.feature.mirror.core.annotation.popoverLayout
import presentation.feature.mirror.core.annotation.sizeLabel
import presentation.feature.mirror.core.geometry.FrameGeometry
import presentation.feature.mirror.core.measure.MeasureDispatch
import presentation.feature.mirror.core.measure.annotationAt
import presentation.feature.mirror.core.measure.measureDispatch
import presentation.feature.mirror.core.measure.measureTarget
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_banner_stream_dropped
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_banner_annotations_kept
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_banner_reconnect
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_action_resume_live
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_annotation_delete
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_annotation_empty_comment
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_annotation_placeholder
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_annotation_save_hint
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_measure_footer
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_measure_hierarchy_dumping
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_measure_hierarchy_unavailable
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_measure_row_bottom
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_measure_row_left
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_measure_row_right
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_measure_row_size
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_measure_row_top
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_measure_title

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
private val PopoverTailGap: Dp = POPOVER_TAIL_GAP_DP.dp

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

/** Design: the stream-drop banner sits `top:14px` over the stale frame. */
private val BannerTopInset: Dp = 14.dp

/**
 * Width the floating Measuring panel is laid out at — the design's own 228 px.
 *
 * Named, and the only raw dp this spec adds at a call site: the `ErrorCardWidth` /
 * [BannerTopInset] precedent in this package. `MeasureCard` itself owns every other figure
 * (padding, gap, radius, border) as theme tokens, and clamps to its own 360 dp maximum, so the
 * caller supplies nothing but this width.
 */
private val MeasureCardWidth: Dp = 228.dp

/**
 * Dash pattern of a gap guide, in pixels — the same 4-on/3-off hairline `MeasureMark` outlines a
 * neighbour with, so the guide between two marks reads as part of the same drawing.
 *
 * Pixels and not dp because `PathEffect` takes pixels; it is a stroke pattern, not a coordinate,
 * and it goes nowhere near [FrameGeometry].
 */
private val GuideDashIntervals = floatArrayOf(4f, 3f)

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
        // Nothing is frozen, so there is nothing to resume from and nothing to keep.
        canResumeLive = false,
        banner = null,
        // Measuring needs a pinned frame and a hierarchy dump of it; neither exists while the
        // stream is live, which is also why `shouldMeasure` gates on `frozen`.
        measure = null,
        hierarchyStatus = HierarchyStatus.Absent,
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
 * @param measure The measuring readout in flight, or `null`.
 * @param hierarchyStatus Whether a hierarchy is available for this frame — the panel's footer.
 * @param onIntent Intent sink.
 * @param modifier Modifier applied to the root composable.
 */
@Composable
internal fun MirrorFrozenStage(
    frame: ImageBitmap,
    deviceName: String,
    frozen: FrozenFrame,
    canResumeLive: Boolean,
    banner: MirrorDropBanner?,
    measure: MeasureReadout?,
    hierarchyStatus: HierarchyStatus,
    onIntent: (MirrorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    MirrorAnnotationStage(
        frame = frame,
        deviceName = deviceName,
        frozen = frozen,
        canResumeLive = canResumeLive,
        banner = banner,
        measure = measure,
        hierarchyStatus = hierarchyStatus,
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
 *
 * ## The Alt layer (FR-36 / FR-44 / AC-71)
 *
 * Hover, `altHeld` and the last dispatched measure target are all `remember`ed, for the same
 * reason the drag is: a pointer event per frame of movement must not become a reducer pass per
 * frame. Only [MirrorIntent.StartMeasure] / [MirrorIntent.EndMeasure] reduce, and they are
 * dispatched on the **edge** of the remembered `(altHeld, target)` pair — see
 * [measureDispatch], which is where that decision lives so it can be tested without a renderer.
 *
 * Alt is detected **twice over**, deliberately. `onPreviewKeyEvent` needs focus and this stage
 * root requested none before this spec, so the primary sampler is focus-independent: a
 * `pointerInput` reading `event.keyboardModifiers.isAltPressed` at
 * [PointerEventPass.Initial], which supplies the hover position in the same pass. The key path
 * is kept as well, because a hold with no pointer movement produces no pointer events at all.
 * Release therefore has three paths — `KeyUp`, focus loss, and the next pointer-down — any one
 * of which clears `altHeld` and ends the measurement. A stuck Measuring panel (Cmd-Tab away
 * mid-hold) is the risk that redundancy buys off.
 */
@Composable
private fun MirrorAnnotationStage(
    frame: ImageBitmap,
    deviceName: String,
    frozen: FrozenFrame?,
    canResumeLive: Boolean,
    banner: MirrorDropBanner?,
    measure: MeasureReadout?,
    hierarchyStatus: HierarchyStatus,
    onIntent: (MirrorIntent) -> Unit,
    modifier: Modifier,
) {
    val deviceSize = remember(frame) { IntSize(width = frame.width, height = frame.height) }
    var framePx by remember { mutableStateOf(Size.Zero) }
    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragCurrent by remember { mutableStateOf<Offset?>(null) }
    var altHeld by remember { mutableStateOf(false) }
    var hoverPx by remember { mutableStateOf<Offset?>(null) }
    var measuringIndex by remember { mutableStateOf<Int?>(null) }
    val measureFocus = remember { FocusRequester() }

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

    // The pointer's own position, converted by the one [FrameGeometry] into the device pixels
    // the annotations are filed in, then hit-tested by the pure `annotationAt`. A degenerate
    // start==end rect is how a point goes through `toDevice`'s clamping and rounding.
    val hoverIndex = hoverPx?.let { point ->
        val devicePoint = geometry.toDevice(point, point)
        annotationAt(
            point = IntOffset(devicePoint.left, devicePoint.top),
            annotations = frozen?.annotations.orEmpty(),
        )?.index
    }
    val measureIndex = measureTarget(
        altHeld = altHeld,
        // FR-44 as reworded: Alt is IGNORED until the drag ends, never cancels it.
        dragInProgress = dragStart != null,
        frozen = frozen != null,
        hoverIndex = hoverIndex,
    )

    LaunchedEffect(measureIndex) {
        when (measureDispatch(active = measuringIndex, desired = measureIndex)) {
            MeasureDispatch.Start -> measureIndex?.let {
                currentOnIntent(MirrorIntent.StartMeasure(it))
            }
            MeasureDispatch.End -> currentOnIntent(MirrorIntent.EndMeasure)
            MeasureDispatch.None -> Unit
        }
        measuringIndex = measureIndex
    }

    // Requested ONCE, keyed on Unit: the stage root wants focus so the key path works at all,
    // but it must not keep taking it back. `CommentPopover` re-requests focus in its own
    // LaunchedEffect whenever it opens, so the popover wins while it is open (TAC-25).
    LaunchedEffect(Unit) { measureFocus.requestFocus() }

    MirrorFrameView(frame = frame, deviceName = deviceName, modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .onSizeChanged { size -> framePx = size.toSize() }
                .focusRequester(measureFocus)
                .focusable()
                // Release path 2: focus lost (Cmd-Tab, another window, a dialog) while the key
                // is still physically down. No KeyUp ever arrives in that case.
                .onFocusChanged { focus -> if (!focus.isFocused) altHeld = false }
                .onPreviewKeyEvent { event ->
                    // Release path 1. Alt's own KeyUp is taken as a release even on a host that
                    // still reports the modifier as pressed in the release event itself.
                    val altReleased = event.type == KeyEventType.KeyUp &&
                        (event.key == Key.AltLeft || event.key == Key.AltRight)
                    altHeld = event.isAltKeyDown && !altReleased
                    // Never consumed: every other shortcut on this stage, and the popover's own
                    // preview handler, must still see the event.
                    false
                }
                .pointerInput(Unit) {
                    // The focus-independent sampler. Initial pass, so it sees the pointer even
                    // when a child (the popover plate, the Resume Live button) takes the hit.
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val pressed = event.changes.any { it.pressed }
                            // Release path 3: the next pointer-down. A press cannot be part of
                            // a measure hold, so it clears the hold - and a genuinely still-held
                            // key re-asserts itself on the very next move event.
                            altHeld = !pressed && event.keyboardModifiers.isAltPressed
                            hoverPx = event.changes.lastOrNull()?.position
                        }
                    }
                },
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
                // Computed ONCE: the dim gate and the overlay must agree about what marks
                // exist, and calling overlayMarks twice is how they come to disagree.
                val marks = overlayMarks(geometry = geometry, frozen = frozen, dragRect = dragRect)
                AnnotationOverlay(
                    marks = marks,
                    // Gated on marks, not just on `frozen`: with nothing annotated there is
                    // nothing to punch out of the scrim, so dimming would hand the user a
                    // uniformly darkened screenshot with no indication why. The design gates
                    // its own dimming the same way (annotations.length > 0).
                    dimmed = frozen != null && marks.isNotEmpty(),
                )

                if (dragRect != null) {
                    DragGuides(
                        deviceSize = deviceSize,
                        frameBounds = frameBounds,
                        dragRect = dragRect,
                    )
                }

                // Inside the same frame-sized box as the annotation marks, so the measure marks,
                // the guide lines and the numerals are all one coordinate space with the
                // annotations they measure.
                measure?.let { readout -> MeasureOverlay(geometry = geometry, readout = readout) }
            }

            frozen?.let { pinned ->
                val layout = popoverLayout(geometry = geometry, frozen = pinned)
                AnchoredComments(
                    geometry = geometry,
                    frameBounds = frameBounds,
                    annotations = layout.anchored,
                )
                AnnotationGutter(
                    geometry = geometry,
                    frameBounds = frameBounds,
                    relocated = layout.gutter,
                )
                // Suppressed while measuring: the editable popover sits exactly where the
                // readout's own marks and numerals are, and it would also steal the focus the
                // key path needs for the duration of the hold.
                if (measure == null) {
                    SelectedComment(
                        geometry = geometry,
                        frameBounds = frameBounds,
                        frozen = pinned,
                        onIntent = onIntent,
                    )
                }
                // NFR-6. Resume Live CLEARS the frozen frame and every annotation on it -
                // deliberately, and F-4's AC-51 is confirmed against exactly that. So while
                // the session is Lost it must not be the only thing on offer, because then a
                // user whose cable fell out has one button and it destroys their work. The
                // handler is untouched; the affordance is what is withheld.
                if (canResumeLive) {
                    AppButton(
                        text = stringResource(Res.string.mirror_action_resume_live),
                        onClick = { onIntent(MirrorIntent.ResumeLive) },
                        variant = AppButtonVariant.Secondary,
                        size = AppButtonSize.Toolbar,
                        modifier = Modifier.align(Alignment.TopEnd).padding(Theme.spacing.spacingS),
                    )
                }
            }

            // FR-24 / AC-29. Top-centre, over the stale frame, per the design's
            // `top:14px; left:50%; translateX(-50%)`. It is `surface` rather than an error
            // fill - only the dot carries the red, the same restraint as StatusPill - because
            // it reports a recoverable state, not a failure.
            banner?.let { dropped ->
                FeedbackBar(
                    text = stringResource(Res.string.mirror_banner_stream_dropped),
                    status = FeedbackStatus.Error,
                    // COR-1. Suppressed at zero. dropBanner()'s KDoc delegates this to the
                    // content layer, and the content layer is here — without it the banner
                    // renders "Your 0 annotations are kept on the frozen frame.", which is the
                    // reassurance turned into an insult: it names the absence of the work it
                    // is claiming to protect. Reachable via ClearStaged on a pinned frame
                    // (AC-48 keeps the frame) followed by a drop.
                    detail = dropped.annotationCount
                        .takeIf { it > 0 }
                        ?.let { stringResource(Res.string.mirror_banner_annotations_kept, it) },
                    actionLabel = stringResource(Res.string.mirror_banner_reconnect),
                    onAction = { onIntent(MirrorIntent.Reconnect) },
                    actionVariant = AppButtonVariant.Primary,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = BannerTopInset),
                )
            }

            // FR-42 / AC-67. Fixed at TopStart with one inset, in the same frame-sized box the
            // banner and Resume Live float in. It is NOT flipped to TopEnd when the measured
            // node is underneath it: occlusion is accepted (step-2 gate ruling 3), because a
            // panel that jumps sides as the pointer moves is harder to read than one that
            // occasionally covers a corner of the frame.
            measure?.let { readout ->
                MeasureFloatingCard(readout = readout, hierarchyStatus = hierarchyStatus)
            }
        }
    }
}

/**
 * The floating Measuring panel (FR-42 / AC-67).
 *
 * Every figure, the node id and the finding arrive already formatted on [MeasureReadout] — this
 * supplies only the localised chrome (title, the five row labels, the footer sentence) and the
 * one width. Padding, gap, radius and border are the organism's own theme tokens.
 */
@Composable
private fun BoxScope.MeasureFloatingCard(
    readout: MeasureReadout,
    hierarchyStatus: HierarchyStatus,
) {
    MeasureCard(
        nodeId = readout.nodeId,
        specs = readout.values.map { value ->
            MeasureSpec(label = measureRowLabel(value.side), value = value.value)
        },
        title = stringResource(Res.string.mirror_measure_title),
        index = readout.annotationIndex,
        finding = readout.finding,
        footer = measureFooter(hierarchyStatus),
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(Theme.spacing.spacingL)
            .width(MeasureCardWidth),
    )
}

/** The panel's row labels. Localised chrome, unlike the figures beside them. */
@Composable
private fun measureRowLabel(side: MeasureSide): String = when (side) {
    MeasureSide.Size -> stringResource(Res.string.mirror_measure_row_size)
    MeasureSide.Top -> stringResource(Res.string.mirror_measure_row_top)
    MeasureSide.Right -> stringResource(Res.string.mirror_measure_row_right)
    MeasureSide.Bottom -> stringResource(Res.string.mirror_measure_row_bottom)
    MeasureSide.Left -> stringResource(Res.string.mirror_measure_row_left)
}

/**
 * The footer sentence — what becomes of these numbers, or why the hierarchy is not behind them.
 *
 * The pendency case (AC-72) and the no-hierarchy case (AC-69) are reported HERE rather than by
 * withholding the panel: every figure is still a real measurement in both, taken against the
 * annotation box and the frame edges (FR-41), so the honest UI is the same panel with a footer
 * that says what it was measured against.
 */
@Composable
private fun measureFooter(hierarchyStatus: HierarchyStatus): String = when (hierarchyStatus) {
    HierarchyStatus.Ready -> stringResource(Res.string.mirror_measure_footer)
    HierarchyStatus.Dumping -> stringResource(Res.string.mirror_measure_hierarchy_dumping)
    HierarchyStatus.Absent, HierarchyStatus.Unavailable ->
        stringResource(Res.string.mirror_measure_hierarchy_unavailable)
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
internal fun overlayMarks(
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
 * The read-only popovers that stay on their own marks — every non-selected annotation, whenever
 * the frame is not crowded (FR-8).
 *
 * Unconditional on purpose: a committed comment must not disappear from the stage just because
 * the selection moved to another mark. Crowding only decides whether this set sits here or in
 * [AnnotationGutter]; see `popoverLayout`.
 *
 * `AnnotationPopover` installs no pointer input, so these plates are invisible to hit-testing
 * and the drag layer underneath still starts a new annotation through them.
 */
@Composable
private fun BoxScope.AnchoredComments(
    geometry: FrameGeometry,
    frameBounds: DpRect,
    annotations: List<StagedAnnotation>,
) {
    if (annotations.isEmpty()) return

    val emptyLabel = stringResource(Res.string.mirror_annotation_empty_comment)
    val maxX = (frameBounds.width - PopoverNominalWidth).coerceAtLeast(Dp.Hairline)
    val maxY = (frameBounds.height - PopoverNominalHeight).coerceAtLeast(Dp.Hairline)

    annotations.forEach { annotation ->
        val mark = geometry.toDp(geometry.toFrame(annotation.rect))
        AnnotationPopover(
            text = annotation.comment.ifEmpty { emptyLabel },
            index = annotation.index,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(
                    x = mark.left.coerceIn(Dp.Hairline, maxX),
                    y = (mark.bottom + PopoverTailGap).coerceIn(Dp.Hairline, maxY),
                )
                .width(PopoverNominalWidth),
        )
    }
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
    relocated: List<StagedAnnotation>,
) {
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
 * The measuring overlay: the snapped target, its neighbours, the gap guides and their numerals
 * (FR-37 / FR-39 / FR-42 / AC-10).
 *
 * Nothing is computed here. [MeasureReadout] arrives fully measured and fully formatted from
 * `core/measure`, so this places rectangles and strings and nothing else — which is what keeps
 * the arithmetic testable without a renderer.
 *
 * Every coordinate makes exactly one trip through [FrameGeometry]: `toFrame` for the [Canvas],
 * which draws in the frame's own host pixels, and `toFrame` + `toDp` for anything positioned by
 * a modifier. There is no density term at either call site — adding one is the bug
 * [FrameGeometry]'s own KDoc records, where every mark came out at half size on a Retina host.
 *
 * The gap guides are dashed and measure-blue; the alignment rail is solid, and its numeral keeps
 * [MeasureTag]'s default annotate red, because the rail only ever appears for a misalignment
 * finding (AC-11) while the gap numerals are plain measurements.
 */
@Composable
private fun BoxScope.MeasureOverlay(
    geometry: FrameGeometry,
    readout: MeasureReadout,
) {
    val accent = Theme.color.measure
    val guideWidth = Theme.stroke.thin
    val railLabelLift = Theme.spacing.spacingL

    MeasureMark(
        role = MeasureMarkRole.Target,
        modifier = markModifier(geometry.toDp(geometry.toFrame(readout.targetRect))),
    )
    // Up to four: the nearest non-overlapping node on each side that exists. A 1px divider
    // already carries its height floor from the readout, so it stays a visible outline.
    readout.neighbourRects.forEach { neighbour ->
        MeasureMark(
            role = MeasureMarkRole.Neighbour,
            modifier = markModifier(geometry.toDp(geometry.toFrame(neighbour))),
        )
    }

    Canvas(modifier = Modifier.matchParentSize()) {
        val dashed = PathEffect.dashPathEffect(GuideDashIntervals, 0f)
        readout.guides.forEach { guide ->
            val line = geometry.guideFrame(guide)
            drawLine(
                color = accent,
                start = Offset(line.left, line.top),
                end = Offset(line.right, line.bottom),
                strokeWidth = guideWidth.toPx(),
                pathEffect = dashed,
            )
        }
        readout.rail?.let { rail ->
            val line = geometry.guideFrame(rail)
            drawLine(
                color = accent,
                start = Offset(line.left, line.top),
                end = Offset(line.right, line.bottom),
                strokeWidth = guideWidth.toPx(),
            )
        }
    }

    readout.guides.forEach { guide ->
        val bounds = geometry.toDp(geometry.guideFrame(guide))
        MeasureTag(
            text = guide.label,
            containerColor = accent,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(
                    x = (bounds.left + bounds.right) / 2f,
                    y = (bounds.top + bounds.bottom) / 2f,
                ),
        )
    }
    // The design puts the rail's label at `L.x, L.y - 16` — the neighbour's left edge, 16 above
    // its top. 16 dp is `spacingL` exactly, so no raw value is needed to say it.
    readout.rail?.let { rail ->
        val bounds = geometry.toDp(geometry.guideFrame(rail))
        MeasureTag(
            text = rail.label,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(
                    x = bounds.left,
                    y = (bounds.top - railLabelLift).coerceAtLeast(Dp.Hairline),
                ),
        )
    }
}

/** Places and sizes one measure mark from its already-converted bounds. */
private fun BoxScope.markModifier(bounds: DpRect): Modifier = Modifier
    .align(Alignment.TopStart)
    .offset(x = bounds.left, y = bounds.top)
    .size(width = bounds.width, height = bounds.height)

/**
 * One guide's two endpoints in frame (host) pixels, as a degenerate-on-one-axis rectangle.
 *
 * Every guide is axis-aligned and ordered `from -> to`, so `left`/`top` is the start and
 * `right`/`bottom` is the end, and both come out of the single [FrameGeometry.toFrame].
 */
private fun FrameGeometry.guideFrame(guide: MeasureGuide): Rect = toFrame(
    DeviceRect(
        left = guide.from.x,
        top = guide.from.y,
        right = guide.to.x,
        bottom = guide.to.y,
    ),
)

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

/**
 * A frozen stage mid-measurement — the only rendering check this repo has for AC-67 and NFR-9.
 *
 * The five figures, the finding and the node id are the design's own `item_price` readout
 * verbatim, including the `15dp` Top the design's `measure()` actually produces (the step-2 gate
 * addendum; the shipped `MeasureCard` preview had `9dp`, which that algorithm cannot produce).
 * The rects are preview placements on the 1080x2400 stand-in frame, not a re-derivation of the
 * design's 296x640 fixture — this is the panel and overlay being LOOKED at, not arithmetic being
 * checked. `core/measure`'s own tests are where the numbers are proved.
 */
private fun previewMeasure(): MeasureReadout = MeasureReadout(
    annotationIndex = 2,
    nodeId = "TextView#item_price",
    targetRect = DeviceRect(left = 80, top = 1500, right = 298, bottom = 1577),
    neighbourRects = listOf(
        DeviceRect(left = 0, top = 1500, right = 28, bottom = 1577),
        DeviceRect(left = 60, top = 1400, right = 320, bottom = 1460),
    ),
    values = listOf(
        MeasureValue(MeasureSide.Size, "48\u00d717dp"),
        MeasureValue(MeasureSide.Top, "15dp"),
        MeasureValue(MeasureSide.Right, "20dp"),
        MeasureValue(MeasureSide.Bottom, "49dp"),
        MeasureValue(MeasureSide.Left, "62dp"),
    ),
    guides = listOf(
        MeasureGuide(
            side = MeasureSide.Top,
            label = "15dp",
            from = IntOffset(189, 1460),
            to = IntOffset(189, 1500),
        ),
        MeasureGuide(
            side = MeasureSide.Left,
            label = "20dp",
            from = IntOffset(28, 1538),
            to = IntOffset(80, 1538),
        ),
    ),
    rail = MeasureGuide(
        side = MeasureSide.Top,
        label = "\u03946dp",
        from = IntOffset(60, 1400),
        to = IntOffset(298, 1400),
    ),
    finding = "top \u03946dp vs item_title \u00b7 expected 0",
    unit = MeasureUnit.Dp,
    noteLine = "\u2325 TextView#item_price 48\u00d717dp \u00b7 top 15dp",
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
                canResumeLive = true,
                banner = null,
                measure = null,
                hierarchyStatus = HierarchyStatus.Absent,
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
                // The dropped state, so the banner and the withheld Resume Live are both
                // visible in a preview - the only rendering check this repo has.
                canResumeLive = false,
                banner = MirrorDropBanner(deviceId = "37281FDH2000AB", annotationCount = 2),
                measure = null,
                hierarchyStatus = HierarchyStatus.Absent,
                onIntent = {},
            )
        }
    }
}

/**
 * The measuring stage, light and dark (AC-67 / NFR-9).
 *
 * A pair and not one preview with a toggle: the finding block is `annotate` at 10% over
 * `surface` and the node id is the `measure` signal colour, and those are exactly the two
 * composites that can go unreadable in one palette while looking right in the other.
 */
@Preview
@Composable
private fun MirrorFrozenStageMeasuringLightPreview() {
    AppTheme(mode = ThemeMode.Light) {
        Box(modifier = Modifier.background(Theme.color.stage)) {
            MirrorFrozenStage(
                frame = previewFrame(),
                deviceName = "Pixel 8 Pro",
                frozen = previewFrozen(),
                canResumeLive = true,
                banner = null,
                measure = previewMeasure(),
                hierarchyStatus = HierarchyStatus.Ready,
                onIntent = {},
            )
        }
    }
}

@Preview
@Composable
private fun MirrorFrozenStageMeasuringDarkPreview() {
    AppTheme(mode = ThemeMode.Dark) {
        Box(modifier = Modifier.background(Theme.color.stage)) {
            MirrorFrozenStage(
                frame = previewFrame(),
                deviceName = "Pixel 8 Pro",
                frozen = previewFrozen(),
                canResumeLive = true,
                banner = null,
                measure = previewMeasure(),
                hierarchyStatus = HierarchyStatus.Ready,
                onIntent = {},
            )
        }
    }
}

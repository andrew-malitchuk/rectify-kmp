package presentation.feature.mirror.source.mirror

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas as ImageCanvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import domain.core.source.model.device.HierarchySnapshotModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonSize
import presentation.core.ui.source.kit.atom.button.AppButtonVariant
import presentation.core.ui.source.kit.atom.input.MultilineInput
import presentation.feature.mirror.core.annotation.TIMES
import presentation.feature.mirror.core.payload.CropWindow
import presentation.feature.mirror.core.payload.buildPrompt
import presentation.feature.mirror.core.payload.cropWindow
import presentation.feature.mirror.core.payload.tokenEstimate
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_payload_empty
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_payload_prompt_edit
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_payload_prompt_edit_description

/** Tallest the prompt body grows before it scrolls — the design's own figure, no token fits. */
private val PromptBodyMaxHeight: Dp = 214.dp

/** Inset of the prompt body inside its code block; between `spacingS` and `spacingL`. */
private val PromptBodyPadding: Dp = 10.dp

/**
 * Section label of the prompt block. Machine text, deliberately **not** localised: it names the
 * block that gets pasted into Claude, and the surrounding readouts are machine text too.
 */
private const val PROMPT_LABEL = "PROMPT"

/**
 * Prefix of the token readout — `≈` (U+2248), not `=`. The count comes from [tokenEstimate], a
 * linear heuristic rather than a tokenizer, and the glyph is the only thing that says so.
 */
private const val TOKEN_PREFIX = "≈ "

/** Suffix of the token readout. Machine text, unpluralised for the same reason as the prefix. */
private const val TOKEN_SUFFIX = " tokens"

/** Section label of the crop grid. Machine text, for [PROMPT_LABEL]'s reason. */
private const val CROPS_LABEL = "CROPS"

/**
 * Prefix of the crop grid's frame readout. [FrozenFrame.frameId] is printed **bare** after it —
 * no padding, no `#`: it is a plain counter, and a zero-padded or hashed one would read as an id
 * the payload could be looked up by.
 */
private const val CROP_FRAME_PREFIX = "from frame "

/** Prefix of a crop caption's badge number, i.e. the `#` of `#2 · 218×77`. */
private const val CROP_BADGE_PREFIX = "#"

/** Separator between a crop's badge number and its size. */
private const val CROP_CAPTION_SEPARATOR = " · "

/**
 * Columns in the crop grid — the design's `repeat(3, minmax(0, 1fr))`.
 *
 * Weighted [Row]s are the Compose equivalent, which is why a short last row needs explicit
 * fillers: three weighted cells and two gaps, or two cells would each take half the panel.
 */
private const val CROP_COLUMNS = 3

/** Height of one crop cell. The design's own figure; no spacing token is 72 dp. */
private val CropCellHeight: Dp = 72.dp

/**
 * Width the design quotes for a crop cell. The cells themselves are **weighted**, so this sizes
 * only the crop handed to [cropWindow] — a cell a few dp wider than this shows the slack on its
 * right, which is exactly what drawing at `dstOffset = IntOffset.Zero` means.
 */
private val CropCellWidth: Dp = 84.dp

/** Corner radius of a crop cell; smaller than `Theme.corner.xs` (8 dp), which has no token. */
private val CropCellCorner: Dp = 6.dp

/**
 * The side panel's Payload tab: exactly what Claude receives for the pinned frame (FR-46).
 *
 * The tab is a **preview, not a form**. Everything it shows is derived from [MirrorUiState] and
 * the pinned [FrozenFrame], so it cannot drift from the payload that actually ships — the only
 * thing the user can change here is the prompt, and that change lands in state
 * ([MirrorIntent.EditPrompt] → [FrozenFrame.promptOverride]) rather than in a field this
 * composable owns.
 *
 * Zero staged annotations is the **first** thing handled, with a literal early return: there is
 * no payload to preview, and rendering an empty Prompt block over an empty crop strip would
 * imply one is being assembled. Same shape as [MirrorNotesTab]'s empty state.
 *
 * Scrolls rather than lazily composing, for [MirrorNotesTab]'s reason: a frozen frame carries a
 * handful of annotations, and the sections below the prompt are a fixed, small set.
 *
 * Still to land in this file: the hierarchy block and the payload footer.
 *
 * @param state Current mirror state; only [MirrorUiState.frozen] is read.
 * @param frame The pinned frame's pixels, or `null` when none were decoded. Second parameter to
 *   match the order of the host's content lambda. Nullable because the bitmap lives on a
 *   `StateFlow` outside the reduced state (see the module's `CLAUDE.md`), so it can lag a freeze
 *   by a frame; a `null` renders empty crop cells rather than inventing pixels.
 * @param hierarchy The pinned frame's view hierarchy, or [HierarchySnapshotModel.Empty] when
 *   none was captured. **Not read yet** — it is here for the hierarchy block, which resolves the
 *   node each annotation snapped to; it is already keyed into the prompt's `remember` so that
 *   block's arrival does not need to re-derive the key set.
 * @param onIntent Intent sink.
 * @param modifier Modifier applied to the root composable.
 */
@Composable
public fun MirrorPayloadTab(
    state: MirrorUiState,
    frame: ImageBitmap?,
    hierarchy: HierarchySnapshotModel,
    onIntent: (MirrorIntent) -> Unit,
    modifier: Modifier = Modifier,
): Unit {
    val frozen = state.frozen

    if (frozen == null || frozen.annotations.isEmpty()) {
        Text(
            text = stringResource(Res.string.mirror_payload_empty),
            style = Theme.typography.secondary,
            color = Theme.color.ink2,
            modifier = modifier.fillMaxWidth().padding(Theme.spacing.spacingL),
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(Theme.spacing.spacingS),
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingL),
    ) {
        PayloadPromptSection(frozen = frozen, hierarchy = hierarchy, onIntent = onIntent)
        PayloadCropsSection(frozen = frozen, frame = frame)
    }
}

/**
 * The Prompt block: label, token estimate, Edit toggle, and the prompt itself.
 *
 * The generated prompt is assembled inside a keyed [remember] and not per recomposition —
 * [buildPrompt] walks every annotation, and the panel recomposes on hover and on selection.
 * The keys are exactly what [buildPrompt] reads today plus [hierarchy], which it will read once
 * node ids resolve.
 *
 * An override **persists until Edit is clicked again**: it is not cleared when the annotation
 * list changes (AC-79). A prompt the user typed is the one piece of this payload a machine did
 * not write, and silently regenerating it on the next drag would throw it away at the exact
 * moment the user was still working. [MirrorIntent.ResumeLive] drops it, by dropping the frame.
 *
 * The Edit toggle is a local [remember] rather than a [MirrorUiState] field — which panel body
 * is showing is view state, invisible to the reducer and to the exported payload. It resets on a
 * new freeze because it is keyed on [FrozenFrame.frameId].
 *
 * @param frozen The pinned frame; guaranteed to carry at least one annotation.
 * @param hierarchy The frame's view hierarchy; a `remember` key only, for now.
 * @param onIntent Intent sink.
 */
@Composable
private fun PayloadPromptSection(
    frozen: FrozenFrame,
    hierarchy: HierarchySnapshotModel,
    onIntent: (MirrorIntent) -> Unit,
) {
    var isEditing by remember(frozen.frameId) { mutableStateOf(false) }

    val generated = remember(
        frozen.annotations,
        frozen.deviceLabel,
        frozen.deviceSize,
        hierarchy,
    ) {
        // ponytail: no node ids yet — buildPrompt prints nothing rather than a fake name, which
        // is exactly what its `nodeIdFor` contract asks for. The hierarchy block supplies the
        // real resolver.
        buildPrompt(frozen) { null }
    }
    val effective = frozen.promptOverride ?: generated

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            PayloadSectionLabel(text = PROMPT_LABEL)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = TOKEN_PREFIX + tokenEstimate(frozen.annotations.size) + TOKEN_SUFFIX,
                style = Theme.typography.monoTag,
                color = Theme.color.ink3,
            )
            val editDescription = stringResource(Res.string.mirror_payload_prompt_edit_description)
            AppButton(
                text = stringResource(Res.string.mirror_payload_prompt_edit),
                onClick = { isEditing = !isEditing },
                variant = AppButtonVariant.Ghost,
                // ListAction is the smallest preset that still clears the 24 dp touch target
                // (InlineRow is 22 dp).
                size = AppButtonSize.ListAction,
                modifier = Modifier.semantics { contentDescription = editDescription },
            )
        }

        if (isEditing) {
            // Seeded once per editing session, not from `effective`: re-seeding on every
            // keystroke would re-key the input's internal state as the override flows back.
            val seed = remember(isEditing) { effective }
            MultilineInput(
                modifier = Modifier.fillMaxWidth(),
                initialText = seed,
                onTextChanged = { onIntent(MirrorIntent.EditPrompt(it)) },
                textStyle = Theme.typography.monoData,
                maxHeight = PromptBodyMaxHeight,
            )
        } else {
            // Selectable, not editable: the prompt is copied out far more often than it is
            // rewritten, and a read-only field would still steal focus and the caret.
            SelectionContainer {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Theme.corner.xs)
                        .background(Theme.color.codeBg)
                        .border(
                            width = Theme.stroke.thin,
                            color = Theme.color.line,
                            shape = Theme.corner.xs,
                        )
                        .heightIn(max = PromptBodyMaxHeight)
                        .verticalScroll(rememberScrollState())
                        .padding(PromptBodyPadding),
                ) {
                    Text(
                        text = effective,
                        style = Theme.typography.monoData,
                        color = Theme.color.ink,
                    )
                }
            }
        }
    }
}

/**
 * A block's section label, e.g. `PROMPT`.
 *
 * Shared by every block in this tab rather than written out per block: the labels are a set, and
 * one of them drifting to a different weight or ink is the exact defect nobody notices. Each
 * block supplies its own trailing readout, which is the only part that genuinely differs.
 *
 * @param text The label. Machine text — see [PROMPT_LABEL].
 */
@Composable
private fun PayloadSectionLabel(text: String) {
    Text(
        text = text,
        style = Theme.typography.overline,
        color = Theme.color.ink3,
    )
}

/**
 * The Crops block: one cell per staged annotation, three to a row.
 *
 * Every number a cell draws with comes from [cropWindow] — this composable does no crop, scale or
 * clamp arithmetic of its own, which is why a rect that overhangs the frame edge cannot produce a
 * different crop here than the one the payload exports.
 *
 * The windows are built in a keyed [remember]: [cropWindow] runs once per annotation per freeze,
 * not once per recomposition, and the panel recomposes on hover and on selection. The cell size
 * is a key because it enters the fit — it is a density conversion of two constants, so it changes
 * only when the window moves to a different display.
 *
 * A short last row is padded with weighted [Spacer]s. Without them a trailing row of one would
 * stretch that cell across the whole panel and read as a different, larger crop.
 *
 * @param frozen The pinned frame; guaranteed to carry at least one annotation.
 * @param frame The frame's pixels, or `null` — see [MirrorPayloadTab].
 */
@Composable
private fun PayloadCropsSection(
    frozen: FrozenFrame,
    frame: ImageBitmap?,
) {
    val density = LocalDensity.current
    val cellWidth = with(density) { CropCellWidth.toPx() }
    val cellHeight = with(density) { CropCellHeight.toPx() }

    val windows = remember(frozen.annotations, frozen.deviceSize, cellWidth, cellHeight) {
        frozen.annotations.map { annotation ->
            cropWindow(
                rect = annotation.rect,
                frameSize = frozen.deviceSize,
                boxW = cellWidth,
                boxH = cellHeight,
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PayloadSectionLabel(text = CROPS_LABEL)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = CROP_FRAME_PREFIX + frozen.frameId,
                style = Theme.typography.monoTag,
                color = Theme.color.ink3,
            )
        }

        frozen.annotations.zip(windows).chunked(CROP_COLUMNS).forEach { cells ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
            ) {
                cells.forEach { (annotation, window) ->
                    PayloadCropCell(
                        annotation = annotation,
                        window = window,
                        frame = frame,
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(CROP_COLUMNS - cells.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * One crop cell: the annotated region of the frame, outlined, over its caption.
 *
 * A single [Canvas] draws both, from a single [CropWindow]: the blit and the outline cannot
 * disagree about where the annotation is because neither of them computes it. One `drawImage`
 * against the already-decoded bitmap — no copy, no per-cell decode, no [ImageBitmap] allocated
 * here at all — which is what keeps a grid of crops off the frame budget.
 *
 * `srcOffset`/`srcSize`/`dstSize` are [IntOffset]/[IntSize] while [CropWindow] is float-typed, so
 * the values are narrowed at this call site. That is a unit conversion, not geometry: nothing is
 * re-derived, re-fitted or re-clamped.
 *
 * A `null` [frame] draws **nothing** — the cell stays the flat stage colour. Not even the outline:
 * a lone rect over an empty cell would read as a measured region of a frame that is not there.
 *
 * The caption interpolates [TIMES] directly rather than reusing either module's size formatter —
 * one of those spaces the glyph for a standalone tag and the other appends a unit suffix, and
 * this caption wants neither (AC-72).
 *
 * @param annotation The annotation this cell crops to; read for its badge number and its size.
 * @param window The crop, already fitted by [cropWindow].
 * @param frame The frame's pixels, or `null`.
 * @param modifier Modifier applied to the cell; the grid passes the column weight.
 */
@Composable
private fun PayloadCropCell(
    annotation: StagedAnnotation,
    window: CropWindow,
    frame: ImageBitmap?,
    modifier: Modifier = Modifier,
) {
    val outlineColor = Theme.color.annotate
    val outlineWidth = Theme.stroke.thin

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CropCellHeight)
                .clip(RoundedCornerShape(CropCellCorner))
                .background(Theme.color.stage),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                if (frame == null) return@Canvas
                drawImage(
                    image = frame,
                    srcOffset = IntOffset(
                        x = window.srcOffset.x.toInt(),
                        y = window.srcOffset.y.toInt(),
                    ),
                    srcSize = IntSize(
                        width = window.srcSize.width.toInt(),
                        height = window.srcSize.height.toInt(),
                    ),
                    dstOffset = IntOffset.Zero,
                    dstSize = IntSize(
                        width = window.dstSize.width.toInt(),
                        height = window.dstSize.height.toInt(),
                    ),
                )
                drawRect(
                    color = outlineColor,
                    topLeft = window.outlineRect.topLeft,
                    size = window.outlineRect.size,
                    style = Stroke(width = outlineWidth.toPx()),
                )
            }
        }

        Text(
            text = CROP_BADGE_PREFIX + annotation.index + CROP_CAPTION_SEPARATOR +
                "${annotation.rect.width}$TIMES${annotation.rect.height}",
            style = Theme.typography.monoTag,
            color = Theme.color.ink3,
        )
    }
}

private val PreviewDeviceSize = IntSize(width = 1080, height = 2400)

private fun previewState(): MirrorUiState = MirrorUiState(
    isPickerOpen = false,
    frozen = FrozenFrame(
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
        frameId = 1,
        deviceLabel = "Android 15",
    ),
)

/**
 * A stand-in frame for the preview: a flat field with a lighter block behind each annotation.
 *
 * Previews are this repo's only visual evidence (there is no Compose UI-test harness), and a
 * transparent bitmap would leave the crop cells looking exactly like the `null` case. Painted
 * from theme inks rather than literal colours, and only inside a preview — nothing shipped
 * allocates an [ImageBitmap] in this file.
 */
@Composable
private fun rememberPreviewFrame(): ImageBitmap {
    val field = Theme.color.ink3
    val block = Theme.color.ink
    return remember(field, block) {
        val bitmap = ImageBitmap(PreviewDeviceSize.width, PreviewDeviceSize.height)
        val canvas = ImageCanvas(bitmap)
        val paint = Paint()
        paint.color = field
        canvas.drawRect(
            Rect(
                left = 0f,
                top = 0f,
                right = PreviewDeviceSize.width.toFloat(),
                bottom = PreviewDeviceSize.height.toFloat(),
            ),
            paint,
        )
        paint.color = block
        previewState().frozen?.annotations?.forEach { annotation ->
            canvas.drawRect(
                Rect(
                    left = annotation.rect.left.toFloat(),
                    top = annotation.rect.top.toFloat(),
                    right = annotation.rect.right.toFloat(),
                    bottom = annotation.rect.bottom.toFloat(),
                ),
                paint,
            )
        }
        bitmap
    }
}

@Preview(showBackground = true)
@Composable
private fun MirrorPayloadTabPreview() {
    AppTheme {
        Column(modifier = Modifier.background(Theme.color.surface)) {
            MirrorPayloadTab(
                state = previewState(),
                frame = rememberPreviewFrame(),
                hierarchy = HierarchySnapshotModel.Empty,
                onIntent = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MirrorPayloadTabEmptyPreview() {
    AppTheme {
        Column(modifier = Modifier.background(Theme.color.surface)) {
            MirrorPayloadTab(
                state = MirrorUiState(isPickerOpen = false),
                frame = null,
                hierarchy = HierarchySnapshotModel.Empty,
                onIntent = {},
            )
        }
    }
}

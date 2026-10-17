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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import domain.core.source.model.device.HierarchyNodeModel
import domain.core.source.model.device.HierarchySnapshotModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonSize
import presentation.core.ui.source.kit.atom.button.AppButtonVariant
import presentation.core.ui.source.kit.atom.input.MultilineInput
import presentation.core.ui.source.kit.molecule.row.HierarchyRow
import presentation.feature.mirror.core.annotation.TIMES
import presentation.feature.mirror.core.payload.CropWindow
import presentation.feature.mirror.core.payload.PayloadNodeRow
import presentation.feature.mirror.core.payload.buildPrompt
import presentation.feature.mirror.core.payload.cropWindow
import presentation.feature.mirror.core.payload.footerLabel
import presentation.feature.mirror.core.payload.hierarchySourceLabel
import presentation.feature.mirror.core.payload.payloadHierarchyRows
import presentation.feature.mirror.core.payload.payloadSizeLabel
import presentation.feature.mirror.core.payload.tokenEstimate
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_measure_hierarchy_unavailable
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

/** Section label of the hierarchy outline. Machine text, for [PROMPT_LABEL]'s reason. */
private const val HIERARCHY_LABEL = "HIERARCHY"

/** Prefix of an outline row's annotation tag, i.e. the `#` of `#2`. Same shape as [CROP_BADGE_PREFIX]. */
private const val HIERARCHY_TAG_PREFIX = "#"

/**
 * Tallest the hierarchy outline grows before it scrolls on its own.
 *
 * It exists because the outline is a [LazyColumn] inside a [verticalScroll]ed parent, which
 * hands it an **unbounded** height to measure in and throws. Capping the incoming constraint is
 * the same trick [PromptBodyMaxHeight] plays on the prompt body, and it is what keeps the lazy
 * win real: a ~400-node dump still composes only the rows this height can show.
 *
 * 13 rows at `HierarchyRow`'s shipped 26 dp — the largest block in the panel without pushing the
 * footer off an 820 dp window, and a whole number of rows so no row is clipped in half. That row
 * height is private to `HierarchyRow`, so this is a figure, not a derivation: if it changes, a
 * partly visible last row is the result, which reads as "there is more" anyway.
 */
private val HierarchyOutlineMaxHeight: Dp = 338.dp

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
 * The footer is **pinned**, not appended to the scrolling content: the sections live in an inner
 * `weight(1f)` [Column] that owns the scroll, and [PayloadFooter] sits below it. That is the
 * design's shape (a `flex:none` last child of a column whose content area scrolls) and the only
 * one that keeps the payload's size readable while the user is reading the prompt. It costs this
 * composable **one host contract**: the caller must give it a bounded height — a `weight` child
 * measured under an infinite height constraint collapses to zero. `SidePanelFrame` satisfies
 * that (its content slot is a child of a `fillMaxHeight` column), which is why the previews
 * below pin an explicit [PreviewPanelHeight] instead of wrapping the content.
 *
 * The hierarchy outline is the one block that is **not** a plain [Column] of children: it is a
 * [LazyColumn] with a capped height, because a dump runs to hundreds of nodes. See
 * [HierarchyOutlineMaxHeight].
 *
 * @param state Current mirror state; only [MirrorUiState.frozen] and
 *   [MirrorUiState.hierarchyStatus] are read.
 * @param frame The pinned frame's pixels, or `null` when none were decoded. Second parameter to
 *   match the order of the host's content lambda. Nullable because the bitmap lives on a
 *   `StateFlow` outside the reduced state (see the module's `CLAUDE.md`), so it can lag a freeze
 *   by a frame; a `null` renders empty crop cells rather than inventing pixels.
 * @param hierarchy The pinned frame's view hierarchy, or [HierarchySnapshotModel.Empty] when
 *   none was captured. Read **only** here — the hierarchy is dumped once at freeze and cached,
 *   and nothing in this tab can start a dump (TAC-5). An empty snapshot costs the outline its
 *   rows and nothing else: Prompt, Crops and the footer still render (AC-80).
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

    // Projected once per (hierarchy, annotation set) and shared by both blocks that need it:
    // the outline renders these rows, and the prompt reads the same mapping back to name the
    // node each annotation snapped to. `frozen.deviceSize` is not a key because it cannot
    // change without the annotation list changing with it — a new freeze stages none, and an
    // empty list never reaches here.
    val rows = remember(hierarchy, frozen.annotations) {
        payloadHierarchyRows(
            snapshot = hierarchy,
            frozenFrameSize = frozen.deviceSize,
            annotations = frozen.annotations.map { it.rect },
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(Theme.spacing.spacingS),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingL),
        ) {
            PayloadPromptSection(
                frozen = frozen,
                hierarchy = hierarchy,
                rows = rows,
                onIntent = onIntent,
            )
            PayloadCropsSection(frozen = frozen, frame = frame)
            PayloadHierarchySection(
                rows = rows,
                status = state.hierarchyStatus,
                nodeCount = hierarchy.nodes.size,
            )
        }
        PayloadFooter(count = frozen.annotations.size)
    }
}

/**
 * The Prompt block: label, token estimate, Edit toggle, and the prompt itself.
 *
 * The generated prompt is assembled inside a keyed [remember] and not per recomposition —
 * [buildPrompt] walks every annotation, and the panel recomposes on hover and on selection.
 * The keys are exactly what [buildPrompt] reads: the frame's own fields plus [hierarchy], which
 * is what [rows] — and therefore every node id in the prompt — is derived from.
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
 * @param hierarchy The frame's view hierarchy; a `remember` key — [rows] is its projection.
 * @param rows The outline rows, carrying the annotation -> node mapping the prompt names nodes
 *   from. Already computed by the caller, so the prompt does no snapping of its own.
 * @param onIntent Intent sink.
 */
@Composable
private fun PayloadPromptSection(
    frozen: FrozenFrame,
    hierarchy: HierarchySnapshotModel,
    rows: List<PayloadNodeRow>,
    onIntent: (MirrorIntent) -> Unit,
) {
    var isEditing by remember(frozen.frameId) { mutableStateOf(false) }

    val generated = remember(
        frozen.annotations,
        frozen.deviceLabel,
        frozen.deviceSize,
        hierarchy,
    ) {
        // The snap is already done: `PayloadNodeRow.badge` IS the annotation index that landed
        // on that node, so the resolver is the inverse of the rows rather than a second
        // projectMeasureNodes/snapTarget pass. `StagedAnnotation.index` is contiguous and
        // renumbered on delete (see its KDoc), which is exactly how badges are numbered.
        // Linear per annotation, and annotations are a handful.
        buildPrompt(frozen) { annotation ->
            rows.firstOrNull { row -> row.badge == annotation.index }?.nodeId
        }
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

/**
 * The Hierarchy block: label, source readout, and the dumped tree with the annotation badges
 * folded in (AC-15, AC-74, AC-75).
 *
 * Reads a snapshot that was already captured at freeze — it cannot dump, and it is handed
 * [rows] rather than a snapshot precisely so no arithmetic lives here (TAC-5).
 *
 * The rows are a [LazyColumn] with **no vertical arrangement spacing**, which `HierarchyRow`
 * requires: its tree guides are drawn, not typed, and they only meet when rows are adjacent.
 * Its height is capped — see [HierarchyOutlineMaxHeight] — both to measure at all inside the
 * tab's scroll and so a long dump composes only the visible window. The outline scrolls under
 * the pointer and the tab takes over at either end, which is standard nested-scroll behaviour.
 *
 * No `key` is given to [items]: a duplicate key is a crash, the dump's ids are only documented
 * unique, and the list never reorders in place — a new dump replaces it wholesale.
 *
 * **No rows is a supported state, not an error** (AC-80). [HierarchyStatus.Absent] and
 * [HierarchyStatus.Unavailable] get an explicit line saying so, because an empty area under a
 * label reads as a bug; [HierarchyStatus.Dumping] and a genuinely empty [HierarchyStatus.Ready]
 * dump get nothing extra, since the readout beside the label already says which it is. Either
 * way Prompt, Crops and the footer are untouched.
 *
 * @param rows The outline, projected by the caller; empty renders the no-hierarchy line.
 * @param status Hierarchy verdict for the pinned frame; drives the readout only.
 * @param nodeCount Size of the captured snapshot, for the readout.
 */
@Composable
private fun PayloadHierarchySection(
    rows: List<PayloadNodeRow>,
    status: HierarchyStatus,
    nodeCount: Int,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            PayloadSectionLabel(text = HIERARCHY_LABEL)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = hierarchySourceLabel(status, nodeCount),
                style = Theme.typography.monoTag,
                color = Theme.color.ink3,
            )
        }

        when {
            rows.isNotEmpty() -> LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = HierarchyOutlineMaxHeight),
            ) {
                items(rows) { row ->
                    HierarchyRow(
                        nodeId = row.nodeId,
                        depth = row.depth,
                        last = row.last,
                        tag = row.badge?.let { badge -> HIERARCHY_TAG_PREFIX + badge },
                        matched = row.matched,
                    )
                }
            }

            status == HierarchyStatus.Absent || status == HierarchyStatus.Unavailable -> Text(
                // Borrowed from the Measure tab rather than invented: it is the same verdict,
                // and the fallback it describes — measuring against the annotation box and the
                // frame edges — is exactly what the prompt above prints without node ids.
                text = stringResource(Res.string.mirror_measure_hierarchy_unavailable),
                style = Theme.typography.secondary,
                color = Theme.color.ink2,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * The payload footer: what ships on the left, what it weighs on the right (AC-16).
 *
 * Both strings come straight from `core/payload/PayloadEstimate.kt` — [footerLabel] and
 * [payloadSizeLabel] — and nothing here re-derives either one. That is the whole point of the
 * block: the readout and the exported payload share one source, so a displayed size that
 * disagrees with the real one is not expressible. It also makes AC-78 free — [count] is read
 * from state, so adding, removing or editing an annotation recomposes both labels in the same
 * frame as the crop grid, with no cache to invalidate.
 *
 * The left label is capped to one line with an ellipsis: it is machine text that grows with the
 * crop count, and a panel dragged narrow must not reflow the footer into two rows and shove the
 * size label off the bottom. The size label is unweighted, so it is measured first and keeps its
 * full width — the file list is what gives way.
 *
 * @param count Staged annotations on the pinned frame, i.e. the crop count. Not `frame.png` plus
 *   the hierarchy — [footerLabel] names those itself.
 */
@Composable
private fun PayloadFooter(count: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Same hairline as `ShellStatusBar`'s: a `line`-filled Box, not a `border`, so it spans
        // the full panel width while the row below it keeps its own inset.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Theme.stroke.thin)
                .background(Theme.color.line),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(Theme.spacing.spacingS),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = footerLabel(count),
                style = Theme.typography.monoMeta,
                color = Theme.color.ink3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                // `fill = false` so a short label leaves slack for SpaceBetween to distribute,
                // rather than swallowing the gap and pinning itself to the size label.
                modifier = Modifier.weight(1f, fill = false),
            )
            Text(
                text = payloadSizeLabel(count),
                style = Theme.typography.monoMeta,
                color = Theme.color.ink3,
                maxLines = 1,
            )
        }
    }
}

/*
 * Token mapping, recorded once (AC-14). Design figure on the left, what this tab's code actually
 * uses on the right. Reconciliation record only — nothing here re-skins the tab, and the rows
 * that do NOT match are written down as mismatches rather than asserted away.
 *
 *  design element                     | design             | code                             | verdict
 *  -----------------------------------+--------------------+----------------------------------+--------
 *  Section labels PROMPT/CROPS/       | 10.5px mono,       | typography.overline (10.5sp      | match
 *    HIERARCHY (PayloadSectionLabel)  | ls .08em, ink3     | mono, ls .08em) + color.ink3     |
 *  Prompt body (read + edit)          | 10.5px mono,       | typography.monoData (11sp mono)  | SIZE
 *                                     | codeBg bg,         | over color.codeBg, color.line    | DIFFERS
 *                                     | line border        | border                           | 11/10.5
 *  Crops caption (`#2 · 432x152`)     | 9.5px mono, ink3   | typography.monoTag (9.5sp) +     | match
 *                                     |                    | color.ink3                       |
 *  Crops `frame #N` tag               | ink2 (frame label) | typography.monoTag + color.ink3  | COLOUR
 *                                     |                    |                                  | DIFFERS
 *  Hierarchy row labels (HierarchyRow)| 10.5px mono,       | typography.monoData (11sp,       | NEAR:
 *                                     | line-height 17px   | 16sp line height)                | 11/16
 *  Hierarchy badges (MeasureTag)      | 9.5, white on red  | typography.monoTag (9.5sp),      | match
 *                                     |                    | white on color.annotate          |
 *  Footer, both halves                | 10.5px mono, ink3  | typography.monoMeta (10.5sp      | match
 *                                     |                    | mono) + color.ink3               |
 *  ink2 -> frame/agent labels         | ink2               | only the empty-state and the     | partial
 *                                     |                    | no-hierarchy line use ink2, and  |
 *                                     |                    | at typography.secondary (Roboto  |
 *                                     |                    | 12sp) — the figure has no row    |
 *                                     |                    | for either; `frame #N` is ink3   |
 *  ink -> matched nodes                | ink               | HierarchyRow matched -> ink      | match
 *  red -> crop outline, badges, WARN   | red               | outline and badges are           | match,
 *                                     |                    | color.annotate (one value in     | no WARN
 *                                     |                    | both palettes); this tab has no  | state
 *                                     |                    | WARN — the hierarchy verdict is  | here
 *                                     |                    | plain ink3 text, not a red badge |
 *
 * Two choices worth naming because they look like drift and are not: the footer keeps monoMeta
 * at 10.5sp per the table while crop captions sit on the 9.5sp monoTag scale — the design asks
 * for different scales in those two places. And the prompt body stays on monoData because that
 * is the repo's code/data scale (tabular figures); there is no 10.5sp mono-with-tabular-nums
 * token, and adding one to shave 0.5sp would be a re-skin, not this task.
 */

private val PreviewDeviceSize = IntSize(width = 1080, height = 2400)

/**
 * Height the previews give the tab, standing in for the panel's content area off the canonical
 * 820 dp window. Needed, not cosmetic: the pinned footer rides a `weight(1f)` content area, so a
 * height-wrapping preview would render the footer alone over a collapsed body.
 */
private val PreviewPanelHeight: Dp = 640.dp

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
            // Edge-touching on purpose (TAC-1): `left = 0` and a `bottom` on the frame's last
            // row, so `cropWindow`'s pad clamps on both axes and the preview shows crop
            // placement where the clamp actually engages, not only in open space.
            StagedAnnotation(index = 2, rect = DeviceRect(left = 0, top = 2248, right = 432, bottom = 2400)),
        ),
        selectedIndex = 2,
        frameId = 1,
        deviceLabel = "Android 15",
    ),
    hierarchyStatus = HierarchyStatus.Ready,
)

/**
 * A stand-in dump for the preview, with two nodes sitting exactly on the preview's annotation
 * rects so the outline shows real badges rather than a flat unmatched tree.
 */
private fun previewHierarchy(): HierarchySnapshotModel {
    fun node(id: String, left: Int, top: Int, right: Int, bottom: Int, depth: Int) =
        HierarchyNodeModel(
            id = id,
            className = "android.widget.FrameLayout",
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            isTappable = false,
            depth = depth,
        )

    return HierarchySnapshotModel(
        nodes = listOf(
            node("FrameLayout", 0, 0, 1080, 2400, depth = 0),
            node("LinearLayout#content", 0, 120, 1080, 2400, depth = 1),
            node("RecyclerView#list", 0, 300, 1080, 1900, depth = 2),
            node("ViewGroup#row_0", 80, 360, 1000, 614, depth = 3),
            // Matches annotation #1.
            node("TextView#item_title", 120, 420, 960, 674, depth = 4),
            node("ImageView#item_art", 120, 700, 360, 940, depth = 4),
            node("ViewGroup#row_1", 80, 980, 1000, 1240, depth = 3),
            node("TextView#row_1_label", 120, 1020, 960, 1080, depth = 4),
            node("ViewGroup#footer", 0, 1440, 1080, 1700, depth = 2),
            node("Button#buy_now", 80, 1500, 298, 1577, depth = 3),
            node("TextView#legal", 80, 1620, 1000, 1680, depth = 3),
            node("NavigationBar", 0, 2200, 1080, 2400, depth = 1),
            // Matches annotation #2 exactly, so `snapTarget` takes it over `NavigationBar`.
            node("Button#nav_back", 0, 2248, 432, 2400, depth = 2),
        ),
        frameWidthPx = PreviewDeviceSize.width,
        frameHeightPx = PreviewDeviceSize.height,
        densityDpi = 420,
    )
}

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

/**
 * Shared body for the six previews below, so each one differs only by theme mode and state.
 *
 * @param state The mirror state to render.
 * @param hierarchy The dump to render; [HierarchySnapshotModel.Empty] for the degraded pair.
 * @param frame The frame's pixels, or `null` for the empty pair.
 * @param modifier Carries [PreviewPanelHeight] for every preview that reaches the pinned footer.
 */
@Composable
private fun MirrorPayloadTabPreviewContent(
    state: MirrorUiState,
    hierarchy: HierarchySnapshotModel,
    frame: ImageBitmap?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.background(Theme.color.surface)) {
        MirrorPayloadTab(
            state = state,
            frame = frame,
            hierarchy = hierarchy,
            onIntent = {},
        )
    }
}

/**
 * Populated: two staged annotations, the second edge-touching so crop placement is visible where
 * the clamp engages (AC-14, AC-76), both captions carrying the unspaced glyph at a legible size
 * on a non-degenerate rect (AC-72, TAC-4).
 */
@Preview(showBackground = true)
@Composable
private fun MirrorPayloadTabPreviewLight() {
    AppTheme(mode = ThemeMode.Light) {
        MirrorPayloadTabPreviewContent(
            state = previewState(),
            hierarchy = previewHierarchy(),
            frame = rememberPreviewFrame(),
            modifier = Modifier.height(PreviewPanelHeight),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MirrorPayloadTabPreviewDark() {
    AppTheme(mode = ThemeMode.Dark) {
        MirrorPayloadTabPreviewContent(
            state = previewState(),
            hierarchy = previewHierarchy(),
            frame = rememberPreviewFrame(),
            modifier = Modifier.height(PreviewPanelHeight),
        )
    }
}

/**
 * The AC-80 degradation path, and the spec's **primary evidence** for it: no dump, yet Prompt,
 * Crops and the footer still render and the Hierarchy block says so in words. Forcing a real
 * device dump to fail on demand is not reproducible; this pair is.
 */
@Preview(showBackground = true)
@Composable
private fun MirrorPayloadTabNoHierarchyPreviewLight() {
    AppTheme(mode = ThemeMode.Light) {
        MirrorPayloadTabPreviewContent(
            state = previewState().copy(hierarchyStatus = HierarchyStatus.Unavailable),
            hierarchy = HierarchySnapshotModel.Empty,
            frame = rememberPreviewFrame(),
            modifier = Modifier.height(PreviewPanelHeight),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MirrorPayloadTabNoHierarchyPreviewDark() {
    AppTheme(mode = ThemeMode.Dark) {
        MirrorPayloadTabPreviewContent(
            state = previewState().copy(hierarchyStatus = HierarchyStatus.Unavailable),
            hierarchy = HierarchySnapshotModel.Empty,
            frame = rememberPreviewFrame(),
            modifier = Modifier.height(PreviewPanelHeight),
        )
    }
}

/**
 * Nothing staged (AC-77). No [PreviewPanelHeight]: the tab early-returns before the weighted
 * content area exists, so there is no `weight` child to collapse.
 */
@Preview(showBackground = true)
@Composable
private fun MirrorPayloadTabEmptyPreviewLight() {
    AppTheme(mode = ThemeMode.Light) {
        MirrorPayloadTabPreviewContent(
            state = MirrorUiState(isPickerOpen = false),
            hierarchy = HierarchySnapshotModel.Empty,
            frame = null,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MirrorPayloadTabEmptyPreviewDark() {
    AppTheme(mode = ThemeMode.Dark) {
        MirrorPayloadTabPreviewContent(
            state = MirrorUiState(isPickerOpen = false),
            hierarchy = HierarchySnapshotModel.Empty,
            frame = null,
        )
    }
}

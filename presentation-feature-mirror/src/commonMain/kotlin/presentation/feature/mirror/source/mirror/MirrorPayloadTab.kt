package presentation.feature.mirror.source.mirror

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
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
import presentation.feature.mirror.core.payload.buildPrompt
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
 * Still to land in this file: the crop strip, the hierarchy block and the payload footer.
 *
 * @param state Current mirror state; only [MirrorUiState.frozen] is read.
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
            Text(
                text = PROMPT_LABEL,
                style = Theme.typography.overline,
                color = Theme.color.ink3,
            )
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

@Preview(showBackground = true)
@Composable
private fun MirrorPayloadTabPreview() {
    AppTheme {
        Column(modifier = Modifier.background(Theme.color.surface)) {
            MirrorPayloadTab(
                state = previewState(),
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
                hierarchy = HierarchySnapshotModel.Empty,
                onIntent = {},
            )
        }
    }
}

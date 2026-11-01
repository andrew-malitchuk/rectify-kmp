package presentation.feature.mirror.source.mirror

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.molecule.row.AnnotationRow
import presentation.feature.mirror.core.annotation.metaLine
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_annotation_empty_comment
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_annotation_notes_empty

/**
 * The side panel's Notes tab: one [AnnotationRow] per staged annotation, in badge order (FR-28).
 *
 * The rows and the marks on the frame read from the same list and the same `selectedIndex`, so
 * clicking a row opens the popover on the frame and the row highlights with it — there is no
 * second selection model here to drift out of step.
 *
 * This is **not** the FR-9 gutter. The gutter relocates crowded popovers inside the stage, in
 * the overlay's own coordinate space; this is the permanent list beside it. Rendering the
 * gutter here instead would draw every annotation twice and run its leader lines across two
 * composables.
 *
 * Scrolls rather than lazily composing: a frozen frame carries a handful of annotations, and a
 * `LazyColumn` inside a resizable panel buys nothing but an intrinsic-height argument.
 *
 * @param state Current mirror state; only [MirrorUiState.frozen] is read.
 * @param onIntent Intent sink.
 * @param modifier Modifier applied to the root composable.
 */
@Composable
public fun MirrorNotesTab(
    state: MirrorUiState,
    onIntent: (MirrorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val annotations = state.frozen?.annotations.orEmpty()

    if (annotations.isEmpty()) {
        Text(
            text = stringResource(Res.string.mirror_annotation_notes_empty),
            style = Theme.typography.secondary,
            color = Theme.color.ink2,
            modifier = modifier.fillMaxWidth().padding(Theme.spacing.spacingL),
        )
        return
    }

    val emptyLabel = stringResource(Res.string.mirror_annotation_empty_comment)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(Theme.spacing.spacingS),
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS),
    ) {
        annotations.forEach { annotation ->
            AnnotationRow(
                index = annotation.index,
                meta = metaLine(annotation.rect),
                onClick = { onIntent(MirrorIntent.SelectAnnotation(annotation.index)) },
                comment = annotation.comment.ifEmpty { null },
                selected = annotation.index == state.frozen?.selectedIndex,
                emptyLabel = emptyLabel,
            )
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
    ),
)

@Preview(showBackground = true)
@Composable
private fun MirrorNotesTabPreview() {
    AppTheme {
        Column(modifier = Modifier.background(Theme.color.surface)) {
            MirrorNotesTab(state = previewState(), onIntent = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MirrorNotesTabEmptyPreview() {
    AppTheme {
        Column(modifier = Modifier.background(Theme.color.surface)) {
            MirrorNotesTab(state = MirrorUiState(isPickerOpen = false), onIntent = {})
        }
    }
}

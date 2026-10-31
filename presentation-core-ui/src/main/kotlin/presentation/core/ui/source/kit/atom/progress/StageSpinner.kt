package presentation.core.ui.source.kit.atom.progress

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme

/** Stage scale — larger than `AppButton`'s inline spinner, which is sized to its text. */
private val SpinnerSize: Dp = 28.dp
private val SpinnerStroke: Dp = 2.dp

/**
 * The kit's one "work is in flight" mark for a whole stage.
 *
 * Exists so the two stages that wait — the capture loop starting, and a verification run
 * recapturing — show the SAME thing. They were drifting: one had a spinner, the other had a
 * static status pill alone on an empty stage, which reads as stalled rather than busy.
 *
 * Material's indicator rather than a hand-rolled one: `AppButton` already ships it for its
 * busy state, so this adds no dependency and no second animation to keep in sync.
 */
@Composable
public fun StageSpinner(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier.size(SpinnerSize),
        color = Theme.color.ink2,
        strokeWidth = SpinnerStroke,
    )
}

@Preview(showBackground = true)
@Composable
private fun StageSpinnerPreviewLight() {
    AppTheme(mode = ThemeMode.Light) { StageSpinner() }
}

@Preview(showBackground = true)
@Composable
private fun StageSpinnerPreviewDark() {
    AppTheme(mode = ThemeMode.Dark) { StageSpinner() }
}

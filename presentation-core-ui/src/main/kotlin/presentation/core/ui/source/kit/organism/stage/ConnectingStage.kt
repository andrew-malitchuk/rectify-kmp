package presentation.core.ui.source.kit.organism.stage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.progress.StageSpinner

/**
 * The gap between "a capture loop started" and "the first frame decoded".
 *
 * The stage used to render nothing at all here, on the reasoning that a one-frame flash of
 * "no device" copy would contradict the status bar. That reasoning was right about the copy
 * and wrong about the conclusion: an empty dot grid under a status bar reading `LIVE` is its
 * own contradiction, and on a cold `adb` start the gap is long enough to read as a hang. A
 * spinner says the one thing that is actually true for the whole interval — work is in flight.
 *
 * @param label Copy under the spinner; the caller localises it.
 * @param modifier Modifier applied to the column.
 */
@Composable
public fun ConnectingStage(
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingL),
    ) {
        StageSpinner()
        Text(text = label, style = Theme.typography.body, color = Theme.color.ink2)
    }
}

@Composable
private fun ConnectingStagePreviewContent() {
    ConnectingStage(label = "Starting the capture loop…")
}

@Preview(showBackground = true)
@Composable
private fun ConnectingStagePreviewLight() {
    AppTheme(mode = ThemeMode.Light) { ConnectingStagePreviewContent() }
}

@Preview(showBackground = true)
@Composable
private fun ConnectingStagePreviewDark() {
    AppTheme(mode = ThemeMode.Dark) { ConnectingStagePreviewContent() }
}

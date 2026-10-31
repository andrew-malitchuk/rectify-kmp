package presentation.core.ui.source.kit.organism.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.indicator.StatusIndicator
import presentation.core.ui.source.kit.atom.indicator.StatusIndicatorState

/**
 * The window's bottom chrome bar — one line of machine state, nothing interactive.
 *
 * Two slots either side of a spacer, and no opinion about what goes in them: the shell has no
 * device or tool detection of its own (AC-37), so the pipeline state on the left and the build
 * marker on the right are both the caller's to supply.
 *
 * @param leading Left-aligned content — usually the pipeline's current state.
 * @param trailing Right-aligned content — usually the build / version marker.
 * @param modifier Modifier applied to the root composable.
 */
@Composable
public fun ShellStatusBar(
    leading: @Composable RowScope.() -> Unit,
    trailing: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().background(Theme.color.chrome)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Theme.stroke.thin)
                .background(Theme.color.line),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ShellWindowDefaults.StatusBarHeight)
                .padding(horizontal = Theme.spacing.spacingL),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading()
            Spacer(modifier = Modifier.weight(1f))
            trailing()
        }
    }
}

/**
 * Comp 02's status bar: no device yet, so the pipeline reports the neutral waiting state.
 *
 * Comp 01's green `adb … ready` line is deliberately not modelled — it needs tool detection
 * this spec does not build.
 */
@Composable
private fun ShellStatusBarWaitingPreviewContent() {
    ShellStatusBar(
        leading = {
            StatusIndicator(state = StatusIndicatorState.Offline, label = "Waiting for device…")
        },
        trailing = {
            Text(
                text = "Rectify 0.4",
                style = Theme.typography.monoMeta,
                color = Theme.color.ink2,
                maxLines = 1,
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun ShellStatusBarPreviewLight() {
    AppTheme(mode = ThemeMode.Light) {
        ShellStatusBarWaitingPreviewContent()
    }
}

@Preview(showBackground = true)
@Composable
private fun ShellStatusBarPreviewDark() {
    AppTheme(mode = ThemeMode.Dark) {
        ShellStatusBarWaitingPreviewContent()
    }
}

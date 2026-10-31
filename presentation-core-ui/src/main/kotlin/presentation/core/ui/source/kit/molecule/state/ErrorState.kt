package presentation.core.ui.source.kit.molecule.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.icon.AlertTriangle

/**
 * A full-screen error placeholder showing a warning icon, a title, and a descriptive message.
 *
 * @param title Primary error headline displayed below the icon.
 * @param description Secondary explanatory text displayed below the title.
 * @param modifier Modifier to be applied to the root [Column].
 *
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
public fun ErrorState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = AlertTriangle,
            contentDescription = null,
            tint = Theme.color.primary,
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.height(Theme.spacing.spacingS))
        Text(
            text = title,
            style = Theme.typography.titleS,
            color = Theme.color.ink,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Theme.spacing.spacingS))
        Text(
            text = description,
            style = Theme.typography.body,
            color = Theme.color.ink2,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorStatePreview() {
    AppTheme {
        ErrorState(
            title = "CONNECTION FAILED",
            description = "Unable to reach the device. Check that Bluetooth is enabled and the device is nearby.",
        )
    }
}

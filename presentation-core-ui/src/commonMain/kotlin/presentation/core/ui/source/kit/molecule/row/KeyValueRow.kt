package presentation.core.ui.source.kit.molecule.row

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/**
 * State of a [KeyValueRow]'s value.
 */
public enum class KeyValueState {
    /** A settled value. */
    Neutral,

    /** Still being produced. */
    Busy,

    /** Produced and confirmed. */
    Done,
}

/**
 * A label and its value — `Frame  1344×2992 PNG`, `Hierarchy  412 nodes ✓`.
 *
 * The value is always mono and the label never is: the label is what the app calls the field,
 * the value is what the device reported.
 *
 * @param label Field name.
 * @param value The reported value, already formatted with its unit.
 * @param modifier Modifier applied to the root composable.
 * @param state Whether the value is settled, in flight, or confirmed.
 */
@Composable
public fun KeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    state: KeyValueState = KeyValueState.Neutral,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Theme.spacing.spacingL, vertical = Theme.spacing.spacingS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = Theme.typography.body,
            color = Theme.color.ink,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            // A confirmed value carries its own tick, so the row needs no extra icon column.
            text = if (state == KeyValueState.Done) "$value ✓" else value,
            style = Theme.typography.monoData,
            color = state.valueColor(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun KeyValueState.valueColor(): Color = when (this) {
    KeyValueState.Neutral -> Theme.color.ink
    KeyValueState.Busy -> Theme.color.warnStillOff
    KeyValueState.Done -> Theme.color.sentFixed
}

@Preview(showBackground = true)
@Composable
private fun KeyValueRowPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(Theme.color.surface)
                .padding(vertical = Theme.spacing.spacingS),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS),
        ) {
            KeyValueRow(label = "Frame", value = "1344×2992 PNG")
            KeyValueRow(label = "Hierarchy", value = "dumping…", state = KeyValueState.Busy)
            KeyValueRow(label = "Hierarchy", value = "412 nodes", state = KeyValueState.Done)
        }
    }
}

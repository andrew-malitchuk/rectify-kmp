package presentation.core.ui.source.kit.molecule.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.input.FilledTextField

/**
 * A generic form field wrapper with a label above the [content] slot and an optional
 * [errorMessage] below it.
 *
 * Composes cleanly with any field composable (text inputs, pickers, toggles).
 *
 * @param label Field label text shown above [content].
 * @param errorMessage Validation error shown below [content]; `null` hides the error row.
 * @param content The field composable placed between the label and the error.
 *
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
public fun FormSection(
    label: String,
    errorMessage: String?,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS),
    ) {
        Text(
            text = label,
            style = Theme.typography.overline,
            color = Theme.color.ink2,
        )
        content()
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = Theme.typography.comment,
                color = Theme.color.primary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FormSectionPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Theme.spacing.spacingL),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingL),
        ) {
            FormSection(label = "COMMAND NAME", errorMessage = null) {
                FilledTextField(
                    value = "MUTE",
                    onValueChange = {},
                    placeholder = "Enter command name",
                )
            }
            FormSection(label = "COMMAND STRING", errorMessage = "Command string is required") {
                FilledTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = "e.g. KEY_MUTE",
                )
            }
        }
    }
}

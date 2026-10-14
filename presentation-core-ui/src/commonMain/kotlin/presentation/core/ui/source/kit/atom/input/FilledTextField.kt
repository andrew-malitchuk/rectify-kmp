package presentation.core.ui.source.kit.atom.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/**
 * A single-line text input with a solid fill background and no visible border.
 *
 * Designed for compact form fields where a filled appearance is preferred over the
 * animated-border style of [Input].
 *
 * @param value Current text value.
 * @param onValueChange Called when the user modifies the text.
 * @param placeholder Hint text displayed when [value] is empty.
 * @param modifier Modifier applied to the underlying [BasicTextField].
 * @param imeAction Keyboard IME action (default [ImeAction.Done]).
 *
 * @see Input
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
public fun FilledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Done,
) {
    val inkMain = Theme.color.ink
    val inkSubtle = Theme.color.ink2
    val brand = Theme.color.primary
    val body = Theme.typography.body

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = body.copy(color = inkMain),
        cursorBrush = SolidColor(brand),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            imeAction = imeAction,
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(Theme.cornerToken.input)
            .background(Theme.color.stage)
            .padding(
                horizontal = Theme.spacing.spacingL,
                vertical = Theme.spacing.spacingS,
            ),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = body,
                        color = inkSubtle,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun FilledTextFieldPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Theme.spacing.spacingL),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            FilledTextField(
                value = "KEY_MUTE",
                onValueChange = {},
                placeholder = "Enter command string",
            )
            FilledTextField(
                value = "",
                onValueChange = {},
                placeholder = "e.g. KEY_VOLUMEUP",
            )
        }
    }
}

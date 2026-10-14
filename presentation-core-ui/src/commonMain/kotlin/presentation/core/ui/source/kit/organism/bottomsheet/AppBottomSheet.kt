package presentation.core.ui.source.kit.organism.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/**
 * App-styled modal bottom sheet with a squircle top shape, optional title, and
 * navigation-bar-safe content area.
 *
 * @param onDismiss Called when the user dismisses the sheet (swipe-down or scrim tap).
 * @param modifier Modifier to be applied to the root [ModalBottomSheet].
 * @param title Optional headline rendered at the top of the sheet content.
 * @param content Slot for the sheet body; receives a [ColumnScope].
 *
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun AppBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = Theme.cornerToken.sheet,
        containerColor = Theme.color.surface,
        contentColor = Theme.color.ink,
        // Not part of the Rectify UI Kit token sheet — a modal scrim is a plain black dim,
        // not a themed color.
        scrimColor = Color.Black.copy(alpha = 0.5f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Theme.spacing.spacingS),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 2.dp)
                        .background(color = Theme.color.line),
                )
            }
        },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Theme.spacing.spacingL)
                .padding(top = Theme.spacing.spacingS)
                .navigationBarsPadding(),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = Theme.typography.titleL,
                    color = Theme.color.ink,
                )
                Spacer(modifier = Modifier.height(Theme.spacing.spacingL))
            }
            content()
            Spacer(modifier = Modifier.height(Theme.spacing.spacingL))
        }
    }
}

@Preview
@Composable
private fun AppBottomSheetPreview() {
    AppTheme {
        AppBottomSheet(
            onDismiss = {},
            title = "SELECT DEVICE",
        ) {
            androidx.compose.material3.Text(
                text = "Choose a Bluetooth device to connect to RECTIFY.",
                style = Theme.typography.body,
                color = Theme.color.ink2,
            )
        }
    }
}

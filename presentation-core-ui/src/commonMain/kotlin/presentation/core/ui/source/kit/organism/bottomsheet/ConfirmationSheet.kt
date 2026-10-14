package presentation.core.ui.source.kit.organism.bottomsheet

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.divider.HorizontalAnimatedDivider

/**
 * Standardised confirmation bottom sheet for the RECTIFY design system.
 *
 * Replaces platform `AlertDialog` / `TextButton` patterns with an app-styled
 * [AppBottomSheet] containing:
 * - A **body** description text.
 * - A primary [AppButton] for the affirmative action.
 * - A ghost/outline secondary button for the dismissal action.
 *
 * Usage:
 * ```kotlin
 * if (state.showConfirmation) {
 *     ConfirmationSheet(
 *         title    = "CONNECT",
 *         body     = "Pair with device XYZ?",
 *         confirmText  = "CONFIRM",
 *         dismissText  = "CANCEL",
 *         onConfirm = { onIntent(Intent.OnConfirmed) },
 *         onDismiss = { onIntent(Intent.OnDismissed) },
 *     )
 * }
 * ```
 *
 * @param title        Headline displayed at the top of the sheet.
 * @param body         Explanatory text shown below the title.
 * @param confirmText  Label for the primary affirmative button.
 * @param dismissText  Label for the secondary ghost button.
 * @param onConfirm    Callback for the affirmative action.
 * @param onDismiss    Callback invoked on sheet dismissal (swipe-down, scrim tap, or cancel button).
 * @param isDestructive When `true`, the confirm button uses a red-bordered ghost style
 *                      instead of the hero [AppButton]. Use for irreversible
 *                      actions such as delete or erase.
 * @param modifier     Modifier applied to the root [AppBottomSheet].
 *
 * @see AppBottomSheet
 * @see AppButton
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
public fun ConfirmationSheet(
    title: String,
    body: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
) {
    AppBottomSheet(
        onDismiss = onDismiss,
        title = title,
        modifier = modifier,
    ) {
        Text(
            text = body,
            style = Theme.typography.body,
            color = Theme.color.ink2,
        )

        Spacer(modifier = Modifier.height(Theme.spacing.spacingL))
        HorizontalAnimatedDivider(isVisible = true)
        Spacer(modifier = Modifier.height(Theme.spacing.spacingL))

        if (isDestructive) {
            val destructiveSource = remember { MutableInteractionSource() }
            val isDestructivePressed by destructiveSource.collectIsPressedAsState()
            val destructiveScale by animateFloatAsState(
                targetValue = if (isDestructivePressed) 0.97f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
                label = "DestructiveButton: scale",
            )
            Box(
                modifier = Modifier
                    .scale(destructiveScale)
                    .fillMaxWidth()
                    .clip(Theme.cornerToken.button)
                    .border(
                        width = Theme.stroke.thin,
                        color = Theme.color.primary,
                        shape = Theme.cornerToken.button,
                    )
                    .clickable(
                        interactionSource = destructiveSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onConfirm,
                    )
                    .padding(
                        horizontal = Theme.spacing.spacingXL,
                        vertical = Theme.spacing.spacingL,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = confirmText,
                    style = Theme.typography.bodyStrong,
                    color = Theme.color.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            AppButton(
                text = confirmText,
                onClick = onConfirm,
                fillMaxWidth = true,
            )
        }

        Spacer(modifier = Modifier.height(Theme.spacing.spacingS))

        val dismissSource = remember { MutableInteractionSource() }
        val isDismissPressed by dismissSource.collectIsPressedAsState()
        val dismissScale by animateFloatAsState(
            targetValue = if (isDismissPressed) 0.97f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
            label = "DismissButton: scale",
        )
        Box(
            modifier = Modifier
                .scale(dismissScale)
                .fillMaxWidth()
                .clip(Theme.cornerToken.button)
                .border(
                    width = Theme.stroke.thin,
                    color = Theme.color.line,
                    shape = Theme.cornerToken.button,
                )
                .clickable(
                    interactionSource = dismissSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onDismiss,
                )
                .padding(
                    horizontal = Theme.spacing.spacingXL,
                    vertical = Theme.spacing.spacingL,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = dismissText,
                style = Theme.typography.bodyStrong,
                color = Theme.color.ink2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview
@Composable
private fun ConfirmationSheetPreview() {
    AppTheme {
        ConfirmationSheet(
            title = "CONNECT",
            body = "Pair with RECTIFY-A1 (20:6E:F1:A1:41:1D)?",
            confirmText = "CONFIRM",
            dismissText = "CANCEL",
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun ConfirmationSheetDestructivePreview() {
    AppTheme {
        ConfirmationSheet(
            title = "ERASE ALL DATA",
            body = "This will permanently delete all commands and preferences. This action cannot be undone.",
            confirmText = "ERASE",
            dismissText = "CANCEL",
            onConfirm = {},
            onDismiss = {},
            isDestructive = true,
        )
    }
}

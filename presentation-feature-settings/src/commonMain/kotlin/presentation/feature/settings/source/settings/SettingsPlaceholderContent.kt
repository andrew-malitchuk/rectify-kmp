package presentation.feature.settings.source.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.core.modifier.PlaceholderProvider
import presentation.core.ui.core.modifier.placeholder

/**
 * Loading placeholder for the Settings screen.
 *
 * Mirrors the layout of [SettingsSuccessContent] with flat blocks in place of each
 * content element while preferences are being loaded.
 *
 * @see SettingsSuccessContent
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
internal fun SettingsPlaceholderContent() {
    PlaceholderProvider(isLoading = true) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Theme.spacing.spacingL),
        ) {
            // ---- Title ------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp)
                    .placeholder(),
            )

            Spacer(modifier = Modifier.height(Theme.spacing.spacingS))

            // Accent underline placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.25f)
                    .height(2.dp)
                    .placeholder(),
            )

            Spacer(modifier = Modifier.height(Theme.spacing.spacingXL))

            // ---- Permissions ------------------------------------------------
            // Section header
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .placeholder(),
            )

            Spacer(modifier = Modifier.height(Theme.spacing.spacingS))

            // Toggle row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .placeholder(),
            )

            Spacer(modifier = Modifier.height(Theme.spacing.spacingXL))

            // ---- Appearance -------------------------------------------------
            // Section header
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(12.dp)
                    .placeholder(),
            )

            Spacer(modifier = Modifier.height(Theme.spacing.spacingS))

            // Language dropdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .placeholder(),
            )

            Spacer(modifier = Modifier.height(Theme.spacing.spacingS))

            // Theme cards - side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .placeholder(),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .placeholder(),
                )
            }

            Spacer(modifier = Modifier.height(Theme.spacing.spacing3XL))

            // ---- Info -------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .placeholder(),
            )

            Spacer(modifier = Modifier.height(Theme.spacing.spacingL))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(12.dp)
                            .placeholder(),
                    )
                    Spacer(modifier = Modifier.height(Theme.spacing.spacingXS))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(40.dp)
                            .placeholder(),
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(12.dp)
                            .placeholder(),
                    )
                    Spacer(modifier = Modifier.height(Theme.spacing.spacingXS))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(16.dp)
                            .placeholder(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(Theme.spacing.spacingL))

            // ---- System Registry Card ---------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .placeholder(),
            )

            Spacer(modifier = Modifier.height(Theme.spacing.spacing4XL))
        }
    }
}

@Preview
@Composable
private fun SettingsPlaceholderContentPreview() {
    AppTheme {
        SettingsPlaceholderContent()
    }
}

package presentation.feature.about.source.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonVariant
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.about_back
import rectify_kmp.presentation_core_localisation.generated.resources.about_build_label
import rectify_kmp.presentation_core_localisation.generated.resources.about_description
import rectify_kmp.presentation_core_localisation.generated.resources.about_title
import rectify_kmp.presentation_core_localisation.generated.resources.about_version_label

/**
 * Renders the loaded-state body of the About screen.
 *
 * @param state Current UI state with about information.
 * @param onIntent Callback to dispatch user intents to the ViewModel.
 *
 * @see AboutContent
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
internal fun AboutSuccessContent(
    state: AboutState,
    onIntent: (AboutIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ---- Title -----------------------------------------------------------
        Text(
            text = stringResource(Res.string.about_title),
            style = Theme.typography.titleL,
            color = Theme.color.ink,
            modifier = Modifier
                .padding(horizontal = Theme.spacing.spacingL)
                .padding(top = Theme.spacing.spacingXL, bottom = Theme.spacing.spacingS),
        )
        Box(
            modifier = Modifier
                .padding(horizontal = Theme.spacing.spacingL)
                .width(80.dp)
                .height(Theme.stroke.regular)
                .background(Theme.color.primary),
        )

        Spacer(modifier = Modifier.height(Theme.spacing.spacingXL))

        // ---- App info card ---------------------------------------------------
        Box(
            modifier = Modifier
                .padding(horizontal = Theme.spacing.spacingL)
                .fillMaxWidth()
                .background(
                    color = Theme.color.stage,
                    shape = Theme.cornerToken.card,
                ),
        ) {
            Column(modifier = Modifier.padding(Theme.spacing.spacingL)) {
                Text(
                    text = state.appName,
                    style = Theme.typography.titleL,
                    color = Theme.color.ink,
                )
                Spacer(modifier = Modifier.height(Theme.spacing.spacingXS))
                Text(
                    text = stringResource(Res.string.about_description),
                    style = Theme.typography.comment,
                    color = Theme.color.ink2,
                )
            }
        }

        Spacer(modifier = Modifier.height(Theme.spacing.spacingS))

        // ---- Version info card -----------------------------------------------
        Box(
            modifier = Modifier
                .padding(horizontal = Theme.spacing.spacingL)
                .fillMaxWidth()
                .background(
                    color = Theme.color.stage,
                    shape = Theme.cornerToken.card,
                ),
        ) {
            Column(modifier = Modifier.padding(Theme.spacing.spacingL)) {
                InfoRow(
                    label = stringResource(Res.string.about_version_label),
                    value = state.appVersion,
                )
                Spacer(modifier = Modifier.height(Theme.spacing.spacingS))
                InfoRow(
                    label = stringResource(Res.string.about_build_label),
                    value = state.buildNumber,
                )
            }
        }

        Spacer(modifier = Modifier.height(Theme.spacing.spacing3XL))

        AppButton(
            text = stringResource(Res.string.about_back),
            onClick = { onIntent(AboutIntent.OnBackClick) },
            variant = AppButtonVariant.Secondary,
            fillMaxWidth = true,
            modifier = Modifier.padding(horizontal = Theme.spacing.spacingL),
        )

        Spacer(modifier = Modifier.height(Theme.spacing.spacingXL))
    }
}

@Preview
@Composable
private fun AboutSuccessContentPreview() {
    AppTheme {
        AboutSuccessContent(
            state = AboutState(),
            onIntent = {},
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = Theme.typography.overline,
            color = Theme.color.ink2,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = Theme.typography.monoData,
            color = Theme.color.ink,
        )
    }
}

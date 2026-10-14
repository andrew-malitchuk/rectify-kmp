package presentation.feature.home.source.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.home_action_styleguide
import rectify_kmp.presentation_core_localisation.generated.resources.settings_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonVariant

@Composable
internal fun HomeSuccessContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Theme.color.bg)
            .padding(Theme.spacing.spacingL),
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS, Alignment.CenterVertically),
    ) {
        Text(
            text = "HOME",
            style = Theme.typography.titleL,
            color = Theme.color.ink,
        )

        AppButton(
            text = stringResource(Res.string.settings_title),
            onClick = { onIntent(HomeIntent.OnSettingsClick) },
            fillMaxWidth = true,
        )

        AppButton(
            text = stringResource(Res.string.home_action_styleguide),
            onClick = { onIntent(HomeIntent.OnStyleguideClick) },
            variant = AppButtonVariant.Secondary,
            fillMaxWidth = true,
        )
    }
}

@Preview
@Composable
private fun HomeSuccessContentPreview() {
    AppTheme {
        HomeSuccessContent(
            state = HomeState(),
            onIntent = {},
        )
    }
}

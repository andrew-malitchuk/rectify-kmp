package presentation.feature.settings.source.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.icon.Settings
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonVariant
import presentation.core.ui.source.kit.atom.text.SectionHeaderWithIcon
import presentation.core.ui.source.kit.molecule.item.SelectableCard
import presentation.core.ui.source.kit.molecule.item.SelectionRow
import rectify_kmp.presentation_core_localisation.generated.resources.settings_appearance
import rectify_kmp.presentation_core_localisation.generated.resources.settings_title
import rectify_kmp.presentation_core_localisation.generated.resources.settings_erase_all_data
import presentation.core.ui.source.kit.organism.bottomsheet.ConfirmationSheet
import rectify_kmp.presentation_core_localisation.generated.resources.settings_erase_cancel_button
import rectify_kmp.presentation_core_localisation.generated.resources.settings_erase_confirm_body
import rectify_kmp.presentation_core_localisation.generated.resources.settings_erase_confirm_button
import rectify_kmp.presentation_core_localisation.generated.resources.settings_erase_confirm_title
import rectify_kmp.presentation_core_localisation.generated.resources.settings_core_version
import rectify_kmp.presentation_core_localisation.generated.resources.settings_language_selector
import rectify_kmp.presentation_core_localisation.generated.resources.settings_theme_brutalist
import rectify_kmp.presentation_core_localisation.generated.resources.settings_theme_brutalist_subtitle
import rectify_kmp.presentation_core_localisation.generated.resources.settings_theme_monolith
import rectify_kmp.presentation_core_localisation.generated.resources.settings_theme_monolith_subtitle
import rectify_kmp.presentation_core_localisation.generated.resources.settings_unit_imperial
import rectify_kmp.presentation_core_localisation.generated.resources.settings_unit_imperial_subtitle
import rectify_kmp.presentation_core_localisation.generated.resources.settings_unit_metric
import rectify_kmp.presentation_core_localisation.generated.resources.settings_unit_metric_subtitle
import rectify_kmp.presentation_core_localisation.generated.resources.settings_unit_selector
import rectify_kmp.presentation_core_localisation.generated.resources.settings_system_info
import rectify_kmp.presentation_core_localisation.generated.resources.settings_theme_selector

/**
 * Success-state content for the Settings screen.
 *
 * Renders the full settings UI in three sections:
 * 1. Screen title with accent underline
 * 2. Appearance — language row (navigates to dedicated picker), theme selector, and unit
 *    system selector cards
 * 3. Danger — "Erase All Data" button with a confirmation sheet
 * 4. Info — system info link and app version
 *
 * All section headers, rows, toggles, and cards reuse existing design-system
 * components from `presentation-core-ui`.
 *
 * @param state Current [SettingsState] containing all displayable values.
 * @param onIntent Callback to dispatch [SettingsIntent] actions to the ViewModel.
 *
 * @see SettingsContent
 */
@Composable
internal fun SettingsSuccessContent(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    scrollState: ScrollState = rememberScrollState(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        // ---- Title -----------------------------------------------------------
        Text(
            text = stringResource(Res.string.settings_title),
            style = Theme.typography.titleL,
            color = Theme.color.ink,
            modifier = Modifier
                .padding(horizontal = Theme.spacing.spacingL)
                .padding(top = Theme.spacing.spacingXL, bottom = Theme.spacing.spacingS),
        )
        // Accent underline under the title
        Box(
            modifier = Modifier
                .padding(horizontal = Theme.spacing.spacingL)
                .width(80.dp)
                .height(Theme.stroke.regular)
                .background(Theme.color.primary),
        )

        Spacer(modifier = Modifier.height(Theme.spacing.spacingXL))

        // ---- Appearance Section ----------------------------------------------
        SectionHeaderWithIcon(
            icon = Settings,
            title = stringResource(Res.string.settings_appearance),
        )

        Spacer(modifier = Modifier.height(Theme.spacing.spacingS))

        // Language selector — tapping navigates to the dedicated Language Picker screen
        Box(
            modifier = Modifier
                .padding(horizontal = Theme.spacing.spacingL)
                .fillMaxWidth()
                .clip(Theme.cornerToken.card)
                .background(Theme.color.stage),
        ) {
            Column(modifier = Modifier.padding(Theme.spacing.spacingL)) {
                Text(
                    text = stringResource(Res.string.settings_language_selector),
                    style = Theme.typography.overline,
                    color = Theme.color.ink2,
                )
                Spacer(modifier = Modifier.height(Theme.spacing.spacingS))
                SelectionRow(
                    label = state.languageOptions.getOrElse(state.selectedLanguageIndex) { "English" },
                    onClick = { onIntent(SettingsIntent.OnLanguagePickerClick) },
                )
            }
        }

        Spacer(modifier = Modifier.height(Theme.spacing.spacingS))

        // Theme selector cards
        Box(
            modifier = Modifier
                .padding(horizontal = Theme.spacing.spacingL)
                .fillMaxWidth()
                .clip(Theme.cornerToken.card)
                .background(Theme.color.stage),
        ) {
            Column(modifier = Modifier.padding(Theme.spacing.spacingL)) {
                Text(
                    text = stringResource(Res.string.settings_theme_selector),
                    style = Theme.typography.overline,
                    color = Theme.color.ink2,
                )
                Spacer(modifier = Modifier.height(Theme.spacing.spacingS))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
                ) {
                    // Index 0 = Dark (Monolith)
                    SelectableCard(
                        title = stringResource(Res.string.settings_theme_monolith),
                        subtitle = stringResource(Res.string.settings_theme_monolith_subtitle),
                        selected = state.selectedThemeIndex == 0,
                        onClick = { onIntent(SettingsIntent.SelectTheme(0)) },
                        modifier = Modifier.weight(1f),
                    )
                    // Index 1 = Light (Brutalist)
                    SelectableCard(
                        title = stringResource(Res.string.settings_theme_brutalist),
                        subtitle = stringResource(Res.string.settings_theme_brutalist_subtitle),
                        selected = state.selectedThemeIndex == 1,
                        onClick = { onIntent(SettingsIntent.SelectTheme(1)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Theme.spacing.spacingS))

        // Unit system selector cards
        Box(
            modifier = Modifier
                .padding(horizontal = Theme.spacing.spacingL)
                .fillMaxWidth()
                .clip(Theme.cornerToken.card)
                .background(Theme.color.stage),
        ) {
            Column(modifier = Modifier.padding(Theme.spacing.spacingL)) {
                Text(
                    text = stringResource(Res.string.settings_unit_selector),
                    style = Theme.typography.overline,
                    color = Theme.color.ink2,
                )
                Spacer(modifier = Modifier.height(Theme.spacing.spacingS))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
                ) {
                    SelectableCard(
                        title = stringResource(Res.string.settings_unit_metric),
                        subtitle = stringResource(Res.string.settings_unit_metric_subtitle),
                        selected = state.selectedUnitIndex == 0,
                        onClick = { onIntent(SettingsIntent.SelectUnitSystem(0)) },
                        modifier = Modifier.weight(1f),
                    )
                    SelectableCard(
                        title = stringResource(Res.string.settings_unit_imperial),
                        subtitle = stringResource(Res.string.settings_unit_imperial_subtitle),
                        selected = state.selectedUnitIndex == 1,
                        onClick = { onIntent(SettingsIntent.SelectUnitSystem(1)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Theme.spacing.spacingXL))

        // ---- Danger Section --------------------------------------------------
        AppButton(
            text = stringResource(Res.string.settings_erase_all_data),
            onClick = { onIntent(SettingsIntent.OnEraseAllDataClick) },
            variant = AppButtonVariant.Secondary,
            fillMaxWidth = true,
            modifier = Modifier.padding(horizontal = Theme.spacing.spacingL),
        )

        if (state.showEraseConfirmation) {
            ConfirmationSheet(
                title = stringResource(Res.string.settings_erase_confirm_title),
                body = stringResource(Res.string.settings_erase_confirm_body),
                confirmText = stringResource(Res.string.settings_erase_confirm_button),
                dismissText = stringResource(Res.string.settings_erase_cancel_button),
                onConfirm = { onIntent(SettingsIntent.OnEraseAllData) },
                onDismiss = { onIntent(SettingsIntent.OnDismissEraseConfirmation) },
                isDestructive = true,
            )
        }

        Spacer(modifier = Modifier.height(Theme.spacing.spacingXL))

        // ---- Info Section ---------------------------------------------------
        // System info navigation link
        Text(
            text = stringResource(Res.string.settings_system_info),
            style = Theme.typography.bodyStrong,
            color = Theme.color.primary,
            modifier = Modifier
                .padding(horizontal = Theme.spacing.spacingL)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onIntent(SettingsIntent.OnSystemInfoClick) },
                ),
        )

        Spacer(modifier = Modifier.height(Theme.spacing.spacingS))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Theme.spacing.spacingL)
                .height(Theme.stroke.thin)
                .background(Theme.color.line),
        )

        Spacer(modifier = Modifier.height(Theme.spacing.spacingL))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Theme.spacing.spacingL),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.settings_core_version),
                    style = Theme.typography.overline,
                    color = Theme.color.ink2,
                )
                Spacer(modifier = Modifier.height(Theme.spacing.spacingXS))
                Text(
                    text = state.appVersion,
                    style = Theme.typography.display,
                    color = Theme.color.ink,
                )
            }
        }

        Spacer(modifier = Modifier.height(Theme.spacing.spacingXL))
    }
}

@Preview
@Composable
private fun SettingsSuccessContentPreview() {
    AppTheme {
        SettingsSuccessContent(
            state = SettingsState(),
            onIntent = {},
        )
    }
}

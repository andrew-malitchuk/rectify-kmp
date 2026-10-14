package presentation.feature.settings.source.language

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.molecule.header.NavigationHeader
import presentation.core.ui.source.kit.molecule.item.LanguageOptionRow
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.settings_language

/**
 * Root content composable for the Language Picker screen.
 *
 * Renders a [NavigationHeader] with a back button followed by a scrollable list
 * of language options delegated to [LanguagePickerSuccessContent].
 *
 * @param state Current [LanguagePickerState] driving the UI.
 * @param onIntent Callback to dispatch [LanguagePickerIntent] actions to the ViewModel.
 *
 * @see LanguagePickerSuccessContent
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
internal fun LanguagePickerContent(
    state: LanguagePickerState,
    onIntent: (LanguagePickerIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Theme.color.bg)
            .statusBarsPadding(),
    ) {
        item {
            NavigationHeader(
                title = stringResource(Res.string.settings_language),
                onNavigationClick = { onIntent(LanguagePickerIntent.OnBackClick) },
            )
        }
        itemsIndexed(state.options) { index, label ->
            LanguageOptionRow(
                label = label,
                code = state.codes.getOrElse(index) { "" },
                selected = index == state.selectedIndex,
                onClick = { onIntent(LanguagePickerIntent.SelectLanguage(index)) },
            )
        }
    }
}

@Preview
@Composable
private fun LanguagePickerContentPreview() {
    AppTheme {
        LanguagePickerContent(
            state = LanguagePickerState(selectedIndex = 0),
            onIntent = {},
        )
    }
}

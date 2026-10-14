package presentation.feature.settings.source.language

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import presentation.core.navigation.api.core.composition.LocalAppNavigator

/**
 * Entry-point composable for the Language Picker feature.
 *
 * Collects Orbit MVI state and side-effects from [LanguagePickerViewModel],
 * delegates rendering to [LanguagePickerContent], and handles back navigation
 * via [LocalAppNavigator].
 *
 * @param viewModel Koin-provided [LanguagePickerViewModel] instance.
 *
 * @see LanguagePickerContent
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
public fun LanguagePickerScreen(viewModel: LanguagePickerViewModel = koinViewModel()) {
    val appNavigator = LocalAppNavigator.current
    val state = viewModel.collectAsState()

    viewModel.collectSideEffect { effect ->
        when (effect) {
            LanguagePickerSideEffect.GoBack -> appNavigator?.backAction()
        }
    }

    LanguagePickerContent(
        state = state.value,
        onIntent = viewModel::handleIntent,
    )
}

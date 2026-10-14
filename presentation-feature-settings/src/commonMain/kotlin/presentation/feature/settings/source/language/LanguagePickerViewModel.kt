package presentation.feature.settings.source.language

import androidx.lifecycle.ViewModel
import domain.usecase.api.source.usecase.configuration.GetApplicationLanguageUseCase
import domain.usecase.api.source.usecase.configuration.SetApplicationLanguageUseCase
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.viewmodel.container

/**
 * ViewModel for the Language Picker screen.
 *
 * Loads the persisted language on entry, reflects it in [LanguagePickerState.selectedIndex],
 * and persists the new choice immediately when the user taps a row — then navigates back
 * so the Settings screen (which observes language reactively) updates automatically.
 *
 * @property getApplicationLanguageUseCase Retrieves the currently saved locale code.
 * @property setApplicationLanguageUseCase Persists the newly chosen locale code.
 *
 * @see LanguagePickerContract
 * @see LanguagePickerScreen
 */
@OrbitExperimental
public class LanguagePickerViewModel(
    private val getApplicationLanguageUseCase: GetApplicationLanguageUseCase,
    private val setApplicationLanguageUseCase: SetApplicationLanguageUseCase,
) : ContainerHost<LanguagePickerState, LanguagePickerSideEffect>, ViewModel() {

    override val container: Container<LanguagePickerState, LanguagePickerSideEffect> =
        container<LanguagePickerState, LanguagePickerSideEffect>(LanguagePickerState()) {
            loadCurrentLanguage()
        }

    /** Dispatches a [LanguagePickerIntent] to the appropriate handler. */
    public fun handleIntent(intent: LanguagePickerIntent) {
        when (intent) {
            LanguagePickerIntent.OnBackClick -> onBackClick()
            is LanguagePickerIntent.SelectLanguage -> selectLanguage(intent.index)
        }
    }

    /** Resolves the persisted language code to an index in [LanguagePickerState.codes]. */
    private fun loadCurrentLanguage() = intent {
        val code = getApplicationLanguageUseCase().getOrNull() ?: "en"
        val index = state.codes.indexOf(code).coerceAtLeast(0)
        reduce { state.copy(selectedIndex = index) }
    }

    /**
     * Persists the selected locale and immediately navigates back.
     * The Settings screen will reflect the change via its language observer.
     */
    private fun selectLanguage(index: Int) = intent {
        reduce { state.copy(selectedIndex = index) }
        setApplicationLanguageUseCase(state.codes[index])
        postSideEffect(LanguagePickerSideEffect.GoBack)
    }

    /** Navigates back without changing the selection. */
    private fun onBackClick() = intent {
        postSideEffect(LanguagePickerSideEffect.GoBack)
    }
}

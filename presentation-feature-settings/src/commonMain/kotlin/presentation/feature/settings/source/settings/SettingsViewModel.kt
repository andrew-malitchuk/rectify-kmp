package presentation.feature.settings.source.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.core.source.model.ThemeModel
import domain.core.source.model.UnitSystemModel
import domain.usecase.api.source.usecase.configuration.GetThemeUseCase
import domain.usecase.api.source.usecase.configuration.GetUnitSystemUseCase
import domain.usecase.api.source.usecase.configuration.ObserveApplicationLanguageUseCase
import domain.usecase.api.source.usecase.configuration.ObserveUnitSystemUseCase
import domain.usecase.api.source.usecase.configuration.SetOnboardingStatusUseCase
import domain.usecase.api.source.usecase.configuration.SetThemeUseCase
import domain.usecase.api.source.usecase.configuration.SetUnitSystemUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

public class SettingsViewModel(
    private val observeApplicationLanguage: ObserveApplicationLanguageUseCase,
    private val getThemeUseCase: GetThemeUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val getUnitSystemUseCase: GetUnitSystemUseCase,
    private val setUnitSystemUseCase: SetUnitSystemUseCase,
    private val observeUnitSystemUseCase: ObserveUnitSystemUseCase,
    private val setOnboardingStatus: SetOnboardingStatusUseCase,
) : ContainerHost<SettingsState, SettingsSideEffect>, ViewModel() {

    override val container: Container<SettingsState, SettingsSideEffect> =
        container(SettingsState()) {
            loadCurrentTheme()
            startObservingLanguage()
            startObservingUnitSystem()
        }

    public fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.OnBackClick -> onBackClick()
            SettingsIntent.OnLanguagePickerClick -> onLanguagePickerClick()
            is SettingsIntent.SelectTheme -> selectTheme(intent.index)
            is SettingsIntent.SelectUnitSystem -> selectUnitSystem(intent.index)
            SettingsIntent.OnSystemInfoClick -> onSystemInfoClick()
            SettingsIntent.OnEraseAllDataClick -> onEraseAllDataClick()
            SettingsIntent.OnDismissEraseConfirmation -> onDismissEraseConfirmation()
            SettingsIntent.OnEraseAllData -> onEraseAllData()
        }
    }

    private fun startObservingLanguage() {
        viewModelScope.launch {
            observeApplicationLanguage()
                .onEach { result ->
                    val code = result.getOrNull() ?: "en"
                    val codes = container.stateFlow.value.languageCodes
                    val index = codes.indexOf(code).coerceAtLeast(0)
                    intent { reduce { state.copy(selectedLanguageIndex = index) } }
                }
                .catch { /* preference store unavailable — keep default */ }
                .collect {}
        }
    }

    private fun loadCurrentTheme() = intent {
        val theme = getThemeUseCase().getOrNull() ?: ThemeModel.Dark
        val index = THEME_MODELS.indexOf(theme).coerceAtLeast(0)
        reduce { state.copy(selectedThemeIndex = index) }
    }

    private fun onLanguagePickerClick() = intent {
        postSideEffect(SettingsSideEffect.NavigateToLanguagePicker)
    }

    private fun selectTheme(index: Int) = intent {
        reduce { state.copy(selectedThemeIndex = index) }
        setThemeUseCase(THEME_MODELS[index])
    }

    private fun startObservingUnitSystem() {
        viewModelScope.launch {
            observeUnitSystemUseCase()
                .onEach { result ->
                    val model = result.getOrNull() ?: UnitSystemModel.Metric
                    val index = UNIT_SYSTEM_MODELS.indexOf(model).coerceAtLeast(0)
                    intent { reduce { state.copy(selectedUnitIndex = index) } }
                }
                .catch { /* preference store unavailable — keep default */ }
                .collect {}
        }
    }

    private fun selectUnitSystem(index: Int) = intent {
        reduce { state.copy(selectedUnitIndex = index) }
        setUnitSystemUseCase(UNIT_SYSTEM_MODELS[index])
    }

    private fun onSystemInfoClick() = intent {
        postSideEffect(SettingsSideEffect.NavigateToAbout)
    }

    private fun onBackClick() = intent {
        postSideEffect(SettingsSideEffect.GoBackEffect)
    }

    private fun onEraseAllDataClick() = intent {
        reduce { state.copy(showEraseConfirmation = true) }
    }

    private fun onDismissEraseConfirmation() = intent {
        reduce { state.copy(showEraseConfirmation = false) }
    }

    private fun onEraseAllData() = intent {
        reduce { state.copy(showEraseConfirmation = false) }
        setOnboardingStatus(false)
        postSideEffect(SettingsSideEffect.NavigateToSplash)
    }

    private companion object {
        // System first: it is the effective first-run default, and indexOf() returning -1 for it
        // made the selector show Dark while the app rendered system appearance.
        val THEME_MODELS = listOf(ThemeModel.System, ThemeModel.Dark, ThemeModel.Light)
        val UNIT_SYSTEM_MODELS = listOf(UnitSystemModel.Metric, UnitSystemModel.Imperial)
    }
}

package presentation.feature.settings.source.settings

/**
 * UI state for the Settings screen.
 *
 * @property isLoading Whether a loading indicator should be shown.
 * @property selectedLanguageIndex Index of the currently selected language in [languageOptions].
 * @property languageOptions Display labels for each supported language.
 * @property languageCodes ISO locale codes corresponding to [languageOptions].
 * @property selectedThemeIndex Index of the currently selected theme (0 = Dark, 1 = Light).
 * @property themeOptions Display labels for each supported theme.
 * @property selectedUnitIndex Index of the currently selected unit system (0 = Metric, 1 = Imperial).
 * @property unitSystemOptions Display labels for each supported unit system.
 * @property showEraseConfirmation Whether the erase-all-data confirmation sheet is visible.
 * @property appVersion Application version string for display (e.g. "v0.0.1").
 */
public data class SettingsState(
    val isLoading: Boolean = false,
    val selectedLanguageIndex: Int = 0,
    val languageOptions: List<String> = listOf("English"),
    val languageCodes: List<String> = listOf("en"),
    val selectedThemeIndex: Int = 0,
    val themeOptions: List<String> = listOf("Dark", "Light"),
    val selectedUnitIndex: Int = 0,
    val unitSystemOptions: List<String> = listOf("Metric", "Imperial"),
    /** Whether the erase-all-data confirmation sheet is currently shown. */
    val showEraseConfirmation: Boolean = false,
    val appVersion: String = "v0.0.1",
)

/**
 * One-shot side-effects emitted by [SettingsViewModel].
 */
public sealed class SettingsSideEffect {

    /** Navigate back to the previous screen. */
    public data object GoBackEffect : SettingsSideEffect()

    /** Navigate to the Language Picker screen. */
    public data object NavigateToLanguagePicker : SettingsSideEffect()

    /** Navigate to the About / System Info screen. */
    public data object NavigateToAbout : SettingsSideEffect()

    /** Navigate to the Splash screen after all data has been erased. */
    public data object NavigateToSplash : SettingsSideEffect()

    /**
     * Display an error message to the user.
     *
     * @property messageId String resource identifier for the error message.
     */
    public data class ShowError(val messageId: Int) : SettingsSideEffect()
}

/**
 * User-initiated actions on the Settings screen.
 */
public sealed class SettingsIntent {

    /** User tapped the back / navigation button. */
    public data object OnBackClick : SettingsIntent()

    /**
     * User tapped the language row to open the dedicated Language Picker screen.
     */
    public data object OnLanguagePickerClick : SettingsIntent()

    /**
     * User selected a theme at the given [index] in [SettingsState.themeOptions].
     */
    public data class SelectTheme(val index: Int) : SettingsIntent()

    /**
     * User selected a unit system at the given [index] in [SettingsState.unitSystemOptions].
     */
    public data class SelectUnitSystem(val index: Int) : SettingsIntent()

    /** User tapped "SYSTEM INFO" in the footer to navigate to the About screen. */
    public data object OnSystemInfoClick : SettingsIntent()

    /** User tapped "ERASE ALL DATA" — shows the confirmation sheet. */
    public data object OnEraseAllDataClick : SettingsIntent()

    /** User dismissed the erase confirmation sheet without confirming. */
    public data object OnDismissEraseConfirmation : SettingsIntent()

    /** User confirmed "Erase all data" — clears all preferences. */
    public data object OnEraseAllData : SettingsIntent()
}

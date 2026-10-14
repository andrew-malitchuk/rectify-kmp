package presentation.feature.settings.source.language

/**
 * UI state for the Language Picker screen.
 *
 * @property selectedIndex Index of the currently active language in [options].
 * @property options Human-readable display labels for each supported locale.
 * @property codes ISO locale codes corresponding to [options] by index.
 */
public data class LanguagePickerState(
    val selectedIndex: Int = 0,
    val options: List<String> = listOf("English"),
    val codes: List<String> = listOf("en"),
)

/**
 * One-shot side-effects emitted by [LanguagePickerViewModel].
 */
public sealed class LanguagePickerSideEffect {

    /** Navigate back to the Settings screen after a selection has been persisted. */
    public data object GoBack : LanguagePickerSideEffect()
}

/**
 * User-initiated actions on the Language Picker screen.
 */
public sealed class LanguagePickerIntent {

    /** User tapped the back / navigation button without making a selection. */
    public data object OnBackClick : LanguagePickerIntent()

    /**
     * User tapped a language option at the given [index] in [LanguagePickerState.codes].
     *
     * The selection is persisted immediately and triggers [LanguagePickerSideEffect.GoBack].
     */
    public data class SelectLanguage(val index: Int) : LanguagePickerIntent()
}

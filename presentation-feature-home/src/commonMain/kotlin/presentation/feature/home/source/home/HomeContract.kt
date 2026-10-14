package presentation.feature.home.source.home

public data class HomeState(
    val isLoading: Boolean = false,
)

public sealed class HomeSideEffect {
    public data object NavigateToSettings : HomeSideEffect()
    public data object NavigateToStyleguide : HomeSideEffect()

    public data class ShowError(val messageId: Int) : HomeSideEffect()
}

public sealed class HomeIntent {
    public data object OnSettingsClick : HomeIntent()
    public data object OnStyleguideClick : HomeIntent()
}

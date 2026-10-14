package presentation.feature.onboarding.source.onboarding

public data class OnboardingState(
    val currentPage: Int = 0,
    val isLoading: Boolean = false,
)

public sealed class OnboardingSideEffect {

    public data object NavigateToHome : OnboardingSideEffect()

    public data class ShowError(val messageId: Int) : OnboardingSideEffect()
}

public sealed class OnboardingIntent {

    public data class OnPageChanged(val page: Int) : OnboardingIntent()

    public data object OnGetStartedClick : OnboardingIntent()
}

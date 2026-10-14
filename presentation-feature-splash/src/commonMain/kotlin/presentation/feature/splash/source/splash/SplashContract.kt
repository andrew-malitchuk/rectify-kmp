package presentation.feature.splash.source.splash

public data object SplashState

public sealed class SplashSideEffect {

    public data object NavigateToOnboarding : SplashSideEffect()

    public data object NavigateToHome : SplashSideEffect()
}

package presentation.feature.onboarding.core.page

internal enum class OnboardingPage {
    Welcome,
    Ready,
}

internal val ONBOARDING_PAGE_COUNT = OnboardingPage.entries.size

internal val OnboardingPage.isLast: Boolean
    get() = this == OnboardingPage.Ready

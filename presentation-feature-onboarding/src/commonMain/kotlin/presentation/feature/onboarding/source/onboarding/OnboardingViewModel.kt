package presentation.feature.onboarding.source.onboarding

import androidx.lifecycle.ViewModel
import domain.usecase.api.source.usecase.configuration.SetOnboardingStatusUseCase
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

public class OnboardingViewModel(
    private val setOnboardingStatusUseCase: SetOnboardingStatusUseCase,
) : ContainerHost<OnboardingState, OnboardingSideEffect>, ViewModel() {

    override val container: Container<OnboardingState, OnboardingSideEffect> =
        container(OnboardingState())

    public fun handleIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.OnPageChanged -> onPageChanged(intent.page)
            OnboardingIntent.OnGetStartedClick -> onGetStartedClick()
        }
    }

    private fun onPageChanged(page: Int) = intent {
        reduce { state.copy(currentPage = page) }
    }

    private fun onGetStartedClick() = intent {
        reduce { state.copy(isLoading = true) }
        setOnboardingStatusUseCase(true)
            .onSuccess { postSideEffect(OnboardingSideEffect.NavigateToHome) }
            .onFailure {
                setOnboardingStatusUseCase(true)
                postSideEffect(OnboardingSideEffect.NavigateToHome)
            }
    }
}

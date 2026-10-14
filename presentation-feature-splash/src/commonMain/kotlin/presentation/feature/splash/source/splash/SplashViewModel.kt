package presentation.feature.splash.source.splash

import androidx.lifecycle.ViewModel
import domain.usecase.api.source.usecase.configuration.GetOnboardingStatusUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

private const val MIN_SPLASH_DURATION_MS = 1_500L

public class SplashViewModel(
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
) : ContainerHost<SplashState, SplashSideEffect>, ViewModel() {

    override val container: Container<SplashState, SplashSideEffect> =
        container(SplashState) {
            // Run the use case and minimum display timer concurrently so the total
            // splash duration is max(usecase, 1500ms) rather than their sum.
            val isOnboardingCompleted = coroutineScope {
                val deferred = async { getOnboardingStatusUseCase() }
                delay(MIN_SPLASH_DURATION_MS)
                deferred.await().getOrDefault(false)
            }
            if (isOnboardingCompleted) {
                postSideEffect(SplashSideEffect.NavigateToHome)
            } else {
                postSideEffect(SplashSideEffect.NavigateToOnboarding)
            }
        }
}

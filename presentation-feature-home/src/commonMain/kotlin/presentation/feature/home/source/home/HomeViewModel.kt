package presentation.feature.home.source.home

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

public class HomeViewModel : ContainerHost<HomeState, HomeSideEffect>, ViewModel() {

    override val container: Container<HomeState, HomeSideEffect> = container(HomeState())

    public fun handleIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.OnSettingsClick -> onSettingsClick()
            HomeIntent.OnStyleguideClick -> onStyleguideClick()
        }
    }

    private fun onSettingsClick() = intent {
        postSideEffect(HomeSideEffect.NavigateToSettings)
    }

    private fun onStyleguideClick() = intent {
        postSideEffect(HomeSideEffect.NavigateToStyleguide)
    }
}

package tech.hanasaki.momotalk_plus.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import tech.hanasaki.momotalk_plus.features.home.presentation.state.HomeIntent
import tech.hanasaki.momotalk_plus.features.home.presentation.state.HomeSideEffect
import tech.hanasaki.momotalk_plus.features.home.presentation.state.HomeState

class HomeViewModel : ViewModel(), ContainerHost<HomeState, HomeSideEffect> {

    override val container: Container<HomeState, HomeSideEffect> = viewModelScope.container(HomeState())

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.TabSelected -> intent { reduce { state.copy(currentTab = intent.tab) } }
            HomeIntent.SettingsClicked -> intent { postSideEffect(HomeSideEffect.NavigateToSettings) }
            HomeIntent.ProfileClicked -> intent { postSideEffect(HomeSideEffect.NavigateToProfile) }
            HomeIntent.LogoutClicked -> intent { postSideEffect(HomeSideEffect.NavigateToLogin) }
            is HomeIntent.SetBottomBarVisibility -> intent { reduce { state.copy(showBottomBar = intent.visible) } }
        }
    }
}

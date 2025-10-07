package tech.hanasaki.momotalk_plus.features.home.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.features.home.presentation.state.HomeIntent
import tech.hanasaki.momotalk_plus.features.home.presentation.state.HomeSideEffect
import tech.hanasaki.momotalk_plus.features.home.presentation.state.HomeState

class HomeViewModel : BaseViewModel<HomeState, HomeSideEffect, HomeIntent>(HomeState()) {
    override fun processIntent(intent: HomeIntent) {
        viewModelScope.launch {
            when (intent) {
                is HomeIntent.TabSelected ->
                    updateState { it.copy(currentTab = intent.tab) }

                HomeIntent.ProfileClicked ->
                    sendSideEffect(HomeSideEffect.NavigateToProfile)

                HomeIntent.LogoutClicked ->
                    sendSideEffect(HomeSideEffect.NavigateToLogin)

                is HomeIntent.SetBottomBarVisibility ->
                    updateState { it.copy(showBottomBar = intent.visible) }
            }
        }
    }
}
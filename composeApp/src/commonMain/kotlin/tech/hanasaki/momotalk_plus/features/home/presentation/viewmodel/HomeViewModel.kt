package tech.hanasaki.momotalk_plus.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.features.home.presentation.state.HomeIntent
import tech.hanasaki.momotalk_plus.features.home.presentation.state.HomeSideEffect
import tech.hanasaki.momotalk_plus.features.home.presentation.state.HomeState

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<HomeSideEffect>()
    val sideEffect: Flow<HomeSideEffect> = _sideEffect.receiveAsFlow()

    fun processIntent(intent: HomeIntent) {
        viewModelScope.launch {
            when (intent) {
                is HomeIntent.TabSelected ->
                    _uiState.update { it.copy(currentTab = intent.tab) }

                is HomeIntent.NewChatClicked ->
                    _sideEffect.send(HomeSideEffect.NavigateToNewChat)

                HomeIntent.ProfileClicked ->
                    _sideEffect.send(HomeSideEffect.NavigateToProfile)

                HomeIntent.LogoutClicked ->
                    _sideEffect.send(HomeSideEffect.NavigateToLogin)

                is HomeIntent.SetBottomBarVisibility ->
                    _uiState.update { it.copy(showBottomBar = intent.visible) }
            }
        }
    }
}
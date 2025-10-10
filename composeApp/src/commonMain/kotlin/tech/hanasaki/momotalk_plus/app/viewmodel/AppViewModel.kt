package tech.hanasaki.momotalk_plus.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.app.state.AppUiState
import tech.hanasaki.momotalk_plus.core.domain.repository.SessionRepository
import tech.hanasaki.momotalk_plus.core.domain.usecase.LogoutUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.ObserveCurrentUserUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.ObserveLoginStateUseCase

class AppViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val observeLoginStateUseCase: ObserveLoginStateUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        observeUserAndLoginState()
    }


    private fun observeUserAndLoginState() {
        combine(
            observeCurrentUserUseCase(),
            observeLoginStateUseCase()
        ) { user, isLoggedIn ->
            Pair(user, isLoggedIn)
        }
            .onStart {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                    )
                }
            }
            .onEach { (user, isLoggedIn) ->
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isLoggedIn = isLoggedIn,
                        isLoading = false,
                    )
                }
            }
            .catch { error ->
                error.printStackTrace()
                _uiState.update {
                    it.copy(
                        currentUser = null,
                        isLoggedIn = false,
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * 登出用户
     * 登出后，Flow 会自动推送新的状态到 UI
     */
    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
                .onSuccess {
                    println("登出用户成功")
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }
}
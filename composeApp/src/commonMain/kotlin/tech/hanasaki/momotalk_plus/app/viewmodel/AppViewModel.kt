package tech.hanasaki.momotalk_plus.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.app.state.AppUiState
import tech.hanasaki.momotalk_plus.core.domain.usecase.GetLoginStateUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.GetUserInfoUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.LogoutUserUseCase

class AppViewModel(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getLoginStateUseCase: GetLoginStateUseCase,
    private val logoutUserUseCase: LogoutUserUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        checkUserLoginStatus()
    }

    private fun checkUserLoginStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getLoginStateUseCase().collect { accessToken ->
                if (accessToken == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = false,
                            currentUser = null,
                        )
                    }
                } else {
                    getUserInfoUseCase(accessToken).fold(
                        onSuccess = { user ->
                            println("User info retrieved: $user")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isLoggedIn = true,
                                    currentUser = user,
                                )
                            }
                        },
                        onError = {
                            println("获取用户信息失败")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isLoggedIn = false,
                                    currentUser = null,
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    /**
     * 登出用户
     */
    fun logout() {
        viewModelScope.launch {
            logoutUserUseCase()
        }
    }
}